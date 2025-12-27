package com.travelmanager.domain;

import com.travelmanager.model.Schedule;

import java.time.Duration;
import java.util.*;

/**
 * Graph representation of schedule network
 * Nodes = Cities, Edges = Schedules
 */
public class RouteGraph {
    
    private Map<String, GraphNode> nodes;
    private Map<String, List<GraphEdge>> adjacencyList;
    
    public RouteGraph(List<Schedule> schedules) {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        buildGraph(schedules);
    }
    
    /**
     * Build graph from list of schedules
     */
    private void buildGraph(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        
        // Step 1: Create nodes for all unique locations
        Set<String> locations = new HashSet<>();
        for (Schedule schedule : schedules) {
            locations.add(schedule.getOrigin());
            locations.add(schedule.getDestination());
        }
        
        for (String location : locations) {
            nodes.put(location, new GraphNode(location));
            adjacencyList.put(location, new ArrayList<>());
        }
        
        // Step 2: Create directed edges from schedules
        for (Schedule schedule : schedules) {
            double weight = calculateWeight(schedule);
            GraphEdge edge = new GraphEdge(
                schedule.getOrigin(),
                schedule.getDestination(),
                schedule,
                weight
            );
            adjacencyList.get(schedule.getOrigin()).add(edge);
        }
    }
    
    /**
     * Calculate edge weight (default: time in minutes)
     */
    private double calculateWeight(Schedule schedule) {
        Duration duration = Duration.between(
            schedule.getDepartureTime(),
            schedule.getArrivalTime()
        );
        return duration.toMinutes();
    }
    
    /**
     * Get all neighbors (outgoing edges) of a city
     */
    public List<GraphEdge> getNeighbors(String city) {
        return adjacencyList.getOrDefault(city, new ArrayList<>());
    }
    
    /**
     * Get all cities in the graph
     */
    public Set<String> getAllCities() {
        return nodes.keySet();
    }
    
    /**
     * Check if graph contains a city
     */
    public boolean containsCity(String city) {
        return nodes.containsKey(city);
    }
    
    /**
     * Get number of nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Get number of edges
     */
    public int getEdgeCount() {
        return adjacencyList.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    /**
     * Graph node representing a city
     */
    public static class GraphNode {
        private String cityName;
        private Map<String, Object> metadata;
        
        public GraphNode(String cityName) {
            this.cityName = cityName;
            this.metadata = new HashMap<>();
        }
        
        public String getCityName() {
            return cityName;
        }
        
        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }
    
    /**
     * Graph edge representing a schedule connection
     */
    public static class GraphEdge {
        private String from;
        private String to;
        private Schedule schedule;
        private double weight;
        
        public GraphEdge(String from, String to, Schedule schedule, double weight) {
            this.from = from;
            this.to = to;
            this.schedule = schedule;
            this.weight = weight;
        }
        
        public String getFrom() {
            return from;
        }
        
        public String getTo() {
            return to;
        }
        
        public Schedule getSchedule() {
            return schedule;
        }
        
        public double getWeight() {
            return weight;
        }
    }
}
