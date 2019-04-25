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

package com.myththewolf.modbot.core.lib.plugin.manPage.CommandUsage;

import com.myththewolf.modbot.core.API.command.impl.DiscordCommand;
import com.myththewolf.modbot.core.Util;
import com.myththewolf.modbot.core.lib.plugin.manPage.impl.ManualPageEmbed;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.manager.interfaces.PluginManager;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.json.JSONArray;
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
    private String syntax = "";
    private String synopsis;
    private PluginManager pluginManager;

    public ImplCommandUsageManual(PluginManager manager, BotPlugin plugin, JSONObject dataJsonObject) {
        this.plugin = plugin;
        this.dataJsonObject = dataJsonObject;
        this.pluginManager = manager;
    }

    @Override
    public List<ManualPageEmbed> getEmbeds() {
        return messageList;
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
        if (index < 0 || index > getTotalNumberPages()) {
            return null;
        }
        int arguments_end = dataJsonObject.getJSONArray("arguments").length() + 1;
        if (index == 0) {
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder build = new StringBuilder(dataJsonObject.getString("for"));
            dataJsonObject.getJSONArray("arguments").forEach(arg -> {
                JSONObject argument = (JSONObject) arg;
                syntax = syntax.replace(argument.getString("name"), argument.getBoolean("required") ? "<" + argument.getString("name") + ">" : "[" + argument
                        .getString("name") + "]");
            });
            builder.setTitle("Command Details");
            builder.addField("**NAME**", Util.wrapInCodeBlock(getPageName()), false);
            builder.addField("**SYNTAX**", Util.wrapInCodeBlock(getUsage()), false);
            builder.addField("**DESCRIPTION**", Util.wrapInCodeBlock(dataJsonObject.getString("description")), false);
            builder.setColor(Color.MAGENTA);
            return builder;
        }

        if (index < arguments_end) {
            EmbedBuilder argumentEmbed = new EmbedBuilder();
            argumentEmbed.setColor(((JSONObject) dataJsonObject.getJSONArray("arguments").get(index - 1))
                    .isNull("embedColor") ? Color.GRAY : Util
                    .getColorByName(((JSONObject) dataJsonObject.getJSONArray("arguments").get(index - 1))
                            .getString("embedColor")));
            JSONObject argument = (JSONObject) dataJsonObject.getJSONArray("arguments").get(index - 1);
            argumentEmbed.setTitle("Command argument Details:" + dataJsonObject.getString("syntax")
                    .replace(argument.getString("name"), "__[" + argument.getString("name") + "]__"));
            argumentEmbed.addField("**NAME**", Util.wrapInCodeBlock(argument.getString("name")), false);
            argumentEmbed.addField("**DESCRIPTION**", Util.wrapInCodeBlock(argument.getString("description")), false);
            argumentEmbed.addField("**REQUIRED**", Util
                    .wrapInCodeBlock(Boolean.toString(argument.getBoolean("required"))), false);
            argumentEmbed.addField("**TYPE**", Util.wrapInCodeBlock(argument.getString("type")), false);
            return argumentEmbed;
        }
        if(index > arguments_end){
            //TODO: Implement related commands
            //TODO: Add aliases
        }
        return null;
    }

    @Override
    public String asString(int index) {
        if (index < 0 || index > getTotalNumberPages()) {
            return null;
        }
        int arguments_end = dataJsonObject.getJSONArray("arguments").length() + 1;
        if (index == 0) {
            StringBuilder base = new StringBuilder();
            StringBuilder build = new StringBuilder(dataJsonObject.getString("for"));
            dataJsonObject.getJSONArray("arguments").forEach(arg -> {
                JSONObject argument = (JSONObject) arg;
                syntax = syntax.replace(argument.getString("name"), argument.getBoolean("required") ? "<" + argument.getString("name") + ">" : "[" + argument
                        .getString("name") + "]");
            });
            base.append("\n---------Command Details---------\n");
            base.append("NAME: " + getPageName() + "\n");
            base.append("SYNTAX: " + getUsage() + "\n");
            base.append("DESCRIPTION: " + dataJsonObject.getString("description"));
            return base.toString();
        }

        if (index < arguments_end) {
            StringBuilder base = new StringBuilder();
            JSONObject argument = (JSONObject) dataJsonObject.getJSONArray("arguments").get(index - 1);
            String header = "\n\u001b[32m---------Command argument Details: " + dataJsonObject.getString("syntax")
                    .replace(argument.getString("name"), "[" + argument.getString("name") + "]") + "---------";
            base.append(header);
            base.append("\n\u001b[35mNAME:\u001b[36m " + argument.getString("name"));
            base.append("\n\u001b[35mDESCRIPTION:\u001b[36m " + argument.getString("description"));
            base.append("\n\u001b[35mREQUIRED:\u001b[36m " + argument.getBoolean("required"));
            base.append("\n\u001b[35mTYPE:\u001b[36m " + argument.getString("type") + "\u001b[0m");
            base.append(header.replaceAll(".", "\u001b[32m-") + "\u001b[0m");
            return base.toString();
        }
        if (index > arguments_end) {
            //TODO: Implement related commands
            //TODO: Add aliases
        }
        return "\u001b[31mNo such page found\u001b[0m";
    }

    @Override
    public int getNumRequiredArgs() {
        int num = 0;
        JSONArray arr = dataJsonObject.getJSONArray("arguments");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject ob = arr.getJSONObject(i);
            if(ob.getBoolean("required")){
                num++;
            }
        }
        return num;
    }
    @Override
    public ManualPageEmbed displayNewEmbed(TextChannel scope,int startPage) {
        ManualPageEmbed manualPageEmbed = new ManualPageEmbed(this, scope,startPage);
        manualPageEmbed.instaniateEmbed();
        messageList.add(manualPageEmbed);
        return manualPageEmbed;
    }
    @Override
    public int getTotalNumberPages() {
        int start = 1;
        start += dataJsonObject.getJSONArray("arguments").length();
        start += dataJsonObject.getJSONArray("related-commands").length();
        return start;
    }

    @Override
    public void removeEmebed(ManualPageEmbed embed) {
        this.messageList.remove(embed);
    }

    @Override
    public String getUsage() {
        synopsis = "";
        dataJsonObject.getJSONArray("arguments").forEach(o -> {
            JSONObject argument = (JSONObject) o;
            synopsis += " " + (argument.getBoolean("required") ? "<" + argument
                    .getString("name") + ">" : "[" + argument.getString("name") + "]");
        });
        return getCommand().getTrigger()+" " +synopsis;
    }
    public String getRawUsage(){
        synopsis = "";
        dataJsonObject.getJSONArray("arguments").forEach(o -> {
            JSONObject argument = (JSONObject) o;
            synopsis += argument.getString("name") +" ";
        });
        return synopsis;
    }
    public JSONObject getDataJsonObject() {
        return dataJsonObject;
    }
}
