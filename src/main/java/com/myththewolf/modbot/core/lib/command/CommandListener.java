package com.myththewolf.modbot.core.lib.command;

import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.events.message.MessageCreateEvent;
import de.btobastian.javacord.listeners.message.MessageCreateListener;

import java.util.List;

/**
 * This is a core Message event, it reads incoming messages and controls commands.
 */
public class CommandListener implements MessageCreateListener {
    PluginManager manager;

    public CommandListener(PluginManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        Message message = messageCreateEvent.getMessage();
        String[] content = message.getContent().split(" ");
        manager.getPlugins().stream().map(BotPlugin::getCommands).flatMap(List::stream).filter(cmd -> cmd.getTrigger().equals(content[0])).forEachOrdered(discordCommand -> {
            discordCommand.invokeCommand(messageCreateEvent.getChannel(), messageCreateEvent.getMessage().getAuthor(), messageCreateEvent.getMessage());
        });

    }
}
