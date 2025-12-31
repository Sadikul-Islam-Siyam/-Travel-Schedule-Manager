package com.travelmanager.api.rest;

import com.travelmanager.model.rest.*;
import com.travelmanager.service.rest.ScheduleService;
import io.javalin.http.Context;

import java.util.*;

/**
 * REST Controller for schedule endpoints
 * Handles all HTTP requests for schedule operations
 */
public class ScheduleController {
    private final ScheduleService scheduleService;

    public ScheduleController() {
        this.scheduleService = ScheduleService.getInstance();
    }

    // ============= UNIFIED ENDPOINTS =============

    /**
     * GET /api/schedules
     * Returns all schedules (bus + train) in unified format
     */
    public void getAllSchedules(Context ctx) {
        try {
            List<UnifiedScheduleDTO> schedules = scheduleService.getAllSchedules();
            ctx.json(schedules);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to retrieve schedules", "message", e.getMessage()));
        }
    }

    /**
     * GET /api/routes?start={start}&destination={destination}
     * Search routes across both bus and train schedules
     */
    public void searchRoutes(Context ctx) {
        String start = ctx.queryParam("start");
        String destination = ctx.queryParam("destination");

        if (start == null || start.trim().isEmpty() || destination == null || destination.trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "Missing required parameters", 
                                       "message", "Both 'start' and 'destination' query parameters are required"));
            return;
        }

        try {
            List<UnifiedScheduleDTO> results = scheduleService.searchRoutes(start.trim(), destination.trim());
            ctx.json(results);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to search routes", "message", e.getMessage()));
        }
    }

    // ============= BUS ENDPOINTS =============

    /**
     * GET /api/schedules/bus
     * Returns all bus schedules
     */
    public void getAllBusSchedules(Context ctx) {
        try {
            List<BusScheduleDTO> schedules = scheduleService.getAllBusSchedules();
            ctx.json(schedules);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to retrieve bus schedules", "message", e.getMessage()));
        }
    }

    /**
     * GET /api/schedules/bus/{busName}
     * Get a specific bus schedule
     */
    public void getBusSchedule(Context ctx) {
        String busName = ctx.pathParam("busName");
        
        try {
            Optional<BusScheduleDTO> schedule = scheduleService.getBusSchedule(busName);
            if (schedule.isPresent()) {
                ctx.json(schedule.get());
            } else {
                ctx.status(404).json(Map.of("error", "Bus schedule not found", 
                                           "busName", busName));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to retrieve bus schedule", "message", e.getMessage()));
        }
    }

    /**
     * POST /api/schedules/bus
     * Add a new bus schedule (Developer only)
     */
    public void addBusSchedule(Context ctx) {
        try {
            BusScheduleDTO schedule = ctx.bodyAsClass(BusScheduleDTO.class);
            
            // Validate required fields
            if (schedule.getBusName() == null || schedule.getBusName().trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Validation failed", 
                                           "message", "Bus name is required"));
                return;
            }

            boolean added = scheduleService.addBusSchedule(schedule);
            if (added) {
                ctx.status(201).json(Map.of("message", "Bus schedule added successfully", 
                                           "busName", schedule.getBusName()));
            } else {
                ctx.status(409).json(Map.of("error", "Bus schedule already exists", 
                                           "busName", schedule.getBusName()));
            }
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Invalid request body", "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/schedules/bus/{busName}
     * Update an existing bus schedule (Developer only)
     */
    public void updateBusSchedule(Context ctx) {
        String busName = ctx.pathParam("busName");
        
        try {
            BusScheduleDTO schedule = ctx.bodyAsClass(BusScheduleDTO.class);
            
            boolean updated = scheduleService.updateBusSchedule(busName, schedule);
            if (updated) {
                ctx.json(Map.of("message", "Bus schedule updated successfully", 
                               "busName", busName));
            } else {
                ctx.status(404).json(Map.of("error", "Bus schedule not found", 
                                           "busName", busName));
            }
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Invalid request body", "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/schedules/bus/{busName}
     * Delete a bus schedule (Developer only)
     */
    public void deleteBusSchedule(Context ctx) {
        String busName = ctx.pathParam("busName");
        
        try {
            boolean deleted = scheduleService.deleteBusSchedule(busName);
            if (deleted) {
                ctx.json(Map.of("message", "Bus schedule deleted successfully", 
                               "busName", busName));
            } else {
                ctx.status(404).json(Map.of("error", "Bus schedule not found", 
                                           "busName", busName));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to delete bus schedule", "message", e.getMessage()));
        }
    }

    // ============= TRAIN ENDPOINTS =============

    /**
     * GET /api/schedules/train
     * Returns all train schedules
     */
    public void getAllTrainSchedules(Context ctx) {
        try {
            List<TrainScheduleDTO> schedules = scheduleService.getAllTrainSchedules();
            ctx.json(schedules);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to retrieve train schedules", "message", e.getMessage()));
        }
    }

    /**
     * GET /api/schedules/train/{trainName}
     * Get a specific train schedule
     */
    public void getTrainSchedule(Context ctx) {
        String trainName = ctx.pathParam("trainName");
        
        try {
            Optional<TrainScheduleDTO> schedule = scheduleService.getTrainSchedule(trainName);
            if (schedule.isPresent()) {
                ctx.json(schedule.get());
            } else {
                ctx.status(404).json(Map.of("error", "Train schedule not found", 
                                           "trainName", trainName));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to retrieve train schedule", "message", e.getMessage()));
        }
    }

    /**
     * POST /api/schedules/train
     * Add a new train schedule (Developer only)
     */
    public void addTrainSchedule(Context ctx) {
        try {
            TrainScheduleDTO schedule = ctx.bodyAsClass(TrainScheduleDTO.class);
            
            // Validate required fields
            if (schedule.getTrainName() == null || schedule.getTrainName().trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Validation failed", 
                                           "message", "Train name is required"));
                return;
            }

            boolean added = scheduleService.addTrainSchedule(schedule);
            if (added) {
                ctx.status(201).json(Map.of("message", "Train schedule added successfully", 
                                           "trainName", schedule.getTrainName()));
            } else {
                ctx.status(409).json(Map.of("error", "Train schedule already exists", 
                                           "trainName", schedule.getTrainName()));
            }
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Invalid request body", "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/schedules/train/{trainName}
     * Update an existing train schedule (Developer only)
     */
    public void updateTrainSchedule(Context ctx) {
        String trainName = ctx.pathParam("trainName");
        
        try {
            TrainScheduleDTO schedule = ctx.bodyAsClass(TrainScheduleDTO.class);
            
            boolean updated = scheduleService.updateTrainSchedule(trainName, schedule);
            if (updated) {
                ctx.json(Map.of("message", "Train schedule updated successfully", 
                               "trainName", trainName));
            } else {
                ctx.status(404).json(Map.of("error", "Train schedule not found", 
                                           "trainName", trainName));
            }
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Invalid request body", "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/schedules/train/{trainName}
     * Delete a train schedule (Developer only)
     */
    public void deleteTrainSchedule(Context ctx) {
        String trainName = ctx.pathParam("trainName");
        
        try {
            boolean deleted = scheduleService.deleteTrainSchedule(trainName);
            if (deleted) {
                ctx.json(Map.of("message", "Train schedule deleted successfully", 
                               "trainName", trainName));
            } else {
                ctx.status(404).json(Map.of("error", "Train schedule not found", 
                                           "trainName", trainName));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to delete train schedule", "message", e.getMessage()));
        }
    }
}
