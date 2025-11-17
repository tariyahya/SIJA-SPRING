# TASK 5 - IMPLEMENTASI RFID PRESENSI

## 🎯 TUJUAN TAHAP 5

Menambahkan fitur **RFID (Radio Frequency Identification)** untuk presensi otomatis tanpa perlu login manual.

**Yang akan dipelajari:**
- RFID concept: tap kartu → auto-checkin
- Public endpoint (no JWT authentication)
- Find user by rfidCardId (bukan username/password)
- Security whitelist untuk endpoint tertentu
- Simulasi RFID (manual input rfidCardId, nanti hardware real)

**Perbedaan dengan Tahap 4:**
| Aspek | Tahap 4 (Manual) | Tahap 5 (RFID) |
|-------|------------------|----------------|
| **Authentication** | ✅ Perlu login (JWT) | ❌ Tidak perlu login |
| **User identification** | Dari SecurityContext (who login) | Dari rfidCardId (tap kartu) |
| **Endpoint security** | @PreAuthorize required | permitAll (public) |
| **Client device** | Web/mobile app (authenticated) | RFID reader (hardware) |
| **Use case** | Checkin via HP/laptop | Tap kartu di mesin presensi |

---

## 📋 ANALOGI RFID

**Bayangkan:**

**Cara Lama (Manual - Tahap 4):**
1. Buka HP
2. Buka aplikasi presensi
3. Login dengan username & password
4. Klik tombol "Checkin"
5. ✅ Presensi tercatat

**Cara Baru (RFID - Tahap 5):**
1. Tap kartu RFID di mesin
2. ✅ Presensi langsung tercatat (tanpa login!)

**Kenapa bisa tanpa login?**
- Kartu RFID punya **ID unik** (contoh: `RF001234`)
- ID ini sudah tersimpan di database (field `rfidCardId` di tabel Siswa/Guru)
- Sistem cocokkan: rfidCardId di kartu = rfidCardId di database → ketemu user-nya!
- Auto-checkin atas nama user tersebut

**Analogi dunia nyata:**
- KTP punya NIK unik → identifikasi orang
- SIM punya nomor unik → identifikasi pengemudi
- RFID card punya rfidCardId unik → identifikasi siswa/guru

---

## 🏗️ ARSITEKTUR TAHAP 5

```
┌─────────────┐
│ RFID Reader │ (Hardware: baca kartu RFID)
└──────┬──────┘
       │ Kirim rfidCardId: "RF001234"
       ↓
┌─────────────────────────────────┐
│  POST /api/presensi/rfid/checkin│ (Public endpoint, no JWT)
└──────┬──────────────────────────┘
       │
       ↓
┌─────────────────────┐
│ RfidController      │ (Handle request)
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐
│ PresensiService     │ (Business logic)
│ - checkinRfid()     │
└──────┬──────────────┘
       │
       ├─→ UserRepository.findByRfidCardId() → Cari user
       │
       ├─→ PresensiRepository.existsByUserAndTanggal() → Cek duplikasi
       │
       └─→ PresensiRepository.save() → Insert presensi
```

**Flow lengkap:**
1. User tap kartu RFID → reader baca `rfidCardId`
2. Reader kirim POST request ke backend: `{ "rfidCardId": "RF001234" }`
3. Backend cari user dengan `rfidCardId = "RF001234"`
4. Jika ketemu → auto-checkin (insert record presensi)
5. Jika tidak ketemu → error "Kartu tidak terdaftar"

---

## 📋 STEP-BY-STEP IMPLEMENTATION

### STEP 1: Persiapan Database

**Pastikan field `rfidCardId` sudah ada di entity Siswa & Guru.**

Field ini **sudah dibuat di Tahap 2**, jadi tidak perlu tambah apa-apa!

**Cek di Siswa.java:**
```java
@Column(unique = true, length = 50)
private String rfidCardId;
```

**Cek di Guru.java:**
```java
@Column(unique = true, length = 50)
private String rfidCardId;
```

✅ **Sudah ada? Lanjut ke step berikutnya!**

---

### STEP 2: Buat RfidCheckinRequest DTO

**File:** `backend/src/main/java/com/smk/presensi/dto/presensi/RfidCheckinRequest.java`

