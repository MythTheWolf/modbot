package com.myththewolf.modbot.core.invocation.interfaces;

import java.io.File;

/**
 * This interface blueprints our plugin loader
 */
public interface PluginLoader {
    void loadJarFile(File jar);
    void loadDirectory(File dir);
    void clean();
}
