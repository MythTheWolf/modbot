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

import com.myththewolf.modbot.core.API.command.impl.DiscordCommand;
import com.myththewolf.modbot.core.API.command.interfaces.CommandExecutor;
import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventHandler;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventType;
import com.myththewolf.modbot.core.lib.plugin.manPage.impl.ManualPageEmbed;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.ManualType;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.PluginManualPage;
import com.myththewolf.modbot.core.lib.plugin.manager.interfaces.PluginAdapater;
import org.javacord.api.DiscordApi;
import org.json.JSONObject;

import java.io.File;
import java.net.URLClassLoader;
import java.util.*;

/**
 * This class represents a constructed BotPlugin <br />
 * It is abstract, as @link{PluginAdapater#onEnable} and @link{PluginAdapater#onDisable} should be implemented by the plugin developer.
 */
public abstract class BotPlugin implements PluginAdapater, Loggable {
    /**
     * The unique plugin name
     */
    private String pluginName;
    /**
     * The plugin version string
     */
    private String pluginVersion;
    /**
     * The plugin description
     */
    private String pluginDescription;
    /**
     * The author of this plugin
     */
    private String pluginAuthor;
    /**
     * The parsed JSONObject of this plugin's run configuration
     */
    private JSONObject runconfig;
    /**
     * The URLClassLoader used to load the JAR into the runtime
     */
    private URLClassLoader classLoader;
    /**
     * Boolean value of if this plugin is enabled
     */
    private boolean enabled = false;
    /**
     * A mapping of Discord commands with their triggers.
     */
    private HashMap<String, DiscordCommand> pluginCommands = new HashMap<>();

    /**
     * A mapping of all this plugin's pluginEvents
     */
    private HashMap<EventType, List<Object>> pluginEvents = new HashMap<>();


    private HashMap<ManualType, List<PluginManualPage>> manualPages = new HashMap<>();

    private List<ManualPageEmbed> manualPageEmbeds = new ArrayList<>();

    private DiscordApi api;

    private File jarFile;
    /**
     * Sets up this BotPlugin, it is protected only to the system.
     *
     * @param runconfig The runconfig of the plugin
     * @param loader    The class loader used to import the plugin JAR
     */
    protected void enablePlugin(JSONObject runconfig, URLClassLoader loader,File selfJar,DiscordApi api) {
        this.runconfig = runconfig;
        this.pluginName = runconfig.getString("pluginName");
        this.pluginVersion = runconfig.getString("pluginVersion");
        this.pluginDescription = runconfig.getString("pluginDescription");
        this.pluginAuthor = runconfig.getString("pluginAuthor");
        this.classLoader = loader;
        this.jarFile = selfJar;
        enabled = true;
        for (EventType I : EventType.values()) {
            this.pluginEvents.put(I, new ArrayList<>());
        }
        for (ManualType I : ManualType.values()) {
            this.manualPages.put(I, new ArrayList<>());
        }
        this.api = api;
        onEnable();
    }

    /**
     * Gets the description of this plugin set by runconfig.json
     *
     * @return The plugin description
     */
    public String getPluginDescription() {
        return pluginDescription;
    }

    /**
     * Gets the name of this plugin
     *
     * @return The plugin name
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Gets the version string of this plugin
     *
     * @return The version string
     */
    public String getPluginVersionString() {
        return pluginVersion;
    }

    /**
     * Gets the author of this plugin
     *
     * @return The plugin author
     */
    public String getPluginAuthor() {
        return pluginAuthor;
    }

    /**
     * Gets this plugin's runconfig
     *
     * @return The parsed JSON of this plugin's runconfig
     */
    public JSONObject getRunconfig() {
        return runconfig;
    }

    /**
     * Sets this plugin as disabled, unregistering all commands and events. Also invokes @link{BotPlugin#onDisable}
     */
    public void disablePlugin() {
        getLogger().debug("Disabling plugin: {}",getPluginName());
        this.pluginCommands = new HashMap<>();
        this.pluginEvents = new HashMap<>();
        this.enabled = false;
        onDisable();
    }
    public void enablePlugin(){
        getLogger().debug("Enabling plugin: {}",getPluginName());
        this.pluginCommands = new HashMap<>();
        this.pluginEvents = new HashMap<>();
        this.enabled = true;
        onEnable();
    }
    /**
     * Returns the class loader of this plugin
     *
     * @return The class Loader
     */
    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Registers a command to this plugin
     *
     * @param trigger  The string literal to trigger this command
     * @param executor The executor to invoke upon the command trigger
     * @apiNote This method will fail if this plugin is disabled.
     */
    public void registerCommand(String trigger, CommandExecutor executor) {
        if (!enabled) {
            getLogger()
                    .warn("Ignored registration of command '{}' due to plugin '{}' being disabled", trigger, getPluginName());
            return;
        }
        this.pluginCommands.put(trigger, new DiscordCommand(this, executor, trigger));
    }

