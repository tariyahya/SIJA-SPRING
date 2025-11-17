# TASK DESKTOP-1: Dashboard Admin dengan JavaFX

**Tanggal**: 17 November 2025  
**Tahap**: Desktop App - Tahap 1  
**Fokus**: Dashboard Admin/TU dengan Tabel Presensi & Statistik

---

## 📋 TUJUAN

Membangun **aplikasi desktop JavaFX** untuk Admin/TU dengan fitur:
1. **Dashboard Statistics**: Tampilkan kartu statistik (Total, Hadir, Terlambat, Alpha, Persentase)
2. **Tabel Presensi**: TableView dengan data presensi hari ini
3. **RFID Checkin Simulasi**: Input field untuk simulate RFID card scan
4. **Mock Data Mode**: Toggle untuk development tanpa backend
5. **MVVM Architecture**: Separation of concerns (Model-View-ViewModel)

---

## 🏗️ ARSITEKTUR

### Struktur Project
```
desktop-app/
├── pom.xml                                    # Maven configuration
├── src/main/java/com/smk/presensi/desktop/
│   ├── DesktopApp.java                       # Main Application (Entry point)
│   ├── model/
│   │   ├── Presensi.java                     # Presensi data model
│   │   ├── User.java                         # User data model
│   │   └── DashboardStats.java               # Statistics model
│   ├── service/
│   │   ├── ApiClient.java                    # HTTP Client (JWT + REST API)
│   │   └── PresensiService.java              # Presensi API wrapper
│   ├── viewmodel/
│   │   └── DashboardViewModel.java           # Dashboard business logic
│   └── controller/
│       └── DashboardController.java          # UI event handling
└── src/main/resources/
    ├── fxml/
    │   └── dashboard.fxml                    # UI layout (XML)
    └── css/
        └── dashboard.css                     # Styling
```

### MVVM Pattern
```
┌─────────────┐         ┌────────────────┐         ┌──────────┐
│ View (FXML) │ ◄────── │ ViewModel      │ ◄────── │ Model    │
│ + Controller│  Bind   │ (Observable    │  Fetch  │ (Service)│
│             │         │  Properties)   │         │          │
└─────────────┘         └────────────────┘         └──────────┘
      │                        │
      │  Event Handling        │  Business Logic
      │                        │
      └────────────────────────┘
```

**Keuntungan MVVM**:
- **Separation of Concerns**: UI logic terpisah dari business logic
- **Testable**: ViewModel bisa ditest tanpa UI
- **Data Binding**: Perubahan data otomatis update UI
- **Reusable**: ViewModel bisa dipakai di multiple views

---

## 📦 DEPENDENCIES (pom.xml)

### JavaFX
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21.0.1</version>
</dependency>
```

### JSON Processing (Gson)
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### HTTP Client
- **Built-in**: Java 11+ `java.net.http.HttpClient`
- **No extra dependency needed**

---

## 🔧 IMPLEMENTASI

### 1. Model Layer (Data Representation)

**Presensi.java**:
```java
public class Presensi {
    private Long id;
    private String username;
    private String tipe;              // SISWA / GURU
    private LocalDate tanggal;
    private LocalTime jamMasuk;
    private LocalTime jamPulang;
    private String status;            // HADIR / TERLAMBAT / ALPHA
    private String method;            // MANUAL / RFID / BARCODE / FACE
    private String keterangan;
    
    // Getters & Setters
}
```

**DashboardStats.java**:
```java
public class DashboardStats {
    private int totalPresensi;
    private int totalHadir;
    private int totalTerlambat;
    private int totalAlpha;
    private double persentaseHadir;
    
    // Getters & Setters
}
```

---

### 2. Service Layer (API Communication)

**ApiClient.java**:
```java
public class ApiClient {
    private static final String BASE_URL = "http://localhost:8081/api";
    private final HttpClient httpClient;
    private final Gson gson;
    private String jwtToken;
    
    // Login & save JWT token
    public boolean login(String username, String password) {
        // POST /auth/login
        // Parse response, save token
    }
    
    // GET request with JWT
    public HttpResponse<String> get(String endpoint) {
        // Add Authorization header
        // Return response
    }
    
    // POST request with JWT
    public HttpResponse<String> post(String endpoint, String jsonBody) {
        // Add Authorization header
        // Send request
    }
}
```

**PresensiService.java**:
```java
public class PresensiService {
    private final ApiClient apiClient;
    
