package com.myththewolf.modbot.core.lib.invocation.impl;

import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Implementation of PluginManager
 */
public class ImplPluginLoader implements PluginManager, Loggable {
    /**
     * This map contains all loaded plugins, it is a hashmap so we can grab a plugin by it's name.
     */
    private HashMap<String, BotPlugin> plugins = new HashMap<>();
    /**
     * This map contains all system commands.
     */
    private HashMap<String, SystemCommand> systemCommands = new HashMap<>();

    /**
     * Loads a plugin given a JAR file, and enables it.
     *
     * @param jar A file object pointing to a plugin JAR
     */
    public void loadJarFile(File jar) {
        getLogger().debug("Adding " + jar.getAbsolutePath() + " to this classpath");
        if (!jar.exists()) {
            getLogger().warn(jar.getAbsolutePath() + " does not exist, ignoring.");
            return;
        }
        JSONObject runconfig;
        try {
            runconfig = Util.getResourceFromJar(jar, "runconfig.json").flatMap(Util::inputStreamToString).map(JSONObject::new).orElseThrow(FileNotFoundException::new);
            if (runconfig.isNull("mainClass")) {
                getLogger().warn("Error while enabling plugin: {}, key 'mainClass' in runconfig.json is not optional.", jar.getAbsolutePath());
                return;
            }
            URLClassLoader pluginClassLoader = new PluginClassLoader(getClass().getClassLoader());
            ((PluginClassLoader) pluginClassLoader).loadJarFile(jar);
            Class<?> C = ((PluginClassLoader) pluginClassLoader).getClassByName(runconfig.getString("mainClass"));
            if (runconfig.isNull("pluginName")) {
                getLogger().warn("Error while enabling plugin '{}' : pluginName is NULL", jar.getAbsolutePath());
            }
            String pluginName = runconfig.getString("pluginName");
            if (runconfig.isNull("pluginVersion")) {
                getLogger().warn("Error while enabling plugin '{}' : pluginVersion is NULL", jar.getAbsolutePath());
                return;
            }
            String pluginVersion = runconfig.getString("pluginVersion");
            if (runconfig.isNull("pluginDescription")) {
                getLogger().warn("Error while enabling plugin '{}' : pluginDescription is NULL", jar.getAbsolutePath());
                return;
            }
            String pluginDescription = runconfig.getString("pluginDescription");
            try {
                Object instance = C.newInstance();
                if (!(instance instanceof BotPlugin)) {
                    getLogger().warn("Error while enabling plugin: {}, class '{}' does not extend BotPlugin", jar.getAbsolutePath(), C.getName());
                    return;
                }
                if (!((BotPlugin) instance).getDataFolder().isPresent()) {
                    getLogger().debug("Data folder for plugin '{}' doesn't exist. Making one now.", pluginName);
                    File conf = new File(System.getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + pluginName);
                    conf.mkdir();
                    getLogger().debug("Data folder for plugin '{}' created.", pluginName);
                }
                Optional<JSONObject> pluginConfig = Util.getResourceFromJar(jar, "config.json").flatMap(Util::inputStreamToString).map(JSONObject::new);
                if (!pluginConfig.isPresent()) {
                    getLogger().debug("Found default config for plugin '{}', copying to plugin directory.", pluginName);
                    File conf = new File(System.getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + pluginName + File.separator + "config.json");
                    Util.writeToFile(pluginConfig.get().toString(), conf);
                    getLogger().debug("Default config for plugin '{}' copied.", pluginName);
                } else {
                    getLogger().debug("No default config found for plugin '{}', a new empty config will be assumed.", pluginName);
                    File conf = new File(System.getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + pluginName + File.separator + "config.json");
                    Util.writeToFile(new JSONObject().toString(), conf);
                    getLogger().debug("Empty config generated for plugin '{}'.", pluginName);

                }
                getLogger().info("Enabling plugin: {}", runconfig.getString("pluginName"));
                Thread pluginThread = new Thread(() -> {
                    JSONObject runconfigLamb = Util.getResourceFromJar(jar, "runconfig.json").flatMap(Util::inputStreamToString).map(JSONObject::new).orElseGet(JSONObject::new);
                    ((BotPlugin) instance).enablePlugin(runconfigLamb, pluginClassLoader);
                });
                pluginThread.setName(runconfig.getString("pluginName"));
                pluginThread.start();
                this.plugins.put(((BotPlugin) instance).getPluginName(), ((BotPlugin) instance));
            } catch (InstantiationException | IllegalAccessException e) {
                getLogger().error("Internal Exception while loading plugin: {}, ", jar.getAbsolutePath(), e);
                return;
            }

        } catch (FileNotFoundException e) {
            getLogger().warn("Error while enabling plugin: {}, runconfig.json was not found: ", jar.getAbsolutePath(), e);
        }

    }

    /**
     * Loads all plugins given a directory
     *
     * @param dir A file object pointing to a plugin directory
     */
    public void loadDirectory(File dir) {
        Arrays.stream(dir.listFiles()).filter(file -> file.getName().endsWith(".jar")).forEach(this::loadJarFile);
    }

    @Override
    public List<BotPlugin> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    /**
     * Registers a system command
     *
     * @param trigger The literal command string
     * @param cmd     The executor
     */
    public void registerSystemCommand(String trigger, SystemCommand cmd) {
        systemCommands.put(trigger, cmd);
    }

    /**
     * Gets all system commands
     *
     * @return A list of system commands
     */
    public HashMap<String, SystemCommand> getSystemCommands() {
        return systemCommands;
    }
}
