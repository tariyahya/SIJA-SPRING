# TASK-DESKTOP-5: Advanced Features & Analytics

**Tahap**: Desktop App - Tahap 5  
**Branch**: `tahap-05-desktop-advanced`  
**Durasi**: 4-5 JP (Jam Pelajaran)  
**Prerequisites**: TASK-DESKTOP-4 (UI Implementation) harus selesai

---

## 🎯 TUJUAN PEMBELAJARAN

Setelah menyelesaikan tahap ini, siswa diharapkan mampu:
1. **Membuat visualisasi data** dengan JavaFX Charts (PieChart, BarChart, LineChart)
2. **Mengintegrasikan system tray notifications** dengan AWT
3. **Implement offline mode** dengan SQLite local cache
4. **Mengoptimalkan performance** aplikasi dengan lazy loading dan caching
5. **Memahami advanced JavaFX concepts** (animations, custom controls)

---

## 📚 KONSEP YANG DIPELAJARI

### 1. Data Visualization
- JavaFX Chart API
- PieChart untuk distribution data
- BarChart untuk time-series data
- LineChart untuk trends
- Multi-series charts

### 2. Desktop Integration
- AWT SystemTray API
- Desktop notifications
- Tray icon management
- Cross-platform considerations

### 3. Local Data Storage
- SQLite database
- JDBC connection management
- PreparedStatement dan batch operations
- Database indexing

### 4. Performance Optimization
- Lazy loading patterns
- In-memory caching
- Virtual scrolling
- Background task management

---

## 🗂️ STRUKTUR FILE

```
desktop-app/
├── pom.xml                              # Add SQLite dependency
├── src/main/
│   ├── java/com/smk/presensi/desktop/
│   │   ├── controller/
│   │   │   ├── DashboardController.java     # [UPDATE] Add analytics tab
│   │   │   └── AnalyticsController.java     # [NEW] Charts controller
│   │   ├── service/
│   │   │   ├── NotificationService.java     # [NEW] System tray
│   │   │   ├── LocalCacheService.java       # [NEW] SQLite cache
│   │   │   ├── PresensiService.java         # [UPDATE] Add caching
│   │   │   └── CachedPresensiService.java   # [NEW] Cache wrapper
│   │   ├── model/
│   │   │   └── PagedResult.java             # [NEW] Pagination model
│   │   └── util/
│   │       ├── ChartUtils.java              # [NEW] Chart helpers
│   │       └── InAppNotification.java       # [NEW] In-app toast
│   └── resources/
│       ├── fxml/
│       │   ├── dashboard.fxml               # [UPDATE] Add analytics tab
│       │   └── analytics.fxml               # [NEW] Charts view
│       ├── images/
│       │   └── app-icon.png                 # [NEW] Tray icon
│       └── css/
│           └── analytics.css                # [NEW] Chart styling
```

---

## 📝 TASK LIST

### Phase 1: Charts & Visualization (2 JP)

#### 1.1 Setup Analytics View
- [ ] Create `analytics.fxml` dengan GridPane layout
- [ ] Add PieChart, BarChart, LineChart components
- [ ] Create `AnalyticsController.java`
- [ ] Add date range filter (DatePicker)
- [ ] Add refresh button

#### 1.2 Implement PieChart - Status Distribution
- [ ] Fetch data from PresensiService
- [ ] Group data by status (HADIR, SAKIT, IZIN, ALFA)
- [ ] Create PieChart.Data items
- [ ] Apply custom colors per status
- [ ] Add percentage labels
- [ ] Handle empty data case

**Expected Output**:
```
PieChart showing:
- HADIR: 75% (Green)
- SAKIT: 10% (Orange)
- IZIN: 8% (Blue)
- ALFA: 7% (Red)
```

#### 1.3 Implement BarChart - Daily Attendance
- [ ] Group data by date
- [ ] Sort dates chronologically
- [ ] Create XYChart.Series
- [ ] Format date labels (dd MMM)
- [ ] Configure CategoryAxis and NumberAxis
- [ ] Add data labels on bars

**Expected Output**:
```
BarChart showing daily counts:
15 Nov: 45 presensi
16 Nov: 52 presensi
17 Nov: 48 presensi
...
```

#### 1.4 Implement LineChart - Weekly Trend
- [ ] Group data by week number
- [ ] Calculate weekly totals
- [ ] Create trend line series
- [ ] Add data point markers
- [ ] Implement multi-series (HADIR vs ALFA)
- [ ] Add hover tooltips

**Expected Output**:
```
LineChart showing trend:
W46: 220 presensi
W47: 235 presensi
W48: 210 presensi (downward trend)
```

#### 1.5 Chart Styling & Animation
- [ ] Create `analytics.css` untuk custom colors
- [ ] Add fade-in animation saat chart load
- [ ] Implement smooth transitions on data change
- [ ] Add legend customization
- [ ] Responsive layout untuk window resize

---

### Phase 2: Notification System (1 JP)

