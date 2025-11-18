# TASK DESKTOP-3: Real-time Sync, Export Reports, User Management & Settings

**Tanggal**: 18 November 2025  
**Tahap**: Desktop App - Tahap 3  
**Fokus**: WebSocket Real-time Updates, Export PDF/CSV, User CRUD, Settings Panel

---

## 📋 TUJUAN

Menambahkan fitur advanced untuk desktop app:
1. **Real-time Sync**: WebSocket untuk update presensi otomatis
2. **Export Reports**: Generate PDF dan CSV untuk laporan presensi
3. **User Management**: CRUD untuk kelola user (admin only)
4. **Settings Panel**: Konfigurasi app (server URL, auto-refresh interval, dll)

---

## 🏗️ ARSITEKTUR

### System Overview
```
┌─────────────────────────────────────────────────────────────┐
│                    Desktop Application                       │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Dashboard   │  │     User     │  │   Settings   │      │
│  │  Controller  │  │  Management  │  │    Panel     │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                 │              │
│  ┌──────┴──────────────────┴─────────────────┴───────┐     │
│  │              Service Layer                         │     │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────┐ │     │
│  │  │  WebSocket  │  │   Export    │  │   User   │ │     │
│  │  │   Service   │  │   Service   │  │ Service  │ │     │
│  │  └─────────────┘  └─────────────┘  └──────────┘ │     │
│  └───────────────────────────────────────────────────┘     │
└──────────────┬────────────────────────┬─────────────────────┘
               │                        │
               │ WebSocket              │ REST API
               │ (ws://...)             │ (http://...)
               │                        │
┌──────────────┴────────────────────────┴─────────────────────┐
│                    Backend Server                            │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │  WebSocket       │         │   REST API       │         │
│  │  /ws/presensi    │         │   /api/users     │         │
│  │                  │         │   /api/presensi  │         │
│  └──────────────────┘         └──────────────────┘         │
└──────────────────────────────────────────────────────────────┘
```

---

## 📦 FITUR 1: REAL-TIME SYNC (WebSocket)

### Mengapa WebSocket?

**Masalah dengan Polling**:
- Tahap 2 menggunakan polling (refresh setiap 30 detik)
- ❌ Network overhead (request setiap 30s meskipun tidak ada update)
- ❌ Delay up to 30 detik untuk melihat perubahan
- ❌ Boros bandwidth

**Solusi: WebSocket**:
- ✅ Real-time: Update langsung saat ada presensi baru
- ✅ Efficient: Connection persistent, hanya kirim data saat ada update
- ✅ Bidirectional: Server bisa push data ke client

---

### WebSocket Flow

```
App Start
    ↓
Login Success
    ↓
Dashboard Loaded
    ↓
┌───────────────────────┐
│ WebSocketService.     │
│ connect()             │
└───────────┬───────────┘
            │
            ↓
  ws://localhost:8080/ws/presensi
            ↓
      [Connected]
            │
            ├─→ Server Push: {"type": "PRESENSI_CREATED", "data": {...}}
            │       ↓
            │   Handle Message
            │       ↓
            │   Update Dashboard UI (Observable)
            │
            ├─→ Server Push: {"type": "PRESENSI_UPDATED", "data": {...}}
            │       ↓
            │   Update Dashboard UI
            │
            └─→ Logout / Close
                    ↓
                disconnect()
```

---

### Implementation: WebSocketService.java

**Path**: `src/main/java/com/smk/presensi/desktop/service/WebSocketService.java`

**Dependencies** (tambah ke `pom.xml`):
```xml
<!-- WebSocket Client (JSR 356) -->
<dependency>
    <groupId>org.glassfish.tyrus.bundles</groupId>
    <artifactId>tyrus-standalone-client</artifactId>
    <version>2.1.4</version>
</dependency>
```

**Key Features**:
- Connect to WebSocket server
- Handle incoming messages (PRESENSI_CREATED, PRESENSI_UPDATED, STATS_UPDATE)
- Observable properties untuk UI update
- Auto-reconnect on disconnect
- Thread-safe message handling