```java
package com.smk.presensi.dto.presensi;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk request checkin via RFID.
 * 
 * Sangat sederhana: hanya butuh rfidCardId!
 * 
 * Perbedaan dengan CheckinRequest (manual):
 * - CheckinRequest: butuh tipe (SISWA/GURU), latitude, longitude, keterangan
 * - RfidCheckinRequest: HANYA butuh rfidCardId
 * 
 * Kenapa tidak butuh field lain?
 * - tipe: Auto-detect dari tabel mana user ditemukan (Siswa/Guru)
 * - latitude/longitude: RFID reader fixed di lokasi tertentu (tidak mobile)
 * - keterangan: Tidak perlu (checkin otomatis, tidak ada input manual)
 * 
 * Use case:
 * - RFID reader baca kartu → dapat rfidCardId
 * - Kirim POST request dengan rfidCardId ini
 * - Backend handle sisanya
 */
public record RfidCheckinRequest(
    /**
     * RFID Card ID - ID unik dari kartu RFID.
     * 
     * Format: Bebas (tergantung hardware)
     * Contoh: "RF001234", "1234567890", "A1B2C3D4"
     * 
     * @NotBlank: Wajib diisi, tidak boleh kosong
     */
    @NotBlank(message = "RFID Card ID harus diisi")
    String rfidCardId
) {}
```

---

### STEP 3: Tambah Method di UserRepository

Kita perlu method untuk cari user berdasarkan rfidCardId.

**File:** `backend/src/main/java/com/smk/presensi/repository/UserRepository.java`

```java
package com.smk.presensi.repository;

import com.smk.presensi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY USER - Interface untuk akses data user.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Cari user berdasarkan username.
     * 
     * Generated query:
     * SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * Cek apakah username sudah ada.
     * 
     * Generated query:
     * SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);
}
```

**TUNGGU!** Method `findByRfidCardId()` tidak bisa dibuat di UserRepository karena field `rfidCardId` ada di Siswa/Guru, **bukan di User**.

Jadi kita perlu cari di SiswaRepository dan GuruRepository!

---

### STEP 4: Tambah Method di SiswaRepository & GuruRepository

#### 4.1 Update SiswaRepository

**File:** `backend/src/main/java/com/smk/presensi/repository/SiswaRepository.java`

Tambahkan method baru:

```java
/**
 * Cari siswa berdasarkan RFID Card ID.
 * 
 * Generated query:
 * SELECT * FROM siswa WHERE rfid_card_id = ?
 * 
 * Use case:
 * - RFID checkin: cari siswa yang punya kartu ini
 * - Validasi kartu: apakah kartu ini terdaftar?
 * 
 * @param rfidCardId ID kartu RFID yang dicari
 * @return Optional<Siswa> (ada jika terdaftar, empty jika tidak)
 */
Optional<Siswa> findByRfidCardId(String rfidCardId);
```

#### 4.2 Update GuruRepository

**File:** `backend/src/main/java/com/smk/presensi/repository/GuruRepository.java`

Tambahkan method baru:

```java
/**
 * Cari guru berdasarkan RFID Card ID.
 * 
 * Generated query:
 * SELECT * FROM guru WHERE rfid_card_id = ?
 * 
 * Use case:
 * - RFID checkin: cari guru yang punya kartu ini
 * - Validasi kartu: apakah kartu ini terdaftar?
 * 
 * @param rfidCardId ID kartu RFID yang dicari
 * @return Optional<Guru> (ada jika terdaftar, empty jika tidak)
 */
Optional<Guru> findByRfidCardId(String rfidCardId);
```

---

### STEP 5: Update PresensiService - Tambah Method checkinRfid()

**File:** `backend/src/main/java/com/smk/presensi/service/PresensiService.java`

Tambahkan dependencies di constructor:

```java
private final SiswaRepository siswaRepository;
private final GuruRepository guruRepository;

public PresensiService(
        PresensiRepository presensiRepository,
        UserRepository userRepository,
        SiswaRepository siswaRepository,
        GuruRepository guruRepository
) {
    this.presensiRepository = presensiRepository;
    this.userRepository = userRepository;
    this.siswaRepository = siswaRepository;
    this.guruRepository = guruRepository;
}
```

Tambahkan method baru di akhir class:

