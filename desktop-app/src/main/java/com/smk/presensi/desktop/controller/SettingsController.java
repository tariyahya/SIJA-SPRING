package com.smk.presensi.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller untuk Settings View
 */
public class SettingsController implements Initializable {

    // General Settings
    @FXML private TextField serverUrlField;
    @FXML private Spinner<Integer> refreshIntervalSpinner;
    @FXML private CheckBox enableWebSocketCheck;
    @FXML private CheckBox autoReconnectCheck;
    @FXML private CheckBox showNotificationsCheck;

    // Export Settings
    @FXML private RadioButton pdfFormatRadio;
    @FXML private RadioButton csvFormatRadio;
    @FXML private TextField exportDirField;
    @FXML private CheckBox autoOpenExportCheck;

    // UI Settings
    @FXML private ComboBox<String> themeComboBox;
    @FXML private Spinner<Integer> rowsPerPageSpinner;
    @FXML private CheckBox compactModeCheck;

    // Advanced Settings
    @FXML private Spinner<Integer> connectionTimeoutSpinner;
    @FXML private Spinner<Integer> readTimeoutSpinner;
    @FXML private CheckBox debugModeCheck;
    @FXML private CheckBox mockDataCheck;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup spinners
        setupSpinners();

        // Load current settings
        loadSettings();

        // Setup ComboBox
        if (themeComboBox != null && themeComboBox.getItems().isEmpty()) {
            themeComboBox.getItems().addAll("Light", "Dark");
            themeComboBox.getSelectionModel().select(0);
        }
    }

    private void setupSpinners() {
        // Refresh Interval Spinner (10-300 seconds)
        if (refreshIntervalSpinner != null) {
            SpinnerValueFactory<Integer> refreshFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 300, 30, 5);
            refreshIntervalSpinner.setValueFactory(refreshFactory);
        }

        // Rows Per Page Spinner (10-100)
        if (rowsPerPageSpinner != null) {
            SpinnerValueFactory<Integer> rowsFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100, 50, 10);
            rowsPerPageSpinner.setValueFactory(rowsFactory);
        }

        // Connection Timeout Spinner (5-60 seconds)
        if (connectionTimeoutSpinner != null) {
            SpinnerValueFactory<Integer> connFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, 10, 5);
            connectionTimeoutSpinner.setValueFactory(connFactory);
        }

        // Read Timeout Spinner (5-120 seconds)
        if (readTimeoutSpinner != null) {
            SpinnerValueFactory<Integer> readFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 120, 30, 5);
            readTimeoutSpinner.setValueFactory(readFactory);
        }
    }

    private void loadSettings() {
        // TODO: Load from properties file or preferences
        // For now, use default values
        if (serverUrlField != null) {
            serverUrlField.setText("http://localhost:8080");
        }
        
        if (exportDirField != null) {
            exportDirField.setText(System.getProperty("user.home") + "/Desktop");
        }

        if (enableWebSocketCheck != null) {
            enableWebSocketCheck.setSelected(true);
        }

        if (autoReconnectCheck != null) {
            autoReconnectCheck.setSelected(true);
        }

        if (showNotificationsCheck != null) {
            showNotificationsCheck.setSelected(true);
        }

        if (autoOpenExportCheck != null) {
            autoOpenExportCheck.setSelected(true);
        }

        if (compactModeCheck != null) {
            compactModeCheck.setSelected(false);
        }

        if (debugModeCheck != null) {
            debugModeCheck.setSelected(false);
        }

        if (mockDataCheck != null) {
            mockDataCheck.setSelected(true);
        }
    }

    @FXML
    private void handleBrowseExportDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Export Directory");
        
        // Set initial directory
        String currentDir = exportDirField.getText();
        if (currentDir != null && !currentDir.isEmpty()) {
            File initialDir = new File(currentDir);
            if (initialDir.exists()) {
                directoryChooser.setInitialDirectory(initialDir);
            }
        }

        Stage stage = (Stage) exportDirField.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            exportDirField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleSave() {
        // TODO: Save settings to properties file or preferences
        // For now, just show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings Saved");
        alert.setHeaderText(null);
        alert.setContentText("Settings have been saved successfully!");
        alert.showAndWait();

        // Close window
        handleCancel();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) serverUrlField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleResetDefaults() {
        // Confirm reset
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset to Defaults");
        alert.setHeaderText("Konfirmasi Reset");
        alert.setContentText("Apakah Anda yakin ingin reset ke pengaturan default?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                loadSettings();
                
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Reset Complete");
                info.setHeaderText(null);
                info.setContentText("Settings have been reset to defaults!");
                info.showAndWait();
            }
        });
    }
}
