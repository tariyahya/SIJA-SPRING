# Blog Desktop-1: Membangun Aplikasi Desktop dengan JavaFX & MVVM

**Tanggal**: 17 November 2025  
**Penulis**: Tim Pengembang SIJA  
**Tahap**: Desktop App - Tahap 1  
**Topik**: JavaFX, MVVM Architecture, Desktop Development

---

## 🎯 PENDAHULUAN

Setelah membangun backend REST API dengan Spring Boot, sekarang kita akan membuat **aplikasi desktop** untuk admin/TU menggunakan **JavaFX**. Desktop app ini akan:

- Menampilkan dashboard statistik presensi
- Menampilkan tabel presensi real-time
- Melakukan checkin RFID (simulasi)
- Berkomunikasi dengan backend via HTTP + JWT

**Mengapa Desktop App?**
- **Performance**: Lebih cepat dari web browser
- **Native UI**: Integrasi dengan OS (Windows/Mac/Linux)
- **Offline Capable**: Bisa cache data saat internet putus
- **Hardware Access**: Bisa akses RFID reader, barcode scanner, printer
- **Professional**: Cocok untuk aplikasi internal perusahaan/sekolah

---

## 📚 APA ITU JAVAFX?

**JavaFX** adalah framework untuk membuat aplikasi desktop dengan UI modern menggunakan Java.

### Sejarah Singkat
- **2008**: Oracle rilis JavaFX (pengganti Swing)
- **2018**: Pindah ke open-source (OpenJFX)
- **2025**: JavaFX 21 (stabil, banyak fitur baru)

### Kelebihan JavaFX
✅ **Modern UI**: Material design, animasi, transitions  
✅ **CSS Styling**: Seperti web development  
✅ **FXML**: Declarative UI (XML-based layout)  
✅ **Cross-Platform**: Windows, Mac, Linux (write once, run anywhere)  
✅ **Rich Components**: TableView, Charts, Media Player  
✅ **Scene Builder**: Visual UI editor (drag & drop)

### Perbandingan dengan Framework Lain

| Framework | Platform | Language | UI Style |
|-----------|----------|----------|----------|
| **JavaFX** | Desktop | Java | Modern, CSS |
| Swing | Desktop | Java | Old-school |
| Electron | Desktop | JavaScript | Web-based |
| WPF | Windows only | C# | XAML |
| Qt | Cross-platform | C++ | Native |

**Kesimpulan**: JavaFX cocok untuk Java developers yang ingin bikin desktop app modern tanpa belajar bahasa baru.

---

## 🏗️ MVVM ARCHITECTURE

### Apa itu MVVM?

**MVVM** = **Model-View-ViewModel**

Ini adalah arsitektur pattern untuk memisahkan:
- **UI logic** (tampilan)
- **Business logic** (aturan bisnis)
- **Data** (model)

```
┌─────────┐         ┌──────────┐         ┌───────┐
│  View   │ ◄─────► │ViewModel │ ◄─────► │ Model │
│ (FXML)  │  Bind   │ (Logic)  │  Fetch  │ (API) │
└─────────┘         └──────────┘         └───────┘
```

### Komponen MVVM

**1. Model (Data Layer)**
```java
public class Presensi {
    private Long id;
    private String username;
    private LocalDate tanggal;
    private LocalTime jamMasuk;
    private String status;
    // Getters & Setters
}
```
- **Responsibility**: Represent data structure
- **Example**: Presensi, User, DashboardStats
- **Pure data**: No logic, just properties

**2. View (UI Layer)**
```xml
<!-- dashboard.fxml -->
<Label fx:id="totalPresensiLabel" text="0"/>
<TableView fx:id="presensiTable"/>
<Button fx:id="refreshButton" text="Refresh"/>
```
- **Responsibility**: Display UI, handle user input
- **Technology**: FXML (XML) + CSS
- **No logic**: Hanya layout dan styling

**3. ViewModel (Logic Layer)**
```java
public class DashboardViewModel {
    // Observable properties
    private IntegerProperty totalPresensi = new SimpleIntegerProperty(0);
    private ObservableList<Presensi> presensiList;
    
    // Business logic
    public void loadDashboardData() {
        // Fetch from API
        // Update properties
    }
}
```
- **Responsibility**: Business logic, state management
- **Observable**: Properties yang auto-update UI
- **Testable**: Bisa ditest tanpa UI

