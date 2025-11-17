# TASK 6 - IMPLEMENTASI BARCODE/QR CODE PRESENSI

## 🎯 TUJUAN TAHAP 6

Menambahkan fitur **Barcode/QR Code** untuk presensi otomatis, mirip dengan RFID tapi menggunakan barcode yang bisa discan.

**Yang akan dipelajari:**
- Barcode/QR Code concept: scan barcode → auto-checkin
- Reuse pattern dari RFID (public endpoint, auto-detect user)
- Field barcodeId di Siswa/Guru entity
- Barcode generation (bonus: generate QR code per user)

**Perbedaan RFID vs Barcode:**
| Aspek | RFID (Tahap 5) | Barcode (Tahap 6) |
|-------|----------------|-------------------|
| **Hardware** | RFID reader (tap kartu) | Scanner/camera (scan barcode) |
| **Teknologi** | Radio frequency | Visual (1D/2D barcode) |
| **Field** | rfidCardId | barcodeId |
| **Use case** | Tap kartu di mesin | Scan barcode/QR di ID card |
| **Device** | Dedicated RFID reader | Mobile camera / handheld scanner |

---

## 📋 ANALOGI BARCODE

**Bayangkan:**

**RFID (Tahap 5):**
- Tap kartu → reader baca chip → dapat ID → checkin

**Barcode (Tahap 6):**
- Scan barcode di ID card → camera baca pattern → dapat ID → checkin

**Kesamaan:**
- Keduanya hanya **identifier** (ID unik per user)
- Keduanya tidak perlu login (public endpoint)
- Keduanya auto-detect user dari ID

**Perbedaan:**
- RFID: ID tersimpan di chip elektronik
- Barcode: ID tersimpan dalam bentuk visual (garis-garis atau kotak QR)

**Real world examples:**
- **1D Barcode:** Produk di supermarket (garis-garis vertikal)
- **QR Code:** Link website, e-ticket, menu restoran

---

## 🏗️ ARSITEKTUR TAHAP 6

### System Flow

```
┌──────────────────┐
│ Barcode Scanner  │ Camera/scanner baca barcode
│ (Mobile/Desktop) │
└────────┬─────────┘
         │ HTTP POST
         │ { "barcodeId": "BC123456" }
         ↓
┌───────────────────────────────┐
│ POST /api/presensi/barcode/   │ Public endpoint
│      checkin                   │ (no JWT)
└────────┬──────────────────────┘
         │
         ↓
┌──────────────────┐
│ BarcodeController│
└────────┬─────────┘
         │
         ↓
┌──────────────────────┐
│ PresensiService      │
│ - checkinBarcode()   │
└────────┬─────────────┘
         │
         ├──→ SiswaRepository.findByBarcodeId()
         │    ↓
         │    Found? → user = siswa.getUser(), tipe = SISWA
         │
         ├──→ GuruRepository.findByBarcodeId()
         │    ↓
         │    Found? → user = guru.getUser(), tipe = GURU
         │
         └──→ PresensiRepository.save() → method = BARCODE
```

**Persis sama dengan RFID, hanya ganti:**
- rfidCardId → barcodeId
- method = RFID → method = BARCODE

---

## 📋 STEP-BY-STEP IMPLEMENTATION

### STEP 1: Verifikasi Field barcodeId

Field `barcodeId` sudah ada di entity Siswa & Guru sejak Tahap 2.

**Cek di Siswa.java:**
```java
@Column(unique = true, length = 50)
private String barcodeId;
```

**Cek di Guru.java:**
```java
@Column(unique = true, length = 50)
private String barcodeId;
```

✅ **Sudah ada? Lanjut!**

---

### STEP 2: Buat BarcodeCheckinRequest DTO

**File:** `backend/src/main/java/com/smk/presensi/dto/presensi/BarcodeCheckinRequest.java`

