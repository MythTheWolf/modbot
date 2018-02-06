package com.myththewolf.modbot.core.lib.command;

import com.myththewolf.modbot.core.lib.invocation.impl.PluginClassLoader;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.events.message.MessageCreateEvent;
import de.btobastian.javacord.listeners.message.MessageCreateListener;

public class CommandListener implements MessageCreateListener {
    PluginManager manager;

    public CommandListener(PluginManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        //manager.getPlugins().stream().f
        Message message = messageCreateEvent.getMessage();
        String[] content = message.getContent().split(" ");
    }
}
