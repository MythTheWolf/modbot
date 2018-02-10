package com.myththewolf.modbot.core.lib.invocation.impl;

import com.myththewolf.modbot.core.API.command.impl.DiscordCommand;
import com.myththewolf.modbot.core.API.command.interfaces.CommandExecutor;
import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginAdapater;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import org.json.JSONObject;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
     * Sets up this BotPlugin, it is protected only to the system.
     *
     * @param runconfig The runconfig of the plugin
     * @param loader    The class loader used to import the plugin JAR
     * @throws IllegalStateException If any keys in the runconfig are null
     */
    protected void enablePlugin(JSONObject runconfig, URLClassLoader loader) throws IllegalStateException {
        this.runconfig = runconfig;
        if (runconfig.isNull("pluginName")) {
            throw new IllegalStateException("pluginName is NULL");
        }
        this.pluginName = runconfig.getString("pluginName");
        if (runconfig.isNull("pluginVersion")) {
            throw new IllegalStateException("pluginVersion is NULL");
        }
        this.pluginVersion = runconfig.getString("pluginDescription");
        if (runconfig.isNull("pluginDescription")) {
            throw new IllegalStateException("pluginDescription is NULL");
        }
        this.pluginDescription = runconfig.getString("pluginDescription");
        this.classLoader = loader;
        enabled = true;
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
        this.pluginCommands = new HashMap<>();
        this.enabled = false;
        onDisable();
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
            getLogger().warn("Ignored registration of command '{}' due to plugin '{}' being disabled", trigger, getPluginName());
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
     * Gets this plugin's config in a JSON format
     *
     * @return Optional<JSONObject>; Empty if the config doesn't exist.(Or we don't have permissions to it)
     */
    public Optional<JSONObject> getConfig() {
        File conf = new File(System.getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + getPluginName() + File.separator + "config.json");
        JSONObject config = null;
        if (conf.exists()) {
            config = new JSONObject(Util.readFile(conf).get());
        }
        return Optional.ofNullable(config);
    }

    /**
     * Gets this plugin's data folder in a File object
     *
     * @return Optional<File>; Empty if the data folder doesn't exist.(Or we don't have permissions to it)
     */
    public Optional<File> getDataFolder() {
        File conf = new File(System.getProperty("user.dir") + File.separator + "run" + File.separator + "plugins" + File.separator + getPluginName());
        return Optional.ofNullable(conf.exists() ? conf : null);
    }
}
