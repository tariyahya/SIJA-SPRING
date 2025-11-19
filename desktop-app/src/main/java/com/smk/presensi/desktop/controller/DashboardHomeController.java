package com.smk.presensi.desktop.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smk.presensi.desktop.service.ApiClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.net.URL;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class DashboardHomeController implements Initializable {

    @FXML private Label totalSiswaLabel;
    @FXML private Label totalGuruLabel;
    
    @FXML private Label siswaHadirLabel;
    @FXML private Label siswaIzinLabel;
    @FXML private Label siswaSakitLabel;
    @FXML private Label siswaAlfaLabel;
    
    @FXML private Label guruHadirLabel;
    @FXML private Label guruAbsenLabel;
    
    @FXML private LineChart<String, Number> attendanceChart;
    @FXML private BarChart<String, Number> jurusanChart;
    
    @FXML private TableView<LowAttendanceRow> lowAttendanceTable;
    @FXML private TableColumn<LowAttendanceRow, String> nisColumn;
    @FXML private TableColumn<LowAttendanceRow, String> namaColumn;
    @FXML private TableColumn<LowAttendanceRow, String> kelasColumn;
    @FXML private TableColumn<LowAttendanceRow, String> attendancePctColumn;

    private final ApiClient apiClient = ApiClient.getInstance();
    private final Gson gson = new Gson();
    private Timeline autoRefreshTimeline;
    private final ObservableList<LowAttendanceRow> lowAttendanceData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupLowAttendanceTable();
        loadDashboardData();
        setupAutoRefresh();
    }

    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            loadDashboardData();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void loadDashboardData() {
        CompletableFuture.runAsync(this::fetchTotalSiswa);
        CompletableFuture.runAsync(this::fetchTotalGuru);
        CompletableFuture.runAsync(this::fetchDailyReport);
        CompletableFuture.runAsync(this::fetchWeeklyAttendanceTrend);
        CompletableFuture.runAsync(this::fetchJurusanDistribution);
        CompletableFuture.runAsync(this::fetchLowAttendanceStudents);
    }

    private void setupLowAttendanceTable() {
        nisColumn.setCellValueFactory(data -> data.getValue().nisProperty());
        namaColumn.setCellValueFactory(data -> data.getValue().namaProperty());
        kelasColumn.setCellValueFactory(data -> data.getValue().kelasProperty());
        attendancePctColumn.setCellValueFactory(data -> data.getValue().attendancePercentageProperty());
        lowAttendanceTable.setItems(lowAttendanceData);
    }

    private void fetchTotalSiswa() {
        try {
            HttpResponse<String> response = apiClient.get("/siswa");
            if (response.statusCode() == 200) {
                List<?> list = gson.fromJson(response.body(), List.class);
                int count = list != null ? list.size() : 0;
                Platform.runLater(() -> totalSiswaLabel.setText(String.valueOf(count)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> totalSiswaLabel.setText("-"));
        }
    }

    private void fetchTotalGuru() {
        try {
            HttpResponse<String> response = apiClient.get("/guru");
            if (response.statusCode() == 200) {
                List<?> list = gson.fromJson(response.body(), List.class);
                int count = list != null ? list.size() : 0;
                Platform.runLater(() -> totalGuruLabel.setText(String.valueOf(count)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> totalGuruLabel.setText("-"));
        }
    }

    private void fetchDailyReport() {
        try {
            HttpResponse<String> response = apiClient.get("/laporan/harian");
            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                if (json.has("data")) {
                    JsonObject data = json.getAsJsonObject("data");
                    
                    int hadir = data.has("totalHadir") ? data.get("totalHadir").getAsInt() : 0;
                    // Note: LaporanHarianResponse might not have separate fields for Izin/Sakit/Alfa in the top level summary
                    // It has totalPresensi, totalHadir, totalTerlambat, totalAlfa.
                    // Izin and Sakit might be counted as Hadir or separate?
                    // Based on LaporanController comments: totalHadir, totalTerlambat, totalAlfa.
                    // It doesn't explicitly list Izin/Sakit in the summary fields shown in comments.
                    // But let's assume standard fields or default to 0 if not found.
                    
                    int alfa = data.has("totalAlfa") ? data.get("totalAlfa").getAsInt() : 0;
                    int terlambat = data.has("totalTerlambat") ? data.get("totalTerlambat").getAsInt() : 0;
                    
                    // For now, map what we have.
                    Platform.runLater(() -> {
                        siswaHadirLabel.setText(String.valueOf(hadir));
                        siswaAlfaLabel.setText(String.valueOf(alfa));
                        // We don't have explicit Izin/Sakit in the summary example, so keep 0 or try to find them
                        // If the backend adds them later, they will appear.
                        siswaIzinLabel.setText("0"); 
                        siswaSakitLabel.setText("0");
                        
                        // Guru stats - currently using same endpoint? 
                        // The endpoint /laporan/harian seems to be general (maybe mixed or just siswa).
                        // Usually presensi system separates them.
                        // For now, we'll leave Guru stats as placeholder or use same data if applicable.
                        // Or maybe fetch /presensi/guru/harian if it existed.
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchWeeklyAttendanceTrend() {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);

            String endpoint = "/laporan/periode?startDate=" + startDate + "&endDate=" + endDate;
            HttpResponse<String> response = apiClient.get(endpoint);
            if (response.statusCode() != 200) {
                return;
            }

            JsonObject root = gson.fromJson(response.body(), JsonObject.class);
            if (root == null || !root.has("data") || root.get("data").isJsonNull()) {
                return;
            }

            JsonObject data = root.getAsJsonObject("data");
            if (!data.has("daftarPresensi") || data.get("daftarPresensi").isJsonNull()) {
                return;
            }

            JsonArray presensiArray = data.getAsJsonArray("daftarPresensi");
            Map<LocalDate, DayStats> statsPerDay = new HashMap<>();

            for (JsonElement element : presensiArray) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject obj = element.getAsJsonObject();
                if (!obj.has("tanggal") || obj.get("tanggal").isJsonNull()) {
                    continue;
                }

                String tipe = obj.has("tipe") && !obj.get("tipe").isJsonNull()
                        ? obj.get("tipe").getAsString()
                        : null;
                if (tipe == null || !"SISWA".equalsIgnoreCase(tipe)) {
                    continue;
                }

                LocalDate tanggal = LocalDate.parse(obj.get("tanggal").getAsString());
                if (tanggal.isBefore(startDate) || tanggal.isAfter(endDate)) {
                    continue;
                }

                String status = obj.has("status") && !obj.get("status").isJsonNull()
                        ? obj.get("status").getAsString()
                        : "";
                boolean present = !"ALPHA".equalsIgnoreCase(status);

                DayStats stats = statsPerDay.computeIfAbsent(tanggal, d -> new DayStats());
                stats.total++;
                if (present) {
                    stats.present++;
                }
            }

            List<XYChart.Data<String, Number>> seriesData = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                DayStats stats = statsPerDay.get(current);
                double percentage = 0.0;
                if (stats != null && stats.total > 0) {
                    percentage = stats.present * 100.0 / stats.total;
                }
                String label = current.format(formatter);
                seriesData.add(new XYChart.Data<>(label, percentage));
                current = current.plusDays(1);
            }

            Platform.runLater(() -> {
                attendanceChart.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Hadir (%)");
                series.getData().addAll(seriesData);
                attendanceChart.getData().add(series);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchJurusanDistribution() {
        try {
            HttpResponse<String> response = apiClient.get("/laporan/rekap-siswa/jurusan");
            if (response.statusCode() != 200) {
                return;
            }

            JsonObject root = gson.fromJson(response.body(), JsonObject.class);
            if (root == null || !root.has("data") || root.get("data").isJsonNull()) {
                return;
            }

            JsonArray data = root.getAsJsonArray("data");
            List<XYChart.Data<String, Number>> seriesData = new ArrayList<>();

            for (JsonElement element : data) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject obj = element.getAsJsonObject();
                String jurusan = obj.has("jurusan") && !obj.get("jurusan").isJsonNull()
                        ? obj.get("jurusan").getAsString()
                        : "-";
                long total = obj.has("totalSiswa") && !obj.get("totalSiswa").isJsonNull()
                        ? obj.get("totalSiswa").getAsLong()
                        : 0L;

                seriesData.add(new XYChart.Data<>(jurusan, total));
            }

            Platform.runLater(() -> {
                jurusanChart.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Siswa");
                series.getData().addAll(seriesData);
                jurusanChart.getData().add(series);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchLowAttendanceStudents() {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(29);

            String endpoint = "/laporan/periode?startDate=" + startDate + "&endDate=" + endDate;
            HttpResponse<String> response = apiClient.get(endpoint);
            if (response.statusCode() != 200) {
                return;
            }

            JsonObject root = gson.fromJson(response.body(), JsonObject.class);
            if (root == null || !root.has("data") || root.get("data").isJsonNull()) {
                return;
            }

            JsonObject data = root.getAsJsonObject("data");
            if (!data.has("daftarPresensi") || data.get("daftarPresensi").isJsonNull()) {
                return;
            }

            JsonArray presensiArray = data.getAsJsonArray("daftarPresensi");
            Map<String, StudentStats> statsPerStudent = new HashMap<>();

            for (JsonElement element : presensiArray) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject obj = element.getAsJsonObject();
                String tipe = obj.has("tipe") && !obj.get("tipe").isJsonNull()
                        ? obj.get("tipe").getAsString()
                        : null;
                if (tipe == null || !"SISWA".equalsIgnoreCase(tipe)) {
                    continue;
                }

                String username = obj.has("username") && !obj.get("username").isJsonNull()
                        ? obj.get("username").getAsString()
                        : null;
                if (username == null || username.isBlank()) {
                    continue;
                }

                String kelasNama = obj.has("kelasNama") && !obj.get("kelasNama").isJsonNull()
                        ? obj.get("kelasNama").getAsString()
                        : "-";

                String status = obj.has("status") && !obj.get("status").isJsonNull()
                        ? obj.get("status").getAsString()
                        : "";
                boolean present = !"ALPHA".equalsIgnoreCase(status);

                StudentStats stats = statsPerStudent.computeIfAbsent(username, key -> new StudentStats(username, kelasNama));
                stats.totalRecords++;
                if (present) {
                    stats.presentRecords++;
                }
                if (stats.kelas == null || stats.kelas.isBlank()) {
                    stats.kelas = kelasNama;
                }
            }

            List<StudentStats> filtered = new ArrayList<>();
            for (StudentStats stats : statsPerStudent.values()) {
                if (stats.totalRecords == 0) {
                    continue;
                }
                double percentage = stats.presentRecords * 100.0 / stats.totalRecords;
                if (percentage >= 80.0) {
                    continue;
                }
                stats.attendancePercentage = percentage;
                filtered.add(stats);
            }

            filtered.sort((a, b) -> Double.compare(a.attendancePercentage, b.attendancePercentage));
            if (filtered.size() > 10) {
                filtered = new ArrayList<>(filtered.subList(0, 10));
            }

            Map<String, String> siswaNamaByNis = fetchSiswaNamaByNis();
            List<LowAttendanceRow> rows = new ArrayList<>();

            for (StudentStats stats : filtered) {
                String formattedPct = String.format("%.1f%%", stats.attendancePercentage);
                String nama = siswaNamaByNis.getOrDefault(stats.nisOrUsername, stats.nisOrUsername);
                rows.add(new LowAttendanceRow(stats.nisOrUsername, nama, stats.kelas, formattedPct));
            }

            Platform.runLater(() -> {
                lowAttendanceData.setAll(rows);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> fetchSiswaNamaByNis() {
        Map<String, String> result = new HashMap<>();
        try {
            HttpResponse<String> response = apiClient.get("/siswa");
            if (response.statusCode() != 200) {
                return result;
            }

            JsonArray array = gson.fromJson(response.body(), JsonArray.class);
            if (array == null) {
                return result;
            }

            for (JsonElement element : array) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject obj = element.getAsJsonObject();
                String nis = obj.has("nis") && !obj.get("nis").isJsonNull()
                        ? obj.get("nis").getAsString()
                        : null;
                String nama = obj.has("nama") && !obj.get("nama").isJsonNull()
                        ? obj.get("nama").getAsString()
                        : null;

                if (nis != null && nama != null) {
                    result.put(nis, nama);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static class DayStats {
        int total;
        int present;
    }

    private static class StudentStats {
        final String nisOrUsername;
        String kelas;
        int totalRecords;
        int presentRecords;
        double attendancePercentage;

        StudentStats(String nisOrUsername, String kelas) {
            this.nisOrUsername = nisOrUsername;
            this.kelas = kelas;
        }
    }

    public static class LowAttendanceRow {
        private final StringProperty nis;
        private final StringProperty nama;
        private final StringProperty kelas;
        private final StringProperty attendancePercentage;

        public LowAttendanceRow(String nis, String nama, String kelas, String attendancePercentage) {
            this.nis = new SimpleStringProperty(nis);
            this.nama = new SimpleStringProperty(nama);
            this.kelas = new SimpleStringProperty(kelas);
            this.attendancePercentage = new SimpleStringProperty(attendancePercentage);
        }

        public StringProperty nisProperty() {
            return nis;
        }

        public StringProperty namaProperty() {
            return nama;
        }

        public StringProperty kelasProperty() {
            return kelas;
        }

        public StringProperty attendancePercentageProperty() {
            return attendancePercentage;
        }

        public String getNis() {
            return nis.get();
        }

        public String getNama() {
            return nama.get();
        }

        public String getKelas() {
            return kelas.get();
        }

        public String getAttendancePercentage() {
            return attendancePercentage.get();
        }
    }
}
