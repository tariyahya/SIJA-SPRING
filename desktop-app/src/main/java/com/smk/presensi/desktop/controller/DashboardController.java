package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.ExportService;
import com.smk.presensi.desktop.service.PresensiService;
import com.smk.presensi.desktop.service.SessionManager;
import com.smk.presensi.desktop.viewmodel.DashboardViewModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * Controller untuk Dashboard View
 * Handles UI logic dan event handling
 */
public class DashboardController implements Initializable {
    
    // Statistics Cards
    @FXML private Label totalPresensiLabel;
    @FXML private Label totalHadirLabel;
    @FXML private Label totalTerlambatLabel;
    @FXML private Label totalAlphaLabel;
    @FXML private Label persentaseHadirLabel;

    // Table
    @FXML private TableView<Presensi> presensiTable;
    @FXML private TableColumn<Presensi, Long> idColumn;
    @FXML private TableColumn<Presensi, String> usernameColumn;
    @FXML private TableColumn<Presensi, String> tipeColumn;
    @FXML private TableColumn<Presensi, LocalDate> tanggalColumn;
    @FXML private TableColumn<Presensi, LocalTime> jamMasukColumn;
    @FXML private TableColumn<Presensi, LocalTime> jamPulangColumn;
    @FXML private TableColumn<Presensi, String> statusColumn;
    @FXML private TableColumn<Presensi, String> methodColumn;

    // RFID Input
    @FXML private TextField rfidInput;
    @FXML private Button checkinButton;

    // Controls
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private Label userLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    @FXML private CheckBox mockDataCheckbox;

    private DashboardViewModel viewModel;
    private SessionManager sessionManager;
    private Timeline autoRefreshTimer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Will be set by LoginController
        if (sessionManager == null) {
            sessionManager = new SessionManager();
        }

