# Blog Desktop-2: Authentication, Session Management & Auto-Refresh

**Tanggal**: 18 November 2025  
**Penulis**: Tim Pengembang SIJA  
**Tahap**: Desktop App - Tahap 2  
**Topik**: Login, JWT, Session Persistence, Auto-Refresh Timer

---

## 🎯 PENDAHULUAN

Di Tahap 1, kita sudah membuat dashboard dengan mock data. Sekarang kita akan menambahkan **authentication system** yang proper dengan:
- **Login screen** untuk user authentication
- **JWT token management** untuk secure API calls
- **Session persistence** untuk remember me & auto-login
- **Auto-refresh** untuk update data otomatis

---

## 🔐 SESSION MANAGEMENT

### Mengapa Perlu Session Management?

Desktop app berbeda dengan web app:
- **Web**: Session di server (cookies, HTTP session)
- **Desktop**: Session di client (local storage, preferences)

**Kebutuhan**:
1. **Save JWT token** setelah login
2. **Auto-login** saat app restart (Remember Me)
3. **Persist user info** (username, role)
4. **Logout** → clear session

---

### Java Preferences API

**Preferences API** = Key-value storage yang persist di OS.

**Windows**: `HKEY_CURRENT_USER\Software\JavaSoft\Prefs`  
**Mac**: `~/Library/Preferences`  
**Linux**: `~/.java/.userPrefs`

**Usage**:
```java
// Get preferences node
Preferences prefs = Preferences.userRoot().node("com.smk.presensi.desktop");

// Save
prefs.put("jwt_token", token);
prefs.put("username", "admin");
prefs.putBoolean("remember_me", true);

// Load
String token = prefs.get("jwt_token", null);
boolean rememberMe = prefs.getBoolean("remember_me", false);

// Delete
prefs.remove("jwt_token");
```

**Keuntungan**:
- ✅ Cross-platform (otomatis pilih storage mechanism)
- ✅ Type-safe (putInt, putBoolean, dll)
- ✅ Auto-persist (no need to call save())
- ✅ Secure (user-specific storage)

---

### SessionManager Implementation

```java
public class SessionManager {
    private final Preferences prefs;
    private String jwtToken;
    private User currentUser;
    private boolean rememberMe;
    
    public void saveSession(String token, User user, boolean rememberMe) {
        this.jwtToken = token;
        this.currentUser = user;
        this.rememberMe = rememberMe;
        
        if (rememberMe) {
            // Persist to disk
            prefs.put("jwt_token", token);
            prefs.put("user_data", gson.toJson(user));
            prefs.putBoolean("remember_me", true);
        } else {
            // Session only (RAM)
            prefs.remove("jwt_token");
            prefs.remove("user_data");
        }
    }
    
    public void logout() {
        this.jwtToken = null;
        this.currentUser = null;
        prefs.remove("jwt_token");
        prefs.remove("user_data");
    }
}
```

---

## 🎫 JWT TOKEN HANDLING

### Struktur JWT

JWT (JSON Web Token) format:
```
header.payload.signature
```

**Example**:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJhZG1pbiIsImV4cCI6MTcwMDEyMzQ1Nn0.
sig...
```

**Header** (Base64):
```json
{"alg":"HS256","typ":"JWT"}
```

**Payload** (Base64):
```json
{"sub":"admin","exp":1700123456,"iat":1700037056}
```

---

### Decode JWT Payload

```java
public boolean isTokenExpired() {
    if (jwtToken == null) return true;
    
    // Split by "."
    String[] parts = jwtToken.split("\\.");
    if (parts.length != 3) return true;
    
    // Decode payload (Base64)
    String payload = new String(Base64.getDecoder().decode(parts[1]));
    
    // Parse JSON: {"exp":1700123456,...}
    int expIndex = payload.indexOf("\"exp\":");
    String expStr = payload.substring(expIndex + 6);
    long exp = Long.parseLong(expStr.split(",")[0].trim());
    
    // Compare dengan current time
    long now = System.currentTimeMillis() / 1000;
    return now > exp;
}
```

**Note**: Ini simple parsing tanpa verify signature. Untuk production, gunakan library seperti `jjwt` atau `java-jwt`.

---

## 🔄 SCENE NAVIGATION

### Switch Scene

```java
// Load new FXML
FXMLLoader loader = new FXMLLoader(
    getClass().getResource("/fxml/dashboard.fxml")
);
Parent root = loader.load();

// Get controller
DashboardController controller = loader.getController();
controller.setSessionManager(sessionManager); // Pass data

// Get current stage
Stage stage = (Stage) currentButton.getScene().getWindow();

