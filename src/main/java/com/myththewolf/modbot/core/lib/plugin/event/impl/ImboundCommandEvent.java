package com.myththewolf.modbot.core.lib.plugin.event.impl;

import com.myththewolf.modbot.core.API.command.impl.DiscordCommand;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.BotEvent;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventType;
import com.myththewolf.modbot.core.lib.plugin.invocation.impl.BotPlugin;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.Optional;

public class ImboundCommandEvent implements BotEvent {
    private User user;
    private DiscordCommand command;
    private Message source;
    private boolean cancel = false;
    public ImboundCommandEvent(Message m,DiscordCommand command,User user){
        this.user = user;
        this.command = command;
        this.source = m;
    }
    @Override
    public Optional<User> getUser() {
      return  Optional.ofNullable(user);
    }

    @Override
    public Optional<Server> getServer() {
        return source.getServer();
    }

    @Override
    public EventType getEventType() {
        return EventType.IMBOUND_COMMAND;
    }

    @Override
    public BotPlugin getPlugin() {
        return command.getParentPlugin();
    }

    @Override
    public void cancelEvent() {
        cancel = true;
        source.delete().exceptionally(ExceptionLogger.get());
    }

    public boolean isCancelled() {
        return cancel;
    }
}
