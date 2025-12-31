package com.travelmanager.service.rest;

import com.travelmanager.model.rest.*;
import com.travelmanager.storage.*;

import java.util.*;

/**
 * Service layer for schedule operations
 * Provides unified interface abstracting bus and train storage
 */
public class ScheduleService {
    private static ScheduleService instance;
    
    private final BusScheduleStorage busStorage;
    private final TrainScheduleStorage trainStorage;

    private ScheduleService() {
        this.busStorage = BusScheduleStorage.getInstance();
        this.trainStorage = TrainScheduleStorage.getInstance();
    }

    public static synchronized ScheduleService getInstance() {
        if (instance == null) {
            instance = new ScheduleService();
        }
        return instance;
    }

    // ============= UNIFIED OPERATIONS =============

    /**
     * Get all schedules (bus + train) in unified format
     */
    public List<UnifiedScheduleDTO> getAllSchedules() {
        List<UnifiedScheduleDTO> unified = new ArrayList<>();
        
        // Add all bus schedules
        busStorage.getAllSchedules().stream()
                .map(UnifiedScheduleDTO::fromBus)
                .forEach(unified::add);
        
        // Add all train schedules
        trainStorage.getAllSchedules().stream()
                .map(UnifiedScheduleDTO::fromTrain)
                .forEach(unified::add);
        
        return unified;
    }

    /**
     * Search routes across both bus and train schedules
     * Returns unified results
     */
    public List<UnifiedScheduleDTO> searchRoutes(String start, String destination) {
        List<UnifiedScheduleDTO> results = new ArrayList<>();
        
        // Search bus schedules
        busStorage.searchRoutes(start, destination).stream()
                .map(UnifiedScheduleDTO::fromBus)
                .forEach(results::add);
        
        // Search train schedules
        trainStorage.searchRoutes(start, destination).stream()
                .map(UnifiedScheduleDTO::fromTrain)
                .forEach(results::add);
        
        return results;
    }

    // ============= BUS OPERATIONS =============

    /**
     * Get all bus schedules
     */
    public List<BusScheduleDTO> getAllBusSchedules() {
        return busStorage.getAllSchedules();
    }

    /**
     * Get a specific bus schedule
     */
    public Optional<BusScheduleDTO> getBusSchedule(String busName) {
        return busStorage.getSchedule(busName);
    }

    /**
     * Add a new bus schedule
     */
    public boolean addBusSchedule(BusScheduleDTO schedule) {
        return busStorage.addSchedule(schedule);
    }

    /**
     * Update an existing bus schedule
     */
    public boolean updateBusSchedule(String busName, BusScheduleDTO schedule) {
        return busStorage.updateSchedule(busName, schedule);
    }

    /**
     * Delete a bus schedule
     */
    public boolean deleteBusSchedule(String busName) {
        return busStorage.deleteSchedule(busName);
    }

    // ============= TRAIN OPERATIONS =============

    /**
     * Get all train schedules
     */
    public List<TrainScheduleDTO> getAllTrainSchedules() {
        return trainStorage.getAllSchedules();
    }

    /**
     * Get a specific train schedule
     */
    public Optional<TrainScheduleDTO> getTrainSchedule(String trainName) {
        return trainStorage.getSchedule(trainName);
    }

    /**
     * Add a new train schedule
     */
    public boolean addTrainSchedule(TrainScheduleDTO schedule) {
        return trainStorage.addSchedule(schedule);
    }

    /**
     * Update an existing train schedule
     */
    public boolean updateTrainSchedule(String trainName, TrainScheduleDTO schedule) {
        return trainStorage.updateSchedule(trainName, schedule);
    }

    /**
     * Delete a train schedule
     */
    public boolean deleteTrainSchedule(String trainName) {
        return trainStorage.deleteSchedule(trainName);
    }

    /**
     * Reload all data from files
     */
    public void reloadAll() {
        busStorage.reload();
        trainStorage.reload();
    }
}
