package com.travelmanager.controller;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for viewing plan details in a separate window
 */
public class ViewPlanDetailsController {

    @FXML private Label planTitleLabel;
    @FXML private Label planCreatedLabel;
    @FXML private Label totalLegsLabel;
    @FXML private Label totalFareLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label totalDurationLabel;
    @FXML private VBox planDetailsContainer;
    @FXML private ScrollPane planDetailsScrollPane;
    @FXML private VBox notesSection;
    @FXML private Label notesLabel;

    public void setPlanData(String planName, String createdDate, String notes, Route route) {
        planTitleLabel.setText("📋 " + planName);
        planCreatedLabel.setText("Created: " + createdDate);
        
        // Display notes if present
        if (notes != null && !notes.trim().isEmpty()) {
            notesLabel.setText(notes);
            notesSection.setVisible(true);
            notesSection.setManaged(true);
        } else {
            notesSection.setVisible(false);
            notesSection.setManaged(false);
        }
        
        displayPlanDetails(route);
    }

    private void displayPlanDetails(Route route) {
        planDetailsContainer.getChildren().clear();
        
        List<Schedule> schedules = route.getSchedules();
        double totalFare = 0;
        long totalMinutes = 0;
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 0; i < schedules.size(); i++) {
            Schedule s = schedules.get(i);
            totalFare += s.getFare();
            totalMinutes += Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
            
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
            planDetailsContainer.getChildren().add(card);
        }
        
        // Update totals
        long totalHours = totalMinutes / 60;
        long totalMins = totalMinutes % 60;
        
        // Calculate total journey duration
        LocalDateTime firstDeparture = schedules.get(0).getDepartureTime();
        LocalDateTime lastArrival = schedules.get(schedules.size() - 1).getArrivalTime();
        long journeyMinutes = Duration.between(firstDeparture, lastArrival).toMinutes();
        long journeyHours = journeyMinutes / 60;
        long journeyMins = journeyMinutes % 60;
        
        totalLegsLabel.setText(schedules.size() + " Leg" + (schedules.size() != 1 ? "s" : ""));
        totalFareLabel.setText("৳" + String.format("%.2f", totalFare));
        totalTimeLabel.setText("Travel: " + totalHours + "h " + totalMins + "m");
        totalDurationLabel.setText("Duration: " + journeyHours + "h " + journeyMins + "m");
    }

    @FXML
    private void handleClose() {
        com.travelmanager.util.NavigationManager.navigateTo("saved-plans");
    }
    
    @FXML
    private void handleBack() {
        com.travelmanager.util.NavigationManager.navigateTo("saved-plans");
    }
}
