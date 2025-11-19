package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Guru;
import com.smk.presensi.desktop.model.Kelas;
import com.smk.presensi.desktop.model.UlanganHarian;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.GuruService;
import com.smk.presensi.desktop.service.KelasService;
import com.smk.presensi.desktop.service.UlanganHarianService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UlanganHarianController implements Initializable {

    @FXML private ComboBox<Guru> guruCombo;
    @FXML private ComboBox<Kelas> kelasCombo;
    @FXML private DatePicker tanggalPicker;
    @FXML private ComboBox<String> jenisCombo;
    @FXML private TextField mapelField;
    @FXML private TextField materiField;
    @FXML private TextField nilaiRataField;
    @FXML private TextField jumlahPesertaField;
    @FXML private TextField jumlahRemedialField;
    @FXML private TextArea catatanArea;
    @FXML private Button submitButton;
    @FXML private Button resetButton;

    @FXML private ComboBox<Guru> filterGuruCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button filterButton;
    @FXML private Button refreshButton;

    @FXML private TableView<UlanganHarian> uhTable;
    @FXML private TableColumn<UlanganHarian, LocalDate> tanggalColumn;
    @FXML private TableColumn<UlanganHarian, String> guruColumn;
    @FXML private TableColumn<UlanganHarian, String> kelasColumn;
    @FXML private TableColumn<UlanganHarian, String> mapelColumn;
    @FXML private TableColumn<UlanganHarian, String> jenisColumn;
    @FXML private TableColumn<UlanganHarian, Double> nilaiRataColumn;
    @FXML private TableColumn<UlanganHarian, Integer> pesertaColumn;
    @FXML private TableColumn<UlanganHarian, Integer> remedialColumn;
    @FXML private TableColumn<UlanganHarian, Boolean> autoColumn;

    @FXML private Label statusLabel;
    @FXML private Label infoLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private GuruService guruService;
    private KelasService kelasService;
    private UlanganHarianService uhService;

    private final ObservableList<Guru> guruList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<UlanganHarian> uhList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApiClient apiClient = ApiClient.getInstance();
        guruService = new GuruService(apiClient);
        kelasService = new KelasService(apiClient);
        uhService = new UlanganHarianService(apiClient);

        tanggalPicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        jenisCombo.setItems(FXCollections.observableArrayList("TULIS", "PRAKTIK"));
        jenisCombo.setValue("TULIS");

        setupComboDisplay(guruCombo, Guru::getNama);
        setupComboDisplay(filterGuruCombo, Guru::getNama);
        setupComboDisplay(kelasCombo, Kelas::getNama);

        setupTable();
        setupEventHandlers();

        loadReferenceData();
        loadUhData();
    }

    private <T> void setupComboDisplay(ComboBox<T> comboBox, java.util.function.Function<T, String> labelProvider) {
        comboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(labelProvider.apply(item));
                }
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? comboBox.getPromptText() : labelProvider.apply(item));
            }
        });
    }

    private void setupTable() {
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        guruColumn.setCellValueFactory(new PropertyValueFactory<>("guruNama"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelasNama"));
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mapel"));
        jenisColumn.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        nilaiRataColumn.setCellValueFactory(new PropertyValueFactory<>("nilaiRataRata"));
        pesertaColumn.setCellValueFactory(new PropertyValueFactory<>("jumlahPeserta"));
        remedialColumn.setCellValueFactory(new PropertyValueFactory<>("jumlahRemedial"));
        autoColumn.setCellValueFactory(new PropertyValueFactory<>("autoGenerated"));

        uhTable.setItems(uhList);
    }

    private void setupEventHandlers() {
        submitButton.setOnAction(e -> handleSubmit());
        resetButton.setOnAction(e -> resetForm());
        refreshButton.setOnAction(e -> loadUhData());
        filterButton.setOnAction(e -> applyFilter());
    }

    private void loadReferenceData() {
        setLoading(true, "Memuat data guru & kelas...");
        new Thread(() -> {
            List<Guru> gurus = guruService.getAllGuru();
            List<Kelas> kelas = kelasService.getAllKelas();
            Platform.runLater(() -> {
                guruList.setAll(gurus);
                kelasList.setAll(kelas);
                guruCombo.setItems(guruList);
                filterGuruCombo.setItems(guruList);
                if (!guruList.isEmpty()) {
                    guruCombo.getSelectionModel().selectFirst();
                }
                if (!kelasList.isEmpty()) {
                    kelasCombo.getSelectionModel().selectFirst();
                }
                setLoading(false, "Ready");
            });
        }).start();
    }

    private void loadUhData() {
        setLoading(true, "Memuat riwayat UH...");
        new Thread(() -> {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            Guru selectedGuru = filterGuruCombo.getValue();
            Long guruId = selectedGuru != null ? selectedGuru.getId() : null;

            List<UlanganHarian> data = uhService.list(guruId, start, end);
            Platform.runLater(() -> {
                uhList.setAll(data);
                updateInfoLabel();
                setLoading(false, "Ready");
            });
        }).start();
    }

    private void applyFilter() {
        loadUhData();
    }

    private void handleSubmit() {
        Guru guru = guruCombo.getValue();
        Kelas kelas = kelasCombo.getValue();
        LocalDate tanggal = tanggalPicker.getValue();
        String jenis = jenisCombo.getValue();
        String mapel = mapelField.getText() != null ? mapelField.getText().trim() : "";
        String materi = materiField.getText() != null ? materiField.getText().trim() : "";
        String catatan = catatanArea.getText() != null ? catatanArea.getText().trim() : "";

        if (guru == null) {
            showValidationError("Silakan pilih guru");
            return;
        }
        if (kelas == null) {
            showValidationError("Silakan pilih kelas");
            return;
        }
        if (tanggal == null) {
            showValidationError("Tanggal UH harus diisi");
            return;
        }
        if (jenis == null || jenis.isBlank()) {
            showValidationError("Jenis UH harus dipilih");
            return;
        }
        if (mapel.isBlank()) {
            showValidationError("Mapel tidak boleh kosong");
            return;
        }

        Double nilaiRata = parseDouble(nilaiRataField.getText());
        Integer jumlahPeserta = parseInteger(jumlahPesertaField.getText());
        Integer jumlahRemedial = parseInteger(jumlahRemedialField.getText());

        UlanganHarian uh = new UlanganHarian();
        uh.setGuruId(guru.getId());
        uh.setGuruNama(guru.getNama());
        uh.setKelasId(kelas.getId());
        uh.setKelasNama(kelas.getNama());
        uh.setTanggal(tanggal);
        uh.setJenis(jenis);
        uh.setMapel(mapel);
        uh.setMateri(materi);
        uh.setCatatan(catatan);
        uh.setNilaiRataRata(nilaiRata);
        uh.setJumlahPeserta(jumlahPeserta);
        uh.setJumlahRemedial(jumlahRemedial);
        uh.setAutoGenerated(false);

        setLoading(true, "Menyimpan UH...");
        new Thread(() -> {
            try {
                UlanganHarian created = uhService.create(uh);
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    if (created != null) {
                        uhList.add(0, created);
                        updateInfoLabel();
                        InAppNotification.show("Data UH berhasil disimpan",
                                uhTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                        resetForm();
                    } else {
                        InAppNotification.show("Gagal menyimpan UH",
                                uhTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    InAppNotification.show("Error menyimpan UH: " + e.getMessage(),
                            uhTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private void resetForm() {
        tanggalPicker.setValue(LocalDate.now());
        jenisCombo.setValue("TULIS");
        mapelField.clear();
        materiField.clear();
        nilaiRataField.clear();
        jumlahPesertaField.clear();
        jumlahRemedialField.clear();
        catatanArea.clear();
        if (!guruList.isEmpty()) {
            guruCombo.getSelectionModel().selectFirst();
        }
        if (!kelasList.isEmpty()) {
            kelasCombo.getSelectionModel().selectFirst();
        }
    }

    private void showValidationError(String message) {
        InAppNotification.show(message,
                uhTable.getParent(),
                InAppNotification.NotificationType.ERROR,
                4);
    }

    private Double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setLoading(boolean loading, String message) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(loading);
            statusLabel.setText(message);
        });
    }

    private void updateInfoLabel() {
        int total = uhList.size();
        List<String> guruNames = uhList.stream()
                .map(UlanganHarian::getGuruNama)
                .distinct()
                .collect(Collectors.toList());
        infoLabel.setText("Total: " + total + " UH" + (guruNames.isEmpty() ? "" : " | Guru: " + String.join(", ", guruNames)));
    }
}

