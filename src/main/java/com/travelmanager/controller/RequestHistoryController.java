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
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; " +
                     "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label routeNameLabel = new Label(record.getRouteName());
        routeNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        routeNameLabel.setWrapText(true);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label changeTypeLabel = new Label(record.getChangeType());
        String changeColor = record.getChangeType().equals("ADD") ? "#27ae60" : 
                            (record.getChangeType().equals("DELETE") ? "#e74c3c" : "#f39c12");
        changeTypeLabel.setStyle("-fx-background-color: " + changeColor + "; -fx-text-fill: white; " +
                                "-fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        Label statusBadge = new Label(record.getStatus());
        String statusColor = record.getStatus().equals("APPROVED") ? "#27ae60" : "#e74c3c";
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(routeNameLabel, spacer, changeTypeLabel, statusBadge);
        
        // Route details
        if (!record.getChangeType().equals("DELETE")) {
            Label routeLabel = new Label("📍 " + record.getOrigin() + " → " + record.getDestination());
            routeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
            card.getChildren().add(routeLabel);
        }
        
        // Dates
        HBox datesBox = new HBox(20);
        
        Label submittedLabel = new Label("Submitted: " + formatDate(record.getSubmittedDate()));
        submittedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
        
        Label reviewedLabel = new Label("Reviewed: " + formatDate(record.getReviewedDate()) + 
                                       " by " + record.getReviewedBy());
        reviewedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
        
        datesBox.getChildren().addAll(submittedLabel, reviewedLabel);
        
        // Feedback section
        if (record.getFeedback() != null && !record.getFeedback().isEmpty()) {
            VBox feedbackBox = new VBox(5);
            feedbackBox.setPadding(new Insets(10));
            feedbackBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffc107; " +
                               "-fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
            
            Label feedbackTitle = new Label("💬 Master Feedback:");
            feedbackTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #856404;");
            
            Label feedbackText = new Label(record.getFeedback());
            feedbackText.setStyle("-fx-font-size: 12px; -fx-text-fill: #856404;");
            feedbackText.setWrapText(true);
            
            feedbackBox.getChildren().addAll(feedbackTitle, feedbackText);
            card.getChildren().add(feedbackBox);
        }
        
        card.getChildren().addAll(headerBox, datesBox);
        
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
