package com.travelmanager.controller;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.service.ScheduleService;
import com.travelmanager.util.AutoCompletePopup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
import java.util.stream.Collectors;

/**
 * Controller for creating a new travel plan
 */
public class CreatePlanController {

    @FXML private TextField startField;
    @FXML private TextField destinationField;
    @FXML private DatePicker datePicker;
    @FXML private RadioButton busRadio;
    @FXML private RadioButton trainRadio;
    @FXML private RadioButton allRadio;
    @FXML private VBox scheduleContainer;
    @FXML private ScrollPane scheduleScrollPane;
    @FXML private Label searchResultCountLabel;
    
    @FXML private VBox planContainer;
    @FXML private ScrollPane planScrollPane;
    @FXML private Label legCountLabel;
    @FXML private Label totalFareLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Button searchButton;
    @FXML private Button summarizeButton;

    private ScheduleService scheduleService;
    private List<Schedule> currentPlan;
    private List<String> allLocations;
    private Schedule selectedSchedule;
    private AutoCompletePopup startAutoComplete;
    private AutoCompletePopup destAutoComplete;
    private ToggleGroup transportTypeGroup;
    private List<Schedule> lastSearchResults;

    @FXML
    public void initialize() {
        scheduleService = new ScheduleService();
        currentPlan = new ArrayList<>();
        lastSearchResults = new ArrayList<>();
        
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
        
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Initial button state
        summarizeButton.setDisable(true);
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
            "BUS005", "Dhaka", "Khulna",
            now.plusHours(2), now.plusHours(8), 
            550.0, 35, "Eagle Paribahan", "AC"
        ));
        scheduleService.addSchedule(new BusSchedule(
            "BUS006", "Chittagong", "Dhaka",
            now.plusHours(3), now.plusHours(8), 
            500.0, 40, "Shyamoli", "AC"
        ));
        
        // Sample train schedules
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN001", "Dhaka", "Chittagong",
            now.plusHours(1), now.plusHours(6), 
            600.0, 200, "Subarna Express", "701", "First Class"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN002", "Dhaka", "Sylhet",
            now.plusHours(2), now.plusHours(7), 
            550.0, 180, "Parabat Express", "709", "First Class"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN003", "Dhaka", "Rajshahi",
            now.plusHours(5), now.plusHours(12), 
            700.0, 150, "Silk City Express", "751", "Shovon"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN004", "Chittagong", "Sylhet",
            now.plusHours(3), now.plusHours(9), 
            500.0, 160, "Udayan Express", "753", "Shovon"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN005", "Dhaka", "Khulna",
            now.plusHours(6), now.plusHours(14), 
            650.0, 140, "Sundarban Express", "725", "First Class"
        ));
        scheduleService.addSchedule(new TrainSchedule(
            "TRAIN006", "Chittagong", "Dhaka",
            now.plusHours(4), now.plusHours(9), 
            600.0, 200, "Turna Nishitha", "721", "AC"
        ));
    }

    @FXML
    private void handleSearch() {
        String start = startField.getText().trim();
        String destination = destinationField.getText().trim();
        
        if (start.isEmpty() || destination.isEmpty()) {
            showAlert("Please enter both start and destination locations.");
            return;
        }
        
        List<Schedule> results = scheduleService.searchSchedules(start, destination);
        
        // Filter by transport type
        RadioButton selectedRadio = (RadioButton) transportTypeGroup.getSelectedToggle();
        if (selectedRadio == busRadio) {
            results = results.stream()
                .filter(s -> s instanceof BusSchedule)
                .collect(Collectors.toList());
        } else if (selectedRadio == trainRadio) {
            results = results.stream()
                .filter(s -> s instanceof TrainSchedule)
                .collect(Collectors.toList());
        }
        // If allRadio is selected, no filtering needed
        
        // Sort by departure time
        results.sort((s1, s2) -> s1.getDepartureTime().compareTo(s2.getDepartureTime()));
        
        lastSearchResults = new ArrayList<>(results);
        displaySearchResults(results);
        
        if (results.isEmpty()) {
            showAlert("No schedules found for the given route and transport type.");
        }
    }



    @FXML
    private void handleSummarize() {
        if (currentPlan.isEmpty()) {
            showAlert("No schedules in the plan to summarize.");
            return;
        }
        
        try {
            // Open summarize window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/summarize-plan.fxml"));
            Scene scene = new Scene(loader.load(), 800, 700);
            
            // Pass schedules to summarize controller
            SummarizePlanController controller = loader.getController();
            controller.setSchedules(new ArrayList<>(currentPlan));
            
            Stage stage = new Stage();
            stage.setTitle("Summarize and Save Plan");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setMaximized(true);
            stage.showAndWait();
            
            // Check if plan was saved, then close create plan window
            // The summarize controller will indicate if save was successful
            if (controller.isPlanSaved()) {
                Stage createPlanStage = (Stage) summarizeButton.getScene().getWindow();
                createPlanStage.close();
            }
            
        } catch (Exception e) {
            showAlert("Error opening summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        currentPlan.clear();
        lastSearchResults.clear();
        scheduleContainer.getChildren().clear();
        searchResultCountLabel.setText("");
        updatePlanView();
        startField.clear();
        destinationField.clear();
        datePicker.setValue(LocalDate.now());
        
        summarizeButton.setDisable(true);
    }

    private void updatePlanView() {
        planContainer.getChildren().clear();
        double totalFare = 0.0;
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (int i = 0; i < currentPlan.size(); i++) {
            Schedule s = currentPlan.get(i);
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
                currentPlan.remove(index);
                updatePlanView();
                if (currentPlan.isEmpty()) {
                    summarizeButton.setDisable(true);
                }
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
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
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
            
            // Check for tight connection (if not the last leg)
            if (i < currentPlan.size() - 1) {
                Schedule nextSchedule = currentPlan.get(i + 1);
                long connectionMinutes = Duration.between(s.getArrivalTime(), nextSchedule.getDepartureTime()).toMinutes();
                
                if (connectionMinutes < 30 && connectionMinutes >= 0) {
                    Label warningLabel = new Label("⚠ Tight connection: " + connectionMinutes + " min");
                    warningLabel.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; " +
                                         "-fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10px; -fx-font-weight: bold;");
                    card.getChildren().add(warningLabel);
                }
            }
            
            planContainer.getChildren().add(card);
        }
        
        // Calculate total duration from first departure to last arrival
        long totalMinutes = 0;
        if (!currentPlan.isEmpty()) {
            LocalDateTime firstDeparture = currentPlan.get(0).getDepartureTime();
            LocalDateTime lastArrival = currentPlan.get(currentPlan.size() - 1).getArrivalTime();
            totalMinutes = Duration.between(firstDeparture, lastArrival).toMinutes();
        }
        long totalHours = totalMinutes / 60;
        long totalMins = totalMinutes % 60;
        
        // Update totals
        legCountLabel.setText(currentPlan.size() + " Leg" + (currentPlan.size() != 1 ? "s" : ""));
        totalFareLabel.setText("৳" + String.format("%.2f", totalFare));
        totalTimeLabel.setText(totalHours + "h " + totalMins + "m");
    }
    
    private void displaySearchResults(List<Schedule> results) {
        scheduleContainer.getChildren().clear();
        searchResultCountLabel.setText(results.size() + " Result" + (results.size() != 1 ? "s" : ""));
        
        if (results.isEmpty()) {
            Label emptyLabel = new Label("No schedules found. Try different search criteria.");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-padding: 20;");
            scheduleContainer.getChildren().add(emptyLabel);
            return;
        }
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (Schedule s : results) {
            // Create card for each schedule
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-color: #d0d0d0; " +
                         "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
            
            // Add hover effect
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f0f8ff; -fx-padding: 12; -fx-border-color: #3498db; " +
                         "-fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-color: #d0d0d0; " +
                         "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));
            
            // Click to add to plan
            card.setOnMouseClicked(e -> {
                // Check if this schedule is already in the plan
                if (currentPlan.contains(s)) {
                    showAlert("This schedule is already in your travel plan.");
                    return;
                }
                currentPlan.add(s);
                updatePlanView();
                startField.setText(s.getDestination());
                destinationField.clear();
                // Clear search results after selection
                scheduleContainer.getChildren().clear();
                searchResultCountLabel.setText("");
                lastSearchResults.clear();
                summarizeButton.setDisable(false);
            });
            
            // Type badge and header
            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label typeLabel = new Label(s.getType());
            typeLabel.setStyle("-fx-background-color: " + 
                              (s instanceof BusSchedule ? "#3498db" : "#e74c3c") + 
                              "; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");
            
            header.getChildren().add(typeLabel);
            
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
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
            nameLabel.setWrapText(true);
            
            // Route
            HBox routeBox = new HBox(8);
            routeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label originLabel = new Label(s.getOrigin());
            originLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            Label arrowLabel = new Label("→");
            arrowLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6;");
            Label destLabel = new Label(s.getDestination());
            destLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            routeBox.getChildren().addAll(originLabel, arrowLabel, destLabel);
            
            // Time info
            HBox timeBox = new HBox(15);
            timeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            VBox departBox = new VBox(2);
            Label departLabelTitle = new Label("Departure");
            departLabelTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");
            Label departTime = new Label(s.getDepartureTime().format(dateFormatter) + " " + s.getDepartureTime().format(timeFormatter));
            departTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
            departBox.getChildren().addAll(departLabelTitle, departTime);
            
            VBox arriveBox = new VBox(2);
            Label arriveLabelTitle = new Label("Arrival");
            arriveLabelTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");
            Label arriveTime = new Label(s.getArrivalTime().format(dateFormatter) + " " + s.getArrivalTime().format(timeFormatter));
            arriveTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
            arriveBox.getChildren().addAll(arriveLabelTitle, arriveTime);
            
            timeBox.getChildren().addAll(departBox, arriveBox);
            
            // Calculate duration
            long minutes = Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
            long hours = minutes / 60;
            long mins = minutes % 60;
            String durationStr = hours + "h " + mins + "m";
            
            // Bottom info bar
            HBox bottomBox = new HBox(15);
            bottomBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            bottomBox.setStyle("-fx-padding: 8 0 0 0;");
            
            Label fareLabel = new Label("৳" + String.format("%.2f", s.getFare()));
            fareLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27ae60;");
            
            Label durationLabel = new Label("⏱ " + durationStr);
            durationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label clickHint = new Label("Click to add ➜");
            clickHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
            
            bottomBox.getChildren().addAll(fareLabel, durationLabel, spacer, clickHint);
            
            card.getChildren().addAll(header, nameLabel, routeBox, timeBox, bottomBox);
            scheduleContainer.getChildren().add(card);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
