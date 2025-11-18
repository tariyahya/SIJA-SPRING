# Blog Desktop-5: Advanced Features & Analytics

**Tanggal**: 18 November 2025  
**Penulis**: Tim Pengembang SIJA  
**Tahap**: Desktop App - Tahap 5  
**Topik**: Charts & Visualization, Notification System, Offline Mode, Performance Optimization

---

## 🎯 PENDAHULUAN

Di Tahap 4, kita sudah implement **full UI** dengan User Management, Settings Panel, dan Dashboard Integration. Sekarang di Tahap 5, kita akan menambahkan **advanced features** untuk membuat aplikasi lebih **powerful** dan **professional**.

**Yang Akan Dibuat**:
1. **Charts & Analytics** - Visualisasi data presensi dengan JavaFX Charts
2. **Notification System** - System tray notifications untuk real-time alerts
3. **Offline Mode** - Local caching dengan SQLite
4. **Performance Optimization** - Lazy loading, pagination, caching

---

## 📊 CHARTS & ANALYTICS

### Mengapa Perlu Charts?

**Problem**: TableView hanya tampil raw data, sulit lihat **trends** dan **patterns**.

**Solution**: Visualize data dengan:
- **BarChart**: Presensi per hari
- **PieChart**: Persentase status (HADIR, SAKIT, IZIN, ALFA)
- **LineChart**: Trend presensi per minggu/bulan

---

### JavaFX Charts Overview

JavaFX menyediakan 6 chart types:
1. **PieChart** - Pie/donut chart
2. **BarChart** - Vertical/horizontal bars
3. **LineChart** - Line graphs
4. **AreaChart** - Filled line charts
5. **ScatterChart** - Scatter plots
6. **BubbleChart** - Bubble charts

**Base Components**:
- `XYChart<X, Y>` - Base class untuk XY charts
- `XYChart.Series` - Data series
- `XYChart.Data<X, Y>` - Single data point
- `CategoryAxis` - Axis untuk categories (String)
- `NumberAxis` - Axis untuk numbers

---

### PieChart - Status Distribution

**Goal**: Tampilkan persentase HADIR vs SAKIT vs IZIN vs ALFA.

```java
public class AnalyticsController {
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> dailyBarChart;
    @FXML private LineChart<String, Number> trendLineChart;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button refreshButton;
    
    private PresensiService presensiService;
    
    @FXML
    public void initialize() {
        presensiService = new PresensiService(ApiClient.getInstance());
        
        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        
        // Load initial data
        refreshCharts();
    }
    
    @FXML
    private void handleRefresh() {
        refreshCharts();
    }
    
    private void refreshCharts() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        
        Task<List<Presensi>> task = new Task<>() {
            @Override
            protected List<Presensi> call() throws Exception {
                return presensiService.getPresensiByDateRange(start, end);
            }
        };
        
        task.setOnSucceeded(e -> {
            List<Presensi> dataList = task.getValue();
            
            updatePieChart(dataList);
            updateBarChart(dataList);
            updateLineChart(dataList);
        });
        
        task.setOnFailed(e -> {
            showError("Failed to load data: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    private void updatePieChart(List<Presensi> dataList) {
        // Count by status
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
        statusPieChart.setTitle("Status Distribution");
        
        // Apply colors
        applyPieChartColors();
    }
    
    private void applyPieChartColors() {
        // Wait for chart to render
        Platform.runLater(() -> {
            statusPieChart.getData().forEach(data -> {
                String status = data.getName().split(" ")[0]; // Get "HADIR" from "HADIR (25)"
                
                String color = switch(status) {
                    case "HADIR" -> "#4CAF50"; // Green
                    case "SAKIT" -> "#FF9800"; // Orange
                    case "IZIN" -> "#2196F3";  // Blue
                    case "ALFA" -> "#f44336";  // Red
                    default -> "#9E9E9E";      // Grey
                };
                
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            });
        });
    }
}
```

**Key Points**:
- Stream API untuk group by status
- `PieChart.Data(name, value)` untuk create slices
- `setStyle()` untuk custom colors per slice
- `Platform.runLater()` karena node belum render saat initialization

