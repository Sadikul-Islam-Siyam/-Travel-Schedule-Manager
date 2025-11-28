package com.travelmanager.util;

import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.geometry.Bounds;
import javafx.stage.Window;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Autocomplete popup for text fields
 */
public class AutoCompletePopup extends PopupControl {
    
    private ListView<String> listView;
    private ObservableList<String> suggestions;
    private TextField textField;
    
    public AutoCompletePopup(TextField textField, List<String> allLocations) {
        this.textField = textField;
        this.suggestions = FXCollections.observableArrayList();
        this.listView = new ListView<>(suggestions);
        
        listView.setPrefHeight(200);
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    textField.setText(selected);
                    hide();
                }
            }
        });
        
        // Setup text field listener - only show popup when user is typing
        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            // Only show popup if text field has focus (user is actively typing)
            if (!textField.isFocused()) {
                return;
            }
            
            if (newValue == null || newValue.trim().isEmpty()) {
                hide();
                return;
            }
            
            String input = newValue.toLowerCase().trim();
            List<String> matches = allLocations.stream()
                .filter(loc -> loc.toLowerCase().startsWith(input))
                .limit(10)
                .collect(Collectors.toList());
            
            if (matches.isEmpty()) {
                hide();
            } else {
                suggestions.setAll(matches);
                if (!isShowing()) {
                    showPopup();
                }
            }
        });
        
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                hide();
            }
        });
    }
    
    private void showPopup() {
        Window window = textField.getScene().getWindow();
        Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
        if (bounds != null) {
            show(window, bounds.getMinX(), bounds.getMaxY());
        }
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new AutoCompleteSkin(this);
    }
    
    public ListView<String> getListView() {
        return listView;
    }
    
    private static class AutoCompleteSkin implements Skin<AutoCompletePopup> {
        private AutoCompletePopup control;
        
        public AutoCompleteSkin(AutoCompletePopup control) {
            this.control = control;
        }
        
        @Override
        public AutoCompletePopup getSkinnable() {
            return control;
        }
        
        @Override
        public javafx.scene.Node getNode() {
            return control.getListView();
        }
        
        @Override
        public void dispose() {
        }
    }
}
