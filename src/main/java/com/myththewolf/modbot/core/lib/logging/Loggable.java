package com.myththewolf.modbot.core.lib.logging;

import org.slf4j.Logger;

/**
 * This interface is designed to make sure we implement logging where needed
 */
public interface Loggable {
    /**
     * Returns the logger of this class
     * @return The logger
     */
    public Logger getLogger();
}
