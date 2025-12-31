package com.travelmanager.model.rest;

/**
 * Data Transfer Object for Bus Schedule REST API
 * Represents the bus schedule format for REST operations
 */
public class BusScheduleDTO {
    private String busName;        // Unique identifier
    private String start;
    private String destination;
    private String startTime;      // Format: "HH:MM"
    private String arrivalTime;    // Format: "HH:MM"
    private double fare;
    private String duration;       // Format: "10:00h"

    // Constructors
    public BusScheduleDTO() {}

    public BusScheduleDTO(String busName, String start, String destination, 
                         String startTime, String arrivalTime, double fare, String duration) {
        this.busName = busName;
        this.start = start;
        this.destination = destination;
        this.startTime = startTime;
        this.arrivalTime = arrivalTime;
        this.fare = fare;
        this.duration = duration;
    }

    // Getters and Setters
    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

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

    @Override
    public String toString() {
        return "BusScheduleDTO{" +
                "busName='" + busName + '\'' +
                ", start='" + start + '\'' +
                ", destination='" + destination + '\'' +
                ", startTime='" + startTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", fare=" + fare +
                ", duration='" + duration + '\'' +
                '}';
    }
}
