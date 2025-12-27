package com.travelmanager.controller;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.service.ScheduleService;
import com.travelmanager.util.AutoCompletePopup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    @FXML private ListView<Schedule> scheduleListView;
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
    private Schedule selectedSchedule; // Reserved for schedule selection feature
    // AutoComplete popups attach listeners automatically, no direct access needed
    @SuppressWarnings("unused")
    private AutoCompletePopup startAutoComplete;
    @SuppressWarnings("unused")
    private AutoCompletePopup destAutoComplete;
    private ToggleGroup transportTypeGroup;
    private List<Schedule> lastSearchResults;
    
    public Schedule getSelectedSchedule() { return selectedSchedule; }
    public void setSelectedSchedule(Schedule schedule) { this.selectedSchedule = schedule; }

    public void setSchedules(List<Schedule> schedules) {
        if (schedules != null && !schedules.isEmpty()) {
            this.currentPlan = new ArrayList<>(schedules);
            updatePlanView();
            summarizeButton.setDisable(false);
        }
    }

    @FXML
    public void initialize() {
        scheduleService = new ScheduleService();
        currentPlan = new ArrayList<>();
        lastSearchResults = new ArrayList<>();
        
        // Initialize locations list - All 64 districts of Bangladesh
        allLocations = Arrays.asList(
            "Barguna", "Barishal", "Bhola", "Jhalokati", "Patuakhali", "Pirojpur",
            "Bandarban", "Brahmanbaria", "Chandpur", "Chattogram", "Cox's Bazar", "Cumilla", "Feni", "Khagrachari", "Lakshmipur", "Noakhali", "Rangamati",
            "Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur", "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail",
            "Bagerhat", "Chuadanga", "Jashore", "Jhenaidah", "Khulna", "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira",
            "Jamalpur", "Mymensingh", "Netrokona", "Sherpur",
            "Bogura", "Joypurhat", "Naogaon", "Natore", "Chapai Nawabganj", "Pabna", "Rajshahi", "Sirajganj",
            "Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari", "Panchagarh", "Rangpur", "Thakurgaon",
            "Habiganj", "Moulvibazar", "Sunamganj", "Sylhet"
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
        
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Setup ListView with custom cell factory
        scheduleListView.setCellFactory(param -> new ScheduleListCell());
        scheduleListView.setPlaceholder(new Label("No schedules found. Search to see available routes."));
        
        // Handle selection in ListView
        scheduleListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                Schedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem();
                if (selectedSchedule != null) {
                    handleScheduleSelection(selectedSchedule);
                }
            }
        });
        
        // Initial button state
        summarizeButton.setDisable(true);
    }

    @FXML
    private void handleSearch() {
        String start = startField.getText().trim();
        String destination = destinationField.getText().trim();
        LocalDate travelDate = datePicker.getValue();
        
        if (start.isEmpty() || destination.isEmpty()) {
            showAlert("Please enter both start and destination locations.");
            return;
        }
        
        if (travelDate == null) {
            showAlert("Please select a travel date.");
            return;
        }
        
        // Show loading indicator
        searchResultCountLabel.setText("Searching schedules...");
        scheduleListView.getItems().clear();
        
        List<Schedule> results;
        
        // Fetch schedules based on transport type selection
        RadioButton selectedRadio = (RadioButton) transportTypeGroup.getSelectedToggle();
        try {
            if (selectedRadio == busRadio) {
                results = scheduleService.searchBusSchedules(start, destination, travelDate);
            } else if (selectedRadio == trainRadio) {
                results = scheduleService.searchTrainSchedules(start, destination, travelDate);
            } else {
                // All transport types
                results = scheduleService.searchSchedules(start, destination, travelDate);
            }
        } catch (Exception e) {
            showAlert("Error fetching schedules: " + e.getMessage());
            searchResultCountLabel.setText("Search Results (0)");
            return;
        }
        
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
        
        // Navigate to summarize view and pass schedules
        SummarizePlanController controller = com.travelmanager.util.NavigationManager.navigateToWithController(
            "summarize-plan", SummarizePlanController.class);
        if (controller != null) {
            controller.setSchedules(new ArrayList<>(currentPlan));
        }
    }

    @FXML
    private void handleReset() {
        currentPlan.clear();
        lastSearchResults.clear();
        scheduleListView.getItems().clear();
        searchResultCountLabel.setText("");
        updatePlanView();
        startField.clear();
        destinationField.clear();
        datePicker.setValue(LocalDate.now());
        
        summarizeButton.setDisable(true);
    }
    
    @FXML
    private void handleBack() {
        com.travelmanager.util.NavigationManager.getInstance().navigateToHome();
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
        searchResultCountLabel.setText(results.size() + " Result" + (results.size() != 1 ? "s" : ""));
        
        ObservableList<Schedule> scheduleList = FXCollections.observableArrayList(results);
        scheduleListView.setItems(scheduleList);
    }
    
    private void handleScheduleSelection(Schedule schedule) {
        // Check if this schedule is already in the plan
        if (currentPlan.contains(schedule)) {
            showAlert("This schedule is already in your travel plan.");
            return;
        }
        currentPlan.add(schedule);
        updatePlanView();
        startField.setText(schedule.getDestination());
        destinationField.clear();
        // Clear search results after selection
        scheduleListView.getItems().clear();
        searchResultCountLabel.setText("");
        lastSearchResults.clear();
        summarizeButton.setDisable(false);
    }
    
    /**
     * Custom ListCell for displaying Schedule items in compact format
     */
    private class ScheduleListCell extends ListCell<Schedule> {
        @Override
        protected void updateItem(Schedule schedule, boolean empty) {
            super.updateItem(schedule, empty);
            
            if (empty || schedule == null) {
                setGraphic(null);
                setStyle("");
                return;
            }
            
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            // Main container
            HBox mainRow = new HBox(12);
            mainRow.setAlignment(Pos.CENTER_LEFT);
            mainRow.setStyle("-fx-padding: 8; -fx-background-color: white; -fx-cursor: hand;");
            
            // Type badge
            Label typeLabel = new Label(schedule.getType());
            typeLabel.setStyle("-fx-background-color: " + 
                              (schedule instanceof BusSchedule ? "#3498db" : "#e74c3c") + 
                              "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px; -fx-font-weight: bold;");
            typeLabel.setMinWidth(50);
            
            // Info section
            VBox infoBox = new VBox(3);
            HBox.setHgrow(infoBox, Priority.ALWAYS);
            
            // Transport name
            String transportName;
            if (schedule instanceof BusSchedule) {
                BusSchedule bus = (BusSchedule) schedule;
                transportName = bus.getBusCompany() + " (" + bus.getBusType() + ")";
            } else if (schedule instanceof TrainSchedule) {
                TrainSchedule train = (TrainSchedule) schedule;
                transportName = train.getTrainName() + " (" + train.getTrainNumber() + ")";
            } else {
                transportName = "Unknown";
            }
            Label nameLabel = new Label(transportName);
            nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            // Route and time in one line
            long minutes = Duration.between(schedule.getDepartureTime(), schedule.getArrivalTime()).toMinutes();
            long hours = minutes / 60;
            long mins = minutes % 60;
            
            String detailsText = String.format("%s → %s • %s-%s • %dh %dm", 
                schedule.getOrigin(), 
                schedule.getDestination(),
                schedule.getDepartureTime().format(timeFormatter),
                schedule.getArrivalTime().format(timeFormatter),
                hours, mins);
            Label detailsLabel = new Label(detailsText);
            detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            
            infoBox.getChildren().addAll(nameLabel, detailsLabel);
            
            // Fare label
            Label fareLabel = new Label("৳" + String.format("%.0f", schedule.getFare()));
            fareLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #27ae60;");
            fareLabel.setMinWidth(70);
            fareLabel.setAlignment(Pos.CENTER_RIGHT);
            
            mainRow.getChildren().addAll(typeLabel, infoBox, fareLabel);
            
            // Hover effect
            mainRow.setOnMouseEntered(e -> mainRow.setStyle("-fx-padding: 8; -fx-background-color: #e3f2fd; -fx-cursor: hand;"));
            mainRow.setOnMouseExited(e -> mainRow.setStyle("-fx-padding: 8; -fx-background-color: white; -fx-cursor: hand;"));
            
            setGraphic(mainRow);
            setStyle("-fx-padding: 0; -fx-background-color: transparent;");
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
