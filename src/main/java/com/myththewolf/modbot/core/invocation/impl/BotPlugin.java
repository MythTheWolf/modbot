package com.myththewolf.modbot.core.invocation.impl;

import com.myththewolf.modbot.core.invocation.interfaces.PluginAdapater;
import com.myththewolf.modbot.core.lib.logging.Loggable;

/**
 * This class represents a constructed BotPlugin <br />
 * It is abstract, as @link{PluginAdapater#onEnable} and @link{PluginAdapater#onDisable} should be implemented by the plugin developer.
 */
public abstract class BotPlugin implements PluginAdapater,Loggable {
    private String pluginName;
    private String pluginVersion;
    private String pluginDescription;

    /**
     * Gets the description of this plugin set by runconfig.json
     * @return The plugin description
     */
    public String getPluginDescription() {
        return pluginDescription;
    }

    /**
     * Gets the name of this plugin
     * @return The plugin name
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Gets the version string of this plugin
     * @return The version string
     */
    public String getPluginVersionString() {
        return pluginVersion;
    }
}
