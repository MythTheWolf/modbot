package com.myththewolf.modbot.core.invocation.interfaces;

import com.myththewolf.modbot.core.invocation.impl.BotPlugin;

import java.io.File;

/**
 * This interface blueprints our plugin loader
 */
public interface PluginLoader {
    void loadJarFile(File jar);
    void loadDirectory(File dir);
    void enablePlugin(BotPlugin plugin);
}
