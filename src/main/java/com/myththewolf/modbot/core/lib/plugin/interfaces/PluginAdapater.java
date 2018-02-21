package com.myththewolf.modbot.core.lib.plugin.interfaces;

/**
 * This interface is used to blueprint the BotPlugin class
 */
public interface PluginAdapater {
    /**
     * This method is ran the extending plugin is enabled
     */
    void onEnable();

    /**
     * This method is ran when the extending plugin is disabled
     */
    void onDisable();
}
