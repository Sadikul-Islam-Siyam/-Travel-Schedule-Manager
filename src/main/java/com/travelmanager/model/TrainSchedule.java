package com.travelmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a train schedule
 */
public class TrainSchedule extends Schedule {
    private String trainName;
    private String trainNumber;
    private String seatClass;
    private String offDay;  // e.g., "Monday", "No off day", "Sunday"

    public TrainSchedule(String id, String origin, String destination,
                        LocalDateTime departureTime, LocalDateTime arrivalTime,
                        double fare, int availableSeats,
                        String trainName, String trainNumber, String seatClass) {
        super(id, origin, destination, departureTime, arrivalTime, fare, availableSeats);
        this.trainName = trainName;
        this.trainNumber = trainNumber;
        this.seatClass = seatClass;
        this.offDay = "No off day";  // default
    }
    
    public TrainSchedule(String id, String origin, String destination,
                        LocalDateTime departureTime, LocalDateTime arrivalTime,
                        double fare, int availableSeats,
                        String trainName, String trainNumber, String seatClass, String offDay) {
        super(id, origin, destination, departureTime, arrivalTime, fare, availableSeats);
        this.trainName = trainName;
        this.trainNumber = trainNumber;
        this.seatClass = seatClass;
        this.offDay = offDay != null ? offDay : "No off day";
    }

    @Override
    public String getType() {
        return "Train";
    }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getSeatClass() { return seatClass; }
    public void setSeatClass(String seatClass) { this.seatClass = seatClass; }
    
    public String getOffDay() { return offDay; }
    public void setOffDay(String offDay) { this.offDay = offDay; }
    
    /**
     * Check if this train is available on the given date
     * @param date The travel date to check
     * @return true if train is available (not an off day)
     */
    public boolean isAvailableOnDate(java.time.LocalDate date) {
        if (offDay == null || offDay.isEmpty() || "No off day".equalsIgnoreCase(offDay)) {
            return true;  // Train runs every day
        }
        
        // Get day of week from date
        String dayOfWeek = date.getDayOfWeek().toString();  // "MONDAY", "TUESDAY", etc.
        
        // Check if the travel date matches the off day
        return !dayOfWeek.equalsIgnoreCase(offDay);
    }
}
