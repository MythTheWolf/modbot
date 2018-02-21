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

package com.myththewolf.modbot.core.lib.plugin.impl;

import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

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
            runconfig = Util.getResourceFromJar(jar, "runconfig.json").flatMap(Util::inputStreamToString)
                    .map(JSONObject::new).orElseThrow(FileNotFoundException::new);
            getLogger().debug("Reading plugin meta for jar: {}", jar.getAbsolutePath());
            if (runconfig.isNull("mainClass")) {
                getLogger()
                        .warn("Error while enabling plugin: {}, key 'mainClass' in runconfig.json is not optional.", jar
                                .getAbsolutePath());
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

            if (runconfig.isNull("pluginAuthor")) {
                getLogger().warn("Error while enabling plugin '{}' : pluginAuthor is NULL", jar.getAbsolutePath());
                return;
            }
            try {
                Object instance = C.newInstance();
                if (!(instance instanceof BotPlugin)) {
                    getLogger().warn("Error while enabling plugin: {}, class '{}' does not extend BotPlugin", jar
                            .getAbsolutePath(), C.getName());
                    return;
                }
                File dataFolder = new File(System
                        .getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + pluginName);
                if (!dataFolder.exists()) {
                    getLogger().debug("Data folder for plugin '{}' doesn't exist. Making one now.", pluginName);
                    File conf = new File(System
                            .getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + pluginName);
                    conf.mkdir();
                    getLogger().debug("Data folder for plugin '{}' created.", pluginName);
                }
                File manDir = new File(dataFolder.getAbsolutePath() + File.separator + "manuals");
                if (!manDir.exists()) {
                    getLogger().debug("Manual page folder for plugin '{}' doesn't exist. Making one now.", pluginName);
                    manDir.mkdir();
                    getLogger().debug("Manual page folder for plugin '{}' created.", pluginName);
                }


                JSONObject cnfg;
                try {
                    cnfg = Util.getResourceFromJar(jar, "config.json").flatMap(Util::inputStreamToString)
                            .map(JSONObject::new).orElseThrow(FileNotFoundException::new);
                } catch (FileNotFoundException e) {
                    cnfg = null;
                }

                Optional<JSONObject> pluginConfig = Optional.ofNullable(cnfg);
                File missingConfMaybe = new File(dataFolder.getAbsolutePath() + File.separator + "config.json");
                if (pluginConfig.isPresent() && !missingConfMaybe.exists()) {
                    getLogger().debug("Found default config for plugin '{}', copying to plugin directory.", pluginName);
                    File conf = new File(System
                            .getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + pluginName + File.separator + "config.json");
                    Util.writeToFile(pluginConfig.get().toString(), conf);
                    getLogger().debug("Default config for plugin '{}' copied.", pluginName);
                } else if (!missingConfMaybe.exists()) {
                    getLogger()
                            .debug("No default config found for plugin '{}', a new empty config will be assumed.", pluginName);
                    getLogger().debug("Creating new blank file.");
                    File conf = new File(dataFolder.getAbsolutePath() + File.separator + "config.json");
                    try {
                        conf.createNewFile();
                    } catch (IOException exception) {
                        getLogger()
                                .error("Could not create new config file for plugin '{}', cancelling plugin enable.", pluginName);
                        getLogger().error("Error message: {}", exception.getMessage());
                        return;
                    }
                    Util.writeToFile(new JSONObject().toString(), conf);
                    getLogger().debug("Empty config generated for plugin '{}'.", pluginName);
                } else if (missingConfMaybe.exists()) {
                    getLogger().debug("Config file for plugin '{}' found on disk, not making new one.", pluginName);
                }
                URL manPages = getClass().getResource("man-pages");
                try {
                    File manualPageFile = new File(manPages.toURI());
                    List<File> manuals = Arrays.stream(manualPageFile.listFiles())
                            .filter(file -> file.getName().endsWith(".json")).collect(Collectors.toList());
                    manuals.stream().map(Util::readFile).map(JSONObject::new).forEach(parsedManual -> {

                    });
                } catch (URISyntaxException excepion) {
                    getLogger().warn("Could not find manual page folder in plugin: {}", pluginName);
                }
                getLogger().info("Enabling plugin: {}", runconfig.getString("pluginName"));
                Thread pluginThread = new Thread(() -> {
                    JSONObject runconfigLamb = Util.getResourceFromJar(jar, "runconfig.json")
                            .flatMap(Util::inputStreamToString).map(JSONObject::new).orElseGet(JSONObject::new);
                    ((BotPlugin) instance).enablePlugin(runconfigLamb, pluginClassLoader);
                });
                pluginThread.setName(runconfig.getString("pluginName"));
                pluginThread.start();
                this.plugins.put(runconfig.getString("pluginName"), ((BotPlugin) instance));
            } catch (InstantiationException | IllegalAccessException e) {
                getLogger().error("Internal Exception while loading plugin: {}, ", jar.getAbsolutePath(), e);
                return;
            }

        } catch (FileNotFoundException e) {
            getLogger()
                    .warn("Error while enabling plugin: {}, runconfig.json was not found: ", jar.getAbsolutePath(), e);
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
        return new ArrayList<BotPlugin>(this.plugins.values());
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
