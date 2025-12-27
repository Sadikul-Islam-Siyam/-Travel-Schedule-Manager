package com.travelmanager.service;

import com.travelmanager.domain.PathfindingEngine;
import com.travelmanager.domain.RouteGraph;
import com.travelmanager.exception.RouteNotFoundException;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.util.CacheManager;
import com.travelmanager.util.Constants;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for route optimization using pathfinding algorithms
 * Orchestrates schedule fetching, graph building, and route finding
 */
public class RouteOptimizationService {
    
    private ScheduleService scheduleService;
    private CacheManager cacheManager;
    
    public RouteOptimizationService() {
        this.scheduleService = new ScheduleService();
        this.cacheManager = CacheManager.getInstance();
    }
    
    /**
     * Find optimal routes between two cities
     * @param origin Starting city
     * @param destination Destination city
     * @param date Travel date
     * @param criteria Optimization criteria
     * @param transportFilter Filter by transport type ("ALL", "BUS", "TRAIN")
     * @param maxResults Maximum number of routes to return
     * @return List of routes sorted by optimization criteria
     */
    public List<Route> findOptimalRoutes(
        String origin,
        String destination,
        LocalDate date,
        PathfindingEngine.OptimizationCriteria criteria,
        String transportFilter,
        int maxResults
    ) throws RouteNotFoundException {
        
        // Check cache first
        String cacheKey = buildCacheKey(origin, destination, date, criteria, transportFilter);
        @SuppressWarnings("unchecked")
        List<Route> cachedRoutes = (List<Route>) cacheManager.get(cacheKey);
        if (cachedRoutes != null) {
            System.out.println("Returning cached routes for " + origin + " → " + destination);
            return cachedRoutes.stream().limit(maxResults).collect(Collectors.toList());
        }
        
        // Fetch schedules for the date
        List<Schedule> schedules = fetchSchedules(origin, destination, date, transportFilter);
        
        if (schedules.isEmpty()) {
            throw new RouteNotFoundException(
                "No schedules found for " + origin + " → " + destination + " on " + date
            );
        }
        
        System.out.println("Building graph with " + schedules.size() + " schedules");
        
        // Build graph from schedules
        RouteGraph graph = new RouteGraph(schedules);
        
        System.out.println("Graph built: " + graph.getNodeCount() + " nodes, " + 
                         graph.getEdgeCount() + " edges");
        
        // Run pathfinding algorithm
        PathfindingEngine engine = new PathfindingEngine(graph, criteria);
        List<Route> routes = engine.findRoutes(origin, destination, maxResults * 2); // Get more for sorting
        
        if (routes.isEmpty()) {
            throw new RouteNotFoundException(
                "No valid routes found between " + origin + " and " + destination
            );
        }
        
        System.out.println("Found " + routes.size() + " routes");
        
        // Sort routes by criteria
        sortRoutes(routes, criteria);
        
        // Limit to max results
        List<Route> limitedRoutes = routes.stream()
            .limit(maxResults)
            .collect(Collectors.toList());
        
        // Cache results
        cacheManager.put(cacheKey, limitedRoutes, Constants.CACHE_TTL_SECONDS);
        
        return limitedRoutes;
    }
    
    /**
     * Fetch all relevant schedules for route finding
     * For multi-hop routes, we need ALL schedules on the date, not just direct connections
     */
    private List<Schedule> fetchSchedules(
        String origin,
        String destination,
        LocalDate date,
        String transportFilter
    ) {
        List<Schedule> allSchedules = new ArrayList<>();
        
        // Fetch all schedules for the date (not just origin→destination)
        // This enables multi-hop route finding
        switch (transportFilter.toUpperCase()) {
            case "BUS":
                allSchedules = scheduleService.searchBusSchedules("", "", date);
                break;
            case "TRAIN":
                allSchedules = scheduleService.searchTrainSchedules("", "", date);
                break;
            case "ALL":
            default:
                allSchedules = scheduleService.searchSchedules("", "", date);
                break;
        }
        
        return allSchedules;
    }
    
    /**
     * Sort routes based on optimization criteria
     */
    private void sortRoutes(List<Route> routes, PathfindingEngine.OptimizationCriteria criteria) {
        switch (criteria) {
            case SHORTEST_TIME:
                routes.sort(Comparator.comparing(r -> 
                    r.getTotalDuration() != null ? r.getTotalDuration().toMinutes() : Long.MAX_VALUE
                ));
                break;
                
            case LOWEST_COST:
                routes.sort(Comparator.comparingDouble(Route::getTotalFare));
                break;
                
            case FEWEST_HOPS:
                routes.sort(Comparator.comparingInt(r -> r.getSchedules().size()));
                break;
                
            case BALANCED:
                routes.sort(Comparator.comparingDouble(r -> {
                    double timeScore = r.getTotalDuration() != null ? 
                        r.getTotalDuration().toMinutes() / 60.0 : 1000.0;
                    double costScore = r.getTotalFare() / 100.0;
                    return (0.6 * timeScore) + (0.4 * costScore);
                }));
                break;
        }
    }
    
    /**
     * Build cache key for route search
     */
    private String buildCacheKey(
        String origin,
        String destination,
        LocalDate date,
        PathfindingEngine.OptimizationCriteria criteria,
        String transportFilter
    ) {
        return String.format("%s%s_%s_%s_%s_%s",
            Constants.CACHE_ROUTE_PREFIX,
            origin,
            destination,
            date,
            criteria,
            transportFilter
        );
    }
    
    /**
     * Find direct connections only (single-hop routes)
     */
    public List<Route> findDirectRoutes(
        String origin,
        String destination,
        LocalDate date,
        String transportFilter
    ) {
        List<Schedule> schedules = new ArrayList<>();
        
        switch (transportFilter.toUpperCase()) {
            case "BUS":
                schedules = scheduleService.searchBusSchedules(origin, destination, date);
                break;
            case "TRAIN":
                schedules = scheduleService.searchTrainSchedules(origin, destination, date);
                break;
            case "ALL":
            default:
                schedules = scheduleService.searchSchedules(origin, destination, date);
                break;
        }
        
        // Convert each schedule to a single-hop route
        List<Route> routes = new ArrayList<>();
        for (Schedule schedule : schedules) {
            Route route = new Route();
            route.addSchedule(schedule);
            routes.add(route);
        }
        
        return routes;
    }
}
