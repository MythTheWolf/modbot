package com.myththewolf.modbot.core.lib.event.interfaces;

import com.myththewolf.modbot.core.lib.event.impl.UserCommandEvent;

public enum EventType {

    COMMAND_RUN(UserCommandEvent.class);


    Class dataClass;

    EventType(Class clazz) {
        dataClass = clazz;
    }

    public Class getDataClass() {
        return dataClass;
    }
}
