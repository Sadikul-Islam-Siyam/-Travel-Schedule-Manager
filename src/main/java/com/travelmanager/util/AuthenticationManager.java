package com.travelmanager.util;

import com.travelmanager.model.User;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.util.Duration;

/**
 * Manages user authentication session with auto-logout after 30 minutes of inactivity
 */
public class AuthenticationManager {
    
    private static AuthenticationManager instance;
    private User currentUser;
    private PauseTransition sessionTimeout;
    private static final int TIMEOUT_MINUTES = 30;
    
    private AuthenticationManager() {
        initializeSessionTimeout();
    }
    
    public static AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }
    
    private void initializeSessionTimeout() {
        sessionTimeout = new PauseTransition(Duration.minutes(TIMEOUT_MINUTES));
        sessionTimeout.setOnFinished(event -> {
            if (currentUser != null) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Session Expired");
                    alert.setHeaderText("Your session has expired");
                    alert.setContentText("You have been logged out due to " + TIMEOUT_MINUTES + " minutes of inactivity.");
                    alert.showAndWait();
                    
                    logout();
                    NavigationManager.navigateTo("login");
                });
            }
        });
    }
    
    public void login(User user) {
        this.currentUser = user;
        resetSessionTimeout();
    }
    
    public void logout() {
        this.currentUser = null;
        if (sessionTimeout != null) {
            sessionTimeout.stop();
        }
    }
    
    public void resetSessionTimeout() {
        if (currentUser != null && sessionTimeout != null) {
            sessionTimeout.playFromStart();
        }
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isDeveloper() {
        return currentUser != null && currentUser.isDeveloper();
    }
    
    public boolean canModifyData() {
        return currentUser != null && currentUser.canModifyData();
    }
    
    public boolean canDeleteData() {
        return currentUser != null && currentUser.canDeleteData();
    }
    
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }
    
    public String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }
}
