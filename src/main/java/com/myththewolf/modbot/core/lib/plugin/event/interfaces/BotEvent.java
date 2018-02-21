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

package com.myththewolf.modbot.core.lib.plugin.event.interfaces;

import com.myththewolf.modbot.core.lib.plugin.impl.BotPlugin;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;

import java.util.Optional;

/**
 * Interface for any events, as all these methods must be implemented per event
 */
public interface BotEvent {
    /**
     * Gets the user that fired this event
     * There may not always be a user
     *
     * @return User optional
     */
    Optional<User> getUser();

    /**
     * Gets the server this event fired in
     *
     * @return Server optional
     */
    Optional<Server> getServer();

    /**
     * Returns this event type
     *
     * @return The event type
     */
    EventType getEventType();

    /**
     * Gets the plugin that this event belongs to
     *
     * @return The plugin
     */
    BotPlugin getPlugin();
}
