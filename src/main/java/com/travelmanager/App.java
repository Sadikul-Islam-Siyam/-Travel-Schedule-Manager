package com.travelmanager;

import com.travelmanager.api.rest.RestApiServer;
import com.travelmanager.util.NavigationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the Travel Schedule Manager
 */
public class App extends Application {

    private static Scene scene;
    private RestApiServer apiServer;

    @Override
    public void start(Stage stage) throws IOException {
        // Start the embedded REST API server
        startRestApiServer();
        
        NavigationManager.setPrimaryStage(stage);
        scene = new Scene(loadFXML("login"), 900, 600);
        stage.setTitle("Travel Schedule Manager");
        
        // Set application icon
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(getClass().getResourceAsStream("/App_icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Warning: Failed to load application icon");
            e.printStackTrace();
        }
        
        stage.setScene(scene);
        
        // Set minimum window size for responsiveness
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        
        stage.setMaximized(true);
        
        // Ensure API server stops when JavaFX app closes
        stage.setOnCloseRequest(event -> {
            stopRestApiServer();
        });
        
        stage.show();
    }
    
    /**
     * Start the embedded REST API server
     */
    private void startRestApiServer() {
        try {
            apiServer = RestApiServer.getInstance(8080);
            apiServer.start();
        } catch (Exception e) {
            System.err.println("Warning: Failed to start REST API server");
            e.printStackTrace();
            // Continue with JavaFX app even if API server fails
        }
    }
    
    /**
     * Stop the embedded REST API server
     */
    private void stopRestApiServer() {
        if (apiServer != null && apiServer.isRunning()) {
            apiServer.stop();
        }
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
