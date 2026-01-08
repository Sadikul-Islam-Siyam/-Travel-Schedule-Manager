package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.AutoCompletePopup;
import com.travelmanager.util.NavigationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddTrainRouteController {
    @FXML private TextField trainNameField;
    @FXML private ComboBox<String> offDayCombo;
    @FXML private TextField startStationField;
    @FXML private TextField startDepartureField;
    @FXML private TextField destStationField;
    @FXML private TextField destArrivalField;
    @FXML private TextField destFareField;
    @FXML private VBox stopsContainer;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;
    
    private DatabaseManager databaseManager;
    private List<StopRowPane> stopRows;
    private List<String> allLocations;
    @SuppressWarnings("unused")
    private AutoCompletePopup startStationAutoComplete;
    @SuppressWarnings("unused")
    private AutoCompletePopup destStationAutoComplete;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        stopRows = new ArrayList<>();
        
        // Initialize locations
        allLocations = Arrays.asList(
            "Barguna", "Barishal", "Bhola", "Jhalokati", "Patuakhali", "Pirojpur",
            "Bandarban", "Brahmanbaria", "Chandpur", "Chattogram", "Chittagong", "Cox's Bazar", "Cumilla", "Feni", "Khagrachari", "Lakshmipur", "Noakhali", "Rangamati",
            "Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur", "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail",
            "Bagerhat", "Chuadanga", "Jashore", "Jhenaidah", "Khulna", "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira",
            "Jamalpur", "Mymensingh", "Netrokona", "Sherpur",
            "Bogura", "Joypurhat", "Naogaon", "Natore", "Chapai Nawabganj", "Pabna", "Rajshahi", "Sirajganj",
            "Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari", "Panchagarh", "Rangpur", "Thakurgaon",
            "Habiganj", "Moulvibazar", "Sunamganj", "Sylhet"
        );
        
        // Setup off day combo
        offDayCombo.getItems().addAll(
            "No off day", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        );
        offDayCombo.getSelectionModel().selectFirst();
        
        // Setup autocomplete for start and destination fields
        startStationAutoComplete = new AutoCompletePopup(startStationField, allLocations);
        destStationAutoComplete = new AutoCompletePopup(destStationField, allLocations);
        
        errorLabel.setText("");
        statusLabel.setText("");
    }
    
    @FXML
    private void handleAddStop() {
        StopRowPane stopRow = new StopRowPane(stopRows.size());
        stopRows.add(stopRow);
        updateStopsDisplay();
    }
    
    private void deleteStop(int index) {
        if (index >= 0 && index < stopRows.size()) {
            stopRows.remove(index);
            updateStopsDisplay();
        }
    }
    
    private void moveStopUp(int index) {
        if (index > 0 && index < stopRows.size()) {
            StopRowPane stop = stopRows.remove(index);
            stopRows.add(index - 1, stop);
            updateStopsDisplay();
        }
    }
    
    private void moveStopDown(int index) {
        if (index >= 0 && index < stopRows.size() - 1) {
            StopRowPane stop = stopRows.remove(index);
            stopRows.add(index + 1, stop);
            updateStopsDisplay();
        }
    }
    
    private void updateStopsDisplay() {
        stopsContainer.getChildren().clear();
        
        for (int i = 0; i < stopRows.size(); i++) {
            StopRowPane row = stopRows.get(i);
            row.setIndex(i);
            row.setIsFirst(i == 0);
            row.setIsLast(i == stopRows.size() - 1);
            stopsContainer.getChildren().add(row.getPane());
        }
    }
    
    @FXML
    private void handleSubmit() {
        errorLabel.setText("");
        
        String trainName = trainNameField.getText().trim();
        String offDay = offDayCombo.getValue();
        String startStation = startStationField.getText().trim();
        String startDeparture = startDepartureField.getText().trim();
        String destStation = destStationField.getText().trim();
        String destArrival = destArrivalField.getText().trim();
        String destFareStr = destFareField.getText().trim();
        
        // Validate mandatory fields
        if (trainName.isEmpty()) {
            errorLabel.setText("⚠ Please enter train name");
            return;
        }
        
        if (startStation.isEmpty()) {
            errorLabel.setText("⚠ Please enter start (origin) station");
            return;
        }
        
        if (startDeparture.isEmpty()) {
            errorLabel.setText("⚠ Please enter departure time from origin");
            return;
        }
        
        if (!isValidTimeFormat(startDeparture)) {
            errorLabel.setText("⚠ Invalid departure time format (use HH:MM)");
            return;
        }
        
        if (destStation.isEmpty()) {
            errorLabel.setText("⚠ Please enter destination station");
            return;
        }
        
        if (destArrival.isEmpty()) {
            errorLabel.setText("⚠ Please enter arrival time at destination");
            return;
        }
        
        if (!isValidTimeFormat(destArrival)) {
            errorLabel.setText("⚠ Invalid arrival time format (use HH:MM)");
            return;
        }
        
        if (destFareStr.isEmpty()) {
            errorLabel.setText("⚠ Please enter fare for full journey");
            return;
        }
        
        double totalFare;
        try {
            totalFare = Double.parseDouble(destFareStr);
            if (totalFare <= 0) {
                errorLabel.setText("⚠ Fare must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("⚠ Invalid fare amount");
            return;
        }
        
        // Validate all intermediate stops
        for (int i = 0; i < stopRows.size(); i++) {
            StopRowPane stop = stopRows.get(i);
            if (stop.getStation().trim().isEmpty()) {
                errorLabel.setText("⚠ Intermediate stop #" + (i + 1) + ": Station name is required");
                return;
            }
            if (stop.getArrivalTime().trim().isEmpty() || stop.getDepartureTime().trim().isEmpty()) {
                errorLabel.setText("⚠ Intermediate stop #" + (i + 1) + ": Arrival and departure times are required");
                return;
            }
            if (!isValidTimeFormat(stop.getArrivalTime()) || !isValidTimeFormat(stop.getDepartureTime())) {
                errorLabel.setText("⚠ Intermediate stop #" + (i + 1) + ": Invalid time format (use HH:MM)");
                return;
            }
            try {
                double fare = Double.parseDouble(stop.getFare());
                if (fare <= 0 || fare >= totalFare) {
                    errorLabel.setText("⚠ Intermediate stop #" + (i + 1) + ": Fare must be between 0 and final fare");
                    return;
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("⚠ Intermediate stop #" + (i + 1) + ": Invalid fare amount");
                return;
            }
        }
        
        // Build stops JSON - Start, Intermediate stops, Destination
        StringBuilder stopsJson = new StringBuilder("[");
        
        // First stop (origin)
        stopsJson.append("{")
            .append("\"station\":\"").append(startStation).append("\",")
            .append("\"arrivalTime\":\"").append(startDeparture).append("\",")
            .append("\"departureTime\":\"").append(startDeparture).append("\",")
            .append("\"cumulativeFare\":0")
            .append("}");
        
        // Intermediate stops
        for (StopRowPane stop : stopRows) {
            stopsJson.append(",{")
                .append("\"station\":\"").append(stop.getStation()).append("\",")
                .append("\"arrivalTime\":\"").append(stop.getArrivalTime()).append("\",")
                .append("\"departureTime\":\"").append(stop.getDepartureTime()).append("\",")
                .append("\"cumulativeFare\":").append(stop.getFare())
                .append("}");
        }
        
        // Last stop (destination)
        stopsJson.append(",{")
            .append("\"station\":\"").append(destStation).append("\",")
            .append("\"arrivalTime\":\"").append(destArrival).append("\",")
            .append("\"departureTime\":\"").append(destArrival).append("\",")
            .append("\"cumulativeFare\":").append(totalFare)
            .append("}]");
        
        String duration = calculateDuration(startDeparture, destArrival);
        int totalStops = 2 + stopRows.size(); // start + intermediate + destination
        
        try {
            String currentUser = AuthenticationManager.getInstance().getCurrentUsername();
            
            // Submit with stops data in metadata
            boolean submitted = databaseManager.submitPendingRoute(
                trainName,
                startStation,
                destStation,
                "TRAIN",
                0,
                totalFare,
                startDeparture + "-" + destArrival,
                "stops:" + stopsJson.toString() + ";offDay:" + offDay + ";duration:" + duration,
                "ADD",
                null,
                currentUser,
                "New train route with " + totalStops + " stops (including " + stopRows.size() + " intermediate)"
            );
            
            if (submitted) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("Train Route Submitted");
                success.setContentText("Your train route has been submitted for master approval.\n\n" +
                                      "Train: " + trainName + "\n" +
                                      "Route: " + startStation + " → " + destStation + "\n" +
                                      "Total Stops: " + totalStops + " (" + stopRows.size() + " intermediate)\n" +
                                      "Total Fare: ৳" + totalFare);
                success.showAndWait();
                
                NavigationManager.navigateTo("manage-routes");
            } else {
                errorLabel.setText("⚠ Failed to submit route. Please try again.");
            }
        } catch (Exception e) {
            errorLabel.setText("⚠ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.navigateTo("add-route-selection");
    }
    
    private boolean isValidTimeFormat(String time) {
        return time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }
    
    private String calculateDuration(String startTime, String endTime) {
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            
            int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
            
            int durationMinutes = endMinutes - startMinutes;
            if (durationMinutes < 0) {
                durationMinutes += 24 * 60;
            }
            
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            
            return String.format("%d:%02dh", hours, minutes);
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    // Inner class for stop row UI
    private class StopRowPane {
        private HBox pane;
        private TextField stationField;
        private TextField arrivalField;
        private TextField departureField;
        private TextField fareField;
        private Label numberLabel;
        private VBox connectLine;
        private int index;
        @SuppressWarnings("unused")
        private AutoCompletePopup stationAutoComplete;
        
        public StopRowPane(int initialIndex) {
            this.index = initialIndex;
            buildPane();
        }
        
        private void buildPane() {
            pane = new HBox(12);
            pane.setAlignment(Pos.CENTER_LEFT);
            pane.setStyle("-fx-padding: 10 0;");
            
            // Left side: Number circle + connect line
            VBox leftSide = new VBox(0);
            leftSide.setAlignment(Pos.TOP_CENTER);
            leftSide.setMinWidth(30);
            leftSide.setMaxWidth(30);
            
            // Numbered circle
            Circle circle = new Circle(15);
            circle.setStyle("-fx-fill: #3498db; -fx-stroke: #2980b9; -fx-stroke-width: 2;");
            numberLabel = new Label(String.valueOf(index + 1));
            numberLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
            
            Region circleContainer = new Region();
            circleContainer.setMinSize(30, 30);
            circleContainer.setMaxSize(30, 30);
            circleContainer.setStyle("-fx-background-color: #3498db; -fx-background-radius: 15; -fx-border-color: #2980b9; -fx-border-width: 2; -fx-border-radius: 15;");
            
            HBox circleBox = new HBox(numberLabel);
            circleBox.setAlignment(Pos.CENTER);
            circleBox.setMinSize(30, 30);
            circleBox.setMaxSize(30, 30);
            circleBox.setStyle("-fx-background-color: #3498db; -fx-background-radius: 15; -fx-border-color: #2980b9; -fx-border-width: 2; -fx-border-radius: 15;");
            
            // Connect line (will be hidden for last element)
            connectLine = new VBox();
            connectLine.setMinWidth(2);
            connectLine.setMaxWidth(2);
            connectLine.setPrefHeight(40);
            connectLine.setStyle("-fx-background-color: #bdc3c7;");
            VBox.setMargin(connectLine, new Insets(2, 0, 0, 0));
            
            leftSide.getChildren().addAll(circleBox, connectLine);
            
            // Main content area
            HBox contentBox = new HBox(10);
            contentBox.setAlignment(Pos.CENTER_LEFT);
            contentBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-border-width: 1;");
            HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);
            
            // Drag handle
            Label dragHandle = new Label("☰");
            dragHandle.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6; -fx-cursor: move;");
            
            // Location icon + station field
            Label locationIcon = new Label("📍");
            locationIcon.setStyle("-fx-font-size: 14px;");
            
            stationField = new TextField();
            stationField.setPromptText("Station name");
            stationField.setPrefWidth(180);
            stationField.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8; -fx-font-size: 13px;");
            
            // Time fields
            Label dashLabel1 = new Label("--:--");
            dashLabel1.setStyle("-fx-text-fill: #bbb; -fx-font-size: 13px;");
            
            arrivalField = new TextField();
            arrivalField.setPromptText("07:00");
            arrivalField.setPrefWidth(70);
            arrivalField.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8; -fx-font-size: 13px; -fx-alignment: center;");
            
            departureField = new TextField();
            departureField.setPromptText("07:00");
            departureField.setPrefWidth(70);
            departureField.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8; -fx-font-size: 13px; -fx-alignment: center;");
            
            // Fare field
            Label rupeeLabel = new Label("৳");
            rupeeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            
            fareField = new TextField("0");
            fareField.setPrefWidth(80);
            fareField.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8; -fx-font-size: 13px; -fx-alignment: center;");
            
            // Delete button
            Button deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color: #fee; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 6 12; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #fcc;");
            deleteBtn.setOnAction(e -> deleteStop(index));
            
            contentBox.getChildren().addAll(
                dragHandle, locationIcon, stationField,
                dashLabel1, arrivalField, departureField,
                rupeeLabel, fareField, deleteBtn
            );
            
            pane.getChildren().addAll(leftSide, contentBox);
            
            // Setup autocomplete for station field
            stationAutoComplete = new AutoCompletePopup(stationField, allLocations);
        }
        
        public void setIndex(int newIndex) {
            this.index = newIndex;
            numberLabel.setText(String.valueOf(newIndex + 1));
        }
        
        public void setIsFirst(boolean isFirst) {
            // First stop logic if needed
        }
        
        public void setIsLast(boolean isLast) {
            connectLine.setVisible(!isLast);
            connectLine.setManaged(!isLast);
        }
        
        public HBox getPane() {
            return pane;
        }
        
        public String getStation() {
            return stationField.getText();
        }
        
        public String getArrivalTime() {
            return arrivalField.getText();
        }
        
        public String getDepartureTime() {
            return departureField.getText();
        }
        
        public String getFare() {
            return fareField.getText();
        }
    }
}
