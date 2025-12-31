package com.travelmanager.controller;

import com.travelmanager.api.ScheduleDataManager;
import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.util.NavigationManager;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
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
    
    private ScheduleDataManager dataManager;
    private ObservableList<RouteRow> routesList;
    private RouteRow editingRoute = null;
    
    @FXML
    public void initialize() {
        System.out.println("ManageRoutesController: Initializing...");
        dataManager = ScheduleDataManager.getInstance();
        routesList = FXCollections.observableArrayList();
        
        statusLabel.setText("");
        
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
        routesTable.setItems(routesList);
        emptyLabel.setVisible(routesList.isEmpty());
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
        editingRoute = null;
        formTitle.setText("Add New Route");
        saveButton.setText("Save Route");
        clearForm();
        showForm();
    }
    
    private void handleEditRoute(RouteRow route) {
        showStatus("Edit functionality - Use REST API endpoints to modify schedules", false);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Route");
        alert.setHeaderText("Route: " + route.getRouteName());
        alert.setContentText("To edit this route, use the REST API directly:\n\n" +
                           "Bus: PUT /api/schedules/bus/{busName}\n" +
                           "Train: PUT /api/schedules/train/{trainName}\n\n" +
                           "Route Details:\n" +
                           "Type: " + route.getTransportType() + "\n" +
                           "Origin: " + route.getOrigin() + "\n" +
                           "Destination: " + route.getDestination() + "\n" +
                           "Fare: ৳" + String.format("%.2f", route.getPrice()));
        alert.showAndWait();
    }
    
    private void handleDeleteRoute(RouteRow route) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Route");
        confirmAlert.setHeaderText("Delete route: " + route.getRouteName() + "?");
        confirmAlert.setContentText("To delete this route, use the REST API:\n\n" +
                                   "Bus: DELETE /api/schedules/bus/{busName}\n" +
                                   "Train: DELETE /api/schedules/train/{trainName}\n\n" +
                                   "This feature is coming soon!");
        confirmAlert.showAndWait();
    }
    
    @FXML
    private void handleSaveRoute() {
        showStatus("Add/Edit functionality via REST API - Coming soon!", false);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add New Route");
        alert.setContentText("To add new routes, use the REST API:\n\n" +
                           "Bus: POST /api/schedules/bus\n" +
                           "Train: POST /api/schedules/train\n\n" +
                           "Feature coming soon in UI!");
        alert.showAndWait();
        handleCloseForm();
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
