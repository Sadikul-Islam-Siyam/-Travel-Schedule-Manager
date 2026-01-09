package com.travelmanager.util;

import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Graph representation of the transportation network
 * Nodes: Cities/Districts
 * Edges: Available routes (trains/buses) with time and fare weights
 */
public class RouteGraph {
    
    // Adjacency list: City -> List of available routes from that city
    private Map<String, List<RouteEdge>> adjacencyList;
    
    // All available schedules
    private List<Schedule> allSchedules;
    
    // Travel date for filtering
    private LocalDate travelDate;
    
    public RouteGraph(List<Schedule> schedules, LocalDate travelDate) {
        this.allSchedules = schedules;
        this.travelDate = travelDate;
        this.adjacencyList = new HashMap<>();
        buildGraph();
    }
    
    /**
     * Build the graph from available schedules
     */
    private void buildGraph() {
        for (Schedule schedule : allSchedules) {
            // Filter out trains not available on travel date
            if (schedule instanceof TrainSchedule) {
                TrainSchedule train = (TrainSchedule) schedule;
                if (!train.isAvailableOnDate(travelDate)) {
                    continue; // Skip this train
                }
            }
            
            String origin = normalizeCity(schedule.getOrigin());
            String destination = normalizeCity(schedule.getDestination());
            
            // Calculate travel time in minutes
            long travelMinutes = Duration.between(
                schedule.getDepartureTime(), 
                schedule.getArrivalTime()
            ).toMinutes();
            
            RouteEdge edge = new RouteEdge(
                origin,
                destination,
                schedule,
                travelMinutes,
                schedule.getFare()
            );
            
            adjacencyList.computeIfAbsent(origin, k -> new ArrayList<>()).add(edge);
        }
    }
    
    /**
     * Find all possible routes (direct and multi-leg) from origin to destination
     * @param origin Starting city
     * @param destination Target city
     * @param maxLegs Maximum number of legs allowed (1-3)
     * @return List of possible journey plans, sorted by total time
     */
    public List<JourneyPlan> findRoutes(String origin, String destination, int maxLegs) {
        origin = normalizeCity(origin);
        destination = normalizeCity(destination);
        
        List<JourneyPlan> allPlans = new ArrayList<>();
        
        // Find direct routes (1 leg)
        List<RouteEdge> directRoutes = adjacencyList.getOrDefault(origin, Collections.emptyList());
        for (RouteEdge edge : directRoutes) {
            if (edge.destination.equals(destination)) {
                JourneyPlan plan = new JourneyPlan();
                plan.addLeg(edge);
                allPlans.add(plan);
            }
        }
        
        // If maxLegs > 1, find multi-leg routes
        if (maxLegs >= 2) {
            find2LegRoutes(origin, destination, allPlans);
        }
        
        if (maxLegs >= 3) {
            find3LegRoutes(origin, destination, allPlans);
        }
        
        // Sort by total travel time
        allPlans.sort(Comparator.comparingLong(JourneyPlan::getTotalTravelTime));
        
        return allPlans;
    }
    
    /**
     * Find 2-leg routes
     */
    private void find2LegRoutes(String origin, String destination, List<JourneyPlan> plans) {
        List<RouteEdge> firstLegs = adjacencyList.getOrDefault(origin, Collections.emptyList());
        
        for (RouteEdge firstLeg : firstLegs) {
            String transferCity = firstLeg.destination;
            
            // Don't go back to origin or already at destination
            if (transferCity.equals(origin) || transferCity.equals(destination)) {
                continue;
            }
            
            List<RouteEdge> secondLegs = adjacencyList.getOrDefault(transferCity, Collections.emptyList());
            
            for (RouteEdge secondLeg : secondLegs) {
                if (secondLeg.destination.equals(destination)) {
                    // Check if connection is valid (30 min buffer)
                    if (isValidConnection(firstLeg, secondLeg)) {
                        JourneyPlan plan = new JourneyPlan();
                        plan.addLeg(firstLeg);
                        plan.addLeg(secondLeg);
                        plans.add(plan);
                    }
                }
            }
        }
    }
    
