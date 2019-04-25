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
import com.myththewolf.modbot.core.MyriadBotLoader;
import com.myththewolf.modbot.core.Util;
import com.myththewolf.modbot.core.lib.plugin.manPage.CommandUsage.ArgumentType;
import com.myththewolf.modbot.core.lib.plugin.manPage.CommandUsage.ImplCommandUsageManual;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.ManualType;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.PluginManualPage;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Optional;

/**
 * This class represents a CommandExecutor container for easy control
 */
public class DiscordCommand {
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
        if (getCommandManual().isPresent()) {
            ImplCommandUsageManual manual = (ImplCommandUsageManual) getCommandManual().get();
            if (args.length < manual.getNumRequiredArgs()) {
                channel.sendMessage(":warning: Command syntax is incorrect! Usage: " + Util.wrapInCodeBlock(manual.getUsage())).join();
                channel.sendMessage("*See `mb.man " + manual.getCommand().getTrigger() + "` for more details.*").join();
                return;
            } else {
                boolean typesOk = true;
                String badType = "";
                String badSupp = "";
                int badIndex = 0;
                String[] usage = manual.getRawUsage().split(" ");
                for (int x = 0; x < manual.getDataJsonObject().getJSONArray("arguments").length(); x++) {
                    if (x > args.length - 1) {
                        break;
                    }
                    JSONObject arg = mapObjectByName(usage[x], manual.getDataJsonObject().getJSONArray("arguments"));
                    ArgumentType type = ArgumentType.valueOf(arg.getString("type"));
                    badType = usage[x];
                    badSupp = args[x];
                    badIndex++;

                    switch (type) {
                        case STRING:
                            typesOk = true;
                            break;
                        case BOOLEAN:
                            typesOk = Boolean.parseBoolean(args[x]);
                            break;
                        case INT:
                            typesOk = Util.isNumber(args[x]);
                            break;
                        case VARARG:
                            typesOk = (x + 1) > usage.length;
                            break;
                        case TEXT_CHANNEL:
                            String id = args[x].substring(2, (args[x].length() - 1));
                            typesOk = channel.getApi().getChannelById(id).isPresent();
                            break;
                        case USER_MENTION:
                            String id2 = args[x].substring(2, (args[x].length() - 1));
                            typesOk = channel.getApi().getUserById(id2).getNow(null) != null;
                            break;
                        case ROLE_MENTION:
                            String id3 = args[x].substring(2, (args[x].length() - 1));
                            typesOk = channel.getApi().getRoleById(id3).isPresent();
                            break;
                        case ROLE_NAME:
                            typesOk = channel.getApi().getRolesByName(args[x]).size() > 0;
                            break;
                        case PLUGIN:
                            final String name = args[x];
                            typesOk = getParentPlugin().getPluginManager().getPlugins().stream().map(BotPlugin::getPluginName).anyMatch(s -> s.equals(name));
                            break;
                        case CONSTANT:
                            typesOk = Util.jsonArray_Contains(arg.getJSONArray("values"), args[x]);
                            break;
                        default:
                            typesOk = false;
                            break;
                    }
                    if (!typesOk) {
                        break;
                    }
                }
                if (!typesOk) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle(":x: Command Syntax incorrect");
                    builder.setDescription(Util.wrapInCodeBlock("One of your argument types is wrong!"));
                    builder.addField("COMMAND USAGE:", Util.wrapInCodeBlock(manual.getUsage()), false);
                    JSONObject argRef = mapObjectByName(badType, manual.getDataJsonObject().getJSONArray("arguments"));
                    if (ArgumentType.valueOf(argRef.getString("type")).equals(ArgumentType.CONSTANT)) {
                        builder.addField("The supplied value '" + badSupp + "' is not a accecptable value for that argument, the possible choices are: ", Util.wrapInCodeBlock(argRef.getJSONArray("values").toString()), false);
                    } else {
                        builder.addField("EXPECTED ARGUMENT TYPE: ", Util.wrapInCodeBlock(badSupp), false);
                        builder.addField("GOT: ", badSupp, false);
                    }
                    builder.setThumbnail(user.getAvatar());
                    builder.setFooter("See " + MyriadBotLoader.COMMAND_KEY + "mb.man " + manual.getPageName() + " " + (badIndex) + "");
                    channel.sendMessage(builder).exceptionally(ExceptionLogger.get());
                    return;
                }
            }
        }
            getExecutor().update(getParentPlugin(), channel, source);
        getExecutor().onCommand(channel, user, args, source);
            getLogger().info("{} ran a command: {}", user.getName(), getTrigger());
    }

    public void invokeCommand(String source) {
        String[] args = Arrays.copyOfRange(source.split(" "), 1, source.split(" ").length);
        if (getCommandManual().isPresent()) {
            ImplCommandUsageManual manual = (ImplCommandUsageManual) getCommandManual().get();
            if (args.length < manual.getNumRequiredArgs()) {
                getLogger().info("\u001b[31mCommand syntax is incorrect! Usage: " + manual.getUsage());
                getLogger().info("See mb.man " + manual.getCommand().getTrigger() + " for more details.\u001b[0m");
                return;
            }
            boolean typesOk = true;
            String badType = "";
            String badSupp = "";
            int badIndex = 0;
            String[] usage = manual.getRawUsage().split(" ");
            for (int x = 0; x < manual.getDataJsonObject().getJSONArray("arguments").length(); x++) {
                if (x > args.length - 1) {
                    break;
                }
                JSONObject arg = mapObjectByName(usage[x], manual.getDataJsonObject().getJSONArray("arguments"));
                ArgumentType type = ArgumentType.valueOf(arg.getString("type"));
                badType = usage[x];
                badSupp = args[x];
                badIndex++;
                switch (type) {
                    case STRING:
                        typesOk = true;
                        break;
                    case BOOLEAN:
                        typesOk = Boolean.parseBoolean(args[x]);
                        break;
                    case INT:
                        typesOk = Util.isNumber(args[x]);
                        break;
                    case VARARG:
                        typesOk = (x + 1) > usage.length;
                        break;
                    case TEXT_CHANNEL:
                        String id = args[x].substring(2, (args[x].length() - 1));
                        typesOk = getParentPlugin().getDiscordAPI().getChannelById(id).isPresent();
                        break;
                    case USER_MENTION:
                        String id2 = args[x].substring(2, (args[x].length() - 1));
                        typesOk = getParentPlugin().getDiscordAPI().getUserById(id2).getNow(null) != null;
                        break;
                    case ROLE_MENTION:
                        String id3 = args[x].substring(2, (args[x].length() - 1));
                        typesOk = getParentPlugin().getDiscordAPI().getRoleById(id3).isPresent();
                        break;
                    case ROLE_NAME:
                        typesOk = getParentPlugin().getDiscordAPI().getRolesByName(args[x]).size() > 0;
                        break;
                    case PLUGIN:
                        final String name = args[x];
                        typesOk = getParentPlugin().getPluginManager().getPlugins().stream().map(BotPlugin::getPluginName).anyMatch(s -> s.equals(name));
                        break;
                    case CONSTANT:
                        typesOk = Util.jsonArray_Contains(arg.getJSONArray("values"), args[x]);
                        break;
                    default:
                        typesOk = true;
                        break;
                }
                if (!typesOk) {
                    break;
                }
            }
            if (!typesOk) {
                getLogger().warn("\u001b[31m:x: Command Syntax incorrect");
                getLogger().warn(Util.wrapInCodeBlock("One of your argument types is wrong!"));
                getLogger().warn("COMMAND USAGE:" + manual.getUsage());
                JSONObject argRef = mapObjectByName(badType, manual.getDataJsonObject().getJSONArray("arguments"));
                getLogger().warn("EXPECTED ARGUEMENT TYPE: " + argRef.getString("type"));
                getLogger().warn("GOT: " + badSupp);
                getLogger().warn("See " + MyriadBotLoader.COMMAND_KEY + "mb.man " + manual.getPageName() + " " + (badIndex) + "\u001b[0m");
                return;
            }
        }
        Thread t = new Thread(() -> {
            getExecutor().update(getParentPlugin(), null, null);
            getExecutor().onCommand(null, null, args, null);
            getLogger().info("{} ran a command: {}", "CONSOLE", getTrigger());
        });
        t.setName(getParentPlugin().getPluginName());
        t.start();
    }

    /**
     * Returns the plugin that this command is registered to
     *
     * @return The plugin
     */
    public BotPlugin getParentPlugin() {
        return parent;
    }

    public Optional<PluginManualPage> getCommandManual() {
        return getParentPlugin().getManuasOfType(ManualType.COMMAND_SYNTAX).stream()
                .filter(pluginManualPage -> ((ImplCommandUsageManual) pluginManualPage).getCommand().equals(this)).findAny();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DiscordCommand) && ((DiscordCommand) obj).getTrigger()
                .equals(getTrigger()) && ((DiscordCommand) obj).getParentPlugin().equals(getParentPlugin());
    }

    private JSONObject mapObjectByName(String name, JSONArray objects) {
        for (Object I : objects) {
            if (((JSONObject) I).getString("name").equals(name)) {
                return ((JSONObject) I);
            }
        }
        return null;
    }

    private Logger getLogger() {
        return getParentPlugin().getLogger();
    }
}
