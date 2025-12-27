package com.travelmanager.domain;

import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.util.Constants;

import java.time.Duration;
import java.util.*;

/**
 * Pathfinding engine using modified Dijkstra's algorithm
 * Finds K-shortest paths between two cities
 */
public class PathfindingEngine {
    
    private RouteGraph graph;
    private OptimizationCriteria criteria;
    
    public enum OptimizationCriteria {
        SHORTEST_TIME,    // Minimize travel time
        LOWEST_COST,      // Minimize fare
        FEWEST_HOPS,      // Minimize transfers
        BALANCED          // Balance time and cost
    }
    
    public PathfindingEngine(RouteGraph graph, OptimizationCriteria criteria) {
        this.graph = graph;
        this.criteria = criteria;
    }
    
    /**
     * Find multiple routes between origin and destination
     * @param origin Starting city
     * @param destination Destination city
     * @param maxRoutes Maximum number of routes to return
     * @return List of routes sorted by optimization criteria
     */
    public List<Route> findRoutes(String origin, String destination, int maxRoutes) {
        if (!graph.containsCity(origin)) {
            System.err.println("Origin city not found in graph: " + origin);
            return new ArrayList<>();
        }
        if (!graph.containsCity(destination)) {
            System.err.println("Destination city not found in graph: " + destination);
            return new ArrayList<>();
        }
        
        return findKShortestPaths(origin, destination, maxRoutes);
    }
    
    /**
     * Modified Dijkstra to find K shortest paths
     */
    private List<Route> findKShortestPaths(String origin, String destination, int k) {
        List<Route> completedRoutes = new ArrayList<>();
        
        // Priority queue: (cost, current city, path taken, visited cities)
        PriorityQueue<PathState> queue = new PriorityQueue<>(
            Comparator.comparingDouble(state -> state.cost)
        );
        
        queue.add(new PathState(0, origin, new ArrayList<>(), new HashSet<>()));
        
        int exploredPaths = 0;
        int maxExplorations = 10000; // Prevent infinite loops
        
        while (!queue.isEmpty() && completedRoutes.size() < k && exploredPaths < maxExplorations) {
            exploredPaths++;
            PathState current = queue.poll();
            
            // Reached destination - create route
            if (current.city.equals(destination)) {
                if (!current.path.isEmpty()) {
                    Route route = buildRoute(current.path);
                    completedRoutes.add(route);
                }
                continue;
            }
            
            // Skip if path is too long
            if (current.path.size() >= Constants.MAX_ROUTE_HOPS) {
                continue;
            }
            
            // Explore neighbors
            for (RouteGraph.GraphEdge edge : graph.getNeighbors(current.city)) {
                // Avoid cycles
                if (current.visited.contains(edge.getTo())) {
                    continue;
                }
                
                // Check connection validity
                if (!current.path.isEmpty()) {
                    Schedule lastSchedule = current.path.get(current.path.size() - 1);
                    if (!isValidConnection(lastSchedule, edge.getSchedule())) {
                        continue;
                    }
                }
                
                // Create new path state
                List<Schedule> newPath = new ArrayList<>(current.path);
                newPath.add(edge.getSchedule());
                
                Set<String> newVisited = new HashSet<>(current.visited);
                newVisited.add(current.city);
                
                double newCost = current.cost + getEdgeCost(edge);
                
                queue.add(new PathState(newCost, edge.getTo(), newPath, newVisited));
            }
        }
        
        return completedRoutes;
    }
    
    /**
     * Check if two schedules can be connected
     * Must have at least 30 minutes layover
     */
    private boolean isValidConnection(Schedule first, Schedule second) {
        // Must connect at same city
        if (!first.getDestination().equals(second.getOrigin())) {
            return false;
        }
        
        // Calculate layover time
        Duration layover = Duration.between(
            first.getArrivalTime(),
            second.getDepartureTime()
        );
        
        // Must have minimum connection time
        if (layover.toMinutes() < Constants.MIN_CONNECTION_TIME_MINUTES) {
            return false;
        }
        
        // Must not have excessively long layover
        if (layover.toHours() > Constants.MAX_LAYOVER_HOURS) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate edge cost based on optimization criteria
     */
    private double getEdgeCost(RouteGraph.GraphEdge edge) {
        Schedule schedule = edge.getSchedule();
        
        switch (criteria) {
            case SHORTEST_TIME:
                return Duration.between(
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime()
                ).toMinutes();
                
            case LOWEST_COST:
                return schedule.getFare();
                
            case FEWEST_HOPS:
                return 1.0;
                
            case BALANCED:
                // Weighted combination: 60% time, 40% cost
                double timeScore = Duration.between(
                    schedule.getDepartureTime(),
                    schedule.getArrivalTime()
                ).toMinutes() / 60.0;
                double costScore = schedule.getFare() / 100.0;
                return (0.6 * timeScore) + (0.4 * costScore);
                
            default:
                return edge.getWeight();
        }
    }
    
    /**
     * Build Route object from schedule path
     */
    private Route buildRoute(List<Schedule> path) {
        Route route = new Route();
        for (Schedule schedule : path) {
            route.addSchedule(schedule);
        }
        return route;
    }
    
    /**
     * Internal state for pathfinding algorithm
     */
    private static class PathState {
        double cost;
        String city;
        List<Schedule> path;
        Set<String> visited;
        
        PathState(double cost, String city, List<Schedule> path, Set<String> visited) {
            this.cost = cost;
            this.city = city;
            this.path = path;
            this.visited = visited;
        }
    }
}
