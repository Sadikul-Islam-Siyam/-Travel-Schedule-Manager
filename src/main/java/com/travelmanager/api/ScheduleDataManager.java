package com.travelmanager.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages manual schedule data stored in JSON format
 * Handles loading, saving, adding, editing, and deleting schedules
 */
public class ScheduleDataManager {
    
    private static final String DATA_FILE = "schedules-data.json";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static ScheduleDataManager instance;
    private Gson gson;
    private ScheduleData data;
    
    private ScheduleDataManager() {
        initializeGson();
        loadData();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ScheduleDataManager getInstance() {
        if (instance == null) {
            instance = new ScheduleDataManager();
        }
        return instance;
    }
    
    /**
     * Initialize Gson with custom serializers for LocalDateTime
     */
    private void initializeGson() {
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, 
                (JsonSerializer<LocalDateTime>) (src, type, context) -> 
                    context.serialize(src.format(DATE_FORMAT)))
            .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                    LocalDateTime.parse(json.getAsString(), DATE_FORMAT))
            .setPrettyPrinting()
            .create();
    }
    
    /**
     * Load schedule data from JSON file
     */
    private void loadData() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                // Create new data file with empty lists
                data = new ScheduleData();
                saveData();
                System.out.println("Created new schedule data file: " + DATA_FILE);
            } else {
                String json = new String(Files.readAllBytes(Paths.get(DATA_FILE)), StandardCharsets.UTF_8);
                data = gson.fromJson(json, ScheduleData.class);
                if (data == null) {
                    data = new ScheduleData();
                }
                System.out.println("Loaded " + data.busSchedules.size() + " bus schedules and " 
                    + data.trainSchedules.size() + " train schedules");
            }
        } catch (Exception e) {
            System.err.println("Error loading schedule data: " + e.getMessage());
            data = new ScheduleData();
        }
    }
    
    /**
     * Save schedule data to JSON file
     */
    public synchronized void saveData() {
        try {
            String json = gson.toJson(data);
            Files.write(Paths.get(DATA_FILE), json.getBytes(StandardCharsets.UTF_8));
            System.out.println("Schedule data saved successfully");
        } catch (Exception e) {
            System.err.println("Error saving schedule data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get all bus schedules
     */
    public List<BusSchedule> getAllBusSchedules() {
        return new ArrayList<>(data.busSchedules);
    }
    
    /**
     * Get all train schedules
     */
    public List<TrainSchedule> getAllTrainSchedules() {
        return new ArrayList<>(data.trainSchedules);
    }
    
    /**
     * Get bus schedules for a specific route
     */
    public List<BusSchedule> getBusSchedules(String origin, String destination) {
        return data.busSchedules.stream()
            .filter(s -> s.getOrigin().equalsIgnoreCase(origin) 
                && s.getDestination().equalsIgnoreCase(destination))
            .collect(Collectors.toList());
    }
    
    /**
     * Get train schedules for a specific route
     */
    public List<TrainSchedule> getTrainSchedules(String origin, String destination) {
        return data.trainSchedules.stream()
            .filter(s -> s.getOrigin().equalsIgnoreCase(origin) 
                && s.getDestination().equalsIgnoreCase(destination))
            .collect(Collectors.toList());
    }
    
    /**
     * Add a new bus schedule
     */
    public void addBusSchedule(BusSchedule schedule) {
        data.busSchedules.add(schedule);
        saveData();
        System.out.println("Added bus schedule: " + schedule.getId());
    }
    
    /**
     * Add a new train schedule
     */
    public void addTrainSchedule(TrainSchedule schedule) {
        data.trainSchedules.add(schedule);
        saveData();
        System.out.println("Added train schedule: " + schedule.getId());
    }
    
    /**
     * Update an existing bus schedule
     */
    public boolean updateBusSchedule(String scheduleId, BusSchedule updatedSchedule) {
        for (int i = 0; i < data.busSchedules.size(); i++) {
            if (data.busSchedules.get(i).getId().equals(scheduleId)) {
                data.busSchedules.set(i, updatedSchedule);
                saveData();
                System.out.println("Updated bus schedule: " + scheduleId);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Update an existing train schedule
     */
    public boolean updateTrainSchedule(String scheduleId, TrainSchedule updatedSchedule) {
        for (int i = 0; i < data.trainSchedules.size(); i++) {
            if (data.trainSchedules.get(i).getId().equals(scheduleId)) {
                data.trainSchedules.set(i, updatedSchedule);
                saveData();
                System.out.println("Updated train schedule: " + scheduleId);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Delete a bus schedule
     */
    public boolean deleteBusSchedule(String scheduleId) {
        boolean removed = data.busSchedules.removeIf(s -> s.getId().equals(scheduleId));
        if (removed) {
            saveData();
            System.out.println("Deleted bus schedule: " + scheduleId);
        }
        return removed;
    }
    
    /**
     * Delete a train schedule
     */
    public boolean deleteTrainSchedule(String scheduleId) {
        boolean removed = data.trainSchedules.removeIf(s -> s.getId().equals(scheduleId));
        if (removed) {
            saveData();
            System.out.println("Deleted train schedule: " + scheduleId);
        }
        return removed;
    }
    
    /**
     * Get a bus schedule by ID
     */
    public BusSchedule getBusScheduleById(String scheduleId) {
        return data.busSchedules.stream()
            .filter(s -> s.getId().equals(scheduleId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get a train schedule by ID
     */
    public TrainSchedule getTrainScheduleById(String scheduleId) {
        return data.trainSchedules.stream()
            .filter(s -> s.getId().equals(scheduleId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Reload data from file (useful for external edits)
     */
    public void reloadData() {
        loadData();
    }
    
    /**
     * Inner class to hold schedule data structure
     */
    private static class ScheduleData {
        List<BusSchedule> busSchedules;
        List<TrainSchedule> trainSchedules;
        
        ScheduleData() {
            this.busSchedules = new ArrayList<>();
            this.trainSchedules = new ArrayList<>();
        }
    }
}
