package com.myththewolf.modbot.core.invocation.impl;

import com.myththewolf.modbot.core.invocation.interfaces.PluginLoader;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Implementation of PluginLoader
 */
public class ImplPluginLoader implements PluginLoader, Loggable {
    /**
     * This map contains all loaded plugins, it is a hashmap so we can grab a plugin by it's name.
     */
    private HashMap<String, BotPlugin> plugins = new HashMap<>();

    public void loadJarFile(File jar) {
        getLogger().debug("Adding " + jar.getAbsolutePath() + " to this classpath");
        if(!jar.exists()){
            getLogger().warn(jar.getAbsolutePath() + " does not exist, ignoring.");
            return;
        }
        loadAllClassesFor(jar);
    }


    public void loadDirectory(File dir) {
        Arrays.stream(dir.listFiles()).filter(file -> file.getName().endsWith(".jar")).forEach(jar -> {
            loadJarFile(jar);
        });
    }


    public void enablePlugin(BotPlugin plugin) {

    }

    private synchronized void loadAllClassesFor(File jar) {
        try {
            /*We are using reflection here to circumvent encapsulation; addURL is not public*/
            java.net.URLClassLoader loader = (java.net.URLClassLoader) ClassLoader.getSystemClassLoader();
            java.net.URL url = jar.toURI().toURL();
            /*Disallow if already loaded*/
            for (java.net.URL it : java.util.Arrays.asList(loader.getURLs())) {
                if (it.equals(url)) {
                    return;
                }
            }
            java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
            method.setAccessible(true); /*promote the method to public access*/
            method.invoke(loader, new Object[]{url});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