**4. Controller (Connector)**
```java
public class DashboardController {
    @FXML private Label totalPresensiLabel;
    private DashboardViewModel viewModel;
    
    public void initialize() {
        // Bind UI to ViewModel
        totalPresensiLabel.textProperty().bind(
            viewModel.totalPresensiProperty().asString()
        );
    }
}
```
- **Responsibility**: Connect View with ViewModel
- **Binding**: Link UI components to ViewModel properties
- **Event handling**: onClick, onAction, etc.

---

## 🔍 DATA BINDING: MAGIC BEHIND MVVM

### Apa itu Data Binding?

**Data Binding** = Menghubungkan UI dengan data secara otomatis.

**Tanpa Binding** (Manual Update):
```java
// ❌ Manual (error-prone, boilerplate)
public void updateUI() {
    int total = viewModel.getTotal();
    totalPresensiLabel.setText(String.valueOf(total));
}

// Setiap kali data berubah, harus panggil updateUI() manual!
```

**Dengan Binding** (Otomatis):
```java
// ✅ Binding (auto-update, clean)
totalPresensiLabel.textProperty().bind(
    viewModel.totalPresensiProperty().asString()
);

// Data berubah? UI otomatis update! 🎉
```

### Property Types

JavaFX menyediakan wrapper untuk primitive types:

| Primitive | Property Class | Example |
|-----------|---------------|---------|
| `int` | `IntegerProperty` | `new SimpleIntegerProperty(0)` |
| `double` | `DoubleProperty` | `new SimpleDoubleProperty(0.0)` |
| `boolean` | `BooleanProperty` | `new SimpleBooleanProperty(false)` |
| `String` | `StringProperty` | `new SimpleStringProperty("")` |

**Contoh Penggunaan**:
```java
public class DashboardViewModel {
    // Property declaration
    private final IntegerProperty totalPresensi = new SimpleIntegerProperty(0);
    
    // Getter for binding
    public IntegerProperty totalPresensiProperty() {
        return totalPresensi;
    }
    
    // Setter for business logic
    public void setTotalPresensi(int value) {
        totalPresensi.set(value); // UI auto-updates!
    }
}
```

### ObservableList

Untuk collections (List, Set), gunakan `ObservableList`:

```java
// Create observable list
ObservableList<Presensi> presensiList = FXCollections.observableArrayList();

// Bind to TableView
tableView.setItems(presensiList);

// Add item → table auto-updates!
presensiList.add(new Presensi(...));

// Replace all → table refreshes!
presensiList.setAll(newList);

// Remove item → table updates!
presensiList.remove(item);
```

**Magic**: Setiap perubahan pada `ObservableList` otomatis trigger UI update!

---

## 🧵 BACKGROUND THREADING

### Masalah: UI Freeze

```java
// ❌ BAD: Network request di main thread
@FXML
private void handleRefresh() {
    List<Presensi> data = apiService.fetchData(); // SLOW! (1-2 detik)
    tableView.setItems(data);
    
    // Problem: UI freeze selama request!
    // User tidak bisa klik apa-apa 😠
}
```

### Solusi: Background Thread

```java
// ✅ GOOD: Background thread
@FXML
private void handleRefresh() {
    showLoadingIndicator();
    
    new Thread(() -> {
        try {
            // Heavy work (network, database, etc.)
            List<Presensi> data = apiService.fetchData();
            
            // Update UI (must use Platform.runLater!)
            Platform.runLater(() -> {
                tableView.setItems(data);
                hideLoadingIndicator();
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                showError(e.getMessage());
            });
        }
    }).start();
}
```

### Platform.runLater() - PENTING!

**Rule**: Hanya **JavaFX Application Thread** yang boleh update UI.

```java
// ❌ CRASH: Update UI dari background thread
new Thread(() -> {
    label.setText("Done"); // IllegalStateException!
}).start();

// ✅ SAFE: Update UI via Platform.runLater()
new Thread(() -> {
    // Do work...
    Platform.runLater(() -> {
        label.setText("Done"); // OK!
    });
}).start();
```

