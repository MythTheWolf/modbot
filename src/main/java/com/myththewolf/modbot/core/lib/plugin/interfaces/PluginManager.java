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

package com.myththewolf.modbot.core.lib.plugin.interfaces;

import com.myththewolf.modbot.core.lib.plugin.impl.BotPlugin;

import java.io.File;
import java.util.List;

/**
 * This interface blueprints our plugin loader
 */
public interface PluginManager {
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
     * Gets all loaded plugins
     *
     * @return A list of all loaded plugins
     * @apiNote This includes all plugins, not just enabled ones.
     */
    List<BotPlugin> getPlugins();
}
