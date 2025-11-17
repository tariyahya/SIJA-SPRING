package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.service.ApiClient;
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
import javafx.scene.layout.VBox;
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

        // Initialize ApiClient with saved token
        ApiClient apiClient = new ApiClient();
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
}