**Analogi**: Seperti kitchen di restoran.
- **Background Thread** = Chef di dapur (heavy work)
- **UI Thread** = Waiter di depan (serve customer)
- **Platform.runLater()** = Bell untuk panggil waiter

Chef tidak boleh langsung serve customer → harus ring bell dulu!

---

## 🎨 FXML: DECLARATIVE UI

### Apa itu FXML?

**FXML** = XML-based markup untuk layout JavaFX UI.

**Analogi**: FXML seperti HTML, tapi untuk desktop app.

### Contoh FXML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.smk.presensi.desktop.controller.DashboardController">
    
    <!-- Top Bar -->
    <top>
        <HBox spacing="10">
            <Label text="Dashboard Admin" styleClass="title"/>
            <Button fx:id="refreshButton" text="Refresh"/>
        </HBox>
    </top>
    
    <!-- Content -->
    <center>
        <VBox spacing="20">
            <!-- Statistics -->
            <HBox>
                <VBox styleClass="stat-card">
                    <Label text="Total Presensi"/>
                    <Label fx:id="totalPresensiLabel" text="0"/>
                </VBox>
            </HBox>
            
            <!-- Table -->
            <TableView fx:id="presensiTable">
                <columns>
                    <TableColumn fx:id="usernameColumn" text="Username" prefWidth="150"/>
                    <TableColumn fx:id="statusColumn" text="Status" prefWidth="100"/>
                </columns>
            </TableView>
        </VBox>
    </center>
</BorderPane>
```

### FXML Attributes

| Attribute | Purpose | Example |
|-----------|---------|---------|
| `fx:id` | Inject ke Controller | `fx:id="refreshButton"` |
| `fx:controller` | Link Controller class | `fx:controller="...DashboardController"` |
| `text` | Static text | `text="Click Me"` |
| `prefWidth` | Preferred width | `prefWidth="200"` |
| `spacing` | Gap between children | `spacing="10"` |
| `styleClass` | CSS class | `styleClass="btn-primary"` |
| `onAction` | Event handler | `onAction="#handleClick"` |

### Loading FXML

```java
public class DesktopApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Load FXML file
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/dashboard.fxml")
        );
        Parent root = loader.load();
        
        // Get controller instance (auto-created by FXMLLoader)
        DashboardController controller = loader.getController();
        
        // Create scene & show
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

---

## 🎨 CSS STYLING

### JavaFX CSS vs Web CSS

**Similarities**:
- Sama-sama pakai selectors (`.class`, `#id`)
- Sama-sama pakai properties (`color`, `font-size`)
- Sama-sama pakai cascade (inheritance)

**Differences**:
- JavaFX pakai prefix `-fx-`
- Beberapa property berbeda nama
- No `px` unit (langsung number)

### Contoh CSS

```css
/* Root styling */
.root {
    -fx-background-color: #f5f5f5;
    -fx-font-family: "Segoe UI", Arial;
}

/* Button */
.btn-primary {
    -fx-background-color: #3498db;
    -fx-text-fill: white;
    -fx-background-radius: 5;
    -fx-padding: 10 20 10 20;
}

.btn-primary:hover {
    -fx-background-color: #2980b9;
    -fx-cursor: hand;
}

/* Card */
.stat-card {
    -fx-background-color: white;
    -fx-background-radius: 8;
    -fx-padding: 20;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);
}

/* Label */
.stat-value {
    -fx-font-size: 32;
    -fx-font-weight: bold;
    -fx-text-fill: #2c3e50;
}
```

### Apply CSS

**Method 1**: Inline in FXML
```xml
<Button text="Click" style="-fx-background-color: blue;"/>
```

**Method 2**: CSS File
```xml
<BorderPane stylesheets="@../css/dashboard.css">
    <Button styleClass="btn-primary" text="Click"/>
</BorderPane>
```

**Method 3**: Programmatically
```java
scene.getStylesheets().add("/css/dashboard.css");
button.getStyleClass().add("btn-primary");
```

---

## 🌐 HTTP CLIENT: KOMUNIKASI DENGAN BACKEND

