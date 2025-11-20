package com.smk.presensi.desktop.controller;

import com.smk.presensi.desktop.model.Guru;
import com.smk.presensi.desktop.model.Kelas;
import com.smk.presensi.desktop.model.QuizQuestion;
import com.smk.presensi.desktop.model.QuizSession;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.GuruService;
import com.smk.presensi.desktop.service.KelasService;
import com.smk.presensi.desktop.service.QuizService;
import com.smk.presensi.desktop.util.InAppNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.stage.FileChooser;

import java.net.URL;
import java.time.LocalDate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class QuizManagementController implements Initializable {

    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private TextField mapelFilterField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button filterButton;
    @FXML private Button refreshButton;
    @FXML private Label infoLabel;

    @FXML private TableView<QuizSession> sessionTable;
    @FXML private TableColumn<QuizSession, Long> idColumn;
    @FXML private TableColumn<QuizSession, String> judulColumn;
    @FXML private TableColumn<QuizSession, String> guruColumn;
    @FXML private TableColumn<QuizSession, String> kelasColumn;
    @FXML private TableColumn<QuizSession, String> mapelColumn;
    @FXML private TableColumn<QuizSession, LocalDate> tanggalColumn;
    @FXML private TableColumn<QuizSession, String> statusColumn;
    @FXML private TableColumn<QuizSession, String> tokenColumn;

    @FXML private Button newSessionButton;
    @FXML private Button generateTokenButton;
    @FXML private Button viewTokenButton;

    @FXML private TextField detailJudulField;
    @FXML private TextField detailMapelField;
    @FXML private TextField detailTanggalField;
    @FXML private TextField detailKelasField;
    @FXML private TextField detailTokenField;
    @FXML private TextField detailQrField;

    @FXML private TableView<QuizQuestion> questionTable;
    @FXML private TableColumn<QuizQuestion, String> soalColumn;
    @FXML private TableColumn<QuizQuestion, String> kunciColumn;
    @FXML private TableColumn<QuizQuestion, Integer> bobotColumn;

    @FXML private TextArea soalArea;
    @FXML private TextField opsiAField;
    @FXML private TextField opsiBField;
    @FXML private TextField opsiCField;
    @FXML private TextField opsiDField;
    @FXML private ComboBox<String> jawabanCombo;
    @FXML private TextField bobotField;
    @FXML private Button addQuestionButton;
    @FXML private Button importQuestionsButton;
    @FXML private Button downloadTemplateButton;

    private QuizService quizService;
    private GuruService guruService;
    private KelasService kelasService;

    private final ObservableList<QuizSession> sessionList = FXCollections.observableArrayList();
    private final ObservableList<QuizQuestion> questionList = FXCollections.observableArrayList();

    private List<Guru> guruCache;
    private List<Kelas> kelasCache;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApiClient apiClient = ApiClient.getInstance();
        quizService = new QuizService(apiClient);
        guruService = new GuruService(apiClient);
        kelasService = new KelasService(apiClient);

        setupSessionTable();
        setupQuestionTable();
        setupControls();
        setupEventHandlers();

        loadReferenceData();
        loadSessions();
    }

    private void setupSessionTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        judulColumn.setCellValueFactory(new PropertyValueFactory<>("judul"));
        guruColumn.setCellValueFactory(new PropertyValueFactory<>("guruNama"));
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("kelasNama"));
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mapel"));
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        tokenColumn.setCellValueFactory(new PropertyValueFactory<>("token"));

        sessionTable.setItems(sessionList);

        sessionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            generateTokenButton.setDisable(!selected);
            viewTokenButton.setDisable(!selected || newVal.getToken() == null || newVal.getToken().isBlank());
            addQuestionButton.setDisable(!selected);
            importQuestionsButton.setDisable(!selected);
            loadSessionDetail(newVal);
        });
    }

    private void setupQuestionTable() {
        soalColumn.setCellValueFactory(new PropertyValueFactory<>("soal"));
        kunciColumn.setCellValueFactory(new PropertyValueFactory<>("jawabanBenar"));
        bobotColumn.setCellValueFactory(new PropertyValueFactory<>("bobot"));
        questionTable.setItems(questionList);
    }

    private void setupControls() {
        statusFilterCombo.setItems(FXCollections.observableArrayList("", "DRAFT", "ACTIVE", "CLOSED"));
        statusFilterCombo.setValue("");

        jawabanCombo.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));
        jawabanCombo.setValue("A");

        generateTokenButton.setDisable(true);
        viewTokenButton.setDisable(true);
        addQuestionButton.setDisable(true);
        importQuestionsButton.setDisable(true);
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadSessions());
        filterButton.setOnAction(e -> applyFilter());
        newSessionButton.setOnAction(e -> handleNewSession());
        generateTokenButton.setOnAction(e -> handleGenerateToken());
        viewTokenButton.setOnAction(e -> handleViewToken());
        addQuestionButton.setOnAction(e -> handleAddQuestion());
        importQuestionsButton.setOnAction(e -> handleImportQuestions());
        downloadTemplateButton.setOnAction(e -> handleDownloadTemplate());
    }

    private void loadReferenceData() {
        setLoading(true, "Memuat data guru & kelas...");
        new Thread(() -> {
            List<Guru> gurus = guruService.getAllGuru();
            List<Kelas> kelas = kelasService.getAllKelas();
            Platform.runLater(() -> {
                this.guruCache = gurus;
                this.kelasCache = kelas;
                setLoading(false, "Ready");
            });
        }).start();
    }

    private void loadSessions() {
        setLoading(true, "Memuat daftar sesi quiz...");
        new Thread(() -> {
            List<QuizSession> sessions = quizService.listSessions();
            Platform.runLater(() -> {
                sessionList.setAll(sessions);
                applyFilter();
                setLoading(false, "Ready");
            });
        }).start();
    }

    private void applyFilter() {
        String mapelFilter = mapelFilterField.getText() != null ? mapelFilterField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterCombo.getValue();

        List<QuizSession> filtered = sessionList.stream()
                .filter(s -> mapelFilter.isEmpty() ||
                        (s.getMapel() != null && s.getMapel().toLowerCase().contains(mapelFilter)))
                .filter(s -> statusFilter == null || statusFilter.isBlank() ||
                        (s.getStatus() != null && s.getStatus().equalsIgnoreCase(statusFilter)))
                .collect(Collectors.toList());

        sessionTable.setItems(FXCollections.observableArrayList(filtered));
        updateInfoLabel(filtered.size(), sessionList.size());
    }

    private void updateInfoLabel(int filtered, int total) {
        if (filtered == total) {
            infoLabel.setText("Total: " + total + " sesi");
        } else {
            infoLabel.setText("Total: " + filtered + " sesi (dari " + total + ")");
        }
    }

    private void loadSessionDetail(QuizSession session) {
        if (session == null) {
            detailJudulField.clear();
            detailMapelField.clear();
            detailTanggalField.clear();
            detailKelasField.clear();
            detailTokenField.clear();
            detailQrField.clear();
            questionList.clear();
            return;
        }

        detailJudulField.setText(session.getJudul());
        detailMapelField.setText(session.getMapel());
        detailTanggalField.setText(session.getTanggal() != null ? session.getTanggal().toString() : "");
        detailKelasField.setText(session.getKelasNama());
        detailTokenField.setText(session.getToken() != null ? session.getToken() : "");
        detailQrField.setText(session.getQrCodeUrl() != null ? session.getQrCodeUrl() : "");

        // Jika session di-list tanpa questions, ambil detail dari backend
        setLoading(true, "Memuat detail sesi...");
        new Thread(() -> {
            try {
                QuizSession full = quizService.getSession(session.getId());
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    if (full != null && full.getQuestions() != null) {
                        questionList.setAll(full.getQuestions());
                    } else {
                        questionList.clear();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    questionList.clear();
                });
            }
        }).start();
    }

    private void handleNewSession() {
        Dialog<QuizSession> dialog = new Dialog<>();
        dialog.setTitle("Sesi Quiz Baru");
        dialog.setHeaderText("Buat sesi quiz baru");

        ButtonType createButtonType = new ButtonType("Buat", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        ComboBox<Guru> guruCombo = new ComboBox<>();
        ComboBox<Kelas> kelasCombo = new ComboBox<>();
        TextField judulField = new TextField();
        TextField mapelField = new TextField();
        TextField materiField = new TextField();
        DatePicker tanggalPicker = new DatePicker(LocalDate.now());

        if (guruCache != null) {
            guruCombo.setItems(FXCollections.observableArrayList(guruCache));
        }
        if (kelasCache != null) {
            kelasCombo.setItems(FXCollections.observableArrayList(kelasCache));
        }
        guruCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Guru item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNama());
            }
        });
        guruCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Guru item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? guruCombo.getPromptText() : item.getNama());
            }
        });
        kelasCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Kelas item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNama());
            }
        });
        kelasCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Kelas item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? kelasCombo.getPromptText() : item.getNama());
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("Guru:"), 0, 0);
        grid.add(guruCombo, 1, 0);
        grid.add(new Label("Kelas:"), 0, 1);
        grid.add(kelasCombo, 1, 1);
        grid.add(new Label("Judul:"), 0, 2);
        grid.add(judulField, 1, 2);
        grid.add(new Label("Mapel:"), 0, 3);
        grid.add(mapelField, 1, 3);
        grid.add(new Label("Materi:"), 0, 4);
        grid.add(materiField, 1, 4);
        grid.add(new Label("Tanggal:"), 0, 5);
        grid.add(tanggalPicker, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == createButtonType) {
                Guru guru = guruCombo.getValue();
                Kelas kelas = kelasCombo.getValue();
                if (guru == null || kelas == null ||
                        judulField.getText().trim().isEmpty() ||
                        mapelField.getText().trim().isEmpty()) {
                    return null;
                }
                QuizSession session = new QuizSession();
                session.setGuruId(guru.getId());
                session.setGuruNama(guru.getNama());
                session.setKelasId(kelas.getId());
                session.setKelasNama(kelas.getNama());
                session.setJudul(judulField.getText().trim());
                session.setMapel(mapelField.getText().trim());
                session.setMateri(materiField.getText().trim());
                session.setTanggal(tanggalPicker.getValue());
                return session;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(session -> {
            setLoading(true, "Menyimpan sesi quiz...");
            new Thread(() -> {
                try {
                    QuizSession created = quizService.createSession(session);
                    Platform.runLater(() -> {
                        setLoading(false, "Ready");
                        if (created != null) {
                            sessionList.add(0, created);
                            applyFilter();
                            sessionTable.getSelectionModel().select(created);
                            InAppNotification.show("Sesi quiz berhasil dibuat",
                                    sessionTable.getParent(),
                                    InAppNotification.NotificationType.SUCCESS,
                                    3);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        setLoading(false, "Ready");
                        InAppNotification.show("Gagal membuat sesi: " + e.getMessage(),
                                sessionTable.getParent(),
                                InAppNotification.NotificationType.ERROR,
                                5);
                    });
                }
            }).start();
        });
    }

    private void handleGenerateToken() {
        QuizSession selected = sessionTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        setLoading(true, "Generate token untuk sesi...");
        new Thread(() -> {
            try {
                QuizSession updated = quizService.generateToken(selected.getId());
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    if (updated != null) {
                        int index = sessionList.indexOf(selected);
                        if (index >= 0) {
                            sessionList.set(index, updated);
                            applyFilter();
                        }
                        sessionTable.getSelectionModel().select(updated);
                        InAppNotification.show("Token berhasil dibuat: " + updated.getToken(),
                                sessionTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                4);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    InAppNotification.show("Gagal generate token: " + e.getMessage(),
                            sessionTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private void handleViewToken() {
        QuizSession selected = sessionTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getToken() == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Token Quiz");
        alert.setHeaderText("Token Quiz: " + selected.getToken());
        StringBuilder sb = new StringBuilder();
        sb.append("Judul: ").append(selected.getJudul()).append("\n");
        sb.append("Mapel: ").append(selected.getMapel()).append("\n");
        sb.append("Kelas: ").append(selected.getKelasNama()).append("\n");
        if (selected.getQrCodeUrl() != null) {
            sb.append("\nQR URL:\n").append(selected.getQrCodeUrl()).append("\n");
        }
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void handleAddQuestion() {
        QuizSession session = sessionTable.getSelectionModel().getSelectedItem();
        if (session == null) {
            InAppNotification.show("Pilih sesi dulu sebelum menambah soal",
                    questionTable.getParent(),
                    InAppNotification.NotificationType.ERROR,
                    4);
            return;
        }

        String soal = soalArea.getText() != null ? soalArea.getText().trim() : "";
        String a = opsiAField.getText() != null ? opsiAField.getText().trim() : "";
        String b = opsiBField.getText() != null ? opsiBField.getText().trim() : "";
        String c = opsiCField.getText() != null ? opsiCField.getText().trim() : "";
        String d = opsiDField.getText() != null ? opsiDField.getText().trim() : "";
        String kunci = jawabanCombo.getValue();
        Integer bobot = 1;
        if (bobotField.getText() != null && !bobotField.getText().trim().isEmpty()) {
            try {
                bobot = Integer.parseInt(bobotField.getText().trim());
            } catch (NumberFormatException ignored) {
            }
        }

        if (soal.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            InAppNotification.show("Soal dan semua opsi (A-D) wajib diisi",
                    questionTable.getParent(),
                    InAppNotification.NotificationType.ERROR,
                    4);
            return;
        }

        QuizQuestion question = new QuizQuestion();
        question.setSoal(soal);
        question.setOpsiA(a);
        question.setOpsiB(b);
        question.setOpsiC(c);
        question.setOpsiD(d);
        question.setJawabanBenar(kunci);
        question.setBobot(bobot);

        setLoading(true, "Menyimpan soal baru...");
        new Thread(() -> {
            try {
                QuizQuestion created = quizService.addQuestion(session.getId(), question);
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    if (created != null) {
                        questionList.add(created);
                        soalArea.clear();
                        opsiAField.clear();
                        opsiBField.clear();
                        opsiCField.clear();
                        opsiDField.clear();
                        bobotField.clear();
                        jawabanCombo.setValue("A");
                        InAppNotification.show("Soal berhasil ditambahkan",
                                questionTable.getParent(),
                                InAppNotification.NotificationType.SUCCESS,
                                3);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    InAppNotification.show("Gagal menambah soal: " + e.getMessage(),
                            questionTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private void handleImportQuestions() {
        QuizSession session = sessionTable.getSelectionModel().getSelectedItem();
        if (session == null) {
            InAppNotification.show("Pilih sesi dulu sebelum import soal",
                    questionTable.getParent(),
                    InAppNotification.NotificationType.ERROR,
                    4);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih file Excel (xlsx) bank soal");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(questionTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        setLoading(true, "Import soal dari Excel...");
        new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(file);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
                if (sheet == null) {
                    throw new RuntimeException("Sheet kosong");
                }

                int imported = 0;
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // baris 0 = header
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    String soal = getStringCell(row, 0);
                    String opsiA = getStringCell(row, 1);
                    String opsiB = getStringCell(row, 2);
                    String opsiC = getStringCell(row, 3);
                    String opsiD = getStringCell(row, 4);
                    String jawaban = getStringCell(row, 5);
                    Integer bobot = parseIntegerSafe(getStringCell(row, 6));

                    if (soal == null || soal.isBlank()) {
                        continue; // lewati baris kosong
                    }

                    QuizQuestion q = new QuizQuestion();
                    q.setSoal(soal);
                    q.setOpsiA(opsiA != null ? opsiA : "");
                    q.setOpsiB(opsiB != null ? opsiB : "");
                    q.setOpsiC(opsiC != null ? opsiC : "");
                    q.setOpsiD(opsiD != null ? opsiD : "");
                    q.setJawabanBenar(jawaban != null && !jawaban.isBlank() ? jawaban : "A");
                    q.setBobot(bobot != null ? bobot : 1);

                    try {
                        QuizQuestion created = quizService.addQuestion(session.getId(), q);
                        if (created != null) {
                            QuizQuestion finalCreated = created;
                            Platform.runLater(() -> questionList.add(finalCreated));
                            imported++;
                        }
                    } catch (Exception ex) {
                        // Abaikan baris yang gagal agar baris lainnya tetap diimport
                        ex.printStackTrace();
                    }
                }

                int finalImported = imported;
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    InAppNotification.show("Import soal selesai: " + finalImported + " soal",
                            questionTable.getParent(),
                            InAppNotification.NotificationType.SUCCESS,
                            4);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false, "Ready");
                    InAppNotification.show("Gagal import soal: " + e.getMessage(),
                            questionTable.getParent(),
                            InAppNotification.NotificationType.ERROR,
                            5);
                });
            }
        }).start();
    }

    private void handleDownloadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan template bank soal");
        fileChooser.setInitialFileName("template_quiz.xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(questionTable.getScene().getWindow());
        if (file == null) return;

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Soal");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Soal");
            header.createCell(1).setCellValue("Opsi A");
            header.createCell(2).setCellValue("Opsi B");
            header.createCell(3).setCellValue("Opsi C");
            header.createCell(4).setCellValue("Opsi D");
            header.createCell(5).setCellValue("Jawaban Benar (A/B/C/D)");
            header.createCell(6).setCellValue("Bobot (angka, opsional)");

            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Contoh soal: Apa ibu kota Indonesia?");
            sample.createCell(1).setCellValue("Jakarta");
            sample.createCell(2).setCellValue("Bandung");
            sample.createCell(3).setCellValue("Surabaya");
            sample.createCell(4).setCellValue("Medan");
            sample.createCell(5).setCellValue("A");
            sample.createCell(6).setCellValue(1);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            InAppNotification.show("Template disimpan: " + file.getName(),
                    questionTable.getParent(),
                    InAppNotification.NotificationType.SUCCESS,
                    3);
        } catch (Exception e) {
            e.printStackTrace();
            InAppNotification.show("Gagal menyimpan template: " + e.getMessage(),
                    questionTable.getParent(),
                    InAppNotification.NotificationType.ERROR,
                    5);
        }
    }

    private String getStringCell(Row row, int idx) {
        if (row.getCell(idx) == null) return "";
        return row.getCell(idx).toString().trim();
    }

    private Integer parseIntegerSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim().replace(".0", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setLoading(boolean loading, String message) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(loading);
            statusLabel.setText(message);
        });
    }
}
