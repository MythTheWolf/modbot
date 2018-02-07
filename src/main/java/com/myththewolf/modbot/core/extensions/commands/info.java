package com.myththewolf.modbot.core.extensions.commands;

import com.myththewolf.modbot.core.API.command.CommandExecutor;
import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import de.btobastian.javacord.entities.channels.TextChannel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAuthor;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;


public class info extends CommandExecutor {
    private PluginManager manager;

    public info(PluginManager manager) {
        this.manager = manager;
    }

    @Override
    public void onCommand(TextChannel sourceChannel, MessageAuthor sender, String[] args, Message source) {
        int numPlugins = manager.getPlugins().size();
        long numCommands = manager.getPlugins().stream().map(BotPlugin::getCommands).count();
        EmbedBuilder infoEmbed = new EmbedBuilder();
        infoEmbed.setTitle("System Info");
        infoEmbed.addField("Plugins", numPlugins + " loaded;" + numCommands + "  registered commands", false);
        reply(infoEmbed);
    }
}
