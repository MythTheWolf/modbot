package com.myththewolf.modbot.core.invocation.impl;

import com.myththewolf.modbot.core.invocation.interfaces.PluginLoader;

import java.io.File;

/**
 * Implementation of PluginLoader
 */
public class ImplPluginLoader implements PluginLoader {
    public void loadJarFile(File jar) {

    }

    public void loadDirectory(File dir) {

    }

    public void clean() {

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
