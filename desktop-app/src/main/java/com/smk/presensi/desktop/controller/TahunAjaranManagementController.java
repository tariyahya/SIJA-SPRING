package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.TahunAjaran;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.TahunAjaranService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TahunAjaranManagementController implements Initializable {

    @FXML private TableView<TahunAjaran> tahunAjaranTable;
    @FXML private TableColumn<TahunAjaran, Long> idColumn;
    @FXML private TableColumn<TahunAjaran, String> namaColumn;
    @FXML private TableColumn<TahunAjaran, Integer> tahunMulaiColumn;
    @FXML private TableColumn<TahunAjaran, Integer> tahunSelesaiColumn;
    @FXML private TableColumn<TahunAjaran, String> semesterColumn;
    @FXML private TableColumn<TahunAjaran, String> statusColumn;
    @FXML private TableColumn<TahunAjaran, Void> actionColumn;

    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button setActiveButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private Label statusLabel;
    @FXML private Label activeTahunLabel;
    @FXML private Label activeSemesterLabel;
    @FXML private Label infoLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private TahunAjaranService tahunAjaranService;
    private ObservableList<TahunAjaran> tahunAjaranList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApiClient apiClient = ApiClient.getInstance();
        tahunAjaranService = new TahunAjaranService(apiClient);

        tahunAjaranList = FXCollections.observableArrayList();

        setupTableColumns();
        setupEventHandlers();
        loadData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        tahunMulaiColumn.setCellValueFactory(new PropertyValueFactory<>("tahunMulai"));
        tahunSelesaiColumn.setCellValueFactory(new PropertyValueFactory<>("tahunSelesai"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button setActiveBtn = new Button("Set Aktif");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, setActiveBtn, editBtn, deleteBtn);

            {
                setActiveBtn.getStyleClass().add("btn-primary");
                editBtn.getStyleClass().add("btn-warning");
                deleteBtn.getStyleClass().add("btn-danger");

                setActiveBtn.setOnAction(e -> handleSetActive(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    TahunAjaran ta = getTableRow().getItem();
                    setActiveBtn.setDisable(ta != null && "AKTIF".equalsIgnoreCase(ta.getStatus()));
                    setGraphic(pane);
                }
            }
        });

        tahunAjaranTable.setItems(tahunAjaranList);

        tahunAjaranTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            setActiveButton.setDisable(!hasSelection || "AKTIF".equalsIgnoreCase(newVal.getStatus()));
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadData());
        addButton.setOnAction(e -> handleAdd());
        setActiveButton.setOnAction(e -> {
            TahunAjaran selected = tahunAjaranTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleSetActive(selected);
            }
        });
        editButton.setOnAction(e -> {
            TahunAjaran selected = tahunAjaranTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEdit(selected);
            }
        });
        deleteButton.setOnAction(e -> {
            TahunAjaran selected = tahunAjaranTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDelete(selected);
            }
        });
    }

    private void loadData() {
        setLoading(true, "Memuat data tahun ajaran...");
        new Thread(() -> {
            try {
                List<TahunAjaran> data = tahunAjaranService.getAll();
                Platform.runLater(() -> {
                    tahunAjaranList.setAll(data);
                    updateActiveLabels();
                    infoLabel.setText("Total: " + tahunAjaranList.size() + " tahun ajaran");
                    setLoading(false, "Data tahun ajaran berhasil dimuat");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Gagal memuat data tahun ajaran");
                    InAppNotification.show("Gagal memuat data: " + e.getMessage(),
                            tahunAjaranTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private void updateActiveLabels() {
        TahunAjaran active = tahunAjaranList.stream()
                .filter(ta -> "AKTIF".equalsIgnoreCase(ta.getStatus()))
                .findFirst()
                .orElse(null);
        if (active != null) {
            activeTahunLabel.setText(active.getNama());
            activeSemesterLabel.setText(active.getSemester());
        } else {
            activeTahunLabel.setText("Tidak Ada");
            activeSemesterLabel.setText("-");
        }
    }

    private void handleAdd() {
        showFormDialog(null).ifPresent(ta -> {
            setLoading(true, "Menambah tahun ajaran...");
            new Thread(() -> {
                try {
                    tahunAjaranService.create(ta);
                    Platform.runLater(() -> {
                        loadData();
                        InAppNotification.show("Tahun ajaran berhasil ditambahkan",
                                tahunAjaranTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setLoading(false, "Gagal menambah tahun ajaran");
                        InAppNotification.show("Gagal menambah: " + e.getMessage(),
                                tahunAjaranTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    });
                }
            }).start();
        });
    }

    private void handleEdit(TahunAjaran existing) {
        if (existing == null) return;
        showFormDialog(existing).ifPresent(ta -> {
            setLoading(true, "Mengupdate tahun ajaran...");
            new Thread(() -> {
                try {
                    tahunAjaranService.update(existing.getId(), ta);
                    Platform.runLater(() -> {
                        loadData();
                        InAppNotification.show("Tahun ajaran berhasil diupdate",
                                tahunAjaranTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setLoading(false, "Gagal mengupdate tahun ajaran");
                        InAppNotification.show("Gagal update: " + e.getMessage(),
                                tahunAjaranTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    });
                }
            }).start();
        });
    }

    private void handleDelete(TahunAjaran ta) {
        if (ta == null) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Hapus");
        confirmation.setHeaderText("Hapus Tahun Ajaran?");
        confirmation.setContentText("Yakin ingin menghapus tahun ajaran: " + ta.getNama() + "?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                setLoading(true, "Menghapus tahun ajaran...");
                new Thread(() -> {
                    try {
                        tahunAjaranService.delete(ta.getId());
                        Platform.runLater(() -> {
                            loadData();
                            InAppNotification.show("Tahun ajaran berhasil dihapus",
                                    tahunAjaranTable.getParent(),
                                    InAppNotification.NotificationType.SUCCESS,
                                    3);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            setLoading(false, "Gagal menghapus tahun ajaran");
                            InAppNotification.show("Gagal hapus: " + e.getMessage(),
                                    tahunAjaranTable.getParent(),
                                    InAppNotification.NotificationType.ERROR,
                                    5);
                        });
                    }
                }).start();
            }
        });
    }

    private void handleSetActive(TahunAjaran ta) {
        if (ta == null) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Set Tahun Ajaran Aktif");
        confirmation.setHeaderText("Aktifkan Tahun Ajaran?");
        confirmation.setContentText("Set " + ta.getNama() + " - " + ta.getSemester() + " sebagai tahun ajaran aktif?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                setLoading(true, "Mengaktifkan tahun ajaran...");
                new Thread(() -> {
                    try {
                        tahunAjaranService.setActive(ta.getId());
                        Platform.runLater(() -> {
                            loadData();
                            InAppNotification.show("Tahun ajaran berhasil diaktifkan",
                                    tahunAjaranTable.getParent(),
                                    InAppNotification.NotificationType.SUCCESS,
                                    3);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            setLoading(false, "Gagal mengaktifkan tahun ajaran");
                            InAppNotification.show("Gagal aktivasi: " + e.getMessage(),
                                    tahunAjaranTable.getParent(),
                                    InAppNotification.NotificationType.ERROR,
                                    5);
                        });
                    }
                }).start();
            }
        });
    }

    private Optional<TahunAjaran> showFormDialog(TahunAjaran existing) {
        Dialog<TahunAjaran> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Tambah Tahun Ajaran" : "Edit Tahun Ajaran");
        dialog.setHeaderText(existing == null ? "Tambah tahun ajaran baru" : "Edit tahun ajaran");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField namaField = new TextField();
        TextField tahunMulaiField = new TextField();
        TextField tahunSelesaiField = new TextField();
        ComboBox<String> semesterCombo = new ComboBox<>();

        namaField.setPromptText("Nama (misal: 2024/2025)");
        tahunMulaiField.setPromptText("Tahun Mulai (misal: 2024)");
        tahunSelesaiField.setPromptText("Tahun Selesai (misal: 2025)");
        semesterCombo.setItems(FXCollections.observableArrayList("Ganjil", "Genap"));
        semesterCombo.setPromptText("Semester");

        if (existing != null) {
            namaField.setText(existing.getNama());
            if (existing.getTahunMulai() != null) {
                tahunMulaiField.setText(existing.getTahunMulai().toString());
            }
            if (existing.getTahunSelesai() != null) {
                tahunSelesaiField.setText(existing.getTahunSelesai().toString());
            }
            if (existing.getSemester() != null) {
                semesterCombo.getSelectionModel().select(existing.getSemester());
            }
        }

        grid.add(new Label("Nama:"), 0, 0);
        grid.add(namaField, 1, 0);
        grid.add(new Label("Tahun Mulai:"), 0, 1);
        grid.add(tahunMulaiField, 1, 1);
        grid.add(new Label("Tahun Selesai:"), 0, 2);
        grid.add(tahunSelesaiField, 1, 2);
        grid.add(new Label("Semester:"), 0, 3);
        grid.add(semesterCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(namaField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                TahunAjaran ta = new TahunAjaran();
                ta.setNama(namaField.getText().trim());
                try {
                    ta.setTahunMulai(Integer.parseInt(tahunMulaiField.getText().trim()));
                    ta.setTahunSelesai(Integer.parseInt(tahunSelesaiField.getText().trim()));
                } catch (NumberFormatException e) {
                    InAppNotification.show("Tahun harus berupa angka",
                            tahunAjaranTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            3);
                    return null;
                }
                ta.setSemester(semesterCombo.getSelectionModel().getSelectedItem());
                ta.setStatus("TIDAK_AKTIF");
                return ta;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void setLoading(boolean loading, String message) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(loading);
            statusLabel.setText(message);
        });
    }
}
