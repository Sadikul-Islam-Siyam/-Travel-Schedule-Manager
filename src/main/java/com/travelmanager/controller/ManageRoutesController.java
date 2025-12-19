package com.travelmanager.controller;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.database.DatabaseManager.RouteData;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ManageRoutesController {
    @FXML private TableView<RouteRow> routesTable;
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;
    @FXML private VBox formPanel;
    @FXML private Label formTitle;
    @FXML private TextField routeNameField;
    @FXML private TextField originField;
    @FXML private TextField destinationField;
    @FXML private ComboBox<String> transportTypeCombo;
    @FXML private TextField durationField;
    @FXML private TextField priceField;
    @FXML private TextField scheduleTimeField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea metadataField;
    @FXML private Label formErrorLabel;
    @FXML private Button saveButton;
    
    private DatabaseManager databaseManager;
    private ObservableList<RouteRow> routesList;
    private RouteRow editingRoute = null;
    
    @FXML
    public void initialize() {
        databaseManager = DatabaseManager.getInstance();
        routesList = FXCollections.observableArrayList();
        
        statusLabel.setText("");
        
        // Set default values for combos
        transportTypeCombo.getSelectionModel().selectFirst();
        statusCombo.getSelectionModel().select("ACTIVE");
        
        loadRoutes();
    }
    
    private void loadRoutes() {
        try {
            List<RouteData> routes = databaseManager.getAllRoutes();
            routesList.clear();
            
            for (RouteData route : routes) {
                routesList.add(new RouteRow(
                    route.getId(),
                    route.getRouteName(),
                    route.getOrigin(),
                    route.getDestination(),
                    route.getTransportType(),
                    route.getDurationMinutes(),
                    route.getPrice(),
                    route.getStatus()
                ));
            }
            
            routesTable.setItems(routesList);
            emptyLabel.setVisible(routesList.isEmpty());
            setupActionsColumn();
            
        } catch (SQLException e) {
            showStatus("Error loading routes: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    private void setupActionsColumn() {
        @SuppressWarnings("unchecked")
        TableColumn<RouteRow, Void> actionsColumn = (TableColumn<RouteRow, Void>) routesTable.getColumns().get(8);
        
        actionsColumn.setCellFactory(new Callback<TableColumn<RouteRow, Void>, TableCell<RouteRow, Void>>() {
            @Override
            public TableCell<RouteRow, Void> call(TableColumn<RouteRow, Void> param) {
                return new TableCell<RouteRow, Void>() {
                    private final Button editBtn = new Button("✏ Edit");
                    private final Button deleteBtn = new Button("🗑 Delete");
                    private final HBox container = new HBox(5, editBtn, deleteBtn);
                    
                    {
                        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-cursor: hand; -fx-background-radius: 4;");
                        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-cursor: hand; -fx-background-radius: 4;");
                        container.setAlignment(Pos.CENTER);
                        
                        editBtn.setOnAction(event -> {
                            RouteRow route = getTableView().getItems().get(getIndex());
                            handleEditRoute(route);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            RouteRow route = getTableView().getItems().get(getIndex());
                            handleDeleteRoute(route);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                };
            }
        });
    }
    
    @FXML
    private void handleAddRoute() {
        editingRoute = null;
        formTitle.setText("Add New Route");
        saveButton.setText("Save Route");
        clearForm();
        showForm();
    }
    
    private void handleEditRoute(RouteRow route) {
        try {
            editingRoute = route;
            formTitle.setText("Edit Route");
            saveButton.setText("Update Route");
            
            // Load route data from database
            List<RouteData> routes = databaseManager.getAllRoutes();
            for (RouteData rd : routes) {
                if (rd.getId() == route.getId()) {
                    routeNameField.setText(rd.getRouteName());
                    originField.setText(rd.getOrigin());
                    destinationField.setText(rd.getDestination());
                    transportTypeCombo.setValue(rd.getTransportType());
                    durationField.setText(String.valueOf(rd.getDurationMinutes()));
                    priceField.setText(String.format("%.2f", rd.getPrice()));
                    scheduleTimeField.setText(rd.getScheduleTime() != null ? rd.getScheduleTime() : "");
                    statusCombo.setValue(rd.getStatus());
                    metadataField.setText(rd.getMetadata() != null ? rd.getMetadata() : "");
                    break;
                }
            }
            
            showForm();
        } catch (SQLException e) {
            showStatus("Error loading route data: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    private void handleDeleteRoute(RouteRow route) {
        AuthenticationManager auth = AuthenticationManager.getInstance();
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Route");
        confirmAlert.setHeaderText("Delete route: " + route.getRouteName() + "?");
        
        if (auth.getCurrentUser().isMaster()) {
            confirmAlert.setContentText("This action cannot be undone.");
        } else {
            confirmAlert.setContentText("This will submit a deletion request for master approval.");
        }
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success;
            
            if (auth.getCurrentUser().isMaster()) {
                // Master: Direct deletion
                success = databaseManager.deleteRoute(route.getId());
                if (success) {
                    showStatus("Route deleted successfully: " + route.getRouteName(), false);
                    loadRoutes();
                } else {
                    showStatus("Failed to delete route", true);
                }
            } else {
                // Developer: Submit deletion request
                success = databaseManager.submitPendingRoute(
                    route.getRouteName(), route.getOrigin(), route.getDestination(),
                    route.getTransportType(), route.getDurationMinutes(), route.getPrice(),
                    "", "", "DELETE", route.getId(), auth.getCurrentUser().getUsername(),
                    "Route deletion request"
                );
                if (success) {
                    showStatus("Deletion request submitted: " + route.getRouteName(), false);
                } else {
                    showStatus("Failed to submit deletion request", true);
                }
            }
        }
    }
    
    @FXML
    private void handleSaveRoute() {
        formErrorLabel.setText("");
        
        // Validate inputs
        String routeName = routeNameField.getText().trim();
        String origin = originField.getText().trim();
        String destination = destinationField.getText().trim();
        String transportType = transportTypeCombo.getValue();
        String durationStr = durationField.getText().trim();
        String priceStr = priceField.getText().trim();
        String scheduleTime = scheduleTimeField.getText().trim();
        String status = statusCombo.getValue();
        String metadata = metadataField.getText().trim();
        
        if (routeName.isEmpty() || origin.isEmpty() || destination.isEmpty() || 
            transportType == null || durationStr.isEmpty() || priceStr.isEmpty()) {
            formErrorLabel.setText("Please fill in all required fields (*)");
            return;
        }
        
        int duration;
        double price;
        try {
            duration = Integer.parseInt(durationStr);
            price = Double.parseDouble(priceStr);
            
            if (duration <= 0 || price < 0) {
                formErrorLabel.setText("Duration must be positive, price cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            formErrorLabel.setText("Invalid duration or price format");
            return;
        }
        
        boolean success;
        AuthenticationManager auth = AuthenticationManager.getInstance();
        
        if (editingRoute == null) {
            // Create new route
            if (auth.getCurrentUser().isMaster()) {
                // Master: Direct to live routes table
                success = databaseManager.createRoute(routeName, origin, destination, transportType,
                                                     duration, price, scheduleTime, metadata);
                if (success) {
                    showStatus("Route created successfully: " + routeName, false);
                } else {
                    showStatus("Failed to create route", true);
                }
            } else {
                // Developer: Submit for approval
                success = databaseManager.submitPendingRoute(routeName, origin, destination, transportType,
                                                            duration, price, scheduleTime, metadata, 
                                                            "CREATE", null, auth.getCurrentUser().getUsername(),
                                                            "New route submission");
                if (success) {
                    showStatus("Route submitted for approval: " + routeName, false);
                } else {
                    showStatus("Failed to submit route", true);
                }
            }
        } else {
            // Update existing route
            if (auth.getCurrentUser().isMaster()) {
                // Master: Direct update to live routes
                success = databaseManager.updateRoute(editingRoute.getId(), routeName, origin, destination,
                                                     transportType, duration, price, scheduleTime, metadata, status);
                if (success) {
                    showStatus("Route updated successfully: " + routeName, false);
                } else {
                    showStatus("Failed to update route", true);
                }
            } else {
                // Developer: Submit update for approval
                success = databaseManager.submitPendingRoute(routeName, origin, destination, transportType,
                                                            duration, price, scheduleTime, metadata,
                                                            "UPDATE", editingRoute.getId(), 
                                                            auth.getCurrentUser().getUsername(),
                                                            "Route update request");
                if (success) {
                    showStatus("Update submitted for approval: " + routeName, false);
                } else {
                    showStatus("Failed to submit update", true);
                }
            }
        }
        
        if (success) {
            loadRoutes();
            handleCloseForm();
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadRoutes();
        showStatus("Routes list refreshed", false);
    }
    
    @FXML
    private void handleBack() {
        NavigationManager.navigateTo("home");
    }
    
    @FXML
    private void handleCloseForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        clearForm();
    }
    
    private void showForm() {
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        formErrorLabel.setText("");
    }
    
    private void clearForm() {
        routeNameField.clear();
        originField.clear();
        destinationField.clear();
        transportTypeCombo.getSelectionModel().selectFirst();
        durationField.clear();
        priceField.clear();
        scheduleTimeField.clear();
        statusCombo.getSelectionModel().select("ACTIVE");
        metadataField.clear();
        formErrorLabel.setText("");
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + 
                           (isError ? "#e74c3c" : "#27ae60") + ";");
    }
    
    // Row class for TableView
    public static class RouteRow {
        private int id;
        private String routeName;
        private String origin;
        private String destination;
        private String transportType;
        private int durationMinutes;
        private double price;
        private String status;
        
        public RouteRow(int id, String routeName, String origin, String destination,
                       String transportType, int durationMinutes, double price, String status) {
            this.id = id;
            this.routeName = routeName;
            this.origin = origin;
            this.destination = destination;
            this.transportType = transportType;
            this.durationMinutes = durationMinutes;
            this.price = price;
            this.status = status;
        }
        
        public int getId() { return id; }
        public String getRouteName() { return routeName; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getTransportType() { return transportType; }
        public int getDurationMinutes() { return durationMinutes; }
        public double getPrice() { return price; }
        public String getStatus() { return status; }
    }
}