// Set new scene
Scene scene = new Scene(root, 1200, 700);
stage.setScene(scene);
stage.setTitle("Dashboard");
```

**Key Points**:
1. **FXMLLoader.load()** → create scene graph + instantiate controller
2. **loader.getController()** → get controller instance untuk pass data
3. **button.getScene().getWindow()** → get current Stage
4. **stage.setScene()** → switch scene

---

## ⏱️ AUTO-REFRESH DENGAN TIMELINE

### JavaFX Timeline API

**Timeline** = Animation API untuk periodic tasks.

**Example**:
```java
Timeline timer = new Timeline(
    new KeyFrame(Duration.seconds(30), event -> {
        // Executed every 30 seconds
        viewModel.refreshData();
    })
);
timer.setCycleCount(Animation.INDEFINITE); // Repeat forever
timer.play(); // Start
```

**Lifecycle**:
```java
// Start timer
timer.play();

// Pause timer
timer.pause();

// Stop timer (cannot resume)
timer.stop();

// Check status
timer.getStatus(); // RUNNING, PAUSED, STOPPED
```

---

### Auto-Refresh Implementation

```java
public class DashboardController {
    private Timeline autoRefreshTimer;
    
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
    
    private void handleLogout() {
        // Stop timer on logout
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        // ... navigate to login
    }
}
```

---

## 🎨 UI/UX BEST PRACTICES

### 1. Loading State

```java
// Show loading
loadingIndicator.setVisible(true);
loginButton.setDisable(true);

// Hide loading
loadingIndicator.setVisible(false);
loginButton.setDisable(false);
```

**Better**: Bind ke ViewModel property
```java
loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
loginButton.disableProperty().bind(viewModel.loadingProperty());
```

---

### 2. Error Handling

```java
// Show error
errorLabel.setText("Username atau password salah");
errorLabel.setVisible(true);

// Auto-hide error saat user ketik
usernameField.textProperty().addListener((obs, old, newVal) -> {
    if (!newVal.isEmpty()) {
        errorLabel.setVisible(false);
    }
});
```

---

### 3. Enter Key untuk Submit

```java
// Enter di password field = login
passwordField.setOnAction(event -> handleLogin());

// Atau set button sebagai defaultButton di FXML
<Button defaultButton="true" text="Login"/>
```

---

### 4. Confirmation Dialog

```java
Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
alert.setTitle("Logout");
alert.setHeaderText("Konfirmasi Logout");
alert.setContentText("Apakah Anda yakin?");

alert.showAndWait().ifPresent(response -> {
    if (response == ButtonType.OK) {
        // User clicked OK
        performLogout();
    }
});
```

---

## 🔍 DEBUGGING TIPS

### 1. Console Logging

```java
System.out.println("Login attempt: " + username);
System.out.println("Token: " + token);
System.out.println("Auto-refresh at: " + LocalTime.now());
```

### 2. Check Preferences

**Windows**: Run `regedit` → Navigate to:
```
HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\smk\presensi\desktop
```

**Mac/Linux**:
```bash
# Mac
cd ~/Library/Preferences
cat com.smk.presensi.desktop.plist

# Linux
cd ~/.java/.userPrefs/com/smk/presensi/desktop
cat prefs.xml
```

### 3. Network Debugging

```java
HttpResponse<String> response = apiClient.post(...);
System.out.println("Status: " + response.statusCode());
System.out.println("Body: " + response.body());
```

---

## ⚡ PERFORMANCE TIPS

### 1. Background Threading

```java
// ❌ BAD: Block UI thread
List<Data> data = apiService.fetchData(); // 2 seconds...

// ✅ GOOD: Background thread
new Thread(() -> {
    List<Data> data = apiService.fetchData();
    Platform.runLater(() -> updateUI(data));
}).start();
```

### 2. Debounce Auto-Refresh

```java
// Pause refresh saat user sedang interaksi
@FXML
private void handleUserInput() {
    autoRefreshTimer.pause();
    
    // Resume setelah 5 detik idle
    PauseTransition delay = new PauseTransition(Duration.seconds(5));
    delay.setOnFinished(event -> autoRefreshTimer.play());
    delay.playFromStart();
}
```

---

## 🎓 KESIMPULAN

Tahap 2 menambahkan:
1. ✅ **Login Screen**: Modern UI dengan gradient background
2. ✅ **Session Management**: Persistent login dengan Preferences API
3. ✅ **JWT Handling**: Token storage, expiration check, auto-login
4. ✅ **Scene Navigation**: Smooth transition Login ↔ Dashboard
5. ✅ **Auto-Refresh**: Timeline API untuk periodic updates
6. ✅ **Logout**: Clear session dan kembali ke login

**Key Learnings**:
- Preferences API untuk persistent storage
- JWT token parsing (Base64 decode)
- Timeline untuk periodic tasks
- Scene navigation dengan FXMLLoader
- Data passing antar controller

**Next**: Tahap 3 akan implement WebSocket untuk real-time updates (menggantikan polling).

---

**Author**: Tim Pengembang SIJA  
**Last Updated**: 18 November 2025  
**License**: Educational Use Only
