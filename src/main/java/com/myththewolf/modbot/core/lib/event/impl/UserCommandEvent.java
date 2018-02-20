package com.myththewolf.modbot.core.lib.event.impl;

import com.myththewolf.modbot.core.API.command.interfaces.CommandExecutor;
import com.myththewolf.modbot.core.lib.event.interfaces.BotEvent;
import com.myththewolf.modbot.core.lib.event.interfaces.EventType;
import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;

import java.util.List;
import java.util.Optional;

/**
 * This class represents a Event where a user entered a command
 */
public class UserCommandEvent implements BotEvent {
    /**
     * The orgin message of the command run
     */
    private Message message;
    /**
     * The executor of the command run
     */
    private CommandExecutor commandExecutor;

    /**
     * Constructs a new UserCommandEvent
     *
     * @param message The message that triggered the command
     */
    public UserCommandEvent(PluginManager manager, Message message) {
        commandExecutor = manager.getPlugins()
                .stream()
                .map(BotPlugin::getCommands)
                .flatMap(List::stream)
                .filter(discordCommand -> discordCommand.getTrigger()
                        .equals(message.getContent()
                                .split(" ")[0]))
                .findFirst()
                .get()
                .getExecutor();
        this.message = message;
    }

    @Override
    public Optional<User> getUser() {
        return message.getAuthor().asUser();
    }

    @Override
    public Optional<Server> getServer() {
        return message.getServer();
    }

    /**
     * Gets the command executor that this event represents
     *
     * @return The command
     */
    private CommandExecutor getCommand() {
        return this.commandExecutor;
    }

    @Override
    public EventType getEventType() {
        return EventType.COMMAND_RUN;
    }
}
