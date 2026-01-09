package com.travelmanager.controller;

import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Route;
import com.travelmanager.util.RouteGraph;
import com.travelmanager.util.RouteGraph.JourneyPlan;
import com.travelmanager.util.RouteGraph.RouteEdge;
import com.travelmanager.service.rest.RestScheduleService;
import com.travelmanager.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.geometry.Side;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Controller for automatic route generation with multi-leg pathfinding
 */
public class AutomaticRouteController {

    @FXML
    private TextField startField;
    
    @FXML
    private TextField destinationField;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private ComboBox<String> maxLegsCombo;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private VBox resultsContainer;
    
    private final RestScheduleService scheduleService = new RestScheduleService();
    private Set<String> availableCities = new TreeSet<>();

    @FXML
    public void initialize() {
        // Set default values
        datePicker.setValue(LocalDate.now());
        maxLegsCombo.getItems().addAll("1 (Direct only)", "2 (1 transfer)", "3 (2 transfers)");
        maxLegsCombo.setValue("3 (2 transfers)");
        statusLabel.setText("Enter your journey details and click Generate Routes");
        
        // Load cities for autocomplete in background
        loadAvailableCities();
        
        // Setup autocomplete for both fields
        setupAutoComplete(startField);
        setupAutoComplete(destinationField);
    }
    
