package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Siswa;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.SiswaService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Siswa Management View
 * Handles CRUD operations for students
 */
public class SiswaManagementController implements Initializable {
    
    // Table and columns
    @FXML private TableView<Siswa> siswaTable;
    @FXML private TableColumn<Siswa, Long> idColumn;
    @FXML private TableColumn<Siswa, String> nisColumn;
    @FXML private TableColumn<Siswa, String> namaColumn;
    @FXML private TableColumn<Siswa, String> kelasColumn;
    @FXML private TableColumn<Siswa, String> jurusanColumn;
    @FXML private TableColumn<Siswa, String> rfidColumn;
    @FXML private TableColumn<Siswa, String> barcodeColumn;
    @FXML private TableColumn<Siswa, Void> actionColumn;
    
    // Search and filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> kelasFilter;
    @FXML private ComboBox<String> jurusanFilter;
    
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
    
    private SiswaService siswaService;
    private ObservableList<Siswa> siswaList;
    private ObservableList<Siswa> filteredList;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize service
        ApiClient apiClient = new ApiClient();
        siswaService = new SiswaService(apiClient);
        
        siswaList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();
        
        // Setup table columns
        setupTableColumns();
        
        // Setup filters
        setupFilters();
        
        // Setup event handlers
        setupEventHandlers();
        
