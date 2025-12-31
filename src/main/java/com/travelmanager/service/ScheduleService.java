package com.travelmanager.service;

import com.travelmanager.model.Schedule;
import com.travelmanager.service.rest.RestScheduleService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing schedules
 * Now uses REST API instead of direct file access
 */
public class ScheduleService {
    private RestScheduleService restService;
    private List<Schedule> cachedSchedules;

    public ScheduleService() {
        this.restService = new RestScheduleService();
        this.cachedSchedules = new ArrayList<>();
    }

    /**
     * Search schedules from REST API
     * @param origin Starting location
     * @param destination Ending location
     * @param date Travel date
     * @return List of schedules matching criteria
     */
    public List<Schedule> searchSchedules(String origin, String destination, LocalDate date) {
        try {
            List<Schedule> results = restService.searchSchedules(origin, destination, date);
            // Cache results for offline use
            cachedSchedules = new ArrayList<>(results);
            return results;
        } catch (Exception e) {
            System.err.println("Error searching schedules: " + e.getMessage());
            e.printStackTrace();
            // Fall back to cached schedules if available
            return searchCachedSchedules(origin, destination);
        }
    }
    
    /**
     * Search only bus schedules
     * @param origin Starting location
     * @param destination Ending location
     * @param date Travel date
     * @return List of bus schedules
     */
    public List<Schedule> searchBusSchedules(String origin, String destination, LocalDate date) {
        try {
            return restService.searchBusSchedules(origin, destination, date);
        } catch (Exception e) {
            System.err.println("Error searching bus schedules: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Search only train schedules
     * @param origin Starting location
     * @param destination Ending location
     * @param date Travel date
     * @return List of train schedules
     */
    public List<Schedule> searchTrainSchedules(String origin, String destination, LocalDate date) {
        try {
            return restService.searchTrainSchedules(origin, destination, date);
        } catch (Exception e) {
            System.err.println("Error searching train schedules: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Legacy method for backward compatibility - searches without date
     * Uses current date as default
     */
    public List<Schedule> searchSchedules(String origin, String destination) {
        return searchSchedules(origin, destination, LocalDate.now());
    }
    
    /**
     * Search cached schedules (offline mode)
     */
    private List<Schedule> searchCachedSchedules(String origin, String destination) {
        List<Schedule> results = new ArrayList<>();
        for (Schedule schedule : cachedSchedules) {
            if (schedule.getOrigin().equalsIgnoreCase(origin) && 
                schedule.getDestination().equalsIgnoreCase(destination)) {
                results.add(schedule);
            }
        }
        return results;
    }
    
    /**
     * Get all cached schedules
     */
    public List<Schedule> getAllSchedules() {
        return new ArrayList<>(cachedSchedules);
    }
    
    /**
     * Manually add a schedule to cache
     */
    public void addSchedule(Schedule schedule) {
        cachedSchedules.add(schedule);
    }
    
    /**
     * Clear all cached schedules
     */
    public void clearCache() {
        cachedSchedules.clear();
    }
}
