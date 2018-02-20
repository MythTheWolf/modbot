package com.myththewolf.modbot.core.lib.event.interfaces;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
}
