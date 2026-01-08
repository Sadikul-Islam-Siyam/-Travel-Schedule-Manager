package com.travelmanager.controller;

import com.travelmanager.api.ScheduleDataManager;
import com.travelmanager.database.DatabaseManager;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.util.AuthenticationManager;
import com.travelmanager.util.NavigationManager;
import com.travelmanager.util.AutoCompletePopup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.Arrays;
import java.util.List;

public class ManageRoutesController {
    @FXML private TableView<RouteRow> routesTable;
    @FXML private TableColumn<RouteRow, Integer> idColumn;
    @FXML private TableColumn<RouteRow, String> routeNameColumn;
    @FXML private TableColumn<RouteRow, String> originColumn;
    @FXML private TableColumn<RouteRow, String> destinationColumn;
    @FXML private TableColumn<RouteRow, String> typeColumn;
    @FXML private TableColumn<RouteRow, Integer> durationColumn;
    @FXML private TableColumn<RouteRow, Double> priceColumn;
    @FXML private TableColumn<RouteRow, String> statusColumn;
    @FXML private TableColumn<RouteRow, Void> actionsColumn;
    
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;
    @FXML private Button showAllBtn;
    @FXML private Button showBusBtn;
    @FXML private Button showTrainBtn;
    @FXML private TextField searchField;
    @FXML private VBox formPanel;
    @FXML private Label formTitle;
    @FXML private TextField routeNameField;
    @FXML private TextField originField;
    @FXML private TextField destinationField;
    @FXML private ComboBox<String> transportTypeCombo;
    @FXML private TextField durationField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea metadataField;
    @FXML private TextField departureTimeField;
    @FXML private TextField arrivalTimeField;
    @FXML private TextArea notesField;
    @FXML private Label formErrorLabel;
    @FXML private Button saveButton;
    
    private ScheduleDataManager dataManager;
    private DatabaseManager databaseManager;
    private ObservableList<RouteRow> routesList;
    private ObservableList<RouteRow> filteredRoutesList;
    private RouteRow editingRoute;
    private boolean isEditMode = false;
    private String currentFilter = "ALL"; // ALL, BUS, TRAIN
    // AutoComplete popups for origin and destination fields
    @SuppressWarnings("unused")
    private AutoCompletePopup originAutoComplete;
    @SuppressWarnings("unused")
    private AutoCompletePopup destinationAutoComplete;
    @SuppressWarnings("unused")
    private AutoCompletePopup searchAutoComplete;
    private List<String> allLocations;
    
    @FXML
    public void initialize() {
        System.out.println("ManageRoutesController: Initializing...");
        dataManager = ScheduleDataManager.getInstance();
        databaseManager = DatabaseManager.getInstance();
        routesList = FXCollections.observableArrayList();
        filteredRoutesList = FXCollections.observableArrayList();
        
        statusLabel.setText("");
        
        // Setup search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }
        
        // Initialize locations list - All 64 districts of Bangladesh
        allLocations = Arrays.asList(
            "Barguna", "Barishal", "Bhola", "Jhalokati", "Patuakhali", "Pirojpur",
            "Bandarban", "Brahmanbaria", "Chandpur", "Chattogram", "Chittagong", "Cox's Bazar", "Cumilla", "Feni", "Khagrachari", "Lakshmipur", "Noakhali", "Rangamati",
            "Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur", "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail",
            "Bagerhat", "Chuadanga", "Jashore", "Jhenaidah", "Khulna", "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira",
            "Jamalpur", "Mymensingh", "Netrokona", "Sherpur",
            "Bogura", "Joypurhat", "Naogaon", "Natore", "Chapai Nawabganj", "Pabna", "Rajshahi", "Sirajganj",
            "Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari", "Panchagarh", "Rangpur", "Thakurgaon",
            "Habiganj", "Moulvibazar", "Sunamganj", "Sylhet"
        );
        
        // Setup autocomplete for origin and destination fields
        setupAutoComplete();
        
        // Initialize ComboBoxes
        transportTypeCombo.getItems().addAll("BUS", "TRAIN");
        statusCombo.getItems().addAll("ACTIVE", "INACTIVE", "MAINTENANCE");
        
        // Setup table columns
        setupTableColumns();
        
        // Set default values for combos
        transportTypeCombo.getSelectionModel().selectFirst();
        statusCombo.getSelectionModel().select("ACTIVE");
        
        loadRoutes();
    }
    
