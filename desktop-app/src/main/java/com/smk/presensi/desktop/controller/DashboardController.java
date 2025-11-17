package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.viewmodel.DashboardViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

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
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    @FXML private CheckBox mockDataCheckbox;

    private DashboardViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize ViewModel
        ApiClient apiClient = new ApiClient();
        viewModel = new DashboardViewModel(apiClient);

        // Setup table columns
        setupTableColumns();

        // Bind UI to ViewModel
        bindUI();

        // Load initial data
        viewModel.loadDashboardData();

        // Setup event handlers
        setupEventHandlers();
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
}
