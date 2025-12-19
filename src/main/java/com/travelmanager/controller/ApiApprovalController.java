package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.util.NavigationManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ApiApprovalController {
    
    @FXML private TableView<PendingChangeRow> pendingChangesTable;
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;
    
    private DatabaseManager databaseManager;
    private ObservableList<PendingChangeRow> pendingChangesData;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        pendingChangesData = FXCollections.observableArrayList();
        pendingChangesTable.setItems(pendingChangesData);
        
        // Add custom cell factory for actions column
        @SuppressWarnings("unchecked")
        TableColumn<PendingChangeRow, Void> actionsColumn = 
            (TableColumn<PendingChangeRow, Void>) pendingChangesTable.getColumns().get(8);
        
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveButton = new Button("✓ Approve");
            private final Button rejectButton = new Button("✗ Reject");
            private final Button detailsButton = new Button("Details");
            private final HBox buttons = new HBox(5, detailsButton, approveButton, rejectButton);
            
            {
                buttons.setAlignment(Pos.CENTER);
                
                // Style buttons
                approveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                     "-fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 11px; " +
                                     "-fx-background-radius: 4;");
                rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                    "-fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 11px; " +
                                    "-fx-background-radius: 4;");
                detailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                     "-fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 11px; " +
                                     "-fx-background-radius: 4;");
                
                // Button actions
                detailsButton.setOnAction(event -> {
                    PendingChangeRow row = getTableView().getItems().get(getIndex());
                    showDetailsDialog(row);
                });
                
                approveButton.setOnAction(event -> {
                    PendingChangeRow row = getTableView().getItems().get(getIndex());
                    handleApprove(row);
                });
                
                rejectButton.setOnAction(event -> {
                    PendingChangeRow row = getTableView().getItems().get(getIndex());
                    handleReject(row);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        loadPendingChanges();
    }
    
    private void loadPendingChanges() {
        try {
            List<DatabaseManager.PendingRouteData> pendingRoutes = databaseManager.getPendingRoutes();
            pendingChangesData.clear();
            
            for (DatabaseManager.PendingRouteData data : pendingRoutes) {
                pendingChangesData.add(new PendingChangeRow(
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
                ));
            }
            
            updateStatusLabel();
            emptyLabel.setVisible(pendingChangesData.isEmpty());
            
        } catch (SQLException e) {
            showError("Failed to load pending changes: " + e.getMessage());
        }
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
            details.append("  ID: ").append(row.getOriginalRouteId()).append("\n");
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
                databaseManager.approvePendingRoute(row.getId());
                showSuccess("Change approved successfully! Route " + row.getChangeType().toLowerCase() + 
                          "d in live API.");
                loadPendingChanges();
            } catch (SQLException e) {
                showError("Failed to approve change: " + e.getMessage());
            }
        }
    }
    
    private void handleReject(PendingChangeRow row) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reject Change");
        confirmAlert.setHeaderText("Reject API Change #" + row.getId() + "?");
        confirmAlert.setContentText("This will discard the pending change request.\n\nRoute: " + 
                                   row.getRouteName() + "\nSubmitted by: " + row.getSubmittedBy());
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseManager.rejectPendingRoute(row.getId());
                showSuccess("Change rejected successfully.");
                loadPendingChanges();
            } catch (SQLException e) {
                showError("Failed to reject change: " + e.getMessage());
            }
        }
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