    /**
     * Find 3-leg routes
     */
    private void find3LegRoutes(String origin, String destination, List<JourneyPlan> plans) {
        List<RouteEdge> firstLegs = adjacencyList.getOrDefault(origin, Collections.emptyList());
        
        for (RouteEdge firstLeg : firstLegs) {
            String transfer1 = firstLeg.destination;
            if (transfer1.equals(origin) || transfer1.equals(destination)) continue;
            
            List<RouteEdge> secondLegs = adjacencyList.getOrDefault(transfer1, Collections.emptyList());
            
            for (RouteEdge secondLeg : secondLegs) {
                String transfer2 = secondLeg.destination;
                if (transfer2.equals(origin) || transfer2.equals(transfer1) || transfer2.equals(destination)) {
                    continue;
                }
                
                if (!isValidConnection(firstLeg, secondLeg)) continue;
                
                List<RouteEdge> thirdLegs = adjacencyList.getOrDefault(transfer2, Collections.emptyList());
                
                for (RouteEdge thirdLeg : thirdLegs) {
                    if (thirdLeg.destination.equals(destination)) {
                        if (isValidConnection(secondLeg, thirdLeg)) {
                            JourneyPlan plan = new JourneyPlan();
                            plan.addLeg(firstLeg);
                            plan.addLeg(secondLeg);
                            plan.addLeg(thirdLeg);
                            plans.add(plan);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check if two legs can be connected (30 min minimum buffer)
     */
    private boolean isValidConnection(RouteEdge firstLeg, RouteEdge secondLeg) {
        LocalDateTime firstArrival = firstLeg.schedule.getArrivalTime();
        LocalDateTime secondDeparture = secondLeg.schedule.getDepartureTime();
        
        long bufferMinutes = Duration.between(firstArrival, secondDeparture).toMinutes();
        
        // Must have at least 30 minutes buffer and second leg departs after first arrival
        return bufferMinutes >= 30;
    }
    
    /**
     * Normalize city names for consistent matching
     */
    private String normalizeCity(String city) {
        if (city == null) return "";
        return city.trim().toLowerCase()
            .replace("chattogram", "chittagong")
            .replace("dhaka", "dhaka");
    }
    
    /**
     * Inner class representing an edge in the graph (a route)
     */
    public static class RouteEdge {
        public String origin;
        public String destination;
        public Schedule schedule;
        public long travelTimeMinutes;
        public double fare;
        
        public RouteEdge(String origin, String destination, Schedule schedule, 
                        long travelTime, double fare) {
            this.origin = origin;
            this.destination = destination;
            this.schedule = schedule;
            this.travelTimeMinutes = travelTime;
            this.fare = fare;
        }
    }
    
    /**
     * Represents a complete journey plan (one or more legs)
     */
    public static class JourneyPlan {
        private List<RouteEdge> legs;
        
        public JourneyPlan() {
            this.legs = new ArrayList<>();
        }
        
        public void addLeg(RouteEdge leg) {
            legs.add(leg);
        }
        
        public List<RouteEdge> getLegs() {
            return new ArrayList<>(legs);
        }
        
        public int getNumberOfLegs() {
            return legs.size();
        }
        
        public long getTotalTravelTime() {
            if (legs.isEmpty()) return 0;
            
            LocalDateTime start = legs.get(0).schedule.getDepartureTime();
            LocalDateTime end = legs.get(legs.size() - 1).schedule.getArrivalTime();
            
            return Duration.between(start, end).toMinutes();
        }
        
        public double getTotalFare() {
            return legs.stream().mapToDouble(leg -> leg.fare).sum();
        }
        
        public List<Schedule> getSchedules() {
            List<Schedule> schedules = new ArrayList<>();
            for (RouteEdge leg : legs) {
                schedules.add(leg.schedule);
            }
            return schedules;
        }
        
        public List<String> getTransferPoints() {
            List<String> transfers = new ArrayList<>();
            for (int i = 0; i < legs.size() - 1; i++) {
                transfers.add(legs.get(i).destination);
            }
            return transfers;
        }
    }
}
