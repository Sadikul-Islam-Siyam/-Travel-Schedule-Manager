module com.travelmanager {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive com.google.gson;
    requires java.sql;

    opens com.travelmanager to javafx.fxml;
    opens com.travelmanager.controller to javafx.fxml;
    opens com.travelmanager.model to com.google.gson;
    
    exports com.travelmanager;
    exports com.travelmanager.controller;
    exports com.travelmanager.model;
    exports com.travelmanager.service;
    exports com.travelmanager.util;
    exports com.travelmanager.database;
}
