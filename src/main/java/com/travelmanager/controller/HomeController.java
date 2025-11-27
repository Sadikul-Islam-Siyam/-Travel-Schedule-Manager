package com.travelmanager.controller;

import com.travelmanager.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

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
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/create-plan.fxml"));
            Scene scene = new Scene(loader.load(), 900, 650);
            Stage stage = new Stage();
            stage.setTitle("Create New Plan");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSavedPlans() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/saved-plans.fxml"));
            Scene scene = new Scene(loader.load(), 900, 650);
            Stage stage = new Stage();
            stage.setTitle("Saved Plans");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAutomaticRoute() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/automatic-route.fxml"));
            Scene scene = new Scene(loader.load(), 900, 650);
            Stage stage = new Stage();
            stage.setTitle("Automatic Route Generation");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHelp() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/help.fxml"));
            Scene scene = new Scene(loader.load(), 700, 600);
            Stage stage = new Stage();
            stage.setTitle("Help - How to Use This App");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