```java
package com.smk.presensi.dto.presensi;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk request checkin via Barcode/QR Code.
 * 
 * Sama seperti RfidCheckinRequest, hanya ganti field name.
 * 
 * Barcode bisa berupa:
 * - 1D Barcode: CODE128, CODE39, EAN13, dll (contoh: BC123456)
 * - 2D QR Code: QR matrix (contoh: QR_SISWA_001)
 * 
 * Backend tidak peduli format barcode-nya, yang penting:
 * - Unique per user
 * - Tersimpan di database
 * 
 * Use case:
 * 1. Siswa/guru punya ID card dengan barcode printed
 * 2. Scanner/camera baca barcode → dapat barcodeId
 * 3. App kirim POST request dengan barcodeId
 * 4. Backend cari user dengan barcodeId tersebut
 * 5. Auto-checkin jika ditemukan
 */
public record BarcodeCheckinRequest(
    /**
     * Barcode ID - ID unik dari barcode/QR code.
     * 
     * Format: Bebas (tergantung sistem barcode yang dipakai)
     * Contoh 1D: "BC123456", "1234567890123"
     * Contoh QR: "QR_SISWA_001", "GURU_12345"
     * 
     * @NotBlank: Wajib diisi, tidak boleh kosong
     */
    @NotBlank(message = "Barcode ID harus diisi")
    String barcodeId
) {}
```

---

### STEP 3: Update SiswaRepository & GuruRepository

Tambah method `findByBarcodeId()` di kedua repository.

#### 3.1 Update SiswaRepository

**File:** `backend/src/main/java/com/smk/presensi/repository/SiswaRepository.java`

Tambahkan method:

```java
/**
 * FIND BY BARCODE ID - Cari siswa berdasarkan Barcode ID.
 * 
 * Generated SQL:
 * SELECT * FROM siswa WHERE barcode_id = ?
 * 
 * Use case:
 * - Barcode checkin: cari siswa yang punya barcode ini
 * - Validasi barcode: apakah barcode ini terdaftar?
 * 
 * @param barcodeId ID barcode yang dicari
 * @return Optional<Siswa> (ada jika terdaftar, empty jika tidak)
 */
Optional<Siswa> findByBarcodeId(String barcodeId);
```

#### 3.2 Update GuruRepository

**File:** `backend/src/main/java/com/smk/presensi/repository/GuruRepository.java`

Tambahkan method:

```java
/**
 * FIND BY BARCODE ID - Cari guru berdasarkan Barcode ID.
 * 
 * Generated SQL:
 * SELECT * FROM guru WHERE barcode_id = ?
 * 
 * Use case:
 * - Barcode checkin: cari guru yang punya barcode ini
 * - Validasi barcode: apakah barcode ini terdaftar?
 * 
 * @param barcodeId ID barcode yang dicari
 * @return Optional<Guru> (ada jika terdaftar, empty jika tidak)
 */
Optional<Guru> findByBarcodeId(String barcodeId);
```

---

### STEP 4: Update PresensiService - Tambah Method checkinBarcode()

**File:** `backend/src/main/java/com/smk/presensi/service/PresensiService.java`

**Tambahkan import:**
```java
import com.smk.presensi.dto.presensi.BarcodeCheckinRequest;
```

**Tambahkan method baru di akhir class:**

