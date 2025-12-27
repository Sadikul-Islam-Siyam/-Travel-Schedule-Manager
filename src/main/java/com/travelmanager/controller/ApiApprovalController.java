package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ApiApprovalController {
    
    @FXML private VBox changesContainer;
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;
    
    private DatabaseManager databaseManager;
    private ObservableList<PendingChangeRow> pendingChangesData;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        pendingChangesData = FXCollections.observableArrayList();
        
        loadPendingChanges();
    }
    
    private void loadPendingChanges() {
        try {
            List<DatabaseManager.PendingRouteData> pendingRoutes = databaseManager.getPendingRoutes();
            pendingChangesData.clear();
            changesContainer.getChildren().clear();
            
            for (DatabaseManager.PendingRouteData data : pendingRoutes) {
                PendingChangeRow row = new PendingChangeRow(
                    data.getId(),
                    data.getChangeType(),
                    data.getRouteName(),
                    data.getOrigin(),
                    data.getDestination(),
                    data.getTransportType(),
                    data.getDurationMinutes(),
                    data.getPrice(),
                    data.getScheduleTime(),
                    data.getMetadata(),
                    data.getOriginalRouteId(),
                    data.getSubmittedBy(),
                    data.getSubmittedDate(),
                    data.getNotes()
                );
                pendingChangesData.add(row);
                changesContainer.getChildren().add(createChangeCard(row));
            }
            
            updateStatusLabel();
            emptyLabel.setVisible(pendingChangesData.isEmpty());
            
        } catch (SQLException e) {
            showError("Failed to load pending changes: " + e.getMessage());
        }
    }
    
    private VBox createChangeCard(PendingChangeRow row) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; " +
                     "-fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        // Single compact row: Route Name + Details + Type Badge + Buttons
        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        
        // Left: Route Name and Origin → Destination
        VBox infoBox = new VBox(3);
        Label routeNameLabel = new Label(row.getRouteName());
        routeNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        String detailsText = "";
        if (!row.getChangeType().equals("DELETE")) {
            detailsText = row.getOrigin() + " → " + row.getDestination() + " • " + 
                         row.getTransportType() + " • " + row.getDurationMinutes() + "min • ৳" + 
                         String.format("%.0f", row.getPrice());
        } else {
            detailsText = "Deletion request";
        }
        Label detailsLabel = new Label(detailsText);
        detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        infoBox.getChildren().addAll(routeNameLabel, detailsLabel);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        // Change type badge
        Label changeTypeLabel = new Label(row.getChangeType());
        String changeColor = row.getChangeType().equals("ADD") ? "#27ae60" : 
                            (row.getChangeType().equals("DELETE") ? "#e74c3c" : "#f39c12");
        changeTypeLabel.setStyle("-fx-background-color: " + changeColor + "; -fx-text-fill: white; " +
                                "-fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        // Submitted by
        Label submittedLabel = new Label("by " + row.getSubmittedBy());
        submittedLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
        
        Region spacer2 = new Region();
        
        // Action buttons (compact)
        HBox buttonBox = new HBox(6);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button approveBtn = new Button("✓ Approve");
        approveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px; " +
                           "-fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        approveBtn.setOnAction(e -> handleApprove(row));
        
        Button rejectBtn = new Button("✗ Reject");
        rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; " +
                          "-fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        rejectBtn.setOnAction(e -> handleReject(row));
        
        buttonBox.getChildren().addAll(approveBtn, rejectBtn);
        
        mainRow.getChildren().addAll(infoBox, spacer1, changeTypeLabel, submittedLabel, spacer2, buttonBox);
        card.getChildren().add(mainRow);
        
        return card;
    }
    
    private void updateStatusLabel() {
        if (pendingChangesData.isEmpty()) {
            statusLabel.setText("All pending changes reviewed.");
            statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("⚠ " + pendingChangesData.size() + " pending change(s) awaiting approval");
            statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
    }
    
    @SuppressWarnings("unused")
    private void showDetailsDialog(PendingChangeRow row) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Change Details");
        alert.setHeaderText("API Change Request #" + row.getId());
        
        StringBuilder details = new StringBuilder();
        details.append("Change Type: ").append(row.getChangeType()).append("\n");
        details.append("Submitted By: ").append(row.getSubmittedBy()).append("\n");
        details.append("Submitted Date: ").append(row.getSubmittedDate()).append("\n\n");
        
        if ("DELETE".equals(row.getChangeType())) {
            details.append("Route to Delete:\n");
            if (row.getOriginalRouteId() != null) {
                details.append("  ID: ").append(row.getOriginalRouteId()).append("\n");
            }
            details.append("  Name: ").append(row.getRouteName()).append("\n");
        } else {
            details.append("Route Name: ").append(row.getRouteName()).append("\n");
            details.append("Origin: ").append(row.getOrigin()).append("\n");
            details.append("Destination: ").append(row.getDestination()).append("\n");
            details.append("Transport Type: ").append(row.getTransportType()).append("\n");
            details.append("Duration: ").append(row.getDurationMinutes()).append(" minutes\n");
            details.append("Price: $").append(row.getPrice()).append("\n");
            details.append("Schedule Time: ").append(row.getScheduleTime()).append("\n");
            
            if (row.getMetadata() != null && !row.getMetadata().isEmpty()) {
                details.append("Metadata: ").append(row.getMetadata()).append("\n");
            }
            
            if ("UPDATE".equals(row.getChangeType()) && row.getOriginalRouteId() != null) {
                details.append("\nOriginal Route ID: ").append(row.getOriginalRouteId()).append("\n");
            }
        }
        
        if (row.getNotes() != null && !row.getNotes().isEmpty()) {
            details.append("\nNotes: ").append(row.getNotes());
        }
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    private void handleApprove(PendingChangeRow row) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Approve Change");
        confirmAlert.setHeaderText("Approve API Change #" + row.getId() + "?");
        confirmAlert.setContentText("This will " + row.getChangeType().toLowerCase() + 
                                   " the route in the live API.\n\nRoute: " + row.getRouteName() +
                                   "\nSubmitted by: " + row.getSubmittedBy());
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String reviewedBy = AuthenticationManager.getInstance().getCurrentUsername();
                databaseManager.approvePendingRoute(row.getId(), reviewedBy);
                showSuccess("Change approved successfully! Route " + row.getChangeType().toLowerCase() + 
                          "d in live API.");
                loadPendingChanges();
            } catch (SQLException e) {
                showError("Failed to approve change: " + e.getMessage());
            }
        }
    }
    
    private void handleReject(PendingChangeRow row) {
        // Create custom dialog for feedback
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reject API Change");
        dialog.setHeaderText("Reject API Change Request #" + row.getId());
        
        ButtonType rejectButtonType = new ButtonType("Reject", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rejectButtonType, ButtonType.CANCEL);
        
        // Create feedback input
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Route: " + row.getRouteName() + "\nSubmitted by: " + row.getSubmittedBy());
        infoLabel.setStyle("-fx-font-size: 12px;");
        
        Label feedbackLabel = new Label("Provide feedback to the developer:");
        feedbackLabel.setStyle("-fx-font-weight: bold;");
        
        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Explain why this request is being rejected...");
        feedbackArea.setPrefRowCount(4);
        feedbackArea.setWrapText(true);
        
        content.getChildren().addAll(infoLabel, feedbackLabel, feedbackArea);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == rejectButtonType) {
                return feedbackArea.getText().trim();
            }
            return null;
        });
        
        Optional<String> feedback = dialog.showAndWait();
        feedback.ifPresent(feedbackText -> {
            try {
                String reviewedBy = AuthenticationManager.getInstance().getCurrentUsername();
                databaseManager.rejectPendingRoute(row.getId(), 
                    feedbackText.isEmpty() ? "No feedback provided" : feedbackText, reviewedBy);
                showSuccess("Change rejected successfully. Feedback sent to developer.");
                loadPendingChanges();
            } catch (SQLException e) {
                showError("Failed to reject change: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadPendingChanges();
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.getInstance().navigateToHome();
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // TableView row class
    public static class PendingChangeRow {
        private final int id;
        private final String changeType;
        private final String routeName;
        private final String origin;
        private final String destination;
        private final String transportType;
        private final int durationMinutes;
        private final double price;
        private final String scheduleTime;
        private final String metadata;
        private final Integer originalRouteId;
        private final String submittedBy;
        private final String submittedDate;
        private final String notes;
        
        public PendingChangeRow(int id, String changeType, String routeName, String origin, 
                               String destination, String transportType, int durationMinutes,
                               double price, String scheduleTime, String metadata,
                               Integer originalRouteId, String submittedBy, String submittedDate, String notes) {
            this.id = id;
            this.changeType = changeType;
            this.routeName = routeName;
            this.origin = origin;
            this.destination = destination;
            this.transportType = transportType;
            this.durationMinutes = durationMinutes;
            this.price = price;
            this.scheduleTime = scheduleTime;
            this.metadata = metadata;
            this.originalRouteId = originalRouteId;
            this.submittedBy = submittedBy;
            this.submittedDate = submittedDate;
            this.notes = notes;
        }
        
        // Getters
        public int getId() { return id; }
        public String getChangeType() { return changeType; }
        public String getRouteName() { return routeName; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getTransportType() { return transportType; }
        public int getDurationMinutes() { return durationMinutes; }
        public double getPrice() { return price; }
        public String getScheduleTime() { return scheduleTime; }
        public String getMetadata() { return metadata; }
        public Integer getOriginalRouteId() { return originalRouteId; }
        public String getSubmittedBy() { return submittedBy; }
        public String getSubmittedDate() { return submittedDate; }
        public String getNotes() { return notes; }
    }
}
