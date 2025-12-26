package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

/**
 * Controller for the home page with three main sections
 */
public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private HBox masterSection;
    @FXML private VBox developerSection;
    @FXML private HBox regularUserSection;
    @FXML private VBox statsSection;
    @FXML private Label totalUsersLabel;
    @FXML private Label pendingAccountsLabel;
    @FXML private Label recentApprovalsLabel;
    @FXML private Label failedLoginsLabel;
    @FXML private Label todayLoginsLabel;
    @FXML private Label notificationBadge;

    @FXML
    public void initialize() {
        // Display user information
        AuthenticationManager auth = AuthenticationManager.getInstance();
        if (auth.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + auth.getCurrentUserFullName() + "!");
            
            // Display role with proper formatting
            String roleText = "Role: ";
            if (auth.getCurrentUser().isMaster()) {
                roleText += "Master";
                // Show master-only section, hide other sections
                if (masterSection != null) {
                    masterSection.setVisible(true);
                    masterSection.setManaged(true);
                }
                if (developerSection != null) {
                    developerSection.setVisible(false);
                    developerSection.setManaged(false);
                }
                if (regularUserSection != null) {
                    regularUserSection.setVisible(false);
                    regularUserSection.setManaged(false);
                }
                // Load statistics
                loadStatistics();
            } else if (auth.isDeveloper()) {
                roleText += "Developer";
                // Show developer section, hide other sections
                if (developerSection != null) {
                    developerSection.setVisible(true);
                    developerSection.setManaged(true);
                }
                if (masterSection != null) {
                    masterSection.setVisible(false);
                    masterSection.setManaged(false);
                }
                if (regularUserSection != null) {
                    regularUserSection.setVisible(false);
                    regularUserSection.setManaged(false);
                }
                // Load notification count for developers
                updateNotificationBadge();
            } else {
                roleText += "User";
                // Show regular user section (default behavior)
                if (regularUserSection != null) {
                    regularUserSection.setVisible(true);
                    regularUserSection.setManaged(true);
                }
                if (masterSection != null) {
                    masterSection.setVisible(false);
                    masterSection.setManaged(false);
                }
                if (developerSection != null) {
                    developerSection.setVisible(false);
                    developerSection.setManaged(false);
                }
            }
            roleLabel.setText(roleText);
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
    
    @FXML
    private void handlePendingAccounts() {
        NavigationManager.navigateTo("account-approval");
    }
    
    @FXML
    private void handleApiApproval() {
        NavigationManager.navigateTo("api-approval");
    }
    
    @FXML
    private void handleManageRoutes() {
        NavigationManager.navigateTo("manage-routes");
    }
    
    @FXML
    private void handleManageUsers() {
        NavigationManager.navigateTo("manage-users");
    }
    
    @FXML
    private void handleMyPendingRequests() {
        NavigationManager.navigateTo("my-pending-requests");
    }
    
    @FXML
    private void handleApiTesting() {
        NavigationManager.navigateTo("api-testing-tool");
    }
    
    @FXML
    private void handleManageSchedules() {
        NavigationManager.navigateTo("manage-schedules");
    }
    
    @FXML
    private void handleRequestHistory() {
        NavigationManager.navigateTo("request-history");
    }
    
    private void updateNotificationBadge() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            String username = AuthenticationManager.getInstance().getCurrentUsername();
            int count = dbManager.getUnreadNotificationsCount(username);
            
            if (notificationBadge != null) {
                if (count > 0) {
                    notificationBadge.setText(String.valueOf(count));
                    notificationBadge.setVisible(true);
                    notificationBadge.setManaged(true);
                } else {
                    notificationBadge.setVisible(false);
                    notificationBadge.setManaged(false);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading notifications: " + e.getMessage());
        }
    }
    
    private void loadStatistics() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            DatabaseManager.SystemStatistics stats = dbManager.getSystemStatistics();
            
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(stats.totalUsers));
            if (pendingAccountsLabel != null) pendingAccountsLabel.setText(String.valueOf(stats.pendingAccounts));
            if (recentApprovalsLabel != null) recentApprovalsLabel.setText(String.valueOf(stats.recentApprovals));
            if (failedLoginsLabel != null) failedLoginsLabel.setText(String.valueOf(stats.failedLogins));
            if (todayLoginsLabel != null) todayLoginsLabel.setText(String.valueOf(stats.todayLogins));
            
            // Show stats section
            if (statsSection != null) {
                statsSection.setVisible(true);
                statsSection.setManaged(true);
            }
        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }
}