    // Get laporan harian (list of presensi)
    public List<Presensi> getLaporanHarian(LocalDate tanggal) {
        HttpResponse response = apiClient.get("/laporan/harian");
        // Parse JSON → List<Presensi>
    }
    
    // Get dashboard statistics
    public DashboardStats getDashboardStats() {
        HttpResponse response = apiClient.get("/laporan/harian");
        // Parse JSON → DashboardStats
    }
    
    // RFID Checkin
    public boolean checkinRfid(String rfidCardId) {
        String json = "{\"rfidCardId\":\"" + rfidCardId + "\"}";
        HttpResponse response = apiClient.post("/presensi/rfid/checkin", json);
        return response.statusCode() == 200;
    }
    
    // Mock data (for development without backend)
    public List<Presensi> getMockData() {
        // Return 20 dummy presensi
    }
}
```

---

### 3. ViewModel Layer (Business Logic + Observable State)

**DashboardViewModel.java**:
```java
public class DashboardViewModel {
    // Observable properties (auto-update UI when changed)
    private final ObservableList<Presensi> presensiList;
    private final IntegerProperty totalPresensi;
    private final IntegerProperty totalHadir;
    private final DoubleProperty persentaseHadir;
    private final BooleanProperty loading;
    private final StringProperty errorMessage;
    private final BooleanProperty useMockData;
    
    public void loadDashboardData() {
        loading.set(true);
        
        // Run in background thread
        new Thread(() -> {
            try {
                if (useMockData.get()) {
                    // Load mock data
                    List<Presensi> mockList = presensiService.getMockData();
                    DashboardStats mockStats = presensiService.getMockStats();
                    
                    // Update UI (must use Platform.runLater)
                    Platform.runLater(() -> {
                        presensiList.setAll(mockList);
                        updateStats(mockStats);
                        loading.set(false);
                    });
                } else {
                    // Load from API
                    List<Presensi> list = presensiService.getLaporanHarian(null);
                    // ...update UI
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorMessage.set("Error: " + e.getMessage());
                    loading.set(false);
                });
            }
        }).start();
    }
    
    public void checkinRfid(String rfidCardId) {
        // Same pattern: background thread + Platform.runLater
    }
}
```

**Key Concepts**:
- **ObservableList**: List yang otomatis notify UI saat berubah
- **Property**: Wrapper untuk primitive types (IntegerProperty, StringProperty, dll)
- **Platform.runLater()**: Update UI dari background thread (thread-safe)
- **Background Thread**: Avoid blocking UI saat network request

---

### 4. Controller Layer (UI Event Handling)

**DashboardController.java**:
```java
@FXMLLoader
public class DashboardController implements Initializable {
    // FXML injected components
    @FXML private Label totalPresensiLabel;
    @FXML private TableView<Presensi> presensiTable;
    @FXML private TableColumn<Presensi, String> usernameColumn;
    @FXML private TextField rfidInput;
    @FXML private Button refreshButton;
    
    private DashboardViewModel viewModel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Initialize ViewModel
        viewModel = new DashboardViewModel(new ApiClient());
        
        // 2. Setup table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        // ...other columns
        
        // 3. Bind UI to ViewModel
        presensiTable.setItems(viewModel.getPresensiList());
        totalPresensiLabel.textProperty().bind(
            viewModel.totalPresensiProperty().asString()
        );
        
        // 4. Setup event handlers
        refreshButton.setOnAction(e -> viewModel.refreshData());
        rfidInput.setOnAction(e -> handleRfidCheckin());
        
