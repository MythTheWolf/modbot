/*
 * Copyright (c) 2018 MythTheWolf
 *  Nicholas Agner, USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.myththewolf.modbot.core;


import com.myththewolf.modbot.core.API.command.impl.DiscordCommand;
import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.command.CommandListener;
import com.myththewolf.modbot.core.lib.plugin.event.impl.ImboundCommandEvent;
import com.myththewolf.modbot.core.lib.plugin.event.impl.UserCommandEvent;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventHandler;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventType;
import com.myththewolf.modbot.core.lib.plugin.manPage.impl.ManualPageReactionListner;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.lib.plugin.manager.interfaces.PluginManager;
import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class is the core of everything, starting all the sub-processes
 */
public class MyriadBotLoader implements Loggable {
    public static Logger SYSTEM_LOGGER;
    public static LocalDateTime START;
    public static String COMMAND_KEY;
    public static LineReader lineReader;
    private static boolean withoutBot;
    private static PluginManager PM;
    private static boolean isValidCommand = false;
    private JSONObject runConfig;

    /**
     * The main method, starts everything
     *
     * @param args Any args to pass to the system (Not currently used)
     */
    public static void main(String[] args) {
        withoutBot = Arrays.asList(args).contains("--nobot");
        MyriadBotLoader MBCL = new MyriadBotLoader();
        MBCL.start(Arrays.asList(args));
    }

    private static BotPlugin runnerToBotPlugin(Object runner) {
        return PM.getPlugins().stream().filter((BotPlugin plugin) -> plugin.getEvents().stream()
                .anyMatch(o -> o.getClass().getName().equals(runner.getClass().getName())))
                .findFirst().orElse(null);
    }

