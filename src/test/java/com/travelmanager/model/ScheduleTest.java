package com.travelmanager.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Schedule models
 */
public class ScheduleTest {

    @Test
    public void testBusScheduleCreation() {
        LocalDateTime departure = LocalDateTime.of(2025, 11, 27, 10, 0);
        LocalDateTime arrival = LocalDateTime.of(2025, 11, 27, 14, 30);
        
        BusSchedule bus = new BusSchedule(
            "BUS001", "Dhaka", "Chittagong",
            departure, arrival, 500.0, 40,
            "Green Line", "AC"
        );
        
        assertEquals("BUS001", bus.getId());
        assertEquals("Bus", bus.getType());
        assertEquals("Green Line", bus.getBusCompany());
    }

    @Test
    public void testTrainScheduleCreation() {
        LocalDateTime departure = LocalDateTime.of(2025, 11, 27, 8, 0);
        LocalDateTime arrival = LocalDateTime.of(2025, 11, 27, 13, 0);
        
        TrainSchedule train = new TrainSchedule(
            "TRAIN001", "Dhaka", "Sylhet",
            departure, arrival, 600.0, 200,
            "Parabat Express", "701", "First Class"
        );
        
        assertEquals("TRAIN001", train.getId());
        assertEquals("Train", train.getType());
        assertEquals("Parabat Express", train.getTrainName());
    }
}
