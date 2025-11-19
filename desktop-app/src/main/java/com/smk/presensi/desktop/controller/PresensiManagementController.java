package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.PresensiService;
import com.smk.presensi.desktop.service.UserService;
import com.smk.presensi.desktop.util.InAppNotification;
import com.smk.presensi.desktop.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Presensi Management View
 * Handles CRUD operations for attendance records
 */
public class PresensiManagementController implements Initializable {

    private static final List<String> STATUS_OPTIONS = List.of("HADIR", "TERLAMBAT", "ALPHA", "IZIN", "SAKIT", "DISPENSASI");
    
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
        // Initialize service with singleton ApiClient (shares JWT token)
        ApiClient apiClient = ApiClient.getInstance();
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
        ObservableList<String> statusChoices = FXCollections.observableArrayList();
        statusChoices.add("ALL");
        statusChoices.addAll(STATUS_OPTIONS);
        statusFilter.setItems(statusChoices);
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
        try {
            Dialog<Presensi> dialog = new Dialog<>();
            dialog.setTitle("Tambah Presensi");
            dialog.setHeaderText("Masukkan data presensi baru");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/presensi-form-dialog.fxml"));
            GridPane form = loader.load();

            TextField userIdField = (TextField) form.lookup("#userIdField");
            TextField usernameField = (TextField) form.lookup("#usernameField");
            ComboBox<String> tipeCombo = (ComboBox<String>) form.lookup("#tipeCombo");
            DatePicker tanggalPicker = (DatePicker) form.lookup("#tanggalPicker");
            TextField jamMasukField = (TextField) form.lookup("#jamMasukField");
            TextField jamPulangField = (TextField) form.lookup("#jamPulangField");
            ComboBox<String> statusCombo = (ComboBox<String>) form.lookup("#statusCombo");
            ComboBox<String> methodCombo = (ComboBox<String>) form.lookup("#methodCombo");
            TextField keteranganField = (TextField) form.lookup("#keteranganField");
            Button selectUserButton = (Button) form.lookup("#selectUserButton");

            tipeCombo.setItems(FXCollections.observableArrayList("SISWA", "GURU"));
            statusCombo.setItems(FXCollections.observableArrayList(STATUS_OPTIONS));
            methodCombo.setItems(FXCollections.observableArrayList("MANUAL", "RFID", "BARCODE", "FACE"));

            tanggalPicker.setValue(LocalDate.now());

            if (selectUserButton != null) {
                selectUserButton.setOnAction(e -> openUserLookup(userIdField, usernameField));
            }

            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Presensi p = new Presensi();
                    try {
                        p.setUserId(Long.parseLong(userIdField.getText()));
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                    p.setUsername(usernameField.getText());
                    p.setTipe(tipeCombo.getValue());
                    p.setTanggal(tanggalPicker.getValue());
                    if (!jamMasukField.getText().isBlank()) {
                        p.setJamMasuk(LocalTime.parse(jamMasukField.getText(), timeFormatter));
                    }
                    if (!jamPulangField.getText().isBlank()) {
                        p.setJamPulang(LocalTime.parse(jamPulangField.getText(), timeFormatter));
                    }
                    p.setStatus(statusCombo.getValue());
                    p.setMethod(methodCombo.getValue());
                    p.setKeterangan(keteranganField.getText());
                    return p;
                }
                return null;
            });

            Optional<Presensi> result = dialog.showAndWait();
            result.ifPresent(p -> {
                setLoading(true);
                new Thread(() -> {
                    try {
                        Presensi created = presensiService.createPresensi(p);
                        Platform.runLater(() -> {
                            setLoading(false);
                            if (created != null) {
                                loadData();
                                InAppNotification.show("Presensi berhasil ditambahkan",
                                        presensiTable.getParent(),
                                        InAppNotification.NotificationType.SUCCESS, 3);
                            } else {
                                InAppNotification.show("Gagal menambahkan presensi",
                                        presensiTable.getParent(),
                                        InAppNotification.NotificationType.ERROR, 5);
                            }
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            setLoading(false);
                            InAppNotification.show("Error: " + ex.getMessage(),
                                    presensiTable.getParent(),
                                    InAppNotification.NotificationType.ERROR, 5);
                        });
                    }
                }).start();
            });
        } catch (Exception e) {
            e.printStackTrace();
            InAppNotification.show("Error: " + e.getMessage(),
                    presensiTable.getParent(),
                    InAppNotification.NotificationType.ERROR, 5);
        }
    }
    
    @FXML
    private void handleEdit() {
        Presensi selected = presensiTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Dialog<Presensi> dialog = new Dialog<>();
                dialog.setTitle("Edit Presensi");
                dialog.setHeaderText("Edit presensi: " + selected.getUsername());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/presensi-form-dialog.fxml"));
                GridPane form = loader.load();

            TextField userIdField = (TextField) form.lookup("#userIdField");
            TextField usernameField = (TextField) form.lookup("#usernameField");
            ComboBox<String> tipeCombo = (ComboBox<String>) form.lookup("#tipeCombo");
                DatePicker tanggalPicker = (DatePicker) form.lookup("#tanggalPicker");
                TextField jamMasukField = (TextField) form.lookup("#jamMasukField");
            TextField jamPulangField = (TextField) form.lookup("#jamPulangField");
            ComboBox<String> statusCombo = (ComboBox<String>) form.lookup("#statusCombo");
            ComboBox<String> methodCombo = (ComboBox<String>) form.lookup("#methodCombo");
            TextField keteranganField = (TextField) form.lookup("#keteranganField");
            Button selectUserButton = (Button) form.lookup("#selectUserButton");

                tipeCombo.setItems(FXCollections.observableArrayList("SISWA", "GURU"));
                statusCombo.setItems(FXCollections.observableArrayList(STATUS_OPTIONS));
                methodCombo.setItems(FXCollections.observableArrayList("MANUAL", "RFID", "BARCODE", "FACE"));

                userIdField.setText(selected.getUserId() != null ? selected.getUserId().toString() : "");
                usernameField.setText(selected.getUsername());
                tipeCombo.setValue(selected.getTipe());
                tanggalPicker.setValue(selected.getTanggal());
                if (selected.getJamMasuk() != null) {
                    jamMasukField.setText(selected.getJamMasuk().toString().substring(0,5));
                }
                if (selected.getJamPulang() != null) {
                    jamPulangField.setText(selected.getJamPulang().toString().substring(0,5));
                }
            statusCombo.setValue(selected.getStatus());
            methodCombo.setValue(selected.getMethod());
            keteranganField.setText(selected.getKeterangan());

            if (selectUserButton != null) {
                selectUserButton.setOnAction(e -> openUserLookup(userIdField, usernameField));
            }

                dialog.getDialogPane().setContent(form);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                dialog.setResultConverter(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        try {
                            selected.setUserId(Long.parseLong(userIdField.getText()));
                        } catch (NumberFormatException ex) {
                            return null;
                        }
                        selected.setUsername(usernameField.getText());
                        selected.setTipe(tipeCombo.getValue());
                        selected.setTanggal(tanggalPicker.getValue());
                        if (!jamMasukField.getText().isBlank()) {
                            selected.setJamMasuk(LocalTime.parse(jamMasukField.getText(), timeFormatter));
                        } else {
                            selected.setJamMasuk(null);
                        }
                        if (!jamPulangField.getText().isBlank()) {
                            selected.setJamPulang(LocalTime.parse(jamPulangField.getText(), timeFormatter));
                        } else {
                            selected.setJamPulang(null);
                        }
                        selected.setStatus(statusCombo.getValue());
                        selected.setMethod(methodCombo.getValue());
                        selected.setKeterangan(keteranganField.getText());
                        return selected;
                    }
                    return null;
                });

                Optional<Presensi> result = dialog.showAndWait();
                result.ifPresent(updated -> {
                    setLoading(true);
                    new Thread(() -> {
                        try {
                            Presensi updatedFromServer = presensiService.updatePresensi(updated);
                            Platform.runLater(() -> {
                                setLoading(false);
                                if (updatedFromServer != null) {
                                    loadData();
                                    InAppNotification.show("Presensi berhasil diupdate",
                                            presensiTable.getParent(),
                                            InAppNotification.NotificationType.SUCCESS, 3);
                                } else {
                                    InAppNotification.show("Gagal mengupdate presensi",
                                            presensiTable.getParent(),
                                            InAppNotification.NotificationType.ERROR, 5);
                                }
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> {
                                setLoading(false);
                                InAppNotification.show("Error: " + ex.getMessage(),
                                        presensiTable.getParent(),
                                        InAppNotification.NotificationType.ERROR, 5);
                            });
                        }
                    }).start();
                });
            } catch (Exception e) {
                e.printStackTrace();
                InAppNotification.show("Error: " + e.getMessage(),
                        presensiTable.getParent(),
                        InAppNotification.NotificationType.ERROR, 5);
            }
        }
    }

    /**
     * Dialog sederhana untuk memilih User (ADMIN/GURU/SISWA) dari daftar /api/users.
     * Mengisi userIdField dan usernameField berdasarkan pilihan.
     */
    private void openUserLookup(TextField userIdField, TextField usernameField) {
        try {
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Pilih User");
            dialog.setHeaderText("Pilih user untuk presensi");

            TableView<User> tableView = new TableView<>();
            TableColumn<User, Long> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(80);

            TableColumn<User, String> usernameCol = new TableColumn<>("Username");
            usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
            usernameCol.setPrefWidth(180);

            TableColumn<User, String> roleCol = new TableColumn<>("Role");
            roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
            roleCol.setPrefWidth(120);

            tableView.getColumns().addAll(idCol, usernameCol, roleCol);
            tableView.setPrefHeight(300);

            Label info = new Label("Catatan: daftar user berasal dari /api/users (role ADMIN/GURU/SISWA).");

            VBox content = new VBox(10, info, tableView);
            content.setPrefWidth(420);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Load data (synchronous, daftar biasanya kecil)
            ApiClient apiClient = ApiClient.getInstance();
            UserService userService = new UserService(apiClient);
            List<User> users = userService.getAllUsers();
            tableView.setItems(FXCollections.observableArrayList(users));

            // Default selection
            if (!users.isEmpty()) {
                tableView.getSelectionModel().selectFirst();
            }

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return tableView.getSelectionModel().getSelectedItem();
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();
            result.ifPresent(user -> {
                if (user.getId() != null) {
                    userIdField.setText(user.getId().toString());
                }
                if (user.getUsername() != null) {
                    usernameField.setText(user.getUsername());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            InAppNotification.show("Error memuat daftar user: " + e.getMessage(),
                    presensiTable.getParent(),
                    InAppNotification.NotificationType.ERROR, 5);
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
                    setLoading(true);
                    new Thread(() -> {
                        try {
                            boolean success = presensiService.deletePresensi(selected.getId());
                            Platform.runLater(() -> {
                                setLoading(false);
                                if (success) {
                                    loadData();
                                    InAppNotification.show("Presensi berhasil dihapus",
                                            presensiTable.getParent(),
                                            InAppNotification.NotificationType.SUCCESS, 3);
                                } else {
                                    InAppNotification.show("Gagal menghapus presensi",
                                            presensiTable.getParent(),
                                            InAppNotification.NotificationType.ERROR, 5);
                                }
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> {
                                setLoading(false);
                                InAppNotification.show("Error: " + ex.getMessage(),
                                        presensiTable.getParent(),
                                        InAppNotification.NotificationType.ERROR, 5);
                            });
                        }
                    }).start();
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