```java
/**
 * CHECKIN RFID - Checkin via tap kartu RFID.
 * 
 * Perbedaan dengan checkin() manual:
 * - Tidak perlu authentication (no JWT)
 * - Tidak ambil user dari SecurityContext
 * - Cari user berdasarkan rfidCardId
 * - Auto-detect tipe (SISWA/GURU) dari tabel yang ditemukan
 * 
 * Alur:
 * 1. Cari rfidCardId di tabel Siswa → jika ada, ambil User-nya
 * 2. Jika tidak ada, cari di tabel Guru
 * 3. Jika kedua-duanya tidak ada → error "Kartu tidak terdaftar"
 * 4. Validasi: sudah checkin hari ini atau belum?
 * 5. Insert presensi baru
 * 6. Set method = RFID (bukan MANUAL)
 * 
 * @param request RfidCheckinRequest (berisi rfidCardId)
 * @return PresensiResponse
 * @throws RuntimeException jika kartu tidak terdaftar atau sudah checkin
 */
@Transactional
public PresensiResponse checkinRfid(RfidCheckinRequest request) {
    String rfidCardId = request.rfidCardId();
    
    // 1. Cari user berdasarkan rfidCardId
    User user = null;
    TipeUser tipe = null;
    
    // Cari di tabel Siswa dulu
    Optional<Siswa> siswaOpt = siswaRepository.findByRfidCardId(rfidCardId);
    if (siswaOpt.isPresent()) {
        Siswa siswa = siswaOpt.get();
        user = siswa.getUser(); // Ambil User dari relasi OneToOne
        tipe = TipeUser.SISWA;
    } else {
        // Jika tidak ada di Siswa, cari di tabel Guru
        Optional<Guru> guruOpt = guruRepository.findByRfidCardId(rfidCardId);
        if (guruOpt.isPresent()) {
            Guru guru = guruOpt.get();
            user = guru.getUser(); // Ambil User dari relasi OneToOne
            tipe = TipeUser.GURU;
        }
    }
    
    // Jika tidak ketemu di Siswa maupun Guru
    if (user == null) {
        throw new RuntimeException("Kartu RFID tidak terdaftar: " + rfidCardId);
    }
    
    // 2. Validasi duplikasi
    LocalDate today = LocalDate.now();
    if (presensiRepository.existsByUserAndTanggal(user, today)) {
        throw new RuntimeException("User dengan kartu " + rfidCardId + " sudah checkin hari ini");
    }
    
    // 3. Buat record presensi baru
    Presensi presensi = new Presensi();
    presensi.setUser(user);
    presensi.setTipe(tipe); // SISWA atau GURU (auto-detect)
    presensi.setTanggal(today);
    
    LocalTime now = LocalTime.now();
    presensi.setJamMasuk(now);
    
    // 4. Hitung status (HADIR/TERLAMBAT)
    presensi.setStatus(hitungStatus(now));
    
    // 5. Set method = RFID (bukan MANUAL)
    presensi.setMethod(MethodPresensi.RFID);
    
    // GPS tidak ada (RFID reader fixed location)
    presensi.setLatitude(null);
    presensi.setLongitude(null);
    
    // Keterangan otomatis
    presensi.setKeterangan("Checkin via RFID: " + rfidCardId);
    
    // 6. Save
    Presensi saved = presensiRepository.save(presensi);
    
    // 7. Convert ke DTO
    return toResponse(saved);
}
```

---

### STEP 6: Buat RfidController

**File:** `backend/src/main/java/com/smk/presensi/controller/RfidController.java`

