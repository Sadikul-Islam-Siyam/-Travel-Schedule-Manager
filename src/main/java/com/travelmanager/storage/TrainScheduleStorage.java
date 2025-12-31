package com.travelmanager.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.travelmanager.model.rest.TrainScheduleDTO;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage manager for train schedules using JSON persistence
 * Thread-safe with in-memory caching
 */
public class TrainScheduleStorage {
    private static final String DATA_DIR = "data";
    private static final String TRAIN_SCHEDULES_FILE = "train_schedules.json";
    private static TrainScheduleStorage instance;
    
    private final Map<String, TrainScheduleDTO> trainSchedules;
    private final Gson gson;
    private final Path filePath;

    private TrainScheduleStorage() {
        this.trainSchedules = new ConcurrentHashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.filePath = Paths.get(DATA_DIR, TRAIN_SCHEDULES_FILE);
        
        // Ensure data directory exists
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
        
        loadFromFile();
    }

    public static synchronized TrainScheduleStorage getInstance() {
        if (instance == null) {
            instance = new TrainScheduleStorage();
        }
        return instance;
    }

    /**
     * Load train schedules from JSON file into memory cache
     */
    private void loadFromFile() {
        if (!Files.exists(filePath)) {
            System.out.println("Train schedules file not found. Starting with empty data.");
            saveToFile(); // Create empty file
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            Type listType = new TypeToken<List<TrainScheduleDTO>>(){}.getType();
            List<TrainScheduleDTO> schedules = gson.fromJson(reader, listType);
            
            if (schedules != null) {
                trainSchedules.clear();
                for (TrainScheduleDTO schedule : schedules) {
                    trainSchedules.put(schedule.getTrainName(), schedule);
                }
                System.out.println("Loaded " + trainSchedules.size() + " train schedules from file.");
            }
        } catch (IOException e) {
            System.err.println("Error loading train schedules: " + e.getMessage());
        }
    }

    /**
     * Save all train schedules from memory cache to JSON file
     */
    private void saveToFile() {
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            List<TrainScheduleDTO> scheduleList = new ArrayList<>(trainSchedules.values());
            gson.toJson(scheduleList, writer);
        } catch (IOException e) {
            System.err.println("Error saving train schedules: " + e.getMessage());
        }
    }

    /**
     * Get all train schedules
     */
    public List<TrainScheduleDTO> getAllSchedules() {
        return new ArrayList<>(trainSchedules.values());
    }

    /**
     * Get a specific train schedule by train name
     */
    public Optional<TrainScheduleDTO> getSchedule(String trainName) {
        return Optional.ofNullable(trainSchedules.get(trainName));
    }

    /**
     * Add a new train schedule
     */
    public boolean addSchedule(TrainScheduleDTO schedule) {
        if (trainSchedules.containsKey(schedule.getTrainName())) {
            return false; // Already exists
        }
        trainSchedules.put(schedule.getTrainName(), schedule);
        saveToFile();
        return true;
    }

    /**
     * Update an existing train schedule
     */
    public boolean updateSchedule(String trainName, TrainScheduleDTO schedule) {
        if (!trainSchedules.containsKey(trainName)) {
            return false; // Not found
        }
        
        // Remove old entry if name changed
        if (!trainName.equals(schedule.getTrainName())) {
            trainSchedules.remove(trainName);
        }
        
        trainSchedules.put(schedule.getTrainName(), schedule);
        saveToFile();
        return true;
    }

    /**
     * Delete a train schedule
     */
    public boolean deleteSchedule(String trainName) {
        boolean removed = trainSchedules.remove(trainName) != null;
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    /**
     * Search train schedules by start and destination
     * Supports flexible matching for common city name variations
     */
    public List<TrainScheduleDTO> searchRoutes(String start, String destination) {
        return trainSchedules.values().stream()
                .filter(schedule -> matchesLocation(schedule.getStart(), start) &&
                                  matchesLocation(schedule.getDestination(), destination))
                .toList();
    }
    
    /**
     * Flexible location matching to handle spelling variations
     * e.g., Chittagong/Chattogram, Cox's Bazar/Coxs Bazar
     */
    private boolean matchesLocation(String scheduleLocation, String searchLocation) {
        if (scheduleLocation.equalsIgnoreCase(searchLocation)) {
            return true;
        }
        
        // Common variations
        String normalized1 = normalizeLocation(scheduleLocation);
        String normalized2 = normalizeLocation(searchLocation);
        
        return normalized1.equals(normalized2);
    }
    
    private String normalizeLocation(String location) {
        return location.toLowerCase()
                .replace("chattogram", "chittagong")
                .replace("chittagong", "chittagong")
                .replace("'", "")
                .replace(" ", "")
                .trim();
    }

    /**
     * Force reload from file (useful for testing or external changes)
     */
    public void reload() {
        loadFromFile();
    }
}
