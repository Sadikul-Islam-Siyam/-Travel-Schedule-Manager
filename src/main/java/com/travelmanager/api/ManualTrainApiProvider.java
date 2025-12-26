package com.travelmanager.api;

import com.travelmanager.model.Schedule;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual train schedule provider - reads from manually maintained JSON data
 * Schedules are managed through the Schedule Data Manager
 */
public class ManualTrainApiProvider implements ScheduleApiProvider {
    
    private ScheduleDataManager dataManager;
    
    public ManualTrainApiProvider() {
        this.dataManager = ScheduleDataManager.getInstance();
    }
    
    @Override
    public List<Schedule> fetchSchedules(String origin, String destination, LocalDate date) throws Exception {
        // Get all train schedules for this route
        List<Schedule> schedules = dataManager.getTrainSchedules(origin, destination)
            .stream()
            .map(train -> (Schedule) train)
            .collect(Collectors.toList());
        
        // Filter by date if schedules have specific dates
        // Currently returns all schedules for the route regardless of date
        // You can enhance this to filter by date if needed
        
        return schedules;
    }
    
    @Override
    public String getProviderName() {
        return "Manual Train Schedules";
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Always available as it uses local data
    }
}