    private void loadAvailableCities() {
        new Thread(() -> {
            try {
                List<Schedule> allSchedules = scheduleService.getAllSchedules();
                Set<String> cities = allSchedules.stream()
                    .flatMap(s -> List.of(s.getOrigin(), s.getDestination()).stream())
                    .collect(Collectors.toCollection(TreeSet::new));
                
                javafx.application.Platform.runLater(() -> {
                    availableCities.clear();
                    availableCities.addAll(cities);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void setupAutoComplete(TextField textField) {
        ContextMenu contextMenu = new ContextMenu();
        
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            String input = newValue.trim();
            
            if (input.isEmpty()) {
                contextMenu.hide();
                return;
            }
            
            // Filter cities that match the input
            List<String> matches = availableCities.stream()
                .filter(city -> city.toLowerCase().startsWith(input.toLowerCase()))
                .limit(8)
                .collect(Collectors.toList());
            
            if (matches.isEmpty()) {
                contextMenu.hide();
            } else {
                contextMenu.getItems().clear();
                
                for (String city : matches) {
                    MenuItem item = new MenuItem(city);
                    item.setOnAction(e -> {
                        textField.setText(city);
                        textField.positionCaret(city.length());
                        contextMenu.hide();
                    });
                    contextMenu.getItems().add(item);
                }
                
                if (!contextMenu.isShowing()) {
                    contextMenu.show(textField, Side.BOTTOM, 0, 0);
                }
            }
        });
        
        // Hide context menu when field loses focus
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                contextMenu.hide();
            }
        });
    }
    
    @FXML
    private void handleGenerateRoutes() {
        String start = startField.getText().trim();
        String destination = destinationField.getText().trim();
        LocalDate date = datePicker.getValue();
        
        // Validation
        if (start.isEmpty() || destination.isEmpty()) {
            showError("Please enter both starting city and destination");
            return;
        }
        
        if (date == null) {
            showError("Please select a travel date");
            return;
        }
        
        if (date.isBefore(LocalDate.now())) {
            showError("Travel date cannot be in the past");
            return;
        }
        
        // Extract max legs from combo box
        String selected = maxLegsCombo.getValue();
        int maxLegs = Integer.parseInt(selected.substring(0, 1));
        
        statusLabel.setText("🔍 Searching for routes...");
        statusLabel.setTextFill(Color.web("#3498db"));
        resultsContainer.getChildren().clear();
        
        // Perform search in background thread
        new Thread(() -> {
            try {
                List<Schedule> allSchedules = scheduleService.getAllSchedules();
                
                if (allSchedules.isEmpty()) {
                    javafx.application.Platform.runLater(() -> 
                        showError("No schedules available. Please check your internet connection."));
                    return;
                }
                
                RouteGraph graph = new RouteGraph(allSchedules, date);
                List<JourneyPlan> routes = graph.findRoutes(start, destination, maxLegs);
                
                javafx.application.Platform.runLater(() -> {
                    if (routes.isEmpty()) {
                        statusLabel.setText("❌ No routes found between " + start + " and " + destination);
                        statusLabel.setTextFill(Color.web("#e74c3c"));
                        
                        VBox noResultsBox = new VBox(10);
                        noResultsBox.setStyle("-fx-background-color: #fff3cd; -fx-padding: 20; -fx-background-radius: 10;");
                        
                        Label noResultsLabel = new Label("💡 Suggestions:");
                        noResultsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                        
                        Label suggestion1 = new Label("• Check the spelling of city names");
                        Label suggestion2 = new Label("• Try increasing maximum legs/transfers");
                        Label suggestion3 = new Label("• Check if schedules are available for this date");
                        
                        noResultsBox.getChildren().addAll(noResultsLabel, suggestion1, suggestion2, suggestion3);
                        resultsContainer.getChildren().add(noResultsBox);
                    } else {
                        displayRoutes(routes, start, destination, date);
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> 
                    showError("Error searching routes: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }
    
    private void displayRoutes(List<JourneyPlan> routes, String start, String destination, LocalDate date) {
        statusLabel.setText("✅ Found " + routes.size() + " route(s) from " + start + " to " + destination);
        statusLabel.setTextFill(Color.web("#27ae60"));
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        
        for (int i = 0; i < routes.size(); i++) {
            JourneyPlan journey = routes.get(i);
            
            VBox journeyBox = new VBox(12);
            journeyBox.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 8; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2); -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
            
            // Header with journey number and summary
            HBox header = new HBox(12);
            header.setStyle("-fx-alignment: center-left;");
            
            Label optionLabel = new Label("Option " + (i + 1));
            optionLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
            
            Label legsLabel = new Label(journey.getNumberOfLegs() + " leg" + (journey.getNumberOfLegs() > 1 ? "s" : ""));
            legsLabel.setStyle("-fx-background-color: #e8f4fd; -fx-text-fill: #2196F3; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
            
            Duration totalDuration = Duration.ofMinutes(journey.getTotalTravelTime());
            Label timeLabel = new Label("⏱ " + formatDuration(totalDuration));
            timeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
            
            Label fareLabel = new Label("৳" + String.format("%.0f", journey.getTotalFare()));
            fareLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
            
            // Add spacer
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            // Save button
            Button saveBtn = new Button("💾 Save Plan");
            saveBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 6 15; " +
                           "-fx-cursor: hand; -fx-background-radius: 5; -fx-font-size: 12px;");
            final int journeyIndex = i;
            saveBtn.setOnAction(e -> handleSavePlan(journey, journeyIndex + 1));
            
            header.getChildren().addAll(optionLabel, legsLabel, timeLabel, fareLabel, spacer, saveBtn);
            journeyBox.getChildren().add(header);
            
            // Separator
            Separator sep1 = new Separator();
            journeyBox.getChildren().add(sep1);
            
            // Legs details
            List<RouteEdge> legs = journey.getLegs();
            for (int legIndex = 0; legIndex < legs.size(); legIndex++) {
                RouteEdge leg = legs.get(legIndex);
                Schedule schedule = leg.schedule;
                
                VBox legBox = new VBox(6);
                legBox.setStyle("-fx-padding: 12; -fx-background-color: #f8f9fa; -fx-background-radius: 6;");
                
                HBox legHeader = new HBox(10);
                legHeader.setStyle("-fx-alignment: center-left;");
                
                Label legNumber = new Label("Leg " + (legIndex + 1));
                legNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #333;");
                
                // Get route name from specific schedule type
                String routeName = "";
                if (schedule instanceof TrainSchedule) {
                    routeName = ((TrainSchedule) schedule).getTrainName();
                } else if (schedule instanceof BusSchedule) {
                    routeName = ((BusSchedule) schedule).getBusCompany();
                }
                
                Label routeNameLabel = new Label(routeName);
                routeNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-font-weight: 600;");
                
                Label typeLabel = new Label(schedule.getType().equals("Train") ? "🚆" : "🚌");
                typeLabel.setStyle("-fx-font-size: 14px;");
                
                legHeader.getChildren().addAll(typeLabel, legNumber, routeNameLabel);
                legBox.getChildren().add(legHeader);
                
                // Route details
                Label fromLabel = new Label("From: " + schedule.getOrigin() + " at " + schedule.getDepartureTime().format(timeFormatter));
                fromLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                
                Label toLabel = new Label("To: " + schedule.getDestination() + " at " + schedule.getArrivalTime().format(timeFormatter));
                toLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                
                Duration legDuration = Duration.between(schedule.getDepartureTime(), schedule.getArrivalTime());
                Label durationLabel = new Label("Duration: " + formatDuration(legDuration) + 
                                              " • Fare: ৳" + String.format("%.0f", schedule.getFare()));
                durationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
                
                legBox.getChildren().addAll(fromLabel, toLabel, durationLabel);
                journeyBox.getChildren().add(legBox);
                
                // Transfer point (if not last leg)
                if (legIndex < legs.size() - 1) {
                    String transferCity = journey.getTransferPoints().get(legIndex);
                    RouteEdge nextLeg = legs.get(legIndex + 1);
                    long waitMinutes = java.time.Duration.between(schedule.getArrivalTime(), nextLeg.schedule.getDepartureTime()).toMinutes();
                    
                    HBox transferBox = new HBox(8);
                    transferBox.setStyle("-fx-alignment: center-left; -fx-padding: 8; -fx-background-color: #fff9e6; -fx-background-radius: 5;");
                    
                    Label transferIcon = new Label("🔄");
                    transferIcon.setStyle("-fx-font-size: 14px;");
                    
                    Label transferLabel = new Label("Transfer at " + transferCity + " • Wait: " + waitMinutes + " min");
                    transferLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #d68910; -fx-font-size: 11px;");
                    
                    transferBox.getChildren().addAll(transferIcon, transferLabel);
                    journeyBox.getChildren().add(transferBox);
                }
            }
            
            resultsContainer.getChildren().add(journeyBox);
        }
    }
    
    private String formatDuration(java.time.Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%dh %dm", hours, minutes);
    }
    
    private void handleSavePlan(JourneyPlan journey, int optionNumber) {
        TextInputDialog dialog = new TextInputDialog("Multi-leg Route Option " + optionNumber);
        dialog.setTitle("Save Travel Plan");
        dialog.setHeaderText("Save this route to your plans");
        dialog.setContentText("Plan name:");
        
        dialog.showAndWait().ifPresent(planName -> {
            if (planName.trim().isEmpty()) {
                showError("Plan name cannot be empty");
                return;
            }
            
            try {
                // Convert JourneyPlan to Route
                Route route = new Route();
                for (Schedule schedule : journey.getSchedules()) {
                    route.addSchedule(schedule);
                }
                
                // Save to database
                DatabaseManager.getInstance().savePlan(planName, route, 
                    "Multi-leg route with " + journey.getNumberOfLegs() + " transfer(s)");
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Plan '" + planName + "' saved successfully!");
                alert.showAndWait();
                
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to save plan");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        });
    }
    
    @FXML
    private void handleClear() {
        startField.clear();
        destinationField.clear();
        datePicker.setValue(LocalDate.now());
        maxLegsCombo.setValue("3 (2 transfers)");
        resultsContainer.getChildren().clear();
        statusLabel.setText("Enter your journey details and click Generate Routes");
        statusLabel.setTextFill(Color.web("#7f8c8d"));
    }
    
    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setTextFill(Color.web("#e74c3c"));
    }
    
    @FXML
    private void handleBack() {
        com.travelmanager.util.NavigationManager.getInstance().navigateToHome();
    }
}
