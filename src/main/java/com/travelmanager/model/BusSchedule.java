package com.travelmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a bus schedule
 */
public class BusSchedule extends Schedule {
    private String busCompany;
    private String busType;

    public BusSchedule(String id, String origin, String destination,
                      LocalDateTime departureTime, LocalDateTime arrivalTime,
                      double fare, int availableSeats,
                      String busCompany, String busType) {
        super(id, origin, destination, departureTime, arrivalTime, fare, availableSeats);
        this.busCompany = busCompany;
        this.busType = busType;
    }

    @Override
    public String getType() {
        return "Bus";
    }

    public String getBusCompany() { return busCompany; }
    public void setBusCompany(String busCompany) { this.busCompany = busCompany; }

    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }
}
