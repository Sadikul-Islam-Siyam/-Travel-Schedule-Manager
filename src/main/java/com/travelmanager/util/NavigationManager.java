package com.travelmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to manage navigation between views with global Help support
 * Tracks navigation history to allow returning to the previous page from Help
 */
public class NavigationManager {
    
    private static Stage primaryStage;
    private static NavigationManager instance;
    private static String previousPage = "login"; // Previous page for back navigation
    private static String currentPage = "login"; // Current page being displayed
    private static Map<String, Object> navigationContext = new HashMap<>();
    
    private NavigationManager() {
    }
    
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    
    /**
     * Store context data for navigation
     * @param key The context key
     * @param value The context value
     */
    public static void setContext(String key, Object value) {
        navigationContext.put(key, value);
    }
    
    /**
     * Retrieve context data
     * @param key The context key
     * @return The context value or null if not found
     */
    public static Object getContext(String key) {
        return navigationContext.get(key);
    }
    
    /**
     * Clear a specific context entry
     * @param key The context key to clear
     */
    public static void clearContext(String key) {
        navigationContext.remove(key);
    }
    
    /**
     * Clear all context data
     */
    public static void clearAllContext() {
        navigationContext.clear();
    }
    
    public void navigateToHome() {
        navigateTo("home");
    }
    
    /**
     * Navigate to a page and track navigation history
     * @param fxmlFile The FXML file name (without .fxml extension)
     */
    public static void navigateTo(String fxmlFile) {
        navigateTo(fxmlFile, true);
    }
    
    /**
     * Navigate to a page with optional history tracking
     * @param fxmlFile The FXML file name (without .fxml extension)
     * @param trackHistory Whether to track this navigation for back button
     */
    public static void navigateTo(String fxmlFile, boolean trackHistory) {
        try {
            System.out.println("[Navigation] From: " + currentPage + " -> To: " + fxmlFile);
            
            // Reset session timeout on user activity
            AuthenticationManager.getInstance().resetSessionTimeout();
            
            // Store current page as previous when navigating to help
            if (trackHistory && fxmlFile.equals("help")) {
                previousPage = currentPage;
                System.out.println("[Navigation] Stored previous page: " + previousPage);
            }
            // Update current page when NOT navigating to help
            else if (trackHistory && !fxmlFile.equals("help")) {
                previousPage = currentPage;
                currentPage = fxmlFile;
                System.out.println("[Navigation] Updated current page: " + currentPage);
            }
            
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource("/fxml/" + fxmlFile + ".fxml"));
            Parent root = loader.load();
            
            if (primaryStage != null && primaryStage.getScene() != null) {
                primaryStage.getScene().setRoot(root);
            } else if (primaryStage != null) {
                Scene scene = new Scene(root, 1000, 700);
                primaryStage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[Navigation ERROR] Failed to load view: " + fxmlFile);
        }
    }
    
    public static <T> T navigateToWithController(String fxmlFile, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource("/fxml/" + fxmlFile + ".fxml"));
            Parent root = loader.load();
            T controller = loader.getController();
            
            if (primaryStage != null && primaryStage.getScene() != null) {
                primaryStage.getScene().setRoot(root);
            } else if (primaryStage != null) {
                Scene scene = new Scene(root, 1000, 700);
                primaryStage.setScene(scene);
            }
            
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[Navigation ERROR] Failed to load view with controller: " + fxmlFile);
            return null;
        }
    }
    
    /**
     * Navigate back to the previous page (used by Help page Back button)
     */
    public static void goBack() {
        System.out.println("[Navigation] Going back to: " + previousPage);
        navigateTo(previousPage, false);
        // Restore the current page after going back
        currentPage = previousPage;
    }
    
    public static String getPreviousPage() {
        return previousPage;
    }
    
    public static String getCurrentPage() {
        return currentPage;
    }
}
