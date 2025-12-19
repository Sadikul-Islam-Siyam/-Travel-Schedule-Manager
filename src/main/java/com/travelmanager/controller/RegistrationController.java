package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.User;
import com.travelmanager.util.NavigationManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegistrationController {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    
    private DatabaseManager databaseManager;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        
        // Populate role combo box
        roleComboBox.setItems(FXCollections.observableArrayList("USER", "DEVELOPER"));
        roleComboBox.getSelectionModel().selectFirst();
        
        // Clear labels initially
        errorLabel.setText("");
        successLabel.setText("");
    }
    
    @FXML
    private void handleRegister() {
        System.out.println("=== handleRegister called ===");
        clearMessages();
        
        // Validate inputs
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();
        
        System.out.println("Full Name: " + fullName);
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Role: " + role);
        
        // Validation checks
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            System.out.println("Validation failed: Empty fields");
            showError("Please fill in all fields.");
            return;
        }
        
        if (username.length() < 3) {
            System.out.println("Validation failed: Username too short");
            showError("Username must be at least 3 characters long.");
            return;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("Validation failed: Invalid email");
            showError("Please enter a valid email address.");
            return;
        }
        
        if (password.length() < 6) {
            System.out.println("Validation failed: Password too short");
            showError("Password must be at least 6 characters long.");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            System.out.println("Validation failed: Passwords don't match");
            showError("Passwords do not match.");
            return;
        }
        
        System.out.println("Checking if user exists...");
        // Check if username already exists
        try {
            if (databaseManager.userExists(username)) {
                System.out.println("User already exists in users table");
                showError("Username already exists.");
                return;
            }
            
            if (databaseManager.isPendingUser(username)) {
                System.out.println("User pending approval");
                showError("Username pending approval.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Error checking user existence: " + e.getMessage());
            e.printStackTrace();
            showError("Error checking username: " + e.getMessage());
            return;
        }
        
        System.out.println("Creating pending user...");
        // Create pending user
        try {
            User.Role userRole = User.Role.valueOf(role);
            boolean success = databaseManager.createPendingUser(username, email, password, userRole, fullName);
            
            System.out.println("Create pending user result: " + success);
            
            if (success) {
                showSuccess("Account created successfully! Awaiting master approval.");
                clearFields();
            } else {
                showError("Failed to create account. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Exception creating account: " + e.getMessage());
            showError("Error creating account: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== handleRegister completed ===");
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.navigateTo("login");
    }
    
    @FXML
    private void handleHelp() {
        NavigationManager.navigateTo("help");
    }
    
    private void clearFields() {
        fullNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        roleComboBox.getSelectionModel().selectFirst();
    }
    
    private void clearMessages() {
        errorLabel.setText("");
        successLabel.setText("");
    }
    
    private void showError(String message) {
        System.out.println("showError called with: " + message);
        
        // Show alert dialog
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
        
        // Also update label
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        if (successLabel != null) {
            successLabel.setText("");
            successLabel.setVisible(false);
            successLabel.setManaged(false);
        }
    }
    
    private void showSuccess(String message) {
        System.out.println("showSuccess called with: " + message);
        
        // Show success dialog
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Account Created!");
            alert.setContentText(message);
            alert.showAndWait();
        });
        
        // Also update label
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
            successLabel.setManaged(true);
        }
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}
