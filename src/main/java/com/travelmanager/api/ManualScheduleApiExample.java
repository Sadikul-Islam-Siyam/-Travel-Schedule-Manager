package com.travelmanager.api;

import java.time.LocalDateTime;

/**
 * Example usage and testing of Manual Schedule API
 * This demonstrates how to add, edit, and delete schedules programmatically
 */
public class ManualScheduleApiExample {
    
    public static void main(String[] args) {
        ManualScheduleService service = ManualScheduleService.getInstance();
        
        System.out.println("=== Manual Schedule API Demo ===\n");
        
        // Display current data
        System.out.println("Current Schedules:");
        System.out.println("- Bus Schedules: " + service.getTotalBusSchedules());
        System.out.println("- Train Schedules: " + service.getTotalTrainSchedules());
        System.out.println();
        
        // ==================== ADDING NEW SCHEDULES ====================
        
        System.out.println("1. Adding a new bus schedule...");
        service.createBusSchedule(
            "BUS999",                           // Schedule ID
            "Dhaka",                            // Origin
            "Cox's Bazar",                      // Destination
            LocalDateTime.of(2025, 12, 28, 9, 0),  // Departure
            LocalDateTime.of(2025, 12, 28, 18, 0), // Arrival
            1200.0,                             // Fare
            40,                                 // Available Seats
            "Hanif Enterprise",                 // Company
            "AC Sleeper"                        // Bus Type
        );
        System.out.println("✓ Bus schedule BUS999 added!\n");
        
        System.out.println("2. Adding a new train schedule...");
        service.createTrainSchedule(
            "TRAIN999",                         // Schedule ID
            "Dhaka",                            // Origin
            "Cox's Bazar",                      // Destination
            LocalDateTime.of(2025, 12, 28, 7, 30), // Departure
            LocalDateTime.of(2025, 12, 28, 16, 0), // Arrival
            950.0,                              // Fare
            150,                                // Available Seats
            "Cox's Express",                    // Train Name
            "AC"                                // Train Class
        );
        System.out.println("✓ Train schedule TRAIN999 added!\n");
        
        // ==================== READING SCHEDULES ====================
        
        System.out.println("3. Reading a bus schedule...");
        var busSchedule = service.getBusSchedule("BUS999");
        if (busSchedule != null) {
            System.out.println("   ID: " + busSchedule.getId());
            System.out.println("   Route: " + busSchedule.getOrigin() + " → " + busSchedule.getDestination());
            System.out.println("   Company: " + busSchedule.getBusCompany());
            System.out.println("   Type: " + busSchedule.getBusType());
            System.out.println("   Fare: ৳" + busSchedule.getFare());
        }
        System.out.println();
        
        // ==================== UPDATING SCHEDULES ====================
        
        System.out.println("4. Updating bus schedule (changing fare and seats)...");
        boolean updated = service.updateBusSchedule(
            "BUS999",                           // Schedule ID to update
            "Dhaka",                            // Origin
            "Cox's Bazar",                      // Destination
            LocalDateTime.of(2025, 12, 28, 9, 0),  // Departure
            LocalDateTime.of(2025, 12, 28, 18, 0), // Arrival
            1150.0,                             // NEW Fare (reduced)
            45,                                 // NEW Available Seats (increased)
            "Hanif Enterprise",                 // Company
            "AC Sleeper"                        // Bus Type
        );
        System.out.println(updated ? "✓ Updated successfully!" : "✗ Update failed");
        System.out.println();
        
        // ==================== DELETING SCHEDULES ====================
        
        System.out.println("5. Deleting test schedules...");
        boolean busDeleted = service.deleteBusSchedule("BUS999");
        boolean trainDeleted = service.deleteTrainSchedule("TRAIN999");
        System.out.println(busDeleted ? "✓ Bus schedule deleted" : "✗ Bus delete failed");
        System.out.println(trainDeleted ? "✓ Train schedule deleted" : "✗ Train delete failed");
        System.out.println();
        
        // ==================== FINAL COUNT ====================
        
        System.out.println("Final Count:");
        System.out.println("- Bus Schedules: " + service.getTotalBusSchedules());
        System.out.println("- Train Schedules: " + service.getTotalTrainSchedules());
        
        System.out.println("\n=== Demo Complete ===");
        System.out.println("Check 'schedules-data.json' to see the saved data!");
    }
}
