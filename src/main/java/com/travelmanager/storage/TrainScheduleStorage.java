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
     * Includes intermediate stops logic
     */
    public List<TrainScheduleDTO> searchRoutes(String start, String destination) {
        List<TrainScheduleDTO> results = new ArrayList<>();
        
        for (TrainScheduleDTO schedule : trainSchedules.values()) {
            // Check if stops array exists and is not empty
            if (schedule.getStops() != null && !schedule.getStops().isEmpty()) {
                // Find if both start and destination exist in stops
                int startIndex = findStopIndex(schedule.getStops(), start);
                int destIndex = findStopIndex(schedule.getStops(), destination);
                
                // Valid route if both stops exist and start comes before destination
                if (startIndex >= 0 && destIndex > startIndex) {
                    // Create a customized schedule for this segment
                    TrainScheduleDTO segmentSchedule = createSegmentSchedule(schedule, startIndex, destIndex);
                    results.add(segmentSchedule);
                }
            } else {
                // Fallback to old logic if no stops defined
                if (matchesLocation(schedule.getStart(), start) && 
                    matchesLocation(schedule.getDestination(), destination)) {
                    results.add(schedule);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Find the index of a stop in the stops list
     */
    private int findStopIndex(List<TrainScheduleDTO.TrainStop> stops, String location) {
        for (int i = 0; i < stops.size(); i++) {
            if (matchesLocation(stops.get(i).getStation(), location)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Create a customized schedule for a specific segment of the route
     */
    private TrainScheduleDTO createSegmentSchedule(TrainScheduleDTO originalSchedule, int startIndex, int destIndex) {
        TrainScheduleDTO segment = new TrainScheduleDTO();
        segment.setTrainName(originalSchedule.getTrainName());
        segment.setOffDay(originalSchedule.getOffDay());
        
        List<TrainScheduleDTO.TrainStop> stops = originalSchedule.getStops();
        TrainScheduleDTO.TrainStop startStop = stops.get(startIndex);
        TrainScheduleDTO.TrainStop destStop = stops.get(destIndex);
        
        segment.setStart(startStop.getStation());
        segment.setDestination(destStop.getStation());
        segment.setStartTime(startStop.getDepartureTime());
        segment.setArrivalTime(destStop.getArrivalTime());
        
        // Calculate fare for this segment
        double segmentFare = destStop.getCumulativeFare() - startStop.getCumulativeFare();
        segment.setFare(segmentFare);
        
        // Calculate duration
        String duration = calculateDuration(startStop.getDepartureTime(), destStop.getArrivalTime());
        segment.setDuration(duration);
        
        // Include only relevant stops for this segment
        List<TrainScheduleDTO.TrainStop> segmentStops = new ArrayList<>();
        for (int i = startIndex; i <= destIndex; i++) {
            segmentStops.add(stops.get(i));
        }
        segment.setStops(segmentStops);
        
        return segment;
    }
    
    /**
     * Calculate duration between two times (simple calculation, doesn't handle day overflow perfectly)
     */
    private String calculateDuration(String startTime, String endTime) {
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            
            int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
            
            int durationMinutes = endMinutes - startMinutes;
            if (durationMinutes < 0) {
                durationMinutes += 24 * 60; // Add 24 hours if crossing midnight
            }
            
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            
            return String.format("%d:%02dh", hours, minutes);
        } catch (Exception e) {
            return "N/A";
        }
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
