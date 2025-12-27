package com.travelmanager.util;

/**
 * Application-wide constants
 */
public class Constants {
    
    // Timing constraints
    public static final int MIN_CONNECTION_TIME_MINUTES = 30;
    public static final int MAX_LAYOVER_HOURS = 12;
    public static final int MAX_JOURNEY_HOURS = 48;
    
    // Validation limits
    public static final double MAX_FARE = 10000.0;
    public static final int MAX_SEATS = 500;
    public static final double MIN_FARE = 1.0;
    
    // Cache configuration
    public static final int CACHE_TTL_SECONDS = 3600;
    public static final String CACHE_SCHEDULE_PREFIX = "schedule_";
    public static final String CACHE_ROUTE_PREFIX = "route_";
    
    // File paths
    public static final String SCHEDULE_DATA_FILE = "schedules-data.json";
    public static final String DATABASE_URL = "jdbc:sqlite:data/travel_plans.db";
    
    // UI Configuration
    public static final String APP_TITLE = "Smart Multi-Modal Travel Schedule Manager";
    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 600;
    public static final int MIN_WINDOW_WIDTH = 600;
    public static final int MIN_WINDOW_HEIGHT = 400;
    
    // Route optimization
    public static final int DEFAULT_MAX_ROUTES = 10;
    public static final int MAX_ROUTE_HOPS = 5;
    
    // Prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
