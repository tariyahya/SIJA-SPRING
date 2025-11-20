package com.smk.presensi.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Placeholder sederhana untuk rekap presensi.
 * Menampilkan mock data agar halaman tidak kosong.
 */
public class PresensiRekapPlaceholderController implements Initializable {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button filterButton;
    @FXML private Button resetButton;

    @FXML private TableView<RekapRow> rekapTable;
    @FXML private TableColumn<RekapRow, LocalDate> tanggalColumn;
    @FXML private TableColumn<RekapRow, String> kelasColumn;
    @FXML private TableColumn<RekapRow, Integer> hadirColumn;
    @FXML private TableColumn<RekapRow, Integer> izinColumn;
    @FXML private TableColumn<RekapRow, Integer> sakitColumn;
    @FXML private TableColumn<RekapRow, Integer> alfaColumn;

    @FXML private Label infoLabel;

    private final ObservableList<RekapRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());

        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        hadirColumn.setCellValueFactory(new PropertyValueFactory<>("hadir"));
        izinColumn.setCellValueFactory(new PropertyValueFactory<>("izin"));
        sakitColumn.setCellValueFactory(new PropertyValueFactory<>("sakit"));
        alfaColumn.setCellValueFactory(new PropertyValueFactory<>("alfa"));

        data.addAll(
                new RekapRow(LocalDate.now().minusDays(1), "XII RPL 1", 32, 2, 1, 0),
                new RekapRow(LocalDate.now().minusDays(1), "XI TKJ 2", 28, 3, 2, 1),
                new RekapRow(LocalDate.now(), "XII RPL 1", 30, 3, 1, 1),
                new RekapRow(LocalDate.now(), "XI TKJ 2", 29, 2, 1, 2)
        );

        rekapTable.setItems(data);
        updateInfo();

        filterButton.setOnAction(e -> applyFilter());
        resetButton.setOnAction(e -> resetFilter());
    }

    private void applyFilter() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        ObservableList<RekapRow> filtered = data.filtered(r -> {
            boolean afterStart = start == null || !r.getTanggal().isBefore(start);
            boolean beforeEnd = end == null || !r.getTanggal().isAfter(end);
            return afterStart && beforeEnd;
        });
        rekapTable.setItems(filtered);
        updateInfo(filtered.size(), data.size());
    }

    private void resetFilter() {
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());
        rekapTable.setItems(data);
        updateInfo();
    }

    private void updateInfo() {
        updateInfo(data.size(), data.size());
    }

    private void updateInfo(int filtered, int total) {
        if (filtered == total) {
            infoLabel.setText("Total baris: " + total + " (mock data)");
        } else {
            infoLabel.setText("Total baris: " + filtered + " dari " + total + " (mock data)");
        }
    }

    public static class RekapRow {
        private final LocalDate tanggal;
        private final String kelas;
        private final int hadir;
        private final int izin;
        private final int sakit;
        private final int alfa;

        public RekapRow(LocalDate tanggal, String kelas, int hadir, int izin, int sakit, int alfa) {
            this.tanggal = tanggal;
            this.kelas = kelas;
            this.hadir = hadir;
            this.izin = izin;
            this.sakit = sakit;
            this.alfa = alfa;
        }

        public LocalDate getTanggal() { return tanggal; }
        public String getKelas() { return kelas; }
        public int getHadir() { return hadir; }
        public int getIzin() { return izin; }
        public int getSakit() { return sakit; }
        public int getAlfa() { return alfa; }
    }
}

