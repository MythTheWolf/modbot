package com.myththewolf.modbot.core.API.command.impl;


import com.myththewolf.modbot.core.API.command.interfaces.CommandExecutor;
import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import de.btobastian.javacord.entities.channels.TextChannel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAuthor;

/**
 * This class represents a CommandExecutor container for easy control
 */
public class DiscordCommand implements Loggable {
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
        Thread commandThread = new Thread(() -> {
            String[] args = source.getContent().substring(getTrigger().length(), source.getContent().length()).split(" ");
            getExecutor().update(getParentPlugin(), channel, source);
            getExecutor().onCommand(channel, user, args, source);
            getLogger().info("{} ran a command: {}", user.getName(), getTrigger());
        });
        commandThread.setName(getParentPlugin().getPluginName());
        commandThread.start();
    }

    /**
     * Returns the plugin that this command is registered to
     *
     * @return The plugin
     */
    public BotPlugin getParentPlugin() {
        return parent;
    }
}