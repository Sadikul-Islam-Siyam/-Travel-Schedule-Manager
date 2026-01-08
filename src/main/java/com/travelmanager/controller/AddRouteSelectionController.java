package com.travelmanager.controller;

import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;

public class AddRouteSelectionController {
    
    @FXML
    private void handleBack() {
        NavigationManager.navigateTo("manage-routes");
    }
    
    @FXML
    private void handleAddBus() {
        NavigationManager.navigateTo("add-bus-route");
    }
    
    @FXML
    private void handleAddTrain() {
        NavigationManager.navigateTo("add-train-route");
    }
}
