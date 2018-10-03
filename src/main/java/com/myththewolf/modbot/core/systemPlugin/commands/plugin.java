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

package com.myththewolf.modbot.core.systemPlugin.commands;

import com.myththewolf.modbot.core.ModBotCoreLoader;
import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.manager.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class plugin implements SystemCommand {
    private PluginManager pluginManager;
    private ModBotCoreLoader loader;

    public plugin(PluginManager manager, ModBotCoreLoader modBotCoreLoaderloader) {
        pluginManager = manager;
        loader = modBotCoreLoaderloader;
    }

    @Override
    public void onCommand(MessageAuthor author, Message message) {
        if (!Util.canRunSupercommand(author, message, loader)) {
            message.getChannel().sendMessage(":thinking: You aren't the bot owner or a superuser!").exceptionally(ExceptionLogger.get());
            return;
        }

        if (message.getContent().split(" ").length < 2) {
            message.getChannel().sendMessage(":warning: Usage: `mb.plugin [disable|enable|reload|info|commands] <plugin name> `")
                    .exceptionally(ExceptionLogger.get());
            return;
        }
        String[] args = message.getContent().split(" ");
        String plugin = Util.arrayToString(2, message.getContent().split(" "));
        Optional<BotPlugin> theBotPlugin = pluginManager.getPlugins().stream()
                .filter(botPlugin -> botPlugin.getPluginName().equals(plugin)).findFirst();
        if (!theBotPlugin.isPresent()) {
            message.getChannel().sendMessage(":warning: No plugin found that name: " + plugin)
                    .exceptionally(ExceptionLogger.get());
            return;
        }
        if (args[1].equals("disable")) {
            if (!theBotPlugin.get().isEnabled()) {
                message.getChannel().sendMessage(":warning: Plugin '" + theBotPlugin.get().getPluginName() + "' already disabled!");
                return;
            }
            theBotPlugin.get().disablePlugin();
            message.getChannel().sendMessage(":white_check_mark: Disabled plugin '" + theBotPlugin.get().getPluginName() + "'");
            return;
        } else if (args[1].equals("enable")) {
            if (theBotPlugin.get().isEnabled()) {
                message.getChannel()
                        .sendMessage(":warning: Plugin '" + theBotPlugin.get().getPluginName() + "' already enabled!");
                return;
            }
            theBotPlugin.get().enablePlugin();
            message.getChannel()
                    .sendMessage(":white_check_mark: Enabled plugin '" + theBotPlugin.get().getPluginName() + "'");
            return;
        } else if (args[1].equals("commands")) {
            StringBuilder cmd = new StringBuilder();
            theBotPlugin.get().getCommands().forEach(discordCommand -> {
                cmd.append(discordCommand.getTrigger() + ",");
            });
            message.getChannel().sendMessage(Util.wrapInCodeBlock(cmd.toString().substring(0, cmd.toString().length() - 1)));
            return;
        } else if (args[1].equals("reload")) {
            CompletableFuture.runAsync(() -> {
                message.getChannel()
                        .sendMessage(":timer: Running plugin onDisable method..");
                pluginManager.reloadPlugin(theBotPlugin.get());
            }).whenComplete((aVoid, throwable) -> {
                message.getChannel()
                        .sendMessage(":white_check_mark: Reload for plugin '" + theBotPlugin.get().getPluginName() + "' complete.");
            });
            return;
        } else if (args[1].equals("info")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.CYAN);
            embedBuilder.setTitle(theBotPlugin.get().getPluginName());
            embedBuilder.addField("Author", theBotPlugin.get().getPluginAuthor(), false);
            embedBuilder.addField("Version", theBotPlugin.get().getPluginVersionString(), false);
            embedBuilder
                    .addField("Data folder location:", theBotPlugin.get().getDataFolder().get()
                            .getAbsolutePath(), false);
            embedBuilder.addField("Number of registered commands:", Integer
                    .toString(theBotPlugin.get().getCommands().size()), false);
            embedBuilder.addField("Number of registered events:", Integer
                    .toString(theBotPlugin.get().getEvents().size()), false);
            embedBuilder.setDescription(theBotPlugin.get().getPluginDescription());
            embedBuilder.setFooter("Use 'mb.plugin commands " + theBotPlugin.get().getPluginName() + "' to view the commands list");
            message.getChannel().sendMessage(embedBuilder).exceptionally(ExceptionLogger.get());
            return;
        } else {
            message.getChannel().sendMessage(":warning: Usage: `mb.plugin [disable|enable|info|commands] <plugin name> `")
                    .exceptionally(ExceptionLogger.get());
            return;
        }
    }
}
