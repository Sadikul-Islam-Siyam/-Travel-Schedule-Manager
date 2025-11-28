package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.service.ScheduleService;
import com.travelmanager.util.AutoCompletePopup;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for editing existing travel plans
 */
public class EditPlanController {

    @FXML private Label planNameLabel;
    @FXML private VBox currentPlanContainer;
    @FXML private ScrollPane currentPlanScrollPane;
    @FXML private Label legCountLabel;
    @FXML private Button addScheduleButton;
    @FXML private Button saveChangesButton;
    @FXML private Button cancelButton;
    @FXML private Label totalFareLabel;
    @FXML private Label totalLegsLabel;
    @FXML private Label totalTimeLabel;
    
    // Add schedule section
    @FXML private TextField startField;
    @FXML private TextField destinationField;
    @FXML private DatePicker datePicker;
    @FXML private RadioButton allRadio;
    @FXML private RadioButton busRadio;
    @FXML private RadioButton trainRadio;
    @FXML private Button searchButton;
    @FXML private ScrollPane searchResultsScrollPane;
    @FXML private VBox searchResultsContainer;

    private String originalPlanName;
    private List<Schedule> schedules;
    private ScheduleService scheduleService;
    private ToggleGroup transportTypeGroup;
    private List<String> allLocations;
    private AutoCompletePopup startAutoComplete;
    private AutoCompletePopup destAutoComplete;

    public void setPlanData(String planName, Route route) {
        this.originalPlanName = planName;
        this.schedules = new ArrayList<>(route.getSchedules());
        displaySchedules();
    }

    @FXML
    public void initialize() {
        scheduleService = new ScheduleService();
        
        // Initialize locations list
        allLocations = Arrays.asList(
            "Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", 
            "Barisal", "Rangpur", "Mymensingh", "Cox's Bazar", 
            "Comilla", "Dinajpur", "Jessore", "Bogra", "Pabna"
        );
        
        // Setup autocomplete for text fields
        startAutoComplete = new AutoCompletePopup(startField, allLocations);
        destAutoComplete = new AutoCompletePopup(destinationField, allLocations);
        
        // Setup transport type radio buttons
        transportTypeGroup = new ToggleGroup();
        allRadio.setToggleGroup(transportTypeGroup);
        busRadio.setToggleGroup(transportTypeGroup);
        trainRadio.setToggleGroup(transportTypeGroup);
        allRadio.setSelected(true);
        
        // Load sample schedules
        loadSampleSchedules();
        
        datePicker.setValue(LocalDate.now());
    }
    
