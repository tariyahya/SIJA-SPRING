package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.SessionManager;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

public class ImportExportController implements Initializable {

    @FXML private Button exportSiswaBtn;
    @FXML private Button exportDudiBtn;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;

    private ApiClient apiClient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SessionManager sessionManager = new SessionManager();
        apiClient = ApiClient.getInstance();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }
    }

    @FXML
    private void handleExportSiswa() {
        exportData("/export/siswa", "siswa_export.xlsx");
    }

    @FXML
    private void handleExportDudi() {
        exportData("/export/dudi", "dudi_export.xlsx");
    }

    private void exportData(String endpoint, String defaultFilename) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Export File");
        fileChooser.setInitialFileName(defaultFilename);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        
        File file = fileChooser.showSaveDialog(exportSiswaBtn.getScene().getWindow());
        if (file != null) {
            loadingIndicator.setVisible(true);
            statusLabel.setText("Exporting...");
            
            new Thread(() -> {
                try {
                    HttpResponse<byte[]> response = apiClient.getBinary(endpoint);
                    if (response.statusCode() == 200) {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(response.body());
                        }
                        javafx.application.Platform.runLater(() -> {
                            loadingIndicator.setVisible(false);
                            statusLabel.setText("Export completed");
                            InAppNotification.show("Export completed successfully", exportSiswaBtn.getParent(), InAppNotification.NotificationType.SUCCESS, 3);
                        });
                    } else {
                        throw new RuntimeException("Failed to download: " + response.statusCode());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        statusLabel.setText("Export failed");
                        InAppNotification.show("Export failed: " + e.getMessage(), exportSiswaBtn.getParent(), InAppNotification.NotificationType.ERROR, 5);
                    });
                }
            }).start();
        }
    }
}