**Code Structure**:
```java
@ClientEndpoint
public class WebSocketService {
    private Session session;
    private final String wsUrl;
    private final StringProperty connectionStatus;
    private final ObjectProperty<Presensi> latestPresensi;
    
    // Callbacks untuk UI
    private Consumer<Presensi> onPresensiCreated;
    private Consumer<Presensi> onPresensiUpdated;
    private Consumer<DashboardStats> onStatsUpdate;
    
    public void connect(String jwtToken) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        
        // Add Authorization header
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
            .configurator(new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(Map<String, List<String>> headers) {
                    headers.put("Authorization", List.of("Bearer " + jwtToken));
                }
            })
            .build();
        
        // Connect
        session = container.connectToServer(this, config, new URI(wsUrl));
    }
    
    @OnMessage
    public void onMessage(String message) {
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
        String type = json.get("type").getAsString();
        
        switch (type) {
            case "PRESENSI_CREATED":
                Presensi presensi = gson.fromJson(json.get("data"), Presensi.class);
                Platform.runLater(() -> {
                    if (onPresensiCreated != null) {
                        onPresensiCreated.accept(presensi);
                    }
                });
                break;
                
            case "PRESENSI_UPDATED":
                // Similar handling
                break;
                
            case "STATS_UPDATE":
                DashboardStats stats = gson.fromJson(json.get("data"), DashboardStats.class);
                Platform.runLater(() -> {
                    if (onStatsUpdate != null) {
                        onStatsUpdate.accept(stats);
                    }
                });
                break;
        }
    }
    
    @OnClose
    public void onClose(CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
        // Auto-reconnect logic
        scheduleReconnect();
    }
}
```

**Integration dengan Dashboard**:
```java
// DashboardController.java
private WebSocketService wsService;

@FXML
public void initialize() {
    wsService = new WebSocketService("ws://localhost:8080/ws/presensi");
    
    // Set callbacks
    wsService.setOnPresensiCreated(presensi -> {
        // Add to table
        presensiTable.getItems().add(0, presensi);
        // Update stats
        refreshStats();
    });
    
    wsService.setOnStatsUpdate(stats -> {
        updateStatsUI(stats);
    });
    
    // Connect
    String token = SessionManager.getInstance().getJwtToken();
    wsService.connect(token);
}

@FXML
public void handleLogout() {
    wsService.disconnect();
    // ... navigate to login
}
```

---

## 📦 FITUR 2: EXPORT REPORTS (PDF/CSV)

### Export Flow

```
User Click "Export" Button
    ↓
Show Dialog: Choose Format (PDF/CSV)
    ↓
┌─────────────────────┐
│  Select Date Range  │
│  [Start] - [End]    │
│  □ All Data         │
└─────────┬───────────┘
          │
          ↓
    ExportService
          │
          ├─→ PDF: iText library
          │     ↓
          │   Generate PDF with table + chart
          │     ↓
          │   Save: presensi_2025-11-18.pdf
          │
          └─→ CSV: Manual CSV generation
                ↓
              Generate CSV with headers
                ↓
              Save: presensi_2025-11-18.csv
```

---

### Implementation: ExportService.java

**Path**: `src/main/java/com/smk/presensi/desktop/service/ExportService.java`

**Dependencies** (tambah ke `pom.xml`):
```xml
<!-- PDF Generation (iText) -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>

<!-- CSV (Apache Commons CSV) -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.0</version>
</dependency>
```

