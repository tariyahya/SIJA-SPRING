package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.PresensiService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Presensi Management View
 * Handles CRUD operations for attendance records
 */
public class PresensiManagementController implements Initializable {
    
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
    @FXML private TableColumn<Presensi, String> keteranganColumn;
    @FXML private TableColumn<Presensi, Void> actionColumn;
    
    // Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> tipeFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    
    // Buttons
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;
    
    // Status
    @FXML private Label statusLabel;
    @FXML private Label infoLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private CheckBox mockDataCheckbox;
    
    private PresensiService presensiService;
    private ObservableList<Presensi> presensiList;
    private ObservableList<Presensi> filteredList;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize service
        ApiClient apiClient = new ApiClient();
        presensiService = new PresensiService(apiClient);
        
        presensiList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();
        
        // Setup filters
        setupFilters();
        
        // Setup table
        setupTableColumns();
        
        // Setup event handlers
        setupEventHandlers();
        
        // Load data
        loadData();
    }
    
    private void setupFilters() {
        // Setup tipe filter
        tipeFilter.setItems(FXCollections.observableArrayList("ALL", "SISWA", "GURU"));
        tipeFilter.setValue("ALL");
        
        // Setup status filter
        statusFilter.setItems(FXCollections.observableArrayList("ALL", "HADIR", "TERLAMBAT", "ALPHA"));
        statusFilter.setValue("ALL");
        
        // Setup date pickers with default values
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
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
        keteranganColumn.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        
        presensiTable.setItems(filteredList);
        
        presensiTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }
    
    private void setupEventHandlers() {
        searchButton.setOnAction(e -> handleSearch());
        resetButton.setOnAction(e -> handleReset());
        addButton.setOnAction(e -> handleAdd());
        refreshButton.setOnAction(e -> loadData());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        closeButton.setOnAction(e -> handleClose());
        mockDataCheckbox.setOnAction(e -> loadData());
        searchField.setOnAction(e -> handleSearch());
    }
    
    private void loadData() {
        setLoading(true);
        statusLabel.setText("Loading...");
        
        new Thread(() -> {
            try {
                List<Presensi> data;
                
                if (mockDataCheckbox.isSelected()) {
                    data = presensiService.getMockData();
                } else {
                    LocalDate today = LocalDate.now();
                    data = presensiService.getLaporanHarian(today);
                }
                
                Platform.runLater(() -> {
                    presensiList.setAll(data);
                    filteredList.setAll(data);
                    updateInfoLabel();
                    statusLabel.setText("Ready");
                    setLoading(false);
                    InAppNotification.show("Data loaded", presensiTable.getParent(), 
                                          InAppNotification.NotificationType.SUCCESS, 3);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    setLoading(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String tipe = tipeFilter.getValue();
        String status = statusFilter.getValue();
        
        List<Presensi> filtered = presensiList.stream()
            .filter(p -> {
                boolean matchSearch = searchText.isEmpty() || 
                    p.getUsername().toLowerCase().contains(searchText);
                
                boolean matchTipe = tipe == null || tipe.equals("ALL") || 
                    p.getTipe().equals(tipe);
                
                boolean matchStatus = status == null || status.equals("ALL") || 
                    p.getStatus().equals(status);
                
                return matchSearch && matchTipe && matchStatus;
            })
            .collect(Collectors.toList());
        
        filteredList.setAll(filtered);
        updateInfoLabel();
    }
    
    @FXML
    private void handleReset() {
        searchField.clear();
        tipeFilter.setValue("ALL");
        statusFilter.setValue("ALL");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        filteredList.setAll(presensiList);
        updateInfoLabel();
    }
    
    @FXML
    private void handleAdd() {
        InAppNotification.show("Add Presensi - Coming Soon!", presensiTable.getParent(), 
                              InAppNotification.NotificationType.INFO, 3);
    }
    
    @FXML
    private void handleEdit() {
        Presensi selected = presensiTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            InAppNotification.show("Edit Presensi - Coming Soon!", presensiTable.getParent(), 
                                  InAppNotification.NotificationType.INFO, 3);
        }
    }
    
    @FXML
    private void handleDelete() {
        Presensi selected = presensiTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Konfirmasi Hapus");
            confirm.setHeaderText("Hapus presensi: " + selected.getUsername() + "?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    InAppNotification.show("Delete Presensi - Coming Soon!", presensiTable.getParent(), 
                                          InAppNotification.NotificationType.INFO, 3);
                }
            });
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        statusLabel.setVisible(!loading);
    }
    
    private void updateInfoLabel() {
        infoLabel.setText("Total: " + filteredList.size() + " presensi" + 
                         (filteredList.size() != presensiList.size() ? 
                          " (dari " + presensiList.size() + ")" : ""));
    }
}
