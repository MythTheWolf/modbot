/*
 * Copyright (c) 2018 MythTheWolf
 *  Nicholas Agner, USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.myththewolf.modbot.core.lib.plugin.impl;

import com.myththewolf.modbot.core.lib.logging.Loggable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This is a custom  URL Class Loader so we can load plugins and keep them secure.
 */
public class PluginClassLoader extends URLClassLoader implements Loggable {
    public PluginClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }

    public void loadJarFile(File jar) {
        try {
            addURL(jar.toURI().toURL());
        } catch (MalformedURLException exception) {
            getLogger().error("Error while importing jar file from PluginClassLoader: {}", exception);
        }
    }

    public Class<?> getClassByName(String clazz) {

        try {
            return loadClass(clazz);
        } catch (ClassNotFoundException exception) {
            getLogger().error("Error while importing jar file from PluginClassLoader: {}", exception);
        }
        return null;
    }
}
