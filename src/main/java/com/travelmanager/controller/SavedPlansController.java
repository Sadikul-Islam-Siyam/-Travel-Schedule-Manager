package com.travelmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

/**
 * Controller for viewing saved plans
 */
public class SavedPlansController {

    @FXML
    private ListView<String> savedPlansList;

    @FXML
    public void initialize() {
        // TODO: Load saved plans from data folder
        savedPlansList.getItems().add("No saved plans yet");
    }
}