### Java HTTP Client (Java 11+)

```java
public class ApiClient {
    private final HttpClient httpClient;
    private String jwtToken;
    
    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    // GET request
    public HttpResponse<String> get(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api" + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();
        
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    // POST request
    public HttpResponse<String> post(String endpoint, String jsonBody) 
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api" + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
```

### JSON Parsing dengan Gson

```java
// Convert JSON string → Java object
Gson gson = new Gson();
Presensi presensi = gson.fromJson(jsonString, Presensi.class);

// Convert Java object → JSON string
String json = gson.toJson(presensi);
```

**Custom Deserializer** (untuk LocalDate, LocalTime):
```java
Gson gson = new GsonBuilder()
    .registerTypeAdapter(LocalDate.class, 
        (JsonDeserializer<LocalDate>) (json, type, context) -> 
            LocalDate.parse(json.getAsString()))
    .registerTypeAdapter(LocalTime.class,
        (JsonDeserializer<LocalTime>) (json, type, context) -> 
            LocalTime.parse(json.getAsString()))
    .create();
```

---

## 📊 TABLEVIEW: MENAMPILKAN DATA TABEL

### Setup TableView

**1. FXML Definition**:
```xml
<TableView fx:id="presensiTable">
    <columns>
        <TableColumn fx:id="idColumn" text="ID" prefWidth="50"/>
        <TableColumn fx:id="usernameColumn" text="Username" prefWidth="150"/>
        <TableColumn fx:id="statusColumn" text="Status" prefWidth="100"/>
    </columns>
</TableView>
```

**2. Controller Setup**:
```java
@FXML private TableView<Presensi> presensiTable;
@FXML private TableColumn<Presensi, Long> idColumn;
@FXML private TableColumn<Presensi, String> usernameColumn;
@FXML private TableColumn<Presensi, String> statusColumn;

@Override
public void initialize(URL url, ResourceBundle rb) {
    // Bind columns to model properties
    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
    usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
    statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    
    // Bind data
    presensiTable.setItems(viewModel.getPresensiList());
}
```

**3. PropertyValueFactory**:
```java
new PropertyValueFactory<>("username")
```
Ini akan otomatis panggil `presensi.getUsername()` untuk setiap row.

**Requirement**: Model harus punya getter dengan nama yang sama!
```java
public class Presensi {
    private String username;
    
    public String getUsername() { // ← Must match!
        return username;
    }
}
```

### Custom Cell Rendering

```java
statusColumn.setCellFactory(column -> new TableCell<Presensi, String>() {
    @Override
    protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);
        
        if (empty || status == null) {
            setText(null);
            setStyle("");
        } else {
            setText(status);
            
            // Color coding
            switch (status) {
                case "HADIR":
                    setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    break;
                case "TERLAMBAT":
                    setStyle("-fx-text-fill: orange;");
                    break;
                case "ALPHA":
                    setStyle("-fx-text-fill: red;");
                    break;
            }
        }
    }
});
```

---

## 🚀 MENJALANKAN APLIKASI

### 1. Via Maven
```bash
cd desktop-app
mvn javafx:run
```

### 2. Via IDE (IntelliJ IDEA)
1. Open `DesktopApp.java`
2. Right-click → Run 'DesktopApp.main()'

### 3. Build JAR
```bash
mvn clean package
java -jar target/desktop-app-1.0.0.jar
```

### 4. Module Path (jika error)
```bash
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar desktop-app.jar
```

---

## 🐛 COMMON ERRORS & SOLUTIONS

### Error 1: FXML Location Not Found
```
java.lang.NullPointerException: Location is required
```

**Cause**: Path FXML salah

**Solution**:
```java
// ❌ Wrong
getClass().getResource("dashboard.fxml")

// ✅ Correct (dengan leading slash)
getClass().getResource("/fxml/dashboard.fxml")
```

### Error 2: Controller Not Found
```
javafx.fxml.LoadException: Controller not specified
```

**Cause**: Missing `fx:controller` di FXML

**Solution**:
```xml
<BorderPane xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.smk.presensi.desktop.controller.DashboardController">
```

### Error 3: IllegalStateException (Not on FX Thread)
```
java.lang.IllegalStateException: Not on FX application thread
```

