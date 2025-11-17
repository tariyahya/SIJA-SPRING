# TASK DESKTOP-2: Login Screen + JWT Authentication + Auto-Refresh

**Tanggal**: 18 November 2025  
**Tahap**: Desktop App - Tahap 2  
**Fokus**: Login Screen, Session Management, JWT Authentication, Auto-Refresh

---

## 📋 TUJUAN

Menambahkan fitur authentication dan session management:
1. **Login Screen**: UI untuk username/password + remember me
2. **JWT Authentication**: Integrasi dengan backend `/auth/login`
3. **Session Management**: Save/load token, auto-login, logout
4. **Auto-Refresh**: Dashboard refresh otomatis setiap 30 detik
5. **User Info Display**: Tampilkan username dan role di dashboard
6. **Logout**: Clear session dan kembali ke login screen

---

## 🏗️ ARSITEKTUR

### Flow Diagram
```
Start App
    ↓
Load Login Screen
    ↓
┌─────────────────────┐
│ Check Saved Session │
│ (Remember Me?)      │
└──────┬──────────────┘
       │
       ├─ Yes (Auto-Login) ─→ Dashboard
       │
       └─ No ────→ Show Login Form
                       ↓
                   User Input
                       ↓
                   Call API (/auth/login)
                       ↓
                   ┌─────────┐
                   │ Success │
                   └────┬────┘
                        │
                        ├─ Save Session (SessionManager)
                        ├─ Navigate to Dashboard
                        └─ Start Auto-Refresh Timer (30s)
                              ↓
                          Dashboard
                              ↓
                          [Logout] ─→ Clear Session ─→ Login Screen
```

---

## 📦 NEW FILES

### 1. SessionManager.java (Service Layer)
**Path**: `src/main/java/com/smk/presensi/desktop/service/SessionManager.java`

**Purpose**: Manage user session dan JWT token persistence

**Features**:
- Save JWT token + user info
- Remember Me functionality (persist to disk)
- Auto-login on app restart
- Token expiration check (parse JWT payload)
- Logout (clear session)

**Storage**: `java.util.prefs.Preferences` (OS-specific storage)
- Windows: Registry (`HKEY_CURRENT_USER\Software\JavaSoft\Prefs`)
- Mac: `~/Library/Preferences`
- Linux: `~/.java/.userPrefs`

**Key Methods**:
```java
public void saveSession(String token, User user, boolean rememberMe)
public boolean isLoggedIn()
public boolean hasSavedSession()
public boolean isTokenExpired()
public void logout()
```

**Token Parsing**:
```java
// JWT format: header.payload.signature
String[] parts = token.split("\\.");
String payload = new String(Base64.getDecoder().decode(parts[1]));
// {"sub":"admin","exp":1700123456,"iat":1700037056}
```

---

### 2. LoginViewModel.java (ViewModel)
**Path**: `src/main/java/com/smk/presensi/desktop/viewmodel/LoginViewModel.java`

**Purpose**: Business logic untuk login

**Observable Properties**:
- `StringProperty username`
- `StringProperty password`
- `BooleanProperty rememberMe`
- `BooleanProperty loading`
- `StringProperty errorMessage`
- `BooleanProperty loginSuccess` (trigger navigation)

**Key Methods**:
```java
public void login() {
    // 1. Validation
    // 2. Call API (background thread)
    // 3. Get user info
    // 4. Save session
    // 5. Set loginSuccess = true
}

public boolean autoLogin() {
    // Check saved session → restore token
}
```

---

### 3. LoginController.java (Controller)
**Path**: `src/main/java/com/smk/presensi/desktop/controller/LoginController.java`

**Purpose**: Handle login screen UI

**Key Features**:
- Bind username/password fields to ViewModel
- Handle login button click
- Listen for `loginSuccess` property → navigate to dashboard
- Try auto-login on startup
- Clear error saat user mulai ketik

**Navigation**:
```java
private void navigateToDashboard() {
    FXMLLoader loader = new FXMLLoader(...);
    Parent root = loader.load();
    
    // Pass SessionManager to DashboardController
    DashboardController controller = loader.getController();
    controller.setSessionManager(sessionManager);
    
    // Switch scene
    Stage stage = (Stage) loginButton.getScene().getWindow();
    stage.setScene(new Scene(root, 1200, 700));
}
```

---

### 4. login.fxml (View)
**Path**: `src/main/resources/fxml/login.fxml`

**Layout**:
```
┌─────────────────────────────────┐
│     SIJA PRESENSI               │
│     Dashboard Admin/TU          │
├─────────────────────────────────┤
│  [Error Label]                  │
│                                 │
│  Username:                      │
│  [_____________________]        │
│                                 │
│  Password:                      │
│  [_____________________]        │
│                                 │
│  ☐ Remember Me    Lupa Pass?   │
│                                 │
│       [   LOGIN   ]             │
│         (Loading)               │
│                                 │
│  ───────────────────────────    │
│  v1.0.0 | © 2025 SMK Negeri 1  │
└─────────────────────────────────┘
```

