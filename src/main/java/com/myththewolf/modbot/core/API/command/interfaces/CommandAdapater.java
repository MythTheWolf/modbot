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

package com.myththewolf.modbot.core.API.command.interfaces;


import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import java.util.Optional;

/**
 * The interface in which command classes should implement
 */
public interface CommandAdapater {
    /**
     * Runs when the command registered to this class is triggered from a readable text channel
     *
     * @param sourceChannel The TextChannel in which the command was ran in
     * @param sender        The user who had ran this command
     * @param args          A array of Strings that denotes arguments. This is taken by splitting the initial messages by spaces, but removing the first index, as the first index is the command itself.
     * @param source        The original Message,unmodified, in which triggered this command.
     */
    void onCommand(Optional<TextChannel> sourceChannel, Optional<MessageAuthor> sender, String[] args, Optional<Message> source);

}
