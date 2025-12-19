package com.travelmanager.api;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mock bus schedule API provider for development/testing
 */
public class MockBusApiProvider implements ScheduleApiProvider {
    
    private Random random = new Random();
    
    private static final String[] BUS_COMPANIES = {
        "Green Line", "Shyamoli Paribahan", "Hanif Enterprise", 
        "Ena Transport", "Shohag Paribahan", "TR Travels"
    };
    
    private static final String[] BUS_TYPES = {
        "AC", "Non-AC", "Sleeper", "Deluxe"
    };
    
    @Override
    public List<Schedule> fetchSchedules(String origin, String destination, LocalDate date) {
        List<Schedule> schedules = new ArrayList<>();
        
        // Generate 5-10 random bus schedules
        int scheduleCount = 5 + random.nextInt(6);
        
        for (int i = 0; i < scheduleCount; i++) {
            // Random departure time between 6 AM and 11 PM
            int departureHour = 6 + random.nextInt(17);
            int departureMinute = random.nextInt(60);
            LocalDateTime departureTime = LocalDateTime.of(date, LocalTime.of(departureHour, departureMinute));
            
            // Random journey duration between 4-12 hours
            int journeyHours = 4 + random.nextInt(9);
            int journeyMinutes = random.nextInt(60);
            LocalDateTime arrivalTime = departureTime.plusHours(journeyHours).plusMinutes(journeyMinutes);
            
            // Random fare between 400-1500 BDT
            double fare = 400 + random.nextDouble() * 1100;
            
            // Random seat availability
            int availableSeats = 10 + random.nextInt(30);
            
            // Random company and type
            String company = BUS_COMPANIES[random.nextInt(BUS_COMPANIES.length)];
            String busType = BUS_TYPES[random.nextInt(BUS_TYPES.length)];
            
            String scheduleId = "BUS" + String.format("%03d", i + 1);
            
            BusSchedule schedule = new BusSchedule(
                scheduleId,
                origin,
                destination,
                departureTime,
                arrivalTime,
                fare,
                availableSeats,
                company,
                busType
            );
            
            schedules.add(schedule);
        }
        
        return schedules;
    }
    
    @Override
    public String getProviderName() {
        return "Mock Bus API";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
}
