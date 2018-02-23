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

package com.myththewolf.modbot.core.lib.plugin.manPage.interfaces;

/**
 * This Enum represents all the different manual types
 */
public enum ManualType {
    /**
     * A syntax manual. This should include the command itslef,arguments,permissions and use cases.
     */
    COMMAND_SYNTAX,
    /**
     * A "about this [thing]" manual. This is not strictly typed.
     */
    ABOUT,
    /**
     * General help manual, this should be for use cases and tutorials about commands or functions.
     */
    USAGE,
    /**
     * A abstract manual, this is loose, and is just here for pagination help
     */
    ABSTRACT
}
