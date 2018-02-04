package com.myththewolf.modbot.core.invocation.impl;

import com.myththewolf.modbot.core.invocation.interfaces.PluginLoader;
import com.myththewolf.modbot.core.lib.logging.Loggable;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

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
        if (!jar.exists()) {
            getLogger().warn(jar.getAbsolutePath() + " does not exist, ignoring.");
            return;
        }
        loadAllClassesFor(jar);
        JSONObject runconfig = null;
        try {
            runconfig = getResourceFromJar(jar, "runconfig.json").flatMap(this::inputStreamToString).map(JSONObject::new).orElseThrow(FileNotFoundException::new);
            if (runconfig.isNull("main-class")) {
                getLogger().warn("Error while enabling plugin: {}, key 'main-class' in runconfig.json is not optional.", jar.getAbsolutePath());
                getLogger().debug("Runconfig JSON: {}", runconfig.toString());
                return;
            }
            Class C = Class.forName(runconfig.getString("main-class"));
            try {
                Object instance = C.newInstance();
                if (!(instance instanceof BotPlugin)) {
                    getLogger().warn("Error while enabling plugin: {}, class '{}' does not extend BotPlugin", jar.getAbsolutePath(), C.getName());
                    return;
                }
                ((BotPlugin) instance).onEnable();
            } catch (InstantiationException | IllegalAccessException e) {
                getLogger().error("Internal Exception while loading plugin: {}, ", jar.getAbsolutePath(), e);
                return;
            }

        } catch (FileNotFoundException e) {
            getLogger().warn("Error while enabling plugin: {}, runconfig.json was not found: ", jar.getAbsolutePath(), e);
        } catch (ClassNotFoundException e) {
            getLogger().warn("Error while enabling plugin: {}, class '{}' does not exist!", jar.getAbsolutePath(), runconfig.getString("main-class"));
        }

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

    private Optional<InputStream> getResourceFromJar(File theJar, String pathInJar) {
        InputStream is = null;
        try {
            URL url = new URL("jar:file:" + theJar.getAbsolutePath() + "!/" + pathInJar);
            is = url.openStream();
        } catch (IOException exception) {
            getLogger().error("A internal error has occurred: {}", exception);
        }
        return Optional.ofNullable(is);
    }

    private Optional<String> inputStreamToString(InputStream source) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(source));
        String decoded = "";
        String pivot;
        try {
            while ((pivot = reader.readLine()) != null) {
                decoded += pivot;
            }
        } catch (IOException exception) {
            getLogger().error("A internal error has occurred: {}", exception);
            decoded = null;
        }
        return Optional.ofNullable(decoded);
    }
}
