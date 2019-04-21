package com.myththewolf.modbot.core.lib.plugin.event.impl;

import com.myththewolf.modbot.core.lib.plugin.event.interfaces.BotEvent;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventType;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.Optional;

public class PluginEnableEvent implements BotEvent {

    private boolean cancel = false;
    private BotPlugin plugin;

    public PluginEnableEvent(BotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<User> getUser() {
        return Optional.empty();
    }

    @Override
    public Optional<Server> getServer() {
        return Optional.empty();
    }

    @Override
    public EventType getEventType() {
        return EventType.PLUGIN_ENABLE;
    }

    @Override
    public BotPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void cancelEvent() {
        cancel = true;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }
}