#### 2.1 System Tray Integration
- [ ] Create `NotificationService.java` dengan singleton pattern
- [ ] Check if SystemTray.isSupported()
- [ ] Load app icon (app-icon.png)
- [ ] Create TrayIcon and add to SystemTray
- [ ] Implement showInfo(), showWarning(), showError()
- [ ] Test on Windows (try WSL/Linux jika ada)

**Code Snippet**:
```java
public class NotificationService {
    private static NotificationService instance;
    private TrayIcon trayIcon;
    
    public void showInfo(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, MessageType.INFO);
        }
    }
}
```

#### 2.2 WebSocket Integration
- [ ] Update DashboardController
- [ ] Call NotificationService.showInfo() di onPresensiCreated callback
- [ ] Check settings: isShowNotifications()
- [ ] Format message: "Username - Status"
- [ ] Test dengan create presensi via Postman

#### 2.3 In-App Notifications (Alternative)
- [ ] Create `InAppNotification.java` utility class
- [ ] Design notification popup (HBox dengan label + close button)
- [ ] Position at bottom-right of window
- [ ] Add fade-in animation
- [ ] Auto-hide setelah 3 detik dengan PauseTransition
- [ ] Implement close button

**Expected Behavior**:
```
User creates presensi via mobile → 
WebSocket message received → 
Desktop shows notification: "Budi Santoso - HADIR"
```

---

### Phase 3: Offline Mode (1.5 JP)

#### 3.1 SQLite Setup
- [ ] Add SQLite JDBC dependency ke pom.xml
- [ ] Create `LocalCacheService.java`
- [ ] Implement initDatabase() method
- [ ] Create schema: presensi_cache table
- [ ] Add indexes: idx_tanggal, idx_synced
- [ ] Test database creation (~/.sija/cache.db)

**Database Schema**:
```sql
CREATE TABLE presensi_cache (
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
```

#### 3.2 Cache Operations
- [ ] Implement cachePresensi(List<Presensi>)
- [ ] Use PreparedStatement dengan batch insert
- [ ] Implement getCachedPresensi(startDate, endDate)
- [ ] Implement clearCache() method
- [ ] Handle SQLException properly

#### 3.3 Service Integration
- [ ] Update PresensiService.getPresensiByDateRange()
- [ ] Try fetch from server first
- [ ] On success: cache the result
- [ ] On failure: fallback to getCachedPresensi()
- [ ] Show warning toast: "Using cached data (offline mode)"

**Expected Flow**:
```
1. User opens dashboard
2. Try fetch from http://localhost:8080/api/presensi
3. If success: cache data to SQLite + display
4. If fail: load from SQLite cache + show "offline" indicator
```

#### 3.4 Sync Mechanism (Optional)
- [ ] Add "synced" flag to track unsynced data
- [ ] Implement background sync when connection restored
- [ ] Update UI dengan sync status
- [ ] Handle conflict resolution

---

### Phase 4: Performance Optimization (1 JP)

#### 4.1 Backend Pagination
- [ ] Verify backend has /api/presensi/paginated endpoint
- [ ] Create PagedResult model class
- [ ] Update PresensiService untuk support pagination
- [ ] Implement getPresensiPaginated(page, size, startDate, endDate)
- [ ] Parse page metadata (totalPages, currentPage, hasNext)

#### 4.2 Lazy Loading TableView
- [ ] Create LazyTableView<T> extends TableView<T>
- [ ] Implement setAllData(List<T>) method
- [ ] Implement loadNextPage() method
- [ ] Add scroll listener untuk detect bottom scroll
- [ ] Trigger loadNextPage() saat scroll > 90%
- [ ] Show loading indicator saat fetching

#### 4.3 Image Caching
- [ ] Create imageCache Map<String, Image>
- [ ] Update foto TableCell setCellFactory
- [ ] Use computeIfAbsent() untuk lazy load
- [ ] Implement cache eviction strategy (LRU)
- [ ] Add placeholder image untuk loading state

**Code Snippet**:
```java
Map<String, Image> imageCache = new ConcurrentHashMap<>();

fotoColumn.setCellFactory(col -> new TableCell<>() {
    @Override
    protected void updateItem(String fotoPath, boolean empty) {
        super.updateItem(fotoPath, empty);
        if (!empty && fotoPath != null) {
            Image img = imageCache.computeIfAbsent(fotoPath, path ->
                new Image("http://localhost:8080" + path, true) // background loading
            );
            setGraphic(new ImageView(img));
        }
    }
});
```

#### 4.4 In-Memory Cache
- [ ] Create CachedPresensiService wrapper
- [ ] Implement CacheEntry<T> inner class dengan TTL
- [ ] Cache results dengan key = "startDate_endDate"
- [ ] Check cache expiration (default: 5 minutes)
- [ ] Return cached data jika belum expired
- [ ] Clear cache on manual refresh

---

## 🧪 TESTING CHECKLIST

