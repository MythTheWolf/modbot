package com.myththewolf.modbot.core.lib.invocation.interfaces;

/**
 * This interface is used to blueprint the BotPlugin class
 */
public interface PluginAdapater {
    /**
     * This method is ran the extending plugin is enabled
     */
    public void onEnable();

    /**
     * This method is ran when the extending plugin is disabled
     */
    public void onDisable();
}
