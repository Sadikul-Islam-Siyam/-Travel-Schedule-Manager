package com.travelmanager.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.travelmanager.model.BusSchedule;
// Reserved for polymorphic schedule handling
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.service.rest.RestScheduleService;

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
 * Manages schedule data - now uses REST API instead of local JSON
 * Handles loading, saving, adding, editing, and deleting schedules via REST API
 */
@SuppressWarnings("unused") // Reserved import for polymorphic schedule handling
public class ScheduleDataManager {
    
    private static final String DATA_FILE = "schedules-data.json";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static ScheduleDataManager instance;
    private Gson gson;
    private RestScheduleService restService;
    
    private ScheduleDataManager() {
        initializeGson();
        restService = new RestScheduleService();
        System.out.println("ScheduleDataManager initialized with REST API");
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
     * Get all bus schedules - now from REST API
     */
    public List<BusSchedule> getAllBusSchedules() {
        try {
            System.out.println("ScheduleDataManager: Getting all bus schedules from REST API...");
            List<BusSchedule> schedules = restService.getAllBusSchedules();
            System.out.println("ScheduleDataManager: Retrieved " + schedules.size() + " bus schedules");
            return schedules;
        } catch (Exception e) {
            System.err.println("Error getting bus schedules: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all train schedules - now from REST API
     */
    public List<TrainSchedule> getAllTrainSchedules() {
        try {
            System.out.println("ScheduleDataManager: Getting all train schedules from REST API...");
            List<TrainSchedule> schedules = restService.getAllTrainSchedules();
            System.out.println("ScheduleDataManager: Retrieved " + schedules.size() + " train schedules");
            return schedules;
        } catch (Exception e) {
            System.err.println("Error getting train schedules: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get bus schedules for a specific route - now from REST API
     */
    public List<BusSchedule> getBusSchedules(String origin, String destination) {
        return getAllBusSchedules().stream()
            .filter(s -> s.getOrigin().equalsIgnoreCase(origin) 
                && s.getDestination().equalsIgnoreCase(destination))
            .collect(Collectors.toList());
    }
    
    /**
     * Get train schedules for a specific route - now from REST API
     */
    public List<TrainSchedule> getTrainSchedules(String origin, String destination) {
        return getAllTrainSchedules().stream()
            .filter(s -> s.getOrigin().equalsIgnoreCase(origin) 
                && s.getDestination().equalsIgnoreCase(destination))
            .collect(Collectors.toList());
    }
    
    /**
     * Save data - kept for compatibility but now no-op (REST API handles persistence)
     */
    public synchronized void saveData() {
        // No-op: REST API handles persistence automatically
        System.out.println("Save operation called - REST API handles persistence");
    }
    
    /**
     * Add bus schedule - stubbed (use REST API directly)
     */
    public void addBusSchedule(BusSchedule schedule) {
        System.out.println("addBusSchedule called - please use REST API for adding schedules");
        // TODO: Implement via REST API POST /api/schedules/bus
    }
    
    /**
     * Add train schedule - stubbed (use REST API directly)
     */
    public void addTrainSchedule(TrainSchedule schedule) {
        System.out.println("addTrainSchedule called - please use REST API for adding schedules");
    }
    
    /**
     * Update an existing bus schedule - stubbed
     */
    public boolean updateBusSchedule(String scheduleId, BusSchedule updatedSchedule) {
        System.out.println("updateBusSchedule called - please use REST API for updating schedules");
        return false;
    }
    
    /**
     * Update an existing train schedule - stubbed
     */
    public boolean updateTrainSchedule(String scheduleId, TrainSchedule updatedSchedule) {
        System.out.println("updateTrainSchedule called - please use REST API for updating schedules");
        return false;
    }
    
    /**
     * Delete a bus schedule - stubbed
     */
    public boolean deleteBusSchedule(String scheduleId) {
        System.out.println("deleteBusSchedule called - please use REST API for deleting schedules");
        return false;
    }
    
    /**
     * Delete a train schedule - stubbed
     */
    public boolean deleteTrainSchedule(String scheduleId) {
        System.out.println("deleteTrainSchedule called - please use REST API for deleting schedules");
        return false;
    }
    
    /**
     * Get a bus schedule by ID
     */
    public BusSchedule getBusScheduleById(String scheduleId) {
        return getAllBusSchedules().stream()
            .filter(s -> s.getId().equals(scheduleId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get a train schedule by ID
     */
    public TrainSchedule getTrainScheduleById(String scheduleId) {
        return getAllTrainSchedules().stream()
            .filter(s -> s.getId().equals(scheduleId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Reload data from file (kept for compatibility)
     */
    public void reloadData() {
        System.out.println("reloadData called - REST API data is always current");
    }
}
