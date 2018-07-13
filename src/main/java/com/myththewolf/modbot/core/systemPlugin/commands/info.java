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
import com.myththewolf.modbot.core.lib.plugin.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import org.javacord.api.Javacord;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;


import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;


public class info implements SystemCommand {
    private PluginManager manager;

    public info(PluginManager manager) {
        this.manager = manager;
    }

    String plList = "";
    String plList2 = "";
    int com = 0;
    int event = 0;
    long users;
    @Override
    public void onCommand(MessageAuthor author, Message message) {
        int numPlugins = manager.getPlugins().size();
        long numCommands = manager.getPlugins().stream().map(BotPlugin::getCommands).count();
        plList = "";
        plList2 = "";
        com =0;
        event =0;
        users = 0;
        message.getApi().getServers().forEach(s -> users += s.getMemberCount());
        manager.getPlugins().stream().filter(BotPlugin::isEnabled).forEach(plugin -> { plList += plugin.getPluginName() + " "; com += plugin.getCommands().size(); event += plugin.getEvents().size(); });
        manager.getPlugins().stream().filter(botPlugin -> !botPlugin.isEnabled()).forEach(plugin -> plList2 += plugin.getPluginName() + " ");
        EmbedBuilder infoEmbed = new EmbedBuilder();
        Duration D = Duration.between(ModBotCoreLoader.START,LocalDateTime.now());
        String uptime = Util.milisToTimeString(D.toMillis());
        infoEmbed.setTitle("**:information_source:  System Info**");
        infoEmbed.addField(":clock: Uptime: ",Util.wrapInCodeBlock(uptime),false);
        infoEmbed.addField(":white_check_mark: Enabled Plugins: ",Util.wrapInCodeBlock(plList.isEmpty() ? "[NONE]" : plList),true);
        infoEmbed.addField(":x: Disabled Plugins", Util.wrapInCodeBlock(plList2.isEmpty() ? "[NONE]" : plList2),true);
        infoEmbed.addField(":hammer_pick: Total Registered Commands: ",Util.wrapInCodeBlock(com+""),true);
        infoEmbed.addField(":desktop: Total Joined Servers:",Util.wrapInCodeBlock(message.getApi().getServers().size()+""),true);
        infoEmbed.setUrl("https://github.com/MythTheWolf/modbot");
        infoEmbed.setFooter("Modbot, made with ❤ by MythTheWolf#7561");
        infoEmbed.setColor(Color.BLACK);
        message.getChannel().asServerTextChannel().get().sendMessage(infoEmbed).exceptionally(ExceptionLogger.get());
    }
}
