package com.myththewolf.modbot.core.API.command;


import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.channels.ServerTextChannel;
import de.btobastian.javacord.entities.message.Message;

/**
 * The interface in which command classes should implement
 */
public interface CommandExecutor {
    /**
     * Runs when the command registered to this class is triggered from a readable text channel
     *
     * @param sourceChannel The TextChannel in which the command was ran in
     * @param sender        The user who had ran this command
     * @param args          A array of Strings that denotes arguments. This is taken by splitting the initial messages by spaces, but removing the first index, as the first index is the command itself.
     * @param source        The original Message,unmodified, in which triggered this command.
     */
    public void onCommand(ServerTextChannel sourceChannel, User sender, String[] args, Message source);
}
