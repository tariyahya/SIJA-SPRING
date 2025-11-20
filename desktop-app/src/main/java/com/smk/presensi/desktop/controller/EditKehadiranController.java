package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.model.Siswa;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.PresensiService;
import com.smk.presensi.desktop.service.SiswaService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class EditKehadiranController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TableView<SiswaPresensiRow> siswaTable;
    @FXML private TableColumn<SiswaPresensiRow, Integer> noColumn;
    @FXML private TableColumn<SiswaPresensiRow, String> nisnColumn;
    @FXML private TableColumn<SiswaPresensiRow, String> namaColumn;
    @FXML private TableColumn<SiswaPresensiRow, String> kehadiranColumn;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private SiswaService siswaService;
    private PresensiService presensiService;
    private ObservableList<SiswaPresensiRow> tableData;
    private String currentKelas;
    private LocalDate currentDate;
    private String currentMapel;
    private LocalTime currentJamMulai;
    private LocalTime currentJamSelesai;

    private static final List<String> STATUS_OPTIONS = List.of("HADIR", "SAKIT", "IZIN", "ALPHA", "DISPENSASI");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApiClient apiClient = ApiClient.getInstance();
        siswaService = new SiswaService(apiClient);
        presensiService = new PresensiService(apiClient);
        
        tableData = FXCollections.observableArrayList();
        siswaTable.setItems(tableData);
        
        setupColumns();
        setupButtons();
    }

    public void initData(String kelas, LocalDate date, String mapel, LocalTime jamMulai, LocalTime jamSelesai) {
        this.currentKelas = kelas;
        this.currentDate = date;
        this.currentMapel = mapel;
        this.currentJamMulai = jamMulai;
        this.currentJamSelesai = jamSelesai;
        
        titleLabel.setText("Edit Kehadiran - " + kelas);
        loadSiswa();
    }

    private void setupColumns() {
        noColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(tableData.indexOf(cellData.getValue()) + 1));
        nisnColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSiswa().getNis()));
        namaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSiswa().getNama()));
        
        kehadiranColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<SiswaPresensiRow, String> call(TableColumn<SiswaPresensiRow, String> param) {
                return new TableCell<>() {
                    private final ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(STATUS_OPTIONS));

                    {
                        comboBox.setOnAction(event -> {
                            SiswaPresensiRow row = getTableView().getItems().get(getIndex());
                            if (row != null) {
                                row.setStatus(comboBox.getValue());
                                updateRowStyle(row, comboBox);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            SiswaPresensiRow row = getTableView().getItems().get(getIndex());
                            if (row != null) {
                                comboBox.setValue(row.getStatus());
                                updateRowStyle(row, comboBox);
                                setGraphic(comboBox);
                            }
                        }
                    }
                    
                    private void updateRowStyle(SiswaPresensiRow row, ComboBox<String> combo) {
                        String status = row.getStatus();
                        if ("HADIR".equals(status)) {
                            combo.setStyle("-fx-background-color: #d1e7dd; -fx-text-fill: #0f5132;");
                        } else if ("SAKIT".equals(status)) {
                            combo.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #664d03;");
                        } else if ("IZIN".equals(status)) {
                            combo.setStyle("-fx-background-color: #cff4fc; -fx-text-fill: #055160;");
                        } else if ("ALPHA".equals(status)) {
                            combo.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #842029;");
                        } else if ("DISPENSASI".equals(status)) {
                            combo.setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;");
                        } else {
                             combo.setStyle("");
                        }
                    }
                };
            }
        });
    }

    private void loadSiswa() {
        List<Siswa> siswaList = siswaService.getSiswaByKelas(currentKelas);
        
        // Fetch existing presensi for this date
        List<Presensi> existingPresensi = null;
        try {
            existingPresensi = presensiService.getLaporanHarian(currentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (siswaList != null) {
            for (Siswa s : siswaList) {
                String status = "HADIR"; // Default
                Long presensiId = null;
                
                if (existingPresensi != null) {
                    for (Presensi p : existingPresensi) {
                        // Match by User ID (assuming Presensi.userId == Siswa.id)
                        if (p.getUserId() != null && p.getUserId().equals(s.getId())) {
                            status = p.getStatus();
                            presensiId = p.getId();
                            break;
                        }
                    }
                }
                
                SiswaPresensiRow row = new SiswaPresensiRow(s, status);
                row.setPresensiId(presensiId);
                tableData.add(row);
            }
        }
    }

    private void setupButtons() {
        cancelButton.setOnAction(e -> closeDialog());
        saveButton.setOnAction(e -> savePresensi());
    }

    private void savePresensi() {
        int successCount = 0;
        for (SiswaPresensiRow row : tableData) {
            Presensi p = new Presensi();
            p.setId(row.getPresensiId());
            p.setUserId(row.getSiswa().getId());
            p.setUsername(row.getSiswa().getNama());
            p.setTipe("SISWA");
            p.setTanggal(currentDate);
            p.setJamMasuk(currentJamMulai);
            p.setJamPulang(currentJamSelesai);
            p.setStatus(row.getStatus());
            p.setMethod("MANUAL");
            p.setKeterangan("Mapel: " + currentMapel);
            
            try {
                Presensi result;
                if (p.getId() != null) {
                    result = presensiService.updatePresensi(p);
                } else {
                    result = presensiService.createPresensi(p);
                }
                
                if (result != null) {
                    successCount++;
                    row.setPresensiId(result.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (successCount == tableData.size()) {
            InAppNotification.showSuccess("Berhasil", "Data presensi berhasil disimpan.");
            closeDialog();
        } else {
            InAppNotification.showError("Peringatan", "Beberapa data gagal disimpan (" + successCount + "/" + tableData.size() + ")");
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public static class SiswaPresensiRow {
        private final Siswa siswa;
        private final SimpleStringProperty status;
        private Long presensiId;

        public SiswaPresensiRow(Siswa siswa, String status) {
            this.siswa = siswa;
            this.status = new SimpleStringProperty(status);
        }

        public Siswa getSiswa() {
            return siswa;
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
        
        public SimpleStringProperty statusProperty() {
            return status;
        }

        public Long getPresensiId() {
            return presensiId;
        }

        public void setPresensiId(Long presensiId) {
            this.presensiId = presensiId;
        }
    }
}
