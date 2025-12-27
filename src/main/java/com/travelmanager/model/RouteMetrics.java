package com.travelmanager.model;

import java.time.Duration;

/**
 * Metrics for computed routes
 */
public class RouteMetrics {
    private double totalFare;
    private Duration totalTime;
    private int numberOfTransfers;
    private Duration longestLayover;
    private double costPerHour;
    
    public RouteMetrics() {
        this.totalFare = 0.0;
        this.totalTime = Duration.ZERO;
        this.numberOfTransfers = 0;
        this.longestLayover = Duration.ZERO;
        this.costPerHour = 0.0;
    }
    
    public void calculate(double fare, Duration time, int transfers, Duration layover) {
        this.totalFare = fare;
        this.totalTime = time;
        this.numberOfTransfers = transfers;
        this.longestLayover = layover;
        this.costPerHour = time.toHours() > 0 ? fare / time.toHours() : 0.0;
    }
    
    // Getters and setters
    public double getTotalFare() { return totalFare; }
    public void setTotalFare(double totalFare) { this.totalFare = totalFare; }
    
    public Duration getTotalTime() { return totalTime; }
    public void setTotalTime(Duration totalTime) { this.totalTime = totalTime; }
    
    public int getNumberOfTransfers() { return numberOfTransfers; }
    public void setNumberOfTransfers(int numberOfTransfers) { this.numberOfTransfers = numberOfTransfers; }
    
    public Duration getLongestLayover() { return longestLayover; }
    public void setLongestLayover(Duration longestLayover) { this.longestLayover = longestLayover; }
    
    public double getCostPerHour() { return costPerHour; }
    public void setCostPerHour(double costPerHour) { this.costPerHour = costPerHour; }
}