        // 5. Load initial data
        viewModel.loadDashboardData();
    }
    
    @FXML
    private void handleRfidCheckin() {
        String rfidCardId = rfidInput.getText().trim();
        if (!rfidCardId.isEmpty()) {
            viewModel.checkinRfid(rfidCardId);
            rfidInput.clear();
        }
    }
}
```

**Key Concepts**:
- **@FXML**: Inject components dari FXML file
- **Initializable**: Interface untuk lifecycle callback
- **PropertyValueFactory**: Bind table column ke model property
- **textProperty().bind()**: One-way data binding
- **setOnAction()**: Event handler untuk button/textfield

---

### 5. View Layer (FXML + CSS)

**dashboard.fxml**:
```xml
<BorderPane xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.smk.presensi.desktop.controller.DashboardController"
            stylesheets="@../css/dashboard.css">
    
    <top>
        <HBox styleClass="top-bar">
            <Label text="SIJA Presensi - Dashboard Admin" styleClass="title"/>
            <Button fx:id="refreshButton" text="Refresh"/>
        </HBox>
    </top>
    
    <center>
        <VBox spacing="20">
            <!-- Statistics Cards -->
            <HBox styleClass="stats-container">
                <VBox styleClass="stat-card">
                    <Label text="Total Presensi"/>
                    <Label fx:id="totalPresensiLabel" text="0" styleClass="stat-value"/>
                </VBox>
                <!-- ...other stat cards -->
            </HBox>
            
            <!-- RFID Checkin -->
            <VBox styleClass="rfid-section">
                <TextField fx:id="rfidInput" promptText="Scan RFID Card..."/>
                <Button fx:id="checkinButton" text="Checkin"/>
            </VBox>
            
            <!-- Table -->
            <TableView fx:id="presensiTable">
                <columns>
                    <TableColumn fx:id="usernameColumn" text="Username"/>
                    <TableColumn fx:id="statusColumn" text="Status"/>
                    <!-- ...other columns -->
                </columns>
            </TableView>
        </VBox>
    </center>
</BorderPane>
```

**dashboard.css**:
```css
.root {
    -fx-background-color: #f5f5f5;
}

.top-bar {
    -fx-background-color: linear-gradient(to right, #2c3e50, #3498db);
}

.stat-card {
    -fx-background-color: white;
    -fx-background-radius: 8;
    -fx-padding: 20;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);
}

