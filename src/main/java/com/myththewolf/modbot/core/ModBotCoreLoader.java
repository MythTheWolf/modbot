package com.myththewolf.modbot.core;

import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * This class is the core of everything, starting all the sub-processes
 */
public class ModBotCoreLoader implements Loggable {
    /**
     * The main method, starts everything
     *
     * @param args Any args to pass to the system (Not currently used)
     */
    public static void main(String[] args) {
        ModBotCoreLoader MBCL = new ModBotCoreLoader();
        MBCL.start();
    }

    public void start() {
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

        } catch (JSONException exception) {
            getLogger().error("Could not read JSON configuration: {}", exception.getMessage());
        }
    }
}