**Key Methods**:
```java
public class ExportService {
    private final PresensiService presensiService;
    
    // Export ke PDF
    public File exportToPdf(LocalDate startDate, LocalDate endDate) throws Exception {
        // 1. Fetch data dari API
        List<Presensi> dataList = presensiService.getPresensiByDateRange(
            startDate, endDate
        );
        
        // 2. Create PDF document
        String filename = "presensi_" + LocalDate.now() + ".pdf";
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        
        document.open();
        
        // 3. Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Laporan Presensi", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        // 4. Add date range
        Paragraph dateRange = new Paragraph(
            "Periode: " + startDate + " s/d " + endDate
        );
        dateRange.setSpacingAfter(20);
        document.add(dateRange);
        
        // 5. Create table
        PdfPTable table = new PdfPTable(6); // 6 columns
        table.setWidthPercentage(100);
        
        // Header
        table.addCell("Tanggal");
        table.addCell("NIS");
        table.addCell("Nama");
        table.addCell("Status");
        table.addCell("Waktu Masuk");
        table.addCell("Waktu Keluar");
        
        // Data rows
        for (Presensi p : dataList) {
            table.addCell(p.getTanggal().toString());
            table.addCell(p.getSiswa().getNis());
            table.addCell(p.getSiswa().getNama());
            table.addCell(p.getStatus().toString());
            table.addCell(p.getWaktuMasuk() != null ? p.getWaktuMasuk().toString() : "-");
            table.addCell(p.getWaktuKeluar() != null ? p.getWaktuKeluar().toString() : "-");
        }
        
        document.add(table);
        
        // 6. Add summary
        Paragraph summary = new Paragraph("\n\nRingkasan:");
        document.add(summary);
        
        long hadir = dataList.stream().filter(p -> p.getStatus() == Status.HADIR).count();
        long sakit = dataList.stream().filter(p -> p.getStatus() == Status.SAKIT).count();
        long izin = dataList.stream().filter(p -> p.getStatus() == Status.IZIN).count();
        long alpha = dataList.stream().filter(p -> p.getStatus() == Status.ALPHA).count();
        
        document.add(new Paragraph("Total Hadir: " + hadir));
        document.add(new Paragraph("Total Sakit: " + sakit));
        document.add(new Paragraph("Total Izin: " + izin));
        document.add(new Paragraph("Total Alpha: " + alpha));
        
        document.close();
        
        return new File(filename);
    }
    
    // Export ke CSV
    public File exportToCsv(LocalDate startDate, LocalDate endDate) throws Exception {
        List<Presensi> dataList = presensiService.getPresensiByDateRange(
            startDate, endDate
        );
        
        String filename = "presensi_" + LocalDate.now() + ".csv";
        
        try (FileWriter writer = new FileWriter(filename);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT
                 .withHeader("Tanggal", "NIS", "Nama", "Kelas", "Status", 
                            "Waktu Masuk", "Waktu Keluar", "Keterangan"))) {
            
            for (Presensi p : dataList) {
                csv.printRecord(
                    p.getTanggal(),
                    p.getSiswa().getNis(),
                    p.getSiswa().getNama(),
                    p.getSiswa().getKelas(),
                    p.getStatus(),
                    p.getWaktuMasuk(),
                    p.getWaktuKeluar(),
                    p.getKeterangan()
                );
            }
        }
        
        return new File(filename);
    }
}
```

**UI Integration**:
```java
// DashboardController.java
@FXML
private void handleExport() {
    // Show dialog untuk pilih format
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Export Laporan");
    alert.setHeaderText("Pilih format export:");
    
    ButtonType pdfButton = new ButtonType("PDF");
    ButtonType csvButton = new ButtonType("CSV");
    ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    
    alert.getButtonTypes().setAll(pdfButton, csvButton, cancelButton);
    
    Optional<ButtonType> result = alert.showAndWait();
    
    if (result.isPresent()) {
        if (result.get() == pdfButton) {
            exportToPdf();
        } else if (result.get() == csvButton) {
            exportToCsv();
        }
    }
}

private void exportToPdf() {
    // Show date picker dialog
    DateRangeDialog dialog = new DateRangeDialog();
    Optional<DateRange> result = dialog.showAndWait();
    
    if (result.isPresent()) {
        DateRange range = result.get();
        
        Task<File> exportTask = new Task<>() {
            @Override
            protected File call() throws Exception {
                return exportService.exportToPdf(range.getStart(), range.getEnd());
            }
        };
        
        exportTask.setOnSucceeded(e -> {
            File file = exportTask.getValue();
            showSuccess("Export berhasil: " + file.getAbsolutePath());
            
            // Open file
            Desktop.getDesktop().open(file);
        });
        
        exportTask.setOnFailed(e -> {
            showError("Export gagal: " + exportTask.getException().getMessage());
        });
        
        new Thread(exportTask).start();
    }
}
```

---

## 📦 FITUR 3: USER MANAGEMENT (CRUD)

### User Management Flow

