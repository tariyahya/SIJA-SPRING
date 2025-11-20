package com.smk.presensi.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Placeholder untuk Prestasi Siswa.
 */
public class PrestasiManagementPlaceholderController implements Initializable {

    @FXML private TableView<PrestasiRow> prestasiTable;
    @FXML private TableColumn<PrestasiRow, String> siswaColumn;
    @FXML private TableColumn<PrestasiRow, String> kelasColumn;
    @FXML private TableColumn<PrestasiRow, String> prestasiColumn;
    @FXML private TableColumn<PrestasiRow, String> tingkatColumn;
    @FXML private TableColumn<PrestasiRow, LocalDate> tanggalColumn;
    @FXML private Label infoLabel;

    private final ObservableList<PrestasiRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        siswaColumn.setCellValueFactory(new PropertyValueFactory<>("siswa"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        prestasiColumn.setCellValueFactory(new PropertyValueFactory<>("prestasi"));
        tingkatColumn.setCellValueFactory(new PropertyValueFactory<>("tingkat"));
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        data.addAll(
                new PrestasiRow("Larasati", "XII RPL 1", "Juara 1 LKS Software", "Nasional", LocalDate.now().minusDays(30)),
                new PrestasiRow("Bagas", "XI TKJ 2", "Juara 2 Networking", "Provinsi", LocalDate.now().minusDays(45)),
                new PrestasiRow("Rizky", "XII MM 1", "Juara 3 Desain Grafis", "Kota", LocalDate.now().minusDays(10))
        );

        prestasiTable.setItems(data);
        infoLabel.setText("Total prestasi: " + data.size() + " (mock data)");
    }

    public static class PrestasiRow {
        private final String siswa;
        private final String kelas;
        private final String prestasi;
        private final String tingkat;
        private final LocalDate tanggal;

        public PrestasiRow(String siswa, String kelas, String prestasi, String tingkat, LocalDate tanggal) {
            this.siswa = siswa;
            this.kelas = kelas;
            this.prestasi = prestasi;
            this.tingkat = tingkat;
            this.tanggal = tanggal;
        }

        public String getSiswa() { return siswa; }
        public String getKelas() { return kelas; }
        public String getPrestasi() { return prestasi; }
        public String getTingkat() { return tingkat; }
        public LocalDate getTanggal() { return tanggal; }
    }
}

