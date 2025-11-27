package com.travelmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a train schedule
 */
public class TrainSchedule extends Schedule {
    private String trainName;
    private String trainNumber;
    private String seatClass;

    public TrainSchedule(String id, String origin, String destination,
                        LocalDateTime departureTime, LocalDateTime arrivalTime,
                        double fare, int availableSeats,
                        String trainName, String trainNumber, String seatClass) {
        super(id, origin, destination, departureTime, arrivalTime, fare, availableSeats);
        this.trainName = trainName;
        this.trainNumber = trainNumber;
        this.seatClass = seatClass;
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
}
