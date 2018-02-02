package com.myththewolf.modbot.core.lib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interface is designed to make sure we implement logging where needed
 */
public interface Loggable {
    /**
     * Returns the logger of this class
     * @return The logger
     */
    default Logger getLogger(){
        return LoggerFactory.getLogger(getClass());
    }
}
