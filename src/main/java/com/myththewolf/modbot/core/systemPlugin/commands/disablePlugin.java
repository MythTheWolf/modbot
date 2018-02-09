package com.myththewolf.modbot.core.systemPlugin.commands;

import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAuthor;
import de.btobastian.javacord.entities.permissions.PermissionState;
import de.btobastian.javacord.entities.permissions.PermissionType;


public class disablePlugin implements SystemCommand {
    private PluginManager pluginManager;
    private int affected = 0;

    public disablePlugin(PluginManager manager) {
        this.pluginManager = manager;
    }

    @Override
    public void onCommand(MessageAuthor author, Message message) {
        affected = 0;
        if (!(message.getContent().split(" ")[1].length() >= 2)) {
            message.getChannel().sendMessage(":warning: Invalid parameter length! Usage: `>pldisable <plugin name>`").exceptionally(Javacord::exceptionLogger);
            return;
        }
        if (message.getServer().isPresent() && (message.getServer().get().getPermissionsOf(author.asUser().get()).getState(PermissionType.ADMINISTRATOR).equals(PermissionState.ALLOWED))) {
            pluginManager.getPlugins().stream().filter(botPlugin -> botPlugin.getPluginName().equals(Util.arrayToString(1, message.getContent().split(" ")))).findFirst().ifPresent(plugin -> {
                affected++;
                message.getChannel().sendMessage(":white_check_mark: Disabled plugin.").exceptionally(Javacord::exceptionLogger);
                plugin.disablePlugin();
            });
            if (affected == 0) {
                message.getChannel().sendMessage(":warning: No plugin found by that name").exceptionally(Javacord::exceptionLogger);
            }
            return;
        } else {
            message.getChannel().sendMessage(":warning: Invalid permissions").exceptionally(Javacord::exceptionLogger);
        }


    }
}
