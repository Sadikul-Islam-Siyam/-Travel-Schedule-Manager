package com.travelmanager.api;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.TrainSchedule;
import java.time.LocalDateTime;

/**
 * API Service for managing manual schedules
 * Provides CRUD operations for bus and train schedules
 */
public class ManualScheduleService {
    
    private static ManualScheduleService instance;
    private ScheduleDataManager dataManager;
    
    private ManualScheduleService() {
        this.dataManager = ScheduleDataManager.getInstance();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ManualScheduleService getInstance() {
        if (instance == null) {
            instance = new ManualScheduleService();
        }
        return instance;
    }
    
    // ==================== BUS SCHEDULE OPERATIONS ====================
    
    /**
     * Create a new bus schedule
     */
    public void createBusSchedule(String scheduleId, String origin, String destination,
                                  LocalDateTime departureTime, LocalDateTime arrivalTime,
                                  double fare, int availableSeats, String company, String busType) {
        BusSchedule schedule = new BusSchedule(
            scheduleId, origin, destination, departureTime, 
            arrivalTime, fare, availableSeats, company, busType
        );
        dataManager.addBusSchedule(schedule);
    }
    
    /**
     * Update an existing bus schedule
     */
    public boolean updateBusSchedule(String scheduleId, String origin, String destination,
                                     LocalDateTime departureTime, LocalDateTime arrivalTime,
                                     double fare, int availableSeats, String company, String busType) {
        BusSchedule schedule = new BusSchedule(
            scheduleId, origin, destination, departureTime,
            arrivalTime, fare, availableSeats, company, busType
        );
        return dataManager.updateBusSchedule(scheduleId, schedule);
    }
    
    /**
     * Delete a bus schedule
     */
    public boolean deleteBusSchedule(String scheduleId) {
        return dataManager.deleteBusSchedule(scheduleId);
    }
    
    /**
     * Get a bus schedule by ID
     */
    public BusSchedule getBusSchedule(String scheduleId) {
        return dataManager.getBusScheduleById(scheduleId);
    }
    
    // ==================== TRAIN SCHEDULE OPERATIONS ====================
    
    /**
     * Create a new train schedule
     */
    public void createTrainSchedule(String scheduleId, String origin, String destination,
                                    LocalDateTime departureTime, LocalDateTime arrivalTime,
                                    double fare, int availableSeats, String trainName, String trainClass) {
        TrainSchedule schedule = new TrainSchedule(
            scheduleId, origin, destination, departureTime,
            arrivalTime, fare, availableSeats, trainName, "", trainClass
        );
        dataManager.addTrainSchedule(schedule);
    }
    
    /**
     * Update an existing train schedule
     */
    public boolean updateTrainSchedule(String scheduleId, String origin, String destination,
                                       LocalDateTime departureTime, LocalDateTime arrivalTime,
                                       double fare, int availableSeats, String trainName, String trainClass) {
        TrainSchedule schedule = new TrainSchedule(
            scheduleId, origin, destination, departureTime,
            arrivalTime, fare, availableSeats, trainName, "", trainClass
        );
        return dataManager.updateTrainSchedule(scheduleId, schedule);
    }
    
    /**
     * Delete a train schedule
     */
    public boolean deleteTrainSchedule(String scheduleId) {
        return dataManager.deleteTrainSchedule(scheduleId);
    }
    
    /**
     * Get a train schedule by ID
     */
    public TrainSchedule getTrainSchedule(String scheduleId) {
        return dataManager.getTrainScheduleById(scheduleId);
    }
    
    // ==================== UTILITY OPERATIONS ====================
    
    /**
     * Reload data from file (useful after manual edits to JSON)
     */
    public void reloadData() {
        dataManager.reloadData();
    }
    
    /**
     * Get total number of bus schedules
     */
    public int getTotalBusSchedules() {
        return dataManager.getAllBusSchedules().size();
    }
    
    /**
     * Get total number of train schedules
     */
    public int getTotalTrainSchedules() {
        return dataManager.getAllTrainSchedules().size();
    }
}