    /**
     * Gets all commands of this plugin
     *
     * @return The list of commands
     */
    public List<DiscordCommand> getCommands() {
        return new ArrayList<>(this.pluginCommands.values());
    }

    /**
     * Gets the full map of plugin commands
     *
     * @return A new HashMap identical to this plugin's command map
     */
    public HashMap<String, DiscordCommand> getCommandMap() {
        return new HashMap<>(pluginCommands);
    }

    /**
     * Gets all events of this plugin
     *
     * @return The List of events
     */
    public List<Object> getEvents() {
        List<Object> finalList = new ArrayList<>();
        this.pluginEvents.entrySet().stream().map(Map.Entry::getValue).forEach(finalList::addAll);
        return finalList;
    }


    /**
     * Gets this plugin's config in a JSON format
     *
     * @return Optional<JSONObject>; Empty if the config doesn't exist.(Or we don't have permissions to it)
     */
    public Optional<JSONObject> getConfig() {
        File conf = new File(System
                .getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + getPluginName() + File.separator + "config.json");
        JSONObject config = null;
        if (conf.exists()) config = new JSONObject(Util.readFile(conf).get());
        return Optional.ofNullable(config);
    }

    /**
     * Gets this plugin's data folder in a File object
     *
     * @return Optional<File>; Empty if the data folder doesn't exist.(Or we don't have permissions to it)
     */
    public Optional<File> getDataFolder() {
        File conf = new File(System
                .getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + getPluginName());
        return Optional.ofNullable(conf.exists() ? conf : null);
    }

    /**
     * Registers a event to this plugin
     *
     * @param event A object which contains a EventHandler annotated method
     */
    public void registerEvent(Object event) {
        Optional<EventType> optionalEventType = findEventType(event);
        if (!optionalEventType.isPresent()) {
            getLogger().warn("Could not register event '{}' to plugin '{}'; No valid event handlers found.");
            return;
        }
        getLogger().debug("Mapped object '{}' to event type '{}'", event.getClass().getName(), optionalEventType.get()
                .toString());
        List<Object> oldEventList = this.pluginEvents.get(optionalEventType.get());
        oldEventList.add(event);
        this.pluginEvents.put(optionalEventType.get(), oldEventList);
        getLogger().debug("Registered event type of {}:{} to plugin '{}'", optionalEventType.get().toString(), event
                .getClass().getName(), getPluginName());
    }

    /**
     * Finds the EventType given the runner object
     *
     * @param event The event runner
     * @return An Optional<EventType>, empty if nothing was found
     */
    private Optional<EventType> findEventType(Object event) {
        Class<?> parameterClass = Arrays.stream(event.getClass().getMethods())
                .filter(method -> (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1))
                .flatMap(method -> Arrays
                        .stream(method.getParameterTypes())).findFirst().orElse(null);
        if (parameterClass == null) {
            return Optional.empty();
        }
        return Arrays.stream(EventType.values()).filter(eventType -> eventType.getDataClass().equals(parameterClass))
                .findAny();
    }

    /**
     * Gets a list of events with the desired type
     *
     * @param type The Event type
     * @return The list of events
     */
    public List<Object> getEventsOfType(EventType type) {
        return this.pluginEvents.get(type);
    }

    public File getJarFile() {
        return jarFile;
    }

    /**
     * Gets a list of plugin manuals of the given type
     *
     * @param type The type
     * @return A list of manual entries
     */
    public List<PluginManualPage> getManuasOfType(ManualType type) {
        return manualPages.get(type);
    }

    /**
     * Updates the list of manual pages given the type
     *
     * @param type  The type of manual
     * @param pages The collection of pages
     */
    public void updateManualList(ManualType type, List<PluginManualPage> pages) {
        this.manualPages.put(type, new ArrayList<>(pages));
    }

    /**
     * Gets all manuals of all types
     * @return The manual list
     */
    public List<PluginManualPage> getManuals() {
        List<PluginManualPage> finalList = new ArrayList<>();
        this.manualPages.entrySet().stream().map(Map.Entry::getValue).forEach(finalList::addAll);
        return finalList;
    }
    public void saveConfig(JSONObject root){
        File conf = new File(getDataFolder().get().getAbsolutePath()+File.separator+"config.json");
        Util.writeToFile(root.toString(4),conf);
    }
    public boolean isEnabled() {
        return enabled;
    }

    public DiscordApi getDiscordAPI(){
        return api;
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof BotPlugin && ((BotPlugin) obj).getClassLoader().equals(getClassLoader());
    }
}