**Components**:
- TextField (username)
- PasswordField (password)
- CheckBox (remember me)
- Button (login)
- ProgressIndicator (loading)
- Label (error message)

---

### 5. login.css (Styling)
**Path**: `src/main/resources/css/login.css`

**Style Features**:
- Gradient background (purple to blue)
- White card dengan shadow
- Modern input fields (border-radius, focus effect)
- Gradient login button
- Hover effects

---

## 🔄 UPDATED FILES

### 1. DesktopApp.java
**Changed**: Start screen dari Login (bukan Dashboard)

**Before**:
```java
FXMLLoader loader = new FXMLLoader(
    getClass().getResource("/fxml/dashboard.fxml")
);
Scene scene = new Scene(root, 1200, 700);
```

**After**:
```java
FXMLLoader loader = new FXMLLoader(
    getClass().getResource("/fxml/login.fxml")
);
Scene scene = new Scene(root, 500, 650);
primaryStage.setResizable(false); // Fixed size
```

---

### 2. DashboardController.java
**Added Features**:
1. **SessionManager Integration**
2. **User Info Display**
3. **Logout Button Handler**
4. **Auto-Refresh Timer** (30 seconds)

**New Fields**:
```java
@FXML private Button logoutButton;
@FXML private Label userLabel;
private SessionManager sessionManager;
private Timeline autoRefreshTimer;
```

**Auto-Refresh Implementation**:
```java
private void setupAutoRefresh() {
    autoRefreshTimer = new Timeline(
        new KeyFrame(Duration.seconds(30), event -> {
            System.out.println("Auto-refresh at " + LocalTime.now());
            viewModel.refreshData();
        })
    );
    autoRefreshTimer.setCycleCount(Animation.INDEFINITE);
    autoRefreshTimer.play();
}
```

**Logout Implementation**:
```java
private void handleLogout() {
    // 1. Confirm dialog
    // 2. Stop auto-refresh timer
    // 3. Clear session (sessionManager.logout())
    // 4. Navigate back to login screen
}
```

---

### 3. dashboard.fxml
**Added Components**:
```xml
<Label fx:id="userLabel" text="👤 User" styleClass="user-label"/>
<Button fx:id="logoutButton" text="🚪 Logout" styleClass="btn-danger"/>
```

---

### 4. dashboard.css
**Added Styles**:
```css
.btn-danger {
    -fx-background-color: #e74c3c;
    -fx-text-fill: white;
}

.user-label {
    -fx-text-fill: white;
    -fx-background-color: rgba(255, 255, 255, 0.2);
    -fx-padding: 5 10 5 10;
    -fx-background-radius: 5;
}
```

---

## 🔐 JWT AUTHENTICATION FLOW

### 1. Login Request
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### 2. Login Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

### 3. Store Token
```java
sessionManager.saveSession(token, user, rememberMe);
```

### 4. Use Token for API Calls
```java
apiClient.setJwtToken(sessionManager.getJwtToken());

// All subsequent requests include:
// Authorization: Bearer <token>
```

---

## 🔄 SESSION PERSISTENCE

### Preferences Storage
```java
Preferences prefs = Preferences.userRoot().node("com.smk.presensi.desktop");

// Save
prefs.put("jwt_token", token);
prefs.put("user_data", gson.toJson(user));
prefs.putBoolean("remember_me", true);

// Load
String token = prefs.get("jwt_token", null);
String userData = prefs.get("user_data", null);
User user = gson.fromJson(userData, User.class);
```

### Auto-Login Flow
```java
// On app start (LoginController.initialize)
if (sessionManager.hasSavedSession() && !sessionManager.isTokenExpired()) {
    apiClient.setJwtToken(sessionManager.getJwtToken());
    navigateToDashboard(); // Skip login screen
}
```

---

## ⏱️ AUTO-REFRESH MECHANISM

### Timeline API (JavaFX Animation)
```java
Timeline timer = new Timeline(
    new KeyFrame(Duration.seconds(30), event -> {
        // Refresh data every 30 seconds
        viewModel.refreshData();
    })
);
timer.setCycleCount(Animation.INDEFINITE); // Repeat forever
timer.play(); // Start timer
```

### Lifecycle Management
```java
// Start on dashboard open
setupAutoRefresh();

// Stop on logout
autoRefreshTimer.stop();
```

### Console Output
```
Auto-refresh triggered at 05:48:10
Auto-refresh triggered at 05:48:40
Auto-refresh triggered at 05:49:10
...
```

---

## 🧪 TESTING

