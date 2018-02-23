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

package com.myththewolf.modbot.core.lib.plugin.manPage.impl;

import com.myththewolf.modbot.core.API.command.impl.DiscordCommand;
import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.plugin.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.CommandUsageManual;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.channels.TextChannel;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CommandUsageManual
 */
public class ImplCommandUsageManual implements CommandUsageManual {
    /**
     * The raw JSON data pulled from file
     */
    private JSONObject dataJsonObject;
    /**
     * The list of messages that are mapped to this manual page
     */
    private List<ManualPageEmbed> messageList = new ArrayList<>();
    /**
     * The plugin that this manual is mapped to
     */
    private BotPlugin plugin;
    /**
     * The manual name
     */
    private String name;
    /**
     * Gets the mapped commands
     */
    private List<String> mappedCommands = new ArrayList<>();
    private String synopsis = "";
    private PluginManager pluginManager;

    public ImplCommandUsageManual(PluginManager manager, BotPlugin plugin, JSONObject dataJsonObject) {
        this.plugin = plugin;
        this.dataJsonObject = dataJsonObject;
        this.pluginManager = manager;
    }

    @Override
    public List<ManualPageEmbed> getEmbeds() {
        return null;
    }

    @Override
    public BotPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String getPageName() {
        return getCommand().getTrigger();
    }

    @Override
    public DiscordCommand getCommand() {
        return getPlugin().getCommands().stream()
                .filter(command -> command.getTrigger().equals(dataJsonObject.getString("for"))).findFirst()
                .orElse(null);
    }

    @Override
    public Object getPageOf(int index) {
        if (index == 0) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Command Details");
            builder.addField("**NAME**", Util.wrapInCodeBlock(getPageName()), false);
            builder.addField("**SYNTAX**", Util.wrapInCodeBlock(getPageName()), false);
            builder.addField("**DESCRIPTION**", Util.wrapInCodeBlock(dataJsonObject.getString("description")), false);
            builder.setColor(Color.MAGENTA);
            return builder;
        } else if (dataJsonObject.getJSONArray("arguments").length() - 1 <= index) {
            EmbedBuilder argumentEmbed = new EmbedBuilder();
            argumentEmbed.setColor(((JSONObject) dataJsonObject.getJSONArray("arguments").get(index - 1))
                    .isNull("embedColor") ? Color.GRAY : Util.getColorByName("embedColor"));
            JSONObject argument = (JSONObject) dataJsonObject.getJSONArray("arguments").get(index - 1);
            argumentEmbed.addField("**NAME**", Util.wrapInCodeBlock(argument.getString("name")), false);
            argumentEmbed.addField("**REQUIRED**", Util.wrapInCodeBlock(argument.getString("required")), false);
            argumentEmbed.addField("**TYPE**", Util.wrapInCodeBlock(argument.getString("type")), false);
            argumentEmbed.setDescription(Util.wrapInCodeBlock(argument.getString("description")));
            return argumentEmbed;
        }
        return null;
    }

    @Override
    public int getNumRequiredArgs() {
        return dataJsonObject.getJSONArray("arguments").length();
    }

    @Override
    public void displayNewEmbed(TextChannel scope) {

    }

    @Override
    public String getUsage() {
        synopsis = "";
        dataJsonObject.getJSONArray("arguments").forEach(o -> {
            JSONObject argument = (JSONObject) o;
            synopsis += " " + (argument.getBoolean("required") ? "<" + argument
                    .getString("name") + ">" : "[" + argument.getString("name") + "]");
        });
        return synopsis;
    }
}
