package com.travelmanager.controller;

import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;

/**
 * Controller for the home page with three main sections
 */
public class HomeController {

    @FXML
    public void initialize() {
        // Initialize home page
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
}
