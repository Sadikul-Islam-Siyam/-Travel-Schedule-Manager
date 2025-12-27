package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
// Reserved for future session validation in plan management
import com.travelmanager.util.AuthenticationManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for viewing saved plans
 */
@SuppressWarnings("unused") // Reserved import for session validation
public class SavedPlansController {

    @FXML
    private ScrollPane plansScrollPane;
    @FXML
    private VBox plansContainer;
    @FXML
    private Button viewButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private String selectedPlanName;

    @FXML
    public void initialize() {
        // Disable buttons initially
        viewButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        
        // All users can edit and delete their own plans
        // No need to hide these buttons based on role
        
        // Load plans from database
        loadPlans();
    }

    private void loadPlans() {
        plansContainer.getChildren().clear();
        String previousSelection = selectedPlanName;
        updateButtonStates();
        
        List<String> plans = DatabaseManager.getInstance().getAllPlanNames();
        
        if (plans.isEmpty()) {
            javafx.scene.control.Label emptyLabel = new javafx.scene.control.Label("No saved plans yet.");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-padding: 20; -fx-font-size: 14px;");
            plansContainer.getChildren().add(emptyLabel);
            selectedPlanName = null;
            return;
        }
        
        try {
            for (String planName : plans) {
                DatabaseManager.PlanSummary summary = DatabaseManager.getInstance().getPlanSummary(planName);
                Route route = DatabaseManager.getInstance().loadPlan(planName);
                plansContainer.getChildren().add(createPlanCard(summary, route));
            }
            selectedPlanName = previousSelection;
        } catch (Exception e) {
            showAlert("Error loading plans: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewPlan() {
        if (selectedPlanName == null) {
            showAlert("Please select a plan to view.");
            return;
        }
        
        try {
            Route route = DatabaseManager.getInstance().loadPlan(selectedPlanName);
            DatabaseManager.PlanSummary summary = DatabaseManager.getInstance().getPlanSummary(selectedPlanName);
            
            // Navigate to view details
            ViewPlanDetailsController controller = com.travelmanager.util.NavigationManager.navigateToWithController(
                "view-plan-details", ViewPlanDetailsController.class);
            if (controller != null) {
                String formattedDate = LocalDateTime.parse(summary.getCreatedDate())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                controller.setPlanData(summary.getName(), formattedDate, summary.getNotes(), route);
            }
            
        } catch (Exception e) {
            showAlert("Error loading plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeletePlan() {
        if (selectedPlanName == null) {
            return;
        }
        
        // All users can delete their own plans
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Plan");
        confirmAlert.setHeaderText("Are you sure you want to delete this plan?");
        confirmAlert.setContentText("Plan: " + selectedPlanName);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                DatabaseManager.getInstance().deletePlan(selectedPlanName);
                loadPlans();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Plan deleted successfully!");
                successAlert.showAndWait();
                
            } catch (Exception e) {
                showAlert("Error deleting plan: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditPlan() {
        if (selectedPlanName == null) {
            return;
        }
        
        // All users can edit their own plans
        
        try {
            Route route = DatabaseManager.getInstance().loadPlan(selectedPlanName);
            
            // Navigate to edit view
            EditPlanController controller = com.travelmanager.util.NavigationManager.navigateToWithController(
                "edit-plan", EditPlanController.class);
            if (controller != null) {
                controller.setPlanData(selectedPlanName, route);
            }
            
        } catch (Exception e) {
            showAlert("Error opening edit window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadPlans();
    }
    
    @FXML
    private void handleBack() {
        com.travelmanager.util.NavigationManager.getInstance().navigateToHome();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedPlanName != null;
        viewButton.setDisable(!hasSelection);
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    private VBox createPlanCard(DatabaseManager.PlanSummary summary, Route route) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; " +
                     "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        
        // Plan name header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(summary.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label legsLabel = new Label(summary.getLegCount() + " Leg" + 
                                   (summary.getLegCount() != 1 ? "s" : ""));
        legsLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                          "-fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 11px;");
        
        headerBox.getChildren().addAll(nameLabel, spacer, legsLabel);
        
        // Route info - get from actual route schedules
        String origin = route.getSchedules().isEmpty() ? "Unknown" : route.getSchedules().get(0).getOrigin();
        String destination = route.getSchedules().isEmpty() ? "Unknown" : 
                           route.getSchedules().get(route.getSchedules().size() - 1).getDestination();
        Label routeLabel = new Label(origin + " → " + destination);
        routeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        
        // Bottom info row
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Created date
        String formattedDate = LocalDateTime.parse(summary.getCreatedDate())
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Label dateLabel = new Label("📅 " + formattedDate);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        // Total fare
        Label fareLabel = new Label("৳" + String.format("%.2f", summary.getTotalFare()));
        fareLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #27ae60;");
        
        // Calculate total travel time
        long totalMinutes = 0;
        for (Schedule s : route.getSchedules()) {
            totalMinutes += java.time.Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
        }
        long totalHours = totalMinutes / 60;
        long totalMins = totalMinutes % 60;
        
        Label timeLabel = new Label("⏱ " + totalHours + "h " + totalMins + "m");
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
        
        bottomBox.getChildren().addAll(dateLabel, fareLabel, timeLabel);
        
        card.getChildren().addAll(headerBox, routeLabel, bottomBox);
        
        // Selection handling
        card.setOnMouseEntered(e -> {
            if (selectedPlanName == null || !selectedPlanName.equals(summary.getName())) {
                card.setStyle(card.getStyle() + "; -fx-border-color: #3498db; -fx-border-width: 2;");
            }
        });
        
        card.setOnMouseExited(e -> {
            if (selectedPlanName == null || !selectedPlanName.equals(summary.getName())) {
                card.setStyle(card.getStyle().replace("; -fx-border-color: #3498db; -fx-border-width: 2;", ""));
            }
        });
        
        card.setOnMouseClicked(e -> {
            // Deselect if clicking same card
            if (selectedPlanName != null && selectedPlanName.equals(summary.getName())) {
                selectedPlanName = null;
                loadPlans();
            } else {
                selectedPlanName = summary.getName();
                loadPlans();
            }
            updateButtonStates();
        });
        
        // Highlight if selected
        if (selectedPlanName != null && selectedPlanName.equals(summary.getName())) {
            card.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #3498db; " +
                         "-fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        }
        
        return card;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
