package com.myththewolf.modbot.core.lib;

import com.myththewolf.modbot.core.ModBotCoreLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Optional;

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
            getLogger().error("A internal error has occurred: {}", exception);
        }
        return Optional.ofNullable(is);
    }

    /**
     * Gets the logger from the Main class
     *
     * @return The logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ModBotCoreLoader.class);
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
            finalStr += array[i];
        }
        return finalStr;
    }
}
