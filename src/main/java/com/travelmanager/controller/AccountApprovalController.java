package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.database.DatabaseManager.PendingUser;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AccountApprovalController {
    @FXML private TableView<PendingUserRow> pendingUsersTable;
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
            
            for (PendingUser user : pendingUsers) {
                pendingUsersList.add(new PendingUserRow(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().toString(),
                    formatDate(user.getCreatedDate())
                ));
            }
            
            pendingUsersTable.setItems(pendingUsersList);
            
            // Show/hide empty message
            emptyLabel.setVisible(pendingUsersList.isEmpty());
            
            // Set up the actions column with approve/reject buttons
            setupActionsColumn();
            
        } catch (SQLException e) {
            showStatus("Error loading pending users: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    private void setupActionsColumn() {
        @SuppressWarnings("unchecked")
        TableColumn<PendingUserRow, Void> actionsColumn = (TableColumn<PendingUserRow, Void>) pendingUsersTable.getColumns().get(6);
        
        actionsColumn.setCellFactory(new Callback<TableColumn<PendingUserRow, Void>, TableCell<PendingUserRow, Void>>() {
            @Override
            public TableCell<PendingUserRow, Void> call(TableColumn<PendingUserRow, Void> param) {
                return new TableCell<PendingUserRow, Void>() {
                    private final Button approveBtn = new Button("✓ Approve");
                    private final Button rejectBtn = new Button("✗ Reject");
                    private final HBox container = new HBox(5, approveBtn, rejectBtn);
                    
                    {
                        approveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-cursor: hand; -fx-background-radius: 4;");
                        rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-cursor: hand; -fx-background-radius: 4;");
                        container.setAlignment(Pos.CENTER);
                        
                        approveBtn.setOnAction(event -> {
                            PendingUserRow user = getTableView().getItems().get(getIndex());
                            handleApprove(user);
                        });
                        
                        rejectBtn.setOnAction(event -> {
                            PendingUserRow user = getTableView().getItems().get(getIndex());
                            handleReject(user);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                };
            }
        });
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
