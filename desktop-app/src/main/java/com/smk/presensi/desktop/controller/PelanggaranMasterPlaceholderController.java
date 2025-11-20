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
 * Placeholder untuk master data pelanggaran.
 */
public class PelanggaranMasterPlaceholderController implements Initializable {

    @FXML private TableView<MasterRow> masterTable;
    @FXML private TableColumn<MasterRow, String> kodeColumn;
    @FXML private TableColumn<MasterRow, String> namaColumn;
    @FXML private TableColumn<MasterRow, Integer> poinColumn;
    @FXML private Label infoLabel;

    private final ObservableList<MasterRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        kodeColumn.setCellValueFactory(new PropertyValueFactory<>("kode"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        poinColumn.setCellValueFactory(new PropertyValueFactory<>("poin"));

        data.addAll(
                new MasterRow("PL-01", "Alfa Tanpa Keterangan", 10),
                new MasterRow("PL-02", "Terlambat", 3),
                new MasterRow("PL-03", "Tidak berseragam", 5)
        );

        masterTable.setItems(data);
        infoLabel.setText("Total pelanggaran master: " + data.size() + " (mock data)");
    }

    public static class MasterRow {
        private final String kode;
        private final String nama;
        private final int poin;

        public MasterRow(String kode, String nama, int poin) {
            this.kode = kode;
            this.nama = nama;
            this.poin = poin;
        }

        public String getKode() { return kode; }
        public String getNama() { return nama; }
        public int getPoin() { return poin; }
    }
}

