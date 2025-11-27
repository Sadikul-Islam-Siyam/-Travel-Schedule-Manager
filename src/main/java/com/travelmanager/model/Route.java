package com.travelmanager.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete travel route with multiple schedules
 */
public class Route {
    private List<Schedule> schedules;
    private double totalFare;
    private Duration totalDuration;

    public Route() {
        this.schedules = new ArrayList<>();
        this.totalFare = 0.0;
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        calculateTotals();
    }

    private void calculateTotals() {
        totalFare = schedules.stream().mapToDouble(Schedule::getFare).sum();
        
        if (schedules.size() > 0) {
            Schedule first = schedules.get(0);
            Schedule last = schedules.get(schedules.size() - 1);
            totalDuration = Duration.between(first.getDepartureTime(), last.getArrivalTime());
        }
    }

    public List<Schedule> getSchedules() { return schedules; }
    public double getTotalFare() { return totalFare; }
    public Duration getTotalDuration() { return totalDuration; }
}
