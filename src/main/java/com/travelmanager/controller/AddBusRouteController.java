package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.rest.BusScheduleDTO;
import com.travelmanager.storage.BusScheduleStorage;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.AutoCompletePopup;
import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.List;

public class AddBusRouteController {
    @FXML private TextField busNameField;
    @FXML private TextField originField;
    @FXML private TextField destinationField;
    @FXML private TextField departureTimeField;
    @FXML private TextField arrivalTimeField;
    @FXML private TextField fareField;
    @FXML private TextField durationField;
    @FXML private TextArea noteToMasterField;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;
    
    @SuppressWarnings("unused")
    private AutoCompletePopup originAutoComplete;
    @SuppressWarnings("unused")
    private AutoCompletePopup destinationAutoComplete;
    private DatabaseManager databaseManager;
    private BusScheduleStorage busScheduleStorage;
    private List<String> allLocations;
    private boolean isEditMode = false;
    private ManageRoutesController.RouteRow editingRoute = null;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        busScheduleStorage = BusScheduleStorage.getInstance();
        
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
        
        errorLabel.setText("");
        statusLabel.setText("");
        
        // Check if we're in edit mode
        Object editContext = NavigationManager.getContext("editRoute");
        if (editContext instanceof ManageRoutesController.RouteRow) {
            ManageRoutesController.RouteRow route = (ManageRoutesController.RouteRow) editContext;
            populateFormForEdit(route);
            NavigationManager.clearContext("editRoute");
        }
    }
    
    private void populateFormForEdit(ManageRoutesController.RouteRow route) {
        isEditMode = true;
        editingRoute = route; // Store for later use when submitting
        
        // Fetch the actual bus schedule DTO which contains all data including duration
        BusScheduleDTO busSchedule = busScheduleStorage.getSchedule(route.getRouteName()).orElse(null);
        
        if (busSchedule != null) {
            // Use actual data from the schedule DTO
            busNameField.setText(busSchedule.getBusName());
            originField.setText(busSchedule.getStart());
            destinationField.setText(busSchedule.getDestination());
            fareField.setText(String.valueOf(busSchedule.getFare()));
            
            // Set times directly from DTO
            departureTimeField.setText(busSchedule.getStartTime());
            arrivalTimeField.setText(busSchedule.getArrivalTime());
            
            // Set duration from DTO
            if (busSchedule.getDuration() != null && !busSchedule.getDuration().isEmpty()) {
                durationField.setText(busSchedule.getDuration());
            }
        } else {
            // Fallback to route row data
            busNameField.setText(route.getRouteName());
            originField.setText(route.getOrigin());
            destinationField.setText(route.getDestination());
            fareField.setText(String.valueOf(route.getPrice()));
        }
        
        statusLabel.setText("Editing route: " + route.getRouteName());
        statusLabel.setStyle("-fx-text-fill: #3498db;");
    }
    
    @FXML
    private void handleSubmit() {
        errorLabel.setText("");
        
        // Validate inputs
        String busName = busNameField.getText().trim();
        String origin = originField.getText().trim();
        String destination = destinationField.getText().trim();
        String departureTime = departureTimeField.getText().trim();
        String arrivalTime = arrivalTimeField.getText().trim();
        String fareStr = fareField.getText().trim();
        String duration = durationField.getText().trim();
        
        if (busName.isEmpty() || origin.isEmpty() || destination.isEmpty() || 
            departureTime.isEmpty() || arrivalTime.isEmpty() || fareStr.isEmpty()) {
            errorLabel.setText("⚠ Please fill all required fields (marked with *)");
            return;
        }
        
        // Validate fare
        double fare;
        try {
            fare = Double.parseDouble(fareStr);
            if (fare <= 0) {
                errorLabel.setText("⚠ Fare must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("⚠ Invalid fare amount");
            return;
        }
        
        // Validate time format
        if (!isValidTimeFormat(departureTime) || !isValidTimeFormat(arrivalTime)) {
            errorLabel.setText("⚠ Time must be in HH:MM format (e.g., 08:30)");
            return;
        }
        
        // Auto-calculate duration if not provided
        if (duration.isEmpty()) {
            duration = calculateDuration(departureTime, arrivalTime);
        }
        
        try {
            String currentUser = AuthenticationManager.getInstance().getCurrentUsername();
            
            // Get note from user
            String noteToMaster = noteToMasterField.getText().trim();
            String description = "New bus route submission";
            if (!noteToMaster.isEmpty()) {
                description = "Note: " + noteToMaster;
            }
            
            // Submit to database for approval
            boolean submitted = databaseManager.submitPendingRoute(
                busName,
                origin,
                destination,
                "BUS",
                0, // duration in minutes (not used for now)
                fare,
                departureTime,
                "duration:" + duration + ";arrivalTime:" + arrivalTime,
                isEditMode ? "UPDATE" : "ADD",
                isEditMode && editingRoute != null ? editingRoute.getId() : null,
                currentUser,
                description
            );
            
            if (submitted) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("Bus Route Submitted");
                success.setContentText("Your bus route has been submitted for master approval.\n\n" +
                                      "Bus: " + busName + "\n" +
                                      "Route: " + origin + " → " + destination + "\n" +
                                      "Fare: ৳" + fare);
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
        if (isEditMode) {
            NavigationManager.navigateTo("manage-routes");
        } else {
            NavigationManager.navigateTo("add-route-selection");
        }
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
}
