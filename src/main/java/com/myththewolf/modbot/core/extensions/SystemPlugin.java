package com.myththewolf.modbot.core.extensions;

import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;

public class SystemPlugin extends BotPlugin{
    @Override
    public void onEnable() {
        getLogger().info("System plguin loaded!");
    }

    @Override
    public void onDisable() {

    }
}
