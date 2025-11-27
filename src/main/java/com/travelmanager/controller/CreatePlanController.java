package com.travelmanager.controller;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.service.ScheduleService;
import com.travelmanager.util.DataManager;
import com.travelmanager.util.AutoCompletePopup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

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
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> typeColumn;
    @FXML private TableColumn<Schedule, String> nameColumn;
    @FXML private TableColumn<Schedule, String> originColumn;
    @FXML private TableColumn<Schedule, String> destinationColumn;
    @FXML private TableColumn<Schedule, String> departureColumn;
    @FXML private TableColumn<Schedule, String> arrivalColumn;
    @FXML private TableColumn<Schedule, Double> fareColumn;
    @FXML private TableColumn<Schedule, String> durationColumn;
    @FXML private TableColumn<Schedule, Integer> seatsColumn;
    
    @FXML private ListView<String> planListView;
    @FXML private Label totalFareLabel;
    @FXML private TextField planNameField;
    @FXML private Button searchButton;
    @FXML private Button addToPlanButton;
    @FXML private Button summarizeButton;
    @FXML private Button savePlanButton;

    private ScheduleService scheduleService;
    private List<Schedule> currentPlan;
    private ObservableList<Schedule> scheduleList;
    private ObservableList<String> planItems;
    private boolean isSummarized = false;
    private List<String> allLocations;
    private AutoCompletePopup startAutoComplete;
    private AutoCompletePopup destAutoComplete;
    private ToggleGroup transportTypeGroup;

    @FXML
    public void initialize() {
        scheduleService = new ScheduleService();
        currentPlan = new ArrayList<>();
        scheduleList = FXCollections.observableArrayList();
        planItems = FXCollections.observableArrayList();
        
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
        
        // Setup table columns
        typeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        nameColumn.setCellValueFactory(cellData -> {
            Schedule s = cellData.getValue();
            String name = "";
            if (s instanceof BusSchedule) {
                name = ((BusSchedule) s).getBusCompany();
            } else if (s instanceof TrainSchedule) {
                name = ((TrainSchedule) s).getTrainName();
            }
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        originColumn.setCellValueFactory(new PropertyValueFactory<>("origin"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
        departureColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        arrivalColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getArrivalTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        fareColumn.setCellValueFactory(new PropertyValueFactory<>("fare"));
        durationColumn.setCellValueFactory(cellData -> {
            Schedule s = cellData.getValue();
            long minutes = java.time.Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
            long hours = minutes / 60;
            long mins = minutes % 60;
            return new javafx.beans.property.SimpleStringProperty(hours + "h " + mins + "m");
        });
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        
        scheduleTable.setItems(scheduleList);
        planListView.setItems(planItems);
        
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Initial button states
        addToPlanButton.setDisable(true);
        summarizeButton.setDisable(true);
        savePlanButton.setDisable(true);
        
        // Enable add button when schedule is selected
        scheduleTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                addToPlanButton.setDisable(newSelection == null && !isSummarized);
            });
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
        
        scheduleList.setAll(results);
        
        if (results.isEmpty()) {
            showAlert("No schedules found for the given route and transport type.");
        }
    }

    @FXML
    private void handleAddToPlan() {
        Schedule selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a schedule to add to your plan.");
            return;
        }
        
        currentPlan.add(selected);
        updatePlanView();
        
        // Clear search fields for next leg
        startField.setText(selected.getDestination());
        destinationField.clear();
        scheduleList.clear();
        
        // Enable summarize button
        summarizeButton.setDisable(false);
        isSummarized = false;
    }

    @FXML
    private void handleSummarize() {
        if (currentPlan.isEmpty()) {
            showAlert("No schedules in the plan to summarize.");
            return;
        }
        
        planItems.clear();
        planItems.add("===== TRAVEL PLAN SUMMARY =====");
        planItems.add("");
        
        double totalFare = 0;
        for (int i = 0; i < currentPlan.size(); i++) {
            Schedule s = currentPlan.get(i);
            planItems.add("Leg " + (i + 1) + ":");
            planItems.add("  Type: " + s.getType());
            
            if (s instanceof BusSchedule) {
                BusSchedule bus = (BusSchedule) s;
                planItems.add("  Company: " + bus.getBusCompany() + " (" + bus.getBusType() + ")");
            } else if (s instanceof TrainSchedule) {
                TrainSchedule train = (TrainSchedule) s;
                planItems.add("  Train: " + train.getTrainName() + " (" + train.getTrainNumber() + ")");
                planItems.add("  Class: " + train.getSeatClass());
            }
            
            planItems.add("  From: " + s.getOrigin() + " → To: " + s.getDestination());
            planItems.add("  Departure: " + s.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            planItems.add("  Arrival: " + s.getArrivalTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            planItems.add("  Fare: ৳" + s.getFare());
            planItems.add("");
            
            totalFare += s.getFare();
        }
        
        planItems.add("==============================");
        planItems.add("TOTAL FARE: ৳" + totalFare);
        
        totalFareLabel.setText("Total Fare: ৳" + totalFare);
        
        // Enable save button
        savePlanButton.setDisable(false);
        isSummarized = true;
        
        // Disable add and search during summary
        searchButton.setDisable(true);
        addToPlanButton.setDisable(true);
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
            for (Schedule s : currentPlan) {
                route.addSchedule(s);
            }
            
            DataManager.saveToFile(route, "data/" + planName + ".json");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Plan saved successfully!");
            alert.showAndWait();
            
            // Close window
            Stage stage = (Stage) savePlanButton.getScene().getWindow();
            stage.close();
            
        } catch (Exception e) {
            showAlert("Error saving plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        currentPlan.clear();
        scheduleList.clear();
        planItems.clear();
        startField.clear();
        destinationField.clear();
        planNameField.clear();
        totalFareLabel.setText("Total Fare: ৳0.00");
        datePicker.setValue(LocalDate.now());
        
        searchButton.setDisable(false);
        addToPlanButton.setDisable(true);
        summarizeButton.setDisable(true);
        savePlanButton.setDisable(true);
        isSummarized = false;
    }

    private void updatePlanView() {
        planItems.clear();
        for (int i = 0; i < currentPlan.size(); i++) {
            Schedule s = currentPlan.get(i);
            String name = "";
            if (s instanceof BusSchedule) {
                name = ((BusSchedule) s).getBusCompany();
            } else if (s instanceof TrainSchedule) {
                name = ((TrainSchedule) s).getTrainName();
            }
            planItems.add((i + 1) + ". " + s.getType() + " - " + name + 
                         " (" + s.getOrigin() + " → " + s.getDestination() + ")");
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
