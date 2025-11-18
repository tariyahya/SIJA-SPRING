# SIJA Presensi - Implementasi Plan (Tahap 03 Desktop Dashboard)

**Tanggal**: 18 November 2025  
**Branch**: tahap-03-desktop-dashboard  
**Fokus**: Melengkapi Desktop App dengan fitur User Management, Settings, dan Export

---

## Status Implementasi

### ✅ Sudah Dikerjakan (Task Desktop 1-3)

1. **Login UI & Authentication**
   - LoginController dengan form validation
   - Session management dengan JWT
   - Error handling dan loading indicators

2. **Dashboard UI dengan Real-time Updates**
   - Statistics cards (Total, Hadir, Terlambat, Alpha, Persentase)
   - TableView untuk daftar presensi
   - RFID simulation input
   - Auto-refresh mechanism (polling 30 detik)
   - Mock data untuk development

3. **MenuBar Navigation**
   - File menu: Export PDF, Export CSV, Logout
   - Manage menu: User Management
   - Settings menu: Preferences
   - Help menu: About

4. **Settings UI**
   - General settings (Server URL, refresh interval, WebSocket, notifications)
   - Export settings (format default, export directory)
   - UI settings (theme, rows per page, compact mode)
   - Advanced settings (timeouts, debug mode, mock data)

5. **User Management UI**
   - TableView dengan columns: ID, Username, Email, Role, Tipe, RFID Card ID, Enabled, Created At
   - Toolbar: Add, Edit, Delete, Search, Refresh
   - Mock data untuk preview

6. **Model & Service Layer**
   - User model dengan fields lengkap (tipe, rfidCardId, enabled, createdAt)
   - Presensi model
   - DashboardStats model
   - ApiClient untuk HTTP communication
   - SessionManager untuk session handling
   - PresensiService untuk data fetching
   - WebSocketService (placeholder untuk future implementation)

---

## 🎯 Yang Akan Dikerjakan Selanjutnya

Berdasarkan TASK-DESKTOP-3.md, kita akan melengkapi implementasi dengan prioritas:

### 1. **UserService** - Connect User Management ke Backend API ✅ **COMPLETED**

**File**: `desktop-app/src/main/java/com/smk/presensi/desktop/service/UserService.java`

**Fungsi**:
- `getAllUsers()` - GET /api/users
- `createUser(User user)` - POST /api/users
- `updateUser(Long id, User user)` - PUT /api/users/{id}
- `deleteUser(Long id)` - DELETE /api/users/{id}
- `searchUsers(String keyword)` - GET /api/users?search={keyword}

**Integration**:
- Update UserManagementController untuk menggunakan UserService
- Replace mock data dengan real API calls
- Add proper error handling
- Add loading indicators

**Benefit**: Admin bisa melakukan CRUD user langsung dari desktop app

---

### 2. **ExportService** - Implement PDF dan CSV Export ✅ **COMPLETED**

**File**: `desktop-app/src/main/java/com/smk/presensi/desktop/service/ExportService.java`

**Dependencies** (sudah ditambahkan di pom.xml):
```xml
<!-- PDF Generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>

<!-- CSV -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.0</version>
</dependency>
```

**Fungsi**:
- `exportToPdf(LocalDate startDate, LocalDate endDate, String filePath)` - Generate PDF report
- `exportToCsv(LocalDate startDate, LocalDate endDate, String filePath)` - Generate CSV report
- Date range picker dialog
- File chooser untuk save location
- Auto-open file after export

**PDF Content**:
- Header dengan logo dan judul "Laporan Presensi"
- Date range
- Table dengan columns: Tanggal, Username, Tipe, Status, Jam Masuk, Jam Pulang, Method
- Summary section: Total Hadir, Terlambat, Alpha, Persentase

**CSV Content**:
- Headers: Tanggal, Username, Tipe, Status, Jam Masuk, Jam Pulang, Method
- Data rows sesuai date range

**Integration**:
- Update DashboardController handleExportPdf() dan handleExportCsv()
- Add date range picker dialog
- Add progress indicator during export
- Show success notification with file path

**Benefit**: Admin/Guru bisa export laporan presensi untuk arsip atau analisis

---

### 3. **SettingsManager** - Persistent Settings dengan Java Preferences API ✅ PRIORITAS SEDANG

**File**: `desktop-app/src/main/java/com/smk/presensi/desktop/service/SettingsManager.java`

**Menggunakan**: `java.util.prefs.Preferences`

**Model**: `desktop-app/src/main/java/com/smk/presensi/desktop/model/AppSettings.java`