```java
package com.smk.presensi.controller;

import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.dto.presensi.RfidCheckinRequest;
import com.smk.presensi.service.PresensiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER RFID - REST API endpoints untuk RFID presensi.
 * 
 * Base URL: /api/presensi/rfid
 * 
 * PENTING:
 * - Endpoints ini PUBLIC (tidak perlu JWT authentication)
 * - Karena RFID reader tidak bisa login (hardware, bukan user)
 * - Autentikasi dilakukan via rfidCardId (unik per kartu)
 * 
 * Security:
 * - Endpoint /api/presensi/rfid/** di-whitelist di SecurityConfig
 * - Siapa pun bisa akses (permitAll)
 * - Validasi dilakukan di level bisnis: rfidCardId harus terdaftar
 * 
 * Simulasi RFID:
 * - Tahap 5 ini: Input rfidCardId manual via Postman/curl (simulasi)
 * - Tahap nanti: Hardware RFID reader real yang kirim request otomatis
 */
@RestController
@RequestMapping("/api/presensi/rfid")
public class RfidController {

    private final PresensiService presensiService;

    public RfidController(PresensiService presensiService) {
        this.presensiService = presensiService;
    }

    /**
     * ENDPOINT: POST /api/presensi/rfid/checkin
     * 
     * Checkin via tap kartu RFID.
     * 
     * Access: PUBLIC (no JWT required)
     * 
     * Request body:
     * {
     *   "rfidCardId": "RF001234"
     * }
     * 
     * Response: PresensiResponse
     * 
     * Success response (200 OK):
     * {
     *   "id": 1,
     *   "userId": 5,
     *   "username": "budi",
     *   "tipe": "SISWA",
     *   "tanggal": "2024-01-15",
     *   "jamMasuk": "07:05:30",
     *   "jamPulang": null,
     *   "status": "HADIR",
     *   "method": "RFID",
     *   "latitude": null,
     *   "longitude": null,
     *   "keterangan": "Checkin via RFID: RF001234"
     * }
     * 
     * Error response (400 Bad Request):
     * {
     *   "error": "Kartu RFID tidak terdaftar: RF001234"
     * }
     * 
     * {
     *   "error": "User dengan kartu RF001234 sudah checkin hari ini"
     * }
     * 
     * Use case:
     * 1. Siswa/guru tap kartu RFID di mesin presensi
     * 2. RFID reader baca rfidCardId dari kartu
     * 3. Reader kirim POST request ke endpoint ini
     * 4. Backend cari user dengan rfidCardId tersebut
     * 5. Jika ketemu → auto-checkin
     * 6. Jika tidak ketemu → error
     * 
     * Testing (Postman):
     * - Method: POST
     * - URL: http://localhost:8081/api/presensi/rfid/checkin
     * - Headers: Content-Type: application/json
     * - Body (raw JSON): { "rfidCardId": "RF001234" }
     * - NO NEED Authorization header (public endpoint)
     */
    @PostMapping("/checkin")
    public ResponseEntity<PresensiResponse> checkinRfid(@Valid @RequestBody RfidCheckinRequest request) {
        PresensiResponse response = presensiService.checkinRfid(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * ENDPOINT: GET /api/presensi/rfid/test
     * 
     * Test endpoint untuk cek apakah RFID endpoint accessible (public).
     * 
     * Access: PUBLIC (no JWT required)
     * 
     * Response: Plain text "RFID endpoint is working!"
     * 
     * Use case:
     * - Test koneksi dari RFID reader
     * - Cek apakah endpoint RFID sudah di-whitelist
     * 
     * Testing (browser/Postman):
     * - Method: GET
     * - URL: http://localhost:8081/api/presensi/rfid/test
     * - Expected: "RFID endpoint is working!" (tanpa error 401/403)
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("RFID endpoint is working!");
    }
}
```

---

### STEP 7: Update SecurityConfig - Whitelist RFID Endpoints

**File:** `backend/src/main/java/com/smk/presensi/security/SecurityConfig.java`

Update method `securityFilterChain()` untuk whitelist endpoint RFID:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            // Public endpoints (no JWT required)
            .requestMatchers("/api/auth/**").permitAll()          // Login, Register
            .requestMatchers("/h2-console/**").permitAll()        // H2 Console
            .requestMatchers("/api/presensi/rfid/**").permitAll() // RFID endpoints (NEW!)
            
            // Protected endpoints (JWT required)
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    // H2 Console support (frames)
    http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

    return http.build();
}
```

**Penjelasan:**
- `.requestMatchers("/api/presensi/rfid/**").permitAll()` → Semua endpoint yang dimulai dengan `/api/presensi/rfid/` bisa diakses tanpa JWT
- Hardware RFID reader tidak bisa login → jadi endpoint-nya harus public
- Autentikasi dilakukan di level bisnis: cek rfidCardId apakah terdaftar

---

### STEP 8: Update Import Statements

Pastikan import statement di **PresensiService.java** lengkap:

```java
import com.smk.presensi.dto.presensi.CheckinRequest;
import com.smk.presensi.dto.presensi.CheckoutRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.dto.presensi.RfidCheckinRequest; // NEW!
import com.smk.presensi.entity.Guru;                       // NEW!
import com.smk.presensi.entity.Presensi;
import com.smk.presensi.entity.Siswa;                      // NEW!
import com.smk.presensi.entity.User;
import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.enums.TipeUser;
import com.smk.presensi.repository.GuruRepository;         // NEW!
import com.smk.presensi.repository.PresensiRepository;
import com.smk.presensi.repository.SiswaRepository;        // NEW!
import com.smk.presensi.repository.UserRepository;
```

---

### STEP 9: Compile & Test

#### 9.1 Compile

```bash
cd backend
mvn clean compile
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Compiling 43 source files (was 41, now +2: RfidCheckinRequest, RfidController)
```

#### 9.2 Run Application

```bash
mvn spring-boot:run
```

Expected output:
```
Tomcat started on port 8081
Started PresensiApplication
```

#### 9.3 Test RFID Endpoint

**Test 1: Cek endpoint accessible (GET /test)**

```bash
curl http://localhost:8081/api/presensi/rfid/test
```

Expected: `"RFID endpoint is working!"`

**Test 2: Checkin dengan RFID (simulasi)**

Perlu data siswa/guru dengan rfidCardId terlebih dahulu!

**Cara 1: Update via SQL (H2 Console)**
1. Buka: http://localhost:8081/h2-console
2. Login (JDBC URL: `jdbc:h2:mem:presensidb`, User: `sa`, Password: kosong)
3. Run query:
```sql
-- Update rfidCardId untuk siswa dengan ID 1
UPDATE siswa SET rfid_card_id = 'RF001234' WHERE id = 1;

