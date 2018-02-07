package com.myththewolf.modbot.core.extensions;

import com.myththewolf.modbot.core.extensions.commands.info;
import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;

public class SystemPlugin extends BotPlugin {
    private PluginManager pluginManager;

    public SystemPlugin(PluginManager man) {
        pluginManager = man;
    }

    @Override
    public void onEnable() {
        getLogger().info("System plugin loaded!");
        registerCommand("~info", new info(pluginManager));
    }

    @Override
    public void onDisable() {

    }
}
