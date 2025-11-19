package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AnalyticsReportController implements Initializable {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private BarChart<String, Number> attendanceChart;
    @FXML private PieChart statusPieChart;
    @FXML private Label totalPresensiLabel;
    @FXML private Label avgAttendanceLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private ApiClient apiClient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SessionManager sessionManager = new SessionManager();
        apiClient = ApiClient.getInstance();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }

        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());

        loadData();
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        loadingIndicator.setVisible(true);
        
        // Mock data for now, as backend integration requires parsing complex JSON
        // In real implementation, call /api/laporan/statistik
        
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate network delay
                
                javafx.application.Platform.runLater(() -> {
                    // Update Charts
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Kehadiran");
                    series.getData().add(new XYChart.Data<>("Senin", 95));
                    series.getData().add(new XYChart.Data<>("Selasa", 92));
                    series.getData().add(new XYChart.Data<>("Rabu", 98));
                    series.getData().add(new XYChart.Data<>("Kamis", 90));
                    series.getData().add(new XYChart.Data<>("Jumat", 85));
                    
                    attendanceChart.getData().clear();
                    attendanceChart.getData().add(series);
                    
                    statusPieChart.getData().clear();
                    statusPieChart.getData().add(new PieChart.Data("Hadir", 75));
                    statusPieChart.getData().add(new PieChart.Data("Terlambat", 15));
                    statusPieChart.getData().add(new PieChart.Data("Izin", 5));
                    statusPieChart.getData().add(new PieChart.Data("Alpha", 5));
                    
                    totalPresensiLabel.setText("450");
                    avgAttendanceLabel.setText("92%");
                    
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
