package com.travelmanager.model;

import java.time.LocalDateTime;

/**
 * Base class for transportation schedules
 */
public abstract class Schedule {
    protected String id;
    protected String origin;
    protected String destination;
    protected LocalDateTime departureTime;
    protected LocalDateTime arrivalTime;
    protected double fare;
    protected int availableSeats;

    public Schedule(String id, String origin, String destination, 
                   LocalDateTime departureTime, LocalDateTime arrivalTime, 
                   double fare, int availableSeats) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.fare = fare;
        this.availableSeats = availableSeats;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public abstract String getType();
}