---

### BarChart - Daily Attendance

**Goal**: Bar chart dengan X-axis = date, Y-axis = count.

```java
private void updateBarChart(List<Presensi> dataList) {
    // Group by date
    Map<LocalDate, Long> dailyCounts = dataList.stream()
        .collect(Collectors.groupingBy(
            presensi -> LocalDate.parse(presensi.getTanggal()),
            TreeMap::new, // Sorted by date
            Collectors.counting()
        ));
    
    // Create series
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Daily Attendance");
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
    
    dailyCounts.forEach((date, count) -> {
        series.getData().add(
            new XYChart.Data<>(date.format(formatter), count)
        );
    });
    
    dailyBarChart.getData().clear();
    dailyBarChart.getData().add(series);
    dailyBarChart.setTitle("Presensi per Hari");
    
    // Customize axis
    CategoryAxis xAxis = (CategoryAxis) dailyBarChart.getXAxis();
    xAxis.setLabel("Tanggal");
    
    NumberAxis yAxis = (NumberAxis) dailyBarChart.getYAxis();
    yAxis.setLabel("Jumlah Presensi");
    yAxis.setTickUnit(1); // Integer steps
}
```

**Key Points**:
- `TreeMap` untuk auto-sort by date
- `DateTimeFormatter` untuk format date labels
- `XYChart.Series` dapat contain multiple data points
- `CategoryAxis` untuk string labels, `NumberAxis` untuk numbers

---

### LineChart - Weekly Trend

**Goal**: Line chart untuk track trend per minggu.

```java
private void updateLineChart(List<Presensi> dataList) {
    // Group by week
    Map<String, Long> weeklyCounts = dataList.stream()
        .collect(Collectors.groupingBy(
            presensi -> {
                LocalDate date = LocalDate.parse(presensi.getTanggal());
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
    series.setName("Weekly Trend");
    
    weeklyCounts.forEach((week, count) -> {
        series.getData().add(new XYChart.Data<>(week, count));
    });
    
    trendLineChart.getData().clear();
    trendLineChart.getData().add(series);
    trendLineChart.setTitle("Trend Mingguan");
    
    // Smooth line (optional)
    trendLineChart.setCreateSymbols(true); // Show data points
}
```

**Advanced**: Multiple series untuk compare HADIR vs ALFA:

```java
private void updateMultiSeriesLineChart(List<Presensi> dataList) {
    // Series 1: HADIR
    XYChart.Series<String, Number> hadirSeries = new XYChart.Series<>();
    hadirSeries.setName("HADIR");
    
    // Series 2: ALFA
    XYChart.Series<String, Number> alfaSeries = new XYChart.Series<>();
    alfaSeries.setName("ALFA");
    
    // Group by week and status
    Map<String, Map<String, Long>> weeklyByStatus = dataList.stream()
        .collect(Collectors.groupingBy(
            presensi -> {
                LocalDate date = LocalDate.parse(presensi.getTanggal());
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int week = date.get(weekFields.weekOfWeekBasedYear());
                return "W" + week;
            },
            TreeMap::new,
            Collectors.groupingBy(
                Presensi::getStatus,
                Collectors.counting()
            )
        ));
    
    weeklyByStatus.forEach((week, statusMap) -> {
        hadirSeries.getData().add(
            new XYChart.Data<>(week, statusMap.getOrDefault("HADIR", 0L))
        );
        alfaSeries.getData().add(
            new XYChart.Data<>(week, statusMap.getOrDefault("ALFA", 0L))
        );
    });
    
    trendLineChart.getData().clear();
    trendLineChart.getData().addAll(hadirSeries, alfaSeries);
}
```

---

### FXML Layout

