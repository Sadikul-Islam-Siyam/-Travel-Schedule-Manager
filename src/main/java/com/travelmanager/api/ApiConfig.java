package com.travelmanager.api;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration manager for API settings
 */
public class ApiConfig {
    
    private static final String CONFIG_FILE = "api-config.properties";
    private static Properties properties;
    
    static {
        properties = new Properties();
        loadConfig();
    }
    
    /**
     * Load configuration from file
     */
    private static void loadConfig() {
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            // File doesn't exist, use defaults
            setDefaults();
        }
    }
    
    /**
     * Set default configuration values
     */
    private static void setDefaults() {
        // Bus API Configuration
        properties.setProperty("bus.api.enabled", "true");
        properties.setProperty("bus.api.endpoint", "https://api.example.com/bus/schedules");
        properties.setProperty("bus.api.key", "");
        
        // Train API Configuration  
        properties.setProperty("train.api.enabled", "true");
        properties.setProperty("train.api.endpoint", "https://api.example.com/train/schedules");
        properties.setProperty("train.api.key", "");
        
        // Data source configuration
        properties.setProperty("use.mock.data", "true");
        properties.setProperty("use.manual.data", "false");
        
        saveConfig();
    }
    
    /**
     * Save configuration to file
     */
    public static void saveConfig() {
        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Travel Schedule Manager API Configuration");
        } catch (IOException e) {
            System.err.println("Error saving API configuration: " + e.getMessage());
        }
    }
    
    /**
     * Get a configuration property
     * @param key Property key
     * @return Property value or null if not found
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get a configuration property with default value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default value
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Set a configuration property
     * @param key Property key
     * @param value Property value
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Check if mock data should be used
     * @return true if mock data is enabled
     */
    public static boolean useMockData() {
        return Boolean.parseBoolean(getProperty("use.mock.data", "true"));
    }
    
    /**
     * Check if manual data should be used
     * @return true if manual data is enabled
     */
    public static boolean useManualData() {
        return Boolean.parseBoolean(getProperty("use.manual.data", "false"));
    }
    
    /**
     * Check if bus API is enabled
     * @return true if bus API is enabled
     */
    public static boolean isBusApiEnabled() {
        return Boolean.parseBoolean(getProperty("bus.api.enabled", "true"));
    }
    
    /**
     * Check if train API is enabled
     * @return true if train API is enabled
     */
    public static boolean isTrainApiEnabled() {
        return Boolean.parseBoolean(getProperty("train.api.enabled", "true"));
    }
    
    /**
     * Get bus API endpoint
     * @return Bus API endpoint URL
     */
    public static String getBusApiEndpoint() {
        return getProperty("bus.api.endpoint", "");
    }
    
    /**
     * Get train API endpoint
     * @return Train API endpoint URL
     */
    public static String getTrainApiEndpoint() {
        return getProperty("train.api.endpoint", "");
    }
    
    /**
     * Get bus API key
     * @return Bus API key
     */
    public static String getBusApiKey() {
        return getProperty("bus.api.key", "");
    }
    
    /**
     * Get train API key
     * @return Train API key
     */
    public static String getTrainApiKey() {
        return getProperty("train.api.key", "");
    }
}
