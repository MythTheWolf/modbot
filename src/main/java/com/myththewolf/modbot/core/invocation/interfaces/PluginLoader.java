package com.myththewolf.modbot.core.invocation.interfaces;

import com.myththewolf.modbot.core.invocation.impl.BotPlugin;

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

    /**
     * This prepares the plugin's default config and file structure, and calls @link{BotPlugin#onEnable}
     *
     * @param plugin The plugin to enable
     */
    void enablePlugin(BotPlugin plugin);
}
