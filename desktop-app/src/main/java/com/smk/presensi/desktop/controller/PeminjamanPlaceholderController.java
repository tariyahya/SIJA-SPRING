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
 * Placeholder sederhana untuk Peminjaman Sarpras.
 */
public class PeminjamanPlaceholderController implements Initializable {

    @FXML private TableView<PeminjamanRow> pinjamTable;
    @FXML private TableColumn<PeminjamanRow, String> barangColumn;
    @FXML private TableColumn<PeminjamanRow, String> peminjamColumn;
    @FXML private TableColumn<PeminjamanRow, LocalDate> pinjamDateColumn;
    @FXML private TableColumn<PeminjamanRow, LocalDate> kembaliDateColumn;
    @FXML private TableColumn<PeminjamanRow, String> statusColumn;
    @FXML private Label infoLabel;

    private final ObservableList<PeminjamanRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        barangColumn.setCellValueFactory(new PropertyValueFactory<>("barang"));
        peminjamColumn.setCellValueFactory(new PropertyValueFactory<>("peminjam"));
        pinjamDateColumn.setCellValueFactory(new PropertyValueFactory<>("pinjam"));
        kembaliDateColumn.setCellValueFactory(new PropertyValueFactory<>("kembali"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        data.addAll(
                new PeminjamanRow("Laptop Lenovo", "Pa Andi", LocalDate.now().minusDays(2), LocalDate.now().plusDays(3), "Dipinjam"),
                new PeminjamanRow("Proyektor Epson", "Bu Sari", LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), "Dikembalikan"),
                new PeminjamanRow("Router Mikrotik", "Lab TKJ", LocalDate.now(), LocalDate.now().plusDays(7), "Dipinjam")
        );

        pinjamTable.setItems(data);
        infoLabel.setText("Total peminjaman: " + data.size() + " (mock data)");
    }

    public static class PeminjamanRow {
        private final String barang;
        private final String peminjam;
        private final LocalDate pinjam;
        private final LocalDate kembali;
        private final String status;

        public PeminjamanRow(String barang, String peminjam, LocalDate pinjam, LocalDate kembali, String status) {
            this.barang = barang;
            this.peminjam = peminjam;
            this.pinjam = pinjam;
            this.kembali = kembali;
            this.status = status;
        }

        public String getBarang() { return barang; }
        public String getPeminjam() { return peminjam; }
        public LocalDate getPinjam() { return pinjam; }
        public LocalDate getKembali() { return kembali; }
        public String getStatus() { return status; }
    }
}

