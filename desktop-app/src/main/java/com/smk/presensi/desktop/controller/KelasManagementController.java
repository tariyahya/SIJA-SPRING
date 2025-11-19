package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Kelas;
import com.smk.presensi.desktop.model.Siswa;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.KelasService;
import com.smk.presensi.desktop.service.SiswaService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class KelasManagementController implements Initializable {

    @FXML private TableView<Kelas> kelasTable;
    @FXML private TableColumn<Kelas, Long> idColumn;
    @FXML private TableColumn<Kelas, String> namaColumn;
    @FXML private TableColumn<Kelas, String> tingkatColumn;
    @FXML private TableColumn<Kelas, String> jurusanColumn;
    @FXML private TableColumn<Kelas, Integer> kapasitasColumn;
    @FXML private TableColumn<Kelas, Long> waliColumn;
    @FXML private TableColumn<Kelas, Void> actionColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> jurusanFilter;
    @FXML private ComboBox<String> tingkatFilter;

    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;
    @FXML private Button assignSiswaButton;

    @FXML private Label statusLabel;
    @FXML private Label infoLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private KelasService kelasService;
    private SiswaService siswaService;
    private ObservableList<Kelas> kelasList;
    private ObservableList<Kelas> filteredList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApiClient apiClient = ApiClient.getInstance();
        kelasService = new KelasService(apiClient);
        siswaService = new SiswaService(apiClient);

        kelasList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupTableColumns();
        setupFilters();
        setupEventHandlers();
        loadData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        tingkatColumn.setCellValueFactory(new PropertyValueFactory<>("tingkat"));
        jurusanColumn.setCellValueFactory(new PropertyValueFactory<>("jurusan"));
        if (kapasitasColumn != null) {
            kapasitasColumn.setCellValueFactory(new PropertyValueFactory<>("kapasitas"));
        }
        waliColumn.setCellValueFactory(new PropertyValueFactory<>("waliKelasId"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button assignBtn = new Button("Assign Siswa");
            private final HBox pane = new HBox(5, editBtn, deleteBtn, assignBtn);

            {
                editBtn.getStyleClass().add("btn-warning");
                deleteBtn.getStyleClass().add("btn-danger");
                assignBtn.getStyleClass().add("btn-info");

                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
                assignBtn.setOnAction(e -> handleAssignSiswa(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        kelasTable.setItems(filteredList);

        kelasTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            assignSiswaButton.setDisable(!hasSelection);
        });
    }

    private void setupFilters() {
        tingkatFilter.setItems(FXCollections.observableArrayList("X", "XI", "XII"));
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> applyFilter());
        resetButton.setOnAction(e -> {
            searchField.clear();
            jurusanFilter.getSelectionModel().clearSelection();
            tingkatFilter.getSelectionModel().clearSelection();
            applyFilter();
        });
        refreshButton.setOnAction(e -> loadData());
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> {
            Kelas selected = kelasTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEdit(selected);
            }
        });
        deleteButton.setOnAction(e -> {
            Kelas selected = kelasTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDelete(selected);
            }
        });
        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
        assignSiswaButton.setOnAction(e -> {
            Kelas selected = kelasTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleAssignSiswa(selected);
            }
        });
    }

    private void loadData() {
        setLoading(true, "Memuat data kelas...");
        new Thread(() -> {
            try {
                List<Kelas> data = kelasService.getAllKelas();
                Platform.runLater(() -> {
                    kelasList.setAll(data);
                    refreshJurusanFilter();
                    applyFilter();
                    infoLabel.setText("Total: " + kelasList.size() + " kelas");
                    setLoading(false, "Data kelas berhasil dimuat");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memuat data kelas");
                    InAppNotification.show("Gagal memuat data kelas: " + e.getMessage(),
                            kelasTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private void refreshJurusanFilter() {
        Set<String> jurusanSet = new TreeSet<>();
        for (Kelas k : kelasList) {
            if (k.getJurusan() != null && !k.getJurusan().isBlank()) {
                jurusanSet.add(k.getJurusan());
            }
        }
        jurusanFilter.setItems(FXCollections.observableArrayList(jurusanSet));
    }

    private void applyFilter() {
        String keyword = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String jurusan = jurusanFilter.getSelectionModel().getSelectedItem();
        String tingkat = tingkatFilter.getSelectionModel().getSelectedItem();

        List<Kelas> filtered = kelasList.stream()
                .filter(k -> keyword.isEmpty()
                        || (k.getNama() != null && k.getNama().toLowerCase().contains(keyword)))
                .filter(k -> jurusan == null || jurusan.isBlank()
                        || (k.getJurusan() != null && k.getJurusan().equalsIgnoreCase(jurusan)))
                .filter(k -> tingkat == null || tingkat.isBlank()
                        || (k.getTingkat() != null && k.getTingkat().equalsIgnoreCase(tingkat)))
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
        infoLabel.setText("Total: " + filtered.size() + " kelas (filtered)");
    }

    private void handleAdd() {
        Kelas kelas = showKelasForm(null);
        if (kelas != null) {
            setLoading(true, "Menyimpan kelas...");
            new Thread(() -> {
                Kelas created = kelasService.createKelas(kelas);
                Platform.runLater(() -> {
                    setLoading(false, created != null ? "Kelas berhasil dibuat" : "Gagal membuat kelas");
                    if (created != null) {
                        kelasList.add(created);
                        refreshJurusanFilter();
                        applyFilter();
                        InAppNotification.show("Kelas berhasil dibuat",
                                kelasTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    } else {
                        InAppNotification.show("Gagal membuat kelas",
                                kelasTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    }
                });
            }).start();
        }
    }

    private void handleEdit(Kelas kelas) {
        if (kelas == null) return;

        Kelas updatedData = showKelasForm(kelas);
        if (updatedData != null) {
            setLoading(true, "Mengupdate kelas...");
            new Thread(() -> {
                Kelas updated = kelasService.updateKelas(kelas.getId(), updatedData);
                Platform.runLater(() -> {
                    setLoading(false, updated != null ? "Kelas berhasil diupdate" : "Gagal mengupdate kelas");
                    if (updated != null) {
                        int idx = kelasList.indexOf(kelas);
                        if (idx >= 0) {
                            kelasList.set(idx, updated);
                            refreshJurusanFilter();
                            applyFilter();
                        }
                        InAppNotification.show("Kelas berhasil diupdate",
                                kelasTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    } else {
                        InAppNotification.show("Gagal mengupdate kelas",
                                kelasTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    }
                });
            }).start();
        }
    }

    private void handleDelete(Kelas kelas) {
        if (kelas == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Kelas");
        alert.setContentText("Yakin ingin menghapus kelas \"" + kelas.getNama() + "\"?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            setLoading(true, "Menghapus kelas...");
            new Thread(() -> {
                boolean success = kelasService.deleteKelas(kelas.getId());
                Platform.runLater(() -> {
                    setLoading(false, success ? "Kelas berhasil dihapus" : "Gagal menghapus kelas");
                    if (success) {
                        kelasList.remove(kelas);
                        refreshJurusanFilter();
                        applyFilter();
                        InAppNotification.show("Kelas berhasil dihapus",
                                kelasTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    } else {
                        InAppNotification.show("Gagal menghapus kelas",
                                kelasTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    }
                });
            }).start();
        }
    }

    private void handleAssignSiswa(Kelas kelas) {
        if (kelas == null) return;

        setLoading(true, "Memuat data siswa untuk kelas " + kelas.getNama() + "...");
        new Thread(() -> {
            try {
                List<Siswa> allSiswa = siswaService.getAllSiswa();
                Platform.runLater(() -> {
                    setLoading(false, "Data siswa dimuat");
                    List<Siswa> selected = showAssignDialog(kelas, allSiswa);
                    if (selected != null && !selected.isEmpty()) {
                        doAssignSiswa(kelas, selected);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memuat data siswa");
                    InAppNotification.show("Gagal memuat data siswa: " + e.getMessage(),
                            kelasTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private List<Siswa> showAssignDialog(Kelas kelas, List<Siswa> allSiswa) {
        Dialog<List<Siswa>> dialog = new Dialog<>();
        dialog.setTitle("Assign Siswa ke " + kelas.getNama());
        dialog.setHeaderText("Pilih siswa yang akan di-assign ke kelas ini.\n" +
                "Siswa yang sudah berada di kelas ini akan terpilih otomatis.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        List<Siswa> kandidat = allSiswa.stream()
                .filter(s -> kelas.getJurusan() == null
                        || s.getJurusan() == null
                        || s.getJurusan().equalsIgnoreCase(kelas.getJurusan()))
                .collect(Collectors.toList());

        ListView<Siswa> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setItems(FXCollections.observableArrayList(kandidat));

        for (Siswa s : kandidat) {
            if (s.getKelas() != null && s.getKelas().equalsIgnoreCase(kelas.getNama())) {
                listView.getSelectionModel().select(s);
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Daftar siswa (jurusan sama atau tanpa jurusan):"), 0, 0);
        grid.add(listView, 0, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        Optional<List<Siswa>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void doAssignSiswa(Kelas kelas, List<Siswa> selected) {
        List<Long> ids = selected.stream()
                .map(Siswa::getId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            InAppNotification.show("Tidak ada siswa valid yang dipilih untuk di-assign",
                    kelasTable.getParent(),
                    InAppNotification.NotificationType.WARNING,
                    3);
            return;
        }

        setLoading(true, "Meng-assign " + ids.size() + " siswa ke " + kelas.getNama() + "...");
        new Thread(() -> {
            List<Siswa> updated = kelasService.assignSiswaToKelas(kelas.getId(), ids);
            Platform.runLater(() -> {
                setLoading(false, updated.isEmpty()
                        ? "Gagal meng-assign siswa ke kelas"
                        : "Berhasil meng-assign siswa ke kelas");

                if (updated.isEmpty()) {
                    InAppNotification.show("Gagal meng-assign siswa ke kelas",
                            kelasTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                } else {
                    InAppNotification.show("Berhasil meng-assign " + updated.size() + " siswa ke " + kelas.getNama(),
                            kelasTable.getParent(),
                            InAppNotification.NotificationType.SUCCESS,
                            3);
                }
            });
        }).start();
    }

    private Kelas showKelasForm(Kelas existing) {
        Dialog<Kelas> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Tambah Kelas" : "Edit Kelas");
        dialog.setHeaderText(existing == null ? "Tambah kelas baru" : "Edit kelas");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField namaField = new TextField();
        ComboBox<String> tingkatCombo = new ComboBox<>();
        TextField jurusanField = new TextField();
        TextField kapasitasField = new TextField();
        TextField waliField = new TextField();

        namaField.setPromptText("Nama kelas (misal: XII SIJA 1)");
        tingkatCombo.setItems(FXCollections.observableArrayList("X", "XI", "XII"));
        tingkatCombo.setPromptText("Tingkat");
        jurusanField.setPromptText("Jurusan (misal: SIJA)");
        kapasitasField.setPromptText("Kapasitas siswa (kosongkan jika tidak dibatasi)");
        waliField.setPromptText("ID wali kelas (opsional)");

        if (existing != null) {
            namaField.setText(existing.getNama());
            if (existing.getTingkat() != null) {
                tingkatCombo.getSelectionModel().select(existing.getTingkat());
            }
            jurusanField.setText(existing.getJurusan());
            if (existing.getWaliKelasId() != null) {
                waliField.setText(existing.getWaliKelasId().toString());
            }
            if (existing.getKapasitas() != null) {
                kapasitasField.setText(existing.getKapasitas().toString());
            }
        }

        grid.add(new Label("Nama Kelas:"), 0, 0);
        grid.add(namaField, 1, 0);
        grid.add(new Label("Tingkat:"), 0, 1);
        grid.add(tingkatCombo, 1, 1);
        grid.add(new Label("Jurusan:"), 0, 2);
        grid.add(jurusanField, 1, 2);
        grid.add(new Label("Kapasitas:"), 0, 3);
        grid.add(kapasitasField, 1, 3);
        grid.add(new Label("Wali Kelas ID:"), 0, 4);
        grid.add(waliField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (namaField.getText() == null || namaField.getText().trim().isEmpty()) {
                    InAppNotification.show("Nama kelas wajib diisi",
                            kelasTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }
                Kelas k = existing != null ? existing : new Kelas();
                k.setNama(namaField.getText().trim());
                k.setTingkat(tingkatCombo.getSelectionModel().getSelectedItem());
                k.setJurusan(jurusanField.getText() != null ? jurusanField.getText().trim() : null);
                try {
                    String kapasitasText = kapasitasField.getText() != null ? kapasitasField.getText().trim() : "";
                    k.setKapasitas(kapasitasText.isEmpty() ? null : Integer.parseInt(kapasitasText));
                } catch (NumberFormatException e) {
                    InAppNotification.show("Kapasitas harus berupa angka",
                            kelasTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }
                try {
                    String waliText = waliField.getText().trim();
                    k.setWaliKelasId(waliText.isEmpty() ? null : Long.parseLong(waliText));
                } catch (NumberFormatException e) {
                    InAppNotification.show("ID wali kelas harus angka",
                            kelasTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }
                return k;
            }
            return null;
        });

        Optional<Kelas> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void setLoading(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        statusLabel.setText(message);
    }
}