```xml
<BorderPane xmlns:fx="http://javafx.com/fxml">
    <top>
        <HBox spacing="10" style="-fx-padding: 20;">
            <Label text="From:"/>
            <DatePicker fx:id="startDatePicker"/>
            <Label text="To:"/>
            <DatePicker fx:id="endDatePicker"/>
            <Button fx:id="refreshButton" text="Refresh" onAction="#handleRefresh"/>
        </HBox>
    </top>
    
    <center>
        <GridPane hgap="20" vgap="20" style="-fx-padding: 20;">
            <!-- Row 1: PieChart + BarChart -->
            <PieChart fx:id="statusPieChart" 
                      GridPane.columnIndex="0" GridPane.rowIndex="0"
                      prefWidth="400" prefHeight="400"/>
            
            <BarChart fx:id="dailyBarChart" 
                      GridPane.columnIndex="1" GridPane.rowIndex="0"
                      prefWidth="600" prefHeight="400">
                <xAxis>
                    <CategoryAxis label="Date"/>
                </xAxis>
                <yAxis>
                    <NumberAxis label="Count"/>
                </yAxis>
            </BarChart>
            
            <!-- Row 2: LineChart (full width) -->
            <LineChart fx:id="trendLineChart" 
                       GridPane.columnIndex="0" GridPane.rowIndex="1" GridPane.columnSpan="2"
                       prefWidth="1000" prefHeight="400">
                <xAxis>
                    <CategoryAxis label="Week"/>
                </xAxis>
                <yAxis>
                    <NumberAxis label="Count"/>
                </yAxis>
            </LineChart>
        </GridPane>
    </center>
</BorderPane>
```

---

## 🔔 NOTIFICATION SYSTEM

### System Tray Integration

**Goal**: Show desktop notifications saat ada presensi baru (via WebSocket).

**Challenge**: JavaFX tidak support native system tray, butuh **AWT**.

```java
import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class NotificationService {
    private static NotificationService instance;
    private SystemTray tray;
    private TrayIcon trayIcon;
    
    private NotificationService() {
        setupSystemTray();
    }
    
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported");
            return;
        }
        
        try {
            tray = SystemTray.getSystemTray();
            
            // Load icon
            Image icon = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/images/app-icon.png")
            );
            
            trayIcon = new TrayIcon(icon, "SIJA Desktop");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("SIJA Desktop App");
            
            // Add to tray
            tray.add(trayIcon);
            
            System.out.println("System tray initialized");
        } catch (AWTException e) {
            System.err.println("Failed to add tray icon: " + e.getMessage());
        }
    }
    
    public void showNotification(String title, String message, MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        }
    }
    
    public void showInfo(String title, String message) {
        showNotification(title, message, MessageType.INFO);
    }
    
    public void showWarning(String title, String message) {
        showNotification(title, message, MessageType.WARNING);
    }
    
    public void showError(String title, String message) {
        showNotification(title, message, MessageType.ERROR);
    }
    
    public void remove() {
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
        }
    }
}
```

### WebSocket Integration

```java
// In DashboardController
private void setupWebSocket(String serverUrl) {
    String wsUrl = serverUrl.replace("http://", "ws://") + "/ws/presensi";
    wsService = new WebSocketService(wsUrl);
    
    wsService.setOnPresensiCreated(presensi -> {
        // Add to table
        presensiTableData.add(0, presensi);
        
        // Show notification
        if (SettingsManager.getInstance().getSettings().isShowNotifications()) {
            NotificationService.getInstance().showInfo(
                "Presensi Baru",
                presensi.getUsername() + " - " + presensi.getStatus()
            );
        }
    });
    
    // ...rest of setup
}
```

### In-App Notifications

**Alternative**: Notification popup di dalam aplikasi (tanpa system tray).

```java
public class InAppNotification {
    public static void show(String message, Parent root) {
        // Create notification node
        HBox notification = new HBox();
        notification.setStyle(
            "-fx-background-color: #323232; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 5; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2);"
        );
        
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        
        Button closeBtn = new Button("×");
        closeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 20;"
        );
        
        notification.getChildren().addAll(label, closeBtn);
        
        // Position at bottom
        notification.setLayoutX(10);
        notification.setLayoutY(((Pane) root).getHeight() - 70);
        
        // Add to root
        if (root instanceof Pane) {
            ((Pane) root).getChildren().add(notification);
        }
        
        // Auto-hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), notification);
            fade.setToValue(0);
            fade.setOnFinished(evt -> ((Pane) root).getChildren().remove(notification));
            fade.play();
        });
        pause.play();
        
        // Close button
        closeBtn.setOnAction(e -> {
            ((Pane) root).getChildren().remove(notification);
        });
    }
}
```