```
Admin Login
    ↓
Dashboard Menu: "User Management"
    ↓
┌─────────────────────────────────────┐
│     User Management Screen          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Search: [_________] 🔍    │   │
│  │                             │   │
│  │  [+ Add User]              │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────────┐│
│  │ ID │ Username │ Role │ Actions │ │
│  ├────┼──────────┼──────┼─────────┤ │
│  │ 1  │ admin    │ ADMIN│ Edit Del│ │
│  │ 2  │ tu001    │ TU   │ Edit Del│ │
│  │ 3  │ guru01   │ GURU │ Edit Del│ │
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
         │
         ├─→ Add User: Show form dialog
         │     ↓
         │   Input: username, password, nama, role
         │     ↓
         │   POST /api/users
         │     ↓
         │   Refresh table
         │
         ├─→ Edit User: Show form dialog (prefilled)
         │     ↓
         │   PUT /api/users/{id}
         │     ↓
         │   Refresh table
         │
         └─→ Delete User: Confirm dialog
               ↓
             DELETE /api/users/{id}
               ↓
             Refresh table
```

---

### Implementation: User Management

**1. UserService.java**

**Path**: `src/main/java/com/smk/presensi/desktop/service/UserService.java`

```java
public class UserService {
    private final ApiClient apiClient;
    
    public List<User> getAllUsers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiClient.getBaseUrl() + "/api/users"))
            .header("Authorization", "Bearer " + SessionManager.getInstance().getJwtToken())
            .GET()
            .build();
        
        HttpResponse<String> response = apiClient.getHttpClient().send(
            request, 
            HttpResponse.BodyHandlers.ofString()
        );
        
        if (response.statusCode() == 200) {
            Type listType = new TypeToken<List<User>>(){}.getType();
            return new Gson().fromJson(response.body(), listType);
        } else {
            throw new Exception("Failed to fetch users: " + response.statusCode());
        }
    }
    
    public User createUser(User user) throws Exception {
        String json = new Gson().toJson(user);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiClient.getBaseUrl() + "/api/users"))
            .header("Authorization", "Bearer " + SessionManager.getInstance().getJwtToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        
        HttpResponse<String> response = apiClient.getHttpClient().send(
            request, 
            HttpResponse.BodyHandlers.ofString()
        );
        
        if (response.statusCode() == 201) {
            return new Gson().fromJson(response.body(), User.class);
        } else {
            throw new Exception("Failed to create user: " + response.statusCode());
        }
    }
    
    public User updateUser(Long id, User user) throws Exception {
        String json = new Gson().toJson(user);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiClient.getBaseUrl() + "/api/users/" + id))
            .header("Authorization", "Bearer " + SessionManager.getInstance().getJwtToken())
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .build();
        
        HttpResponse<String> response = apiClient.getHttpClient().send(
            request, 
            HttpResponse.BodyHandlers.ofString()
        );
        
        if (response.statusCode() == 200) {
            return new Gson().fromJson(response.body(), User.class);
        } else {
            throw new Exception("Failed to update user: " + response.statusCode());
        }
    }
    
    public void deleteUser(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiClient.getBaseUrl() + "/api/users/" + id))
            .header("Authorization", "Bearer " + SessionManager.getInstance().getJwtToken())
            .DELETE()
            .build();
        
        HttpResponse<String> response = apiClient.getHttpClient().send(
            request, 
            HttpResponse.BodyHandlers.ofString()
        );
        
        if (response.statusCode() != 204) {
            throw new Exception("Failed to delete user: " + response.statusCode());
        }
    }
}
```

**2. UserManagementController.java**

**Path**: `src/main/java/com/smk/presensi/desktop/controller/UserManagementController.java`

