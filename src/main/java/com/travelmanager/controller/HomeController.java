package com.travelmanager.controller;

import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the home page with three main sections
 */
public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;

    @FXML
    public void initialize() {
        // Display user information
        AuthenticationManager auth = AuthenticationManager.getInstance();
        if (auth.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + auth.getCurrentUserFullName() + "!");
            roleLabel.setText(auth.isDeveloper() ? "Role: Developer" : "Role: User");
        }
    }

    @FXML
    private void handleCreatePlan() {
        NavigationManager.navigateTo("create-plan");
    }

    @FXML
    private void handleSavedPlans() {
        NavigationManager.navigateTo("saved-plans");
    }

    @FXML
    private void handleAutomaticRoute() {
        NavigationManager.navigateTo("automatic-route");
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
}
