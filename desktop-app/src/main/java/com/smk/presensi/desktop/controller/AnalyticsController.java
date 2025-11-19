package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.model.RekapSiswaPerJurusan;
import com.smk.presensi.desktop.model.RekapSiswaPerKelas;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.CachedPresensiService;
import com.smk.presensi.desktop.service.LaporanSiswaService;
import com.smk.presensi.desktop.service.PresensiService;
import com.smk.presensi.desktop.util.ChartUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Analytics View with Charts
 */
public class AnalyticsController {
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> dailyBarChart;
    @FXML private LineChart<String, Number> trendLineChart;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button refreshButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    // Rekap siswa (per kelas & jurusan)
    @FXML private TableView<RekapSiswaPerKelas> rekapKelasTable;
    @FXML private TableColumn<RekapSiswaPerKelas, String> rekapKelasKelasColumn;
    @FXML private TableColumn<RekapSiswaPerKelas, String> rekapKelasJurusanColumn;
    @FXML private TableColumn<RekapSiswaPerKelas, Number> rekapKelasTotalColumn;

    @FXML private TableView<RekapSiswaPerJurusan> rekapJurusanTable;
    @FXML private TableColumn<RekapSiswaPerJurusan, String> rekapJurusanJurusanColumn;
    @FXML private TableColumn<RekapSiswaPerJurusan, Number> rekapJurusanTotalColumn;

    private CachedPresensiService cachedService;
    private PresensiService presensiService;
    private LaporanSiswaService laporanSiswaService;

    @FXML
    public void initialize() {
        // Initialize services
        ApiClient apiClient = ApiClient.getInstance();
        presensiService = new PresensiService(apiClient);
        cachedService = new CachedPresensiService(presensiService);
        laporanSiswaService = new LaporanSiswaService(apiClient);

        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // Configure charts & tables
        configureCharts();
        configureRekapTables();

        // Load initial data
        refreshCharts();
    }

    private void configureCharts() {
        // PieChart configuration
        statusPieChart.setTitle("Status Distribution");
        statusPieChart.setLegendVisible(true);

        // BarChart configuration
        dailyBarChart.setTitle("Daily Attendance");
        CategoryAxis xAxis = (CategoryAxis) dailyBarChart.getXAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = (NumberAxis) dailyBarChart.getYAxis();
        yAxis.setLabel("Count");
        ChartUtils.configureIntegerAxis(yAxis);

        // LineChart configuration
        trendLineChart.setTitle("Weekly Trend");
        trendLineChart.setCreateSymbols(true);
        CategoryAxis lineXAxis = (CategoryAxis) trendLineChart.getXAxis();
        lineXAxis.setLabel("Week");
        NumberAxis lineYAxis = (NumberAxis) trendLineChart.getYAxis();
        lineYAxis.setLabel("Count");
        ChartUtils.configureIntegerAxis(lineYAxis);
    }

    private void configureRekapTables() {
        if (rekapKelasTable != null) {
            rekapKelasKelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
            rekapKelasJurusanColumn.setCellValueFactory(new PropertyValueFactory<>("jurusan"));
            rekapKelasTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalSiswa"));
        }

        if (rekapJurusanTable != null) {
            rekapJurusanJurusanColumn.setCellValueFactory(new PropertyValueFactory<>("jurusan"));
            rekapJurusanTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalSiswa"));
        }
    }

    @FXML
    private void handleRefresh() {
        refreshCharts();
    }

    @FXML
    private void handleRekapKelasRefresh() {
        loadRekapKelas();
    }

    @FXML
    private void handleRekapJurusanRefresh() {
        loadRekapJurusan();
    }

    @FXML
    private void handleRekapKelasExportCsv() {
        exportRekapKelasToCsv();
    }

    @FXML
    private void handleRekapJurusanExportCsv() {
        exportRekapJurusanToCsv();
    }

