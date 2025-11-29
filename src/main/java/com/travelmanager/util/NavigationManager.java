package com.travelmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class to manage navigation between views
 */
public class NavigationManager {
    
    private static Stage primaryStage;
    
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    
    public static void navigateTo(String fxmlFile) {
        try {
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
            System.err.println("Error loading view: " + fxmlFile);
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
            System.err.println("Error loading view: " + fxmlFile);
            return null;
        }
    }
    
    public static void goBack() {
        navigateTo("home");
    }
}
