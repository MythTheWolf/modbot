package com.myththewolf.modbot.core.lib.event.interfaces;

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
     * @return User optional
     */
    Optional<User> getUser();

    /**
     *Gets the server this event fired in
     * @return Server optional
     */
    Optional<Server> getServer();

    /**
     * Returns this event type
     *
     * @return The event type
     */
    EventType getEventType();
}