```java
/**
 * CHECKIN BARCODE - Checkin via scan barcode/QR code.
 * 
 * Logic identik dengan checkinRfid(), hanya ganti:
 * - rfidCardId → barcodeId
 * - method = RFID → method = BARCODE
 * 
 * Alur:
 * 1. Cari barcodeId di tabel Siswa → jika ada, ambil User-nya
 * 2. Jika tidak ada, cari di tabel Guru
 * 3. Jika kedua-duanya tidak ada → error "Barcode tidak terdaftar"
 * 4. Validasi: sudah checkin hari ini atau belum?
 * 5. Insert presensi baru dengan method = BARCODE
 * 
 * @param request BarcodeCheckinRequest (berisi barcodeId)
 * @return PresensiResponse
 * @throws RuntimeException jika barcode tidak terdaftar atau sudah checkin
 */
@Transactional
public PresensiResponse checkinBarcode(BarcodeCheckinRequest request) {
    String barcodeId = request.barcodeId();
    
    // 1. Cari user berdasarkan barcodeId
    User user = null;
    TipeUser tipe = null;
    
    // Cari di tabel Siswa dulu
    Optional<Siswa> siswaOpt = siswaRepository.findByBarcodeId(barcodeId);
    if (siswaOpt.isPresent()) {
        Siswa siswa = siswaOpt.get();
        user = siswa.getUser(); // Ambil User dari relasi OneToOne
        tipe = TipeUser.SISWA;
    } else {
        // Jika tidak ada di Siswa, cari di tabel Guru
        Optional<Guru> guruOpt = guruRepository.findByBarcodeId(barcodeId);
        if (guruOpt.isPresent()) {
            Guru guru = guruOpt.get();
            user = guru.getUser(); // Ambil User dari relasi OneToOne
            tipe = TipeUser.GURU;
        }
    }
    
    // Jika tidak ketemu di Siswa maupun Guru
    if (user == null) {
        throw new RuntimeException("Barcode tidak terdaftar: " + barcodeId);
    }
    
    // 2. Validasi duplikasi
    LocalDate today = LocalDate.now();
    if (presensiRepository.existsByUserAndTanggal(user, today)) {
        throw new RuntimeException("User dengan barcode " + barcodeId + " sudah checkin hari ini");
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
    
    // 5. Set method = BARCODE (bukan MANUAL atau RFID)
    presensi.setMethod(MethodPresensi.BARCODE);
    
    // GPS tidak ada (barcode scanner bisa mobile tapi kita skip GPS dulu)
    presensi.setLatitude(null);
    presensi.setLongitude(null);
    
    // Keterangan otomatis
    presensi.setKeterangan("Checkin via Barcode: " + barcodeId);
    
    // 6. Save
    Presensi saved = presensiRepository.save(presensi);
    
    // 7. Convert ke DTO
    return toResponse(saved);
}
```

---

### STEP 5: Buat BarcodeController

**File:** `backend/src/main/java/com/smk/presensi/controller/BarcodeController.java`

```java
package com.smk.presensi.controller;

import com.smk.presensi.dto.presensi.BarcodeCheckinRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.service.PresensiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER BARCODE - REST API endpoints untuk Barcode/QR Code presensi.
 * 
 * Base URL: /api/presensi/barcode
 * 
 * Pattern sama dengan RfidController:
 * - Public endpoint (no JWT)
 * - Auto-detect user from barcodeId
 * - Method = BARCODE
 * 
 * Barcode vs RFID:
 * - RFID: Tap kartu (radio frequency)
 * - Barcode: Scan visual pattern (camera/scanner)
 * - Keduanya: Identifier untuk auto-checkin
 */
@RestController
@RequestMapping("/api/presensi/barcode")
public class BarcodeController {

    private final PresensiService presensiService;

    public BarcodeController(PresensiService presensiService) {
        this.presensiService = presensiService;
    }

    /**
     * ENDPOINT: POST /api/presensi/barcode/checkin
     * 
     * Checkin via scan barcode/QR code.
     * 
     * Access: PUBLIC (no JWT required)
     * 
     * Request body:
     * {
     *   "barcodeId": "BC123456"
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
     *   "tanggal": "2025-11-17",
     *   "jamMasuk": "07:05:30",
     *   "jamPulang": null,
     *   "status": "HADIR",
     *   "method": "BARCODE",
     *   "latitude": null,
     *   "longitude": null,
     *   "keterangan": "Checkin via Barcode: BC123456"
     * }
     * 
     * Error response (500 Internal Server Error):
     * {
     *   "error": "Barcode tidak terdaftar: BC123456"
     * }
     * 
     * {
     *   "error": "User dengan barcode BC123456 sudah checkin hari ini"
     * }
     * 
     * Use case:
     * 1. Siswa/guru punya ID card dengan barcode printed
     * 2. Mobile app scan barcode via camera
     * 3. App kirim POST request ke endpoint ini
     * 4. Backend cari user dengan barcodeId tersebut
     * 5. Jika ketemu → auto-checkin dengan method=BARCODE
     * 6. Jika tidak ketemu → error
     * 
     * Testing (Postman):
     * - Method: POST
     * - URL: http://localhost:8081/api/presensi/barcode/checkin
     * - Headers: Content-Type: application/json
     * - Body (raw JSON): { "barcodeId": "BC123456" }
     * - NO NEED Authorization header (public endpoint)
     */
    @PostMapping("/checkin")
    public ResponseEntity<PresensiResponse> checkinBarcode(@Valid @RequestBody BarcodeCheckinRequest request) {
        PresensiResponse response = presensiService.checkinBarcode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * ENDPOINT: GET /api/presensi/barcode/test
     * 
     * Test endpoint untuk cek apakah Barcode endpoint accessible (public).
     * 
     * Access: PUBLIC (no JWT required)
     * 
     * Response: Plain text "Barcode endpoint is working!"
     * 
     * Use case:
     * - Test koneksi dari barcode scanner app
     * - Cek apakah endpoint barcode sudah di-whitelist
     * 
     * Testing (browser/Postman):
     * - Method: GET
     * - URL: http://localhost:8081/api/presensi/barcode/test
     * - Expected: "Barcode endpoint is working!" (tanpa error 401/403)
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Barcode endpoint is working!");
    }
}
```

