package com.travelmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for automatic route generation
 */
public class AutomaticRouteController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("Automatic route generation will be implemented soon");
    }
    
    @FXML
    private void handleBack() {
        com.travelmanager.util.NavigationManager.goBack();
    }
}
