package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.User;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ManageUsersController {
    @FXML private VBox usersContainer;
    @FXML private Label statusLabel;
    
    private DatabaseManager databaseManager;
    private ObservableList<UserRow> usersList;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        usersList = FXCollections.observableArrayList();
        
        loadUsers();
    }
    
    private void loadUsers() {
        try {
            List<User> users = databaseManager.getAllUsers();
            usersList.clear();
            usersContainer.getChildren().clear();
            
            for (User user : users) {
                UserRow userRow = new UserRow(
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().toString()
                );
                usersList.add(userRow);
                usersContainer.getChildren().add(createUserCard(userRow));
            }
            
            statusLabel.setText("");
        } catch (SQLException e) {
            showStatus("Error loading users: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    private VBox createUserCard(UserRow user) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; " +
                     "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header with username and role badge
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label currentRoleLabel = new Label(user.getRole());
        String roleColor = user.getRole().equals("MASTER") ? "#e74c3c" : 
                          (user.getRole().equals("DEVELOPER") ? "#f39c12" : "#3498db");
        currentRoleLabel.setStyle("-fx-background-color: " + roleColor + "; -fx-text-fill: white; " +
                                 "-fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(usernameLabel, spacer, currentRoleLabel);
        
        // Full name
        Label fullNameLabel = new Label("👤 " + user.getFullName());
        fullNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        
        // Email
        Label emailLabel = new Label("✉ " + user.getEmail());
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        
        // Role change section
        HBox roleChangeBox = new HBox(10);
        roleChangeBox.setAlignment(Pos.CENTER_LEFT);
        roleChangeBox.setPadding(new Insets(5, 0, 0, 0));
        
        Label changeLabel = new Label("Change Role:");
        changeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-font-weight: bold;");
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("USER", "DEVELOPER", "MASTER");
        roleComboBox.setValue(user.getRole());
        roleComboBox.setStyle("-fx-font-size: 12px;");
        roleComboBox.setPrefWidth(150);
        
        Button applyBtn = new Button("Apply");
        applyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; " +
                         "-fx-padding: 6 15; -fx-cursor: hand; -fx-background-radius: 4; -fx-font-weight: bold;");
        applyBtn.setOnAction(e -> {
            String newRole = roleComboBox.getValue();
            if (newRole != null && !newRole.equals(user.getRole())) {
                handleRoleChange(user, newRole);
            }
        });
        
        roleChangeBox.getChildren().addAll(changeLabel, roleComboBox, applyBtn);
        
        // Add all elements to card
        card.getChildren().addAll(headerBox, fullNameLabel, emailLabel, roleChangeBox);
        
        return card;
    }
    
    private void handleRoleChange(UserRow user, String newRole) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Change User Role");
        confirmAlert.setHeaderText("Change role for " + user.getUsername() + "?");
        confirmAlert.setContentText("Current Role: " + user.getRole() + "\nNew Role: " + newRole);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = databaseManager.changeUserRole(user.getUsername(), newRole);
                if (success) {
                    databaseManager.logActivity(
                        AuthenticationManager.getInstance().getCurrentUsername(),
                        "ROLE_CHANGED",
                        "Changed role for " + user.getUsername() + " from " + user.getRole() + " to " + newRole,
                        true
                    );
                    showStatus("Role changed successfully for " + user.getUsername(), false);
                    loadUsers(); // Refresh table
                } else {
                    showStatus("Failed to change role", true);
                }
            } catch (SQLException e) {
                showStatus("Error changing role: " + e.getMessage(), true);
                e.printStackTrace();
            }
        } else {
            loadUsers(); // Refresh to reset ComboBox
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadUsers();
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
    
    // Row class for TableView
    public static class UserRow {
        private String username;
        private String fullName;
        private String email;
        private String role;
        
        public UserRow(String username, String fullName, String email, String role) {
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
        }
        
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
