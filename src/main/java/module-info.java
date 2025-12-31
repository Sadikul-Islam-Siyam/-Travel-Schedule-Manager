module com.travelmanager {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive com.google.gson;
    requires java.sql;
    requires java.net.http;
    
    // Javalin dependencies
    requires io.javalin;
    requires org.slf4j;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.util;

    opens com.travelmanager to javafx.fxml;
    opens com.travelmanager.controller to javafx.fxml;
    opens com.travelmanager.model to com.google.gson;
    opens com.travelmanager.model.rest to com.google.gson;
    
    exports com.travelmanager;
    exports com.travelmanager.controller;
    exports com.travelmanager.model;
    exports com.travelmanager.model.rest;
    exports com.travelmanager.service;
    exports com.travelmanager.service.rest;
    exports com.travelmanager.util;
    exports com.travelmanager.database;
    exports com.travelmanager.domain;
    exports com.travelmanager.exception;
    exports com.travelmanager.api;
    exports com.travelmanager.api.rest;
    exports com.travelmanager.storage;
}
