package com.travelmanager.api;

import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mock train schedule API provider for development/testing
 */
public class MockTrainApiProvider implements ScheduleApiProvider {
    
    private Random random = new Random();
    
    private static final String[] TRAIN_NAMES = {
        "Subarna Express", "Turna Nishitha", "Silk City Express", 
        "Sundarban Express", "Mohanagar Godhuli", "Upaban Express",
        "Padma Express", "Chattala Express", "Ekota Express"
    };
    
    private static final String[] TRAIN_CLASSES = {
        "AC", "First Class", "Snigdha", "S_Chair", "Shovan"
    };
    
    @Override
    public List<Schedule> fetchSchedules(String origin, String destination, LocalDate date) {
        List<Schedule> schedules = new ArrayList<>();
        
        // Generate 4-8 random train schedules
        int scheduleCount = 4 + random.nextInt(5);
        
        for (int i = 0; i < scheduleCount; i++) {
            // Random departure time between 5 AM and 11 PM
            int departureHour = 5 + random.nextInt(18);
            int departureMinute = random.nextInt(60);
            LocalDateTime departureTime = LocalDateTime.of(date, LocalTime.of(departureHour, departureMinute));
            
            // Random journey duration between 5-14 hours
            int journeyHours = 5 + random.nextInt(10);
            int journeyMinutes = random.nextInt(60);
            LocalDateTime arrivalTime = departureTime.plusHours(journeyHours).plusMinutes(journeyMinutes);
            
            // Random fare between 300-1200 BDT
            double fare = 300 + random.nextDouble() * 900;
            
            // Random seat availability
            int availableSeats = 20 + random.nextInt(100);
            
            // Random train name and class
            String trainName = TRAIN_NAMES[random.nextInt(TRAIN_NAMES.length)];
            String trainClass = TRAIN_CLASSES[random.nextInt(TRAIN_CLASSES.length)];
            
            // Generate train number (3 digits)
            String trainNumber = String.format("%d", 700 + random.nextInt(300));
            
            String scheduleId = "TRAIN" + String.format("%03d", i + 1);
            
            TrainSchedule schedule = new TrainSchedule(
                scheduleId,
                origin,
                destination,
                departureTime,
                arrivalTime,
                fare,
                availableSeats,
                trainName,
                trainNumber,
                trainClass
            );
            
            schedules.add(schedule);
        }
        
        return schedules;
    }
    
    @Override
    public String getProviderName() {
        return "Mock Train API";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
}
