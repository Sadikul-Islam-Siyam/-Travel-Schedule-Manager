package com.travelmanager.api.rest.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.travelmanager.model.rest.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Example client demonstrating how to consume the REST API
 * This shows how JavaFX controllers should interact with the API
 */
public class RestApiClientExample {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final Gson gson;

    public RestApiClientExample() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // ============= USER MODE OPERATIONS (GET only) =============

    /**
     * Search for routes (User mode)
     */
    public List<UnifiedScheduleDTO> searchRoutes(String start, String destination) throws Exception {
        String encodedStart = URLEncoder.encode(start, StandardCharsets.UTF_8);
        String encodedDest = URLEncoder.encode(destination, StandardCharsets.UTF_8);
        String url = BASE_URL + "/routes?start=" + encodedStart + "&destination=" + encodedDest;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<UnifiedScheduleDTO>>(){}.getType());
        } else {
            throw new RuntimeException("Failed to search routes: " + response.body());
        }
    }

    /**
     * Get all schedules (User mode)
     */
    public List<UnifiedScheduleDTO> getAllSchedules() throws Exception {
        String url = BASE_URL + "/schedules";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<UnifiedScheduleDTO>>(){}.getType());
        } else {
            throw new RuntimeException("Failed to get schedules: " + response.body());
        }
    }

    /**
     * Get all bus schedules (User mode)
     */
    public List<BusScheduleDTO> getAllBusSchedules() throws Exception {
        String url = BASE_URL + "/schedules/bus";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<BusScheduleDTO>>(){}.getType());
        } else {
            throw new RuntimeException("Failed to get bus schedules: " + response.body());
        }
    }

    /**
     * Get all train schedules (User mode)
     */
    public List<TrainScheduleDTO> getAllTrainSchedules() throws Exception {
        String url = BASE_URL + "/schedules/train";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), new TypeToken<List<TrainScheduleDTO>>(){}.getType());
        } else {
            throw new RuntimeException("Failed to get train schedules: " + response.body());
        }
    }

    // ============= DEVELOPER MODE OPERATIONS (POST/PUT/DELETE) =============

    /**
     * Add a new bus schedule (Developer mode)
     */
    public boolean addBusSchedule(BusScheduleDTO schedule) throws Exception {
        String url = BASE_URL + "/schedules/bus";
        String jsonBody = gson.toJson(schedule);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 201;
    }

    /**
     * Update a bus schedule (Developer mode)
     */
    public boolean updateBusSchedule(String busName, BusScheduleDTO schedule) throws Exception {
        String encodedName = URLEncoder.encode(busName, StandardCharsets.UTF_8);
        String url = BASE_URL + "/schedules/bus/" + encodedName;
        String jsonBody = gson.toJson(schedule);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 200;
    }

    /**
     * Delete a bus schedule (Developer mode)
     */
    public boolean deleteBusSchedule(String busName) throws Exception {
        String encodedName = URLEncoder.encode(busName, StandardCharsets.UTF_8);
        String url = BASE_URL + "/schedules/bus/" + encodedName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 200;
    }

    /**
     * Add a new train schedule (Developer mode)
     */
    public boolean addTrainSchedule(TrainScheduleDTO schedule) throws Exception {
        String url = BASE_URL + "/schedules/train";
        String jsonBody = gson.toJson(schedule);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 201;
    }

    /**
     * Update a train schedule (Developer mode)
     */
    public boolean updateTrainSchedule(String trainName, TrainScheduleDTO schedule) throws Exception {
        String encodedName = URLEncoder.encode(trainName, StandardCharsets.UTF_8);
        String url = BASE_URL + "/schedules/train/" + encodedName;
        String jsonBody = gson.toJson(schedule);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 200;
    }

    /**
     * Delete a train schedule (Developer mode)
     */
    public boolean deleteTrainSchedule(String trainName) throws Exception {
        String encodedName = URLEncoder.encode(trainName, StandardCharsets.UTF_8);
        String url = BASE_URL + "/schedules/train/" + encodedName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.statusCode() == 200;
    }

    // ============= EXAMPLE USAGE =============

    public static void main(String[] args) {
        RestApiClientExample client = new RestApiClientExample();

        try {
            System.out.println("=== REST API Client Example ===\n");

            // Example 1: Search routes (User mode)
            System.out.println("1. Searching routes from Dhaka to Chittagong...");
            List<UnifiedScheduleDTO> routes = client.searchRoutes("Dhaka", "Chittagong");
            System.out.println("Found " + routes.size() + " routes:");
            routes.forEach(route -> 
                System.out.println("  - " + route.getType() + ": " + route.getName() + 
                                 " | Fare: " + route.getFare() + " | Duration: " + route.getDuration())
            );
            System.out.println();

            // Example 2: Get all bus schedules (User mode)
            System.out.println("2. Getting all bus schedules...");
            List<BusScheduleDTO> buses = client.getAllBusSchedules();
            System.out.println("Total bus schedules: " + buses.size());
            System.out.println();

            // Example 3: Add a new bus schedule (Developer mode)
            System.out.println("3. Adding a new bus schedule...");
            BusScheduleDTO newBus = new BusScheduleDTO(
                "Example Bus Service",
                "Dhaka",
                "Khulna",
                "11:00",
                "19:00",
                800.0,
                "8:00h"
            );
            boolean added = client.addBusSchedule(newBus);
            System.out.println("Bus schedule added: " + added);
            System.out.println();

            // Example 4: Update the bus schedule (Developer mode)
            System.out.println("4. Updating the bus schedule...");
            newBus.setFare(850.0);
            boolean updated = client.updateBusSchedule("Example Bus Service", newBus);
            System.out.println("Bus schedule updated: " + updated);
            System.out.println();

            // Example 5: Delete the bus schedule (Developer mode)
            System.out.println("5. Deleting the bus schedule...");
            boolean deleted = client.deleteBusSchedule("Example Bus Service");
            System.out.println("Bus schedule deleted: " + deleted);
            System.out.println();

            System.out.println("=== All examples completed successfully! ===");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