**Fields**:
```java
public class AppSettings {
    private String serverUrl;              // default: http://localhost:8081
    private int autoRefreshInterval;       // default: 30 (seconds)
    private boolean enableWebSocket;       // default: false (belum implemented)
    private boolean autoReconnect;         // default: true
    private boolean showNotifications;     // default: true
    private String defaultExportFormat;    // default: "PDF"
    private String defaultExportPath;      // default: user.home/Documents
    private String theme;                  // default: "Light"
    private int rowsPerPage;               // default: 50
    private boolean compactMode;           // default: false
    private int connectionTimeout;         // default: 10 (seconds)
    private int readTimeout;               // default: 30 (seconds)
    private boolean debugMode;             // default: false
    private boolean mockData;              // default: true
}
```

**Fungsi**:
- `loadSettings()` - Load from Preferences
- `saveSettings(AppSettings settings)` - Save to Preferences
- `resetToDefault()` - Reset all settings to default
- `getSettings()` - Get current settings
- Singleton pattern

**Integration**:
- SettingsController load/save settings
- DashboardController apply settings (server URL, refresh interval, mock data)
- ApiClient update base URL dari settings
- DashboardViewModel update refresh timer

**Benefit**: User bisa customize aplikasi sesuai kebutuhan dan settings akan persistent

---

### 4. **Improve Mock Data & API Toggle** ✅ **COMPLETED**

**Current State**: DashboardViewModel menggunakan mock data

**Goal**: Bisa switch antara mock data dan real API

**Implementation**:
- Add `useMockData` property di DashboardViewModel (bind ke checkbox)
- If mock: gunakan data dummy
- If not mock: call PresensiService.getPresensiToday()
- Add error handling untuk API failure
- Add loading indicator saat fetch data

**Files to Update**:
- `DashboardViewModel.java` - add API integration
- `PresensiService.java` - implement getTodayPresensi()
- `DashboardController.java` - handle loading states

**Benefit**: Development bisa pakai mock, production pakai real API

---

### 5. **Add User Form Dialog** ✅ **COMPLETED**

**File**: `desktop-app/src/main/java/com/smk/presensi/desktop/controller/UserFormDialog.java`

**FXML**: `desktop-app/src/main/resources/fxml/user-form-dialog.fxml`

**Fields**:
- Username (TextField)
- Email (TextField)
- Password (PasswordField) - only for create
- Nama (TextField)
- Role (ComboBox: ADMIN, USER)
- Tipe (ComboBox: GURU, SISWA)
- RFID Card ID (TextField)
- Enabled (CheckBox)

**Validation**:
- Username: required, min 3 chars
- Email: required, valid email format
- Password: required for create, min 6 chars
- Nama: required

**Buttons**:
- Save
- Cancel

**Integration**:
- UserManagementController handleAddUser() → show dialog
- UserManagementController handleEditUser() → show dialog with pre-filled data
- On save: call UserService create/update

**Benefit**: User-friendly form untuk menambah/edit user

---

### 6. **WebSocket Enhancement** (OPTIONAL - Untuk Future)

**Current State**: WebSocketService adalah stub/placeholder

**Challenge**: Jakarta WebSocket API implementation cukup kompleks

**Alternative untuk saat ini**:
- Tetap gunakan polling (auto-refresh 30 detik)
- WebSocket bisa ditambahkan di tahap berikutnya
- Untuk sekarang fokus ke fitur lain yang lebih prioritas

**Future Implementation**:
- Bisa gunakan Spring STOMP over WebSocket
- Atau SockJS client untuk fallback
- Atau library WebSocket client yang lebih user-friendly

---

## 📋 Implementation Checklist

### Tahap 1: Core Functionality ✅ **COMPLETED**
- [x] Implement UserService
  - [x] getAllUsers()
  - [x] createUser()
  - [x] updateUser()
  - [x] deleteUser()
  - [x] searchUsers()
- [x] Update UserManagementController
  - [x] Replace mock data dengan UserService
  - [x] Add loading indicators
  - [x] Add error handling
- [x] Create UserFormDialog
  - [x] Design FXML
  - [x] Implement controller
  - [x] Add validation
  - [x] Integrate with UserManagementController

### Tahap 2: Export Functionality ✅ **COMPLETED**
- [x] Implement ExportService
  - [x] exportToPdf() dengan iText
  - [x] exportToCsv() dengan Commons CSV
  - [x] Date range picker dialog
  - [x] File chooser
- [x] Update DashboardController
  - [x] handleExportPdf() implementation
  - [x] handleExportCsv() implementation
  - [x] Add progress indicators
  - [x] Show success notifications

### Tahap 3: Settings & Configuration ✅ **COMPLETED**
- [x] Create AppSettings model
- [x] Implement SettingsManager
  - [x] loadSettings()
  - [x] saveSettings()
  - [x] resetToDefault()