```java
public class UserManagementController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Long> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> namaColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private TextField searchField;
    
    private final UserService userService = new UserService(ApiClient.getInstance());
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        
        // Search listener
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filterUsers(newVal);
        });
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Actions column (Edit + Delete buttons)
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        userTable.setItems(userList);
    }
    
    private void loadUsers() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return userService.getAllUsers();
            }
        };
        
        task.setOnSucceeded(e -> {
            userList.setAll(task.getValue());
        });
        
        task.setOnFailed(e -> {
            showError("Gagal memuat data user: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleAddUser() {
        UserFormDialog dialog = new UserFormDialog(null);
        Optional<User> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            User user = result.get();
            
            Task<User> task = new Task<>() {
                @Override
                protected User call() throws Exception {
                    return userService.createUser(user);
                }
            };
            
            task.setOnSucceeded(e -> {
                showSuccess("User berhasil ditambahkan");
                loadUsers();
            });
            
            task.setOnFailed(e -> {
                showError("Gagal menambah user: " + task.getException().getMessage());
            });
            
            new Thread(task).start();
        }
    }
    
    private void handleEdit(User user) {
        UserFormDialog dialog = new UserFormDialog(user);
        Optional<User> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            User updatedUser = result.get();
            
            Task<User> task = new Task<>() {
                @Override
                protected User call() throws Exception {
                    return userService.updateUser(user.getId(), updatedUser);
                }
            };
            
            task.setOnSucceeded(e -> {
                showSuccess("User berhasil diupdate");
                loadUsers();
            });
            
            task.setOnFailed(e -> {
                showError("Gagal update user: " + task.getException().getMessage());
            });
            
            new Thread(task).start();
        }
    }
    
    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Delete");
        alert.setHeaderText("Hapus user " + user.getUsername() + "?");
        alert.setContentText("Action ini tidak bisa di-undo.");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.deleteUser(user.getId());
                    return null;
                }
            };
            
            task.setOnSucceeded(e -> {
                showSuccess("User berhasil dihapus");
                loadUsers();
            });
            
            task.setOnFailed(e -> {
                showError("Gagal hapus user: " + task.getException().getMessage());
            });
            
            new Thread(task).start();
        }
    }
    
    private void filterUsers(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            userTable.setItems(userList);
        } else {
            ObservableList<User> filtered = userList.filtered(user ->
                user.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                user.getNama().toLowerCase().contains(keyword.toLowerCase())
            );
            userTable.setItems(filtered);
        }
    }
}
```

**3. user-management.fxml**

**Path**: `src/main/resources/fxml/user-management.fxml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.smk.presensi.desktop.controller.UserManagementController"
            prefWidth="900" prefHeight="600">
    
    <!-- Top: Header + Search + Add Button -->
    <top>
        <VBox spacing="10" style="-fx-padding: 20;">
            <Label text="User Management" style="-fx-font-size: 24px; -fx-font-weight: bold;"/>
            
            <HBox spacing="10" alignment="CENTER_LEFT">
                <TextField fx:id="searchField" promptText="Search username or nama..." prefWidth="300"/>
                <Button text="+ Add User" onAction="#handleAddUser" 
                        style="-fx-background-color: #2196F3; -fx-text-fill: white;"/>
            </HBox>
        </VBox>
    </top>
    
    <!-- Center: Table -->
    <center>
        <TableView fx:id="userTable" style="-fx-padding: 20;">
            <columns>
                <TableColumn fx:id="idColumn" text="ID" prefWidth="50"/>
                <TableColumn fx:id="usernameColumn" text="Username" prefWidth="150"/>
                <TableColumn fx:id="namaColumn" text="Nama Lengkap" prefWidth="250"/>
                <TableColumn fx:id="roleColumn" text="Role" prefWidth="100"/>
                <TableColumn fx:id="actionsColumn" text="Actions" prefWidth="200"/>
            </columns>
        </TableView>
    </center>
</BorderPane>
```

---

## 📦 FITUR 4: SETTINGS PANEL

### Settings Flow

```
Dashboard Menu: "Settings"
    ↓
┌─────────────────────────────────────────────┐
│            Settings Panel                   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │  General Settings                   │   │
│  │                                     │   │
│  │  Server URL:                        │   │
│  │  [http://localhost:8080________]    │   │
│  │                                     │   │
│  │  Auto-Refresh Interval (seconds):  │   │
│  │  [30____] (10-300)                  │   │
│  │                                     │   │
│  │  ☑ Enable WebSocket                │   │
│  │  ☑ Auto-reconnect on disconnect    │   │
│  │  ☑ Show notifications               │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │  Export Settings                    │   │
│  │                                     │   │
│  │  Default export format:             │   │
│  │  ( ) PDF  (•) CSV                   │   │
│  │                                     │   │
│  │  Default export path:               │   │
│  │  [C:\Users\...\Documents] [Browse]  │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  [Save Settings] [Reset to Default]        │
└─────────────────────────────────────────────┘
```

---

### Implementation: Settings

**1. AppSettings.java (Model)**

**Path**: `src/main/java/com/smk/presensi/desktop/model/AppSettings.java`

