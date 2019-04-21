package com.myththewolf.modbot.core.lib.plugin.manPage.impl;

import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.PluginManualPage;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import com.myththewolf.modbot.core.lib.plugin.manager.interfaces.PluginManager;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.event.message.MessageEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

import java.util.List;
import java.util.stream.Collectors;

public class ManualPageReactionListner implements ReactionAddListener, ReactionRemoveListener {
    private PluginManager manager;

    public ManualPageReactionListner(PluginManager manager) {
        this.manager = manager;
    }

    @Override
    public void onReactionAdd(ReactionAddEvent reactionAddEvent) {
        if (reactionAddEvent.getUser().isYourself() || reactionAddEvent.getEmoji().isCustomEmoji()) {
            return;
        }
        processReaction(reactionAddEvent.getEmoji(), reactionAddEvent);
    }

    @Override
    public void onReactionRemove(ReactionRemoveEvent reactionRemoveEvent) {
        if (reactionRemoveEvent.getUser().isYourself() || reactionRemoveEvent.getEmoji().isCustomEmoji()) {
            return;
        }
        processReaction(reactionRemoveEvent.getEmoji(), reactionRemoveEvent);
    }

    private void processReaction(Emoji emojoi, MessageEvent event) {
        List<ManualPageEmbed> mappedEmbeds = manager.getPlugins().stream().map(BotPlugin::getManuals)
                .flatMap(pluginManualPages -> pluginManualPages.stream()
                        .map(PluginManualPage::getEmbeds))
                .flatMap(List::stream)
                .filter(manualPageEmbed -> manualPageEmbed.getMessage()
                        .getId() == event
                        .getMessageId()).collect(Collectors.toList());
        if (emojoi.asUnicodeEmoji().get().equals("◀")) {
            mappedEmbeds.forEach(ManualPageEmbed::decementPage);
            return;
        }
        if (emojoi.asUnicodeEmoji().get().equals("\uD83C\uDFE0")) {
            mappedEmbeds.forEach(manualPageEmbed -> manualPageEmbed.setCurrentPage(0));
            return;
        }
        if (emojoi.asUnicodeEmoji().get().equals("▶")) {
            mappedEmbeds.forEach(ManualPageEmbed::incrementPage);
            return;
        }
        if (emojoi.asUnicodeEmoji().get().equals("❌")) {
            mappedEmbeds.forEach(ManualPageEmbed::destroy);
            return;
        }
    }
}