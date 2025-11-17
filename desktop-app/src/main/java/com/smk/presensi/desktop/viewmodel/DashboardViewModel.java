package com.smk.presensi.desktop.viewmodel;

import com.smk.presensi.desktop.model.DashboardStats;
import com.smk.presensi.desktop.model.Presensi;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.PresensiService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;

/**
 * ViewModel untuk Dashboard
 * MVVM Pattern: Menjembatani antara View (FXML) dan Model (Service layer)
 * 
 * Responsibilities:
 * - Manage UI state (loading, error messages)
 * - Fetch data dari service
 * - Provide observable properties untuk UI binding
 */
public class DashboardViewModel {
    private final ApiClient apiClient;
    private final PresensiService presensiService;

    // Observable properties untuk UI binding
    private final ObservableList<Presensi> presensiList;
    private final IntegerProperty totalPresensi;
    private final IntegerProperty totalHadir;
    private final IntegerProperty totalTerlambat;
    private final IntegerProperty totalAlpha;
    private final DoubleProperty persentaseHadir;
    private final BooleanProperty loading;
    private final StringProperty errorMessage;
    private final BooleanProperty useMockData;

    public DashboardViewModel(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.presensiService = new PresensiService(apiClient);

        // Initialize observable properties
        this.presensiList = FXCollections.observableArrayList();
        this.totalPresensi = new SimpleIntegerProperty(0);
        this.totalHadir = new SimpleIntegerProperty(0);
        this.totalTerlambat = new SimpleIntegerProperty(0);
        this.totalAlpha = new SimpleIntegerProperty(0);
        this.persentaseHadir = new SimpleDoubleProperty(0.0);
        this.loading = new SimpleBooleanProperty(false);
        this.errorMessage = new SimpleStringProperty("");
        this.useMockData = new SimpleBooleanProperty(true); // Default: use mock data
    }

    /**
     * Load dashboard data (statistics + presensi list)
     */
    public void loadDashboardData() {
        loading.set(true);
        errorMessage.set("");

        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                if (useMockData.get()) {
                    // Use mock data (development mode)
                    List<Presensi> mockList = presensiService.getMockData();
                    DashboardStats mockStats = presensiService.getMockStats();
                    
                    // Update UI on JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        presensiList.setAll(mockList);
                        updateStats(mockStats);
                        loading.set(false);
                    });
                } else {
                    // Fetch from real API
                    List<Presensi> list = presensiService.getLaporanHarian(null);
                    DashboardStats stats = presensiService.getDashboardStats();
                    
                    javafx.application.Platform.runLater(() -> {
                        presensiList.setAll(list);
                        updateStats(stats);
                        loading.set(false);
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    errorMessage.set("Error loading data: " + e.getMessage());
                    loading.set(false);
                });
            }
        }).start();
    }

    /**
     * Refresh data (reload dari API)
     */
    public void refreshData() {
        loadDashboardData();
    }

    /**
     * RFID Checkin simulation
     */
    public void checkinRfid(String rfidCardId) {
        loading.set(true);
        errorMessage.set("");

        new Thread(() -> {
            try {
                if (useMockData.get()) {
                    // Simulate success
                    Thread.sleep(500); // Simulate network delay
                    javafx.application.Platform.runLater(() -> {
                        loading.set(false);
                        refreshData(); // Reload dashboard
                    });
                } else {
                    boolean success = presensiService.checkinRfid(rfidCardId);
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            refreshData();
                        } else {
                            errorMessage.set("Checkin gagal");
                        }
                        loading.set(false);
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    errorMessage.set("Error: " + e.getMessage());
                    loading.set(false);
                });
            }
        }).start();
    }

    /**
     * Filter presensi by status
     */
    public void filterByStatus(String status) {
        // TODO: Implement filtering logic
    }

    /**
     * Update statistics properties
     */
    private void updateStats(DashboardStats stats) {
        totalPresensi.set(stats.getTotalPresensi());
        totalHadir.set(stats.getTotalHadir());
        totalTerlambat.set(stats.getTotalTerlambat());
        totalAlpha.set(stats.getTotalAlpha());
        persentaseHadir.set(stats.getPersentaseHadir());
    }

    // Getters for observable properties
    public ObservableList<Presensi> getPresensiList() {
        return presensiList;
    }

    public IntegerProperty totalPresensiProperty() {
        return totalPresensi;
    }

    public IntegerProperty totalHadirProperty() {
        return totalHadir;
    }

    public IntegerProperty totalTerlambatProperty() {
        return totalTerlambat;
    }

    public IntegerProperty totalAlphaProperty() {
        return totalAlpha;
    }

    public DoubleProperty persentaseHadirProperty() {
        return persentaseHadir;
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public BooleanProperty useMockDataProperty() {
        return useMockData;
    }

    public void setUseMockData(boolean useMockData) {
        this.useMockData.set(useMockData);
    }
}
