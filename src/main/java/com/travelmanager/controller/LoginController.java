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
 * Controller for user login
 */
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Label infoLabel;
    
    @FXML
    public void initialize() {
        errorLabel.setText("");
        if (infoLabel != null) {
            infoLabel.setText("");
        }
        // Focus on username field
        usernameField.requestFocus();
    }
    
    @FXML
    private void handleLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();
        
        clearMessages();
        
        // Validation
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showError("⚠ Please enter both username and password");
            return;
        }
        
        try {
            // Check if account is pending approval
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            if (dbManager.isPendingUser(usernameOrEmail)) {
                showError("⏳ Account pending approval. Please wait for Master to approve your account.");
                showInfo("💡 Tip: Contact the system administrator or login as Master to approve accounts.");
                return;
            }
            
            // Authenticate user
            User user = dbManager.authenticateUser(usernameOrEmail, password);
            
            if (user != null) {
                // Login successful
                dbManager.logActivity(user.getUsername(), "LOGIN_SUCCESS", "User logged in successfully", true);
                AuthenticationManager.getInstance().login(user);
                
                // Navigate to home page
                NavigationManager.navigateTo("home");
            } else {
                // Check if username exists but password is wrong
                if (dbManager.userExists(usernameOrEmail)) {
                    dbManager.logActivity(usernameOrEmail, "LOGIN_FAILED", "Invalid password", false);
                    showError("❌ Invalid password. Please try again.");
                } else {
                    dbManager.logActivity(usernameOrEmail, "LOGIN_FAILED", "Account not found", false);
                    showError("❌ Account not found. Please check your username or create a new account.");
                    showInfo("💡 New users must create an account and wait for Master approval.");
                }
                passwordField.clear();
            }
            
        } catch (SQLException e) {
            showError("⚠ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCreateAccount() {
        NavigationManager.navigateTo("register");
    }
    
    @FXML
    private void handleHelp() {
        NavigationManager.navigateTo("help");
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void showInfo(String message) {
        if (infoLabel != null) {
            infoLabel.setText(message);
            infoLabel.setVisible(true);
        }
    }
    
    private void clearMessages() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        if (infoLabel != null) {
            infoLabel.setText("");
            infoLabel.setVisible(false);
        }
    }
}