---

### STEP 6: Update SecurityConfig - Whitelist Barcode Endpoints

**File:** `backend/src/main/java/com/smk/presensi/security/SecurityConfig.java`

Update method `filterChain()`:

```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints (tidak perlu authentication)
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/h2-console/**").permitAll()
    .requestMatchers("/api/hello").permitAll()
    .requestMatchers("/api/presensi/rfid/**").permitAll()     // RFID endpoints
    .requestMatchers("/api/presensi/barcode/**").permitAll()  // BARCODE endpoints (NEW!)
    
    // Semua endpoint lain PERLU authentication
    .anyRequest().authenticated()
)
```

---

### STEP 7: Compile & Test

#### 7.1 Compile

```bash
cd backend
mvn clean compile
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Compiling 45 source files (was 43, now +2: BarcodeCheckinRequest, BarcodeController)
```

#### 7.2 Run Application

```bash
mvn spring-boot:run
```

#### 7.3 Test dengan Postman

**Persiapan data:**

```sql
-- H2 Console: http://localhost:8081/h2-console
UPDATE siswa SET barcode_id = 'BC123456' WHERE id = 1;
UPDATE guru SET barcode_id = 'BC999888' WHERE id = 1;
```

**Test barcode checkin:**

```bash
curl -X POST http://localhost:8081/api/presensi/barcode/checkin \
  -H "Content-Type: application/json" \
  -d '{"barcodeId":"BC123456"}'
```

Expected response:
```json
{
  "id": 1,
  "userId": 2,
  "username": "budi",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:05:30",
  "status": "HADIR",
  "method": "BARCODE",
  "keterangan": "Checkin via Barcode: BC123456"
}
```

---

## 🎓 PEMBELAJARAN PENTING

### 1. Pattern Reusability

**RFID (Tahap 5):**
- findByRfidCardId()
- checkinRfid()
- RfidController
- method = RFID

**Barcode (Tahap 6):**
- findByBarcodeId()
- checkinBarcode()
- BarcodeController
- method = BARCODE

**Pattern sama, hanya ganti nama field!**

### 2. Method Enum Usage

```java
public enum MethodPresensi {
    MANUAL,   // Tahap 4: Login via app
    RFID,     // Tahap 5: Tap kartu
    BARCODE,  // Tahap 6: Scan barcode
    FACE      // Tahap 7: Face recognition (next)
}
```

**Benefits:**
- Track cara checkin
- Statistik per method
- Audit trail
- Troubleshooting

### 3. Barcode Types

**1D Barcode (Linear):**
- CODE128, CODE39, EAN13, UPC
- 1 dimensi (horizontal lines)
- Capacity: ~20-30 characters
- Use case: Product barcode, inventory

**2D Barcode (QR Code):**
- QR Code, Data Matrix, PDF417
- 2 dimensi (matrix/boxes)
- Capacity: ~4000 characters
- Use case: URL, vCard, payment, ID card

