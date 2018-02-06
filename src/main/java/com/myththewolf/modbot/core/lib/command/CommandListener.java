package com.myththewolf.modbot.core.lib.command;

import com.myththewolf.modbot.core.API.command.DiscordCommand;
import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.events.message.MessageCreateEvent;
import de.btobastian.javacord.listeners.message.MessageCreateListener;

import java.util.List;

/**
 * This is a core Message event, it reads incoming messages and controls commands.
 */
public class CommandListener implements MessageCreateListener, Loggable {
    PluginManager manager;

    public CommandListener(PluginManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        Message message = messageCreateEvent.getMessage();
        String[] content = message.getContent().split(" ");
        manager.getPlugins().stream().map(BotPlugin::getCommands).flatMap(List::stream).filter((DiscordCommand cmd) -> {
          //  messageCreateEvent.getChannel().sendMessage("Evaluation.java$20deF93 \n " + cmd.getParentPlugin().getPluginName() + ":" + cmd.getTrigger());
            return (cmd.getTrigger().equals(content[0]) || content[0].equals(cmd.getParentPlugin().getPluginName() + ":" + cmd.getTrigger()));

        }).forEachOrdered(discordCommand -> discordCommand.invokeCommand(messageCreateEvent.getChannel(), messageCreateEvent.getMessage().getAuthor(), messageCreateEvent.getMessage()));

    }

}




