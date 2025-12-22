package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.User;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

/**
 * Controller for user login with enhanced UX features
 */
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private StackPane passwordContainer;
    @FXML private Button togglePasswordButton;
    @FXML private Label errorLabel;
    @FXML private Label infoLabel;
    @FXML private ToggleButton darkModeToggle;
    @FXML private BorderPane rootPane;
    @FXML private VBox loginCard;
    
    private boolean passwordVisible = false;
    private boolean isDarkMode = false;
    
    @FXML
    public void initialize() {
        errorLabel.setText("");
        if (infoLabel != null) {
            infoLabel.setText("");
        }
        
        // Setup password visibility toggle
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        
        // Bind text fields together
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        
        // Focus on username field
        usernameField.requestFocus();
    }
    
    @FXML
    private void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        
        if (passwordVisible) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            togglePasswordButton.setText("Hide");
        } else {
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordButton.setText("Show");
        }
        updateToggleButtonStyle();
    }
    
    private void updateToggleButtonStyle() {
        String baseStyle = isDarkMode
            ? "-fx-background-color: transparent; -fx-text-fill: #60a5fa; -fx-font-size: 12px; -fx-cursor: hand; " +
              "-fx-padding: 4 8; -fx-underline: false; -fx-font-weight: 600;"
            : "-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-font-size: 12px; -fx-cursor: hand; " +
              "-fx-padding: 4 8; -fx-underline: false; -fx-font-weight: 600;";
        
        togglePasswordButton.setStyle(baseStyle);
    }
    
    @FXML
    private void handleDarkModeToggle() {
        isDarkMode = !isDarkMode;
        
        if (isDarkMode) {
            // Apply dark mode styles with optimized colors
            rootPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #0d1117, #161b22);");
            loginCard.setStyle("-fx-background-color: #1f2937; -fx-padding: 35 40; -fx-background-radius: 15; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 25, 0, 0, 8); " +
                             "-fx-border-color: #374151; -fx-border-width: 1; -fx-border-radius: 15;");
            
            // Update all labels to light text
            updateLabelsForDarkMode(true);
            updateToggleButtonStyle();
            darkModeToggle.setText("☀");
        } else {
            // Apply light mode styles
            rootPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c3e50, #34495e);");
            loginCard.setStyle("-fx-background-color: white; -fx-padding: 35 40; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
            
            // Restore original label colors
            updateLabelsForDarkMode(false);
            updateToggleButtonStyle();
            darkModeToggle.setText("🌙");
        }
    }
    
    private void updateLabelsForDarkMode(boolean isDark) {
        // Find all labels in the login card and update their styles
        loginCard.lookupAll(".label").forEach(node -> {
            if (node instanceof javafx.scene.control.Label) {
                javafx.scene.control.Label label = (javafx.scene.control.Label) node;
                String text = label.getText();
                
                if (isDark) {
                    // Dark mode text colors
                    if (text.equals("Sign In")) {
                        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
                    } else if (text.equals("Username or Email") || text.equals("Password")) {
                        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #d1d5db; -fx-font-weight: bold;");
                    } else if (text.equals("Don't have an account?")) {
                        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
                    }
                } else {
                    // Light mode text colors (original)
                    if (text.equals("Sign In")) {
                        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    } else if (text.equals("Username or Email") || text.equals("Password")) {
                        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-weight: bold;");
                    } else if (text.equals("Don't have an account?")) {
                        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                    }
                }
            }
        });
        
        // Update text fields
        String fieldStyle = isDark 
            ? "-fx-font-size: 14px; -fx-padding: 12; -fx-border-color: #4b5563; -fx-border-radius: 5; " +
              "-fx-background-radius: 5; -fx-background-color: #111827; -fx-text-fill: #f3f4f6; " +
              "-fx-prompt-text-fill: #6b7280;"
            : "-fx-font-size: 14px; -fx-padding: 12; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5;";
        
        String passwordFieldStyle = isDark
            ? "-fx-font-size: 14px; -fx-padding: 12 80 12 12; -fx-border-color: #4b5563; -fx-border-radius: 5; " +
              "-fx-background-radius: 5; -fx-background-color: #111827; -fx-text-fill: #f3f4f6; " +
              "-fx-prompt-text-fill: #6b7280;"
            : "-fx-font-size: 14px; -fx-padding: 12 80 12 12; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5;";
        
        usernameField.setStyle(fieldStyle);
        passwordField.setStyle(passwordFieldStyle);
        passwordTextField.setStyle(passwordFieldStyle);
        
        // Update hyperlinks
        loginCard.lookupAll(".hyperlink").forEach(node -> {
            if (node instanceof javafx.scene.control.Hyperlink) {
                javafx.scene.control.Hyperlink link = (javafx.scene.control.Hyperlink) node;
                String linkText = link.getText();
                
                if (isDark) {
                    if (linkText.equals("Create Account")) {
                        link.setStyle("-fx-font-size: 12px; -fx-text-fill: #60a5fa; -fx-font-weight: bold; -fx-underline: true;");
                    } else if (linkText.contains("help")) {
                        link.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af; -fx-underline: true;");
                    }
                } else {
                    if (linkText.equals("Create Account")) {
                        link.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-font-weight: bold; -fx-underline: true;");
                    } else if (linkText.contains("help")) {
                        link.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-underline: true;");
                    }
                }
            }
        });
        
        // Update Sign In button
        loginCard.lookupAll(".button").forEach(node -> {
            if (node instanceof javafx.scene.control.Button) {
                javafx.scene.control.Button button = (javafx.scene.control.Button) node;
                if (button.getText().equals("Sign In")) {
                    if (isDark) {
                        button.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 16px; " +
                                      "-fx-font-weight: bold; -fx-padding: 12 40; -fx-cursor: hand; -fx-background-radius: 8; " +
                                      "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.4), 8, 0, 0, 3);");
                    } else {
                        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; " +
                                      "-fx-font-weight: bold; -fx-padding: 12 40; -fx-cursor: hand; -fx-background-radius: 8; " +
                                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
                    }
                }
            }
        });
    }
    
    @FXML
    private void handleLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();
        
        clearMessages();
        
        // Enhanced validation with specific feedback
        if (usernameOrEmail.isEmpty() && password.isEmpty()) {
            showError("⚠ Please enter your username and password to continue");
            usernameField.requestFocus();
            return;
        }
        
        if (usernameOrEmail.isEmpty()) {
            showError("⚠ Username or email is required. Please enter your credentials.");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("⚠ Password is required. Please enter your password.");
            if (passwordVisible) {
                passwordTextField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            return;
        }
        
        if (usernameOrEmail.length() < 3) {
            showError("⚠ Username must be at least 3 characters long. Please check your input.");
            usernameField.requestFocus();
            return;
        }
        
        try {
            // Check if account is pending approval
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            if (dbManager.isPendingUser(usernameOrEmail)) {
                showError("⏳ Account Pending Approval");
                showInfo("💡 Your account is awaiting approval from the system administrator. This typically takes 24-48 hours.\n✉ Contact support if urgent, or try logging in as Master to self-approve.");
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
                    showError("❌ Incorrect Password");
                    showInfo("💡 The password you entered is incorrect. Please try again.\n🔒 Passwords are case-sensitive. Check if Caps Lock is on.");
                    showErrorAlert("Incorrect Password", "The password you entered is incorrect.\n\n" +
                            "Please try again.\n" +
                            "Note: Passwords are case-sensitive. Check if Caps Lock is on.");
                } else {
                    dbManager.logActivity(usernameOrEmail, "LOGIN_FAILED", "Account not found", false);
                    showError("❌ Account Not Found");
                    showInfo("💡 No account exists with this username or email.\n✨ Click 'Create Account' below to register.\n⏳ New accounts require Master approval before first login.");
                    showErrorAlert("Account Not Found", "No account exists with this username or email.\n\n" +
                            "Click 'Create Account' below to register.\n" +
                            "Note: New accounts require Master approval before first login.");
                }
                passwordField.clear();
                passwordTextField.clear();
            }
            
        } catch (SQLException e) {
            showError("⚠ Database Connection Error");
            showInfo("💡 Unable to connect to the database. Please check:\n• Database file exists in 'data' folder\n• No other instances are accessing the database\n• You have proper read/write permissions");
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
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Failed");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
