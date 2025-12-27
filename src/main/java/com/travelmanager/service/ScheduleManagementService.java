package com.travelmanager.service;

import com.travelmanager.api.ScheduleDataManager;
import com.travelmanager.exception.ValidationException;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.util.CacheManager;
import com.travelmanager.util.Constants;

import java.util.List;

/**
 * Service for managing schedules with validation
 * Used by DEV mode to add/edit/delete schedules
 */
public class ScheduleManagementService {
    
    private ScheduleDataManager dataManager;
    private ScheduleValidationService validator;
    private CacheManager cacheManager;
    
    public ScheduleManagementService() {
        this.dataManager = ScheduleDataManager.getInstance();
        this.validator = new ScheduleValidationService();
        this.cacheManager = CacheManager.getInstance();
    }
    
    /**
     * Add new schedule with validation
     */
    public void addSchedule(Schedule schedule) throws ValidationException {
        // Validate
        validator.validateSchedule(schedule);
        
        // Check for duplicate ID
        if (scheduleExists(schedule.getId())) {
            throw new ValidationException("Schedule ID already exists: " + schedule.getId());
        }
        
        // Save based on type
        try {
            if (schedule instanceof BusSchedule) {
                dataManager.addBusSchedule((BusSchedule) schedule);
            } else if (schedule instanceof TrainSchedule) {
                dataManager.addTrainSchedule((TrainSchedule) schedule);
            }
            
            // Invalidate cache
            invalidateCache();
            
        } catch (Exception e) {
            throw new ValidationException("Failed to add schedule: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update existing schedule with validation
     */
    public void updateSchedule(Schedule schedule) throws ValidationException {
        // Validate
        validator.validateSchedule(schedule);
        
        // Check if exists
        if (!scheduleExists(schedule.getId())) {
            throw new ValidationException("Schedule not found: " + schedule.getId());
        }
        
        // Update based on type
        try {
            if (schedule instanceof BusSchedule) {
                dataManager.updateBusSchedule(schedule.getId(), (BusSchedule) schedule);
            } else if (schedule instanceof TrainSchedule) {
                dataManager.updateTrainSchedule(schedule.getId(), (TrainSchedule) schedule);
            }
            
            // Invalidate cache
            invalidateCache();
            
        } catch (Exception e) {
            throw new ValidationException("Failed to update schedule: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete schedule
     */
    public void deleteSchedule(String scheduleId, String type) throws ValidationException {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            throw new ValidationException("Schedule ID is required");
        }
        
        try {
            if ("BUS".equalsIgnoreCase(type)) {
                dataManager.deleteBusSchedule(scheduleId);
            } else if ("TRAIN".equalsIgnoreCase(type)) {
                dataManager.deleteTrainSchedule(scheduleId);
            } else {
                throw new ValidationException("Invalid schedule type: " + type);
            }
            
            // Invalidate cache
            invalidateCache();
            
        } catch (Exception e) {
            throw new ValidationException("Failed to delete schedule: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if schedule ID exists
     */
    private boolean scheduleExists(String scheduleId) {
        List<BusSchedule> busSchedules = dataManager.getAllBusSchedules();
        for (BusSchedule bus : busSchedules) {
            if (bus.getId().equals(scheduleId)) {
                return true;
            }
        }
        
        List<TrainSchedule> trainSchedules = dataManager.getAllTrainSchedules();
        for (TrainSchedule train : trainSchedules) {
            if (train.getId().equals(scheduleId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Invalidate all schedule-related cache
     */
    private void invalidateCache() {
        cacheManager.invalidatePrefix(Constants.CACHE_SCHEDULE_PREFIX);
        cacheManager.invalidatePrefix(Constants.CACHE_ROUTE_PREFIX);
    }
    
    /**
     * Get all bus schedules
     */
    public List<BusSchedule> getAllBusSchedules() {
        return dataManager.getAllBusSchedules();
    }
    
    /**
     * Get all train schedules
     */
    public List<TrainSchedule> getAllTrainSchedules() {
        return dataManager.getAllTrainSchedules();
    }
}