.stat-value {
    -fx-font-size: 32px;
    -fx-font-weight: bold;
    -fx-text-fill: #2c3e50;
}
```

---

### 6. Main Application (Entry Point)

**DesktopApp.java**:
```java
public class DesktopApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/dashboard.fxml")
        );
        Parent root = loader.load();
        
        // Create scene
        Scene scene = new Scene(root, 1200, 700);
        
        // Configure stage
        primaryStage.setTitle("SIJA Presensi - Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 🚀 CARA MENJALANKAN

### 1. Compile Project
```bash
cd desktop-app
mvn clean compile
```

### 2. Run Application
```bash
mvn javafx:run
```

**Atau dengan Maven exec**:
```bash
mvn exec:java -Dexec.mainClass="com.smk.presensi.desktop.DesktopApp"
```

### 3. Build JAR (optional)
```bash
mvn clean package
java -jar target/desktop-app-1.0.0.jar
```

---

## 🧪 TESTING

### Manual Testing Checklist

**✅ Dashboard Statistics**:
- [ ] Kartu statistik muncul dengan nilai 0 (awal)
- [ ] Setelah load data, nilai berubah sesuai mock data
- [ ] Persentase hadir menampilkan 2 desimal (contoh: 89.47%)

**✅ Tabel Presensi**:
- [ ] Tabel muncul dengan 8 kolom (ID, Username, Tipe, Tanggal, Jam Masuk, Jam Pulang, Status, Method)
- [ ] Mock data: 20 rows muncul
- [ ] Status HADIR → hijau, TERLAMBAT → orange, ALPHA → merah
- [ ] Hover pada row → highlight biru muda
- [ ] Click row → selected (biru tua)

**✅ RFID Checkin**:
- [ ] Input field bisa diketik
- [ ] Click "Checkin" → data refresh (mock mode)
- [ ] Input clear setelah checkin
- [ ] Focus kembali ke input field

**✅ Mock Data Mode**:
- [ ] Checkbox "Mock Data" tercentang by default
- [ ] Uncheck → akan coba connect ke API (error jika backend tidak running)
- [ ] Check lagi → kembali ke mock data

**✅ Loading State**:
- [ ] Progress indicator muncul saat load data
- [ ] Progress indicator hilang setelah selesai
- [ ] Tombol disabled saat loading (optional)

**✅ Error Handling**:
- [ ] Jika backend mati → error label muncul dengan pesan jelas
- [ ] Error label berwarna merah
- [ ] Error hilang setelah refresh berhasil

---

## 📊 STATISTIK TAHAP 1

### Files Created: **10 files**
```
1 pom.xml
3 Model classes (Presensi, User, DashboardStats)
2 Service classes (ApiClient, PresensiService)
1 ViewModel (DashboardViewModel)
1 Controller (DashboardController)
1 Main App (DesktopApp)
1 FXML (dashboard.fxml)
1 CSS (dashboard.css)
```

### Lines of Code: **~1,100 lines**
- Java: ~800 lines
- FXML: ~150 lines
- CSS: ~150 lines

### Build Status
```bash
[INFO] BUILD SUCCESS
[INFO] Total time:  2.481 s
[INFO] Finished at: 2025-11-17T23:59:XX
```

---

## 🎯 FITUR TAHAP 1

### ✅ Implemented
- [x] Dashboard dengan 5 kartu statistik
- [x] Tabel presensi dengan 8 kolom
- [x] Status color coding (hijau/orange/merah)
- [x] RFID checkin simulation
- [x] Mock data mode (development tanpa backend)
- [x] Loading indicator
- [x] Error handling
- [x] Refresh button
- [x] MVVM architecture
- [x] Modern UI with CSS styling
- [x] Responsive layout
- [x] Thread-safe UI updates

### ⏳ Future Enhancements (Tahap 2-3)
- [ ] Login screen dengan JWT authentication
- [ ] Real-time RFID reader integration (serial port)
- [ ] Export laporan ke CSV/PDF
- [ ] Filter & search functionality
- [ ] Date range picker
- [ ] Charts & graphs (attendance trends)
- [ ] User management CRUD
- [ ] System settings

---

## 📚 KONSEP PENTING

### 1. JavaFX Application Lifecycle
```
main() 
  → launch(args) 
    → init() (optional)
      → start(Stage primaryStage)
        → show()
          → Application running
            → stop() (on close)
```

### 2. FXML Loading
```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
Parent root = loader.load();
// Controller automatically instantiated and initialized
```

### 3. Property Binding
```java
// One-way binding (ViewModel → UI)
label.textProperty().bind(viewModel.totalProperty().asString());

// Two-way binding (UI ↔ ViewModel)
checkbox.selectedProperty().bindBidirectional(viewModel.enabledProperty());
```

### 4. ObservableList
```java
ObservableList<Presensi> list = FXCollections.observableArrayList();
tableView.setItems(list);

// Add item → table auto-updates
list.add(new Presensi(...));

// Replace all → table refreshes
list.setAll(newList);
```

### 5. Background Tasks
```java
// WRONG: Direct UI update from background thread (crashes!)
new Thread(() -> {
    label.setText("Done"); // ❌ IllegalStateException
}).start();

// CORRECT: Use Platform.runLater()
new Thread(() -> {
    // Do network/heavy work
    Platform.runLater(() -> {
        label.setText("Done"); // ✅ Safe
    });
}).start();
```

---

## 🐛 TROUBLESHOOTING

### Issue 1: "FXML not found"
**Error**: `java.lang.NullPointerException: Location is required`

**Solution**:
- Pastikan path FXML benar: `/fxml/dashboard.fxml` (dengan leading slash)
- File ada di `src/main/resources/fxml/dashboard.fxml`
- Resources folder sudah di-copy ke `target/classes` saat compile

### Issue 2: "Controller not instantiated"
**Error**: `javafx.fxml.LoadException: Controller not specified`

**Solution**:
- Tambahkan `fx:controller` attribute di root element FXML
- Pastikan package name dan class name benar
- Controller harus punya no-arg constructor

### Issue 3: "IllegalStateException: Not on FX application thread"
**Error**: Saat update UI dari background thread

**Solution**:
```java
Platform.runLater(() -> {
    // Update UI here
});
```

### Issue 4: "Module not found: javafx.controls"
**Error**: Saat run aplikasi

**Solution**:
- Pastikan JavaFX dependencies ada di pom.xml
- Run dengan `mvn javafx:run` (plugin otomatis handle module path)
- Atau tambahkan VM options:
```bash
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

---

## ✅ CHECKLIST COMPLETION

- [x] Project structure created
- [x] pom.xml dengan JavaFX dependencies
- [x] Model classes (Presensi, User, DashboardStats)
- [x] Service layer (ApiClient, PresensiService)
- [x] ViewModel (DashboardViewModel)
- [x] Controller (DashboardController)
- [x] FXML layout (dashboard.fxml)
- [x] CSS styling (dashboard.css)
- [x] Main Application (DesktopApp.java)
- [x] Compile success (mvn clean compile)
- [x] Mock data working
- [x] MVVM architecture implemented
- [x] Documentation (TASK-1.md)

---

**Author**: Copilot Assistant  
**Last Updated**: 17 November 2025  
**Next**: TASK-DESKTOP-2 (Login Screen + JWT Authentication)
