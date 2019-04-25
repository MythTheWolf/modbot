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

package com.myththewolf.modbot.core;

import com.myththewolf.modbot.core.lib.plugin.event.interfaces.BotEvent;
import com.myththewolf.modbot.core.lib.plugin.event.interfaces.EventHandler;
import com.myththewolf.modbot.core.lib.plugin.manager.impl.BotPlugin;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Utils Class, contains random Utilities that will be used more than once
 */
public class Util {
    /**
     * Converts a InputStream to a String
     *
     * @param source The InputStream to convert
     * @return A Optional, empty if the source is null.
     */
    public static Optional<String> inputStreamToString(InputStream source) {
        String decoded = "";
        String pivot;
        if (source == null) {
            decoded = null;
            return Optional.ofNullable(decoded);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(source));

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

    /**
     * Gets a InputStream sourced from a File inside a jar file
     *
     * @param theJar    The jar file to extract the resource from
     * @param pathInJar The file inside the jar
     * @return A Optional, empty if the resource doesn't exist.
     */
    public static Optional<InputStream> getResourceFromJar(File theJar, String pathInJar) {
        InputStream is = null;
        try {
            URL url = new URL("jar:file:" + theJar.getAbsolutePath() + "!/" + pathInJar);
            is = url.openStream();
        } catch (IOException exception) {
            is = null;
        }
        return Optional.ofNullable(is);
    }

    /**
     * Gets the logger from the Main class
     *
     * @return The logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(MyriadBotLoader.class);
    }

    /**
     * Reads a file and gets the contents in a string.
     *
     * @param source The file to read
     * @return A optional, empty if the file does not exist or is not readable.
     */
    public static Optional<String> readFile(File source) {
        String line = null;
        String fin = "";
        try {
            FileReader fileReader =
                    new FileReader(source);
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                fin += line;
            }
            bufferedReader.close();
        } catch (Exception exception) {
            getLogger().error("Internal Exception in Utils class: {}", exception);
            fin = null;
        }
        return Optional.ofNullable(fin);
    }

    /**
     * Writes a given string to file
     *
     * @param content The string to write
     * @param out     The file to write out to.
     */
    public static void writeToFile(String content, File out) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            getLogger().error("Internal error in Utils class: Could not write to file! \n File: {} \n Content: {}", (out != null ? out.getAbsolutePath() : null), content);
        }
    }

    /**
     * Combines a array to a single string sentence
     * @param startIndex Where to start from in the array
     * @param array The array
     * @return The newly formed string
     */
    public static String arrayToString(int startIndex,String[] array){
        String finalStr = "";
        for(int i=startIndex; i<array.length; i++){
            finalStr += array[i] +" ";
        }
        return finalStr.trim();
    }

    /**
     * Gets a color by name
     *
     * @param name The color name
     * @return The color
     */
    public static Color getColorByName(String name) {
        try {
            return (Color) Color.class.getField(name.toUpperCase()).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            getLogger().warn("Invalid color name: {}" + name);
            return Color.BLACK;
        }
    }

    public static String wrapInCodeBlock(String source) {
        return "```" + source + "```";
    }
    public static String milisToTimeString(long l){
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d hours, %02d min, %02d sec, %03d ms", hr, min, sec, ms);
    }

    public static boolean canRunSupercommand(MessageAuthor author, Message message, MyriadBotLoader loader) {
        String OWNER_ID = loader.getRunConfig().getString("ownerID");
        List<Object> ids = loader.getRunConfig().getJSONArray("manager-roles").toList();
        boolean isManager = author.asUser().get().getRoles(message.getServer().get()).stream().anyMatch(role -> ids.contains(role.getIdAsString()));
        return isManager || author.getIdAsString().equals(OWNER_ID);
    }

    public static boolean isNumber(String in){
        try {
            Integer.parseInt(in);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public static boolean fireEvent(BotEvent event) {
        boolean result = false;
        MyriadBotLoader.PM.getPlugins().stream().flatMap(plugin -> plugin.getEventsOfType(event.getEventType()).stream()).forEachOrdered(runner -> {
            Optional<Method> methodOptional = Arrays.stream(runner.getClass().getMethods())
                    .filter(method -> method.isAnnotationPresent(EventHandler.class)).findAny();
            if (!methodOptional.isPresent()) {
                getLogger()
                        .warn("Could not pass event of  {} to class '{}', no runner method found", event.getEventType().toString(), runner
                                .getClass().getName());
            } else {
                try {
                    methodOptional.get()
                            .invoke(runner, event, runnerToBotPlugin(runner));
                } catch (Exception e) {
                    getLogger()
                            .error("Could not pass event of type {} to class '{}': Internal error! (Our fault): {}", event.getEventType().toString(), runner
                                    .getClass().getName(), e.getMessage());
                }
            }
        });
        return event.isCancelled();
    }

    public static boolean jsonArray_Contains(JSONArray needle, Object haystack) {
        for (Object I : needle) {
            if (I.equals(haystack))
                return true;
        }
        return false;
    }
    private static BotPlugin runnerToBotPlugin(Object runner) {
        return MyriadBotLoader.PM.getPlugins().stream().filter((BotPlugin plugin) -> plugin.getEvents().stream()
                .anyMatch(o -> o.getClass().getName().equals(runner.getClass().getName())))
                .findFirst().orElse(null);
    }
}

