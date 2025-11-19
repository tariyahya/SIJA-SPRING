package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Izin;
import com.smk.presensi.desktop.model.Siswa;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.IzinService;
import com.smk.presensi.desktop.service.SessionManager;
import com.smk.presensi.desktop.service.SiswaService;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller untuk layar perizinan (pengajuan + approval sederhana).
 */
public class ApprovalIzinController implements Initializable {

    @FXML private TableView<Izin> izinTable;
    @FXML private TableColumn<Izin, String> siswaColumn;
    @FXML private TableColumn<Izin, String> kelasColumn;
    @FXML private TableColumn<Izin, String> jurusanColumn;
    @FXML private TableColumn<Izin, String> jenisColumn;
    @FXML private TableColumn<Izin, LocalDate> mulaiColumn;
    @FXML private TableColumn<Izin, LocalDate> selesaiColumn;
    @FXML private TableColumn<Izin, String> statusColumn;
    @FXML private TableColumn<Izin, String> alasanColumn;

    @FXML private Button tambahButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button refreshButton;
    @FXML private Button closeButton;

    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private IzinService izinService;
    private SiswaService siswaService;
    private ObservableList<Izin> izinList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SessionManager sessionManager = new SessionManager();
        ApiClient apiClient = ApiClient.getInstance();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }

        izinService = new IzinService(apiClient);
        siswaService = new SiswaService(apiClient);

        izinList = FXCollections.observableArrayList();

        setupTable();
        setupSelectionHandling();

        loadPendingIzin();
    }

    private void setupTable() {
        siswaColumn.setCellValueFactory(new PropertyValueFactory<>("siswaNama"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        jurusanColumn.setCellValueFactory(new PropertyValueFactory<>("jurusan"));
        jenisColumn.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        mulaiColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalMulai"));
        selesaiColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalSelesai"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        alasanColumn.setCellValueFactory(new PropertyValueFactory<>("alasan"));

        izinTable.setItems(izinList);
    }

    private void setupSelectionHandling() {
        izinTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            approveButton.setDisable(!hasSelection);
            rejectButton.setDisable(!hasSelection);
        });
    }

    @FXML
    private void handleRefresh() {
        loadPendingIzin();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleTambahIzin() {
        setLoading(true, "Memuat data siswa...");
        new Thread(() -> {
            List<Siswa> allSiswa = siswaService.getAllSiswa();
            Platform.runLater(() -> {
                if (allSiswa.isEmpty()) {
                    setLoading(false, "Tidak ada data siswa untuk pengajuan izin");
                    showAlert(Alert.AlertType.WARNING, "Perizinan", "Data siswa kosong",
                            "Tambahkan data siswa terlebih dahulu sebelum membuat izin.");
                    return;
                }

                Izin izinDraft = showIzinFormDialog(allSiswa);
                if (izinDraft != null) {
                    submitIzin(izinDraft);
                } else {
                    setLoading(false, "Pengajuan izin dibatalkan");
                }
            });
        }).start();
    }

    @FXML
    private void handleApprove() {
        Izin selected = izinTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approval Izin");
        dialog.setHeaderText("Setujui izin untuk " + selected.getSiswaNama());
        dialog.setContentText("Catatan (opsional):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String note = result.get();
        processApproval(selected, "APPROVED", note);
    }

    @FXML
    private void handleReject() {
        Izin selected = izinTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Tolak Izin");
        dialog.setHeaderText("Tolak izin untuk " + selected.getSiswaNama());
        dialog.setContentText("Alasan penolakan:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String note = result.get();
        processApproval(selected, "REJECTED", note);
    }

    private void loadPendingIzin() {
        setLoading(true, "Memuat izin pending untuk hari ini...");
        new Thread(() -> {
            try {
                List<Izin> list = izinService.getPendingToday();
                Platform.runLater(() -> {
                    izinList.setAll(list);
                    setLoading(false, "Memuat " + list.size() + " izin pending");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memuat izin pending");
                    showAlert(Alert.AlertType.ERROR, "Perizinan",
                            "Gagal memuat data izin", e.getMessage());
                });
            }
        }).start();
    }

    private Izin showIzinFormDialog(List<Siswa> allSiswa) {
        Dialog<Izin> dialog = new Dialog<>();
        dialog.setTitle("Pengajuan Izin Siswa");
        dialog.setHeaderText("Isi form pengajuan izin siswa");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ComboBox<Siswa> siswaCombo = new ComboBox<>();
        siswaCombo.setItems(FXCollections.observableArrayList(allSiswa));
        siswaCombo.setPromptText("Pilih siswa");

        ComboBox<String> jenisCombo = new ComboBox<>();
        jenisCombo.setItems(FXCollections.observableArrayList("SAKIT", "IZIN", "DISPENSASI"));
        jenisCombo.setPromptText("Jenis izin");

        DatePicker mulaiPicker = new DatePicker(LocalDate.now());
        DatePicker selesaiPicker = new DatePicker(LocalDate.now());

        TextArea alasanArea = new TextArea();
        alasanArea.setPromptText("Alasan izin");
        alasanArea.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Siswa:"), 0, 0);
        grid.add(siswaCombo, 1, 0);
        grid.add(new Label("Jenis:"), 0, 1);
        grid.add(jenisCombo, 1, 1);
        grid.add(new Label("Tanggal Mulai:"), 0, 2);
        grid.add(mulaiPicker, 1, 2);
        grid.add(new Label("Tanggal Selesai:"), 0, 3);
        grid.add(selesaiPicker, 1, 3);
        grid.add(new Label("Alasan:"), 0, 4);
        grid.add(alasanArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                Siswa selectedSiswa = siswaCombo.getValue();
                String jenis = jenisCombo.getValue();
                LocalDate mulai = mulaiPicker.getValue();
                LocalDate selesai = selesaiPicker.getValue();
                String alasan = alasanArea.getText() != null ? alasanArea.getText().trim() : "";

                if (selectedSiswa == null || jenis == null || mulai == null || selesai == null || alasan.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Perizinan",
                            "Form belum lengkap",
                            "Pastikan siswa, jenis, tanggal, dan alasan sudah diisi.");
                    return null;
                }

                Izin izin = new Izin();
                izin.setSiswaId(selectedSiswa.getId());
                izin.setSiswaNama(selectedSiswa.getNama());
                izin.setKelas(selectedSiswa.getKelas());
                izin.setJurusan(selectedSiswa.getJurusan());
                izin.setJenis(jenis);
                izin.setTanggalMulai(mulai);
                izin.setTanggalSelesai(selesai);
                izin.setAlasan(alasan);
                return izin;
            }
            return null;
        });

        Optional<Izin> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void submitIzin(Izin draft) {
        setLoading(true, "Mengirim pengajuan izin...");
        new Thread(() -> {
            try {
                Izin created = izinService.createIzin(
                        draft.getSiswaId(),
                        draft.getJenis(),
                        draft.getTanggalMulai(),
                        draft.getTanggalSelesai(),
                        draft.getAlasan()
                );

                Platform.runLater(() -> {
                    setLoading(false, "Izin berhasil diajukan");
                    showAlert(Alert.AlertType.INFORMATION, "Perizinan",
                            "Izin berhasil diajukan",
                            "Izin untuk " + created.getSiswaNama() + " berhasil disimpan.");
                    loadPendingIzin();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal mengirim pengajuan izin");
                    showAlert(Alert.AlertType.ERROR, "Perizinan",
                            "Gagal menyimpan izin", e.getMessage());
                });
            }
        }).start();
    }

    private void processApproval(Izin izin, String status, String note) {
        setLoading(true, "Memproses " + (status.equals("APPROVED") ? "approval" : "penolakan") + " izin...");
        new Thread(() -> {
            try {
                izinService.approveIzin(izin.getId(), status, note);
                Platform.runLater(() -> {
                    setLoading(false, "Izin berhasil diperbarui");
                    loadPendingIzin();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memproses izin");
                    showAlert(Alert.AlertType.ERROR, "Perizinan",
                            "Gagal memproses izin", e.getMessage());
                });
            }
        }).start();
    }

    private void setLoading(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        statusLabel.setText(message);
        tambahButton.setDisable(loading);
        approveButton.setDisable(loading || izinTable.getSelectionModel().getSelectedItem() == null);
        rejectButton.setDisable(loading || izinTable.getSelectionModel().getSelectedItem() == null);
        refreshButton.setDisable(loading);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