```java
public class AppSettings {
    private String serverUrl;
    private int autoRefreshInterval; // seconds
    private boolean enableWebSocket;
    private boolean autoReconnect;
    private boolean showNotifications;
    private String defaultExportFormat; // "PDF" or "CSV"
    private String defaultExportPath;
    
    // Default values
    public static AppSettings getDefault() {
        AppSettings settings = new AppSettings();
        settings.serverUrl = "http://localhost:8080";
        settings.autoRefreshInterval = 30;
        settings.enableWebSocket = true;
        settings.autoReconnect = true;
        settings.showNotifications = true;
        settings.defaultExportFormat = "PDF";
        settings.defaultExportPath = System.getProperty("user.home") + "/Documents";
        return settings;
    }
    
    // Getters & Setters...
}
```

**2. SettingsManager.java**

**Path**: `src/main/java/com/smk/presensi/desktop/service/SettingsManager.java`

```java
public class SettingsManager {
    private static SettingsManager instance;
    private final Preferences prefs;
    private AppSettings settings;
    private final Gson gson;
    
    private SettingsManager() {
        prefs = Preferences.userRoot().node("com.smk.presensi.desktop.settings");
        gson = new Gson();
        loadSettings();
    }
    
    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }
    
    private void loadSettings() {
        String json = prefs.get("app_settings", null);
        
        if (json != null) {
            settings = gson.fromJson(json, AppSettings.class);
        } else {
            settings = AppSettings.getDefault();
        }
    }
    
    public void saveSettings(AppSettings settings) {
        this.settings = settings;
        String json = gson.toJson(settings);
        prefs.put("app_settings", json);
    }
    
    public AppSettings getSettings() {
        return settings;
    }
    
    public void resetToDefault() {
        settings = AppSettings.getDefault();
        saveSettings(settings);
    }
}
```

**3. SettingsController.java**

**Path**: `src/main/java/com/smk/presensi/desktop/controller/SettingsController.java`

```java
public class SettingsController {
    @FXML private TextField serverUrlField;
    @FXML private Spinner<Integer> refreshIntervalSpinner;
    @FXML private CheckBox enableWebSocketCheck;
    @FXML private CheckBox autoReconnectCheck;
    @FXML private CheckBox showNotificationsCheck;
    @FXML private RadioButton pdfRadio;
    @FXML private RadioButton csvRadio;
    @FXML private TextField exportPathField;
    @FXML private Button browseButton;
    
    private final SettingsManager settingsManager = SettingsManager.getInstance();
    private AppSettings settings;
    
    @FXML
    public void initialize() {
        // Load current settings
        settings = settingsManager.getSettings();
        
        // Populate UI
        serverUrlField.setText(settings.getServerUrl());
        
        refreshIntervalSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 300, 
                settings.getAutoRefreshInterval(), 10)
        );
        
        enableWebSocketCheck.setSelected(settings.isEnableWebSocket());
        autoReconnectCheck.setSelected(settings.isAutoReconnect());
        showNotificationsCheck.setSelected(settings.isShowNotifications());
        
        if (settings.getDefaultExportFormat().equals("PDF")) {
            pdfRadio.setSelected(true);
        } else {
            csvRadio.setSelected(true);
        }
        
        exportPathField.setText(settings.getDefaultExportPath());
    }
    
    @FXML
    private void handleBrowse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Export Directory");
        chooser.setInitialDirectory(new File(settings.getDefaultExportPath()));
        
        File selected = chooser.showDialog(browseButton.getScene().getWindow());
        if (selected != null) {
            exportPathField.setText(selected.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleSave() {
        // Update settings from UI
        settings.setServerUrl(serverUrlField.getText());
        settings.setAutoRefreshInterval(refreshIntervalSpinner.getValue());
        settings.setEnableWebSocket(enableWebSocketCheck.isSelected());
        settings.setAutoReconnect(autoReconnectCheck.isSelected());
        settings.setShowNotifications(showNotificationsCheck.isSelected());
        settings.setDefaultExportFormat(pdfRadio.isSelected() ? "PDF" : "CSV");
        settings.setDefaultExportPath(exportPathField.getText());
        
        // Save to preferences
        settingsManager.saveSettings(settings);
        
        showSuccess("Settings saved successfully!");
        
        // Apply changes (restart connections, timers, etc.)
        applySettings();
    }
    
    @FXML
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Settings");
        alert.setHeaderText("Reset to default settings?");
        alert.setContentText("All custom settings will be lost.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            settingsManager.resetToDefault();
            settings = settingsManager.getSettings();
            initialize(); // Reload UI
            showSuccess("Settings reset to default");
        }
    }
    
    private void applySettings() {
        // Notify other components about settings change
        // e.g., restart WebSocket, update refresh timer, etc.
        
        // Dispatch event atau update singleton services
    }
}
```

