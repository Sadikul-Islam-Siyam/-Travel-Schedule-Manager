package com.travelmanager.util;

import javafx.scene.control.Button;

/**
 * Helper class to create standardized Help buttons for all pages
 */
public class HelpButtonHelper {
    
    /**
     * Creates a standard Help button that navigates to the help page
     * @return Configured Help button
     */
    public static Button createHelpButton() {
        Button helpButton = new Button("❓ Help");
        helpButton.setStyle(
            "-fx-background-color: #95a5a6; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 8 20; " +
            "-fx-cursor: hand; " +
            "-fx-font-size: 12px; " +
            "-fx-background-radius: 5;"
        );
        
        helpButton.setOnAction(event -> NavigationManager.navigateTo("help"));
        
        return helpButton;
    }
}
