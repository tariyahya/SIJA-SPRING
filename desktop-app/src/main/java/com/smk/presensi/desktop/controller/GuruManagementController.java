package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Guru;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.GuruService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
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
 * Controller for Guru Management View
 * Handles CRUD operations for teachers
 */
public class GuruManagementController implements Initializable {
    
    @FXML private TableView<Guru> guruTable;
    @FXML private TableColumn<Guru, Long> idColumn;
    @FXML private TableColumn<Guru, String> nipColumn;
    @FXML private TableColumn<Guru, String> namaColumn;
    @FXML private TableColumn<Guru, String> mapelColumn;
    @FXML private TableColumn<Guru, String> rfidColumn;
    @FXML private TableColumn<Guru, String> barcodeColumn;
    @FXML private TableColumn<Guru, Void> actionColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> mapelFilter;
    
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;
    
    @FXML private Label statusLabel;
    @FXML private Label infoLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private CheckBox mockDataCheckbox;
    
    private GuruService guruService;
    private ObservableList<Guru> guruList;
    private ObservableList<Guru> filteredList;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApiClient apiClient = ApiClient.getInstance();
        guruService = new GuruService(apiClient);
        
        guruList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();
        
        setupTableColumns();
        setupFilters();
        setupEventHandlers();
        loadData();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nipColumn.setCellValueFactory(new PropertyValueFactory<>("nip"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mapel"));
        rfidColumn.setCellValueFactory(new PropertyValueFactory<>("rfidCardId"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcodeId"));
        
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
        
        guruTable.setItems(filteredList);
        
        guruTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }
    
    private void setupFilters() {
        mapelFilter.setItems(FXCollections.observableArrayList(
            "Matematika", "Bahasa Indonesia", "Bahasa Inggris", 
            "Pemrograman Web", "Pemrograman Mobile", "Basis Data",
            "Jaringan Komputer", "Sistem Operasi", "PKN", "Agama", "Olahraga"
        ));
    }
    
    private void setupEventHandlers() {
        searchButton.setOnAction(e -> handleSearch());
        resetButton.setOnAction(e -> handleReset());
        addButton.setOnAction(e -> handleAdd());
        refreshButton.setOnAction(e -> loadData());
        editButton.setOnAction(e -> handleEdit(guruTable.getSelectionModel().getSelectedItem()));
        deleteButton.setOnAction(e -> handleDelete(guruTable.getSelectionModel().getSelectedItem()));
        closeButton.setOnAction(e -> handleClose());
        mockDataCheckbox.setOnAction(e -> loadData());
        searchField.setOnAction(e -> handleSearch());
    }
    
    private void loadData() {
        setLoading(true);
        statusLabel.setText("Loading...");
        
        new Thread(() -> {
            try {
                List<Guru> data = mockDataCheckbox.isSelected() ? 
                    guruService.getMockData() : guruService.getAllGuru();
                
                Platform.runLater(() -> {
                    guruList.setAll(data);
                    filteredList.setAll(data);
                    updateInfoLabel();
                    statusLabel.setText("Ready");
                    setLoading(false);
                    InAppNotification.show("Data loaded", guruTable.getParent(), 
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
        String selectedMapel = mapelFilter.getValue();
        
        List<Guru> filtered = guruList.stream()
            .filter(g -> {
                boolean matchSearch = searchText.isEmpty() || 
                    g.getNip().toLowerCase().contains(searchText) ||
                    g.getNama().toLowerCase().contains(searchText);
                
                boolean matchMapel = selectedMapel == null || 
                    (g.getMapel() != null && g.getMapel().equals(selectedMapel));
                
                return matchSearch && matchMapel;
            })
            .collect(Collectors.toList());
        
        filteredList.setAll(filtered);
        updateInfoLabel();
    }
    
    @FXML
    private void handleReset() {
        searchField.clear();
        mapelFilter.setValue(null);
        filteredList.setAll(guruList);
        updateInfoLabel();
    }
    
    @FXML
    private void handleAdd() {
        try {
            Dialog<Guru> dialog = new Dialog<>();
            dialog.setTitle("Tambah Guru Baru");
            dialog.setHeaderText("Masukkan data guru");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/guru-form-dialog.fxml"));
            GridPane form = loader.load();
            
            TextField nipField = (TextField) form.lookup("#nipField");
            TextField namaField = (TextField) form.lookup("#namaField");
            ComboBox<String> mapelCombo = (ComboBox<String>) form.lookup("#mapelCombo");
            TextField rfidField = (TextField) form.lookup("#rfidField");
            TextField barcodeField = (TextField) form.lookup("#barcodeField");
            
            // Populate mapel ComboBox
            mapelCombo.setItems(FXCollections.observableArrayList(
                "Matematika", "Bahasa Indonesia", "Bahasa Inggris", 
                "Pemrograman Web", "Pemrograman Mobile", "Basis Data",
                "Jaringan Komputer", "Sistem Operasi", "PKN", "Agama", "Olahraga"
            ));
            
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Guru guru = new Guru();
                    guru.setNip(nipField.getText());
                    guru.setNama(namaField.getText());
                    guru.setMapel(mapelCombo.getValue());
                    guru.setRfidCardId(rfidField.getText());
                    guru.setBarcodeId(barcodeField.getText());
                    return guru;
                }
                return null;
            });
            
            Optional<Guru> result = dialog.showAndWait();
            result.ifPresent(guru -> {
                setLoading(true);
                new Thread(() -> {
                    Guru created = guruService.createGuru(guru);
                    Platform.runLater(() -> {
                        setLoading(false);
                        if (created != null) {
                            loadData();
                            InAppNotification.show("Guru berhasil ditambahkan", 
                                                  guruTable.getParent(), 
                                                  InAppNotification.NotificationType.SUCCESS, 3);
                        } else {
                            InAppNotification.show("Gagal menambahkan guru", 
                                                  guruTable.getParent(), 
                                                  InAppNotification.NotificationType.ERROR, 5);
                        }
                    });
                }).start();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleEdit(Guru guru) {
        if (guru == null) return;

        try {
            Dialog<Guru> dialog = new Dialog<>();
            dialog.setTitle("Edit Guru");
            dialog.setHeaderText("Edit data guru: " + guru.getNama());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/guru-form-dialog.fxml"));
            GridPane form = loader.load();

            TextField nipField = (TextField) form.lookup("#nipField");
            TextField namaField = (TextField) form.lookup("#namaField");
            ComboBox<String> mapelCombo = (ComboBox<String>) form.lookup("#mapelCombo");
            TextField rfidField = (TextField) form.lookup("#rfidField");
            TextField barcodeField = (TextField) form.lookup("#barcodeField");

            mapelCombo.setItems(FXCollections.observableArrayList(
                "Matematika", "Bahasa Indonesia", "Bahasa Inggris",
                "Pemrograman Web", "Pemrograman Mobile", "Basis Data",
                "Jaringan Komputer", "Sistem Operasi", "PKN", "Agama", "Olahraga"
            ));

            nipField.setText(guru.getNip());
            nipField.setDisable(true);
            namaField.setText(guru.getNama());
            mapelCombo.setValue(guru.getMapel());
            rfidField.setText(guru.getRfidCardId());
            barcodeField.setText(guru.getBarcodeId());

            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    guru.setNama(namaField.getText());
                    guru.setMapel(mapelCombo.getValue());
                    guru.setRfidCardId(rfidField.getText());
                    guru.setBarcodeId(barcodeField.getText());
                    return guru;
                }
                return null;
            });

            Optional<Guru> result = dialog.showAndWait();
            result.ifPresent(updatedGuru -> {
                setLoading(true);
                new Thread(() -> {
                    Guru updated = guruService.updateGuru(updatedGuru.getId(), updatedGuru);
                    Platform.runLater(() -> {
                        setLoading(false);
                        if (updated != null) {
                            loadData();
                            InAppNotification.show("Guru berhasil diupdate",
                                    guruTable.getParent(),
                                    InAppNotification.NotificationType.SUCCESS, 3);
                        } else {
                            InAppNotification.show("Gagal mengupdate guru",
                                    guruTable.getParent(),
                                    InAppNotification.NotificationType.ERROR, 5);
                        }
                    });
                }).start();
            });
        } catch (Exception e) {
            e.printStackTrace();
            InAppNotification.show("Error: " + e.getMessage(),
                    guruTable.getParent(),
                    InAppNotification.NotificationType.ERROR, 5);
        }
    }
    
    private void handleView(Guru guru) {
        if (guru == null) return;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail Guru");
        alert.setHeaderText(guru.getNama());
        alert.setContentText(
            "ID: " + guru.getId() + "\n" +
            "NIP: " + guru.getNip() + "\n" +
            "Nama: " + guru.getNama() + "\n" +
            "Mata Pelajaran: " + guru.getMapel() + "\n" +
            "RFID: " + guru.getRfidCardId() + "\n" +
            "Barcode: " + guru.getBarcodeId()
        );
        alert.showAndWait();
    }
    
    private void handleDelete(Guru guru) {
        if (guru == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus guru: " + guru.getNama() + "?");
        confirm.setContentText("Data yang dihapus tidak dapat dikembalikan.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            setLoading(true);
            new Thread(() -> {
                boolean success = guruService.deleteGuru(guru.getId());
                Platform.runLater(() -> {
                    setLoading(false);
                    if (success) {
                        loadData();
                        InAppNotification.show("Guru berhasil dihapus", 
                                              guruTable.getParent(), 
                                              InAppNotification.NotificationType.SUCCESS, 3);
                    } else {
                        InAppNotification.show("Gagal menghapus guru", 
                                              guruTable.getParent(), 
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
        infoLabel.setText("Total: " + filteredList.size() + " guru" + 
                         (filteredList.size() != guruList.size() ? 
                          " (dari " + guruList.size() + ")" : ""));
    }
}
