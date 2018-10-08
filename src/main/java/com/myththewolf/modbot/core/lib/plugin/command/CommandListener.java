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
import com.myththewolf.modbot.core.MyriadBotLoader;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.event.impl.ImboundCommandEvent;
import com.myththewolf.modbot.core.lib.plugin.event.impl.UserCommandEvent;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventHandler;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventType;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.manager.interfaces.PluginManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;

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
    boolean isSystemCommand;
    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if (!messageCreateEvent.getMessage().getContent().startsWith(MyriadBotLoader.COMMAND_KEY)) {
            return;
        }
        isValidCommand = false;
        isSystemCommand = false;
        Thread.currentThread().setName("Events");
        Message message = messageCreateEvent.getMessage();
        String[] content = message.getContent().split(" ");
        content[0] = content[0].substring(MyriadBotLoader.COMMAND_KEY.length());


        manager.getPlugins().stream().map(BotPlugin::getCommands).flatMap(List::stream)
                .filter((DiscordCommand cmd) -> (cmd.getTrigger().equals(content[0]) || content[0]
                        .equals(cmd.getParentPlugin().getPluginName() + ":" + cmd.getTrigger()))
                ).forEachOrdered(discordCommand -> {
            ImboundCommandEvent commandEvent = new ImboundCommandEvent(message,discordCommand,messageCreateEvent.getMessage().getUserAuthor().get());
            manager.getPlugins().stream().flatMap(plugin -> plugin.getEventsOfType(EventType.IMBOUND_COMMAND).stream()).forEachOrdered(runner -> {
                Optional<Method> methodOptional = Arrays.stream(runner.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(EventHandler.class)).findAny();
                if (!methodOptional.isPresent()) {
                    getLogger()
                            .warn("Could not pass event of type IMBOUND_COMMAND to class '{}', no runner method found", runner
                                    .getClass().getName());
                } else {
                    try {
                        methodOptional.get()
                                .invoke(runner, commandEvent, runnerToBotPlugin(runner));
                    } catch (Exception e) {
                        getLogger()
                                .error("Could not pass event of type IMBOUND_COMMAND to class '{}': Internal error! (Our fault): {}", runner
                                        .getClass().getName(), e.getMessage());
                    }
                }
            });
            if(!commandEvent.isCancelled()){
                discordCommand
                        .invokeCommand(messageCreateEvent.getChannel(), messageCreateEvent.getMessage()
                                .getAuthor(), messageCreateEvent.getMessage());
                isValidCommand = true;
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

        if(!isSystemCommand && !isValidCommand){
            message.getChannel().sendMessage(content[0]+": Command not found").exceptionally(ExceptionLogger.get());
            return;
        }
    }

    private BotPlugin runnerToBotPlugin(Object runner) {
        return manager.getPlugins().stream().filter((BotPlugin plugin) -> plugin.getEvents().stream()
                .anyMatch(o -> o.getClass().getName().equals(runner.getClass().getName())))
                .findFirst().orElse(null);
    }
}




