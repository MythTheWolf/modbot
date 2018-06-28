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

import com.myththewolf.modbot.core.lib.plugin.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import org.javacord.api.Javacord;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;


import java.awt.*;


public class info implements SystemCommand {
    private PluginManager manager;

    public info(PluginManager manager) {
        this.manager = manager;
    }

    String plList = "";

    @Override
    public void onCommand(MessageAuthor author, Message message) {
        int numPlugins = manager.getPlugins().size();
        long numCommands = manager.getPlugins().stream().map(BotPlugin::getCommands).count();
        EmbedBuilder infoEmbed = new EmbedBuilder();
        infoEmbed.setTitle("System Info");
        infoEmbed.setColor(Color.GREEN);
        plList = "";
        manager.getPlugins().forEach(plugin -> plList += plugin.getPluginName() + " ");
        infoEmbed.addField("Plugins:", plList, false);
        infoEmbed.setFooter(numPlugins + " loaded plugins, with " + numCommands + " total commands.");
        message.getChannel().sendMessage(infoEmbed).exceptionally(ExceptionLogger.get());
    }
}
