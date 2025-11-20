package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Mapel;
import com.smk.presensi.desktop.service.MapelService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MapelManagementController implements Initializable {

    @FXML private TableView<Mapel> mapelTable;
    @FXML private TableColumn<Mapel, Long> idColumn;
    @FXML private TableColumn<Mapel, String> kodeColumn;
    @FXML private TableColumn<Mapel, String> namaColumn;
    @FXML private TableColumn<Mapel, String> deskripsiColumn;

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    @FXML private Label statusLabel;
    @FXML private Label infoLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private MapelService mapelService;
    private ObservableList<Mapel> mapelList;
    private ObservableList<Mapel> filteredList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapelService = new MapelService();
        mapelList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupTable();
        setupEventHandlers();
        loadData();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        kodeColumn.setCellValueFactory(new PropertyValueFactory<>("kode"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        deskripsiColumn.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));

        mapelTable.setItems(filteredList);
        mapelTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean has = newVal != null;
            editButton.setDisable(!has);
            deleteButton.setDisable(!has);
        });
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> applyFilter());
        resetButton.setOnAction(e -> resetFilter());
        refreshButton.setOnAction(e -> loadData());
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        searchField.setOnAction(e -> applyFilter());
    }

    private void loadData() {
        setLoading(true, "Memuat data mapel...");
        mapelList.setAll(mapelService.findAll());
        filteredList.setAll(mapelList);
        updateInfo();
        setLoading(false, "Ready");
    }

    private void applyFilter() {
        String q = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        if (q.isEmpty()) {
            filteredList.setAll(mapelList);
        } else {
            filteredList.setAll(mapelList.filtered(m ->
                    (m.getKode() != null && m.getKode().toLowerCase().contains(q)) ||
                    (m.getNama() != null && m.getNama().toLowerCase().contains(q)) ||
                    (m.getDeskripsi() != null && m.getDeskripsi().toLowerCase().contains(q))
            ));
        }
        updateInfo();
    }

    private void resetFilter() {
        searchField.clear();
        filteredList.setAll(mapelList);
        updateInfo();
    }

    private void handleAdd() {
        showFormDialog(null).ifPresent(mapel -> {
            mapelService.create(mapel);
            loadData();
            InAppNotification.show("Mapel berhasil ditambahkan",
                    mapelTable.getParent(),
                    InAppNotification.NotificationType.SUCCESS,
                    3);
        });
    }

    private void handleEdit() {
        Mapel selected = mapelTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showFormDialog(selected).ifPresent(mapel -> {
            mapelService.update(mapel);
            loadData();
            InAppNotification.show("Mapel berhasil diupdate",
                    mapelTable.getParent(),
                    InAppNotification.NotificationType.SUCCESS,
                    3);
        });
    }

    private void handleDelete() {
        Mapel selected = mapelTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus mapel: " + selected.getNama() + "?");
        confirm.setContentText("Data ini hanya tersimpan lokal (mock).");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                mapelService.delete(selected.getId());
                loadData();
                InAppNotification.show("Mapel berhasil dihapus",
                        mapelTable.getParent(),
                        InAppNotification.NotificationType.SUCCESS,
                        3);
            }
        });
    }

    private Optional<Mapel> showFormDialog(Mapel existing) {
        Dialog<Mapel> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Tambah Mapel" : "Edit Mapel");
        dialog.setHeaderText(existing == null ? "Tambah mata pelajaran baru" : "Edit mata pelajaran");

        ButtonType saveButton = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField kodeField = new TextField();
        TextField namaField = new TextField();
        TextArea deskArea = new TextArea();
        deskArea.setPrefRowCount(3);

        if (existing != null) {
            kodeField.setText(existing.getKode());
            namaField.setText(existing.getNama());
            deskArea.setText(existing.getDeskripsi());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Kode:"), kodeField);
        grid.addRow(1, new Label("Nama:"), namaField);
        grid.addRow(2, new Label("Deskripsi:"), deskArea);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                String kode = kodeField.getText() != null ? kodeField.getText().trim() : "";
                String nama = namaField.getText() != null ? namaField.getText().trim() : "";
                String desk = deskArea.getText() != null ? deskArea.getText().trim() : "";
                if (kode.isEmpty() || nama.isEmpty()) {
                    InAppNotification.show("Kode dan Nama wajib diisi",
                            mapelTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            4);
                    return null;
                }
                Mapel m = existing != null ? existing : new Mapel();
                m.setKode(kode);
                m.setNama(nama);
                m.setDeskripsi(desk);
                return m;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void setLoading(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        statusLabel.setText(message);
    }

    private void updateInfo() {
        infoLabel.setText("Total: " + filteredList.size() + " mapel");
    }
}

