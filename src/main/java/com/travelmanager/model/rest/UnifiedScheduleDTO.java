package com.travelmanager.model.rest;

/**
 * Unified Data Transfer Object for REST API
 * Combines both bus and train schedules with a type indicator
 */
public class UnifiedScheduleDTO {
    private String type;           // "bus" or "train"
    private String name;           // busName or trainName
    private String start;
    private String destination;
    private String startTime;
    private String arrivalTime;
    private double fare;
    private String duration;
    private String offDay;         // Only for trains (null for buses)

    // Constructors
    public UnifiedScheduleDTO() {}

    // Factory method to create from BusScheduleDTO
    public static UnifiedScheduleDTO fromBus(BusScheduleDTO bus) {
        UnifiedScheduleDTO dto = new UnifiedScheduleDTO();
        dto.type = "bus";
        dto.name = bus.getBusName();
        dto.start = bus.getStart();
        dto.destination = bus.getDestination();
        dto.startTime = bus.getStartTime();
        dto.arrivalTime = bus.getArrivalTime();
        dto.fare = bus.getFare();
        dto.duration = bus.getDuration();
        dto.offDay = null;
        return dto;
    }

    // Factory method to create from TrainScheduleDTO
    public static UnifiedScheduleDTO fromTrain(TrainScheduleDTO train) {
        UnifiedScheduleDTO dto = new UnifiedScheduleDTO();
        dto.type = "train";
        dto.name = train.getTrainName();
        dto.start = train.getStart();
        dto.destination = train.getDestination();
        dto.startTime = train.getStartTime();
        dto.arrivalTime = train.getArrivalTime();
        dto.fare = train.getFare();
        dto.duration = train.getDuration();
        dto.offDay = train.getOffDay();
        return dto;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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
}
