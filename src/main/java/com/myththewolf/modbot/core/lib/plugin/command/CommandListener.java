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

package com.myththewolf.modbot.core.lib.plugin.command;

import com.myththewolf.modbot.core.API.command.impl.DiscordCommand;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.event.impl.UserCommandEvent;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventHandler;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventType;
import com.myththewolf.modbot.core.lib.plugin.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.lib.plugin.interfaces.PluginManager;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.events.message.MessageCreateEvent;
import de.btobastian.javacord.listeners.message.MessageCreateListener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This is a core Message event, it reads incoming messages and controls commands.
 */
public class CommandListener implements MessageCreateListener, Loggable {
    /**
     * Used to break out of the event method if we receive a message event but it's not a command
     */
    private boolean isValidCommand = false;
    /**
     * The plugin manager
     */
    private PluginManager manager;

    /**
     * Constructs a new CommandListener
     *
     * @param manager The system plugin manager
     */
    public CommandListener(PluginManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {

        isValidCommand = false;
        Thread.currentThread().setName("Events");
        Message message = messageCreateEvent.getMessage();
        String[] content = message.getContent().split(" ");
        manager.getPlugins().stream().map(BotPlugin::getCommands).flatMap(List::stream)
                .filter((DiscordCommand cmd) -> (cmd.getTrigger().equals(content[0]) || content[0]
                        .equals(cmd.getParentPlugin().getPluginName() + ":" + cmd.getTrigger()))
                ).forEachOrdered(discordCommand -> {
            discordCommand
                    .invokeCommand(messageCreateEvent.getChannel(), messageCreateEvent.getMessage()
                            .getAuthor(), messageCreateEvent.getMessage());
            isValidCommand = true;
        });

        ((ImplPluginLoader) manager).getSystemCommands().forEach((key, val) -> {
            if (key.equals(content[0])) {
                getLogger().info("{} ran a system command: {}", messageCreateEvent.getMessage().getAuthor()
                        .getName(), key);
                val.onCommand(messageCreateEvent.getMessage().getAuthor(), messageCreateEvent.getMessage());
            }
        });
        if (isValidCommand) {
            manager.getPlugins().stream().flatMap(plugin -> plugin.getEventsOfType(EventType.COMMAND_RUN).stream())
                    .forEach(runner -> {
                        Optional<Method> methodOptional = Arrays.stream(runner.getClass().getMethods())
                                .filter(method -> method.isAnnotationPresent(EventHandler.class)).findAny();
                        if (!methodOptional.isPresent()) {
                            getLogger()
                                    .warn("Could not pass event of type COMMAND_RUN to class '{}', no runner method found", runner
                                            .getClass().getName());
                        } else {
                            try {
                                methodOptional.get()
                                        .invoke(runner, new UserCommandEvent(manager, messageCreateEvent
                                                .getMessage(), runnerToBotPlugin(runner)));
                            } catch (Exception e) {
                                getLogger()
                                        .error("Could not pass event of type COMMAND_RUN to class '{}': Internal error! (Our fault): {}", runner
                                                .getClass().getName(), e.getMessage());
                            }
                        }
                    });
        }
    }

    private BotPlugin runnerToBotPlugin(Object runner) {
        return manager.getPlugins().stream().filter(plugin -> plugin.getEvents().stream()
                .filter(o -> o.getClass().getName().equals(runner.getClass().getName())).findAny().isPresent())
                .findFirst().orElse(null);
    }
}




