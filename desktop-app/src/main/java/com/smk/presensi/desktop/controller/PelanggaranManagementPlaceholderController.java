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
import java.util.ResourceBundle;

/**
 * Placeholder untuk Pelanggaran Siswa (data kasus).
 */
public class PelanggaranManagementPlaceholderController implements Initializable {

    @FXML private TableView<PelanggaranRow> pelanggaranTable;
    @FXML private TableColumn<PelanggaranRow, String> siswaColumn;
    @FXML private TableColumn<PelanggaranRow, String> kelasColumn;
    @FXML private TableColumn<PelanggaranRow, String> pelanggaranColumn;
    @FXML private TableColumn<PelanggaranRow, Integer> poinColumn;
    @FXML private TableColumn<PelanggaranRow, String> statusColumn;
    @FXML private Label infoLabel;

    private final ObservableList<PelanggaranRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        siswaColumn.setCellValueFactory(new PropertyValueFactory<>("siswa"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        pelanggaranColumn.setCellValueFactory(new PropertyValueFactory<>("pelanggaran"));
        poinColumn.setCellValueFactory(new PropertyValueFactory<>("poin"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        data.addAll(
                new PelanggaranRow("Andi Rahman", "XII RPL 1", "Tidak memakai seragam lengkap", 5, "Tercatat"),
                new PelanggaranRow("Siti Lestari", "XI TKJ 2", "Terlambat 3x", 10, "Diproses BK"),
                new PelanggaranRow("Budi Santoso", "XII MM 1", "Membawa HP saat ujian", 15, "Selesai")
        );

        pelanggaranTable.setItems(data);
        infoLabel.setText("Total kasus: " + data.size() + " (mock data)");
    }

    public static class PelanggaranRow {
        private final String siswa;
        private final String kelas;
        private final String pelanggaran;
        private final int poin;
        private final String status;

        public PelanggaranRow(String siswa, String kelas, String pelanggaran, int poin, String status) {
            this.siswa = siswa;
            this.kelas = kelas;
            this.pelanggaran = pelanggaran;
            this.poin = poin;
            this.status = status;
        }

        public String getSiswa() { return siswa; }
        public String getKelas() { return kelas; }
        public String getPelanggaran() { return pelanggaran; }
        public int getPoin() { return poin; }
        public String getStatus() { return status; }
    }
}

