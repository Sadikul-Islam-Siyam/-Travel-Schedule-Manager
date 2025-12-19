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
    
    @FXML
    public void initialize() {
        errorLabel.setText("");
        // Focus on username field
        usernameField.requestFocus();
    }
    
    @FXML
    private void handleLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }
        
        try {
            // Authenticate user
            DatabaseManager dbManager = DatabaseManager.getInstance();
            User user = dbManager.authenticateUser(usernameOrEmail, password);
            
            if (user != null) {
                // Login successful
                AuthenticationManager.getInstance().login(user);
                
                // Navigate to home page
                NavigationManager.navigateTo("home");
            } else {
                // Login failed
                showError("Invalid username or password");
                passwordField.clear();
            }
            
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
    }
}
