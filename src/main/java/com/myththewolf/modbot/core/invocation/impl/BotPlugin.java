package com.myththewolf.modbot.core.invocation.impl;

import com.myththewolf.modbot.core.invocation.interfaces.PluginAdapater;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import jdk.nashorn.api.scripting.JSObject;
import org.json.JSONObject;

import java.net.URLClassLoader;
import java.util.UUID;

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
     * Sets up this BotPlugin, it is protected only to the system.
     * @param runconfig The runconfig of the plugin
     * @param loader The class loader used to import the plugin JAR
     * @throws IllegalStateException If any keys in the runconfig are null
     */
    protected void initate(JSONObject runconfig, URLClassLoader loader) throws IllegalStateException {
        this.runconfig = runconfig;
        if (runconfig.isNull("pluginName")) {
            throw new IllegalStateException("pluginName is NULL");
        }
        this.pluginName = runconfig.getString("plugin-name");
        if (runconfig.isNull("pluginVersion")) {
            throw new IllegalStateException("pluginVersion is NULL");
        }
        this.pluginVersion = runconfig.getString("pluginDescription");
        if (runconfig.isNull("pluginDescription")) {
            throw new IllegalStateException("pluginDescription is NULL");
        }
        this.pluginDescription = runconfig.getString("pluginDescription");
        this.classLoader = loader;

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
}