        // Load initial data
        loadData();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nisColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        jurusanColumn.setCellValueFactory(new PropertyValueFactory<>("jurusan"));
        rfidColumn.setCellValueFactory(new PropertyValueFactory<>("rfidCardId"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcodeId"));
        
        // Action column with buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox pane = new HBox(5, viewBtn, editBtn, deleteBtn);
            
            {
                viewBtn.getStyleClass().add("btn-info");
                editBtn.getStyleClass().add("btn-warning");
                deleteBtn.getStyleClass().add("btn-danger");
                
                viewBtn.setOnAction(e -> handleView(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        siswaTable.setItems(filteredList);
        
        // Enable/disable edit/delete buttons based on selection
        siswaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }
    
    private void setupFilters() {
        // Populate filter dropdowns
        kelasFilter.setItems(FXCollections.observableArrayList(
            "X RPL 1", "X RPL 2", "XI RPL 1", "XI RPL 2", "XII RPL 1", "XII RPL 2",
            "X TKJ 1", "X TKJ 2", "XI TKJ 1", "XI TKJ 2", "XII TKJ 1", "XII TKJ 2",
            "X MM 1", "XI MM 1", "XII MM 1"
        ));
        
        jurusanFilter.setItems(FXCollections.observableArrayList(
            "RPL", "TKJ", "MM", "OTKP", "AKL"
        ));
    }
    
    private void setupEventHandlers() {
        searchButton.setOnAction(e -> handleSearch());
        resetButton.setOnAction(e -> handleReset());
        addButton.setOnAction(e -> handleAdd());
        refreshButton.setOnAction(e -> loadData());
        editButton.setOnAction(e -> handleEdit(siswaTable.getSelectionModel().getSelectedItem()));
        deleteButton.setOnAction(e -> handleDelete(siswaTable.getSelectionModel().getSelectedItem()));
        closeButton.setOnAction(e -> handleClose());
        mockDataCheckbox.setOnAction(e -> loadData());
        
        // Enter key in search field
        searchField.setOnAction(e -> handleSearch());
    }
    
    private void loadData() {
        setLoading(true);
        statusLabel.setText("Loading...");
        
        new Thread(() -> {
            try {
                List<Siswa> data;
                
                if (mockDataCheckbox.isSelected()) {
                    data = siswaService.getMockData();
                } else {
                    data = siswaService.getAllSiswa();
                }
                
                Platform.runLater(() -> {
                    siswaList.setAll(data);
                    filteredList.setAll(data);
                    updateInfoLabel();
                    statusLabel.setText("Ready");
                    setLoading(false);
                    InAppNotification.show("Data loaded successfully", 
                                          siswaTable.getParent(), 
                                          InAppNotification.NotificationType.SUCCESS, 3);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    setLoading(false);
                    InAppNotification.show("Error loading data: " + e.getMessage(), 
                                          siswaTable.getParent(), 
                                          InAppNotification.NotificationType.ERROR, 5);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String selectedKelas = kelasFilter.getValue();
        String selectedJurusan = jurusanFilter.getValue();
        
        List<Siswa> filtered = siswaList.stream()
            .filter(s -> {
                boolean matchSearch = searchText.isEmpty() || 
                    s.getNis().toLowerCase().contains(searchText) ||
                    s.getNama().toLowerCase().contains(searchText) ||
                    (s.getKelas() != null && s.getKelas().toLowerCase().contains(searchText));
                
                boolean matchKelas = selectedKelas == null || 
                    (s.getKelas() != null && s.getKelas().equals(selectedKelas));
                
                boolean matchJurusan = selectedJurusan == null || 
                    (s.getJurusan() != null && s.getJurusan().equals(selectedJurusan));
                
                return matchSearch && matchKelas && matchJurusan;
            })
            .collect(Collectors.toList());
        
        filteredList.setAll(filtered);
        updateInfoLabel();
    }
    
    @FXML
    private void handleReset() {
        searchField.clear();
        kelasFilter.setValue(null);
        jurusanFilter.setValue(null);
        filteredList.setAll(siswaList);
        updateInfoLabel();
    }
    
    @FXML
    private void handleAdd() {
        try {
            // Create dialog
            Dialog<Siswa> dialog = new Dialog<>();
            dialog.setTitle("Tambah Siswa Baru");
            dialog.setHeaderText("Masukkan data siswa");
            
            // Load form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/siswa-form-dialog.fxml"));
            GridPane form = loader.load();
            
            // Get form fields
            TextField nisField = (TextField) form.lookup("#nisField");
            TextField namaField = (TextField) form.lookup("#namaField");
            TextField kelasField = (TextField) form.lookup("#kelasField");
            ComboBox<String> jurusanCombo = (ComboBox<String>) form.lookup("#jurusanCombo");
            TextField rfidField = (TextField) form.lookup("#rfidField");
            TextField barcodeField = (TextField) form.lookup("#barcodeField");
            
            // Populate jurusan ComboBox
            jurusanCombo.setItems(FXCollections.observableArrayList("RPL", "TKJ", "MM", "OTKP", "AKL"));
            
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Convert result
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Siswa siswa = new Siswa();
                    siswa.setNis(nisField.getText());
                    siswa.setNama(namaField.getText());
                    siswa.setKelas(kelasField.getText());
                    siswa.setJurusan(jurusanCombo.getValue());
                    siswa.setRfidCardId(rfidField.getText());
                    siswa.setBarcodeId(barcodeField.getText());
                    return siswa;
                }
                return null;
            });
            
            Optional<Siswa> result = dialog.showAndWait();
            result.ifPresent(siswa -> {
                setLoading(true);
                new Thread(() -> {
                    Siswa created = siswaService.createSiswa(siswa);
                    Platform.runLater(() -> {
                        setLoading(false);
                        if (created != null) {
                            loadData();
                            InAppNotification.show("Siswa berhasil ditambahkan", 
                                                  siswaTable.getParent(), 
                                                  InAppNotification.NotificationType.SUCCESS, 3);
                        } else {
                            InAppNotification.show("Gagal menambahkan siswa", 
                                                  siswaTable.getParent(), 
                                                  InAppNotification.NotificationType.ERROR, 5);
                        }
                    });
                }).start();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            InAppNotification.show("Error: " + e.getMessage(), 
                                  siswaTable.getParent(), 
                                  InAppNotification.NotificationType.ERROR, 5);
        }
    }
    
    private void handleEdit(Siswa siswa) {
        if (siswa == null) return;
        
        try {
            // Create dialog
            Dialog<Siswa> dialog = new Dialog<>();
            dialog.setTitle("Edit Siswa");
            dialog.setHeaderText("Edit data siswa: " + siswa.getNama());
            
            // Load form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/siswa-form-dialog.fxml"));
            GridPane form = loader.load();
            
            // Get form fields and populate
            TextField nisField = (TextField) form.lookup("#nisField");
            TextField namaField = (TextField) form.lookup("#namaField");
            TextField kelasField = (TextField) form.lookup("#kelasField");
            ComboBox<String> jurusanCombo = (ComboBox<String>) form.lookup("#jurusanCombo");
            TextField rfidField = (TextField) form.lookup("#rfidField");
            TextField barcodeField = (TextField) form.lookup("#barcodeField");
            
            // Populate jurusan ComboBox
            jurusanCombo.setItems(FXCollections.observableArrayList("RPL", "TKJ", "MM", "OTKP", "AKL"));
            
            nisField.setText(siswa.getNis());
            nisField.setDisable(true); // NIS tidak bisa diubah
            namaField.setText(siswa.getNama());
            kelasField.setText(siswa.getKelas());
            jurusanCombo.setValue(siswa.getJurusan());
            rfidField.setText(siswa.getRfidCardId());
            barcodeField.setText(siswa.getBarcodeId());
            
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Convert result
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    siswa.setNama(namaField.getText());
                    siswa.setKelas(kelasField.getText());
                    siswa.setJurusan(jurusanCombo.getValue());
                    siswa.setRfidCardId(rfidField.getText());
                    siswa.setBarcodeId(barcodeField.getText());
                    return siswa;
                }
                return null;
            });
            
            Optional<Siswa> result = dialog.showAndWait();
            result.ifPresent(updated -> {
                setLoading(true);
                new Thread(() -> {
                    Siswa result2 = siswaService.updateSiswa(siswa.getId(), updated);
                    Platform.runLater(() -> {
                        setLoading(false);
                        if (result2 != null) {
                            loadData();
                            InAppNotification.show("Siswa berhasil diupdate", 
                                                  siswaTable.getParent(), 
                                                  InAppNotification.NotificationType.SUCCESS, 3);
                        } else {
                            InAppNotification.show("Gagal mengupdate siswa", 
                                                  siswaTable.getParent(), 
                                                  InAppNotification.NotificationType.ERROR, 5);
                        }
                    });
                }).start();
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            InAppNotification.show("Error: " + e.getMessage(), 
                                  siswaTable.getParent(), 
                                  InAppNotification.NotificationType.ERROR, 5);
        }
    }
    
    private void handleView(Siswa siswa) {
        if (siswa == null) return;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail Siswa");
        alert.setHeaderText(siswa.getNama());
        alert.setContentText(
            "ID: " + siswa.getId() + "\n" +
            "NIS: " + siswa.getNis() + "\n" +
            "Nama: " + siswa.getNama() + "\n" +
            "Kelas: " + siswa.getKelas() + "\n" +
            "Jurusan: " + siswa.getJurusan() + "\n" +
            "RFID: " + siswa.getRfidCardId() + "\n" +
            "Barcode: " + siswa.getBarcodeId()
        );
        alert.showAndWait();
    }
    
    private void handleDelete(Siswa siswa) {
        if (siswa == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus siswa: " + siswa.getNama() + "?");
        confirm.setContentText("Data yang dihapus tidak dapat dikembalikan.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            setLoading(true);
            new Thread(() -> {
                boolean success = siswaService.deleteSiswa(siswa.getId());
                Platform.runLater(() -> {
                    setLoading(false);
                    if (success) {
                        loadData();
                        InAppNotification.show("Siswa berhasil dihapus", 
                                              siswaTable.getParent(), 
                                              InAppNotification.NotificationType.SUCCESS, 3);
                    } else {
                        InAppNotification.show("Gagal menghapus siswa", 
                                              siswaTable.getParent(), 
                                              InAppNotification.NotificationType.ERROR, 5);
                    }
                });
            }).start();
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
        infoLabel.setText("Total: " + filteredList.size() + " siswa" + 
                         (filteredList.size() != siswaList.size() ? 
                          " (dari " + siswaList.size() + ")" : ""));
    }
}
