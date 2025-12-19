package com.travelmanager.api;

import com.travelmanager.model.Schedule;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface for schedule API providers
 */
public interface ScheduleApiProvider {
    
    /**
     * Fetch schedules from the API
     * @param origin Starting location
     * @param destination Ending location
     * @param date Travel date
     * @return List of available schedules
     * @throws Exception if API call fails
     */
    List<Schedule> fetchSchedules(String origin, String destination, LocalDate date) throws Exception;
    
    /**
     * Get the provider name
     * @return Provider name
     */
    String getProviderName();
    
    /**
     * Check if the provider is available/configured
     * @return true if provider can be used
     */
    boolean isAvailable();
}
