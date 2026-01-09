package com.travelmanager.service.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.model.rest.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for consuming the REST API endpoints
 * Converts between REST DTOs and domain models
 */
public class RestScheduleService {
    private static final String BASE_URL = "http://localhost:8080/api";
    
    private final HttpClient httpClient;
    private final Gson gson;

    public RestScheduleService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().create();
    }

    /**
     * Search for schedules using the unified REST API endpoint
     */
    public List<Schedule> searchSchedules(String origin, String destination, LocalDate date) {
        try {
            System.out.println("RestScheduleService: Searching schedules - Origin: " + origin + ", Dest: " + destination);
            String encodedStart = URLEncoder.encode(origin, StandardCharsets.UTF_8);
            String encodedDest = URLEncoder.encode(destination, StandardCharsets.UTF_8);
            String url = BASE_URL + "/routes?start=" + encodedStart + "&destination=" + encodedDest;
            System.out.println("RestScheduleService: Calling URL: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("RestScheduleService: Response status: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                List<UnifiedScheduleDTO> dtos = gson.fromJson(response.body(), 
                        new TypeToken<List<UnifiedScheduleDTO>>(){}.getType());
                System.out.println("RestScheduleService: Found " + dtos.size() + " schedules from REST API");
                List<Schedule> schedules = convertUnifiedToSchedules(dtos, date);
                System.out.println("RestScheduleService: Converted to " + schedules.size() + " schedule objects");
                return schedules;
            } else {
                System.err.println("Failed to search schedules: HTTP " + response.statusCode() + " - " + response.body());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error searching schedules via REST API: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get all bus schedules
     */
    public List<BusSchedule> getAllBusSchedules() {
        try {
            String url = BASE_URL + "/schedules/bus";
            System.out.println("RestScheduleService: Fetching all bus schedules from: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("RestScheduleService: Bus schedules response status: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                List<BusScheduleDTO> dtos = gson.fromJson(response.body(), 
                        new TypeToken<List<BusScheduleDTO>>(){}.getType());
                System.out.println("RestScheduleService: Received " + dtos.size() + " bus schedule DTOs");
                List<BusSchedule> schedules = convertBusDTOsToSchedules(dtos);
                System.out.println("RestScheduleService: Converted to " + schedules.size() + " BusSchedule objects");
                return schedules;
            } else {
                System.err.println("Failed to get bus schedules: HTTP " + response.statusCode() + " - " + response.body());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting bus schedules via REST API: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get all train schedules
     */
    public List<TrainSchedule> getAllTrainSchedules() {
        try {
            String url = BASE_URL + "/schedules/train";
            System.out.println("RestScheduleService: Fetching all train schedules from: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("RestScheduleService: Train schedules response status: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                List<TrainScheduleDTO> dtos = gson.fromJson(response.body(), 
                        new TypeToken<List<TrainScheduleDTO>>(){}.getType());
                System.out.println("RestScheduleService: Received " + dtos.size() + " train schedule DTOs");
                List<TrainSchedule> schedules = convertTrainDTOsToSchedules(dtos);
                System.out.println("RestScheduleService: Converted to " + schedules.size() + " TrainSchedule objects");
                return schedules;
            } else {
                System.err.println("Failed to get train schedules: HTTP " + response.statusCode() + " - " + response.body());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting train schedules via REST API: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get all schedules (bus + train)
     */
    public List<Schedule> getAllSchedules() {
        List<Schedule> all = new ArrayList<>();
        all.addAll(getAllBusSchedules());
        all.addAll(getAllTrainSchedules());
        return all;
    }

    /**
     * Search only bus schedules
     */
    public List<Schedule> searchBusSchedules(String origin, String destination, LocalDate date) {
        // Use the REST API endpoint which has flexible matching logic
        List<Schedule> allResults = searchSchedules(origin, destination, date);
        // Filter to only return buses
        return allResults.stream()
                .filter(s -> s instanceof BusSchedule)
                .collect(Collectors.toList());
    }

    /**
     * Search only train schedules
     */
    public List<Schedule> searchTrainSchedules(String origin, String destination, LocalDate date) {
        // Use the REST API endpoint which has flexible matching logic
        List<Schedule> allResults = searchSchedules(origin, destination, date);
        // Filter to only return trains
        return allResults.stream()
                .filter(s -> s instanceof TrainSchedule)
                .collect(Collectors.toList());
    }

    // ============= CONVERSION METHODS =============

    /**
     * Convert UnifiedScheduleDTO list to Schedule list
     */
    private List<Schedule> convertUnifiedToSchedules(List<UnifiedScheduleDTO> dtos, LocalDate date) {
        List<Schedule> schedules = new ArrayList<>();
        for (UnifiedScheduleDTO dto : dtos) {
            if ("bus".equals(dto.getType())) {
                schedules.add(convertBusDTO(dto, date));
            } else if ("train".equals(dto.getType())) {
                schedules.add(convertTrainDTO(dto, date));
            }
        }
        return schedules;
    }

    /**
     * Convert BusScheduleDTO list to BusSchedule list
     */
    private List<BusSchedule> convertBusDTOsToSchedules(List<BusScheduleDTO> dtos) {
        LocalDate today = LocalDate.now();
        return dtos.stream()
                .map(dto -> convertBusDTO(dto, today))
                .collect(Collectors.toList());
    }

    /**
     * Convert TrainScheduleDTO list to TrainSchedule list
     */
    private List<TrainSchedule> convertTrainDTOsToSchedules(List<TrainScheduleDTO> dtos) {
        LocalDate today = LocalDate.now();
        return dtos.stream()
                .map(dto -> convertTrainDTO(dto, today))
                .collect(Collectors.toList());
    }

    /**
     * Convert BusScheduleDTO to BusSchedule
     */
    private BusSchedule convertBusDTO(BusScheduleDTO dto, LocalDate date) {
        LocalDateTime departureTime = parseDateTime(date, dto.getStartTime());
        LocalDateTime arrivalTime = parseDateTime(date, dto.getArrivalTime());
        
        // If arrival is before departure, it's next day
        if (arrivalTime.isBefore(departureTime)) {
            arrivalTime = arrivalTime.plusDays(1);
        }

        return new BusSchedule(
                dto.getBusName(),
                dto.getStart(),
                dto.getDestination(),
                departureTime,
                arrivalTime,
                dto.getFare(),
                50, // default available seats
                dto.getBusName(), // use bus name as company
                "Standard" // default bus type
        );
    }

    /**
     * Convert UnifiedScheduleDTO (bus) to BusSchedule
     */
    private BusSchedule convertBusDTO(UnifiedScheduleDTO dto, LocalDate date) {
        LocalDateTime departureTime = parseDateTime(date, dto.getStartTime());
        LocalDateTime arrivalTime = parseDateTime(date, dto.getArrivalTime());
        
        if (arrivalTime.isBefore(departureTime)) {
            arrivalTime = arrivalTime.plusDays(1);
        }

        return new BusSchedule(
                dto.getName(),
                dto.getStart(),
                dto.getDestination(),
                departureTime,
                arrivalTime,
                dto.getFare(),
                50,
                dto.getName(),
                "Standard"
        );
    }

    /**
     * Convert TrainScheduleDTO to TrainSchedule
     */
    private TrainSchedule convertTrainDTO(TrainScheduleDTO dto, LocalDate date) {
        LocalDateTime departureTime = parseDateTime(date, dto.getStartTime());
        LocalDateTime arrivalTime = parseDateTime(date, dto.getArrivalTime());
        
        if (arrivalTime.isBefore(departureTime)) {
            arrivalTime = arrivalTime.plusDays(1);
        }

        String trainNumber = extractTrainNumber(dto.getTrainName());
        String trainNameOnly = removeTrainNumber(dto.getTrainName());

        return new TrainSchedule(
                dto.getTrainName(),
                dto.getStart(),
                dto.getDestination(),
                departureTime,
                arrivalTime,
                dto.getFare(),
                100, // default available seats
                trainNameOnly,
                trainNumber,
                "Shovan" // default class
        );
    }

    /**
     * Convert UnifiedScheduleDTO (train) to TrainSchedule
     */
    private TrainSchedule convertTrainDTO(UnifiedScheduleDTO dto, LocalDate date) {
        LocalDateTime departureTime = parseDateTime(date, dto.getStartTime());
        LocalDateTime arrivalTime = parseDateTime(date, dto.getArrivalTime());
        
        if (arrivalTime.isBefore(departureTime)) {
            arrivalTime = arrivalTime.plusDays(1);
        }

        String trainNumber = extractTrainNumber(dto.getName());
        String trainNameOnly = removeTrainNumber(dto.getName());

        return new TrainSchedule(
                dto.getName(),
                dto.getStart(),
                dto.getDestination(),
                departureTime,
                arrivalTime,
                dto.getFare(),
                100,
                trainNameOnly,
                trainNumber,
                "Shovan"
        );
    }

    /**
     * Parse time string with date to LocalDateTime
     */
    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour, minute);
        } catch (Exception e) {
            System.err.println("Error parsing time: " + timeStr);
            return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 0, 0);
        }
    }

    /**
     * Extract train number from train name
     */
    private String extractTrainNumber(String trainName) {
        if (trainName.contains("(") && trainName.contains(")")) {
            int start = trainName.indexOf("(") + 1;
            int end = trainName.indexOf(")");
            return trainName.substring(start, end);
        }
        return "000";
    }

    private String removeTrainNumber(String trainName) {
        if (trainName.contains("(") && trainName.contains(")")) {
            int start = trainName.indexOf("(");
            return trainName.substring(0, start).trim();
        }
        return trainName;
    }
}
