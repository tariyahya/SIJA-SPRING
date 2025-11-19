package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.LokasiKantor;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.LokasiKantorService;
import com.smk.presensi.desktop.service.SessionManager;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class OfficeManagementController implements Initializable {

    @FXML private TableView<LokasiKantor> table;
    @FXML private TableColumn<LokasiKantor, String> namaColumn;
    @FXML private TableColumn<LokasiKantor, Double> latColumn;
    @FXML private TableColumn<LokasiKantor, Double> longColumn;
    @FXML private TableColumn<LokasiKantor, Integer> radiusColumn;
    @FXML private TableColumn<LokasiKantor, Boolean> activeColumn;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button activateButton;
    @FXML private ProgressIndicator loadingIndicator;

    private LokasiKantorService service;
    private ObservableList<LokasiKantor> list;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SessionManager sessionManager = new SessionManager();
        ApiClient apiClient = ApiClient.getInstance();
        if (sessionManager.isLoggedIn()) {
            apiClient.setJwtToken(sessionManager.getJwtToken());
        }
        service = new LokasiKantorService(apiClient);
        list = FXCollections.observableArrayList();

        setupTable();
        loadData();
    }

    private void setupTable() {
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        latColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        longColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        radiusColumn.setCellValueFactory(new PropertyValueFactory<>("radiusValidasi"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        activeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(active ? "ACTIVE" : "");
                    setStyle(active ? "-fx-text-fill: green; -fx-font-weight: bold;" : "");
                }
            }
        });

        table.setItems(list);
        
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            editButton.setDisable(!selected);
            deleteButton.setDisable(!selected);
            activateButton.setDisable(!selected);
        });
    }

    private void loadData() {
        loadingIndicator.setVisible(true);
        new Thread(() -> {
            try {
                var data = service.getAll();
                Platform.runLater(() -> {
                    list.setAll(data);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> loadingIndicator.setVisible(false));
            }
        }).start();
    }

    @FXML
    private void handleAdd() {
        LokasiKantor result = showForm(null);
        if (result != null) {
            new Thread(() -> {
                try {
                    service.create(result);
                    Platform.runLater(this::loadData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    private void handleEdit() {
        LokasiKantor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        LokasiKantor result = showForm(selected);
        if (result != null) {
            new Thread(() -> {
                try {
                    service.update(selected.getId(), result);
                    Platform.runLater(this::loadData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    private void handleDelete() {
        LokasiKantor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete location " + selected.getNama() + "?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    service.delete(selected.getId());
                    Platform.runLater(this::loadData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    @FXML
    private void handleActivate() {
        LokasiKantor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        new Thread(() -> {
            try {
                service.activate(selected.getId());
                Platform.runLater(this::loadData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private LokasiKantor showForm(LokasiKantor existing) {
        Dialog<LokasiKantor> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Tambah Lokasi" : "Edit Lokasi");
        
        // Custom button types with Indonesian labels
        ButtonType simpanButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        ButtonType batalButtonType = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(simpanButtonType, batalButtonType);

        TextField namaField = new TextField();
        TextField latField = new TextField();
        TextField longField = new TextField();
        TextField radiusField = new TextField();
        TextField alamatField = new TextField();
        radiusField.setText("50");
        
        // Create WebView for Leaflet map
        WebView webView = new WebView();
        webView.setPrefSize(600, 450);
        WebEngine webEngine = webView.getEngine();
        
        // Load map HTML
        URL mapUrl = getClass().getResource("/html/location-picker.html");
        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
        }
        
        // Bridge to capture coordinates from JavaScript
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", new JavaScriptBridge(latField, longField, alamatField));
                
                // Set initial coordinates if editing existing location
                if (existing != null && existing.getLatitude() != null && existing.getLongitude() != null) {
                    String script = String.format("setInitialCoordinates(%f, %f);", 
                                                  existing.getLatitude(), existing.getLongitude());
                    webEngine.executeScript(script);
                }

                // Force sync current coordinates to JavaFX fields
                webEngine.executeScript("if (typeof forceSyncCoordinates === 'function') { forceSyncCoordinates(); }");
            }
        });

        if (existing != null) {
            namaField.setText(existing.getNama());
            latField.setText(String.valueOf(existing.getLatitude()));
            longField.setText(String.valueOf(existing.getLongitude()));
            radiusField.setText(String.valueOf(existing.getRadiusValidasi()));
            alamatField.setText(existing.getAlamat());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nama:"), 0, 0); grid.add(namaField, 1, 0);
        grid.add(new Label("Latitude:"), 0, 1); grid.add(latField, 1, 1);
        grid.add(new Label("Longitude:"), 0, 2); grid.add(longField, 1, 2);
        grid.add(new Label("Radius (m):"), 0, 3); grid.add(radiusField, 1, 3);
        grid.add(new Label("Alamat:"), 0, 4); grid.add(alamatField, 1, 4);
        
        // Make lat/long fields read-only (updated by map)
        latField.setEditable(false);
        longField.setEditable(false);
        latField.setStyle("-fx-background-color: #f0f0f0;");
        longField.setStyle("-fx-background-color: #f0f0f0;");
        
        VBox container = new VBox(10, grid, webView);
        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().setPrefWidth(650);

        dialog.setResultConverter(btn -> {
            if (btn == simpanButtonType) {
                LokasiKantor loc = existing != null ? existing : new LokasiKantor();
                loc.setNama(namaField.getText());
                try {
                    loc.setLatitude(Double.parseDouble(latField.getText()));
                    loc.setLongitude(Double.parseDouble(longField.getText()));
                    loc.setRadiusValidasi(Integer.parseInt(radiusField.getText()));
                } catch (Exception e) { return null; }
                loc.setAlamat(alamatField.getText());
                return loc;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
    
    /**
     * JavaScript bridge to communicate between Leaflet map and JavaFX
     */
    public static class JavaScriptBridge {
        private final TextField latField;
        private final TextField longField;
        private final TextField alamatField;
        
        public JavaScriptBridge(TextField latField, TextField longField, TextField alamatField) {
            this.latField = latField;
            this.longField = longField;
            this.alamatField = alamatField;
        }
        
        public void updateCoordinates(double lat, double lng) {
            Platform.runLater(() -> {
                latField.setText(String.format("%.6f", lat));
                longField.setText(String.format("%.6f", lng));
            });
        }
        
        public void updateAddress(String address) {
            Platform.runLater(() -> {
                alamatField.setText(address);
            });
        }
    }
}
