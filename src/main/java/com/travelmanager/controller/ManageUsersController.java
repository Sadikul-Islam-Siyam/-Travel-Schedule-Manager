package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.User;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ManageUsersController {
    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, String> usernameColumn;
    @FXML private TableColumn<UserRow, String> fullNameColumn;
    @FXML private TableColumn<UserRow, String> emailColumn;
    @FXML private TableColumn<UserRow, String> roleColumn;
    @FXML private Label statusLabel;
    
    private DatabaseManager databaseManager;
    private ObservableList<UserRow> usersList;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        usersList = FXCollections.observableArrayList();
        
        // Setup table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Setup role change column
        TableColumn<UserRow, Void> actionColumn = new TableColumn<>("Change Role");
        actionColumn.setPrefWidth(150);
        actionColumn.setCellFactory(param -> new TableCell<UserRow, Void>() {
            private final ComboBox<String> roleComboBox = new ComboBox<>();
            
            {
                roleComboBox.getItems().addAll("USER", "DEVELOPER", "MASTER");
                roleComboBox.setStyle("-fx-font-size: 11px;");
                roleComboBox.setOnAction(event -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    String newRole = roleComboBox.getValue();
                    if (newRole != null && !newRole.equals(user.getRole())) {
                        handleRoleChange(user, newRole);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserRow user = getTableView().getItems().get(getIndex());
                    roleComboBox.setValue(user.getRole());
                    setGraphic(roleComboBox);
                }
            }
        });
        
        usersTable.getColumns().add(actionColumn);
        usersTable.setItems(usersList);
        
        loadUsers();
    }
    
    private void loadUsers() {
        try {
            List<User> users = databaseManager.getAllUsers();
            usersList.clear();
            
            for (User user : users) {
                usersList.add(new UserRow(
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().toString()
                ));
            }
            
            statusLabel.setText("");
        } catch (SQLException e) {
            showStatus("Error loading users: " + e.getMessage(), true);
            e.printStackTrace();
        }
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
