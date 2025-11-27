package com.travelmanager.service;

import com.travelmanager.model.Schedule;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing schedules
 */
public class ScheduleService {
    private List<Schedule> allSchedules;

    public ScheduleService() {
        this.allSchedules = new ArrayList<>();
    }

    public void addSchedule(Schedule schedule) {
        allSchedules.add(schedule);
    }

    public List<Schedule> getAllSchedules() {
        return new ArrayList<>(allSchedules);
    }

    public List<Schedule> searchSchedules(String origin, String destination) {
        List<Schedule> results = new ArrayList<>();
        for (Schedule schedule : allSchedules) {
            if (schedule.getOrigin().equalsIgnoreCase(origin) && 
                schedule.getDestination().equalsIgnoreCase(destination)) {
                results.add(schedule);
            }
        }
        return results;
    }
}
