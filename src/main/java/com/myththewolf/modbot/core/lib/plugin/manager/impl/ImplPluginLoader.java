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

package com.myththewolf.modbot.core.lib.plugin.manager.impl;

import com.myththewolf.modbot.core.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.event.impl.PluginEnableEvent;
import com.myththewolf.modbot.core.lib.plugin.manPage.CommandUsage.ArgumentType;
import com.myththewolf.modbot.core.lib.plugin.manPage.CommandUsage.ImplCommandUsageManual;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.ManualType;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.PluginManualPage;
import com.myththewolf.modbot.core.lib.plugin.manager.interfaces.PluginManager;
import org.javacord.api.DiscordApi;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    private DiscordApi api;

    public ImplPluginLoader(DiscordApi api) {
        this.api = api;
    }

    /**
     * Copies a directory from a jar file to an external directory.
     */
    public static void copyResourcesToDirectory(JarFile fromJar, String jarDir, String destDir)
            throws IOException {
        for (Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(jarDir + "/") && !entry.isDirectory()) {
                File dest = new File(destDir + "/" + entry.getName().substring(jarDir.length() + 1));
                File parent = dest.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                FileOutputStream out = new FileOutputStream(dest);
                InputStream in = fromJar.getInputStream(entry);

                try {
                    byte[] buffer = new byte[8 * 1024];

                    int s = 0;
                    while ((s = in.read(buffer)) > 0) {
                        out.write(buffer, 0, s);
                    }
                } catch (IOException e) {
                    throw new IOException("Could not copy asset from jar file", e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                    try {
                        out.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

    }

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
            PluginClassLoader pluginClassLoader = new PluginClassLoader(getClass().getClassLoader());
            pluginClassLoader.loadJarFile(jar);
            Class<?> C = pluginClassLoader.getClassByName(runconfig.getString("mainClass"));
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
                getLogger().info("Enabling plugin: {}", runconfig.getString("pluginName"));
                Thread pluginThread = new Thread(() -> {
                    Thread.currentThread().setName(runconfig.getString("pluginName"));
                    JSONObject runconfigLamb = Util.getResourceFromJar(jar, "runconfig.json")
                            .flatMap(Util::inputStreamToString).map(JSONObject::new).orElseGet(JSONObject::new);
                    ((BotPlugin) instance).enablePlugin(runconfigLamb, pluginClassLoader, jar, this, api);
                    ((BotPlugin) instance).saveConfig(((BotPlugin) instance).getConfig().orElseThrow(IllegalStateException::new));
                });
                CompletableFuture.runAsync(pluginThread).whenComplete((aVoid, throwable) -> {
                    try {
                        copyResourcesToDirectory(new JarFile(jar), "manPages", manDir.getAbsolutePath());
                    } catch (Exception e) {
                        getLogger()
                                .warn("Could not copy manuals for plugin '{}' outside of JAR: {}", pluginName, e.getMessage());
                    }
                    Arrays.stream(manDir.listFiles()).filter(file -> file.getName().endsWith(".json")).forEach(file -> {
                        JSONObject parsedManual = new JSONObject(Util.readFile(file).get());
                        switch (parsedManual.getString("type")) {
                            case "COMMAND_SYNTAX":
                                boolean ok = true;
                                String badType = "";
                                Iterator I = parsedManual.getJSONArray("arguments").iterator();
                                while (I.hasNext()) {
                                    JSONObject ob = (JSONObject) I.next();
                                    ok = isValidArguemtnType(ob.getString("type"));
                                    badType = ob.getString("type");
                                    if (!ok)
                                        break;
                                }
                                if (!((BotPlugin) instance).getCommandMap().containsKey(parsedManual.getString("for"))) {
                                    getLogger()
                                            .warn("Could not enable manual for command '{}', target command doesn't exist.", parsedManual
                                                    .getString("for"));
                                    break;
                                } else if (!ok) {
                                    getLogger()
                                            .warn("Could not enable manual for command '{}': '{}' is not a valid argument type.", parsedManual
                                                    .getString("for"), badType);
                                    break;
                                } else {
                                    addManual(((BotPlugin) instance), ManualType.COMMAND_SYNTAX, parsedManual);
                                    break;
                                }
                            default:
                                getLogger().warn("Could not enable manual '{}': Invalid manual type '{}'.", file.getAbsolutePath(), parsedManual.getString("type"));
                                break;
                        }
                    });
                    this.plugins.put(runconfig.getString("pluginName"), ((BotPlugin) instance));
                    PluginEnableEvent enableEvent = new PluginEnableEvent((BotPlugin) instance);
                    Util.fireEvent(enableEvent);
                    if (enableEvent.isCancelled()) {
                        ((BotPlugin) instance).disablePlugin();
                    }
                });

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


    public void addManual(BotPlugin plugin, ManualType type, JSONObject data) {
        List<PluginManualPage> oldPluginManualPageList = plugin.getManuasOfType(type);
        switch (type) {
            case COMMAND_SYNTAX:
                oldPluginManualPageList.add(new ImplCommandUsageManual(this, plugin, data));
                plugin.updateManualList(type, oldPluginManualPageList);
                break;
            default:
                break;
        }
    }

    public void reloadPlugin(BotPlugin plugin) {
        getLogger().debug("Unloading plugin '{}' from memory", plugin.getPluginName());
        File jar = plugin.getJarFile();
        plugin.onDisable();
        try {
            plugin.getClassLoader().close();
            plugins.remove(plugin.getPluginName());
            loadJarFile(jar);
        } catch (IOException e) {
            getLogger().warn(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deletePlugin(BotPlugin plugin) {
        boolean deleted = deleteFolder(plugin.getDataFolder().orElseThrow(IllegalStateException::new));
        if (!deleted) {
            getLogger().warn("Could not delete folder: {}", plugin.getDataFolder().get().getAbsolutePath());
        }
        boolean delJar = plugin.getJarFile().delete();
        if (!delJar) {
            getLogger().warn("Could not delete jar file: {}", plugin.getJarFile().getAbsolutePath());
        }
        plugins.remove(plugin.getPluginName());
    }

    boolean isValidArguemtnType(String in) {
        try {
            ArgumentType argumentType = ArgumentType.valueOf(in);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        return folder.delete();
    }
}
