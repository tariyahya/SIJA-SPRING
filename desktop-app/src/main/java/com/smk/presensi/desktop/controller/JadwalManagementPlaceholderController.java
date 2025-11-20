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
 * Placeholder simple controller untuk jadwal mengajar.
 * Menampilkan mock data agar modul tidak kosong.
 */
public class JadwalManagementPlaceholderController implements Initializable {

    @FXML private TableView<JadwalRow> jadwalTable;
    @FXML private TableColumn<JadwalRow, String> hariColumn;
    @FXML private TableColumn<JadwalRow, String> mapelColumn;
    @FXML private TableColumn<JadwalRow, String> guruColumn;
    @FXML private TableColumn<JadwalRow, String> kelasColumn;
    @FXML private TableColumn<JadwalRow, String> jamColumn;
    @FXML private Label infoLabel;

    private final ObservableList<JadwalRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hariColumn.setCellValueFactory(new PropertyValueFactory<>("hari"));
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mapel"));
        guruColumn.setCellValueFactory(new PropertyValueFactory<>("guru"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        jamColumn.setCellValueFactory(new PropertyValueFactory<>("jam"));

        data.addAll(
                new JadwalRow("Senin", "Matematika", "Bu Rina", "XII RPL 1", "07.00 - 08.30"),
                new JadwalRow("Senin", "Pemrograman Web", "Pa Dedi", "XII RPL 1", "09.00 - 10.30"),
                new JadwalRow("Selasa", "Basis Data", "Pa Fajar", "XII RPL 2", "08.00 - 09.30"),
                new JadwalRow("Rabu", "Bahasa Inggris", "Bu Ani", "XI TKJ 1", "10.00 - 11.30")
        );

        jadwalTable.setItems(data);
        infoLabel.setText("Total jadwal: " + data.size() + " (mock data)");
    }

    public static class JadwalRow {
        private final String hari;
        private final String mapel;
        private final String guru;
        private final String kelas;
        private final String jam;

        public JadwalRow(String hari, String mapel, String guru, String kelas, String jam) {
            this.hari = hari;
            this.mapel = mapel;
            this.guru = guru;
            this.kelas = kelas;
            this.jam = jam;
        }

        public String getHari() { return hari; }
        public String getMapel() { return mapel; }
        public String getGuru() { return guru; }
        public String getKelas() { return kelas; }
        public String getJam() { return jam; }
    }
}

