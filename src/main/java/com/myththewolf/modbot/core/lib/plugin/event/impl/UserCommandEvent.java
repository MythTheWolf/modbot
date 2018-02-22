/*
 * Copyright (c) 2018 MythTheWolf
 *  Nicholas Agner, USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.myththewolf.modbot.core.lib.plugin.event.impl;

import com.myththewolf.modbot.core.API.command.interfaces.CommandExecutor;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.BotEvent;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventType;
import com.myththewolf.modbot.core.lib.plugin.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.invocation.interfaces.PluginManager;
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
     * The plugin that this event is being passed to
     */
    private BotPlugin botPlugin;

    /**
     * Constructs a new UserCommandEvent
     *
     * @param manager The system plugin manager
     * @param message The source message of the command
     * @param plugin  The plugin that this event will be passed to
     */
    public UserCommandEvent(PluginManager manager, Message message, BotPlugin plugin) {
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
        this.botPlugin = plugin;
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
    public CommandExecutor getCommand() {
        return this.commandExecutor;
    }

    @Override
    public EventType getEventType() {
        return EventType.COMMAND_RUN;
    }

    @Override
    public BotPlugin getPlugin() {
        return botPlugin;
    }
}