- [x] Update SettingsController
  - [x] Load settings on init
  - [x] Save settings on button click
  - [x] Reset to default
- [x] Apply Settings across app
  - [x] ApiClient update base URL
  - [x] DashboardViewModel update refresh timer
  - [x] Update mock data toggle

### Tahap 4: API Integration ✅ **COMPLETED**
- [x] Update DashboardViewModel
  - [x] Toggle mock/real data
  - [x] Implement real API calls
  - [x] Error handling
  - [x] Loading states
- [x] Update PresensiService
  - [x] getTodayPresensi()
  - [x] getPresensiByDateRange()
  - [ ] Add error handling

### Tahap 5: Testing & Polish
- [ ] Test login dengan backend H2
- [ ] Test CRUD operations di User Management
- [ ] Test export PDF & CSV
- [ ] Test settings persistence
- [ ] Test API toggle (mock vs real)
- [ ] Fix CSS warnings
- [ ] Add proper error messages
- [ ] Add success notifications
- [ ] Update README dengan cara penggunaan

---

## 🔧 Technical Stack

### Backend (Already Running)
- Spring Boot 3.2.5
- H2 Database (in-memory)
- Spring Security + JWT
- REST API
- Port: 8081

### Desktop App
- JavaFX 21
- Maven
- Java 17
- Libraries:
  - Gson (JSON parsing)
  - iText (PDF generation)
  - Commons CSV (CSV generation)
  - Java HTTP Client (API calls)
  - Java Preferences (settings storage)

---

## 📁 Project Structure

```
desktop-app/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/smk/presensi/desktop/
│       │   ├── DesktopApp.java
│       │   ├── controller/
│       │   │   ├── DashboardController.java ✅
│       │   │   ├── LoginController.java ✅
│       │   │   ├── UserManagementController.java ✅ (needs API integration)
│       │   │   ├── SettingsController.java ✅ (needs settings manager)
│       │   │   └── UserFormDialog.java ⏳ (to be created)
│       │   ├── model/
│       │   │   ├── User.java ✅
│       │   │   ├── Presensi.java ✅
│       │   │   ├── DashboardStats.java ✅
│       │   │   └── AppSettings.java ⏳ (to be created)
│       │   ├── service/
│       │   │   ├── ApiClient.java ✅
│       │   │   ├── SessionManager.java ✅
│       │   │   ├── PresensiService.java ✅ (needs enhancement)
│       │   │   ├── UserService.java ⏳ (to be created)
│       │   │   ├── ExportService.java ⏳ (to be created)
│       │   │   ├── SettingsManager.java ⏳ (to be created)
│       │   │   └── WebSocketService.java ✅ (placeholder)
│       │   └── viewmodel/
│       │       ├── DashboardViewModel.java ✅ (needs API toggle)
│       │       └── LoginViewModel.java ✅
│       └── resources/
│           ├── fxml/
│           │   ├── dashboard.fxml ✅
│           │   ├── login.fxml ✅
│           │   ├── user-management.fxml ✅
│           │   ├── settings.fxml ✅
│           │   └── user-form-dialog.fxml ⏳ (to be created)
│           └── css/
│               ├── dashboard.css ✅
│               └── login.css ✅ (has minor warning)
```

**Legend**:
- ✅ Done
- ⏳ To be created
- 🔧 Needs modification/enhancement

---

## 🎬 Next Actions

1. **Start with UserService** - Most important for User Management to work
2. **Create UserFormDialog** - Essential for CRUD operations
3. **Implement ExportService** - High value feature for users
4. **Create SettingsManager** - Improve UX with persistent settings
5. **Polish & Test** - Make sure everything works smoothly

---

## 💡 Notes

- **Database**: Menggunakan H2 in-memory, data akan hilang saat restart. Untuk production, gunakan PostgreSQL/MySQL.
- **WebSocket**: Untuk saat ini gunakan polling. WebSocket bisa ditambahkan di tahap lanjut.
- **Security**: JWT token sudah diimplement di backend. Desktop app menyimpan token di SessionManager.
- **Mock Data**: Sangat berguna untuk development tanpa perlu backend running.
- **Export**: PDF untuk report formal, CSV untuk analisis di Excel.

---

## 🚀 Deployment Notes (Future)

### Backend
- Package: `mvn clean package` → jar file
- Run: `java -jar presensi-backend.jar`
- Or Docker: `docker build -t presensi-backend .`

### Desktop App
- Package: `mvn clean package` → executable jar
- Run: `java -jar desktop-app.jar`
- Or native installer dengan jpackage

---

**End of Implementation Plan**