**4. settings.fxml**

**Path**: `src/main/resources/fxml/settings.fxml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.smk.presensi.desktop.controller.SettingsController"
            prefWidth="700" prefHeight="600">
    
    <top>
        <VBox spacing="10" style="-fx-padding: 20;">
            <Label text="Settings" style="-fx-font-size: 24px; -fx-font-weight: bold;"/>
        </VBox>
    </top>
    
    <center>
        <ScrollPane fitToWidth="true" style="-fx-padding: 20;">
            <VBox spacing="20">
                <!-- General Settings -->
                <VBox spacing="10" style="-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;">
                    <Label text="General Settings" style="-fx-font-size: 16px; -fx-font-weight: bold;"/>
                    
                    <HBox spacing="10" alignment="CENTER_LEFT">
                        <Label text="Server URL:" prefWidth="200"/>
                        <TextField fx:id="serverUrlField" prefWidth="350"/>
                    </HBox>
                    
                    <HBox spacing="10" alignment="CENTER_LEFT">
                        <Label text="Auto-Refresh Interval (seconds):" prefWidth="200"/>
                        <Spinner fx:id="refreshIntervalSpinner" prefWidth="100"/>
                    </HBox>
                    
                    <CheckBox fx:id="enableWebSocketCheck" text="Enable WebSocket"/>
                    <CheckBox fx:id="autoReconnectCheck" text="Auto-reconnect on disconnect"/>
                    <CheckBox fx:id="showNotificationsCheck" text="Show notifications"/>
                </VBox>
                
                <!-- Export Settings -->
                <VBox spacing="10" style="-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;">
                    <Label text="Export Settings" style="-fx-font-size: 16px; -fx-font-weight: bold;"/>
                    
                    <HBox spacing="10" alignment="CENTER_LEFT">
                        <Label text="Default export format:" prefWidth="200"/>
                        <RadioButton fx:id="pdfRadio" text="PDF">
                            <toggleGroup>
                                <ToggleGroup fx:id="exportFormatGroup"/>
                            </toggleGroup>
                        </RadioButton>
                        <RadioButton fx:id="csvRadio" text="CSV" toggleGroup="$exportFormatGroup"/>
                    </HBox>
                    
                    <HBox spacing="10" alignment="CENTER_LEFT">
                        <Label text="Default export path:" prefWidth="200"/>
                        <TextField fx:id="exportPathField" prefWidth="250"/>
                        <Button fx:id="browseButton" text="Browse..." onAction="#handleBrowse"/>
                    </HBox>
                </VBox>
            </VBox>
        </ScrollPane>
    </center>
    
    <bottom>
        <HBox spacing="10" alignment="CENTER_RIGHT" style="-fx-padding: 20;">
            <Button text="Save Settings" onAction="#handleSave" 
                    style="-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20;"/>
            <Button text="Reset to Default" onAction="#handleReset"
                    style="-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 20;"/>
        </HBox>
    </bottom>
</BorderPane>
```

---

## 🔧 INTEGRATION: Dashboard Menu

Update `dashboard.fxml` untuk menambahkan menu navigasi:

```xml
<MenuBar>
    <Menu text="File">
        <MenuItem text="Export PDF" onAction="#handleExportPdf"/>
        <MenuItem text="Export CSV" onAction="#handleExportCsv"/>
        <SeparatorMenuItem/>
        <MenuItem text="Logout" onAction="#handleLogout"/>
    </Menu>
    
    <Menu text="Manage">
        <MenuItem text="User Management" onAction="#handleUserManagement"/>
    </Menu>
    
    <Menu text="Settings">
        <MenuItem text="Preferences" onAction="#handleSettings"/>
    </Menu>
    
    <Menu text="Help">
        <MenuItem text="About" onAction="#handleAbout"/>
    </Menu>
</MenuBar>
```

