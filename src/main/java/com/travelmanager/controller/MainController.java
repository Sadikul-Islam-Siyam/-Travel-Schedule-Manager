package com.travelmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Main controller for the application
 */
public class MainController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Travel Schedule Manager");
    }
}
