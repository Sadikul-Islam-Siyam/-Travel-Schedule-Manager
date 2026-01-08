package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
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
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;
    
    @SuppressWarnings("unused")
    private AutoCompletePopup originAutoComplete;
    @SuppressWarnings("unused")
    private AutoCompletePopup destinationAutoComplete;
    private DatabaseManager databaseManager;
    private List<String> allLocations;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        
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
            
            // Submit to database for approval
            boolean submitted = databaseManager.submitPendingRoute(
                busName,
                origin,
                destination,
                "BUS",
                0, // duration in minutes (not used for now)
                fare,
                departureTime + "-" + arrivalTime,
                duration,
                "ADD",
                null,
                currentUser,
                "New bus route submission"
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
}
