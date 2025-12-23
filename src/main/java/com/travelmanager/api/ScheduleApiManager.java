package com.travelmanager.api;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager class that coordinates all schedule API providers
 */
public class ScheduleApiManager {
    
    private static ScheduleApiManager instance;
    private List<ScheduleApiProvider> providers;
    
    private ScheduleApiManager() {
        providers = new ArrayList<>();
        initializeProviders();
    }
    
    /**
     * Get singleton instance
     * @return ScheduleApiManager instance
     */
    public static synchronized ScheduleApiManager getInstance() {
        if (instance == null) {
            instance = new ScheduleApiManager();
        }
        return instance;
    }
    
    /**
     * Initialize all available providers
     */
    private void initializeProviders() {
        // Add mock providers for development
        if (ApiConfig.useMockData()) {
            providers.add(new MockBusApiProvider());
            providers.add(new MockTrainApiProvider());
        }
        
        // TODO: Add real API providers when available
        // if (ApiConfig.isBusApiEnabled()) {
        //     providers.add(new RealBusApiProvider());
        // }
        // if (ApiConfig.isTrainApiEnabled()) {
        //     providers.add(new RealTrainApiProvider());
        // }
    }
    
    /**
     * Fetch all schedules from all providers
     * @param origin Starting location
     * @param destination Ending location
     * @param date Travel date
     * @return Combined list of schedules from all providers
     */
    public List<Schedule> fetchAllSchedules(String origin, String destination, LocalDate date) {
        List<Schedule> allSchedules = new ArrayList<>();
        
        for (ScheduleApiProvider provider : providers) {
            if (provider.isAvailable()) {
                try {
                    List<Schedule> schedules = provider.fetchSchedules(origin, destination, date);
                    allSchedules.addAll(schedules);
                    System.out.println("Fetched " + schedules.size() + " schedules from " + provider.getProviderName());
                } catch (Exception e) {
                    System.err.println("Error fetching from " + provider.getProviderName() + ": " + e.getMessage());
                }
            }
        }
        
        return allSchedules;
    }
    
    /**
     * Fetch only bus schedules
     * @param origin Starting location
     * @param destination Ending location
     * @param date Travel date
     * @return List of bus schedules
     */
    public List<Schedule> fetchBusSchedules(String origin, String destination, LocalDate date) {
        List<Schedule> allSchedules = fetchAllSchedules(origin, destination, date);
        List<Schedule> busSchedules = new ArrayList<>();
        
        for (Schedule schedule : allSchedules) {
            if (schedule instanceof BusSchedule) {
                busSchedules.add(schedule);
            }
        }
        
        return busSchedules;
    }
    
    /**
     * Fetch only train schedules
     * @param origin Starting location
     * @param destination Ending location
     * @param date Travel date
     * @return List of train schedules
     */
    public List<Schedule> fetchTrainSchedules(String origin, String destination, LocalDate date) {
        List<Schedule> allSchedules = fetchAllSchedules(origin, destination, date);
        List<Schedule> trainSchedules = new ArrayList<>();
        
        for (Schedule schedule : allSchedules) {
            if (schedule instanceof TrainSchedule) {
                trainSchedules.add(schedule);
            }
        }
        
        return trainSchedules;
    }
    
    /**
     * Search schedules between origin and destination
     * @param origin Starting location
     * @param destination Ending location
     * @return List of schedules
     */
    public List<Schedule> searchSchedules(String origin, String destination) {
        return fetchAllSchedules(origin, destination, LocalDate.now());
    }
    
    /**
     * Search schedules by type between origin and destination
     * @param origin Starting location
     * @param destination Ending location
     * @param type Transport type (BUS or TRAIN)
     * @return List of schedules
     */
    public List<Schedule> searchSchedulesByType(String origin, String destination, String type) {
        if ("BUS".equalsIgnoreCase(type)) {
            return fetchBusSchedules(origin, destination, LocalDate.now());
        } else if ("TRAIN".equalsIgnoreCase(type)) {
            return fetchTrainSchedules(origin, destination, LocalDate.now());
        }
        return new ArrayList<>();
    }
    
    /**
     * Reload all providers (useful after configuration changes)
     */
    public void reloadProviders() {
        providers.clear();
        initializeProviders();
    }
}
