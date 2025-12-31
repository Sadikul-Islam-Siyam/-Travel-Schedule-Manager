package com.travelmanager.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.travelmanager.model.rest.BusScheduleDTO;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage manager for bus schedules using JSON persistence
 * Thread-safe with in-memory caching
 */
public class BusScheduleStorage {
    private static final String DATA_DIR = "data";
    private static final String BUS_SCHEDULES_FILE = "bus_schedules.json";
    private static BusScheduleStorage instance;
    
    private final Map<String, BusScheduleDTO> busSchedules;
    private final Gson gson;
    private final Path filePath;

    private BusScheduleStorage() {
        this.busSchedules = new ConcurrentHashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.filePath = Paths.get(DATA_DIR, BUS_SCHEDULES_FILE);
        
        // Ensure data directory exists
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
        
        loadFromFile();
    }

    public static synchronized BusScheduleStorage getInstance() {
        if (instance == null) {
            instance = new BusScheduleStorage();
        }
        return instance;
    }

    /**
     * Load bus schedules from JSON file into memory cache
     */
    private void loadFromFile() {
        if (!Files.exists(filePath)) {
            System.out.println("Bus schedules file not found. Starting with empty data.");
            saveToFile(); // Create empty file
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            Type listType = new TypeToken<List<BusScheduleDTO>>(){}.getType();
            List<BusScheduleDTO> schedules = gson.fromJson(reader, listType);
            
            if (schedules != null) {
                busSchedules.clear();
                for (BusScheduleDTO schedule : schedules) {
                    busSchedules.put(schedule.getBusName(), schedule);
                }
                System.out.println("Loaded " + busSchedules.size() + " bus schedules from file.");
            }
        } catch (IOException e) {
            System.err.println("Error loading bus schedules: " + e.getMessage());
        }
    }

    /**
     * Save all bus schedules from memory cache to JSON file
     */
    private void saveToFile() {
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            List<BusScheduleDTO> scheduleList = new ArrayList<>(busSchedules.values());
            gson.toJson(scheduleList, writer);
        } catch (IOException e) {
            System.err.println("Error saving bus schedules: " + e.getMessage());
        }
    }

    /**
     * Get all bus schedules
     */
    public List<BusScheduleDTO> getAllSchedules() {
        return new ArrayList<>(busSchedules.values());
    }

    /**
     * Get a specific bus schedule by bus name
     */
    public Optional<BusScheduleDTO> getSchedule(String busName) {
        return Optional.ofNullable(busSchedules.get(busName));
    }

    /**
     * Add a new bus schedule
     */
    public boolean addSchedule(BusScheduleDTO schedule) {
        if (busSchedules.containsKey(schedule.getBusName())) {
            return false; // Already exists
        }
        busSchedules.put(schedule.getBusName(), schedule);
        saveToFile();
        return true;
    }

    /**
     * Update an existing bus schedule
     */
    public boolean updateSchedule(String busName, BusScheduleDTO schedule) {
        if (!busSchedules.containsKey(busName)) {
            return false; // Not found
        }
        
        // Remove old entry if name changed
        if (!busName.equals(schedule.getBusName())) {
            busSchedules.remove(busName);
        }
        
        busSchedules.put(schedule.getBusName(), schedule);
        saveToFile();
        return true;
    }

    /**
     * Delete a bus schedule
     */
    public boolean deleteSchedule(String busName) {
        boolean removed = busSchedules.remove(busName) != null;
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    /**
     * Search bus schedules by start and destination
     * Supports flexible matching for common city name variations
     */
    public List<BusScheduleDTO> searchRoutes(String start, String destination) {
        return busSchedules.values().stream()
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