**DashboardController.java** - Add navigation methods:
```java
@FXML
private void handleUserManagement() {
    // Check if user is ADMIN
    if (!SessionManager.getInstance().getCurrentUser().getRole().equals("ADMIN")) {
        showError("Access denied. Admin only.");
        return;
    }
    
    // Load user management screen
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-management.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("User Management");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        showError("Failed to load user management: " + e.getMessage());
    }
}

@FXML
private void handleSettings() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("Settings");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        showError("Failed to load settings: " + e.getMessage());
    }
}
```

---

## 📊 DEPENDENCY UPDATE

Update `pom.xml` dengan dependencies baru:

```xml
<!-- Existing dependencies... -->

<!-- WebSocket Client (JSR 356) -->
<dependency>
    <groupId>org.glassfish.tyrus.bundles</groupId>
    <artifactId>tyrus-standalone-client</artifactId>
    <version>2.1.4</version>
</dependency>

<!-- PDF Generation (iText) -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>

<!-- CSV (Apache Commons CSV) -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.0</version>
</dependency>
```

---

## 🧪 TESTING CHECKLIST

### WebSocket Testing
- [ ] Connect to WebSocket server dengan JWT token
- [ ] Receive PRESENSI_CREATED event → Dashboard table update
- [ ] Receive PRESENSI_UPDATED event → Dashboard table update
- [ ] Receive STATS_UPDATE event → Stats cards update
- [ ] Auto-reconnect saat connection lost
- [ ] Disconnect on logout

### Export Testing
- [ ] Export PDF dengan date range → File generated
- [ ] Export CSV dengan date range → File generated
- [ ] PDF content: table + summary correct
- [ ] CSV content: headers + data correct
- [ ] Open exported file otomatis

### User Management Testing
- [ ] List all users → Table populated
- [ ] Add new user → Success, table updated
- [ ] Edit user → Success, table updated
- [ ] Delete user → Confirm dialog, success, table updated
- [ ] Search users → Filter works
- [ ] Access control: Only ADMIN can access

### Settings Testing
- [ ] Load current settings → UI populated
- [ ] Save settings → Preferences persisted
- [ ] Reset to default → Settings reset
- [ ] Apply settings → Components updated (WebSocket, timer, etc.)

---

## 📝 FILE STRUCTURE (FINAL)

```
desktop-app/
├── pom.xml (UPDATED - new dependencies)
├── src/
│   └── main/
│       ├── java/com/smk/presensi/desktop/
│       │   ├── DesktopApp.java
│       │   ├── controller/
│       │   │   ├── DashboardController.java (UPDATED - WebSocket + Export)
│       │   │   ├── LoginController.java
│       │   │   ├── UserManagementController.java (NEW)
│       │   │   └── SettingsController.java (NEW)
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Presensi.java
│       │   │   ├── DashboardStats.java
│       │   │   └── AppSettings.java (NEW)
│       │   ├── service/
│       │   │   ├── ApiClient.java
│       │   │   ├── PresensiService.java
│       │   │   ├── SessionManager.java
│       │   │   ├── WebSocketService.java (NEW)
│       │   │   ├── ExportService.java (NEW)
│       │   │   ├── UserService.java (NEW)
│       │   │   └── SettingsManager.java (NEW)
│       │   └── viewmodel/
│       │       ├── DashboardViewModel.java
│       │       └── LoginViewModel.java
│       └── resources/
│           ├── fxml/
│           │   ├── dashboard.fxml (UPDATED - Menu)
│           │   ├── login.fxml
│           │   ├── user-management.fxml (NEW)
│           │   └── settings.fxml (NEW)
│           └── css/
│               ├── dashboard.css
│               └── login.css
```

---

## 🎯 NEXT STEPS (Tahap 4 - OPTIONAL)

1. **Notifications**: Desktop notifications untuk presensi baru
2. **Reports Dashboard**: Charts & analytics (PieChart, BarChart)
3. **Advanced Search**: Filter presensi by siswa, kelas, date range
4. **Offline Mode**: Cache data, sync on reconnect
5. **Multi-language**: i18n support (Indonesian/English)

---

## 📚 REFERENCES

- **JavaFX**: https://openjfx.io/
- **WebSocket (JSR 356)**: https://tyrus-project.github.io/
- **iText PDF**: https://itextpdf.com/
- **Apache Commons CSV**: https://commons.apache.org/proper/commons-csv/
- **Java Preferences API**: https://docs.oracle.com/javase/8/docs/api/java/util/prefs/Preferences.html

---

**END OF TASK-DESKTOP-3**
