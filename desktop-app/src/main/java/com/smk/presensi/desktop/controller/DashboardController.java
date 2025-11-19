package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Controller for the Dashboard Shell.
 * Handles Sidebar navigation and loading views into the content area.
 */
public class DashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private Label userLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private StackPane contentArea;

    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TitledPane keuanganPane;
    @FXML private Button settingsMenuButton;

    private SessionManager sessionManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            sessionManager = new SessionManager();
            updateUserInfo();
            handleDashboardHome();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing DashboardController", e);
        }
    }

    /**
     * Set SessionManager (called by LoginController)
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        updateUserInfo();
    }

    private void updateUserInfo() {
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            userLabel.setText(sessionManager.getUsername() + " (" + sessionManager.getRole() + ")");
            connectionStatusLabel.setText("Connected");
        } else {
            userLabel.setText("Guest");
            connectionStatusLabel.setText("Disconnected");
        }
        applyRoleBasedMenu();
    }

    private void applyRoleBasedMenu() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            return;
        }
        String role = sessionManager.getRole();
        if (role == null) {
            return;
        }
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        if (!isAdmin) {
            if (keuanganPane != null) {
                keuanganPane.setManaged(false);
                keuanganPane.setVisible(false);
            }
            if (settingsMenuButton != null) {
                settingsMenuButton.setManaged(false);
                settingsMenuButton.setVisible(false);
            }
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + fxmlPath, e);
            Label errorLabel = new Label("Error loading view: " + e.getMessage());
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }

    @FXML
    public void handleLogout() {
        if (sessionManager != null) {
            sessionManager.logout();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - SIJA Presensi");
            stage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load login view", e);
        }
    }

    // Navigation Handlers

    @FXML
    public void handleDashboardHome() {
        loadView("/fxml/dashboard-home.fxml");
    }

    // Data Master
    @FXML public void handleTahunAjaranManagement() { loadView("/fxml/tahun-ajaran-management.fxml"); }
    @FXML public void handleSiswaManagement() { loadView("/fxml/siswa-management.fxml"); }
    @FXML public void handleGuruManagement() { loadView("/fxml/guru-management.fxml"); }
    @FXML public void handleKelasManagement() { loadView("/fxml/kelas-management.fxml"); }
    @FXML public void handleJurusanManagement() { loadView("/fxml/jurusan-management.fxml"); }
    @FXML public void handleDudiManagement() { loadView("/fxml/dudi-management.fxml"); }
    @FXML public void handleOfficeManagement() { loadView("/fxml/office-management.fxml"); }

    // Akademik
    @FXML public void handleMapelManagement() { loadView("/fxml/mapel-management.fxml"); }
    @FXML public void handleJadwalManagement() { loadView("/fxml/jadwal-management.fxml"); }
    @FXML public void handleUlanganHarian() { loadView("/fxml/ulangan-harian.fxml"); }

    // Presensi
    @FXML public void handlePresensiManagement() { loadView("/fxml/presensi-management.fxml"); }
    @FXML public void handlePresensiRekap() { loadView("/fxml/presensi-rekap.fxml"); }
    @FXML public void handlePresensiGuru() { loadView("/fxml/presensi-guru.fxml"); }

    // PKL
    @FXML public void handlePklPlacement() { loadView("/fxml/pkl-placement.fxml"); }
    @FXML public void handlePklLog() { loadView("/fxml/pkl-log.fxml"); }
    @FXML public void handlePklAssessment() { loadView("/fxml/pkl-assessment.fxml"); }

    // Kesiswaan
    @FXML public void handlePelanggaran() { loadView("/fxml/pelanggaran-management.fxml"); }
    @FXML public void handlePrestasi() { loadView("/fxml/prestasi-management.fxml"); }

    // Sarpras
    @FXML public void handleInventaris() { loadView("/fxml/inventaris-management.fxml"); }
    @FXML public void handlePeminjaman() { loadView("/fxml/peminjaman-management.fxml"); }

    // Keuangan
    @FXML public void handlePembayaranMaster() { loadView("/fxml/pembayaran-master.fxml"); }
    @FXML public void handlePembayaranTransaksi() { loadView("/fxml/pembayaran-transaksi.fxml"); }
    @FXML public void handlePembayaranRekap() { loadView("/fxml/pembayaran-rekap.fxml"); }

    // Utilitas
    @FXML public void handleImportExport() { loadView("/fxml/import-export.fxml"); }
    @FXML public void handleUserManagement() { loadView("/fxml/user-management.fxml"); }
    @FXML public void handleSettings() { loadView("/fxml/settings.fxml"); }
}
