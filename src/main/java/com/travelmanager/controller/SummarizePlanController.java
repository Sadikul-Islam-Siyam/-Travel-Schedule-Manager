package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for summarizing and saving travel plans
 */
public class SummarizePlanController {

    @FXML private VBox planSummaryContainer;
    @FXML private ScrollPane planSummaryScrollPane;
    @FXML private Label totalFareLabel;
    @FXML private Label totalLegsLabel;
    @FXML private Label totalTimeLabel;
    @FXML private TextField planNameField;
    @FXML private Button savePlanButton;
    @FXML private Button cancelButton;

    private List<Schedule> schedules;
    private boolean planSaved = false;

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
        displaySummary();
    }

    @FXML
    public void initialize() {
        // Initialize if needed
    }

    private void displaySummary() {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        planSummaryContainer.getChildren().clear();
        double totalFare = 0;
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 0; i < schedules.size(); i++) {
            Schedule s = schedules.get(i);
            totalFare += s.getFare();
            
            // Create card for each schedule
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-color: #d0d0d0; " +
                         "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
            
            // Leg number and type header
            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label legLabel = new Label("Leg " + (i + 1));
            legLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
            
            Label typeLabel = new Label(s.getType());
            typeLabel.setStyle("-fx-background-color: " + 
                              (s instanceof BusSchedule ? "#3498db" : "#e74c3c") + 
                              "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 3; -fx-font-size: 11px;");
            
            header.getChildren().addAll(legLabel, typeLabel);
            
            // Transport name/company
            String transportName = "";
            if (s instanceof BusSchedule) {
                BusSchedule bus = (BusSchedule) s;
                transportName = bus.getBusCompany() + " (" + bus.getBusType() + ")";
            } else if (s instanceof TrainSchedule) {
                TrainSchedule train = (TrainSchedule) s;
                transportName = train.getTrainName() + " (" + train.getTrainNumber() + ")";
            }
            Label nameLabel = new Label(transportName);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #34495e;");
            nameLabel.setWrapText(true);
            
            // Route
            HBox routeBox = new HBox(8);
            routeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label originLabel = new Label(s.getOrigin());
            originLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            Label arrowLabel = new Label("→");
            arrowLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");
            Label destLabel = new Label(s.getDestination());
            destLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            routeBox.getChildren().addAll(originLabel, arrowLabel, destLabel);
            
            // Time info
            VBox timeBox = new VBox(3);
            Label departLabel = new Label("Depart: " + s.getDepartureTime().format(dateFormatter) + " " + 
                                         s.getDepartureTime().format(timeFormatter));
            departLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            Label arriveLabel = new Label("Arrive: " + s.getArrivalTime().format(dateFormatter) + " " + 
                                         s.getArrivalTime().format(timeFormatter));
            arriveLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            timeBox.getChildren().addAll(departLabel, arriveLabel);
            
            // Calculate duration
            long minutes = Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
            long hours = minutes / 60;
            long mins = minutes % 60;
            String durationStr = hours + "h " + mins + "m";
            
            // Fare and duration
            HBox bottomBox = new HBox(15);
            bottomBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label fareLabel = new Label("৳" + String.format("%.2f", s.getFare()));
            fareLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #27ae60;");
            Label durationLabel = new Label("⏱ " + durationStr);
            durationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            bottomBox.getChildren().addAll(fareLabel, durationLabel);
            
            card.getChildren().addAll(header, nameLabel, routeBox, timeBox, bottomBox);
            planSummaryContainer.getChildren().add(card);
        }
        
        // Calculate total travel time
        long totalMinutes = 0;
        for (Schedule s : schedules) {
            totalMinutes += Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
        }
        long totalHours = totalMinutes / 60;
        long totalMins = totalMinutes % 60;
        
        totalFareLabel.setText("Total Fare: ৳" + String.format("%.2f", totalFare));
        totalLegsLabel.setText("Total Legs: " + schedules.size());
        totalTimeLabel.setText("Total Time: " + totalHours + "h " + totalMins + "m");
    }

    @FXML
    private void handleSavePlan() {
        String planName = planNameField.getText().trim();
        
        if (planName.isEmpty()) {
            showAlert("Please enter a name for your plan.");
            return;
        }
        
        try {
            Route route = new Route();
            for (Schedule s : schedules) {
                route.addSchedule(s);
            }
            
            // Save to database
            DatabaseManager.getInstance().savePlan(planName, route);
            
            planSaved = true;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Plan '" + planName + "' saved successfully!");
            alert.showAndWait();
            
            // Close window
            Stage stage = (Stage) savePlanButton.getScene().getWindow();
            stage.close();
            
        } catch (Exception e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                showAlert("A plan with this name already exists. Please choose a different name.");
            } else {
                showAlert("Error saving plan: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public boolean isPlanSaved() {
        return planSaved;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
