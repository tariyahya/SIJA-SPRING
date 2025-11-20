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
 * Placeholder sederhana untuk Monitoring Log PKL.
 */
public class PklLogPlaceholderController implements Initializable {

    @FXML private TableView<LogRow> logTable;
    @FXML private TableColumn<LogRow, LocalDate> tanggalColumn;
    @FXML private TableColumn<LogRow, String> siswaColumn;
    @FXML private TableColumn<LogRow, String> dudiColumn;
    @FXML private TableColumn<LogRow, String> aktivitasColumn;
    @FXML private TableColumn<LogRow, String> statusColumn;
    @FXML private Label infoLabel;

    private final ObservableList<LogRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        siswaColumn.setCellValueFactory(new PropertyValueFactory<>("siswa"));
        dudiColumn.setCellValueFactory(new PropertyValueFactory<>("dudi"));
        aktivitasColumn.setCellValueFactory(new PropertyValueFactory<>("aktivitas"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        data.addAll(
                new LogRow(LocalDate.now().minusDays(1), "Dimas Pratama", "PT Maju Jaya", "Setup jaringan kantor", "Diverifikasi"),
                new LogRow(LocalDate.now(), "Siti Rahma", "CV Networkindo", "Membuat dokumentasi SOP", "Menunggu"),
                new LogRow(LocalDate.now(), "Beni Setiawan", "Studio Kreatif", "Desain banner promosi", "Diverifikasi")
        );

        logTable.setItems(data);
        infoLabel.setText("Total log: " + data.size() + " (mock data)");
    }

    public static class LogRow {
        private final LocalDate tanggal;
        private final String siswa;
        private final String dudi;
        private final String aktivitas;
        private final String status;

        public LogRow(LocalDate tanggal, String siswa, String dudi, String aktivitas, String status) {
            this.tanggal = tanggal;
            this.siswa = siswa;
            this.dudi = dudi;
            this.aktivitas = aktivitas;
            this.status = status;
        }

        public LocalDate getTanggal() { return tanggal; }
        public String getSiswa() { return siswa; }
        public String getDudi() { return dudi; }
        public String getAktivitas() { return aktivitas; }
        public String getStatus() { return status; }
    }
}