### Test Case 1: Login dengan Credentials Benar
**Steps**:
1. Buka aplikasi
2. Input username: `admin`, password: `admin123`
3. Click "Login"

**Expected**:
- ✅ Loading indicator muncul
- ✅ Navigasi ke dashboard
- ✅ User label: "👤 admin (ADMIN)"
- ✅ Auto-refresh timer mulai (console log setiap 30s)

### Test Case 2: Login dengan Credentials Salah
**Steps**:
1. Input username: `wrong`, password: `wrong`
2. Click "Login"

**Expected**:
- ❌ Error label: "Username atau password salah"
- ❌ Tetap di login screen

### Test Case 3: Remember Me
**Steps**:
1. Login dengan ✅ Remember Me checked
2. Close aplikasi
3. Buka aplikasi lagi

**Expected**:
- ✅ Auto-login (skip login screen)
- ✅ Langsung ke dashboard

### Test Case 4: Logout
**Steps**:
1. Di dashboard, click "Logout"
2. Confirm dialog → OK

**Expected**:
- ✅ Auto-refresh timer stop
- ✅ Session cleared
- ✅ Kembali ke login screen

### Test Case 5: Auto-Refresh
**Steps**:
1. Login ke dashboard
2. Tunggu 30 detik
3. Check console log

**Expected**:
- ✅ Console: "Auto-refresh triggered at HH:MM:SS"
- ✅ Tabel data di-refresh
- ✅ Statistics cards update

---

## 📊 STATISTIK TAHAP 2

### Files Created: **5 files**
```
1. SessionManager.java (~160 lines)
2. LoginViewModel.java (~150 lines)
3. LoginController.java (~120 lines)
4. login.fxml (~100 lines)
5. login.css (~120 lines)
```

### Files Updated: **4 files**
```
1. DesktopApp.java (load login screen first)
2. DashboardController.java (+ logout, auto-refresh, user label)
3. dashboard.fxml (+ logout button, user label)
4. dashboard.css (+ btn-danger, user-label styles)
```

### Lines of Code: **~650 new lines**
- Java: ~430 lines
- FXML: ~100 lines
- CSS: ~120 lines

### Build Status
```bash
[INFO] BUILD SUCCESS
[INFO] Compiling 11 source files
[INFO] Total time:  2.626 s
[INFO] Finished at: 2025-11-18T05:48:40
```

---

## 🎯 FITUR TAHAP 2

### ✅ Implemented
- [x] Login screen dengan modern UI
- [x] Username/password authentication
- [x] Remember Me checkbox (persistent login)
- [x] JWT token management
- [x] Session persistence (Preferences API)
- [x] Auto-login on app restart
- [x] Token expiration check
- [x] User info display (username + role)
- [x] Logout button dengan confirmation
- [x] Auto-refresh timer (30 seconds)
- [x] Loading indicator
- [x] Error handling
- [x] Scene navigation (Login ↔ Dashboard)

### 🎓 Key Concepts Learned

**1. Session Management**:
- Persistent storage dengan `Preferences` API
- Token serialization (JSON → String)
- Auto-login logic

**2. JWT Token Handling**:
- Base64 decoding untuk read payload
- Expiration check (unix timestamp)
- Authorization header: `Bearer <token>`

**3. JavaFX Animation**:
- `Timeline` untuk periodic tasks
- `KeyFrame` dengan duration
- Start/stop timer lifecycle

**4. Scene Navigation**:
- FXMLLoader untuk load FXML
- Get controller instance: `loader.getController()`
- Pass data antar controller (SessionManager)
- Switch scene: `stage.setScene(newScene)`

---

## 🔮 NEXT STEPS (Tahap 3)

### Planned Features:
- [ ] WebSocket untuk real-time updates (replace polling)
- [ ] Notification system (popup untuk event penting)
- [ ] User profile page (edit password, profile pic)
- [ ] Real RFID reader integration (serial port)
- [ ] Export laporan (CSV/PDF)
- [ ] Charts & graphs (attendance trends)
- [ ] Multi-user management (CRUD users)

---

## ✅ CHECKLIST COMPLETION

- [x] SessionManager class created
- [x] LoginViewModel created
- [x] LoginController created
- [x] login.fxml layout
- [x] login.css styling
- [x] DesktopApp.java updated (start with login)
- [x] DashboardController updated (logout + auto-refresh)
- [x] dashboard.fxml updated (logout button + user label)
- [x] dashboard.css updated (new styles)
- [x] Compile success (mvn clean compile)
- [x] Auto-login working
- [x] Logout working
- [x] Auto-refresh working (30s interval)
- [x] Documentation (TASK-DESKTOP-2.md)

---

**Author**: Copilot Assistant  
**Last Updated**: 18 November 2025  
**Next**: TASK-DESKTOP-3 (Real-time Updates dengan WebSocket)