    private void loadSampleSchedules() {
        LocalDateTime now = LocalDateTime.now();
        
        // Sample bus schedules
        scheduleService.addSchedule(new BusSchedule(
            "BUS001", "Dhaka", "Chittagong",
            now.plusHours(2), now.plusHours(7), 
            500.0, 40, "Green Line", "AC"
        ));
        scheduleService.addSchedule(new BusSchedule(
            "BUS002", "Dhaka", "Sylhet",
            now.plusHours(3), now.plusHours(8), 
            450.0, 35, "Shyamoli", "Non-AC"
        ));
        scheduleService.addSchedule(new BusSchedule(
            "BUS003", "Chittagong", "Cox's Bazar",
            now.plusHours(1), now.plusHours(5), 
            350.0, 30, "Soudia", "AC"
        ));
        scheduleService.addSchedule(new BusSchedule(
            "BUS004", "Dhaka", "Rajshahi",
            now.plusHours(4), now.plusHours(10), 
            600.0, 38, "Hanif", "AC"
        ));
        scheduleService.addSchedule(new BusSchedule(
            "BUS005", "Sylhet", "Dhaka",
            now.plusHours(2), now.plusHours(7), 
            450.0, 32, "Ena", "AC"
        ));
        scheduleService.addSchedule(new BusSchedule(
            "BUS006", "Chittagong", "Dhaka",
            now.plusHours(3), now.plusHours(8), 
            500.0, 35, "Shohagh", "AC"
        ));
        
        // Sample train schedules
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN001", "Dhaka", "Chittagong",
            now.plusHours(1), now.plusHours(6), 
            350.0, 100, "Suborno Express", "701", "AC_B"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN002", "Dhaka", "Sylhet",
            now.plusHours(2), now.plusHours(9), 
            400.0, 80, "Parabat Express", "709", "AC_B"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN003", "Dhaka", "Rajshahi",
            now.plusHours(5), now.plusHours(11), 
            450.0, 90, "Silk City", "753", "Shovon"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN004", "Chittagong", "Dhaka",
            now.plusHours(1), now.plusHours(6), 
            350.0, 100, "Turna Express", "742", "AC_B"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN005", "Sylhet", "Dhaka",
            now.plusHours(3), now.plusHours(10), 
            400.0, 85, "Upaban Express", "740", "AC_B"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN006", "Khulna", "Dhaka",
            now.plusHours(4), now.plusHours(12), 
            500.0, 95, "Sundarban Express", "725", "Shovon"
        ));
    }

    private void displaySchedules() {
        currentPlanContainer.getChildren().clear();
        planNameLabel.setText(originalPlanName);
        
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
            
            // Remove button
            Button removeBtn = new Button("✕");
            removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                              "-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 2 6; -fx-background-radius: 3;");
            final int index = i;
            removeBtn.setOnAction(e -> {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Remove Schedule");
                confirmAlert.setHeaderText("Remove this leg from the plan?");
                confirmAlert.setContentText("Leg " + (index + 1));
                
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        schedules.remove(index);
                        displaySchedules();
                    }
                });
            });
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(legLabel, typeLabel, spacer, removeBtn);
            
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
            currentPlanContainer.getChildren().add(card);
        }
        
        // Calculate total travel time
        long totalMinutes = 0;
        for (Schedule s : schedules) {
            totalMinutes += Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
        }
        long totalHours = totalMinutes / 60;
        long totalMins = totalMinutes % 60;
        
        // Update totals
        legCountLabel.setText(schedules.size() + " Leg" + (schedules.size() != 1 ? "s" : ""));
        totalFareLabel.setText("Total Fare: ৳" + String.format("%.2f", totalFare));
        totalLegsLabel.setText("Total Legs: " + schedules.size());
        totalTimeLabel.setText("Total Time: " + totalHours + "h " + totalMins + "m");
    }

    @FXML
    private void handleSearch() {
        String start = startField.getText().trim();
        String dest = destinationField.getText().trim();
        
        if (start.isEmpty() || dest.isEmpty()) {
            showAlert("Please enter both start and destination locations.");
            return;
        }
        
        // Get selected transport type
        String selectedType = null;
        if (busRadio.isSelected()) {
            selectedType = "Bus";
        } else if (trainRadio.isSelected()) {
            selectedType = "Train";
        }
        
        List<Schedule> results = scheduleService.searchSchedules(start, dest);
        
        // Filter by transport type if not "All"
        if (selectedType != null) {
            final String type = selectedType;
            results = results.stream()
                .filter(s -> s.getType().equals(type))
                .toList();
        }
        
        displaySearchResults(results);
        
        if (results.isEmpty()) {
            showAlert("No schedules found for the selected route and date.");
        }
    }
    
    private void displaySearchResults(List<Schedule> results) {
        searchResultsContainer.getChildren().clear();
        
        if (results.isEmpty()) {
            Label emptyLabel = new Label("No schedules found. Try searching again.");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-padding: 20;");
            searchResultsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (Schedule s : results) {
            // Check if already in plan
            boolean alreadyAdded = schedules.contains(s);
            
            // Create card for each schedule
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-color: " + 
                         (alreadyAdded ? "#95a5a6" : "#d0d0d0") + 
                         "; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
            
            if (alreadyAdded) {
                card.setOpacity(0.6);
            }
            
            // Type header
            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label typeLabel = new Label(s.getType());
            typeLabel.setStyle("-fx-background-color: " + 
                              (s instanceof BusSchedule ? "#3498db" : "#e74c3c") + 
                              "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 3; -fx-font-size: 11px;");
            
            header.getChildren().add(typeLabel);
            
            if (alreadyAdded) {
                Label addedLabel = new Label("✓ Already in Plan");
                addedLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px; -fx-font-style: italic;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                header.getChildren().addAll(spacer, addedLabel);
            }
            
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
            
            // Add click handler to add schedule
            if (!alreadyAdded) {
                card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "; -fx-border-color: #3498db; -fx-border-width: 2;"));
                card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("; -fx-border-color: #3498db; -fx-border-width: 2;", "")));
                card.setOnMouseClicked(e -> {
                    schedules.add(s);
                    displaySchedules();
                    displaySearchResults(results);
                });
            }
            
            searchResultsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handleSaveChanges() {
        if (schedules.isEmpty()) {
            showAlert("Cannot save an empty plan. Please add at least one schedule.");
            return;
        }
        
        try {
            // Delete old plan
            DatabaseManager.getInstance().deletePlan(originalPlanName);
            
            // Save updated plan
            Route updatedRoute = new Route();
            for (Schedule s : schedules) {
                updatedRoute.addSchedule(s);
            }
            
            DatabaseManager.getInstance().savePlan(originalPlanName, updatedRoute);
            
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Plan updated successfully!");
            successAlert.showAndWait();
            
            Stage stage = (Stage) saveChangesButton.getScene().getWindow();
            stage.close();
            
        } catch (Exception e) {
            showAlert("Error saving changes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Discard Changes");
        confirmAlert.setHeaderText("Are you sure you want to discard changes?");
        confirmAlert.setContentText("Any modifications will be lost.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Stage stage = (Stage) cancelButton.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