        // Initialize ApiClient with saved token (singleton pattern)
        ApiClient apiClient = ApiClient.getInstance();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }
        
        viewModel = new DashboardViewModel(apiClient);

        // Setup table columns
        setupTableColumns();

        // Bind UI to ViewModel
        bindUI();

        // Load initial data
        viewModel.loadDashboardData();

        // Setup event handlers
        setupEventHandlers();
        
        // Display user info
        updateUserInfo();
        
        // Setup auto-refresh (every 30 seconds)
        setupAutoRefresh();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        tipeColumn.setCellValueFactory(new PropertyValueFactory<>("tipe"));
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        jamMasukColumn.setCellValueFactory(new PropertyValueFactory<>("jamMasuk"));
        jamPulangColumn.setCellValueFactory(new PropertyValueFactory<>("jamPulang"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));

        // Style status column (color coding)
        statusColumn.setCellFactory(column -> new TableCell<Presensi, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "HADIR":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "TERLAMBAT":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "ALPHA":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
    }

    private void bindUI() {
        // Bind table data
        presensiTable.setItems(viewModel.getPresensiList());

        // Bind statistics labels
        totalPresensiLabel.textProperty().bind(
            viewModel.totalPresensiProperty().asString()
        );
        totalHadirLabel.textProperty().bind(
            viewModel.totalHadirProperty().asString()
        );
        totalTerlambatLabel.textProperty().bind(
            viewModel.totalTerlambatProperty().asString()
        );
        totalAlphaLabel.textProperty().bind(
            viewModel.totalAlphaProperty().asString()
        );
        persentaseHadirLabel.textProperty().bind(
            viewModel.persentaseHadirProperty().asString("%.2f%%")
        );

        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());

        // Bind error label
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(
            viewModel.errorMessageProperty().isNotEmpty()
        );

        // Bind mock data checkbox
        mockDataCheckbox.selectedProperty().bindBidirectional(
            viewModel.useMockDataProperty()
        );
    }

    private void setupEventHandlers() {
        // Refresh button
        refreshButton.setOnAction(event -> viewModel.refreshData());

        // Logout button
        if (logoutButton != null) {
            logoutButton.setOnAction(event -> handleLogout());
        }

        // RFID Checkin button
        checkinButton.setOnAction(event -> handleRfidCheckin());

        // RFID input (Enter key to checkin)
        rfidInput.setOnAction(event -> handleRfidCheckin());
    }

    @FXML
    private void handleRfidCheckin() {
        String rfidCardId = rfidInput.getText().trim();
        
        if (rfidCardId.isEmpty()) {
            showError("RFID Card ID tidak boleh kosong");
            return;
        }

        viewModel.checkinRfid(rfidCardId);
        rfidInput.clear();
        rfidInput.requestFocus();
    }

    @FXML
    private void handleRefresh() {
        viewModel.refreshData();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Set SessionManager (called by LoginController)
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        updateUserInfo();
    }

    /**
     * Update user info label
     */
    private void updateUserInfo() {
        if (userLabel != null && sessionManager != null && sessionManager.isLoggedIn()) {
            String username = sessionManager.getUsername();
            String role = sessionManager.getRole();
            userLabel.setText("👤 " + username + " (" + role + ")");
        }
    }

    /**
     * Setup auto-refresh timer (every 30 seconds)
     */
    private void setupAutoRefresh() {
        autoRefreshTimer = new Timeline(
            new KeyFrame(Duration.seconds(30), event -> {
                System.out.println("Auto-refresh triggered at " + java.time.LocalTime.now());
                viewModel.refreshData();
            })
        );
        autoRefreshTimer.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimer.play();
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        // Confirm logout
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Konfirmasi Logout");
        alert.setContentText("Apakah Anda yakin ingin logout?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Stop auto-refresh
                if (autoRefreshTimer != null) {
                    autoRefreshTimer.stop();
                }

                // Clear session
                sessionManager.logout();

                // Navigate back to login
                try {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/login.fxml")
                    );
                    Parent root = loader.load();

                    Stage stage = (Stage) logoutButton.getScene().getWindow();
                    Scene scene = new Scene(root, 500, 650);
                    stage.setScene(scene);
                    stage.setTitle("SIJA Presensi - Login");
                    stage.setResizable(false);

                } catch (IOException e) {
                    showError("Error loading login screen: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Handle Export PDF dari menu
     */
    @FXML
    private void handleExportPdf() {
        // Show dialog for date range
        ExportDateRangeDialog dialog = new ExportDateRangeDialog();
        dialog.showAndWait().ifPresent(dateRange -> {
            // Get file location from file chooser
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save PDF Report");
            fileChooser.setInitialFileName("presensi_" + java.time.LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            
            java.io.File file = fileChooser.showSaveDialog(presensiTable.getScene().getWindow());
            if (file != null) {
                updateStatus("Exporting to CSV...");
                
                // Export in background thread
                new Thread(() -> {
                    try {
                        ApiClient apiClient = ApiClient.getInstance();
                        PresensiService presensiService = new PresensiService(apiClient);
                        ExportService exportService = new ExportService(presensiService);
                        exportService.exportToPdf(dateRange[0], dateRange[1], file);
                        
                        javafx.application.Platform.runLater(() -> {
                            updateStatus("PDF exported successfully");
                            showInfo("PDF report exported to:\n" + file.getAbsolutePath());
                            
                            // Auto-open file
                            if (java.awt.Desktop.isDesktopSupported()) {
                                try {
                                    java.awt.Desktop.getDesktop().open(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            updateStatus("Failed to export PDF");
                            showError("Failed to export PDF: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }

    /**
     * Handle Export CSV dari menu
     */
    @FXML
    private void handleExportCsv() {
        // Show dialog for date range
        ExportDateRangeDialog dialog = new ExportDateRangeDialog();
        dialog.showAndWait().ifPresent(dateRange -> {
            // Get file location from file chooser
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save CSV Report");
            fileChooser.setInitialFileName("presensi_" + java.time.LocalDate.now() + ".csv");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            
            java.io.File file = fileChooser.showSaveDialog(presensiTable.getScene().getWindow());
            if (file != null) {
                updateStatus("Exporting to CSV...");
                
                // Export in background thread
                new Thread(() -> {
                    try {
                        ApiClient apiClient = ApiClient.getInstance();
                        PresensiService presensiService = new PresensiService(apiClient);
                        ExportService exportService = new ExportService(presensiService);
                        exportService.exportToCsv(dateRange[0], dateRange[1], file);
                        
                        javafx.application.Platform.runLater(() -> {
                            updateStatus("CSV exported successfully");
                            showInfo("CSV report exported to:\n" + file.getAbsolutePath());
                            
                            // Auto-open file
                            if (java.awt.Desktop.isDesktopSupported()) {
                                try {
                                    java.awt.Desktop.getDesktop().open(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            updateStatus("Failed to export CSV");
                            showError("Failed to export CSV: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }

    /**
     * Handle User Management dari menu
     */
    @FXML
    private void handleUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/user-management.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("User Management");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Error loading User Management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Settings dari menu
     */
    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/settings.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Settings");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Error loading Settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle About dari menu
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("SIJA Presensi Desktop App");
        alert.setContentText(
            "Version: 1.0.0\n" +
            "Build: Tahap 05\n\n" +
            "Desktop application untuk sistem presensi SIJA\n" +
            "dengan integrasi RFID dan real-time monitoring.\n\n" +
            "© 2025 SIJA"
        );
        alert.showAndWait();
    }
    
    /**
     * Handle Siswa Management dari menu
     */
    @FXML
    private void handleSiswaManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/siswa-management.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manajemen Siswa");
            stage.setScene(new Scene(root, 1200, 700));
            stage.showAndWait();
            
            // Refresh dashboard setelah window ditutup
            viewModel.refreshData();
            
        } catch (IOException e) {
            showError("Error loading Siswa Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Guru Management dari menu
     */
    @FXML
    private void handleGuruManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/guru-management.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manajemen Guru");
            stage.setScene(new Scene(root, 1100, 700));
            stage.showAndWait();
            
            // Refresh dashboard setelah window ditutup
            viewModel.refreshData();
            
        } catch (IOException e) {
            showError("Error loading Guru Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Presensi Management dari menu
     */
    @FXML
    private void handlePresensiManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/presensi-management.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manajemen Presensi");
            stage.setScene(new Scene(root, 1400, 700));
            stage.showAndWait();
            
            // Refresh dashboard setelah window ditutup
            viewModel.refreshData();
            
        } catch (IOException e) {
            showError("Error loading Presensi Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Perizinan Siswa dari menu
     */
    @FXML
    private void handleIzinManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/approval-izin.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Perizinan Siswa");
            stage.setScene(new Scene(root, 1100, 650));
            stage.showAndWait();

            viewModel.refreshData();

        } catch (IOException e) {
            showError("Error loading Perizinan view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle DUDI & PKL Management dari menu
     */
    @FXML
    private void handleDudiManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dudi-management.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manajemen DUDI & PKL");
            stage.setScene(new Scene(root, 1100, 650));
            stage.showAndWait();

        } catch (IOException e) {
            showError("Error loading DUDI Management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Jurusan Management dari menu
     */
    @FXML
    private void handleJurusanManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/jurusan-management.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manajemen Jurusan");
            stage.setScene(new Scene(root, 1000, 600));
            stage.showAndWait();
            
            // Refresh dashboard setelah window ditutup
            viewModel.refreshData();
            
        } catch (IOException e) {
            showError("Error loading Jurusan Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Kelas Management dari menu
     */
    @FXML
    private void handleKelasManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/kelas-management.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manajemen Kelas");
            stage.setScene(new Scene(root, 1100, 700));
            stage.showAndWait();
            
            // Refresh dashboard setelah window ditutup
            viewModel.refreshData();
            
        } catch (IOException e) {
            showError("Error loading Kelas Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle Analytics dari menu
     */
    @FXML
    private void handleAnalytics() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/analytics-report.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Analytics & Reports");
            stage.setScene(new Scene(root, 1000, 700));
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Error loading Analytics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Office Management dari menu
     */
    @FXML
    private void handleOfficeManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/office-management.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Office Location Management");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Error loading Office Management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update status bar
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("Status: " + message);
        }
    }

    /**
     * Update connection status
     */
    private void updateConnectionStatus(boolean connected) {
        if (connectionStatusLabel != null) {
            if (connected) {
                connectionStatusLabel.setText("🟢 Connected");
                connectionStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                connectionStatusLabel.setText("🔴 Disconnected");
                connectionStatusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    /**
     * Handle Import / Export dari menu
     */
    @FXML
    private void handleImportExport() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/import-export.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Import / Export Data");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Error loading Import/Export: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Dashboard Piket dari menu
     */
    @FXML
    private void handlePiketDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dashboard-piket.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            // Dashboard Piket biasanya full screen atau window besar
            stage.setTitle("Dashboard Piket");
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();
            
        } catch (IOException e) {
            showError("Error loading Dashboard Piket: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
