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
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.PluginManualPage;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAuthor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class help implements SystemCommand {
    PluginManager manager;

    public help(PluginManager pl) {
        manager = pl;
    }

    @Override
    public void onCommand(MessageAuthor author, Message message) {
        String[] args = Arrays.copyOfRange(message.getContent().split(" "), 1, message.getContent().split(" ").length);
       Optional<PluginManualPage> pageOptional= manager.getPlugins().stream().map(BotPlugin::getManuals).flatMap(List::stream)
                .filter(pluginManualPage -> pluginManualPage.getPageName().equals(args[0]))
                .findFirst();
       if(!pageOptional.isPresent()){
           message.getChannel().sendMessage(":warning: No manual page found by that name!");
           return;
       }
       pageOptional.ifPresent(page -> {

       });
    }
}
