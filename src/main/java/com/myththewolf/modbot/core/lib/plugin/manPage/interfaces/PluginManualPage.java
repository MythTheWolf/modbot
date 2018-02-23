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

package com.myththewolf.modbot.core.lib.plugin.manPage.interfaces;

import com.myththewolf.modbot.core.lib.plugin.invocation.impl.BotPlugin;
import de.btobastian.javacord.entities.channels.TextChannel;

import java.util.List;

public interface PluginManualPage {
    /**
     * Gets a list of all embeds that are attached to this manual page
     *
     * @return The message(s)
     */
    List<ManualPageEmbed> getEmbeds();

    /**
     * Gets the plugin that is mapped to this manual page
     *
     * @return The plugin
     */
    BotPlugin getPlugin();

    /**
     * Gets the name of this manual page
     *
     * @return The name
     */
    String getPageName();

    /**
     * Gets the manual type
     *
     * @return The Type
     */
    ManualType getManualType();

    /**
     * Gets the page (decoded) of the desired index
     *
     * @param index The desired index
     * @return Object of type String or EmbedBuilder
     */
    Object getPageOf(int index);

    /**
     * Displays a new Embed for this manual page
     */
    void displayNewEmbed(TextChannel scope);
}
