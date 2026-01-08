package com.travelmanager.model.rest;

import java.util.List;

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
    private List<TrainStop> stops; // List of intermediate stops

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

    public List<TrainStop> getStops() { return stops; }
    public void setStops(List<TrainStop> stops) { this.stops = stops; }

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
                ", stops=" + (stops != null ? stops.size() + " stops" : "no stops") +
                '}';
    }

    /**
     * Inner class representing a train stop
     */
    public static class TrainStop {
        private String station;
        private String arrivalTime;
        private String departureTime;
        private double cumulativeFare;  // Fare from start station to this station

        public TrainStop() {}

        public TrainStop(String station, String arrivalTime, String departureTime, double cumulativeFare) {
            this.station = station;
            this.arrivalTime = arrivalTime;
            this.departureTime = departureTime;
            this.cumulativeFare = cumulativeFare;
        }

        public String getStation() { return station; }
        public void setStation(String station) { this.station = station; }

        public String getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

        public double getCumulativeFare() { return cumulativeFare; }
        public void setCumulativeFare(double cumulativeFare) { this.cumulativeFare = cumulativeFare; }

        @Override
        public String toString() {
            return station + " (" + arrivalTime + "-" + departureTime + ", ৳" + cumulativeFare + ")";
        }
    }
}

