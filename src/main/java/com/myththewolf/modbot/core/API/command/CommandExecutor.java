package com.myththewolf.modbot.core.API.command;

import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.channels.TextChannel;
import de.btobastian.javacord.entities.message.MessageAuthor;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

import java.awt.*;

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
     * The MessageAuthor from the last command ran
     */
    private MessageAuthor lastAuthor;

    /**
     * Updates the cache so when a command is ran, the helper methods work to the latest
     *
     * @param newTextChannel   The new text channel from the command
     * @param newMessageAuthor The new message author from the command
     */
    public void update(TextChannel newTextChannel, MessageAuthor newMessageAuthor) {
        this.lastTextChannel = newTextChannel;
        this.lastAuthor = newMessageAuthor;
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
        getLastTextChannel().sendMessage(embedBuilder);
    }

    /**
     * Sends a friendly message embed that gives a "success" look with the specified message
     *
     * @param content The message to send within the embed.
     */
    public void succeeded(String content) {
        succeeded(content,"The command completed successfully","Success");
    }

    /**
     * Sends a friendly message embed that gives a "success" look with the specified message
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
    }

    /**
     * Sends a red "failed" message embed with a specified message
     *
     * @param content
     */
    public void failed(String content) {
    }

    /**
     * Sends a red "failed" message embed with the specified message
     *
     * @param content The message to send within the embed.
     * @param footer  The footer to bind to the embed
     * @param title   The title to bind to the embed
     */
    public void failed(String content, String footer, String title) {
    }

    /**
     * Deletes the message that triggered this command.
     */
    public void deleteTriggerMessage() {
    }

    /**
     * Gets the last known User who ran this command
     *
     * @return The user
     */
    public MessageAuthor getLastAuthor() {
        return lastAuthor;
    }

    /**
     * Gets the last known TextChannel in which this command was ran from
     *
     * @return
     */
    public TextChannel getLastTextChannel() {
        return lastTextChannel;
    }
}