**Untuk presensi:**
- 1D: Cukup untuk NIS/NIP (10-20 digit)
- QR: Bisa simpan data tambahan (nama, kelas, foto URL)

### 4. Barcode Generation (Bonus)

**Nanti bisa tambah endpoint:**
```java
@GetMapping("/barcode/generate/{userId}")
public ResponseEntity<byte[]> generateBarcode(@PathVariable Long userId) {
    // Use library: ZXing (Zebra Crossing)
    // Generate barcode image
    // Return as PNG/JPEG
}
```

**Library:**
- ZXing (Java): https://github.com/zxing/zxing
- Barcode4j: http://barcode4j.sourceforge.net/

---

## 📊 STATISTIK TAHAP 6

**Files created/updated:**
- 1 DTO baru: BarcodeCheckinRequest
- 1 Controller baru: BarcodeController
- 1 Method baru di PresensiService: checkinBarcode()
- 2 Method baru di repositories: findByBarcodeId() (Siswa & Guru)
- 1 Update SecurityConfig: whitelist barcode endpoints
- **Total: 6 changes** (sama dengan RFID)

**Lines of code:**
- BarcodeCheckinRequest: ~20 lines
- BarcodeController: ~80 lines
- PresensiService.checkinBarcode(): ~80 lines
- Repository methods: ~20 lines
- SecurityConfig update: ~1 line
- **Total: ~200 lines** (sama dengan RFID)

**Features implemented:**
- ✅ Barcode checkin without JWT authentication
- ✅ Auto-detect user from barcodeId
- ✅ Auto-detect tipe (SISWA/GURU) from table
- ✅ Public endpoint whitelist
- ✅ Method = BARCODE tracking
- ✅ Duplicate checkin validation
- ✅ Test endpoint for connectivity check

---

## 🔧 TROUBLESHOOTING

### Problem 1: 401 Unauthorized saat akses /api/presensi/barcode/checkin

**Cause:** Endpoint belum di-whitelist di SecurityConfig

**Solution:**
```java
.requestMatchers("/api/presensi/barcode/**").permitAll()
```

### Problem 2: "Barcode tidak terdaftar"

**Cause:** Belum ada data siswa/guru dengan barcodeId tersebut

**Solution:**
```sql
UPDATE siswa SET barcode_id = 'BC123456' WHERE id = 1;
```

### Problem 3: barcodeId null di database

**Cause:** Field nullable, belum diisi

**Solution:**
- Update via H2 Console (SQL)
- Atau via API (PUT /api/siswa/1)

---

## 🚀 NEXT STEPS

**Tahap 7 - Face Recognition:**
1. Upload foto wajah (enrollment)
2. Endpoint: POST /api/presensi/face/checkin (with image)
3. Face matching algorithm
4. Liveness detection (anti-spoof: foto di HP)

**Bonus Features (Tahap 6 extended):**
1. Generate barcode per user (endpoint)
2. Print ID card dengan barcode
3. Mobile app dengan barcode scanner (ZXing library)
4. QR Code support (encoding: NIS/NIP + nama + kelas)

---

## 📚 REFERENSI

- **POSTMAN-TAHAP-06.md** - Testing guide Barcode checkin
- **README-TAHAP-06.md** - Architecture overview Tahap 6
- **TASK-5.md** - RFID checkin (comparison)
- ZXing Library: https://github.com/zxing/zxing
- Barcode Types: https://en.wikipedia.org/wiki/Barcode

---

## ✅ CHECKLIST COMPLETION

- [x] Step 1: Cek field barcodeId di Siswa & Guru entity
- [x] Step 2: Buat BarcodeCheckinRequest DTO
- [x] Step 3: Tambah findByBarcodeId() di SiswaRepository & GuruRepository
- [x] Step 4: Tambah checkinBarcode() method di PresensiService
- [x] Step 5: Buat BarcodeController dengan endpoint checkin & test
- [x] Step 6: Update SecurityConfig (whitelist barcode endpoints)
- [x] Step 7: Compile, Run, Test

**Status: ✅ TAHAP 6 READY TO IMPLEMENT!**