-- Atau untuk guru
UPDATE guru SET rfid_card_id = 'RF999888' WHERE id = 1;
```

**Cara 2: Update via API (Postman)**
- PUT request ke `/api/siswa/1` dengan body: `{ ..., "rfidCardId": "RF001234" }`

**Lalu test RFID checkin:**

```bash
curl -X POST http://localhost:8081/api/presensi/rfid/checkin \
  -H "Content-Type: application/json" \
  -d '{"rfidCardId":"RF001234"}'
```

Expected response (200 OK):
```json
{
  "id": 1,
  "userId": 5,
  "username": "budi",
  "tipe": "SISWA",
  "tanggal": "2024-01-15",
  "jamMasuk": "07:05:30",
  "jamPulang": null,
  "status": "HADIR",
  "method": "RFID",
  "latitude": null,
  "longitude": null,
  "keterangan": "Checkin via RFID: RF001234"
}
```

---

## 🎓 PEMBELAJARAN PENTING

### 1. Public Endpoints vs Protected Endpoints

**Protected (JWT required):**
```java
@PreAuthorize("hasRole('SISWA')")
@PostMapping("/checkin")
public ResponseEntity<...> checkin(...) {
    // User harus login dulu (kirim JWT token)
}
```

**Public (no JWT):**
```java
@PostMapping("/rfid/checkin")
public ResponseEntity<...> checkinRfid(...) {
    // Siapa pun bisa akses (hardware RFID reader)
}
```

**Whitelist di SecurityConfig:**
```java
.requestMatchers("/api/presensi/rfid/**").permitAll()
```

### 2. Find User by rfidCardId

**Challenge:**
- Field `rfidCardId` ada di Siswa & Guru (bukan di User)
- User punya relasi OneToOne dengan Siswa/Guru
- Perlu cari di 2 tabel (Siswa dulu, kalau tidak ada cari di Guru)

**Solution:**
```java
// Cari di Siswa
Optional<Siswa> siswaOpt = siswaRepository.findByRfidCardId(rfidCardId);
if (siswaOpt.isPresent()) {
    User user = siswaOpt.get().getUser(); // Ambil User via relasi
    tipe = TipeUser.SISWA;
}

// Kalau tidak ada, cari di Guru
Optional<Guru> guruOpt = guruRepository.findByRfidCardId(rfidCardId);
if (guruOpt.isPresent()) {
    User user = guruOpt.get().getUser();
    tipe = TipeUser.GURU;
}
```

### 3. Auto-detect TipeUser

**Manual checkin (Tahap 4):**
- Client kirim `tipe: "SISWA"` dalam request body
- Client harus tahu dia SISWA atau GURU

**RFID checkin (Tahap 5):**
- Client hanya kirim `rfidCardId`
- Server auto-detect: rfidCardId ditemukan di tabel Siswa → `tipe = SISWA`
- Server auto-detect: rfidCardId ditemukan di tabel Guru → `tipe = GURU`

### 4. Method Presensi

**Tahap 4:** `method = MANUAL` (semua checkin manual via app)
**Tahap 5:** `method = RFID` (checkin via tap kartu)
**Nanti:** `method = BARCODE`, `FACE`

Berguna untuk:
- Statistik: berapa % checkin via RFID vs manual?
- Audit: user ini checkin pakai cara apa?

### 5. Security Best Practices

**Whitelist endpoint specific:**
```java
// ✅ GOOD: Specific path
.requestMatchers("/api/presensi/rfid/**").permitAll()

