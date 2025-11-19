package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Dudi;
import com.smk.presensi.desktop.model.PenempatanPkl;
import com.smk.presensi.desktop.model.Siswa;
import com.smk.presensi.desktop.service.*;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller untuk manajemen DUDI & penempatan PKL.
 */
public class DudiManagementController implements Initializable {

    @FXML private TableView<Dudi> dudiTable;
    @FXML private TableColumn<Dudi, String> namaColumn;
    @FXML private TableColumn<Dudi, String> bidangColumn;
    @FXML private TableColumn<Dudi, String> alamatColumn;
    @FXML private TableColumn<Dudi, String> contactPersonColumn;
    @FXML private TableColumn<Dudi, String> contactPhoneColumn;
    @FXML private TableColumn<Dudi, Integer> kuotaColumn;
    @FXML private TableColumn<Dudi, Boolean> aktifColumn;

    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button assignButton;
    @FXML private Button refreshButton;
    @FXML private Button closeButton;

    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private DudiService dudiService;
    private PenempatanPklService penempatanPklService;
    private SiswaService siswaService;

    private ObservableList<Dudi> dudiList;
    private ObservableList<Dudi> filteredList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SessionManager sessionManager = new SessionManager();
        ApiClient apiClient = ApiClient.getInstance();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }

        dudiService = new DudiService(apiClient);
        penempatanPklService = new PenempatanPklService(apiClient);
        siswaService = new SiswaService(apiClient);

        dudiList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupTable();
        setupEventHandlers();

        loadData();
    }

    private void setupTable() {
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        bidangColumn.setCellValueFactory(new PropertyValueFactory<>("bidangUsaha"));
        alamatColumn.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        contactPersonColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        contactPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("contactPhone"));
        kuotaColumn.setCellValueFactory(new PropertyValueFactory<>("kuotaSiswa"));
        aktifColumn.setCellValueFactory(new PropertyValueFactory<>("aktif"));

        dudiTable.setItems(filteredList);

        aktifColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean aktif, boolean empty) {
                super.updateItem(aktif, empty);
                if (empty || aktif == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(aktif ? "Ya" : "Tidak");
                    setStyle(aktif ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        dudiTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            assignButton.setDisable(!hasSelection);
        });
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void loadData() {
        setLoading(true, "Memuat data DUDI...");
        new Thread(() -> {
            try {
                List<Dudi> data = dudiService.getAll();
                Platform.runLater(() -> {
                    dudiList.setAll(data);
                    applyFilter();
                    setLoading(false, "Memuat " + data.size() + " DUDI");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memuat data DUDI");
                    InAppNotification.show("Gagal memuat data DUDI: " + e.getMessage(),
                            dudiTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private void applyFilter() {
        String keyword = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

        List<Dudi> filtered = dudiList.stream()
                .filter(d -> keyword.isEmpty()
                        || (d.getNama() != null && d.getNama().toLowerCase().contains(keyword))
                        || (d.getBidangUsaha() != null && d.getBidangUsaha().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
        statusLabel.setText("Menampilkan " + filtered.size() + " DUDI (filtered)");
    }

    @FXML
    private void handleAdd() {
        Dudi dudi = showDudiForm(null);
        if (dudi != null) {
            setLoading(true, "Menyimpan DUDI...");
            new Thread(() -> {
                try {
                    Dudi created = dudiService.create(dudi);
                    Platform.runLater(() -> {
                        setLoading(false, "DUDI berhasil dibuat");
                        dudiList.add(created);
                        applyFilter();
                        InAppNotification.show("DUDI berhasil dibuat",
                                dudiTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setLoading(false, "Gagal membuat DUDI");
                        InAppNotification.show("Gagal membuat DUDI: " + e.getMessage(),
                                dudiTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    });
                }
            }).start();
        }
    }

    @FXML
    private void handleEdit() {
        Dudi selected = dudiTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Dudi updatedDraft = showDudiForm(selected);
        if (updatedDraft != null) {
            setLoading(true, "Mengupdate DUDI...");
            new Thread(() -> {
                try {
                    Dudi updated = dudiService.update(selected.getId(), updatedDraft);
                    Platform.runLater(() -> {
                        setLoading(false, "DUDI berhasil diupdate");
                        int idx = dudiList.indexOf(selected);
                        if (idx >= 0) {
                            dudiList.set(idx, updated);
                            applyFilter();
                        }
                        InAppNotification.show("DUDI berhasil diupdate",
                                dudiTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setLoading(false, "Gagal mengupdate DUDI");
                        InAppNotification.show("Gagal mengupdate DUDI: " + e.getMessage(),
                                dudiTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    });
                }
            }).start();
        }
    }

    @FXML
    private void handleDelete() {
        Dudi selected = dudiTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus DUDI");
        alert.setContentText("Yakin ingin menghapus DUDI \"" + selected.getNama() + "\"?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            setLoading(true, "Menghapus DUDI...");
            new Thread(() -> {
                try {
                    dudiService.delete(selected.getId());
                    Platform.runLater(() -> {
                        setLoading(false, "DUDI berhasil dihapus");
                        dudiList.remove(selected);
                        applyFilter();
                        InAppNotification.show("DUDI berhasil dihapus",
                                dudiTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setLoading(false, "Gagal menghapus DUDI");
                        InAppNotification.show("Gagal menghapus DUDI: " + e.getMessage(),
                                dudiTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    });
                }
            }).start();
        }
    }

    @FXML
    private void handleAssignSiswa() {
        Dudi selected = dudiTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        setLoading(true, "Memuat data siswa...");
        new Thread(() -> {
            try {
                List<Siswa> allSiswa = siswaService.getAllSiswa();
                Platform.runLater(() -> {
                    if (allSiswa.isEmpty()) {
                        setLoading(false, "Data siswa kosong");
                        InAppNotification.show("Data siswa kosong, tidak bisa assign PKL",
                                dudiTable.getParent(),
                                InAppNotification.NotificationType.WARNING,
                                3);
                        return;
                    }

                    AssignResult result = showAssignDialog(selected, allSiswa);
                    if (result != null && !result.selectedSiswa().isEmpty()) {
                        doAssign(selected, result);
                    } else {
                        setLoading(false, "Assign PKL dibatalkan");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memuat data siswa");
                    InAppNotification.show("Gagal memuat data siswa: " + e.getMessage(),
                            dudiTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private Dudi showDudiForm(Dudi existing) {
        Dialog<Dudi> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Tambah DUDI" : "Edit DUDI");
        dialog.setHeaderText(existing == null ? "Tambah DUDI baru" : "Edit data DUDI");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField namaField = new TextField();
        TextField bidangField = new TextField();
        TextField alamatField = new TextField();
        TextField contactPersonField = new TextField();
        TextField contactPhoneField = new TextField();
        TextField kuotaField = new TextField();
        CheckBox aktifCheck = new CheckBox("Aktif");
        aktifCheck.setSelected(true);

        namaField.setPromptText("Nama perusahaan");
        bidangField.setPromptText("Bidang usaha (misal: IT, Otomotif)");
        alamatField.setPromptText("Alamat singkat");
        contactPersonField.setPromptText("Nama PIC");
        contactPhoneField.setPromptText("No. HP PIC");
        kuotaField.setPromptText("Kuota siswa (opsional)");

        if (existing != null) {
            namaField.setText(existing.getNama());
            bidangField.setText(existing.getBidangUsaha());
            alamatField.setText(existing.getAlamat());
            contactPersonField.setText(existing.getContactPerson());
            contactPhoneField.setText(existing.getContactPhone());
            if (existing.getKuotaSiswa() != null) {
                kuotaField.setText(existing.getKuotaSiswa().toString());
            }
            aktifCheck.setSelected(existing.getAktif() != null ? existing.getAktif() : true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nama:"), 0, 0);
        grid.add(namaField, 1, 0);
        grid.add(new Label("Bidang Usaha:"), 0, 1);
        grid.add(bidangField, 1, 1);
        grid.add(new Label("Alamat:"), 0, 2);
        grid.add(alamatField, 1, 2);
        grid.add(new Label("Contact Person:"), 0, 3);
        grid.add(contactPersonField, 1, 3);
        grid.add(new Label("No. HP:"), 0, 4);
        grid.add(contactPhoneField, 1, 4);
        grid.add(new Label("Kuota Siswa:"), 0, 5);
        grid.add(kuotaField, 1, 5);
        grid.add(aktifCheck, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                String nama = namaField.getText() != null ? namaField.getText().trim() : "";
                if (nama.isEmpty()) {
                    InAppNotification.show("Nama DUDI wajib diisi",
                            dudiTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }

                Dudi d = existing != null ? existing : new Dudi();
                d.setNama(nama);
                d.setBidangUsaha(bidangField.getText() != null ? bidangField.getText().trim() : null);
                d.setAlamat(alamatField.getText() != null ? alamatField.getText().trim() : null);
                d.setContactPerson(contactPersonField.getText() != null ? contactPersonField.getText().trim() : null);
                d.setContactPhone(contactPhoneField.getText() != null ? contactPhoneField.getText().trim() : null);

                try {
                    String kuotaText = kuotaField.getText() != null ? kuotaField.getText().trim() : "";
                    d.setKuotaSiswa(kuotaText.isEmpty() ? null : Integer.parseInt(kuotaText));
                } catch (NumberFormatException e) {
                    InAppNotification.show("Kuota siswa harus berupa angka",
                            dudiTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }

                d.setAktif(aktifCheck.isSelected());
                return d;
            }
            return null;
        });

        Optional<Dudi> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private AssignResult showAssignDialog(Dudi dudi, List<Siswa> allSiswa) {
        Dialog<AssignResult> dialog = new Dialog<>();
        dialog.setTitle("Assign Siswa PKL ke " + dudi.getNama());
        dialog.setHeaderText("Pilih siswa yang akan ditempatkan di DUDI ini.\n" +
                "Atur juga periode PKL dan keterangan.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ListView<Siswa> siswaListView = new ListView<>();
        siswaListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        siswaListView.setItems(FXCollections.observableArrayList(allSiswa));

        DatePicker mulaiPicker = new DatePicker(LocalDate.now());
        DatePicker selesaiPicker = new DatePicker(LocalDate.now().plusMonths(1));
        TextField ketField = new TextField();
        ketField.setPromptText("Keterangan (opsional)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Siswa:"), 0, 0);
        grid.add(siswaListView, 1, 0);
        grid.add(new Label("Tanggal Mulai:"), 0, 1);
        grid.add(mulaiPicker, 1, 1);
        grid.add(new Label("Tanggal Selesai:"), 0, 2);
        grid.add(selesaiPicker, 1, 2);
        grid.add(new Label("Keterangan:"), 0, 3);
        grid.add(ketField, 1, 3);

        GridPane.setColumnSpan(siswaListView, 2);
        siswaListView.setPrefHeight(250);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                List<Siswa> selected = new ArrayList<>(siswaListView.getSelectionModel().getSelectedItems());
                LocalDate mulai = mulaiPicker.getValue();
                LocalDate selesai = selesaiPicker.getValue();

                if (selected.isEmpty() || mulai == null || selesai == null) {
                    InAppNotification.show("Pilih minimal satu siswa dan isi tanggal mulai/selesai",
                            dudiTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }

                if (mulai.isAfter(selesai)) {
                    InAppNotification.show("Tanggal mulai tidak boleh setelah tanggal selesai",
                            dudiTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            3);
                    return null;
                }

                return new AssignResult(selected, mulai, selesai, ketField.getText());
            }
            return null;
        });

        Optional<AssignResult> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void doAssign(Dudi dudi, AssignResult result) {
        List<Siswa> selected = result.selectedSiswa();
        LocalDate mulai = result.mulai();
        LocalDate selesai = result.selesai();
        String ket = result.keterangan();

        setLoading(true, "Meng-assign " + selected.size() + " siswa ke " + dudi.getNama() + "...");
        new Thread(() -> {
            int successCount = 0;
            List<String> errors = new ArrayList<>();

            for (Siswa s : selected) {
                if (s.getId() == null) continue;
                try {
                    PenempatanPkl penempatan = penempatanPklService.create(
                            s.getId(),
                            dudi.getId(),
                            mulai,
                            selesai,
                            ket
                    );
                    if (penempatan != null) {
                        successCount++;
                    }
                } catch (Exception e) {
                    errors.add("Gagal assign " + s.getNama() + ": " + e.getMessage());
                }
            }

            int finalSuccessCount = successCount;
            Platform.runLater(() -> {
                setLoading(false, "Assign PKL selesai");
                if (finalSuccessCount > 0) {
                    InAppNotification.show("Berhasil meng-assign " + finalSuccessCount + " siswa ke " + dudi.getNama(),
                            dudiTable.getParent(),
                            InAppNotification.NotificationType.SUCCESS,
                            4);
                }
                if (!errors.isEmpty()) {
                    InAppNotification.show("Beberapa assign gagal. Lihat log untuk detail.",
                            dudiTable.getParent(),
                            InAppNotification.NotificationType.WARNING,
                            5);
                    errors.forEach(err -> System.err.println("[PKL ASSIGN ERROR] " + err));
                }
            });
        }).start();
    }

    private void setLoading(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        statusLabel.setText(message);
        addButton.setDisable(loading);
        editButton.setDisable(loading || dudiTable.getSelectionModel().getSelectedItem() == null);
        deleteButton.setDisable(loading || dudiTable.getSelectionModel().getSelectedItem() == null);
        assignButton.setDisable(loading || dudiTable.getSelectionModel().getSelectedItem() == null);
        refreshButton.setDisable(loading);
    }

    private record AssignResult(List<Siswa> selectedSiswa,
                                LocalDate mulai,
                                LocalDate selesai,
                                String keterangan) {
    }
}

