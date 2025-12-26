package com.travelmanager.api;

import com.travelmanager.model.Schedule;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual bus schedule provider - reads from manually maintained JSON data
 * Schedules are managed through the Schedule Data Manager
 */
public class ManualBusApiProvider implements ScheduleApiProvider {
    
    private ScheduleDataManager dataManager;
    
    public ManualBusApiProvider() {
        this.dataManager = ScheduleDataManager.getInstance();
    }
    
    @Override
    public List<Schedule> fetchSchedules(String origin, String destination, LocalDate date) throws Exception {
        // Get all bus schedules for this route
        List<Schedule> schedules = dataManager.getBusSchedules(origin, destination)
            .stream()
            .map(bus -> (Schedule) bus)
            .collect(Collectors.toList());
        
        // Filter by date if schedules have specific dates
        // Currently returns all schedules for the route regardless of date
        // You can enhance this to filter by date if needed
        
        return schedules;
    }
    
    @Override
    public String getProviderName() {
        return "Manual Bus Schedules";
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Always available as it uses local data
    }
}
