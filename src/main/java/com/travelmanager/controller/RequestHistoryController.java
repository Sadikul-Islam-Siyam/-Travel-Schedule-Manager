package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class RequestHistoryController {
    
    @FXML private VBox historyContainer;
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;
    @FXML private ToggleGroup statusFilterGroup;
    
    private DatabaseManager databaseManager;
    private String currentUsername;
    private List<DatabaseManager.RouteHistoryData> allHistory;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        currentUsername = AuthenticationManager.getInstance().getCurrentUsername();
        
        loadHistory();
    }
    
    private void loadHistory() {
        try {
            allHistory = databaseManager.getRouteHistory(currentUsername);
            applyFilter();
        } catch (SQLException e) {
            showError("Failed to load history: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFilter() {
        applyFilter();
    }
    
    private void applyFilter() {
        RadioButton selected = (RadioButton) statusFilterGroup.getSelectedToggle();
        String filter = selected == null ? "All" : selected.getText();
        
        List<DatabaseManager.RouteHistoryData> filtered = allHistory;
        
        if ("Approved".equals(filter)) {
            filtered = allHistory.stream()
                .filter(h -> "APPROVED".equals(h.getStatus()))
                .collect(Collectors.toList());
        } else if ("Rejected".equals(filter)) {
            filtered = allHistory.stream()
                .filter(h -> "REJECTED".equals(h.getStatus()))
                .collect(Collectors.toList());
        }
        
        displayHistory(filtered);
    }
    
    private void displayHistory(List<DatabaseManager.RouteHistoryData> history) {
        historyContainer.getChildren().clear();
        
        if (history.isEmpty()) {
            emptyLabel.setVisible(true);
            statusLabel.setText("No history records found.");
        } else {
            emptyLabel.setVisible(false);
            statusLabel.setText("📊 " + history.size() + " record(s) found");
            
            for (DatabaseManager.RouteHistoryData record : history) {
                historyContainer.getChildren().add(createHistoryCard(record));
            }
        }
    }
    
    private VBox createHistoryCard(DatabaseManager.RouteHistoryData record) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 6; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 3, 0, 0, 1);");
        
        // Main row with all info
        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        
        // Info section (left)
        VBox infoBox = new VBox(3);
        
        Label routeNameLabel = new Label(record.getRouteName());
        routeNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Build info line
        StringBuilder infoBuilder = new StringBuilder();
        if (!record.getChangeType().equals("DELETE")) {
            infoBuilder.append(record.getOrigin()).append(" → ").append(record.getDestination()).append(" • ");
        }
        infoBuilder.append("Submitted: ").append(formatDate(record.getSubmittedDate()))
                   .append(" • Reviewed: ").append(formatDate(record.getReviewedDate()))
                   .append(" by ").append(record.getReviewedBy());
        
        Label infoLabel = new Label(infoBuilder.toString());
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        infoBox.getChildren().addAll(routeNameLabel, infoLabel);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        // Change type badge
        Label changeTypeLabel = new Label(record.getChangeType());
        String changeColor = record.getChangeType().equals("ADD") ? "#27ae60" : 
                            (record.getChangeType().equals("DELETE") ? "#e74c3c" : "#f39c12");
        changeTypeLabel.setStyle("-fx-background-color: " + changeColor + "; -fx-text-fill: white; " +
                               "-fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        // Status badge
        Label statusBadge = new Label(record.getStatus());
        String statusColor = record.getStatus().equals("APPROVED") ? "#27ae60" : "#e74c3c";
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                            "-fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        mainRow.getChildren().addAll(infoBox, spacer1, changeTypeLabel, statusBadge);
        card.getChildren().add(mainRow);
        
        // Feedback section (if exists, displayed below main row)
        if (record.getFeedback() != null && !record.getFeedback().isEmpty()) {
            HBox feedbackBox = new HBox(8);
            feedbackBox.setPadding(new Insets(8));
            feedbackBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffc107; " +
                               "-fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
            
            Label feedbackIcon = new Label("💬");
            feedbackIcon.setStyle("-fx-font-size: 12px;");
            
            Label feedbackText = new Label(record.getFeedback());
            feedbackText.setStyle("-fx-font-size: 11px; -fx-text-fill: #856404;");
            feedbackText.setWrapText(true);
            HBox.setHgrow(feedbackText, Priority.ALWAYS);
            
            feedbackBox.getChildren().addAll(feedbackIcon, feedbackText);
            card.getChildren().add(feedbackBox);
        }
        
        return card;
    }
    
    private String formatDate(String dateString) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateString);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return dateString;
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadHistory();
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.getInstance().navigateToHome();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
