package com.travelmanager.controller;

import com.travelmanager.api.ScheduleDataManager;
import com.travelmanager.exception.ValidationException;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.service.ScheduleManagementService;
import com.travelmanager.util.NavigationManager;
import com.travelmanager.util.AutoCompletePopup;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ManageSchedulesController {
    
    // UI Components - Table
    @FXML private TableView<ScheduleRow> scheduleTable;
    @FXML private TableColumn<ScheduleRow, String> idColumn;
    @FXML private TableColumn<ScheduleRow, String> typeColumn;
    @FXML private TableColumn<ScheduleRow, String> routeColumn;
    @FXML private TableColumn<ScheduleRow, String> fareColumn;
    
    // UI Components - Filter
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private TextField searchField;
    @FXML private Label scheduleCountLabel;
    
    // UI Components - Form
    @FXML private Label formTitleLabel;
    @FXML private ComboBox<String> scheduleTypeCombo;
    @FXML private TextField scheduleIdField;
    @FXML private TextField originField;
    @FXML private TextField destinationField;
    @FXML private DatePicker departureDatePicker;
    @FXML private TextField departureHourField;
    @FXML private TextField departureMinuteField;
    @FXML private DatePicker arrivalDatePicker;
    @FXML private TextField arrivalHourField;
    @FXML private TextField arrivalMinuteField;
    @FXML private TextField fareField;
    @FXML private TextField seatsField;
    
    // Bus-specific
    @FXML private VBox busFieldsContainer;
    @FXML private TextField companyField;
    @FXML private ComboBox<String> busTypeCombo;
    
    // Train-specific
    @FXML private VBox trainFieldsContainer;
    @FXML private TextField trainNameField;
    @FXML private ComboBox<String> trainClassCombo;
    
    @FXML private Button saveButton;
    
    private ScheduleManagementService scheduleManagementService;
    private ScheduleDataManager dataManager;
    private ObservableList<ScheduleRow> scheduleData;
    private boolean isEditMode = false;
    private String editingScheduleId = null;
    
    // AutoComplete popups
    @SuppressWarnings("unused")
    private AutoCompletePopup originAutoComplete;
    @SuppressWarnings("unused")
    private AutoCompletePopup destinationAutoComplete;
    private List<String> allLocations;
    
    @FXML
    public void initialize() {
        scheduleManagementService = new ScheduleManagementService();
        dataManager = ScheduleDataManager.getInstance();
        scheduleData = FXCollections.observableArrayList();
        
        // Initialize locations list
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
        
        // Setup autocomplete
        originAutoComplete = new AutoCompletePopup(originField, allLocations);
        destinationAutoComplete = new AutoCompletePopup(destinationField, allLocations);
        
        setupTable();
        setupForm();
        loadSchedules();
        
        // Set default filter
        filterTypeCombo.setValue("ALL");
        
        // Add listener for schedule type combo to show/hide fields
        scheduleTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFormFieldsVisibility(newVal);
        });
        
        // Add listener for filter
        filterTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty);
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty);
        routeColumn.setCellValueFactory(data -> data.getValue().routeProperty);
        fareColumn.setCellValueFactory(data -> data.getValue().fareProperty);
        
        scheduleTable.setItems(scheduleData);
    }
    
    private void setupForm() {
        // Initialize ComboBoxes with items
        filterTypeCombo.getItems().addAll("ALL", "BUS", "TRAIN");
        scheduleTypeCombo.getItems().addAll("BUS", "TRAIN");
        busTypeCombo.getItems().addAll("AC", "Non-AC", "Sleeper", "Deluxe", "AC Sleeper");
        trainClassCombo.getItems().addAll("AC", "First Class", "Snigdha", "S_Chair", "Shovan");
        
        // Set default values
        scheduleTypeCombo.setValue("BUS");
        updateFormFieldsVisibility("BUS");
        
        // Set default date to today
        departureDatePicker.setValue(LocalDate.now());
        arrivalDatePicker.setValue(LocalDate.now());
    }
    
    private void updateFormFieldsVisibility(String scheduleType) {
        if ("BUS".equals(scheduleType)) {
            busFieldsContainer.setVisible(true);
            busFieldsContainer.setManaged(true);
            trainFieldsContainer.setVisible(false);
            trainFieldsContainer.setManaged(false);
        } else if ("TRAIN".equals(scheduleType)) {
            trainFieldsContainer.setVisible(true);
            trainFieldsContainer.setManaged(true);
            busFieldsContainer.setVisible(false);
            busFieldsContainer.setManaged(false);
        }
    }
    
    private void loadSchedules() {
        System.out.println("ManageSchedulesController: Loading schedules...");
        scheduleData.clear();
        
        // Load bus schedules
        List<BusSchedule> busSchedules = dataManager.getAllBusSchedules();
        System.out.println("ManageSchedulesController: Got " + busSchedules.size() + " bus schedules");
        for (BusSchedule bus : busSchedules) {
            scheduleData.add(new ScheduleRow(
                bus.getId(),
                "BUS",
                bus.getOrigin() + " → " + bus.getDestination(),
                "৳" + String.format("%.2f", bus.getFare()),
                bus
            ));
        }
        
        // Load train schedules
        List<TrainSchedule> trainSchedules = dataManager.getAllTrainSchedules();
        System.out.println("ManageSchedulesController: Got " + trainSchedules.size() + " train schedules");
        for (TrainSchedule train : trainSchedules) {
            scheduleData.add(new ScheduleRow(
                train.getId(),
                "TRAIN",
                train.getOrigin() + " → " + train.getDestination(),
                "৳" + String.format("%.2f", train.getFare()),
                train
            ));
        }
        
        System.out.println("ManageSchedulesController: Total schedules loaded: " + scheduleData.size());
        updateScheduleCount();
        applyFilter();
    }
    
    private void applyFilter() {
        String filterType = filterTypeCombo.getValue();
        String searchText = searchField.getText().toLowerCase();
        
        ObservableList<ScheduleRow> filtered = FXCollections.observableArrayList();
        
        for (ScheduleRow row : scheduleData) {
            boolean matchesType = "ALL".equals(filterType) || row.typeProperty.get().equals(filterType);
            boolean matchesSearch = searchText.isEmpty() || 
                row.idProperty.get().toLowerCase().contains(searchText) ||
                row.routeProperty.get().toLowerCase().contains(searchText);
            
            if (matchesType && matchesSearch) {
                filtered.add(row);
            }
        }
        
        scheduleTable.setItems(filtered);
    }
    
    private void updateScheduleCount() {
        int total = scheduleData.size();
        scheduleCountLabel.setText("Schedules: " + total);
    }
    
    @FXML
    private void handleEdit() {
        ScheduleRow selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a schedule to edit.");
            return;
        }
        
        isEditMode = true;
        editingScheduleId = selected.idProperty.get();
        formTitleLabel.setText("Edit Schedule: " + editingScheduleId);
        saveButton.setText("Update Schedule");
        
        populateFormWithSchedule(selected.schedule);
    }
    
    private void populateFormWithSchedule(Schedule schedule) {
        scheduleIdField.setText(schedule.getId());
        scheduleIdField.setDisable(true); // Can't change ID when editing
        
        originField.setText(schedule.getOrigin());
        destinationField.setText(schedule.getDestination());
        
        // Set dates and times
        LocalDateTime departure = schedule.getDepartureTime();
        LocalDateTime arrival = schedule.getArrivalTime();
        
        departureDatePicker.setValue(departure.toLocalDate());
        departureHourField.setText(String.format("%02d", departure.getHour()));
        departureMinuteField.setText(String.format("%02d", departure.getMinute()));
        
        arrivalDatePicker.setValue(arrival.toLocalDate());
        arrivalHourField.setText(String.format("%02d", arrival.getHour()));
        arrivalMinuteField.setText(String.format("%02d", arrival.getMinute()));
        
        fareField.setText(String.valueOf(schedule.getFare()));
        seatsField.setText(String.valueOf(schedule.getAvailableSeats()));
        
        // Set type-specific fields
        if (schedule instanceof BusSchedule) {
            BusSchedule bus = (BusSchedule) schedule;
            scheduleTypeCombo.setValue("BUS");
            companyField.setText(bus.getBusCompany());
            busTypeCombo.setValue(bus.getBusType());
        } else if (schedule instanceof TrainSchedule) {
            TrainSchedule train = (TrainSchedule) schedule;
            scheduleTypeCombo.setValue("TRAIN");
            trainNameField.setText(train.getTrainName());
            trainClassCombo.setValue(train.getSeatClass());
        }
        
        scheduleTypeCombo.setDisable(true); // Can't change type when editing
    }
    
    @FXML
    private void handleDelete() {
        ScheduleRow selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a schedule to delete.");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Schedule: " + selected.idProperty.get());
        confirmation.setContentText("Are you sure you want to delete this schedule? This action cannot be undone.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String scheduleId = selected.idProperty.get();
            String type = selected.typeProperty.get();
            
            try {
                scheduleManagementService.deleteSchedule(scheduleId, type);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Schedule deleted successfully!");
                loadSchedules();
            } catch (ValidationException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete schedule: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            // Validate inputs first (basic UI validation)
            if (!validateInputs()) {
                return;
            }
            
            // Get common fields
            String scheduleId = scheduleIdField.getText().trim();
            String origin = originField.getText().trim();
            String destination = destinationField.getText().trim();
            
            LocalDate depDate = departureDatePicker.getValue();
            int depHour = Integer.parseInt(departureHourField.getText().trim());
            int depMinute = Integer.parseInt(departureMinuteField.getText().trim());
            LocalDateTime departureTime = LocalDateTime.of(depDate, LocalTime.of(depHour, depMinute));
            
            LocalDate arrDate = arrivalDatePicker.getValue();
            int arrHour = Integer.parseInt(arrivalHourField.getText().trim());
            int arrMinute = Integer.parseInt(arrivalMinuteField.getText().trim());
            LocalDateTime arrivalTime = LocalDateTime.of(arrDate, LocalTime.of(arrHour, arrMinute));
            
            double fare = Double.parseDouble(fareField.getText().trim());
            int seats = Integer.parseInt(seatsField.getText().trim());
            
            String scheduleType = scheduleTypeCombo.getValue();
            
            // Create schedule object based on type
            Schedule schedule;
            if ("BUS".equals(scheduleType)) {
                String company = companyField.getText().trim();
                String busType = busTypeCombo.getValue();
                schedule = new BusSchedule(scheduleId, origin, destination,
                    departureTime, arrivalTime, fare, seats, company, busType);
            } else if ("TRAIN".equals(scheduleType)) {
                String trainName = trainNameField.getText().trim();
                String trainClass = trainClassCombo.getValue();
                schedule = new TrainSchedule(scheduleId, origin, destination,
                    departureTime, arrivalTime, fare, seats, trainName, "", trainClass);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid schedule type");
                return;
            }
            
            // Save or update using service (includes validation)
            if (isEditMode) {
                scheduleManagementService.updateSchedule(schedule);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Schedule updated successfully!");
            } else {
                scheduleManagementService.addSchedule(schedule);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Schedule created successfully!");
            }
            
            loadSchedules();
            handleClearForm();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for time, fare, and seats.");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateInputs() {
        List<String> errors = new ArrayList<>();
        
        if (scheduleIdField.getText().trim().isEmpty()) {
            errors.add("Schedule ID is required");
        }
        if (originField.getText().trim().isEmpty()) {
            errors.add("Origin is required");
        }
        if (destinationField.getText().trim().isEmpty()) {
            errors.add("Destination is required");
        }
        if (departureDatePicker.getValue() == null) {
            errors.add("Departure date is required");
        }
        if (arrivalDatePicker.getValue() == null) {
            errors.add("Arrival date is required");
        }
        if (fareField.getText().trim().isEmpty()) {
            errors.add("Fare is required");
        }
        if (seatsField.getText().trim().isEmpty()) {
            errors.add("Available seats is required");
        }
        
        String scheduleType = scheduleTypeCombo.getValue();
        if ("BUS".equals(scheduleType)) {
            if (companyField.getText().trim().isEmpty()) {
                errors.add("Company name is required for bus schedules");
            }
            if (busTypeCombo.getValue() == null) {
                errors.add("Bus type is required");
            }
        } else if ("TRAIN".equals(scheduleType)) {
            if (trainNameField.getText().trim().isEmpty()) {
                errors.add("Train name is required for train schedules");
            }
            if (trainClassCombo.getValue() == null) {
                errors.add("Train class is required");
            }
        }
        
        // Validate time fields
        try {
            int depHour = Integer.parseInt(departureHourField.getText().trim());
            int depMinute = Integer.parseInt(departureMinuteField.getText().trim());
            if (depHour < 0 || depHour > 23 || depMinute < 0 || depMinute > 59) {
                errors.add("Invalid departure time");
            }
            
            int arrHour = Integer.parseInt(arrivalHourField.getText().trim());
            int arrMinute = Integer.parseInt(arrivalMinuteField.getText().trim());
            if (arrHour < 0 || arrHour > 23 || arrMinute < 0 || arrMinute > 59) {
                errors.add("Invalid arrival time");
            }
        } catch (NumberFormatException e) {
            errors.add("Time fields must be numbers (HH and MM)");
        }
        
        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", String.join("\n", errors));
            return false;
        }
        
        return true;
    }
    
    @FXML
    private void handleClearForm() {
        scheduleIdField.clear();
        scheduleIdField.setDisable(false);
        originField.clear();
        destinationField.clear();
        departureHourField.clear();
        departureMinuteField.clear();
        arrivalHourField.clear();
        arrivalMinuteField.clear();
        fareField.clear();
        seatsField.clear();
        companyField.clear();
        trainNameField.clear();
        
        departureDatePicker.setValue(LocalDate.now());
        arrivalDatePicker.setValue(LocalDate.now());
        scheduleTypeCombo.setValue("BUS");
        scheduleTypeCombo.setDisable(false);
        
        isEditMode = false;
        editingScheduleId = null;
        formTitleLabel.setText("Add New Schedule");
        saveButton.setText("Save Schedule");
    }
    
    @FXML
    private void handleRefresh() {
        loadSchedules();
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "Schedule list refreshed successfully!");
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.getInstance().navigateToHome();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for table rows
    public static class ScheduleRow {
        private final SimpleStringProperty idProperty;
        private final SimpleStringProperty typeProperty;
        private final SimpleStringProperty routeProperty;
        private final SimpleStringProperty fareProperty;
        private final Schedule schedule;
        
        public ScheduleRow(String id, String type, String route, String fare, Schedule schedule) {
            this.idProperty = new SimpleStringProperty(id);
            this.typeProperty = new SimpleStringProperty(type);
            this.routeProperty = new SimpleStringProperty(route);
            this.fareProperty = new SimpleStringProperty(fare);
            this.schedule = schedule;
        }
    }
}
