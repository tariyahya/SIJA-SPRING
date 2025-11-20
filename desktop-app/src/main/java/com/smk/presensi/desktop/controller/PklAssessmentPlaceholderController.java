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
 * Placeholder sederhana untuk Penilaian PKL.
 */
public class PklAssessmentPlaceholderController implements Initializable {

    @FXML private TableView<AssessmentRow> assessmentTable;
    @FXML private TableColumn<AssessmentRow, String> siswaColumn;
    @FXML private TableColumn<AssessmentRow, String> dudiColumn;
    @FXML private TableColumn<AssessmentRow, String> aspekColumn;
    @FXML private TableColumn<AssessmentRow, String> nilaiColumn;
    @FXML private TableColumn<AssessmentRow, String> catatanColumn;
    @FXML private Label infoLabel;

    private final ObservableList<AssessmentRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        siswaColumn.setCellValueFactory(new PropertyValueFactory<>("siswa"));
        dudiColumn.setCellValueFactory(new PropertyValueFactory<>("dudi"));
        aspekColumn.setCellValueFactory(new PropertyValueFactory<>("aspek"));
        nilaiColumn.setCellValueFactory(new PropertyValueFactory<>("nilai"));
        catatanColumn.setCellValueFactory(new PropertyValueFactory<>("catatan"));

        data.addAll(
                new AssessmentRow("Dimas Pratama", "PT Maju Jaya", "Disiplin", "A", "Sangat baik"),
                new AssessmentRow("Siti Rahma", "CV Networkindo", "Teknis", "B+", "Perlu memperkuat dokumentasi"),
                new AssessmentRow("Beni Setiawan", "Studio Kreatif", "Kreativitas", "A", "Layout menarik")
        );

        assessmentTable.setItems(data);
        infoLabel.setText("Total penilaian: " + data.size() + " (mock data)");
    }

    public static class AssessmentRow {
        private final String siswa;
        private final String dudi;
        private final String aspek;
        private final String nilai;
        private final String catatan;

        public AssessmentRow(String siswa, String dudi, String aspek, String nilai, String catatan) {
            this.siswa = siswa;
            this.dudi = dudi;
            this.aspek = aspek;
            this.nilai = nilai;
            this.catatan = catatan;
        }

        public String getSiswa() { return siswa; }
        public String getDudi() { return dudi; }
        public String getAspek() { return aspek; }
        public String getNilai() { return nilai; }
        public String getCatatan() { return catatan; }
    }
}

