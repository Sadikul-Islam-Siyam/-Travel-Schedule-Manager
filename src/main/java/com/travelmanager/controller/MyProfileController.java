package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.User;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

/**
 * Controller for the My Profile page where users can edit their personal information
 */
public class MyProfileController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label accountTypeLabel;
    @FXML private Label userIdLabel;

    @FXML
    public void initialize() {
        // Load current user information
        AuthenticationManager auth = AuthenticationManager.getInstance();
        if (auth.isLoggedIn()) {
            User currentUser = auth.getCurrentUser();
            
            // Set header labels
            welcomeLabel.setText("Welcome, " + currentUser.getFullName() + "!");
            String roleText = "Role: ";
            if (currentUser.isMaster()) {
                roleText += "Master";
            } else if (currentUser.isDeveloper()) {
                roleText += "Developer";
            } else {
                roleText += "User";
            }
            roleLabel.setText(roleText);
            
            // Populate form fields
            fullNameField.setText(currentUser.getFullName());
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            
            // Set account info
            accountTypeLabel.setText(currentUser.getRole().toString());
            userIdLabel.setText(String.valueOf(currentUser.getId()));
            
            // Clear status
            statusLabel.setText("");
        }
    }

    @FXML
    private void handleSaveProfile() {
        AuthenticationManager auth = AuthenticationManager.getInstance();
        if (!auth.isLoggedIn()) {
            showError("You must be logged in to update your profile.");
            return;
        }

        User currentUser = auth.getCurrentUser();
        String newFullName = fullNameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate full name
        if (newFullName.isEmpty()) {
            showError("Full name cannot be empty.");
            return;
        }

        // Validate email
        if (newEmail.isEmpty()) {
            showError("Email cannot be empty.");
            return;
        }

        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address.");
            return;
        }

        // Check if password change is requested
        boolean changePassword = !newPassword.isEmpty() || !confirmPassword.isEmpty();
        
        if (changePassword) {
            // Validate current password is provided
            if (currentPassword.isEmpty()) {
                showError("Please enter your current password to change your password.");
                return;
            }

            // Validate new password
            if (newPassword.length() < 6) {
                showError("New password must be at least 6 characters long.");
                return;
            }

            // Validate password confirmation
            if (!newPassword.equals(confirmPassword)) {
                showError("New passwords do not match.");
                return;
            }
        }

        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            // Verify current password if changing password
            if (changePassword) {
                User verifiedUser = dbManager.authenticateUser(currentUser.getUsername(), currentPassword);
                if (verifiedUser == null) {
                    showError("Current password is incorrect.");
                    return;
                }
            }

            // Update profile
            boolean success;
            if (changePassword) {
                success = dbManager.updateUserProfile(currentUser.getId(), newFullName, newEmail, newPassword);
            } else {
                success = dbManager.updateUserProfile(currentUser.getId(), newFullName, newEmail, null);
            }

            if (success) {
                // Update the current user object in AuthenticationManager
                currentUser.setFullName(newFullName);
                currentUser.setEmail(newEmail);
                
                // Update header labels
                welcomeLabel.setText("Welcome, " + newFullName + "!");
                
                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                
                showSuccess("Profile updated successfully!");
            } else {
                showError("Failed to update profile. Email might already be in use.");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        NavigationManager.navigateTo("home");
    }

    @FXML
    private void handleHelp() {
        NavigationManager.navigateTo("help");
    }

    @FXML
    private void handleLogout() {
        AuthenticationManager.getInstance().logout();
        NavigationManager.navigateTo("login");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2ecc71;");
    }
}
