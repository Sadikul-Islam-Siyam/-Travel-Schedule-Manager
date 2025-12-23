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
import java.util.stream.Collectors;

public class MyPendingRequestsController {
    
    @FXML private VBox requestsContainer;
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;
    
    private DatabaseManager databaseManager;
    private ObservableList<PendingChangeRow> myRequestsData;
    private String currentUsername;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        myRequestsData = FXCollections.observableArrayList();
        currentUsername = AuthenticationManager.getInstance().getCurrentUsername();
        
        loadMyPendingRequests();
    }
    
    private void loadMyPendingRequests() {
        try {
            List<DatabaseManager.PendingRouteData> allPendingRoutes = databaseManager.getPendingRoutes();
            
            // Filter only the routes submitted by current user
            List<DatabaseManager.PendingRouteData> myRoutes = allPendingRoutes.stream()
                .filter(data -> currentUsername.equals(data.getSubmittedBy()))
                .collect(Collectors.toList());
            
            myRequestsData.clear();
            requestsContainer.getChildren().clear();
            
            for (DatabaseManager.PendingRouteData data : myRoutes) {
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
                myRequestsData.add(row);
                requestsContainer.getChildren().add(createRequestCard(row));
            }
            
            updateStatusLabel();
            emptyLabel.setVisible(myRequestsData.isEmpty());
            
        } catch (SQLException e) {
            showError("Failed to load your pending requests: " + e.getMessage());
        }
    }
    
    private VBox createRequestCard(PendingChangeRow row) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; " +
                     "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header with route name and change type badge
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label routeNameLabel = new Label(row.getRouteName());
        routeNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        routeNameLabel.setWrapText(true);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label changeTypeLabel = new Label(row.getChangeType());
        String changeColor = row.getChangeType().equals("ADD") ? "#27ae60" : 
                            (row.getChangeType().equals("DELETE") ? "#e74c3c" : "#f39c12");
        changeTypeLabel.setStyle("-fx-background-color: " + changeColor + "; -fx-text-fill: white; " +
                                "-fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        Label statusBadge = new Label("⏳ PENDING");
        statusBadge.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        Label idLabel = new Label("#" + row.getId());
        idLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
        
        headerBox.getChildren().addAll(routeNameLabel, spacer, changeTypeLabel, statusBadge, idLabel);
        
        // Route details (origin and destination)
        if (!row.getChangeType().equals("DELETE")) {
            Label routeLabel = new Label("📍 " + row.getOrigin() + " → " + row.getDestination());
            routeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
            card.getChildren().add(routeLabel);
        }
        
        // Additional info row
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        if (!row.getChangeType().equals("DELETE")) {
            Label typeLabel = new Label("🚌 " + row.getTransportType());
            typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            
            Label durationLabel = new Label("⏱ " + row.getDurationMinutes() + " min");
            durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            
            Label priceLabel = new Label("৳" + String.format("%.2f", row.getPrice()));
            priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            
            infoBox.getChildren().addAll(typeLabel, durationLabel, priceLabel);
        }
        
        // Submission info
        Label submissionLabel = new Label("📅 Submitted on: " + row.getSubmittedDate());
        submissionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
        
        // Notes if any
        if (row.getNotes() != null && !row.getNotes().isEmpty()) {
            Label notesLabel = new Label("📝 Notes: " + row.getNotes());
            notesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-padding: 5 0;");
            notesLabel.setWrapText(true);
            card.getChildren().add(notesLabel);
        }
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));
        
        Button detailsBtn = new Button("View Details");
        detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; " +
                           "-fx-padding: 6 15; -fx-cursor: hand; -fx-background-radius: 4;");
        detailsBtn.setOnAction(e -> showDetailsDialog(row));
        
        Button editBtn = new Button("✏ Edit");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 12px; " +
                        "-fx-padding: 6 15; -fx-cursor: hand; -fx-background-radius: 4;");
        editBtn.setOnAction(e -> handleEdit(row));
        
        Button withdrawBtn = new Button("🗑 Withdraw");
        withdrawBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; " +
                            "-fx-padding: 6 15; -fx-cursor: hand; -fx-background-radius: 4;");
        withdrawBtn.setOnAction(e -> handleWithdraw(row));
        
        Label waitingLabel = new Label("⏳ Awaiting Master Approval");
        waitingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f39c12; -fx-font-style: italic;");
        
        buttonBox.getChildren().addAll(waitingLabel, detailsBtn, editBtn, withdrawBtn);
        
        // Add all elements to card
        card.getChildren().addAll(headerBox);
        if (infoBox.getChildren().size() > 0) {
            card.getChildren().add(infoBox);
        }
        card.getChildren().addAll(submissionLabel, buttonBox);
        
        return card;
    }
    
    private void updateStatusLabel() {
        if (myRequestsData.isEmpty()) {
            statusLabel.setText("You have no pending requests.");
            statusLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("📋 You have " + myRequestsData.size() + " pending request(s) awaiting approval");
            statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
    }
    
    private void showDetailsDialog(PendingChangeRow row) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Request Details");
        alert.setHeaderText("API Change Request #" + row.getId());
        
        StringBuilder details = new StringBuilder();
        details.append("Change Type: ").append(row.getChangeType()).append("\n");
        details.append("Status: PENDING APPROVAL\n");
        details.append("Submitted Date: ").append(row.getSubmittedDate()).append("\n\n");
        
        if ("DELETE".equals(row.getChangeType())) {
            details.append("Route to Delete:\n");
            details.append("  ID: ").append(row.getOriginalRouteId()).append("\n");
            details.append("  Name: ").append(row.getRouteName()).append("\n");
        } else {
            details.append("Route Name: ").append(row.getRouteName()).append("\n");
            details.append("Origin: ").append(row.getOrigin()).append("\n");
            details.append("Destination: ").append(row.getDestination()).append("\n");
            details.append("Transport Type: ").append(row.getTransportType()).append("\n");
            details.append("Duration: ").append(row.getDurationMinutes()).append(" minutes\n");
            details.append("Price: ৳").append(row.getPrice()).append("\n");
            details.append("Schedule Time: ").append(row.getScheduleTime()).append("\n");
            
            if (row.getMetadata() != null && !row.getMetadata().isEmpty()) {
                details.append("Metadata: ").append(row.getMetadata()).append("\n");
            }
            
            if ("UPDATE".equals(row.getChangeType())) {
                details.append("\nOriginal Route ID: ").append(row.getOriginalRouteId()).append("\n");
            }
        }
        
        if (row.getNotes() != null && !row.getNotes().isEmpty()) {
            details.append("\nNotes: ").append(row.getNotes());
        }
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    @FXML
    private void handleRefresh() {
        loadMyPendingRequests();
    }
    
    private void handleWithdraw(PendingChangeRow row) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Withdraw Request");
        confirmAlert.setHeaderText("Withdraw API Change Request #" + row.getId() + "?");
        confirmAlert.setContentText("This action cannot be undone. The request will be permanently deleted.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = databaseManager.withdrawPendingRoute(row.getId(), currentUsername);
                if (success) {
                    showSuccess("Request withdrawn successfully.");
                    loadMyPendingRequests();
                } else {
                    showError("Failed to withdraw request. It may have already been processed.");
                }
            } catch (SQLException e) {
                showError("Failed to withdraw request: " + e.getMessage());
            }
        }
    }
    
    private void handleEdit(PendingChangeRow row) {
        // Cannot edit DELETE requests
        if ("DELETE".equals(row.getChangeType())) {
            showError("Cannot edit a DELETE request. Please withdraw and submit a new one if needed.");
            return;
        }
        
        // Create edit dialog
        Dialog<PendingChangeRow> dialog = new Dialog<>();
        dialog.setTitle("Edit Pending Request");
        dialog.setHeaderText("Edit API Change Request #" + row.getId());
        
        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create form
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Route name
        Label nameLabel = new Label("Route Name:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField nameField = new TextField(row.getRouteName());
        
        // Origin
        Label originLabel = new Label("Origin:");
        originLabel.setStyle("-fx-font-weight: bold;");
        TextField originField = new TextField(row.getOrigin());
        
        // Destination
        Label destLabel = new Label("Destination:");
        destLabel.setStyle("-fx-font-weight: bold;");
        TextField destField = new TextField(row.getDestination());
        
        // Transport Type
        Label transportLabel = new Label("Transport Type:");
        transportLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> transportCombo = new ComboBox<>();
        transportCombo.getItems().addAll("Bus", "Train", "Both");
        transportCombo.setValue(row.getTransportType());
        
        // Duration
        Label durationLabel = new Label("Duration (minutes):");
        durationLabel.setStyle("-fx-font-weight: bold;");
        TextField durationField = new TextField(String.valueOf(row.getDurationMinutes()));
        
        // Price
        Label priceLabel = new Label("Price (৳):");
        priceLabel.setStyle("-fx-font-weight: bold;");
        TextField priceField = new TextField(String.valueOf(row.getPrice()));
        
        // Schedule Time
        Label scheduleLabel = new Label("Schedule Time:");
        scheduleLabel.setStyle("-fx-font-weight: bold;");
        TextField scheduleField = new TextField(row.getScheduleTime());
        
        // Metadata
        Label metadataLabel = new Label("Metadata (Optional):");
        metadataLabel.setStyle("-fx-font-weight: bold;");
        TextField metadataField = new TextField(row.getMetadata() != null ? row.getMetadata() : "");
        
        content.getChildren().addAll(
            nameLabel, nameField,
            originLabel, originField,
            destLabel, destField,
            transportLabel, transportCombo,
            durationLabel, durationField,
            priceLabel, priceField,
            scheduleLabel, scheduleField,
            metadataLabel, metadataField
        );
        
        dialog.getDialogPane().setContent(content);
        
        // Validate and convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String origin = originField.getText().trim();
                    String dest = destField.getText().trim();
                    String transport = transportCombo.getValue();
                    int duration = Integer.parseInt(durationField.getText().trim());
                    double price = Double.parseDouble(priceField.getText().trim());
                    String schedule = scheduleField.getText().trim();
                    String metadata = metadataField.getText().trim();
                    
                    // Validate required fields
                    if (name.isEmpty() || origin.isEmpty() || dest.isEmpty() || transport == null || schedule.isEmpty()) {
                        showError("Please fill in all required fields.");
                        return null;
                    }
                    
                    if (duration <= 0 || price <= 0) {
                        showError("Duration and price must be positive numbers.");
                        return null;
                    }
                    
                    return new PendingChangeRow(row.getId(), row.getChangeType(), name, origin, dest, 
                                               transport, duration, price, schedule, metadata.isEmpty() ? null : metadata,
                                               row.getOriginalRouteId(), row.getSubmittedBy(), row.getSubmittedDate(), row.getNotes());
                } catch (NumberFormatException e) {
                    showError("Invalid number format for duration or price.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<PendingChangeRow> result = dialog.showAndWait();
        result.ifPresent(updatedRow -> {
            try {
                databaseManager.updatePendingRoute(
                    updatedRow.getId(),
                    currentUsername,
                    updatedRow.getRouteName(),
                    updatedRow.getOrigin(),
                    updatedRow.getDestination(),
                    updatedRow.getTransportType(),
                    updatedRow.getDurationMinutes(),
                    updatedRow.getPrice(),
                    updatedRow.getScheduleTime(),
                    updatedRow.getMetadata(),
                    updatedRow.getNotes()
                );
                showSuccess("Request updated successfully!");
                loadMyPendingRequests();
            } catch (SQLException e) {
                showError("Failed to update request: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.getInstance().navigateToHome();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    
    // Row class for pending changes
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
        private final int originalRouteId;
        private final String submittedBy;
        private final String submittedDate;
        private final String notes;
        
        public PendingChangeRow(int id, String changeType, String routeName, String origin, 
                               String destination, String transportType, int durationMinutes,
                               double price, String scheduleTime, String metadata,
                               int originalRouteId, String submittedBy, String submittedDate, String notes) {
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
        public int getOriginalRouteId() { return originalRouteId; }
        public String getSubmittedBy() { return submittedBy; }
        public String getSubmittedDate() { return submittedDate; }
        public String getNotes() { return notes; }
    }
}
