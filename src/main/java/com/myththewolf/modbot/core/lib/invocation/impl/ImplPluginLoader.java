package com.myththewolf.modbot.core.lib.invocation.impl;

import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import org.json.JSONObject;

import java.io.*;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of PluginManager
 */
public class ImplPluginLoader implements PluginManager, Loggable {
    /**
     * This map contains all loaded plugins, it is a hashmap so we can grab a plugin by it's name.
     */
    private HashMap<String, BotPlugin> plugins = new HashMap<>();

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
            try {
                Object instance = C.newInstance();
                if (!(instance instanceof BotPlugin)) {
                    getLogger().warn("Error while enabling plugin: {}, class '{}' does not extend BotPlugin", jar.getAbsolutePath(), C.getName());
                    return;
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
}
