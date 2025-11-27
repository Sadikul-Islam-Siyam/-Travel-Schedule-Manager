package com.travelmanager.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.time.LocalDateTime;

/**
 * Utility class for data persistence
 */
public class DataManager {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public static <T> void saveToFile(T object, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(object, writer);
        }
    }

    public static <T> T loadFromFile(String filename, Class<T> classType) throws IOException {
        try (FileReader reader = new FileReader(filename)) {
            return gson.fromJson(reader, classType);
        }
    }
}
