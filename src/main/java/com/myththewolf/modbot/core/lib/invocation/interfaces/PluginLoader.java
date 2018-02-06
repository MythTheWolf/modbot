package com.myththewolf.modbot.core.lib.invocation.interfaces;

import com.myththewolf.modbot.core.lib.invocation.impl.BotPlugin;

import java.io.File;

/**
 * This interface blueprints our plugin loader
 */
public interface PluginLoader {
    /**
     * This invokes @link{#loadAllClassesFor} given a plugin jar
     *
     * @param jar A file object pointing to a plugin JAR
     */
    void loadJarFile(File jar);

    /**
     * This will load all plugins given a directory by invoking @link{#loadJarFile}
     *
     * @param dir A file object pointing to a plugin directory
     */
    void loadDirectory(File dir);
}
