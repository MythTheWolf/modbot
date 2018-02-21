package com.myththewolf.modbot.core;


import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.command.CommandListener;
import com.myththewolf.modbot.core.lib.plugin.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.lib.plugin.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.commands.disablePlugin;
import com.myththewolf.modbot.core.systemPlugin.commands.info;
import com.myththewolf.modbot.core.systemPlugin.commands.plugin;
import de.btobastian.javacord.AccountType;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.DiscordApiBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * This class is the core of everything, starting all the sub-processes
 */
public class ModBotCoreLoader implements Loggable {
    public static Logger SYSTEM_LOGGER;
    private static boolean withoutBot;

    /**
     * The main method, starts everything
     *
     * @param args Any args to pass to the system (Not currently used)
     */
    public static void main(String[] args) {
        withoutBot = Arrays.asList(args).contains("--nobot");
        ModBotCoreLoader MBCL = new ModBotCoreLoader();
        MBCL.start();
    }

    /**
     * Starts everything in the following order:
     *  - Look for needed dirs (run, run/plugins) and create if needed <br />
     *  - Load a parse run/runconfig.json; Create if needed <br />
     *  - Start discord bot
     *  - Load plugins & enable plugins
     *  - Register system commands
     */
    public void start() {
        LocalDateTime START = LocalDateTime.now();
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
                String defaultConfig = Util.inputStreamToString(getClass().getResourceAsStream("/run.json")).orElseThrow(() -> new InvalidParameterException("Got empty optional while trying to read the internal default config."));
                systemconfig.createNewFile();
                Util.writeToFile(defaultConfig, systemconfig);
            } catch (InvalidParameterException | IOException exception) {
                getLogger().error("Could not copy default config from jar: {} ", exception.getMessage());
                getLogger().error("Fatal error, terminating.");
                return;
            }
        }
        try {
            getLogger().info("Reading configuration");
            JSONObject theDealio = Util.readFile(systemconfig).map(JSONObject::new).orElseThrow(() -> new JSONException("Input was empty"));


            try {
                DiscordApi discordApi = null;
                if (!ModBotCoreLoader.withoutBot) {
                    getLogger().info("Starting discord bot");
                    if(theDealio.isNull("botType") || !theDealio.getString("botType").equals("CLIENT")) {
                        discordApi = new DiscordApiBuilder().setAccountType(AccountType.BOT).setToken(theDealio.getString("botToken")).login().get();
                    }else{
                        getLogger().warn("****YOU ARE USING A CLIENT TOKEN!****");
                        getLogger().warn("This is not advised and it can get you banned!");
                        discordApi = new DiscordApiBuilder().setAccountType(AccountType.CLIENT).setToken(theDealio.getString("botToken")).login().get();
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
                ((ImplPluginLoader) PM).registerSystemCommand(">info", new info(PM));
                ((ImplPluginLoader) PM).registerSystemCommand(">pldisable", new disablePlugin(PM));
                ((ImplPluginLoader) PM).registerSystemCommand(">plugin", new plugin(PM));
                discordApi.addMessageCreateListener(new CommandListener(PM));
            } catch (Exception e) {
                getLogger().error("Login failed. Exiting.");
                System.exit(0);
                return;
            }
            LocalDateTime END= LocalDateTime.now();

            getLogger().info("System up! (Took {}ms",Duration.between(START,END).toMillis());
        } catch (JSONException exception) {
            getLogger().error("Could not read JSON configuration: {}", exception.getMessage());
        }
    }
}
