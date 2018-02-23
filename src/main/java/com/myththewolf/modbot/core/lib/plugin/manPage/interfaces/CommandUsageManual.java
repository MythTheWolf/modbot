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

import com.myththewolf.modbot.core.API.command.impl.DiscordCommand;

/**
 * This interface represents a command usage/syntax manual.
 */
public interface CommandUsageManual extends PluginManualPage {
    /**
     * Gets the executor that this manual page is for
     *
     * @return The list of alias(es)
     */
    DiscordCommand getCommand();

    @Override
    default ManualType getManualType() {
        return ManualType.COMMAND_SYNTAX;
    }

    /**
     * Gets the number of required command arguments
     *
     * @return The number of required command arguments
     */
    int getNumRequiredArgs();

    /**
     * Returns the String literal of this command's syntax
     *
     * @return The syntax explanation
     */
    String getUsage();
}