**Usage**:

```java
InAppNotification.show("Presensi berhasil disimpan", dashboardRoot);
```

---

## 💾 OFFLINE MODE

### Mengapa Butuh Offline Mode?

**Problem**: 
- Backend down → App crash
- Network lambat → User menunggu lama
- Mobile use case → Intermittent connection

**Solution**: Local caching dengan **SQLite**.

---

### SQLite Setup

**Add dependency** ke `pom.xml`:

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>
```

**Database Schema**:

```sql
CREATE TABLE IF NOT EXISTS presensi_cache (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    username TEXT NOT NULL,
    nama_lengkap TEXT,
    tanggal TEXT NOT NULL,
    jam_masuk TEXT NOT NULL,
    jam_keluar TEXT,
    status TEXT NOT NULL,
    foto_path TEXT,
    synced INTEGER DEFAULT 0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tanggal ON presensi_cache(tanggal);
CREATE INDEX idx_synced ON presensi_cache(synced);
```

---

### LocalCacheService

```java
public class LocalCacheService {
    private static LocalCacheService instance;
    private Connection connection;
    private final String dbPath = System.getProperty("user.home") + "/.sija/cache.db";
    
    private LocalCacheService() {
        initDatabase();
    }
    
    public static LocalCacheService getInstance() {
        if (instance == null) {
            instance = new LocalCacheService();
        }
        return instance;
    }
    
    private void initDatabase() {
        try {
            // Create directory if not exists
            File dbFile = new File(dbPath);
            dbFile.getParentFile().mkdirs();
            
            // Connect
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            // Create tables
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS presensi_cache (
                    id INTEGER PRIMARY KEY,
                    user_id INTEGER NOT NULL,
                    username TEXT NOT NULL,
                    nama_lengkap TEXT,
                    tanggal TEXT NOT NULL,
                    jam_masuk TEXT NOT NULL,
                    jam_kelur TEXT,
                    status TEXT NOT NULL,
                    foto_path TEXT,
                    synced INTEGER DEFAULT 0,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
                
                CREATE INDEX IF NOT EXISTS idx_tanggal ON presensi_cache(tanggal);
                CREATE INDEX IF NOT EXISTS idx_synced ON presensi_cache(synced);
            """;
            
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(createTableSQL);
            
            System.out.println("Local cache database initialized");
        } catch (SQLException e) {
            System.err.println("Failed to init cache database: " + e.getMessage());
        }
    }
    
    public void cachePresensi(List<Presensi> presensiList) {
        String sql = """
            INSERT OR REPLACE INTO presensi_cache 
            (id, user_id, username, nama_lengkap, tanggal, jam_masuk, jam_keluar, status, foto_path, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            
            for (Presensi p : presensiList) {
                pstmt.setLong(1, p.getId());
                pstmt.setLong(2, p.getUserId());
                pstmt.setString(3, p.getUsername());
                pstmt.setString(4, p.getNamaLengkap());
                pstmt.setString(5, p.getTanggal());
                pstmt.setString(6, p.getJamMasuk());
                pstmt.setString(7, p.getJamKeluar());
                pstmt.setString(8, p.getStatus());
                pstmt.setString(9, p.getFotoPath());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            
            System.out.println("Cached " + presensiList.size() + " presensi records");
        } catch (SQLException e) {
            System.err.println("Failed to cache presensi: " + e.getMessage());
        }
    }
    
    public List<Presensi> getCachedPresensi(LocalDate startDate, LocalDate endDate) {
        List<Presensi> result = new ArrayList<>();
        
        String sql = """
            SELECT * FROM presensi_cache
            WHERE tanggal BETWEEN ? AND ?
            ORDER BY tanggal DESC, jam_masuk DESC
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Presensi p = new Presensi();
                p.setId(rs.getLong("id"));
                p.setUserId(rs.getLong("user_id"));
                p.setUsername(rs.getString("username"));
                p.setNamaLengkap(rs.getString("nama_lengkap"));
                p.setTanggal(rs.getString("tanggal"));
                p.setJamMasuk(rs.getString("jam_masuk"));
                p.setJamKeluar(rs.getString("jam_keluar"));
                p.setStatus(rs.getString("status"));
                p.setFotoPath(rs.getString("foto_path"));
                
                result.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get cached presensi: " + e.getMessage());
        }
        
        return result;
    }
    
    public void clearCache() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM presensi_cache");
            System.out.println("Cache cleared");
        } catch (SQLException e) {
            System.err.println("Failed to clear cache: " + e.getMessage());
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close cache connection: " + e.getMessage());
        }
    }
}
```

---

### Integrate with PresensiService

```java
public class PresensiService {
    private ApiClient apiClient;
    private LocalCacheService cacheService;
    
    public PresensiService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.cacheService = LocalCacheService.getInstance();
    }
    
    public List<Presensi> getPresensiByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        try {
            // Try to fetch from server
            String start = startDate.toString();
            String end = endDate.toString();
            String response = apiClient.get("/api/presensi?startDate=" + start + "&endDate=" + end);
            
            List<Presensi> presensiList = parsePresensiList(response);
            
            // Cache the result
            cacheService.cachePresensi(presensiList);
            
            return presensiList;
            
        } catch (Exception e) {
            System.err.println("Failed to fetch from server, using cache: " + e.getMessage());
            
            // Fallback to cache
            List<Presensi> cached = cacheService.getCachedPresensi(startDate, endDate);
            
            if (cached.isEmpty()) {
                throw new Exception("No cached data available");
            }
            
            return cached;
        }
    }
}
```

**Pattern**: Try online → Fallback to cache.

---

## ⚡ PERFORMANCE OPTIMIZATION

### Problem: Slow UI dengan Large Dataset

**Scenario**: Load 1000+ presensi records → UI freeze.

**Root Cause**:
1. Network request di JavaFX thread (blocking)
2. TableView render semua rows (overkill)
3. No pagination

---

### Solution 1: Lazy Loading

```java
public class LazyTableView<T> extends TableView<T> {
    private static final int PAGE_SIZE = 50;
    private int currentPage = 0;
    private ObservableList<T> allData = FXCollections.observableArrayList();
    
    public void setAllData(List<T> data) {
        allData.setAll(data);
        currentPage = 0;
        loadNextPage();
    }
    
    public void loadNextPage() {
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allData.size());
        
        if (start < allData.size()) {
            getItems().addAll(allData.subList(start, end));
            currentPage++;
        }
    }
    
    public boolean hasMorePages() {
        return currentPage * PAGE_SIZE < allData.size();
    }
}
```

**Usage**:

```java
LazyTableView<Presensi> table = new LazyTableView<>();

// Setup columns...

// Load data
Task<List<Presensi>> task = new Task<>() {
    @Override
    protected List<Presensi> call() throws Exception {
        return presensiService.getAllPresensi();
    }
};

task.setOnSucceeded(e -> {
    table.setAllData(task.getValue());
});

// Scroll listener untuk auto-load
table.setOnScroll(event -> {
    if (event.getDeltaY() < 0) { // Scroll down
        double scrollPosition = table.lookup(".scroll-bar:vertical").lookup(".thumb").getLayoutY();
        double maxScroll = table.lookup(".scroll-bar:vertical").lookup(".track").getLayoutBounds().getHeight();
        
        if (scrollPosition / maxScroll > 0.9) { // 90% scrolled
            if (table.hasMorePages()) {
                table.loadNextPage();
            }
        }
    }
});
```

---

### Solution 2: Virtual Flow

**JavaFX TableView** sudah implement virtual scrolling by default!

**Key**: Only visible rows are rendered.

**Optimization**: Avoid heavy operations di `setCellFactory()`.

**Bad**:
```java
column.setCellFactory(col -> new TableCell<>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        
        if (!empty) {
            // Heavy operation di setiap cell update!
            Image img = new Image("http://server.com/" + item);
            setGraphic(new ImageView(img));
        }
    }
});
```

**Good**:
```java
// Cache images
Map<String, Image> imageCache = new HashMap<>();

column.setCellFactory(col -> new TableCell<>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        
        if (!empty) {
            Image img = imageCache.computeIfAbsent(item, path -> 
                new Image("http://server.com/" + path)
            );
            setGraphic(new ImageView(img));
        }
    }
});
```

---

### Solution 3: Backend Pagination

**Better approach**: Paginate di backend, jangan load semua data.

**Backend Endpoint**:
```java
@GetMapping("/api/presensi/paginated")
public Page<Presensi> getPresensiPaginated(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "50") int size,
    @RequestParam(required = false) String startDate,
    @RequestParam(required = false) String endDate
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("tanggal").descending());
    // ...query with pagination
}
```

**Desktop Service**:
```java
public PagedResult<Presensi> getPresensiPaginated(int page, int size, LocalDate startDate, LocalDate endDate) throws Exception {
    String url = String.format(
        "/api/presensi/paginated?page=%d&size=%d&startDate=%s&endDate=%s",
        page, size, startDate, endDate
    );
    
    String response = apiClient.get(url);
    // Parse JSON with page metadata
    
    return new PagedResult<>(presensiList, totalPages, currentPage);
}
```

---

### Solution 4: Caching Strategies

**1. In-Memory Cache** (short-term):

```java
public class CachedPresensiService {
    private Map<String, CacheEntry<List<Presensi>>> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5 minutes
    
    public List<Presensi> getPresensi(LocalDate start, LocalDate end) throws Exception {
        String key = start + "_" + end;
        
        CacheEntry<List<Presensi>> entry = cache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            System.out.println("Returning cached data");
            return entry.getData();
        }
        
        // Fetch fresh data
        List<Presensi> data = fetchFromServer(start, end);
        
        // Cache it
        cache.put(key, new CacheEntry<>(data, CACHE_TTL));
        
        return data;
    }
    
    static class CacheEntry<T> {
        private T data;
        private long timestamp;
        private long ttl;
        
        public CacheEntry(T data, long ttl) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
        
        public T getData() {
            return data;
        }
    }
}
```

**2. Persistent Cache** (SQLite - already covered in Offline Mode section).

---

## 🧪 TESTING TAHAP 5

### Manual Testing Checklist

**Charts**:
- [ ] PieChart shows correct percentages
- [ ] BarChart displays daily counts
- [ ] LineChart shows weekly trends
- [ ] Date range filter updates charts
- [ ] Colors are consistent

**Notifications**:
- [ ] System tray icon appears
- [ ] Notifications appear when WebSocket receives data
- [ ] In-app notifications show and auto-hide
- [ ] Settings toggle enables/disables notifications

**Offline Mode**:
- [ ] Data cached after fetch
- [ ] Offline mode returns cached data
- [ ] Cache persists after app restart
- [ ] Clear cache works

**Performance**:
- [ ] Large dataset (1000+ records) loads without freeze
- [ ] Scrolling is smooth
- [ ] Charts render quickly

---

## 🎯 SUMMARY

**What We Built**:
1. ✅ Charts & Analytics (PieChart, BarChart, LineChart)
2. ✅ Notification System (System Tray + In-App)
3. ✅ Offline Mode (SQLite caching)
4. ✅ Performance Optimization (Lazy loading, caching, pagination)

**Key Libraries**:
- JavaFX Charts (built-in)
- AWT SystemTray (for desktop notifications)
- SQLite JDBC (for local cache)

**Key Patterns**:
- Chart data binding dengan Stream API
- Background tasks untuk avoid UI freeze
- Cache-aside pattern (try online → fallback cache)
- Lazy loading untuk large datasets

**Production Ready**:
- App dapat work offline
- Real-time notifications
- Visual analytics
- Optimized performance

---

**NEXT**: Mobile App Integration (Android) + Barcode Scanner 📱

---

**END OF BLOG-DESKTOP-5**
