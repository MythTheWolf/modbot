package com.myththewolf.modbot.core.systemPlugin.commands;

import com.myththewolf.modbot.core.MyriadBotLoader;
import com.myththewolf.modbot.core.lib.Util;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.lib.plugin.manPage.impl.ManualPageEmbed;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.PluginManualPage;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.manager.interfaces.PluginManager;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.List;
import java.util.stream.Collectors;

public class shutdown implements SystemCommand,Loggable {
    private MyriadBotLoader loader;
    private PluginManager pluginManager;
    private Message message;

    public shutdown(PluginManager manager, MyriadBotLoader loader) {
        this.loader = loader;
        this.pluginManager = manager;
    }
    @Override
    public void onCommand(MessageAuthor author, Message message) {
        this.message = message;
        if(!Util.canRunSupercommand(author,message,loader)){
            message.getChannel().sendMessage(":thinking: You aren't the bot owner or a superuser!").exceptionally(ExceptionLogger
                    .get());
            return;
        }
        message.getApi().updateStatus(UserStatus.DO_NOT_DISTURB);
        reply("Shutdown: Deleting all manual embeds");
        pluginManager.getPlugins().forEach(plugin -> {

           List<ManualPageEmbed> dummy =  plugin.getManuals().stream().map(PluginManualPage::getEmbeds).flatMap(List::stream).collect(Collectors
                    .toList());
           dummy.forEach(ManualPageEmbed::destroy);
            reply("Shutdown: Stopping all tasks");
            //TODO: Task system
            reply("Shutdown: Saving all configs");
            //TODO: Implement config saving
            reply("Shutdown: Disabling all plugins");
            pluginManager.getPlugins().forEach(BotPlugin::disablePlugin);
            this.message.getChannel().sendMessage("Closing bot and shutting down.").thenAccept(message1 -> {
                message1.getApi().disconnect();
                System.exit(0);
            });
        });
    }

    void reply(String message){
        getLogger().debug(message);
        this.message.getChannel().sendMessage(message).exceptionally(ExceptionLogger.get());
    }
}
