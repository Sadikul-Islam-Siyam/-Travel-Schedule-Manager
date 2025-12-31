package com.travelmanager.model.rest;

/**
 * Data Transfer Object for Train Schedule REST API
 * Represents the train schedule format for REST operations
 */
public class TrainScheduleDTO {
    private String trainName;      // Unique identifier like "RANGPUR EXPRESS (772)"
    private String start;
    private String destination;
    private String startTime;      // Format: "HH:MM"
    private String arrivalTime;    // Format: "HH:MM"
    private double fare;
    private String duration;       // Format: "10:00h"
    private String offDay;         // e.g., "No off day", "Sunday", etc.

    // Constructors
    public TrainScheduleDTO() {}

    public TrainScheduleDTO(String trainName, String start, String destination,
                           String startTime, String arrivalTime, double fare, 
                           String duration, String offDay) {
        this.trainName = trainName;
        this.start = start;
        this.destination = destination;
        this.startTime = startTime;
        this.arrivalTime = arrivalTime;
        this.fare = fare;
        this.duration = duration;
        this.offDay = offDay;
    }

    // Getters and Setters
    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getOffDay() { return offDay; }
    public void setOffDay(String offDay) { this.offDay = offDay; }

    @Override
    public String toString() {
        return "TrainScheduleDTO{" +
                "trainName='" + trainName + '\'' +
                ", start='" + start + '\'' +
                ", destination='" + destination + '\'' +
                ", startTime='" + startTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", fare=" + fare +
                ", duration='" + duration + '\'' +
                ", offDay='" + offDay + '\'' +
                '}';
    }
}
