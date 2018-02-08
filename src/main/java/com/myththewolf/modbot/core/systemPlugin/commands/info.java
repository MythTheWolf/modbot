package com.myththewolf.modbot.core.systemPlugin.commands;

import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAuthor;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

import java.awt.*;


public class info implements SystemCommand {
    private PluginManager manager;

    public info(PluginManager manager) {
        this.manager = manager;
    }

    String plList = "";

    @Override
    public void onCommand(MessageAuthor author, Message message) {
        int numPlugins = manager.getPlugins().size();
        long numCommands = manager.getPlugins().stream().map(BotPlugin::getCommands).count();
        EmbedBuilder infoEmbed = new EmbedBuilder();
        infoEmbed.setTitle("System Info");
        infoEmbed.setColor(Color.GREEN);
        plList = "";
        manager.getPlugins().forEach(plugin -> plList += plugin.getPluginName() + " ");
        infoEmbed.addField("Plugins:", plList, false);
        infoEmbed.setFooter(numPlugins + " loaded plugins, with " + numCommands + " total commands.");
        message.getChannel().sendMessage(infoEmbed).exceptionally(Javacord::exceptionLogger);
    }
}
