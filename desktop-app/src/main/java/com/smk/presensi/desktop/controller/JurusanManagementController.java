package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Jurusan;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.JurusanService;
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

public class JurusanManagementController implements Initializable {

    @FXML private TableView<Jurusan> jurusanTable;
    @FXML private TableColumn<Jurusan, Long> idColumn;
    @FXML private TableColumn<Jurusan, String> kodeColumn;
    @FXML private TableColumn<Jurusan, String> namaColumn;
    @FXML private TableColumn<Jurusan, Integer> durasiColumn;
    @FXML private TableColumn<Jurusan, Long> ketuaColumn;
    @FXML private TableColumn<Jurusan, Void> actionColumn;

    @FXML private TextField searchField;

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

    private JurusanService jurusanService;
    private ObservableList<Jurusan> jurusanList;
    private ObservableList<Jurusan> filteredList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApiClient apiClient = ApiClient.getInstance();
        jurusanService = new JurusanService(apiClient);

        jurusanList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupTableColumns();
        setupEventHandlers();
        loadData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        kodeColumn.setCellValueFactory(new PropertyValueFactory<>("kode"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        durasiColumn.setCellValueFactory(new PropertyValueFactory<>("durasiTahun"));
        ketuaColumn.setCellValueFactory(new PropertyValueFactory<>("ketuaJurusanId"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-warning");
                deleteBtn.getStyleClass().add("btn-danger");

                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        jurusanTable.setItems(filteredList);

        jurusanTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> applyFilter());
        resetButton.setOnAction(e -> {
            searchField.clear();
            applyFilter();
        });
        refreshButton.setOnAction(e -> loadData());
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> {
            Jurusan selected = jurusanTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEdit(selected);
            }
        });
        deleteButton.setOnAction(e -> {
            Jurusan selected = jurusanTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDelete(selected);
            }
        });
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
    }

    private void loadData() {
        setLoading(true, "Memuat data jurusan...");
        new Thread(() -> {
            try {
                List<Jurusan> data = jurusanService.getAllJurusan();
                Platform.runLater(() -> {
                    jurusanList.setAll(data);
                    applyFilter();
                    infoLabel.setText("Total: " + jurusanList.size() + " jurusan");
                    setLoading(false, "Data jurusan berhasil dimuat");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memuat data jurusan");
                    InAppNotification.show("Gagal memuat data jurusan: " + e.getMessage(),
                            jurusanTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private void applyFilter() {
        String keyword = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

        List<Jurusan> filtered = jurusanList.stream()
                .filter(j -> keyword.isEmpty()
                        || (j.getKode() != null && j.getKode().toLowerCase().contains(keyword))
                        || (j.getNama() != null && j.getNama().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
        infoLabel.setText("Total: " + filtered.size() + " jurusan (filtered)");
    }

    private void handleAdd() {
        Jurusan jurusan = showJurusanForm(null);
        if (jurusan != null) {
            setLoading(true, "Menyimpan jurusan...");
            new Thread(() -> {
                Jurusan created = jurusanService.createJurusan(jurusan);
                Platform.runLater(() -> {
                    setLoading(false, created != null ? "Jurusan berhasil dibuat" : "Gagal membuat jurusan");
                    if (created != null) {
                        jurusanList.add(created);
                        applyFilter();
                        InAppNotification.show("Jurusan berhasil dibuat",
                                jurusanTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    } else {
                        InAppNotification.show("Gagal membuat jurusan",
                                jurusanTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    }
                });
            }).start();
        }
    }

    private void handleEdit(Jurusan jurusan) {
        if (jurusan == null) return;

        Jurusan updatedData = showJurusanForm(jurusan);
        if (updatedData != null) {
            setLoading(true, "Mengupdate jurusan...");
            new Thread(() -> {
                Jurusan updated = jurusanService.updateJurusan(jurusan.getId(), updatedData);
                Platform.runLater(() -> {
                    setLoading(false, updated != null ? "Jurusan berhasil diupdate" : "Gagal mengupdate jurusan");
                    if (updated != null) {
                        int idx = jurusanList.indexOf(jurusan);
                        if (idx >= 0) {
                            jurusanList.set(idx, updated);
                            applyFilter();
                        }
                        InAppNotification.show("Jurusan berhasil diupdate",
                                jurusanTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    } else {
                        InAppNotification.show("Gagal mengupdate jurusan",
                                jurusanTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    }
                });
            }).start();
        }
    }

    private void handleDelete(Jurusan jurusan) {
        if (jurusan == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Jurusan");
        alert.setContentText("Yakin ingin menghapus jurusan \"" + jurusan.getNama() + "\"?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            setLoading(true, "Menghapus jurusan...");
            new Thread(() -> {
                boolean success = jurusanService.deleteJurusan(jurusan.getId());
                Platform.runLater(() -> {
                    setLoading(false, success ? "Jurusan berhasil dihapus" : "Gagal menghapus jurusan");
                    if (success) {
                        jurusanList.remove(jurusan);
                        applyFilter();
                        InAppNotification.show("Jurusan berhasil dihapus",
                                jurusanTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    } else {
                        InAppNotification.show("Gagal menghapus jurusan",
                                jurusanTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    }
                });
            }).start();
        }
    }

    private Jurusan showJurusanForm(Jurusan existing) {
        Dialog<Jurusan> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Tambah Jurusan" : "Edit Jurusan");
        dialog.setHeaderText(existing == null ? "Tambah jurusan baru" : "Edit jurusan");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField kodeField = new TextField();
        TextField namaField = new TextField();
        TextField durasiField = new TextField();
        TextField ketuaField = new TextField();

        kodeField.setPromptText("Kode (misal: RPL)");
        namaField.setPromptText("Nama jurusan");
        durasiField.setPromptText("Durasi tahun (opsional)");
        ketuaField.setPromptText("ID ketua jurusan (opsional)");

        if (existing != null) {
            kodeField.setText(existing.getKode());
            namaField.setText(existing.getNama());
            if (existing.getDurasiTahun() != null) {
                durasiField.setText(existing.getDurasiTahun().toString());
            }
            if (existing.getKetuaJurusanId() != null) {
                ketuaField.setText(existing.getKetuaJurusanId().toString());
            }
        }

        grid.add(new Label("Kode:"), 0, 0);
        grid.add(kodeField, 1, 0);
        grid.add(new Label("Nama:"), 0, 1);
        grid.add(namaField, 1, 1);
        grid.add(new Label("Durasi (tahun):"), 0, 2);
        grid.add(durasiField, 1, 2);
        grid.add(new Label("Ketua Jurusan ID:"), 0, 3);
        grid.add(ketuaField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (kodeField.getText() == null || kodeField.getText().trim().isEmpty()
                        || namaField.getText() == null || namaField.getText().trim().isEmpty()) {
                    InAppNotification.show("Kode dan nama jurusan wajib diisi",
                            jurusanTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }
                Jurusan j = existing != null ? existing : new Jurusan();
                j.setKode(kodeField.getText().trim());
                j.setNama(namaField.getText().trim());
                try {
                    String durasiText = durasiField.getText().trim();
                    j.setDurasiTahun(durasiText.isEmpty() ? null : Integer.parseInt(durasiText));
                } catch (NumberFormatException e) {
                    InAppNotification.show("Durasi tahun harus angka",
                            jurusanTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }
                try {
                    String ketuaText = ketuaField.getText().trim();
                    j.setKetuaJurusanId(ketuaText.isEmpty() ? null : Long.parseLong(ketuaText));
                } catch (NumberFormatException e) {
                    InAppNotification.show("ID ketua jurusan harus angka",
                            jurusanTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }
                return j;
            }
            return null;
        });

        Optional<Jurusan> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void setLoading(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        statusLabel.setText(message);
    }
}
