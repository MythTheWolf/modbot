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


import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.command.CommandListener;
import com.myththewolf.modbot.core.lib.plugin.invocation.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.lib.plugin.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.lib.plugin.manPage.impl.ManualPageReactionListner;
import com.myththewolf.modbot.core.systemPlugin.commands.help;
import com.myththewolf.modbot.core.systemPlugin.commands.info;
import com.myththewolf.modbot.core.systemPlugin.commands.plugin;
import com.myththewolf.modbot.core.systemPlugin.commands.shutdown;
import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is the core of everything, starting all the sub-processes
 */
public class ModBotCoreLoader implements Loggable {
    public static Logger SYSTEM_LOGGER;
    private static boolean withoutBot;
    public static LocalDateTime START;
    private JSONObject runConfig;
    public static String COMMAND_KEY;
    /**
     * The main method, starts everything
     *
     * @param args Any args to pass to the system (Not currently used)
     */
    public static void main(String[] args) {
        withoutBot = Arrays.asList(args).contains("--nobot");
        ModBotCoreLoader MBCL = new ModBotCoreLoader();
        MBCL.start(Arrays.asList(args));
    }

    /**
     * Starts everything in the following order:
     *  - Look for needed dirs (run, run/plugins) and create if needed <br />
     *  - Load a parse run/runconfig.json; Create if needed <br />
     *  - Start discord bot
     *  - Load plugins & enable plugins
     *  - Register system commands
     */
    public void start(List<String> args) {
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
                Util.writeToFile(defaultConfig.toString(), systemconfig);
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
                if(!runConfig.has(key)) {
                    if(args.contains("--fix-config")){
                        getLogger().debug("Adding key '{}' with the value of '{}'",key,defaultConfig.get(key).toString());
                        runConfig.put(key,defaultConfig.get(key));
                        Util.writeToFile(runConfig.toString(), systemconfig);
                    }else {
                        getLogger()
                                .warn("Your run configuration is out of date! Restart the program with --fix-config to update your config.");
                        System.exit(0);
                    }
                }
            });
            try {
                if(runConfig.getString("command-frontend").isEmpty()){
                    getLogger().warn("Config option 'command-frontend' is empty, using default command trigger '~$'");
                    COMMAND_KEY = "~$";
                }else{
                    COMMAND_KEY = runConfig.getString("command-frontend");
                }

                DiscordApi discordApi = null;
                if (!ModBotCoreLoader.withoutBot) {
                    getLogger().info("Starting discord bot");
                    if(runConfig.isNull("botType") || !runConfig.getString("botType").equals("CLIENT")) {
                        discordApi = new DiscordApiBuilder().setAccountType(AccountType.BOT).setToken(runConfig.getString("botToken")).login().get();
                        discordApi.updateActivity("for commands: "+COMMAND_KEY+"help",ActivityType.WATCHING);
                    }else{
                        getLogger().warn("****YOU ARE USING A CLIENT TOKEN!****");
                        getLogger().warn("This is not advised and it can get you banned!");
                        discordApi = new DiscordApiBuilder().setAccountType(AccountType.CLIENT).setToken(runConfig.getString("botToken")).login().get();
                    }
                    getLogger().info("Logged in. Loading plugins.");
                } else {
                    getLogger().debug("Loading plugins without bot");
                }


                PluginManager PM = new ImplPluginLoader();
                PM.loadDirectory(plugins);
                if (ModBotCoreLoader.withoutBot) {
                    return;
                }
                getLogger().info("Registering System commands");
                ((ImplPluginLoader) PM).registerSystemCommand("mb.info", new info(PM));
                ((ImplPluginLoader) PM).registerSystemCommand("mb.plugin", new plugin(PM,this));
                ((ImplPluginLoader) PM).registerSystemCommand("mb.man",new help(PM));
                ((ImplPluginLoader) PM).registerSystemCommand("mb.shutdown",new shutdown(PM,this));
                discordApi.addMessageCreateListener(new CommandListener(PM));
                discordApi.addReactionAddListener(new ManualPageReactionListner(PM));
            } catch (Exception e) {
                getLogger().error("Exception in main thread:");
                e.printStackTrace();
                System.exit(0);
                return;
            }
            LocalDateTime END= LocalDateTime.now();

            getLogger().info("System up! (Took {}ms)",Duration.between(START,END).toMillis());
        } catch (JSONException exception) {
            getLogger().error("Could not read JSON configuration: {}", exception.getMessage());
        }
    }

    public JSONObject getRunConfig() {
        return runConfig;
    }
}
