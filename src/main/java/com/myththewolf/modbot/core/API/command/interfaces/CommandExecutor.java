package com.myththewolf.modbot.core.API.command.interfaces;


import com.myththewolf.modbot.core.lib.plugin.impl.BotPlugin;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.channels.TextChannel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAuthor;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.Optional;


/**
 * CommandExecutor class to be extended to plugins
 * This class is abstract so we can package helper methods, yet the onCommand can be handled by the plugin dev
 */
public abstract class CommandExecutor implements CommandAdapater {
    /**
     * The TextChannel from the last command ran
     */
    private TextChannel lastTextChannel;
    /**
     * The Message from the last command ran
     */
    private Message lastMessage;
    /**
     * The plugin this command is registered
     */
    private BotPlugin plugin;

    /**
     * Updates the cache so when a command is ran, the helper methods work to the latest
     *
     * @param newTextChannel The new text channel from the command
     * @param source         The command message source
     */
    public void update(BotPlugin plugin, TextChannel newTextChannel, Message source) {
        this.lastTextChannel = newTextChannel;
        this.lastMessage = source;
        this.plugin = plugin;
    }

    /**
     * Sends the specified message to the channel in which the command was sent
     *
     * @param content The message to be sent
     */
    public void reply(String content) {
        getLastTextChannel().sendMessage(content).exceptionally(Javacord::exceptionLogger);
    }

    /**
     * Sends the specified messasage embed to the channel in which the command was sent
     *
     * @param embedBuilder The message embed to be sent
     */
    public void reply(EmbedBuilder embedBuilder) {
        getLastTextChannel().sendMessage(embedBuilder).exceptionally(Javacord::exceptionLogger);
    }

    /**
     * Sends a friendly message embed that gives a "success" look with the specified message
     *
     * @param content The message to send within the embed.
     */
    public void succeeded(String content) {
        succeeded(content, "The command completed successfully", "Success");
    }

    /**
     * Sends a friendly MessageEmbed that gives a "success" look with the specified content
     *
     * @param content The message to send within the embed.
     * @param footer  The footer to bind to the embed
     * @param title   The title to bind to the embed
     */
    public void succeeded(String content, String footer, String title) {
        EmbedBuilder succ = new EmbedBuilder();
        succ.setColor(Color.GREEN);
        succ.setTitle(title);
        succ.setFooter(footer);
        succ.setDescription(content);
        reply(succ);
    }

    /**
     * Sends a red "failed" MessageEmbed with a specified content
     *
     * @param content
     */
    public void failed(String content) {
        failed(content, "Errors occurred while processing your command", "Error");
    }

    /**
     * Sends a red "failed" MessageEmbed with the specified content
     *
     * @param content The message to send within the embed.
     * @param footer  The footer to bind to the embed
     * @param title   The title to bind to the embed
     */
    public void failed(String content, String footer, String title) {
        EmbedBuilder fail = new EmbedBuilder().setAuthor(getLastAuthor().get().getName(), null, getLastAuthor().get().getAvatar().getUrl().toString());
        fail.setFooter(footer);
        fail.setTitle(title);
        fail.setDescription(content);
        reply(fail);
    }

    /**
     * Deletes the message that triggered this command.
     */
    public void deleteTriggerMessage() {
        getLastMessage().delete().exceptionally(Javacord::exceptionLogger);
    }

    /**
     * Gets the last known message of this command run
     *
     * @return The message
     */
    public Message getLastMessage() {
        return lastMessage;
    }

    /**
     * Gets the last known User who ran this command
     *
     * @return Optional, as the message may be deleted
     */
    public Optional<MessageAuthor> getLastAuthor() {
        return Optional.ofNullable(getLastMessage() != null ? getLastMessage().getAuthor() : null);
    }

    /**
     * Gets the last known TextChannel in which this command was ran from
     *
     * @return The TextChannel
     */
    public TextChannel getLastTextChannel() {
        return lastTextChannel;
    }

    /**
     * Gets the plugin instance of this command
     *
     * @return The plugin
     */
    public BotPlugin getPlugin() {
        return plugin;
    }
}