// ❌ BAD: Too broad
.requestMatchers("/api/**").permitAll()
```

**Validasi di business layer:**
- Meskipun endpoint public, tetap ada validasi
- RfidCardId harus terdaftar di database
- Tidak boleh checkin 2x di hari yang sama

---

## 📊 STATISTIK TAHAP 5

**Files created/updated:**
- 1 DTO baru: RfidCheckinRequest
- 1 Controller baru: RfidController
- 1 Method baru di PresensiService: checkinRfid()
- 2 Method baru di repositories: findByRfidCardId() (Siswa & Guru)
- 1 Update SecurityConfig: whitelist RFID endpoints
- **Total: 6 changes**

**Lines of code:**
- RfidCheckinRequest: ~20 lines
- RfidController: ~80 lines
- PresensiService.checkinRfid(): ~80 lines
- Repository methods: ~20 lines
- SecurityConfig update: ~1 line
- **Total: ~200 lines** (excluding comments)

**Features implemented:**
- ✅ RFID checkin without JWT authentication
- ✅ Auto-detect user from rfidCardId
- ✅ Auto-detect tipe (SISWA/GURU) from table
- ✅ Public endpoint whitelist
- ✅ Method = RFID tracking
- ✅ Duplicate checkin validation
- ✅ Test endpoint for connectivity check

---

## 🔧 TROUBLESHOOTING

### Problem 1: 401 Unauthorized saat akses /api/presensi/rfid/checkin

**Cause:** Endpoint belum di-whitelist di SecurityConfig

**Solution:**
```java
.requestMatchers("/api/presensi/rfid/**").permitAll()
```

### Problem 2: "Kartu RFID tidak terdaftar"

**Cause:** Belum ada data siswa/guru dengan rfidCardId tersebut

**Solution:**
1. Buka H2 Console: http://localhost:8081/h2-console
2. Update siswa/guru:
```sql
UPDATE siswa SET rfid_card_id = 'RF001234' WHERE id = 1;
```

### Problem 3: rfidCardId null di database

**Cause:** Field nullable, belum diisi

**Solution:**
- Update via API (PUT /api/siswa/1)
- Atau via SQL (lihat di atas)

### Problem 4: User null di Siswa/Guru

**Cause:** Belum ada relasi OneToOne dengan User

**Solution:**
- Pastikan saat create Siswa/Guru, field `user` diset
- Atau update manual via SQL:
```sql
UPDATE siswa SET user_id = 2 WHERE id = 1;
```

---

## 🚀 NEXT STEPS

**Tahap 6 - Barcode/QR Integration:**
1. Generate unique barcode per user
2. Endpoint: POST /api/presensi/barcode/checkin
3. Scan barcode → auto-checkin
4. QR code support (same flow)

**Tahap 7 - Face Recognition:**
1. Upload foto wajah (enrollment)
2. Face matching algorithm
3. Endpoint: POST /api/presensi/face/checkin (with photo)
4. Liveness detection

**Tahap 8 - Real Hardware Integration:**
1. Connect real RFID reader (serial/USB/network)
2. Arduino/Raspberry Pi integration
3. WebSocket for real-time updates
4. LED/buzzer feedback

---

## 📚 REFERENSI

- **POSTMAN-TAHAP-05.md** - Testing guide RFID checkin
- **README-TAHAP-05.md** - Architecture overview Tahap 5
- **TASK-4.md** - Manual checkin (comparison)
- RFID Technology: https://en.wikipedia.org/wiki/Radio-frequency_identification
- Spring Security permitAll: https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html

---

## ✅ CHECKLIST COMPLETION

- [x] Step 1: Cek field rfidCardId di Siswa & Guru entity
- [x] Step 2: Buat RfidCheckinRequest DTO
- [x] Step 3: (Skip - UserRepository tidak perlu update)
- [x] Step 4: Tambah findByRfidCardId() di SiswaRepository & GuruRepository
- [x] Step 5: Tambah checkinRfid() method di PresensiService
- [x] Step 6: Buat RfidController dengan endpoint checkin & test
- [x] Step 7: Update SecurityConfig (whitelist RFID endpoints)
- [x] Step 8: Update import statements
- [x] Step 9: Compile, Run, Test

**Status: ✅ TAHAP 5 READY TO IMPLEMENT!**
