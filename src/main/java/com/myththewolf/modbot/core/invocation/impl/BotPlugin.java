package com.myththewolf.modbot.core.invocation.impl;

import com.myththewolf.modbot.core.invocation.interfaces.PluginAdapater;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import jdk.nashorn.api.scripting.JSObject;
import org.json.JSONObject;

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
}