### Charts Testing
- [ ] PieChart renders with correct data
- [ ] Colors match status (Green=HADIR, Red=ALFA, etc)
- [ ] BarChart shows daily distribution
- [ ] X-axis labels readable (tidak overlap)
- [ ] LineChart shows trend correctly
- [ ] Date range filter updates all charts
- [ ] Charts responsive saat window resize
- [ ] Empty data shows placeholder message

### Notification Testing
- [ ] System tray icon appears
- [ ] Click icon shows menu (optional)
- [ ] Notification appears on new presensi
- [ ] Notification title and message correct
- [ ] Settings toggle enables/disables notifications
- [ ] In-app notification shows and auto-hides
- [ ] Close button works

### Offline Mode Testing
- [ ] Database file created at ~/.sija/cache.db
- [ ] First load caches data
- [ ] Disconnect backend (stop Spring Boot)
- [ ] Reload dashboard → Shows cached data
- [ ] "Offline mode" indicator visible
- [ ] Reconnect backend → Fetches fresh data
- [ ] Cache cleared after manual refresh

### Performance Testing
- [ ] Load 1000+ records → No UI freeze
- [ ] Scrolling is smooth (60 FPS)
- [ ] Charts render in < 2 seconds
- [ ] Image loading tidak block UI
- [ ] Memory usage stable (< 500 MB)
- [ ] CPU usage low saat idle

---

## 📊 DELIVERABLES

### 1. Source Code
- `AnalyticsController.java` dengan 3 charts
- `NotificationService.java` dengan system tray
- `LocalCacheService.java` dengan SQLite operations
- `CachedPresensiService.java` dengan in-memory cache
- Updated `DashboardController.java` dengan analytics tab

### 2. Resources
- `analytics.fxml` dengan chart layout
- `analytics.css` untuk chart styling
- `app-icon.png` untuk tray icon
- Updated `dashboard.fxml` dengan TabPane

### 3. Database
- SQLite schema script (`schema.sql`)
- Cache database file (`~/.sija/cache.db`)

### 4. Documentation
- README-DESKTOP-TAHAP-05.md dengan:
  - Feature overview
  - Chart usage guide
  - Offline mode explanation
  - Performance tips
  - Troubleshooting

---

## 🎓 LEARNING OUTCOMES

Setelah menyelesaikan tahap ini, siswa akan menguasai:

### Technical Skills
1. **JavaFX Charts API**: PieChart, BarChart, LineChart
2. **AWT Integration**: SystemTray, TrayIcon, Desktop class
3. **SQLite Database**: JDBC, PreparedStatement, transactions
4. **Performance Patterns**: Lazy loading, caching strategies
5. **Asynchronous Programming**: Task, Platform.runLater()

### Software Engineering Concepts
1. **Data Visualization**: Choosing right chart for data type
2. **Offline-First Architecture**: Cache-aside pattern
3. **User Experience**: Notifications, loading states, smooth animations
4. **Performance Optimization**: Profiling, bottleneck identification
5. **Cross-Platform Development**: OS-specific features (System Tray)

---

## 💡 TIPS & BEST PRACTICES

### Chart Design
- Keep charts simple dan focused
- Use consistent color scheme
- Add clear labels dan legends
- Avoid 3D charts (sulit dibaca)
- Update charts in background thread

### Notification UX
- Don't spam notifications
- Use appropriate message type (INFO/WARNING/ERROR)
- Make notifications actionable (jika perlu)
- Respect user preference (settings toggle)

### Caching Strategy
- Cache frequently accessed data
- Set reasonable TTL (Time-To-Live)
- Clear cache on logout
- Handle cache invalidation
- Monitor cache size

### Performance
- Profile before optimize
- Use background threads untuk I/O operations
- Avoid premature optimization
- Test dengan realistic dataset
- Monitor memory leaks

---

## 🚀 NEXT STEPS

Setelah menyelesaikan Tahap 5, lanjut ke:

**Mobile App Integration**:
- Android app dengan barcode scanner
- Camera integration untuk foto presensi
- GPS location tracking
- Push notifications dari server

**Advanced Features**:
- Face recognition untuk presensi
- Report scheduling (email PDF)
- Multi-language support
- Dark mode theme

---

## 📚 REFERENCES

### JavaFX Charts
- [Official JavaFX Charts Tutorial](https://docs.oracle.com/javafx/2/charts/jfxpub-charts.htm)
- [Chart CSS Reference](https://docs.oracle.com/javafx/2/api/javafx/scene/chart/Chart.html)

### AWT SystemTray
- [SystemTray Documentation](https://docs.oracle.com/javase/8/docs/api/java/awt/SystemTray.html)
- [TrayIcon Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/misc/systemtray.html)

### SQLite
- [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)
- [SQLite Tutorial](https://www.sqlitetutorial.net/)

### Performance
- [JavaFX Performance Guide](https://docs.oracle.com/javafx/2/best_practices/jfxpub-best_practices.htm)
- [Concurrent Programming in JavaFX](https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm)

---

**END OF TASK-DESKTOP-5**