    private void setupAutoComplete() {
        // Setup autocomplete for search field immediately
        if (searchField != null) {
            searchAutoComplete = new AutoCompletePopup(searchField, allLocations);
        }
        
        // Setup autocomplete after form fields are initialized
        // The autocomplete will activate when the form is shown
        formPanel.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible && originField != null && destinationField != null) {
                if (originAutoComplete == null) {
                    originAutoComplete = new AutoCompletePopup(originField, allLocations);
                }
                if (destinationAutoComplete == null) {
                    destinationAutoComplete = new AutoCompletePopup(destinationField, allLocations);
                }
            }
        });
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(data -> data.getValue().idProperty.asObject());
        routeNameColumn.setCellValueFactory(data -> data.getValue().routeNameProperty);
        originColumn.setCellValueFactory(data -> data.getValue().originProperty);
        destinationColumn.setCellValueFactory(data -> data.getValue().destinationProperty);
        typeColumn.setCellValueFactory(data -> data.getValue().transportTypeProperty);
        durationColumn.setCellValueFactory(data -> data.getValue().durationMinutesProperty.asObject());
        priceColumn.setCellValueFactory(data -> data.getValue().priceProperty.asObject());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty);
    }
    
    private void loadRoutes() {
        System.out.println("ManageRoutesController: Loading routes from REST API...");
        routesList.clear();
        
        // Load bus schedules from REST API
        List<BusSchedule> busSchedules = dataManager.getAllBusSchedules();
        System.out.println("ManageRoutesController: Loaded " + busSchedules.size() + " bus schedules");
        int id = 1;
        for (BusSchedule bus : busSchedules) {
            long duration = java.time.Duration.between(bus.getDepartureTime(), bus.getArrivalTime()).toMinutes();
            routesList.add(new RouteRow(
                id++,
                bus.getId(),
                bus.getOrigin(),
                bus.getDestination(),
                "BUS",
                (int) duration,
                bus.getFare(),
                "ACTIVE"
            ));
        }
        
        // Load train schedules from REST API
        List<TrainSchedule> trainSchedules = dataManager.getAllTrainSchedules();
        System.out.println("ManageRoutesController: Loaded " + trainSchedules.size() + " train schedules");
        for (TrainSchedule train : trainSchedules) {
            long duration = java.time.Duration.between(train.getDepartureTime(), train.getArrivalTime()).toMinutes();
            routesList.add(new RouteRow(
                id++,
                train.getId(),
                train.getOrigin(),
                train.getDestination(),
                "TRAIN",
                (int) duration,
                train.getFare(),
                "ACTIVE"
            ));
        }
        
        System.out.println("ManageRoutesController: Total routes loaded: " + routesList.size());
        applyFilters();
        setupActionsColumn();
    }
    
    private void setupActionsColumn() {
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
        // Navigate to route selection screen
        NavigationManager.navigateTo("add-route-selection");
    }
    
    private void handleEditRoute(RouteRow route) {
        isEditMode = true;
        editingRoute = route;
        formTitle.setText("Edit Route: " + route.getRouteName());
        saveButton.setText("Submit Edit for Approval");
        
        // Populate form with current data
        routeNameField.setText(route.getRouteName());
        originField.setText(route.getOrigin());
        destinationField.setText(route.getDestination());
        transportTypeCombo.setValue(route.getTransportType());
        durationField.setText(String.valueOf(route.getDurationMinutes()));
        priceField.setText(String.valueOf(route.getPrice()));
        statusCombo.setValue(route.getStatus());
        
        // Set times in 24-hour format from route data
        departureTimeField.setText("08:00"); // Default value in 24-hour format (HH:mm)
        arrivalTimeField.setText(""); // Will be calculated
        
        metadataField.setText("");
        notesField.setText("");
        
        showForm();
    }
    
    private void handleDeleteRoute(RouteRow route) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Route");
        confirmAlert.setHeaderText("Submit deletion request for: " + route.getRouteName());
        confirmAlert.setContentText("This will submit a deletion request to the master for approval.\n\n" +
                                   "Route Details:\n" +
                                   "Type: " + route.getTransportType() + "\n" +
                                   "Route: " + route.getOrigin() + " → " + route.getDestination() + "\n" +
                                   "Price: ৳" + String.format("%.2f", route.getPrice()) + "\n\n" +
                                   "Do you want to continue?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String currentUser = AuthenticationManager.getInstance().getCurrentUsername();
                    
                    // Submit deletion request - find the original route ID from REST API
                    // For now, use route name as identifier
                    boolean submitted = databaseManager.submitPendingRoute(
                        route.getRouteName(),
                        route.getOrigin(),
                        route.getDestination(),
                        route.getTransportType(),
                        route.getDurationMinutes(),
                        route.getPrice(),
                        "", // departure time not needed for deletion
                        "", // metadata not needed for deletion
                        "DELETE",
                        null, // original route ID (would need to be tracked)
                        currentUser,
                        "Request to delete route"
                    );
                    
                    if (submitted) {
                        showStatus("✓ Deletion request submitted successfully. Awaiting master approval.", false);
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Request Submitted");
                        info.setHeaderText("Deletion Request Submitted");
                        info.setContentText("Your deletion request for '" + route.getRouteName() + 
                                          "' has been submitted.\n\nThe route will be removed from the REST API after master approval.");
                        info.showAndWait();
                    } else {
                        showStatus("✗ Failed to submit deletion request", true);
                    }
                } catch (Exception e) {
                    showStatus("✗ Error: " + e.getMessage(), true);
                    e.printStackTrace();
                }
            }
        });
    }
    
    @FXML
    private void handleSaveRoute() {
        formErrorLabel.setText("");
        
        // Validate required fields
        if (routeNameField.getText().trim().isEmpty()) {
            formErrorLabel.setText("⚠ Route name is required");
            formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            return;
        }
        
        if (originField.getText().trim().isEmpty() || destinationField.getText().trim().isEmpty()) {
            formErrorLabel.setText("⚠ Origin and destination are required");
            formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            return;
        }
        
        if (transportTypeCombo.getValue() == null) {
            formErrorLabel.setText("⚠ Transport type is required");
            formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            return;
        }
        
        int durationMinutes;
        double price;
        
        try {
            durationMinutes = Integer.parseInt(durationField.getText().trim());
            if (durationMinutes <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            formErrorLabel.setText("⚠ Duration must be a positive number");
            formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            return;
        }
        
        try {
            price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            formErrorLabel.setText("⚠ Price must be a valid number");
            formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            return;
        }
        
        // Get form data
        String routeName = routeNameField.getText().trim();
        String origin = originField.getText().trim();
        String destination = destinationField.getText().trim();
        String transportType = transportTypeCombo.getValue();
        String departureTime = departureTimeField.getText().trim();
        String metadata = metadataField.getText().trim();
        String notes = notesField.getText().trim();
        
        // Validate departure time format (24-hour format: HH:mm)
        if (!departureTime.isEmpty() && !departureTime.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            formErrorLabel.setText("⚠ Departure time must be in 24-hour format (HH:mm, e.g., 08:00, 14:30, 23:45)");
            formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            return;
        }
        
        try {
            String currentUser = AuthenticationManager.getInstance().getCurrentUsername();
            String changeType = isEditMode ? "UPDATE" : "CREATE";
            
            // Submit to pending routes for approval
            boolean submitted = databaseManager.submitPendingRoute(
                routeName,
                origin,
                destination,
                transportType,
                durationMinutes,
                price,
                departureTime,
                metadata,
                changeType,
                isEditMode ? editingRoute.getId() : null, // original route ID
                currentUser,
                notes.isEmpty() ? (isEditMode ? "Update request" : "New route request") : notes
            );
            
            if (submitted) {
                showStatus("✓ Route " + changeType.toLowerCase() + " request submitted successfully", false);
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Request Submitted");
                success.setHeaderText(isEditMode ? "Edit Request Submitted" : "New Route Request Submitted");
                success.setContentText("Your request has been submitted for master approval.\n\n" +
                                      "Route: " + routeName + "\n" +
                                      "Type: " + transportType + "\n" +
                                      "Route: " + origin + " → " + destination + "\n\n" +
                                      "The route will be added/updated in the REST API after approval.");
                success.showAndWait();
                handleCloseForm();
            } else {
                formErrorLabel.setText("⚠ Failed to submit request. Please try again.");
                formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            formErrorLabel.setText("⚠ Error: " + e.getMessage());
            formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            e.printStackTrace();
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
        statusCombo.getSelectionModel().select("ACTIVE");
        metadataField.clear();
        if (departureTimeField != null) departureTimeField.clear();
        if (arrivalTimeField != null) arrivalTimeField.clear();
        if (notesField != null) notesField.clear();
        formErrorLabel.setText("");
        isEditMode = false;
        editingRoute = null;
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + 
                           (isError ? "#e74c3c" : "#27ae60") + ";");
    }
    
    // ============= FILTER AND SEARCH METHODS =============
    
    @FXML
    private void handleShowAll() {
        currentFilter = "ALL";
        updateFilterButtons();
        applyFilters();
        showStatus("Showing all routes (" + filteredRoutesList.size() + ")", false);
    }
    
    @FXML
    private void handleShowBus() {
        currentFilter = "BUS";
        updateFilterButtons();
        applyFilters();
        showStatus("Showing bus routes only (" + filteredRoutesList.size() + ")", false);
    }
    
    @FXML
    private void handleShowTrain() {
        currentFilter = "TRAIN";
        updateFilterButtons();
        applyFilters();
        showStatus("Showing train routes only (" + filteredRoutesList.size() + ")", false);
    }
    
    @FXML
    private void handleClearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
    }
    
    private void updateFilterButtons() {
        // Active button style
        String activeStyle = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 20; -fx-cursor: hand; -fx-background-radius: 5; -fx-font-weight: bold;";
        // Inactive button style
        String inactiveStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 20; -fx-cursor: hand; -fx-background-radius: 5;";
        
        if (showAllBtn != null) {
            showAllBtn.setStyle(currentFilter.equals("ALL") ? activeStyle : inactiveStyle);
        }
        if (showBusBtn != null) {
            showBusBtn.setStyle(currentFilter.equals("BUS") ? activeStyle : inactiveStyle);
        }
        if (showTrainBtn != null) {
            showTrainBtn.setStyle(currentFilter.equals("TRAIN") ? activeStyle : inactiveStyle);
        }
    }
    
    private void applyFilters() {
        filteredRoutesList.clear();
        
        String searchText = (searchField != null && searchField.getText() != null) 
            ? searchField.getText().toLowerCase().trim() 
            : "";
        
        for (RouteRow route : routesList) {
            // Apply transport type filter
            boolean matchesFilter = currentFilter.equals("ALL") || 
                                   route.getTransportType().equalsIgnoreCase(currentFilter);
            
            // Apply search filter - search for city name in route name, origin OR destination
            boolean matchesSearch = false;
            if (searchText.isEmpty()) {
                matchesSearch = true;
            } else {
                String routeName = route.getRouteName().toLowerCase();
                String origin = route.getOrigin().toLowerCase();
                String destination = route.getDestination().toLowerCase();
                
                // Match if search text is found in route name
                if (routeName.contains(searchText)) {
                    matchesSearch = true;
                }
                // Match if city is origin OR destination (connected to this place)
                else if (origin.contains(searchText) || destination.contains(searchText)) {
                    matchesSearch = true;
                }
                // Also check for partial city name matches
                else if (cityNameMatches(origin, searchText) || cityNameMatches(destination, searchText)) {
                    matchesSearch = true;
                }
            }
            
            if (matchesFilter && matchesSearch) {
                filteredRoutesList.add(route);
            }
        }
        
        routesTable.setItems(filteredRoutesList);
        emptyLabel.setVisible(filteredRoutesList.isEmpty());
        
        if (filteredRoutesList.isEmpty() && !searchText.isEmpty()) {
            emptyLabel.setText("No routes found connected to '" + searchText + "'.");
        } else if (filteredRoutesList.isEmpty()) {
            emptyLabel.setText("No routes available. Click 'Add Route' to create one.");
        }
    }
    
    /**
     * Helper method to match city names flexibly
     * Handles variations like "Chittagong/Chattogram", "Cox's Bazar/Coxs Bazar"
     */
    private boolean cityNameMatches(String cityName, String searchTerm) {
        // Normalize both strings for comparison
        String normalizedCity = cityName.toLowerCase()
            .replace("'", "")
            .replace(" ", "")
            .replace("chattogram", "chittagong");
        
        String normalizedSearch = searchTerm.toLowerCase()
            .replace("'", "")
            .replace(" ", "")
            .replace("chattogram", "chittagong");
        
        return normalizedCity.contains(normalizedSearch) || normalizedSearch.contains(normalizedCity);
    }
    
    // ============= END FILTER AND SEARCH METHODS =============
    
    // Row class for TableView
    public static class RouteRow {
        private final javafx.beans.property.SimpleIntegerProperty idProperty;
        private final javafx.beans.property.SimpleStringProperty routeNameProperty;
        private final javafx.beans.property.SimpleStringProperty originProperty;
        private final javafx.beans.property.SimpleStringProperty destinationProperty;
        private final javafx.beans.property.SimpleStringProperty transportTypeProperty;
        private final javafx.beans.property.SimpleIntegerProperty durationMinutesProperty;
        private final javafx.beans.property.SimpleDoubleProperty priceProperty;
        private final javafx.beans.property.SimpleStringProperty statusProperty;
        
        public RouteRow(int id, String routeName, String origin, String destination,
                       String transportType, int durationMinutes, double price, String status) {
            this.idProperty = new javafx.beans.property.SimpleIntegerProperty(id);
            this.routeNameProperty = new javafx.beans.property.SimpleStringProperty(routeName);
            this.originProperty = new javafx.beans.property.SimpleStringProperty(origin);
            this.destinationProperty = new javafx.beans.property.SimpleStringProperty(destination);
            this.transportTypeProperty = new javafx.beans.property.SimpleStringProperty(transportType);
            this.durationMinutesProperty = new javafx.beans.property.SimpleIntegerProperty(durationMinutes);
            this.priceProperty = new javafx.beans.property.SimpleDoubleProperty(price);
            this.statusProperty = new javafx.beans.property.SimpleStringProperty(status);
        }
        
        public int getId() { return idProperty.get(); }
        public String getRouteName() { return routeNameProperty.get(); }
        public String getOrigin() { return originProperty.get(); }
        public String getDestination() { return destinationProperty.get(); }
        public String getTransportType() { return transportTypeProperty.get(); }
        public int getDurationMinutes() { return durationMinutesProperty.get(); }
        public double getPrice() { return priceProperty.get(); }
        public String getStatus() { return statusProperty.get(); }
    }
}
