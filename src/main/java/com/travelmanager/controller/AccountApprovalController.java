package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.database.DatabaseManager.PendingUser;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
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
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused") // Reserved for future cell factory implementations
public class AccountApprovalController {
    @FXML private VBox accountsContainer;
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;
    
    private DatabaseManager databaseManager;
    private ObservableList<PendingUserRow> pendingUsersList;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        pendingUsersList = FXCollections.observableArrayList();
        
        statusLabel.setText("");
        loadPendingUsers();
    }
    
    private void loadPendingUsers() {
        try {
            List<PendingUser> pendingUsers = databaseManager.getPendingUsers();
            pendingUsersList.clear();
            accountsContainer.getChildren().clear();
            
            // Get current user's role
            String currentUserRole = AuthenticationManager.getInstance().getCurrentUserRole();
            boolean isDeveloper = "Developer".equalsIgnoreCase(currentUserRole);
            
            for (PendingUser user : pendingUsers) {
                // If current user is Developer, only show User role requests
                if (isDeveloper && !"User".equalsIgnoreCase(user.getRole().toString())) {
                    continue;
                }
                
                PendingUserRow userRow = new PendingUserRow(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().toString(),
                    formatDate(user.getCreatedDate())
                );
                pendingUsersList.add(userRow);
                accountsContainer.getChildren().add(createAccountCard(userRow));
            }
            
            // Show/hide empty message
            emptyLabel.setVisible(pendingUsersList.isEmpty());
            
        } catch (SQLException e) {
            showStatus("Error loading pending users: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    private VBox createAccountCard(PendingUserRow user) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; " +
                     "-fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        // Single row: Name + Role + Metadata + Buttons
        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        
        // Left: Name and Email
        VBox infoBox = new VBox(3);
        Label nameLabel = new Label(user.getFullName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        Label usernameEmailLabel = new Label(user.getUsername() + " • " + user.getEmail());
        usernameEmailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        infoBox.getChildren().addAll(nameLabel, usernameEmailLabel);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        // Role badge
        Label roleLabel = new Label(user.getRole());
        String roleColor = user.getRole().equals("MASTER") ? "#e74c3c" : 
                          (user.getRole().equals("DEVELOPER") ? "#f39c12" : "#3498db");
        roleLabel.setStyle("-fx-background-color: " + roleColor + "; -fx-text-fill: white; " +
                          "-fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        // Status label (Pending)
        Label statusLabel = new Label("⏳ PENDING");
        statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        Region spacer2 = new Region();
        
        // Action buttons (compact)
        HBox buttonBox = new HBox(6);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button approveBtn = new Button("✓ Approve");
        approveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px; " +
                           "-fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        approveBtn.setOnAction(e -> handleApprove(user));
        
        Button rejectBtn = new Button("✗ Reject");
        rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; " +
                          "-fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        rejectBtn.setOnAction(e -> handleReject(user));
        
        buttonBox.getChildren().addAll(approveBtn, rejectBtn);
        
        mainRow.getChildren().addAll(infoBox, spacer1, roleLabel, statusLabel, spacer2, buttonBox);
        card.getChildren().add(mainRow);
        
        return card;
    }
    
    private void setupActionsColumn() {
        // This method is no longer needed with card-based UI
    }
    
    private void handleApprove(PendingUserRow user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Approve Account");
        confirmAlert.setHeaderText("Approve account for " + user.getUsername() + "?");
        confirmAlert.setContentText("Role: " + user.getRole() + "\nFull Name: " + user.getFullName());
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = databaseManager.approvePendingUser(user.getId());
                if (success) {
                    databaseManager.logActivity(
                        AuthenticationManager.getInstance().getCurrentUsername(), 
                        "ACCOUNT_APPROVED", 
                        "Approved account: " + user.getUsername() + " (Role: " + user.getRole() + ")", 
                        true
                    );
                    showStatus("Account approved: " + user.getUsername(), false);
                    loadPendingUsers(); // Refresh table
                } else {
                    showStatus("Failed to approve account", true);
                }
            } catch (SQLException e) {
                showStatus("Error approving account: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }
    
    private void handleReject(PendingUserRow user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reject Account");
        confirmAlert.setHeaderText("Reject account for " + user.getUsername() + "?");
        confirmAlert.setContentText("This action cannot be undone. The user will need to re-register.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = databaseManager.rejectPendingUser(user.getId());
                if (success) {
                    databaseManager.logActivity(
                        AuthenticationManager.getInstance().getCurrentUsername(), 
                        "ACCOUNT_REJECTED", 
                        "Rejected account: " + user.getUsername() + " (Role: " + user.getRole() + ")", 
                        true
                    );
                    showStatus("Account rejected: " + user.getUsername(), false);
                    loadPendingUsers(); // Refresh table
                } else {
                    showStatus("Failed to reject account", true);
                }
            } catch (SQLException e) {
                showStatus("Error rejecting account: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadPendingUsers();
        showStatus("List refreshed", false);
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.navigateTo("home");
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + 
                           (isError ? "#e74c3c" : "#27ae60") + ";");
    }
    
    private String formatDate(String dateString) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return dateString;
        }
    }
    
    // Row class for TableView
    public static class PendingUserRow {
        private int id;
        private String username;
        private String fullName;
        private String email;
        private String role;
        private String createdDate;
        
        public PendingUserRow(int id, String username, String fullName, String email, 
                            String role, String createdDate) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
            this.createdDate = createdDate;
        }
        
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getCreatedDate() { return createdDate; }
    }
}
