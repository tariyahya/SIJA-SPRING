package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Izin;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.IzinService;
import com.smk.presensi.desktop.service.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dashboard sederhana untuk Guru Piket.
 * Menampilkan ringkasan izin pending hari ini.
 */
public class DashboardPiketController implements Initializable {

    @FXML private Label subtitleLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label totalIzinLabel;
    @FXML private Label tanggalLabel;

    @FXML private TableView<Izin> izinTable;
    @FXML private TableColumn<Izin, String> siswaColumn;
    @FXML private TableColumn<Izin, String> kelasColumn;
    @FXML private TableColumn<Izin, String> jurusanColumn;
    @FXML private TableColumn<Izin, String> jenisColumn;
    @FXML private TableColumn<Izin, LocalDate> mulaiColumn;
    @FXML private TableColumn<Izin, LocalDate> selesaiColumn;
    @FXML private TableColumn<Izin, String> alasanColumn;

    @FXML private Button refreshButton;
    @FXML private Button openApprovalButton;

    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private IzinService izinService;
    private ObservableList<Izin> izinList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SessionManager sessionManager = new SessionManager();
        ApiClient apiClient = ApiClient.getInstance();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }

        izinService = new IzinService(apiClient);
        izinList = FXCollections.observableArrayList();

        setupTable();
        initHeader();
        loadData();
    }

    private void setupTable() {
        siswaColumn.setCellValueFactory(new PropertyValueFactory<>("siswaNama"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        jurusanColumn.setCellValueFactory(new PropertyValueFactory<>("jurusan"));
        jenisColumn.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        mulaiColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalMulai"));
        selesaiColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalSelesai"));
        alasanColumn.setCellValueFactory(new PropertyValueFactory<>("alasan"));

        izinTable.setItems(izinList);
    }

    private void initHeader() {
        LocalDate today = LocalDate.now();
        tanggalLabel.setText(today.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleOpenApproval() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/approval-izin.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Perizinan Siswa - Approval");
            stage.setScene(new Scene(root, 1100, 650));
            stage.showAndWait();

            // Refresh setelah layar approval ditutup
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal membuka layar approval", e.getMessage());
        }
    }

    private void loadData() {
        setLoading(true, "Memuat izin pending...");
        new Thread(() -> {
            try {
                List<Izin> pending = izinService.getPendingToday();
                Platform.runLater(() -> {
                    izinList.setAll(pending);
                    pendingCountLabel.setText(String.valueOf(pending.size()));
                    totalIzinLabel.setText(String.valueOf(pending.size()));
                    setLoading(false, "Memuat " + pending.size() + " izin pending");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memuat izin pending");
                    showAlert(Alert.AlertType.ERROR, "Gagal memuat data izin", e.getMessage());
                });
            }
        }).start();
    }

    private void setLoading(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        statusLabel.setText(message);
        refreshButton.setDisable(loading);
        openApprovalButton.setDisable(loading);
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("Dashboard Piket");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