    private void refreshCharts() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            showError("Please select date range");
            return;
        }

        if (start.isAfter(end)) {
            showError("Start date must be before end date");
            return;
        }

        // Show loading
        setLoading(true);
        statusLabel.setText("Loading data...");

        Task<List<Presensi>> task = new Task<>() {
            @Override
            protected List<Presensi> call() throws Exception {
                return cachedService.getPresensiByDateRange(start, end);
            }
        };

        task.setOnSucceeded(e -> {
            List<Presensi> dataList = task.getValue();
            
            if (dataList.isEmpty()) {
                showWarning("No data found for selected date range");
                clearCharts();
            } else {
                updatePieChart(dataList);
                updateBarChart(dataList);
                updateLineChart(dataList);
                statusLabel.setText(String.format("Showing %d records from %s to %s", 
                    dataList.size(), start, end));
            }
            
            setLoading(false);
        });

        task.setOnFailed(e -> {
            showError("Failed to load data: " + task.getException().getMessage());
            setLoading(false);
        });

        new Thread(task).start();
    }

    private void loadRekapKelas() {
        if (laporanSiswaService == null || rekapKelasTable == null) {
            return;
        }

        setLoading(true);
        statusLabel.setText("Loading rekap siswa per kelas...");

        Task<List<RekapSiswaPerKelas>> task = new Task<>() {
            @Override
            protected List<RekapSiswaPerKelas> call() throws Exception {
                return laporanSiswaService.getRekapPerKelas();
            }
        };

        task.setOnSucceeded(e -> {
            List<RekapSiswaPerKelas> data = task.getValue();
            rekapKelasTable.setItems(FXCollections.observableArrayList(data));
            statusLabel.setText("Rekap siswa per kelas loaded (" + data.size() + " baris)");
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            setLoading(false);
        });

        task.setOnFailed(e -> {
            showError("Failed to load rekap per kelas: " + task.getException().getMessage());
            setLoading(false);
        });

        new Thread(task).start();
    }

    private void loadRekapJurusan() {
        if (laporanSiswaService == null || rekapJurusanTable == null) {
            return;
        }

        setLoading(true);
        statusLabel.setText("Loading rekap siswa per jurusan...");

        Task<List<RekapSiswaPerJurusan>> task = new Task<>() {
            @Override
            protected List<RekapSiswaPerJurusan> call() throws Exception {
                return laporanSiswaService.getRekapPerJurusan();
            }
        };

        task.setOnSucceeded(e -> {
            List<RekapSiswaPerJurusan> data = task.getValue();
            rekapJurusanTable.setItems(FXCollections.observableArrayList(data));
            statusLabel.setText("Rekap siswa per jurusan loaded (" + data.size() + " baris)");
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            setLoading(false);
        });

        task.setOnFailed(e -> {
            showError("Failed to load rekap per jurusan: " + task.getException().getMessage());
            setLoading(false);
        });

        new Thread(task).start();
    }

    private void updatePieChart(List<Presensi> dataList) {
        // Group by status and count
        Map<String, Long> statusCounts = dataList.stream()
            .collect(Collectors.groupingBy(
                Presensi::getStatus,
                Collectors.counting()
            ));

        // Create pie chart data
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        statusCounts.forEach((status, count) -> {
            pieData.add(new PieChart.Data(status + " (" + count + ")", count));
        });

        statusPieChart.setData(pieData);

        // Apply colors after chart renders
        Platform.runLater(() -> {
            ChartUtils.applyPieChartColors(statusPieChart, ChartUtils.getDefaultStatusColors());
        });
    }

    private void updateBarChart(List<Presensi> dataList) {
        // Group by date and count
        Map<LocalDate, Long> dailyCounts = dataList.stream()
            .collect(Collectors.groupingBy(
                Presensi::getTanggal,
                TreeMap::new,
                Collectors.counting()
            ));

        // Create series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Attendance");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");

        dailyCounts.forEach((date, count) -> {
            series.getData().add(new XYChart.Data<>(date.format(formatter), count));
        });

        dailyBarChart.getData().clear();
        dailyBarChart.getData().add(series);
    }

    private void updateLineChart(List<Presensi> dataList) {
        // Group by week
        Map<String, Long> weeklyCounts = dataList.stream()
            .collect(Collectors.groupingBy(
                presensi -> {
                    LocalDate date = presensi.getTanggal();
                    WeekFields weekFields = WeekFields.of(Locale.getDefault());
                    int week = date.get(weekFields.weekOfWeekBasedYear());
                    int year = date.getYear();
                    return "W" + week + " " + year;
                },
                TreeMap::new,
                Collectors.counting()
            ));

        // Create series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Weekly Total");

        weeklyCounts.forEach((week, count) -> {
            series.getData().add(new XYChart.Data<>(week, count));
        });

        trendLineChart.getData().clear();
        trendLineChart.getData().add(series);

        // Add tooltips
        Platform.runLater(() -> {
            ChartUtils.addTooltips(trendLineChart);
        });
    }

    private void clearCharts() {
        statusPieChart.getData().clear();
        dailyBarChart.getData().clear();
        trendLineChart.getData().clear();
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        refreshButton.setDisable(loading);
        startDatePicker.setDisable(loading);
        endDatePicker.setDisable(loading);
    }

    private void exportRekapKelasToCsv() {
        if (rekapKelasTable == null || rekapKelasTable.getItems().isEmpty()) {
            showWarning("Tidak ada data rekap kelas untuk diexport");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Rekap Siswa per Kelas (CSV)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("rekap-siswa-per-kelas.csv");

        java.io.File file = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            writer.write("Kelas,Jurusan,Total Siswa\n");
            for (RekapSiswaPerKelas row : rekapKelasTable.getItems()) {
                String kelas = row.getKelas() != null ? row.getKelas() : "";
                String jurusan = row.getJurusan() != null ? row.getJurusan() : "";
                writer.write(String.format("\"%s\",\"%s\",%d%n",
                        kelas.replace("\"", "\"\""),
                        jurusan.replace("\"", "\"\""),
                        row.getTotalSiswa()));
            }
            showInfo("Rekap siswa per kelas berhasil diexport ke CSV");
        } catch (Exception ex) {
            showError("Gagal export rekap kelas: " + ex.getMessage());
        }
    }

    private void exportRekapJurusanToCsv() {
        if (rekapJurusanTable == null || rekapJurusanTable.getItems().isEmpty()) {
            showWarning("Tidak ada data rekap jurusan untuk diexport");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Rekap Siswa per Jurusan (CSV)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("rekap-siswa-per-jurusan.csv");

        java.io.File file = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            writer.write("Jurusan,Total Siswa\n");
            for (RekapSiswaPerJurusan row : rekapJurusanTable.getItems()) {
                String jurusan = row.getJurusan() != null ? row.getJurusan() : "";
                writer.write(String.format("\"%s\",%d%n",
                        jurusan.replace("\"", "\"\""),
                        row.getTotalSiswa()));
            }
            showInfo("Rekap siswa per jurusan berhasil diexport ke CSV");
        } catch (Exception ex) {
            showError("Gagal export rekap jurusan: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        statusLabel.setStyle("-fx-text-fill: #f44336;");
    }

    private void showWarning(String message) {
        statusLabel.setText("Warning: " + message);
        statusLabel.setStyle("-fx-text-fill: #FF9800;");
    }

    @FXML
    private void handleClearCache() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cache");
        alert.setHeaderText("Clear all cached data?");
        alert.setContentText("This will remove all offline data. You'll need internet to reload.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cachedService.clearAllCaches();
            showInfo("Cache cleared successfully");
        }
    }

    private void showInfo(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
    }
}