**Cause**: Update UI dari background thread

**Solution**:
```java
// Wrap dengan Platform.runLater()
Platform.runLater(() -> {
    label.setText("Done");
});
```

### Error 4: Module Not Found
```
Error: JavaFX runtime components are missing
```

**Cause**: JavaFX tidak di classpath

**Solution**:
- Run dengan `mvn javafx:run` (plugin handle automatically)
- Atau tambahkan VM options:
```
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
```

---

## 💡 BEST PRACTICES

### 1. Separation of Concerns
✅ **DO**: Pisahkan logic dari UI
```java
// ViewModel: Business logic
public void loadData() {
    List<Presensi> data = service.fetchData();
    presensiList.setAll(data);
}

// Controller: UI binding only
public void initialize() {
    tableView.setItems(viewModel.getPresensiList());
}
```

❌ **DON'T**: Logic di Controller
```java
// Bad: Mixing UI and business logic
public void initialize() {
    HttpClient client = HttpClient.newHttpClient();
    HttpResponse response = client.send(...); // ← Wrong place!
    List<Presensi> data = parseJson(response);
    tableView.setItems(data);
}
```

### 2. Use Data Binding
✅ **DO**: Bind properties
```java
label.textProperty().bind(viewModel.totalProperty().asString());
```

❌ **DON'T**: Manual updates
```java
// Manual update (boilerplate, error-prone)
public void updateUI() {
    label.setText(String.valueOf(viewModel.getTotal()));
}
```

### 3. Background Tasks for Heavy Work
✅ **DO**: Network request di background
```java
new Thread(() -> {
    List<Presensi> data = service.fetchData();
    Platform.runLater(() -> tableView.setItems(data));
}).start();
```

❌ **DON'T**: Block UI thread
```java
// UI freeze!
List<Presensi> data = service.fetchData(); // 2 seconds...
tableView.setItems(data);
```

### 4. Error Handling
✅ **DO**: Try-catch + user-friendly message
```java
try {
    service.fetchData();
} catch (IOException e) {
    showAlert("Gagal koneksi ke server. Cek internet Anda.");
}
```

❌ **DON'T**: Ignore errors
```java
// Silent fail (user confused)
try {
    service.fetchData();
} catch (Exception e) {
    // Do nothing
}
```

---

## 📚 RESOURCES

### Documentation
- **JavaFX Docs**: https://openjfx.io/
- **JavaFX Tutorial**: https://docs.oracle.com/javafx/
- **MVVM Pattern**: https://en.wikipedia.org/wiki/Model–view–viewmodel

### Tools
- **Scene Builder**: Visual FXML editor (https://gluonhq.com/products/scene-builder/)
- **IntelliJ IDEA**: Best IDE for JavaFX development
- **e(fx)clipse**: Eclipse plugin for JavaFX

### Libraries
- **ControlsFX**: Additional UI controls (https://controlsfx.github.io/)
- **JFoenix**: Material Design components (http://www.jfoenix.com/)
- **TilesFX**: Dashboard tiles (https://github.com/HanSolo/tilesfx)

---

## 🎓 KESIMPULAN

Anda sudah belajar:
1. ✅ **JavaFX Basics**: Application, Stage, Scene, Nodes
2. ✅ **MVVM Architecture**: Model, View, ViewModel, Controller
3. ✅ **Data Binding**: ObservableList, Property, Bidirectional binding
4. ✅ **Background Threading**: Platform.runLater(), avoid UI freeze
5. ✅ **FXML**: Declarative UI, separation of layout & logic
6. ✅ **CSS Styling**: Modern UI dengan `-fx-` properties
7. ✅ **HTTP Client**: Komunikasi dengan backend REST API
8. ✅ **TableView**: Display data dalam tabel dengan custom rendering

**Next Steps**:
- Tahap 2: Login screen dengan JWT authentication
- Tahap 3: Real RFID reader integration (serial port)
- Tahap 4: Export laporan ke PDF/CSV

Selamat belajar! 🚀

---

**Author**: Tim Pengembang SIJA  
**Last Updated**: 17 November 2025  
**License**: Educational Use Only
