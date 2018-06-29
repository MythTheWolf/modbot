/*
 * Copyright (c) 2018 MythTheWolf
 *  Nicholas Agner, USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.myththewolf.modbot.core.lib.plugin.manPage.impl;

import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.ManualType;
import com.myththewolf.modbot.core.lib.plugin.manPage.interfaces.PluginManualPage;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.concurrent.CompletableFuture;

/**
 * This class represents a ManualPageEmbed, a manual page display
 */
public class ManualPageEmbed {
    /**
     * The manual this embed is representing
     */
    private PluginManualPage manual;
    /**
     * The current page displayed. Can be a String or a EmbedBuilder
     */
    private Object currentPage;
    /**
     * The current page, but as a integer index
     */
    private int spot;
    /**
     * The current channel the embed is in
     */
    private TextChannel channel;
    /**
     * The embed as a message
     */
    private Message message;

    private Reaction GO_BACK_REACTION;


    private Reaction GO_FORWARD_REACTIOn;

    /**
     * Constructs a ManualPageEmbed
     *
     * @param manualPage The manual to show
     * @param scope      Where to show it
     */
    public ManualPageEmbed(PluginManualPage manualPage, TextChannel scope) {
        this.manual = manualPage;
        this.channel = scope;
    }

    /**
     * Gets the manual type
     *
     * @return The type
     */
    public ManualType getType() {
        return manual.getManualType();
    }

    /**
     * Creates a new message with page 1 of the manual displayed
     */
    public void instaniateEmbed() {
        CompletableFuture<Message> dummy = (manual
                .getPageOf(0) instanceof EmbedBuilder ? channel
                .sendMessage((EmbedBuilder) manual.getPageOf(0)) : channel
                .sendMessage((String) manual.getPageOf(0)));
        dummy.thenAccept(message1 -> {
            spot = 0;
            currentPage = manual.getPageOf(0);
            this.message = message1;
            this.message.addReaction("◀").exceptionally(ExceptionLogger.get());
            this.message.addReaction("❌").exceptionally(ExceptionLogger.get());
            this.message.addReaction("▶").exceptionally(ExceptionLogger.get());
        }).exceptionally(ExceptionLogger.get());

    }

    /**
     * Gets the current page,but as the array index
     *
     * @return The array index of the current page
     */
    public int getSpot() {
        return spot;
    }

    /**
     * Gets the current manual page being displayed
     *
     * @return The page, either a String or EmbedBuilder
     */
    public Object getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets the spot of this page
     *
     * @param page
     */
    public void setCurrentPage(int page) {
        spot = page;
        getMessage().edit((EmbedBuilder) getManual().getPageOf(page)).exceptionally(ExceptionLogger.get());
    }

    /**
     * Gets the current channel the manual is being displayed on
     *
     * @return The text channel
     */
    public TextChannel getChannel() {
        return channel;
    }

    /**
     * Gets the manual being displayed
     *
     * @return The manual
     */
    public PluginManualPage getManual() {
        return manual;
    }

    /**
     * Gets the manual page's embed but as a message
     *
     * @return The message
     */
    public Message getMessage() {
        return message;
    }

    public void incrementPage() {
        Object result = getManual().getPageOf(getSpot() + 1);
        if (result == null) {
            setCurrentPage(0);
        }
    }

    public void decementPage() {
        Object result = getManual().getPageOf(getSpot() - 1);
        if (result == null) {
            setCurrentPage(getManual().getTotalNumberPages());
        }

    }

    /**
     * Deletes the message with this embed
     */
    public void destroy() {
        getMessage().delete().exceptionally(ExceptionLogger.get());
    }
}
