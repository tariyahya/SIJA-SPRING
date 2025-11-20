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
 * Placeholder sederhana untuk Penempatan PKL.
 */
public class PklPlacementPlaceholderController implements Initializable {

    @FXML private TableView<PlacementRow> placementTable;
    @FXML private TableColumn<PlacementRow, String> siswaColumn;
    @FXML private TableColumn<PlacementRow, String> kelasColumn;
    @FXML private TableColumn<PlacementRow, String> dudiColumn;
    @FXML private TableColumn<PlacementRow, LocalDate> mulaiColumn;
    @FXML private TableColumn<PlacementRow, LocalDate> selesaiColumn;
    @FXML private Label infoLabel;

    private final ObservableList<PlacementRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        siswaColumn.setCellValueFactory(new PropertyValueFactory<>("siswa"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        dudiColumn.setCellValueFactory(new PropertyValueFactory<>("dudi"));
        mulaiColumn.setCellValueFactory(new PropertyValueFactory<>("mulai"));
        selesaiColumn.setCellValueFactory(new PropertyValueFactory<>("selesai"));

        data.addAll(
                new PlacementRow("Dimas Pratama", "XII RPL 1", "PT Maju Jaya", LocalDate.now().minusDays(30), LocalDate.now().plusDays(60)),
                new PlacementRow("Siti Rahma", "XI TKJ 2", "CV Networkindo", LocalDate.now().minusDays(10), LocalDate.now().plusDays(90)),
                new PlacementRow("Beni Setiawan", "XII MM 1", "Studio Kreatif", LocalDate.now().minusDays(5), LocalDate.now().plusDays(85))
        );

        placementTable.setItems(data);
        infoLabel.setText("Total penempatan: " + data.size() + " (mock data)");
    }

    public static class PlacementRow {
        private final String siswa;
        private final String kelas;
        private final String dudi;
        private final LocalDate mulai;
        private final LocalDate selesai;

        public PlacementRow(String siswa, String kelas, String dudi, LocalDate mulai, LocalDate selesai) {
            this.siswa = siswa;
            this.kelas = kelas;
            this.dudi = dudi;
            this.mulai = mulai;
            this.selesai = selesai;
        }

        public String getSiswa() { return siswa; }
        public String getKelas() { return kelas; }
        public String getDudi() { return dudi; }
        public LocalDate getMulai() { return mulai; }
        public LocalDate getSelesai() { return selesai; }
    }
}

