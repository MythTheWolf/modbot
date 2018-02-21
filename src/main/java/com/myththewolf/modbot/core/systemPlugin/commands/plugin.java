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

import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.plugin.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAuthor;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.Optional;

public class plugin implements SystemCommand {
    private PluginManager pluginManager;

    public plugin(PluginManager manager) {
        pluginManager = manager;
    }

    @Override
    public void onCommand(MessageAuthor author, Message message) {
        if (!(message.getContent().split(" ").length > 0)) {
            message.getChannel().sendMessage(":warning: Usage: `>plugin <plugin name>`")
                    .exceptionally(Javacord::exceptionLogger);
            return;
        }
        String plugin = Util.arrayToString(1, message.getContent().split(" "));
        Optional<BotPlugin> theBotPlugin = pluginManager.getPlugins().stream()
                .filter(botPlugin -> botPlugin.getPluginName().equals(plugin)).findFirst();
        if (!theBotPlugin.isPresent()) {
            message.getChannel().sendMessage(":warning: No plugin found by that name")
                    .exceptionally(Javacord::exceptionLogger);
            return;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setTitle(theBotPlugin.get().getPluginName());
        embedBuilder.addField("Author", theBotPlugin.get().getPluginAuthor(), false);
        embedBuilder.addField("Version", theBotPlugin.get().getPluginVersionString(), false);
        embedBuilder
                .addField("Data folder location:", theBotPlugin.get().getDataFolder().get().getAbsolutePath(), false);
        embedBuilder.addField("Number of registered commands:", Integer
                .toString(theBotPlugin.get().getCommands().size()), false);
        embedBuilder.addField("Number of registered events:", Integer
                .toString(theBotPlugin.get().getEvents().size()), false);
        embedBuilder.setDescription(theBotPlugin.get().getPluginDescription());
        message.getChannel().sendMessage(embedBuilder).exceptionally(Javacord::exceptionLogger);
    }
}
