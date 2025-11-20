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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Placeholder untuk transaksi pembayaran.
 */
public class PembayaranTransaksiPlaceholderController implements Initializable {

    @FXML private TableView<TransaksiRow> transaksiTable;
    @FXML private TableColumn<TransaksiRow, LocalDate> tanggalColumn;
    @FXML private TableColumn<TransaksiRow, String> siswaColumn;
    @FXML private TableColumn<TransaksiRow, String> jenisColumn;
    @FXML private TableColumn<TransaksiRow, String> nominalColumn;
    @FXML private TableColumn<TransaksiRow, String> statusColumn;
    @FXML private TableColumn<TransaksiRow, String> metodeColumn;
    @FXML private Label infoLabel;

    private final ObservableList<TransaksiRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        siswaColumn.setCellValueFactory(new PropertyValueFactory<>("siswa"));
        jenisColumn.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        nominalColumn.setCellValueFactory(new PropertyValueFactory<>("nominalFormatted"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        metodeColumn.setCellValueFactory(new PropertyValueFactory<>("metode"));

        data.addAll(
                new TransaksiRow(LocalDate.now(), "Ahmad Rifqi", "SPP Bulanan", 250_000, "Lunas", "Cash"),
                new TransaksiRow(LocalDate.now().minusDays(2), "Siti Aisyah", "Iuran Lab", 150_000, "Menunggu", "Transfer"),
                new TransaksiRow(LocalDate.now().minusDays(5), "Budi Setiawan", "Daftar Ulang", 500_000, "Lunas", "VA Bank")
        );

        transaksiTable.setItems(data);
        infoLabel.setText("Total transaksi: " + data.size() + " (mock data)");
    }

    public static class TransaksiRow {
        private final LocalDate tanggal;
        private final String siswa;
        private final String jenis;
        private final long nominal;
        private final String status;
        private final String metode;

        public TransaksiRow(LocalDate tanggal, String siswa, String jenis, long nominal, String status, String metode) {
            this.tanggal = tanggal;
            this.siswa = siswa;
            this.jenis = jenis;
            this.nominal = nominal;
            this.status = status;
            this.metode = metode;
        }

        public LocalDate getTanggal() { return tanggal; }
        public String getSiswa() { return siswa; }
        public String getJenis() { return jenis; }
        public String getStatus() { return status; }
        public String getMetode() { return metode; }
        public String getNominalFormatted() {
            return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(nominal);
        }
    }
}

