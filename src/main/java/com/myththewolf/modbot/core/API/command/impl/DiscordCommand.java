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

package com.myththewolf.modbot.core.API.command.impl;


import com.myththewolf.modbot.core.API.command.interfaces.CommandExecutor;
import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.manPage.impl.ImplCommandUsageManual;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.ManualType;
import org.javacord.api.Javacord;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.util.logging.ExceptionLogger;


import java.util.Arrays;
import java.util.Optional;

/**
 * This class represents a CommandExecutor container for easy control
 */
public class DiscordCommand implements Loggable {
    /**
     * The literal command String
     */
    private String trigger;
    /**
     * The executor to be ran when this command is triggered
     */
    private CommandExecutor executor;
    /**
     * The plugin that registered this command
     */
    private BotPlugin parent;

    /**
     * Constructs a new DiscordCommand
     *
     * @param executor The executor to be ran when this command is triggered
     * @param trigger  The literal command String to map to this command
     */
    public DiscordCommand(BotPlugin plugin, CommandExecutor executor, String trigger) {
        this.trigger = trigger;
        this.executor = executor;
        this.parent = plugin;
    }

    /**
     * Gets the command trigger string
     *
     * @return The literal command string
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * Gets this command's executor
     *
     * @return The executor
     */
    public CommandExecutor getExecutor() {
        return executor;
    }

    /**
     * Runs this command
     *
     * @param channel The TextChannel where the command was ran from
     * @param user    The user who ran the command
     * @param source  The message that triggered the command
     */
    public void invokeCommand(TextChannel channel, MessageAuthor user, Message source) {
        String[] args = Arrays.copyOfRange(source.getContent().split(" "), 1, source.getContent().split(" ").length);
        getLogger().debug(Arrays.toString(args));
        Optional<ImplCommandUsageManual> commandUsageManual = getParentPlugin()
                .getManuasOfType(ManualType.COMMAND_SYNTAX).stream()
                .filter(manualPage -> manualPage.getPageName().equals(getTrigger())).findAny()
                .map(manualPage -> (ImplCommandUsageManual) manualPage);
        getLogger().debug(args.length + "<" + commandUsageManual.get().getNumRequiredArgs());
        if (commandUsageManual.isPresent() && args.length < commandUsageManual.get().getNumRequiredArgs()) {
            channel.sendMessage(":warning: **The syntax of the command is incorrect**: Usage: " + Util
                    .wrapInCodeBlock(getTrigger() + " " + commandUsageManual.get().getUsage()))
                    .exceptionally(ExceptionLogger.get());
            return;
        }
        Thread commandThread = new Thread(() -> {
            getExecutor().update(getParentPlugin(), channel, source);
            getExecutor().onCommand(channel, user, args, source);
            getLogger().info("{} ran a command: {}", user.getName(), getTrigger());
        });
        commandThread.setName(getParentPlugin().getPluginName());
        commandThread.start();
    }

    /**
     * Returns the plugin that this command is registered to
     *
     * @return The plugin
     */
    public BotPlugin getParentPlugin() {
        return parent;
    }
}
