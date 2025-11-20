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
 * Placeholder sederhana untuk Manajemen Karyawan.
 */
public class KaryawanPlaceholderController implements Initializable {

    @FXML private TableView<KaryawanRow> karyawanTable;
    @FXML private TableColumn<KaryawanRow, String> nipColumn;
    @FXML private TableColumn<KaryawanRow, String> namaColumn;
    @FXML private TableColumn<KaryawanRow, String> jabatanColumn;
    @FXML private TableColumn<KaryawanRow, String> statusColumn;
    @FXML private Label infoLabel;

    private final ObservableList<KaryawanRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nipColumn.setCellValueFactory(new PropertyValueFactory<>("nip"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        jabatanColumn.setCellValueFactory(new PropertyValueFactory<>("jabatan"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        data.addAll(
                new KaryawanRow("19700101 199803 1 001", "Agus Santoso", "TU", "Aktif"),
                new KaryawanRow("19750215 200012 2 001", "Sri Wulandari", "Keuangan", "Aktif"),
                new KaryawanRow("19800320 200501 1 002", "Bambang Nugroho", "Laboran", "Aktif")
        );

        karyawanTable.setItems(data);
        infoLabel.setText("Total karyawan: " + data.size() + " (mock data)");
    }

    public static class KaryawanRow {
        private final String nip;
        private final String nama;
        private final String jabatan;
        private final String status;

        public KaryawanRow(String nip, String nama, String jabatan, String status) {
            this.nip = nip;
            this.nama = nama;
            this.jabatan = jabatan;
            this.status = status;
        }

        public String getNip() { return nip; }
        public String getNama() { return nama; }
        public String getJabatan() { return jabatan; }
        public String getStatus() { return status; }
    }
}

