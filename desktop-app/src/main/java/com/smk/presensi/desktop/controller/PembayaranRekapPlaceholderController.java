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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Placeholder untuk rekap pembayaran.
 */
public class PembayaranRekapPlaceholderController implements Initializable {

    @FXML private TableView<RekapRow> rekapTable;
    @FXML private TableColumn<RekapRow, String> jenisColumn;
    @FXML private TableColumn<RekapRow, String> totalColumn;
    @FXML private TableColumn<RekapRow, Integer> lunasColumn;
    @FXML private TableColumn<RekapRow, Integer> belumColumn;
    @FXML private Label infoLabel;

    private final ObservableList<RekapRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jenisColumn.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalFormatted"));
        lunasColumn.setCellValueFactory(new PropertyValueFactory<>("lunas"));
        belumColumn.setCellValueFactory(new PropertyValueFactory<>("belum"));

        data.addAll(
                new RekapRow("SPP Bulanan", 3_500_000, 120, 15),
                new RekapRow("Daftar Ulang", 10_000_000, 98, 5),
                new RekapRow("Iuran Lab", 2_250_000, 80, 40)
        );

        rekapTable.setItems(data);
        infoLabel.setText("Total jenis rekap: " + data.size() + " (mock data)");
    }

    public static class RekapRow {
        private final String jenis;
        private final long total;
        private final int lunas;
        private final int belum;

        public RekapRow(String jenis, long total, int lunas, int belum) {
            this.jenis = jenis;
            this.total = total;
            this.lunas = lunas;
            this.belum = belum;
        }

        public String getJenis() { return jenis; }
        public int getLunas() { return lunas; }
        public int getBelum() { return belum; }
        public String getTotalFormatted() {
            return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(total);
        }
    }
}

