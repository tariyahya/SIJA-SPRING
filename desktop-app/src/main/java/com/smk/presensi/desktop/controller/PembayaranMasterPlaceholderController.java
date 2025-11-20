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
 * Placeholder untuk master pembayaran.
 */
public class PembayaranMasterPlaceholderController implements Initializable {

    @FXML private TableView<MasterRow> masterTable;
    @FXML private TableColumn<MasterRow, String> kodeColumn;
    @FXML private TableColumn<MasterRow, String> namaColumn;
    @FXML private TableColumn<MasterRow, String> nominalColumn;
    @FXML private Label infoLabel;

    private final ObservableList<MasterRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        kodeColumn.setCellValueFactory(new PropertyValueFactory<>("kode"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        nominalColumn.setCellValueFactory(new PropertyValueFactory<>("nominalFormatted"));

        data.addAll(
                new MasterRow("SPP-01", "SPP Bulanan", 250_000),
                new MasterRow("DAFT-01", "Biaya Daftar Ulang", 500_000),
                new MasterRow("LAB-01", "Iuran Lab & Praktikum", 150_000)
        );

        masterTable.setItems(data);
        infoLabel.setText("Total jenis pembayaran: " + data.size() + " (mock data)");
    }

    public static class MasterRow {
        private final String kode;
        private final String nama;
        private final long nominal;

        public MasterRow(String kode, String nama, long nominal) {
            this.kode = kode;
            this.nama = nama;
            this.nominal = nominal;
        }

        public String getKode() { return kode; }
        public String getNama() { return nama; }
        public long getNominal() { return nominal; }
        public String getNominalFormatted() {
            return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(nominal);
        }
    }
}

