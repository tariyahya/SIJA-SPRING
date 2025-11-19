package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Guru;
import com.smk.presensi.desktop.model.JournalEntry;
import com.smk.presensi.desktop.model.Kelas;
import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.GuruService;
import com.smk.presensi.desktop.service.JournalService;
import com.smk.presensi.desktop.service.KelasService;
import com.smk.presensi.desktop.service.PresensiService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PresensiGuruController implements Initializable {

    @FXML private ComboBox<Guru> guruCombo;
    @FXML private ComboBox<Kelas> kelasCombo;
    @FXML private TextField mapelField;
    @FXML private DatePicker tanggalPicker;
    @FXML private TextField jamMulaiField;
    @FXML private TextField jamSelesaiField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea materiArea;
    @FXML private TextArea catatanArea;
    @FXML private Button submitButton;
    @FXML private Button resetButton;
    @FXML private Button refreshButton;
    @FXML private TableView<GuruPresensiRow> presensiTable;
    @FXML private TableColumn<GuruPresensiRow, LocalDate> tanggalColumn;
    @FXML private TableColumn<GuruPresensiRow, String> guruColumn;
    @FXML private TableColumn<GuruPresensiRow, String> kelasColumn;
    @FXML private TableColumn<GuruPresensiRow, String> mapelColumn;
    @FXML private TableColumn<GuruPresensiRow, String> statusColumn;
    @FXML private TableColumn<GuruPresensiRow, String> materiColumn;
    @FXML private TableColumn<GuruPresensiRow, String> jurnalColumn;
    @FXML private TableColumn<GuruPresensiRow, String> keteranganColumn;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    @FXML private CheckBox mockDataCheckbox;
    @FXML private VBox journalInfoBox;

    private PresensiService presensiService;
    private GuruService guruService;
    private KelasService kelasService;
    private JournalService journalService;

    private final ObservableList<Guru> guruList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<GuruPresensiRow> tableData = FXCollections.observableArrayList();

    private static final List<String> STATUS_OPTIONS = List.of("HADIR", "IZIN", "SAKIT", "ALPHA", "DISPENSASI");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApiClient apiClient = ApiClient.getInstance();
        presensiService = new PresensiService(apiClient);
        guruService = new GuruService(apiClient);
        kelasService = new KelasService(apiClient);
        journalService = new JournalService(apiClient);

        tanggalPicker.setValue(LocalDate.now());
        statusCombo.setItems(FXCollections.observableArrayList(STATUS_OPTIONS));
        statusCombo.setValue("HADIR");
        materiArea.setPromptText("Contoh: Materi PKK Bab 3 - Evaluasi Quiz");
        catatanArea.setPromptText("Catatan tambahan (opsional)");

        setupComboDisplay(guruCombo, Guru::getNama);
        setupComboDisplay(kelasCombo, Kelas::getNama);

        setupTable();
        setupEventHandlers();

        loadReferenceData();
        loadPresensiData();
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
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mapel"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        materiColumn.setCellValueFactory(new PropertyValueFactory<>("materi"));
        jurnalColumn.setCellValueFactory(new PropertyValueFactory<>("journalStatus"));
        keteranganColumn.setCellValueFactory(new PropertyValueFactory<>("keterangan"));

        presensiTable.setItems(tableData);
        presensiTable.setPlaceholder(new Label("Belum ada presensi guru untuk tanggal ini"));
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadPresensiData());
        submitButton.setOnAction(e -> handleSubmit());
        resetButton.setOnAction(e -> resetForm());
        mockDataCheckbox.setOnAction(e -> loadPresensiData());
        tanggalPicker.valueProperty().addListener((obs, oldVal, newVal) -> loadPresensiData());
        guruCombo.valueProperty().addListener((obs, oldVal, guru) -> {
            if (guru != null && (mapelField.getText() == null || mapelField.getText().isBlank())) {
                mapelField.setText(guru.getMapel());
            }
        });
    }

    private void loadReferenceData() {
        setLoading(true);
        new Thread(() -> {
            try {
                List<Guru> gurus = guruService.getAllGuru();
                if (gurus.isEmpty()) {
                    gurus = guruService.getMockData();
                }
                List<Kelas> kelas = kelasService.getAllKelas();
                if (kelas.isEmpty()) {
                    kelas = createFallbackKelas();
                }

                List<Guru> finalGurus = gurus;
                List<Kelas> finalKelas = kelas;
                Platform.runLater(() -> {
                    guruList.setAll(finalGurus);
                    kelasList.setAll(finalKelas);
                    guruCombo.setItems(guruList);
                    kelasCombo.setItems(kelasList);
                    if (!guruList.isEmpty()) {
                        guruCombo.getSelectionModel().selectFirst();
                    }
                    if (!kelasList.isEmpty()) {
                        kelasCombo.getSelectionModel().selectFirst();
                    }
                    setLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    InAppNotification.show("Gagal memuat referensi guru/kelas: " + e.getMessage(),
                            journalInfoBox,
                            InAppNotification.NotificationType.ERROR, 5);
                });
            }
        }).start();
    }

    private List<Kelas> createFallbackKelas() {
        return List.of(createKelasSample(1L, "XII SIJA 1"),
                createKelasSample(2L, "XII SIJA 2"),
                createKelasSample(3L, "XIII SIJA 1"));
    }

    private Kelas createKelasSample(Long id, String nama) {
        Kelas kelas = new Kelas();
        kelas.setId(id);
        kelas.setNama(nama);
        kelas.setTingkat(nama.split(" ")[0]);
        kelas.setJurusan("SIJA");
        return kelas;
    }

    private void loadPresensiData() {
        setLoading(true);
        LocalDate tanggal = tanggalPicker.getValue();
        new Thread(() -> {
            try {
                List<GuruPresensiRow> rows;
                if (mockDataCheckbox.isSelected()) {
                    rows = buildMockRows(tanggal);
                } else {
                    List<Presensi> data = presensiService.getLaporanHarian(tanggal);
                    rows = data.stream()
                            .filter(p -> "GURU".equalsIgnoreCase(p.getTipe()))
                            .map(p -> GuruPresensiRow.fromPresensi(p))
                            .collect(Collectors.toList());
                }

                Platform.runLater(() -> {
                    tableData.setAll(rows);
                    statusLabel.setText("Total " + rows.size() + " presensi guru");
                    setLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    statusLabel.setText("Gagal memuat presensi");
                    InAppNotification.show("Gagal memuat presensi guru: " + e.getMessage(),
                            journalInfoBox,
                            InAppNotification.NotificationType.ERROR, 5);
                });
            }
        }).start();
    }

    private List<GuruPresensiRow> buildMockRows(LocalDate tanggal) {
        return guruList.stream()
                .limit(3)
                .map(guru -> GuruPresensiRow.builder()
                        .setTanggal(tanggal != null ? tanggal : LocalDate.now())
                        .setGuruNama(guru.getNama())
                        .setKelas("Mock Class")
                        .setMapel(guru.getMapel())
                        .setStatus("HADIR")
                        .setMateri("Mock teaching session")
                        .setJournalStatus("Auto")
                        .setKeterangan("Mock data")
                        .build())
                .collect(Collectors.toList());
    }

    private void handleSubmit() {
        Guru guru = guruCombo.getValue();
        Kelas kelas = kelasCombo.getValue();
        LocalDate tanggal = tanggalPicker.getValue();
        String mapel = mapelField.getText();
        String materi = materiArea.getText();
        String catatan = catatanArea.getText();
        String status = statusCombo.getValue();
        LocalTime jamMulai;
        LocalTime jamSelesai;
        try {
            jamMulai = parseTime(jamMulaiField.getText());
            jamSelesai = parseTime(jamSelesaiField.getText());
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
            return;
        }

        if (guru == null) {
            showValidationError("Silakan pilih guru");
            return;
        }
        if (kelas == null) {
            showValidationError("Silakan pilih kelas");
            return;
        }
        if (tanggal == null) {
            showValidationError("Tanggal presensi harus diisi");
            return;
        }
        if (mapel == null || mapel.isBlank()) {
            showValidationError("Mapel tidak boleh kosong");
            return;
        }
        if (status == null) {
            showValidationError("Status presensi harus dipilih");
            return;
        }

        Presensi presensi = new Presensi();
        presensi.setUserId(guru.getId());
        presensi.setUsername(guru.getNama());
        presensi.setTipe("GURU");
        presensi.setTanggal(tanggal);
        presensi.setJamMasuk(jamMulai);
        presensi.setJamPulang(jamSelesai);
        presensi.setStatus(status);
        presensi.setMethod("MANUAL");
        presensi.setKeterangan(buildKeterangan(kelas, mapel, catatan));

        setLoading(true);
        new Thread(() -> {
            try {
                Presensi created = presensiService.createPresensi(presensi);
                if (created == null) {
                    Platform.runLater(() -> {
                        setLoading(false);
                        InAppNotification.show("Backend tidak mengembalikan data presensi",
                                journalInfoBox,
                                InAppNotification.NotificationType.ERROR, 5);
                    });
                    return;
                }
                JournalEntry journalEntry = null;
                try {
                    journalEntry = journalService.createAutoEntryFromPresensi(created,
                            kelas.getNama(),
                            mapel,
                            materi,
                            catatan);
                } catch (Exception journalEx) {
                    System.err.println("Auto journal failed: " + journalEx.getMessage());
                }
                JournalEntry finalJournalEntry = journalEntry;
                Platform.runLater(() -> {
                    setLoading(false);
                    tableData.add(0, GuruPresensiRow.fromSubmission(created,
                            kelas.getNama(),
                            mapel,
                            materi,
                            finalJournalEntry != null,
                            catatan));
                    InAppNotification.show("Presensi guru tersimpan dan jurnal otomatis dibuat",
                            journalInfoBox,
                            InAppNotification.NotificationType.SUCCESS, 4);
                    resetForm();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    InAppNotification.show("Gagal menyimpan presensi: " + e.getMessage(),
                            journalInfoBox,
                            InAppNotification.NotificationType.ERROR, 5);
                });
            }
        }).start();
    }

    private void resetForm() {
        tanggalPicker.setValue(LocalDate.now());
        jamMulaiField.clear();
        jamSelesaiField.clear();
        materiArea.clear();
        catatanArea.clear();
        statusCombo.setValue("HADIR");
        if (!guruList.isEmpty()) {
            guruCombo.getSelectionModel().selectFirst();
        }
        if (!kelasList.isEmpty()) {
            kelasCombo.getSelectionModel().selectFirst();
        }
    }

    private String buildKeterangan(Kelas kelas, String mapel, String catatan) {
        StringBuilder sb = new StringBuilder();
        if (kelas != null) {
            sb.append("Kelas ").append(kelas.getNama());
        }
        if (mapel != null && !mapel.isBlank()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(mapel);
        }
        if (catatan != null && !catatan.isBlank()) {
            sb.append(" | ").append(catatan);
        }
        return sb.toString();
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format jam harus HH:mm (contoh 07:30)");
        }
    }

    private void showValidationError(String message) {
        InAppNotification.show(message, journalInfoBox,
                InAppNotification.NotificationType.WARNING, 4);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        submitButton.setDisable(loading);
        refreshButton.setDisable(loading);
        statusLabel.setVisible(!loading);
    }

    public static class GuruPresensiRow {
        private final Long presensiId;
        private final LocalDate tanggal;
        private final String guruNama;
        private final String kelas;
        private final String mapel;
        private final String status;
        private final String materi;
        private final String journalStatus;
        private final String keterangan;

        private GuruPresensiRow(Long presensiId,
                                LocalDate tanggal,
                                String guruNama,
                                String kelas,
                                String mapel,
                                String status,
                                String materi,
                                String journalStatus,
                                String keterangan) {
            this.presensiId = presensiId;
            this.tanggal = tanggal;
            this.guruNama = guruNama;
            this.kelas = kelas;
            this.mapel = mapel;
            this.status = status;
            this.materi = materi;
            this.journalStatus = journalStatus;
            this.keterangan = keterangan;
        }

        public static GuruPresensiRow fromPresensi(Presensi presensi) {
            ParsedKeterangan parsed = ParsedKeterangan.from(presensi.getKeterangan());
            return new GuruPresensiRow(presensi.getId(),
                presensi.getTanggal(),
                presensi.getUsername(),
                parsed.kelas,
                parsed.mapel,
                presensi.getStatus(),
                parsed.materi,
                parsed.journalStatus,
                presensi.getKeterangan());
        }

        public static GuruPresensiRow fromSubmission(Presensi presensi,
                                                      String kelas,
                                                      String mapel,
                                                      String materi,
                                                      boolean journalCreated,
                                                      String catatan) {
            return new GuruPresensiRow(presensi.getId(),
                    presensi.getTanggal(),
                    presensi.getUsername(),
                    kelas,
                    mapel,
                    presensi.getStatus(),
                    materi,
                    journalCreated ? "Auto" : "Manual",
                    catatan);
        }

        public Long getPresensiId() {
            return presensiId;
        }

        public LocalDate getTanggal() {
            return tanggal;
        }

        public String getGuruNama() {
            return guruNama;
        }

        public String getKelas() {
            return kelas;
        }

        public String getMapel() {
            return mapel;
        }

        public String getStatus() {
            return status;
        }

        public String getMateri() {
            return materi;
        }

        public String getJournalStatus() {
            return journalStatus;
        }

        public String getKeterangan() {
            return keterangan;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long presensiId;
            private LocalDate tanggal;
            private String guruNama;
            private String kelas;
            private String mapel;
            private String status;
            private String materi;
            private String journalStatus;
            private String keterangan;

            public Builder setPresensiId(Long presensiId) {
                this.presensiId = presensiId;
                return this;
            }

            public Builder setTanggal(LocalDate tanggal) {
                this.tanggal = tanggal;
                return this;
            }

            public Builder setGuruNama(String guruNama) {
                this.guruNama = guruNama;
                return this;
            }

            public Builder setKelas(String kelas) {
                this.kelas = kelas;
                return this;
            }

            public Builder setMapel(String mapel) {
                this.mapel = mapel;
                return this;
            }

            public Builder setStatus(String status) {
                this.status = status;
                return this;
            }

            public Builder setMateri(String materi) {
                this.materi = materi;
                return this;
            }

            public Builder setJournalStatus(String journalStatus) {
                this.journalStatus = journalStatus;
                return this;
            }

            public Builder setKeterangan(String keterangan) {
                this.keterangan = keterangan;
                return this;
            }

            public GuruPresensiRow build() {
                return new GuruPresensiRow(presensiId, tanggal, guruNama, kelas, mapel, status, materi, journalStatus, keterangan);
            }
        }

        private record ParsedKeterangan(String kelas, String mapel, String materi, String journalStatus) {
            private static ParsedKeterangan from(String raw) {
                if (raw == null || raw.isBlank()) {
                    return new ParsedKeterangan("-", "-", "-", "-");
                }

                String kelas = "-";
                String mapel = "-";
                String materi = "-";

                String[] noteSplit = raw.split("\\|", 2);
                String left = noteSplit[0].trim();
                if (left.toLowerCase().startsWith("kelas")) {
                    String afterLabel = left.substring(5).trim();
                    int dashIndex = afterLabel.indexOf('-');
                    if (dashIndex >= 0) {
                        kelas = afterLabel.substring(0, dashIndex).trim();
                        mapel = afterLabel.substring(dashIndex + 1).trim();
                    } else {
                        kelas = afterLabel;
                    }
                } else {
                    materi = left;
                }

                if (noteSplit.length > 1) {
                    materi = noteSplit[1].trim();
                }

                return new ParsedKeterangan(emptyToDash(kelas), emptyToDash(mapel), emptyToDash(materi), "-");
            }

            private static String emptyToDash(String value) {
                return (value == null || value.isBlank()) ? "-" : value;
            }
        }
    }
}