    /**
     * Starts everything in the following order:
     * - Start logger </b>
     * - Look for needed dirs (run, run/plugins) and create if needed <br />
     * - Load a parse run/runconfig.json; Create if needed <br />
     * - Start discord bot
     * - Load plugins & enable plugins
     * - Register system commands
     */
    public void start(List<String> args) {
        Thread jLine = new Thread(() -> {
            try {
                Terminal terminal = TerminalBuilder.terminal();
                lineReader = LineReaderBuilder.builder()
                        .terminal(terminal).build();
                String line;
                do {
                    line = lineReader.readLine(">", null);
                    String[] split = line.split(" ");

                }
                while (line != null && line.length() > 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        jLine.start();
        final JSONObject defaultConfig = Util.inputStreamToString(getClass().getResourceAsStream("/run.json")).map(JSONObject::new).orElseThrow(() -> new InvalidParameterException("Got empty optional while trying to read the internal default config."));
        START = LocalDateTime.now();
        SYSTEM_LOGGER = getLogger();
        Thread.currentThread().setName("System");
        File current = new File(System.getProperty("user.dir") + File.separator + "run");
        File plugins = new File(current.getAbsolutePath() + File.separator + "plugins");
        File systemconfig = new File(current.getAbsolutePath() + File.separator + "run.json");
        getLogger().info("Loading system from working directory: {}", current.getAbsolutePath());
        if (!current.exists()) {
            getLogger().warn("Run dir doesn't exist, making one for you.");
            current.mkdir();
        }
        if (!plugins.exists()) {
            getLogger().warn("Plugins dir doesn't exist, making one for you.");
            plugins.mkdir();
        }
        if (!systemconfig.exists()) {
            getLogger().warn("No run.json, copying default one now.");
            try {
                systemconfig.createNewFile();
                Util.writeToFile(defaultConfig.toString(4), systemconfig);
            } catch (InvalidParameterException | IOException exception) {
                getLogger().error("Could not copy default config from jar: {} ", exception.getMessage());
                getLogger().error("Fatal error, terminating.");
                return;
            }
        }
        try {
            getLogger().info("Reading and checking configuration");
            runConfig = Util.readFile(systemconfig).map(JSONObject::new).orElseThrow(() -> new JSONException("Input was empty"));
            defaultConfig.keySet().iterator().forEachRemaining(key -> {
                if (!runConfig.has(key)) {
                    if (args.contains("--fix-config")) {
                        getLogger().debug("Adding key '{}' with the value of '{}'", key, defaultConfig.get(key).toString());
                        runConfig.put(key, defaultConfig.get(key));
                        Util.writeToFile(runConfig.toString(4), systemconfig);
                    } else {
                        getLogger()
                                .warn("Your run configuration is out of date! Restart the program with --fix-config to update your config.");
                        System.exit(0);
                    }
                }
            });
            try {
                if (runConfig.getString("command-frontend").isEmpty()) {
                    getLogger().warn("Config option 'command-frontend' is empty, using default command trigger '~$'");
                    COMMAND_KEY = "~$";
                } else {
                    COMMAND_KEY = runConfig.getString("command-frontend");
                }

                DiscordApi discordApi = null;
                if (!MyriadBotLoader.withoutBot) {
                    getLogger().info("Starting discord bot");
                    if (runConfig.isNull("botType") || !runConfig.getString("botType").equals("CLIENT")) {
                        discordApi = new DiscordApiBuilder().setAccountType(AccountType.BOT).setToken(runConfig.getString("botToken")).login().join();
                        discordApi.updateActivity(ActivityType.WATCHING, "for commands: " + COMMAND_KEY + "man");
                    } else {
                        getLogger().warn("****YOU ARE USING A CLIENT TOKEN!****");
                        getLogger().warn("This is not advised and it can get you banned!");
                        discordApi = new DiscordApiBuilder().setAccountType(AccountType.CLIENT).setToken(runConfig.getString("botToken")).login().join();
                    }
                    getLogger().info("Logged in. Loading plugins.");
                } else {
                    getLogger().debug("Loading plugins without bot");
                }

                PM = new ImplPluginLoader(discordApi);
                PM.loadDirectory(plugins);
                if (MyriadBotLoader.withoutBot) {
                    return;
                }
                discordApi.addMessageCreateListener(new CommandListener(PM));
                discordApi.addReactionAddListener(new ManualPageReactionListner(PM));
            } catch (Exception e) {
                getLogger().error("Exception in main thread:");
                e.printStackTrace();
                System.exit(0);
                return;
            }
            LocalDateTime END = LocalDateTime.now();

            getLogger().info("System up! (Took {}ms)", Duration.between(START, END).toMillis());
        } catch (JSONException exception) {
            getLogger().error("Could not read JSON configuration: {}", exception.getMessage());
        }
    }

    public JSONObject getRunConfig() {
        return runConfig;
    }

    private void handleJLine(String in) {
        if (!in.startsWith(MyriadBotLoader.COMMAND_KEY)) {
            return;
        }

        Thread.currentThread().setName("Events");
        String[] content = in.split(" ");
        content[0] = content[0].substring(MyriadBotLoader.COMMAND_KEY.length());


        PM.getPlugins().stream().map(BotPlugin::getCommands).flatMap(List::stream)
                .filter((DiscordCommand cmd) -> (cmd.getTrigger().equals(content[0]) || content[0]
                        .equals(cmd.getParentPlugin().getPluginName() + ":" + cmd.getTrigger()))
                ).forEachOrdered(discordCommand -> {
            ImboundCommandEvent commandEvent = new ImboundCommandEvent(null, discordCommand, null);
            PM.getPlugins().stream().flatMap(plugin -> plugin.getEventsOfType(EventType.IMBOUND_COMMAND).stream()).forEachOrdered(runner -> {
                Optional<Method> methodOptional = Arrays.stream(runner.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(EventHandler.class)).findAny();
                if (!methodOptional.isPresent()) {
                    getLogger()
                            .warn("Could not pass event of type IMBOUND_COMMAND to class '{}', no runner method found", runner
                                    .getClass().getName());
                } else try {
                    methodOptional.get()
                            .invoke(runner, commandEvent, runnerToBotPlugin(runner));
                } catch (Exception e) {
                    getLogger()
                            .error("Could not pass event of type IMBOUND_COMMAND to class '{}': Internal error! (Our fault): {}", runner
                                    .getClass().getName(), e.getMessage());
                }
            });
            if (!commandEvent.isCancelled()) {
                discordCommand
                        .invokeCommand(in);
                isValidCommand = true;
            }
        });
        if (isValidCommand) {
            PM.getPlugins().stream().flatMap(plugin -> plugin.getEventsOfType(EventType.COMMAND_RUN).stream())
                    .forEach(runner -> {
                        Optional<Method> methodOptional = Arrays.stream(runner.getClass().getMethods())
                                .filter(method -> method.isAnnotationPresent(EventHandler.class)).findAny();
                        if (!methodOptional.isPresent()) {
                            getLogger()
                                    .warn("Could not pass event of type CONSOLE_COMMAND_RUN to class '{}', no runner method found", runner
                                            .getClass().getName());
                        } else {
                            try {
                                methodOptional.get()
                                        .invoke(runner, new UserCommandEvent(PM, null, runnerToBotPlugin(runner)));
                            } catch (Exception e) {
                                getLogger()
                                        .error("Could not pass event of type CONSOLE_COMMAND_RUN to class '{}': Internal error! (Our fault): {}", runner
                                                .getClass().getName(), e.getMessage());
                            }
                        }
                    });
        }
        if (!isValidCommand) {
            getLogger().info("\u001b[31mCommand not found.");
        }
    }
}
