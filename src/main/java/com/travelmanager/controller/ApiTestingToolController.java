package com.travelmanager.controller;

import com.travelmanager.api.ScheduleApiManager;
import com.travelmanager.model.Schedule;
import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ApiTestingToolController {
    
    @FXML private TextField originField;
    @FXML private TextField destinationField;
    @FXML private ComboBox<String> transportTypeCombo;
    @FXML private VBox resultsContainer;
    @FXML private Label resultCountLabel;
    @FXML private Label emptyLabel;
    
    private ScheduleApiManager apiManager;
    
    @FXML
    public void initialize() {
        apiManager = ScheduleApiManager.getInstance();
        transportTypeCombo.setValue("ALL");
        emptyLabel.setVisible(true);
    }
    
    @FXML
    private void handleSearchRoutes() {
        String origin = originField.getText().trim();
        String destination = destinationField.getText().trim();
        String transportType = transportTypeCombo.getValue();
        
        if (origin.isEmpty() || destination.isEmpty()) {
            showAlert("Please enter both origin and destination");
            return;
        }
        
        List<Schedule> results;
        if ("ALL".equals(transportType)) {
            results = apiManager.searchSchedules(origin, destination);
        } else {
            results = apiManager.searchSchedulesByType(origin, destination, transportType);
        }
        
        displayResults(results);
    }
    
    private void displayResults(List<Schedule> results) {
        resultsContainer.getChildren().clear();
        
        if (results.isEmpty()) {
            emptyLabel.setText("No routes found for the specified criteria.");
            emptyLabel.setVisible(true);
            resultCountLabel.setText("0 routes found");
        } else {
            emptyLabel.setVisible(false);
            resultCountLabel.setText(results.size() + " route(s) found");
            
            for (Schedule schedule : results) {
                resultsContainer.getChildren().add(createResultCard(schedule));
            }
        }
    }
    
    private VBox createResultCard(Schedule schedule) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; " +
                     "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header with route info
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label routeLabel = new Label(schedule.getOrigin() + " → " + schedule.getDestination());
        routeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label typeLabel = new Label(schedule.getTransportType());
        String typeColor = schedule.getTransportType().equals("BUS") ? "#3498db" : "#e67e22";
        typeLabel.setStyle("-fx-background-color: " + typeColor + "; -fx-text-fill: white; " +
                          "-fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 11px;");
        
        headerBox.getChildren().addAll(routeLabel, spacer, typeLabel);
        
        // Details
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Label timeLabel = new Label("🕐 " + schedule.getDepartureTime().format(formatter) + 
                                   " - " + schedule.getArrivalTime().format(formatter));
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        
        Label fareLabel = new Label("💰 Fare: ৳" + String.format("%.2f", schedule.getFare()));
        fareLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        card.getChildren().addAll(headerBox, timeLabel, fareLabel);
        
        return card;
    }
    
    @FXML
    private void handleClear() {
        originField.clear();
        destinationField.clear();
        transportTypeCombo.setValue("ALL");
        resultsContainer.getChildren().clear();
        emptyLabel.setText("Enter search criteria and click 'Search Routes' to test the API.");
        emptyLabel.setVisible(true);
        resultCountLabel.setText("0 routes found");
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.getInstance().navigateToHome();
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Required");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
