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
    @FXML private Button exportGuruBtn;
    @FXML private Button exportKelasBtn;
    @FXML private Button exportJurusanBtn;
    @FXML private Button exportUsersBtn;
    @FXML private Button exportPresensiBtn;

    @FXML private Button importSiswaBtn;
    @FXML private Button importDudiBtn;
    @FXML private Button importGuruBtn;
    @FXML private Button importKelasBtn;
    @FXML private Button importJurusanBtn;
    @FXML private Button importUsersBtn;
    @FXML private Button importPresensiBtn;

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

    @FXML
    private void handleExportGuru() {
        exportData("/export/guru", "guru_export.xlsx");
    }

    @FXML
    private void handleExportKelas() {
        exportData("/export/kelas", "kelas_export.xlsx");
    }

    @FXML
    private void handleExportJurusan() {
        exportData("/export/jurusan", "jurusan_export.xlsx");
    }

    @FXML
    private void handleExportUsers() {
        exportData("/export/users", "users_export.xlsx");
    }

    @FXML
    private void handleExportPresensi() {
        exportData("/export/presensi", "presensi_export.xlsx");
    }

    @FXML
    private void handleImportSiswa() {
        importData("/import/siswa");
    }

    @FXML
    private void handleImportDudi() {
        importData("/import/dudi");
    }

    @FXML
    private void handleImportGuru() {
        importData("/import/guru");
    }

    @FXML
    private void handleImportKelas() {
        importData("/import/kelas");
    }

    @FXML
    private void handleImportJurusan() {
        importData("/import/jurusan");
    }

    @FXML
    private void handleImportUsers() {
        importData("/import/users");
    }

    @FXML
    private void handleImportPresensi() {
        importData("/import/presensi");
    }

    private void importData(String endpoint) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Import File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        
        File file = fileChooser.showOpenDialog(exportSiswaBtn.getScene().getWindow());
        if (file != null) {
            loadingIndicator.setVisible(true);
            statusLabel.setText("Importing...");
            
            new Thread(() -> {
                try {
                    HttpResponse<String> response = apiClient.postFile(endpoint, file);
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        if (response.statusCode() == 200) {
                            statusLabel.setText("Import completed");
                            InAppNotification.show("Import completed successfully", exportSiswaBtn.getParent(), InAppNotification.NotificationType.SUCCESS, 3);
                        } else {
                            statusLabel.setText("Import failed");
                            InAppNotification.show("Import failed: " + response.body(), exportSiswaBtn.getParent(), InAppNotification.NotificationType.ERROR, 5);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        statusLabel.setText("Import failed");
                        InAppNotification.show("Import failed: " + e.getMessage(), exportSiswaBtn.getParent(), InAppNotification.NotificationType.ERROR, 5);
                    });
                }
            }).start();
        }
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

    @FXML
    private void handleTemplateSiswa() {
        downloadTemplate("siswa");
    }

    @FXML
    private void handleTemplateDudi() {
        downloadTemplate("dudi");
    }

    @FXML
    private void handleTemplateGuru() {
        downloadTemplate("guru");
    }

    @FXML
    private void handleTemplateKelas() {
        downloadTemplate("kelas");
    }

    @FXML
    private void handleTemplateJurusan() {
        downloadTemplate("jurusan");
    }

    @FXML
    private void handleTemplateUser() {
        downloadTemplate("user");
    }

    @FXML
    private void handleTemplatePresensi() {
        downloadTemplate("presensi");
    }

    private void downloadTemplate(String type) {
        exportData("/export/template/" + type, "template_" + type + ".xlsx");
    }
}
