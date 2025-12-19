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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    @FXML private Label totalDurationLabel;
    @FXML private TextField planNameField;
    @FXML private TextArea notesArea;
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
            
            // Check for tight connection (if not the last leg)
            if (i < schedules.size() - 1) {
                Schedule next = schedules.get(i + 1);
                long connectionMinutes = Duration.between(s.getArrivalTime(), next.getDepartureTime()).toMinutes();
                
                if (connectionMinutes < 30 && connectionMinutes >= 0) {
                    Label warningLabel = new Label("⚠ Tight connection: " + connectionMinutes + " min");
                    warningLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                                         "-fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");
                    bottomBox.getChildren().add(warningLabel);
                }
            }
            
            card.getChildren().addAll(header, nameLabel, routeBox, timeBox, bottomBox);
            planSummaryContainer.getChildren().add(card);
        }
        
        // Calculate total travel time (actual time spent traveling)
        long totalMinutes = 0;
        for (Schedule s : schedules) {
            totalMinutes += Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
        }
        long totalHours = totalMinutes / 60;
        long totalMins = totalMinutes % 60;
        
        // Calculate total journey duration (from first departure to last arrival, including waits)
        LocalDateTime firstDeparture = schedules.get(0).getDepartureTime();
        LocalDateTime lastArrival = schedules.get(schedules.size() - 1).getArrivalTime();
        long journeyMinutes = Duration.between(firstDeparture, lastArrival).toMinutes();
        long journeyHours = journeyMinutes / 60;
        long journeyMins = journeyMinutes % 60;
        
        totalFareLabel.setText("Total Fare: ৳" + String.format("%.2f", totalFare));
        totalLegsLabel.setText("Total Legs: " + schedules.size());
        totalTimeLabel.setText("Travel Time: " + totalHours + "h " + totalMins + "m");
        totalDurationLabel.setText("Total Duration: " + journeyHours + "h " + journeyMins + "m");
    }

    @FXML
    private void handleSavePlan() {
        String planName = planNameField.getText().trim();
        
        if (planName.isEmpty()) {
            showAlert("Please enter a name for your plan.");
            return;
        }
        
        // Validate plan feasibility
        String validationError = validatePlanFeasibility();
        if (validationError != null) {
            showAlert(validationError);
            return;
        }
        
        try {
            Route route = new Route();
            for (Schedule s : schedules) {
                route.addSchedule(s);
            }
            
            // Save to database
            String notes = notesArea.getText().trim();
            DatabaseManager.getInstance().savePlan(planName, route, notes.isEmpty() ? null : notes);
            
            planSaved = true;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Plan '" + planName + "' saved successfully!");
            alert.showAndWait();
            
            // Go back to home
            com.travelmanager.util.NavigationManager.goBack();
            
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
        CreatePlanController controller = com.travelmanager.util.NavigationManager.navigateToWithController(
            "create-plan", CreatePlanController.class);
        if (controller != null && schedules != null) {
            controller.setSchedules(new ArrayList<>(schedules));
        }
    }
    
    @FXML
    private void handleBack() {
        CreatePlanController controller = com.travelmanager.util.NavigationManager.navigateToWithController(
            "create-plan", CreatePlanController.class);
        if (controller != null && schedules != null) {
            controller.setSchedules(new ArrayList<>(schedules));
        }
    }

    public boolean isPlanSaved() {
        return planSaved;
    }

    private String validatePlanFeasibility() {
        if (schedules == null || schedules.isEmpty()) {
            return "No schedules to validate.";
        }
        
        // Check that each leg connects properly (arrival time < next departure time)
        for (int i = 0; i < schedules.size() - 1; i++) {
            Schedule current = schedules.get(i);
            Schedule next = schedules.get(i + 1);
            
            // Check if current destination matches next origin
            if (!current.getDestination().equalsIgnoreCase(next.getOrigin())) {
                return "Invalid connection: Leg " + (i + 1) + " ends at " + current.getDestination() + 
                       " but Leg " + (i + 2) + " starts at " + next.getOrigin() + ".\n" +
                       "Each leg must start where the previous leg ended.";
            }
            
            // Check if arrival time is before next departure time
            if (current.getArrivalTime().isAfter(next.getDepartureTime())) {
                return "Invalid timing: Leg " + (i + 1) + " arrives at " + 
                       current.getArrivalTime().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) +
                       " but Leg " + (i + 2) + " departs at " +
                       next.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ".\n" +
                       "Each leg must depart after the previous leg arrives.";
            }
        }
        
        return null; // No errors
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
