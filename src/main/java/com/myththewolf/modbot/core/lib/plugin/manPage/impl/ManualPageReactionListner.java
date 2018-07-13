package com.myththewolf.modbot.core.lib.plugin.manPage.impl;

import com.myththewolf.modbot.core.lib.plugin.invocation.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.invocation.interfaces.PluginManager;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.PluginManualPage;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import java.util.List;
import java.util.stream.Collectors;

public class ManualPageReactionListner implements ReactionAddListener {
    private PluginManager manager;

    public ManualPageReactionListner(PluginManager manager) {
        this.manager = manager;
    }

    @Override
    public void onReactionAdd(ReactionAddEvent reactionAddEvent) {
        if (reactionAddEvent.getUser().isYourself() || reactionAddEvent.getEmoji().isCustomEmoji()) {
            return;
        }

        List<ManualPageEmbed> mappedEmbeds = manager.getPlugins().stream().map(BotPlugin::getManuals)
                .flatMap(pluginManualPages -> pluginManualPages.stream()
                        .map(PluginManualPage::getEmbeds))
                .flatMap(List::stream)
                .filter(manualPageEmbed -> manualPageEmbed.getMessage()
                        .getId() == reactionAddEvent
                        .getMessageId()).collect(Collectors.toList());

        if(reactionAddEvent.getEmoji().asUnicodeEmoji().get().equals("◀")){
           mappedEmbeds.forEach(ManualPageEmbed::decementPage);
           return;
        }
        if(reactionAddEvent.getEmoji().asUnicodeEmoji().get().equals("▶")){
            mappedEmbeds.forEach(ManualPageEmbed::incrementPage);
            return;
        }
        if(reactionAddEvent.getEmoji().asUnicodeEmoji().get().equals("❌")){
            mappedEmbeds.forEach(ManualPageEmbed::destroy);
            return;
        }
    }
}