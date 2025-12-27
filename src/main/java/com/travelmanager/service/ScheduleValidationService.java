package com.travelmanager.service;

import com.travelmanager.exception.ValidationException;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.util.Constants;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Service for validating schedule data
 */
public class ScheduleValidationService {
    
    private static final List<String> VALID_LOCATIONS = Arrays.asList(
        "Barguna", "Barishal", "Bhola", "Jhalokati", "Patuakhali", "Pirojpur",
        "Bandarban", "Brahmanbaria", "Chandpur", "Chattogram", "Cox's Bazar", "Cumilla", "Feni", "Khagrachari", "Lakshmipur", "Noakhali", "Rangamati",
        "Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur", "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail",
        "Bagerhat", "Chuadanga", "Jashore", "Jhenaidah", "Khulna", "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira",
        "Jamalpur", "Mymensingh", "Netrokona", "Sherpur",
        "Bogura", "Joypurhat", "Naogaon", "Natore", "Chapai Nawabganj", "Pabna", "Rajshahi", "Sirajganj",
        "Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari", "Panchagarh", "Rangpur", "Thakurgaon",
        "Habiganj", "Moulvibazar", "Sunamganj", "Sylhet"
    );
    
    /**
     * Validate schedule with all rules
     */
    public void validateSchedule(Schedule schedule) throws ValidationException {
        if (schedule == null) {
            throw new ValidationException("Schedule cannot be null");
        }
        
        validateBasicFields(schedule);
        validateLocations(schedule);
        validateTiming(schedule);
        validateFare(schedule);
        validateSeats(schedule);
        validateTypeSpecificFields(schedule);
    }
    
    private void validateBasicFields(Schedule schedule) throws ValidationException {
        if (schedule.getId() == null || schedule.getId().trim().isEmpty()) {
            throw new ValidationException("Schedule ID is required");
        }
        if (schedule.getOrigin() == null || schedule.getOrigin().trim().isEmpty()) {
            throw new ValidationException("Origin is required");
        }
        if (schedule.getDestination() == null || schedule.getDestination().trim().isEmpty()) {
            throw new ValidationException("Destination is required");
        }
        if (schedule.getOrigin().equalsIgnoreCase(schedule.getDestination())) {
            throw new ValidationException("Origin and destination must be different");
        }
    }
    
    private void validateLocations(Schedule schedule) throws ValidationException {
        if (!VALID_LOCATIONS.contains(schedule.getOrigin())) {
            throw new ValidationException("Invalid origin: " + schedule.getOrigin());
        }
        if (!VALID_LOCATIONS.contains(schedule.getDestination())) {
            throw new ValidationException("Invalid destination: " + schedule.getDestination());
        }
    }
    
    private void validateTiming(Schedule schedule) throws ValidationException {
        if (schedule.getDepartureTime() == null) {
            throw new ValidationException("Departure time is required");
        }
        if (schedule.getArrivalTime() == null) {
            throw new ValidationException("Arrival time is required");
        }
        if (!schedule.getArrivalTime().isAfter(schedule.getDepartureTime())) {
            throw new ValidationException("Arrival time must be after departure time");
        }
        
        Duration duration = Duration.between(schedule.getDepartureTime(), schedule.getArrivalTime());
        if (duration.toHours() > Constants.MAX_JOURNEY_HOURS) {
            throw new ValidationException("Journey duration cannot exceed " + Constants.MAX_JOURNEY_HOURS + " hours");
        }
    }
    
    private void validateFare(Schedule schedule) throws ValidationException {
        if (schedule.getFare() < Constants.MIN_FARE) {
            throw new ValidationException("Fare must be at least " + Constants.MIN_FARE);
        }
        if (schedule.getFare() > Constants.MAX_FARE) {
            throw new ValidationException("Fare cannot exceed " + Constants.MAX_FARE);
        }
    }
    
    private void validateSeats(Schedule schedule) throws ValidationException {
        if (schedule.getAvailableSeats() < 0) {
            throw new ValidationException("Available seats cannot be negative");
        }
        if (schedule.getAvailableSeats() > Constants.MAX_SEATS) {
            throw new ValidationException("Available seats cannot exceed " + Constants.MAX_SEATS);
        }
    }
    
    private void validateTypeSpecificFields(Schedule schedule) throws ValidationException {
        if (schedule instanceof BusSchedule) {
            BusSchedule bus = (BusSchedule) schedule;
            if (bus.getBusCompany() == null || bus.getBusCompany().trim().isEmpty()) {
                throw new ValidationException("Bus company is required");
            }
            if (bus.getBusType() == null || bus.getBusType().trim().isEmpty()) {
                throw new ValidationException("Bus type is required");
            }
        } else if (schedule instanceof TrainSchedule) {
            TrainSchedule train = (TrainSchedule) schedule;
            if (train.getTrainName() == null || train.getTrainName().trim().isEmpty()) {
                throw new ValidationException("Train name is required");
            }
            if (train.getSeatClass() == null || train.getSeatClass().trim().isEmpty()) {
                throw new ValidationException("Seat class is required");
            }
        }
    }
    
    /**
     * Get list of valid locations
     */
    public List<String> getValidLocations() {
        return VALID_LOCATIONS;
    }
}
