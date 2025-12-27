package com.travelmanager.service;

import com.travelmanager.domain.PathfindingEngine;
import com.travelmanager.exception.RouteNotFoundException;
import com.travelmanager.model.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for generating possible routes
 * Delegates to RouteOptimizationService for actual route finding
 */
public class RouteGenerator {
    
    private RouteOptimizationService routeOptimizationService;

    public RouteGenerator() {
        this.routeOptimizationService = new RouteOptimizationService();
    }
    
    public RouteGenerator(ScheduleService scheduleService) {
        // Legacy constructor for backward compatibility
        this.routeOptimizationService = new RouteOptimizationService();
    }

    /**
     * Generate routes between origin and destination
     * Uses current date and balanced optimization
     */
    public List<Route> generateRoutes(String origin, String destination) {
        try {
            return routeOptimizationService.findOptimalRoutes(
                origin,
                destination,
                LocalDate.now(),
                PathfindingEngine.OptimizationCriteria.BALANCED,
                "ALL",
                10
            );
        } catch (RouteNotFoundException e) {
            System.err.println("No routes found: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Generate routes with specific date
     */
    public List<Route> generateRoutes(String origin, String destination, LocalDate date) {
        try {
            return routeOptimizationService.findOptimalRoutes(
                origin,
                destination,
                date,
                PathfindingEngine.OptimizationCriteria.BALANCED,
                "ALL",
                10
            );
        } catch (RouteNotFoundException e) {
            System.err.println("No routes found: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Generate routes with optimization criteria
     */
    public List<Route> generateRoutes(
        String origin,
        String destination,
        LocalDate date,
        PathfindingEngine.OptimizationCriteria criteria
    ) {
        try {
            return routeOptimizationService.findOptimalRoutes(
                origin,
                destination,
                date,
                criteria,
                "ALL",
                10
            );
        } catch (RouteNotFoundException e) {
            System.err.println("No routes found: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
