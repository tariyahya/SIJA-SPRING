# TASK-10: Checkout & Work Hours Calculation

**Tahap**: 10  
**Branch**: `tahap-10-checkout`  
**Tanggal**: 17 November 2025  
**Status**: ✅ SELESAI

---

## 🎯 TUJUAN

Menambahkan fitur **checkout** (jam pulang) dan **work hours calculation** untuk melengkapi sistem presensi. Setelah tahap ini, sistem mendukung:
- ✅ **Checkin** (jam masuk) - Tahap 4-9
- ✅ **Checkout** (jam pulang) - Tahap 10 (NEW!)
- ✅ **Work hours calculation** - Total jam kerja (NEW!)
- ✅ **Overtime detection** - Lebih dari 8 jam (NEW!)

**Use Cases**:
- Siswa/Guru: Checkout saat pulang sekolah
- Admin: Monitor jam kerja harian
- Reporting: Average work hours, overtime statistics
- HR: Track attendance completion rate

---

## 📊 FEATURES

### 1. Manual Checkout (with JWT)

**Endpoint**: `POST /api/presensi/checkout`

**Request** (Body JSON):
```json
{
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Pulang tepat waktu"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "userId": 2,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:15:00",
  "jamPulang": "15:00:00",
  "status": "HADIR",
  "method": "MANUAL",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Checkin pagi | Checkout: Pulang tepat waktu"
}
```

**Validations**:
- ❌ User belum checkin hari ini → Error: "Anda belum checkin hari ini"
- ❌ User sudah checkout hari ini → Error: "Anda sudah checkout hari ini"
- ✅ jamPulang diisi dengan waktu sekarang

### 2. RFID Checkout (Public, no JWT)

**Endpoint**: `POST /api/presensi/rfid/checkout`

**Request**:
```json
{
  "rfidCardId": "RF001234"
}
```

**Response** (200 OK):
```json
{
  "id": 5,
  "userId": 10,
  "username": "siswa01",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:05:30",
  "jamPulang": "15:02:00",
  "status": "HADIR",
  "method": "RFID",
  "latitude": null,
  "longitude": null,
  "keterangan": "Checkin via RFID: RF001234 | Checkout via RFID: RF001234"
}
```

**Flow**:
1. RFID reader kirim `rfidCardId`
2. Backend cari user dengan kartu tersebut
3. Validasi: sudah checkin? belum checkout?
4. Update `jamPulang` = sekarang
5. Append keterangan dengan "Checkout via RFID: ..."

### 3. Barcode Checkout (Public, no JWT)

**Endpoint**: `POST /api/presensi/barcode/checkout`

**Request**:
```json
{
  "barcodeId": "BC001234"
}
```

**Response**: Same structure as RFID, method = `BARCODE`

### 4. Face Recognition Checkout (Public, no JWT)

**Endpoint**: `POST /api/presensi/face/checkout`

**Request**:
```json
{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
}
```

**Response**: Same structure, method = `FACE`

**Flow**:
1. Client kirim foto wajah (base64)
2. Backend generate face encoding
3. Loop semua enrolled faces (siswa + guru)
4. Calculate similarity
5. Jika match (> threshold):
   - User identified!
   - Checkout presensi hari ini
   - Update jamPulang

### 5. Work Hours Calculation

**Endpoint**: `GET /api/presensi/{id}/work-hours`

**Request**:
```
GET /api/presensi/1/work-hours
Authorization: Bearer <token>
```

**Response** (200 OK):
```json
{
  "presensiId": 1,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:15:00",
  "jamPulang": "15:00:00",
  "totalMinutes": 465,
  "hours": 7,
  "minutes": 45,
  "isOvertime": false,
  "status": "HADIR"
}
```

**Calculation**:
- `totalMinutes` = Duration between jamMasuk and jamPulang in minutes
- `hours` = totalMinutes / 60 (integer division)
- `minutes` = totalMinutes % 60 (modulo)
- `isOvertime` = true if totalMinutes > 480 (8 hours)

**Example**:
```
jamMasuk: 07:15:00
jamPulang: 15:00:00
Duration: 7 hours 45 minutes = 465 minutes
isOvertime: false (< 8 hours)
```

---

## 🗂️ FILE CHANGES

### 1. DTO: WorkHoursResponse.java (NEW)

**Path**: `backend/src/main/java/com/smk/presensi/dto/WorkHoursResponse.java`

```java
package com.smk.presensi.dto;

/**
 * DTO untuk response work hours calculation.
 * 
 * Contains:
 * - Basic info (presensiId, username, tipe, tanggal)
 * - Time info (jamMasuk, jamPulang)
 * - Duration info (totalMinutes, hours, minutes)
 * - Overtime flag (isOvertime)
 * - Status (HADIR/TERLAMBAT/ALPHA)
 */
public record WorkHoursResponse(
        Long presensiId,
        String username,
        String tipe,
        String tanggal,
        String jamMasuk,
        String jamPulang,
        long totalMinutes,      // Total minutes worked
        long hours,             // Hours (integer part)
        long minutes,           // Minutes (remainder)
        boolean isOvertime,     // true if > 8 hours (480 minutes)
        String status
) {}
```

### 2. Service: PresensiService.java (UPDATED)

**Changes**:
- ✅ Added `checkoutRfid(String rfidCardId)` method
- ✅ Added `checkoutBarcode(String barcodeId)` method
- ✅ Added `checkoutFace(Siswa siswa)` method (overload for Siswa)
- ✅ Added `checkoutFace(Guru guru)` method (overload for Guru)
- ✅ Added `getPresensiById(Long id)` method
- ✅ Added `calculateWorkHours(Presensi presensi)` method

**Key Method: calculateWorkHours()**
```java
public WorkHoursResponse calculateWorkHours(Presensi presensi) {
    // Validation: jamMasuk and jamPulang must exist
    if (presensi.getJamMasuk() == null || presensi.getJamPulang() == null) {
        throw new RuntimeException("Data jam masuk atau jam pulang belum lengkap");
    }
    
    // Calculate duration using java.time.Duration
    java.time.Duration duration = java.time.Duration.between(
        presensi.getJamMasuk(), 
        presensi.getJamPulang()
    );
    
    long totalMinutes = duration.toMinutes();
    long hours = totalMinutes / 60;           // Integer division
    long minutes = totalMinutes % 60;         // Modulo (remainder)
    
    // Check overtime (more than 8 hours = 480 minutes)
    boolean isOvertime = totalMinutes > 480;
    
    return new WorkHoursResponse(
        presensi.getId(),
        presensi.getUser().getUsername(),
        presensi.getTipe().name(),
        presensi.getTanggal().toString(),
        presensi.getJamMasuk().toString(),
        presensi.getJamPulang().toString(),
        totalMinutes,
        hours,
        minutes,
        isOvertime,
        presensi.getStatus().name()
    );
}
```

**Duration API Explanation**:
```java
LocalTime jamMasuk = LocalTime.of(7, 15, 0);    // 07:15:00
LocalTime jamPulang = LocalTime.of(15, 0, 0);   // 15:00:00

Duration duration = Duration.between(jamMasuk, jamPulang);

// Get total minutes
long totalMinutes = duration.toMinutes();  // 465 minutes

// Get hours (integer division)
long hours = totalMinutes / 60;  // 465 / 60 = 7

// Get remaining minutes (modulo)
long minutes = totalMinutes % 60;  // 465 % 60 = 45

// Result: 7 hours 45 minutes
```

### 3. Controller: PresensiController.java (UPDATED)

**Added Endpoint**: `GET /api/presensi/{id}/work-hours`

```java
/**
 * GET WORK HOURS - Hitung jam kerja dari presensi ID.
 * 
 * Access: ADMIN, GURU, SISWA (owner only via service)
 * URL: GET /api/presensi/{id}/work-hours
 */
@GetMapping("/{id}/work-hours")
@PreAuthorize("hasAnyRole('ADMIN', 'GURU', 'SISWA')")
public ResponseEntity<?> getWorkHours(@PathVariable Long id) {
    try {
        var presensi = presensiService.getPresensiById(id);
        var workHours = presensiService.calculateWorkHours(presensi);
        return ResponseEntity.ok(workHours);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
```

### 4. Controller: RfidController.java (UPDATED)

**Added Endpoint**: `POST /api/presensi/rfid/checkout`

```java
/**
 * ENDPOINT: POST /api/presensi/rfid/checkout
 * 
 * Checkout via tap kartu RFID.
 * 
 * Access: PUBLIC (no JWT required)
 * 
 * Request body:
 * {
 *   "rfidCardId": "RF001234"
 * }
 * 
 * Response: PresensiResponse dengan jamPulang terisi
 */
@PostMapping("/checkout")
public ResponseEntity<PresensiResponse> checkoutRfid(@Valid @RequestBody RfidCheckinRequest request) {
    PresensiResponse response = presensiService.checkoutRfid(request.rfidCardId());
    return ResponseEntity.ok(response);
}
```

### 5. Controller: BarcodeController.java (UPDATED)

**Added Endpoint**: `POST /api/presensi/barcode/checkout`

```java
/**
 * ENDPOINT: POST /api/presensi/barcode/checkout
 * 
 * Checkout via scan barcode/QR code.
 * 
 * Access: PUBLIC (no JWT required)
 */
@PostMapping("/checkout")
public ResponseEntity<PresensiResponse> checkoutBarcode(@Valid @RequestBody BarcodeCheckinRequest request) {
    PresensiResponse response = presensiService.checkoutBarcode(request.barcodeId());
    return ResponseEntity.ok(response);
}
```

### 6. Controller: FaceController.java (UPDATED)

**Added Endpoint**: `POST /api/presensi/face/checkout`

```java
/**
 * CHECKOUT VIA FACE RECOGNITION.
 * 
 * POST /api/presensi/face/checkout
 * Body: { "imageBase64": "data:image/jpeg;base64,..." }
 * 
 * Flow sama dengan checkin, tapi panggil checkoutFace() instead of checkinFace().
 */
@PostMapping("/checkout")
public ResponseEntity<?> checkout(@Valid @RequestBody FaceCheckinRequest request) {
    try {
        String imageBase64 = request.imageBase64();

        // Generate encoding dari input image
        String inputEncoding = faceRecognitionService.generateFaceEncoding(imageBase64);

        // Loop semua siswa & guru enrolled
        // Calculate similarity
        // Jika match → checkout!
        
        // ... (implementation similar to checkin)
        
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Checkout gagal: " + e.getMessage());
    }
}
```

### 7. Service: LaporanService.java (UPDATED)

**Added Methods**:

#### 7.1. toPresensiResponse() - UPDATED
```java
private PresensiResponse toPresensiResponse(Presensi presensi) {
    return new PresensiResponse(
            presensi.getId(),
            presensi.getUser().getId(),
            presensi.getUser().getUsername(),
            presensi.getTipe(),
            presensi.getTanggal(),
            presensi.getJamMasuk(),
            presensi.getJamPulang(),  // UPDATED: Now includes jamPulang (was null before)
            presensi.getStatus(),
            presensi.getMethod(),
            presensi.getLatitude(),
            presensi.getLongitude(),
            presensi.getKeterangan()
    );
}
```

#### 7.2. getAverageWorkHours() - NEW
```java
/**
 * Get average work hours for a period.
 * 
 * Logic:
 * 1. Get all presensi with jamPulang (completed checkouts)
 * 2. Calculate work duration for each
 * 3. Return average in hours (decimal)
 * 
 * Example:
 * - User A: 8.5 hours
 * - User B: 7.5 hours
 * - User C: 9.0 hours
 * - Average: (8.5 + 7.5 + 9.0) / 3 = 8.33 hours
 */
public double getAverageWorkHours(LocalDate startDate, LocalDate endDate) {
    List<Presensi> presensiList = presensiRepository.findByTanggalBetween(startDate, endDate);
    
    // Filter only completed (has jamPulang)
    List<Presensi> completed = presensiList.stream()
            .filter(p -> p.getJamPulang() != null)
            .toList();
    
    if (completed.isEmpty()) {
        return 0.0;
    }
    
    // Calculate total minutes
    long totalMinutes = completed.stream()
            .mapToLong(p -> {
                Duration duration = Duration.between(p.getJamMasuk(), p.getJamPulang());
                return duration.toMinutes();
            })
            .sum();
    
    // Average minutes
    double avgMinutes = (double) totalMinutes / completed.size();
    
    // Convert to hours (decimal, rounded to 2 decimals)
    return Math.round((avgMinutes / 60.0) * 100.0) / 100.0;
}
```

#### 7.3. countOvertime() - NEW
```java
/**
 * Count overtime instances (work > 8 hours).
 * 
 * Use case:
 * - Monitor overtime trends
 * - Calculate overtime pay
 * - Identify workaholics
 * 
 * @return Number of overtime instances
 */
public long countOvertime(LocalDate startDate, LocalDate endDate) {
    List<Presensi> presensiList = presensiRepository.findByTanggalBetween(startDate, endDate);
    
    return presensiList.stream()
            .filter(p -> p.getJamPulang() != null)
            .filter(p -> {
                Duration duration = Duration.between(p.getJamMasuk(), p.getJamPulang());
                return duration.toMinutes() > 480; // > 8 hours
            })
            .count();
}
```

#### 7.4. getCheckoutCompletionRate() - NEW
```java
/**
 * Get percentage of users who completed checkout.
 * 
 * Formula:
 * Completion Rate = (Users with jamPulang / Total users) × 100%
 * 
 * Example:
 * - Total checkin: 100
 * - Completed checkout: 95
 * - Rate: 95%
 * 
 * Use case:
 * - Monitor discipline (lupa checkout?)
 * - Track system usage
 * 
 * @return Percentage (0-100)
 */
public double getCheckoutCompletionRate(LocalDate tanggal) {
    List<Presensi> presensiList = presensiRepository.findByTanggal(tanggal);
    
    if (presensiList.isEmpty()) {
        return 0.0;
    }
    
    long completed = presensiList.stream()
            .filter(p -> p.getJamPulang() != null)
            .count();
    
    double rate = (completed * 100.0) / presensiList.size();
    return Math.round(rate * 100.0) / 100.0;
}
```

---

## 🧪 TESTING SCENARIOS

### Scenario 1: Manual Checkout (Happy Path)

**Setup**:
1. User sudah checkin pagi (07:15)
2. User login via JWT
3. User checkout sore (15:00)

**Request**:
```bash
POST http://localhost:8081/api/presensi/checkout
Authorization: Bearer <siswa-token>
Content-Type: application/json

{
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Pulang selesai ekstrakurikuler"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 1,
  "userId": 2,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:15:00",
  "jamPulang": "15:00:00",
  "status": "HADIR",
  "method": "MANUAL",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Checkin pagi | Checkout: Pulang selesai ekstrakurikuler"
}
```

### Scenario 2: Checkout Before Checkin (Error)

**Setup**:
- User belum checkin hari ini
- User langsung checkout

**Request**:
```bash
POST http://localhost:8081/api/presensi/checkout
Authorization: Bearer <siswa-token>
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Anda belum checkin hari ini"
}
```

### Scenario 3: Duplicate Checkout (Error)

**Setup**:
- User sudah checkin (07:15)
- User sudah checkout (15:00)
- User coba checkout lagi (16:00)

**Expected Response** (400 Bad Request):
```json
{
  "error": "Anda sudah checkout hari ini"
}
```

### Scenario 4: RFID Checkout

**Request**:
```bash
POST http://localhost:8081/api/presensi/rfid/checkout
Content-Type: application/json

{
  "rfidCardId": "RF001234"
}
```

**Expected**: Success (200 OK) dengan jamPulang terisi

### Scenario 5: Work Hours Calculation

**Setup**:
- User checkin: 07:15
- User checkout: 15:00
- Total: 7 hours 45 minutes = 465 minutes

**Request**:
```bash
GET http://localhost:8081/api/presensi/1/work-hours
Authorization: Bearer <siswa-token>
```

**Expected Response** (200 OK):
```json
{
  "presensiId": 1,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:15:00",
  "jamPulang": "15:00:00",
  "totalMinutes": 465,
  "hours": 7,
  "minutes": 45,
  "isOvertime": false,
  "status": "HADIR"
}
```

### Scenario 6: Overtime Detection

**Setup**:
- User checkin: 07:00
- User checkout: 18:00
- Total: 11 hours = 660 minutes (OVERTIME!)

**Expected**:
```json
{
  "totalMinutes": 660,
  "hours": 11,
  "minutes": 0,
  "isOvertime": true
}
```

### Scenario 7: Incomplete Checkout (Error)

**Setup**:
- User sudah checkin
- User belum checkout (jamPulang = null)
- Call work hours calculation

**Request**:
```bash
GET http://localhost:8081/api/presensi/1/work-hours
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Data jam masuk atau jam pulang belum lengkap"
}
```

---

## 📊 STATISTIK

### File Count
- **Total Files**: 63 source files (up from 62 in Tahap 9)
- **New Files**: 1 file
  - WorkHoursResponse.java (DTO)
- **Updated Files**: 6 files
  - PresensiService.java (add 5 checkout methods + calculateWorkHours)
  - PresensiController.java (add work hours endpoint)
  - RfidController.java (add checkout endpoint)
  - BarcodeController.java (add checkout endpoint)
  - FaceController.java (add checkout endpoint)
  - LaporanService.java (add 3 analytics methods + update toPresensiResponse)

### Lines of Code
- **WorkHoursResponse.java**: ~15 lines
- **PresensiService.java**: +150 lines (5 checkout methods + helpers)
- **PresensiController.java**: +15 lines (1 endpoint)
- **RfidController.java**: +20 lines (1 endpoint)
- **BarcodeController.java**: +20 lines (1 endpoint)
- **FaceController.java**: +50 lines (1 endpoint with face matching)
- **LaporanService.java**: +80 lines (3 analytics methods)
- **Total New Code**: ~350 lines

### Build Result
```
[INFO] Compiling 63 source files with javac [debug release 17] to target\classes
[INFO] BUILD SUCCESS
[INFO] Total time:  5.681 s
```

---

## 🎓 LEARNING OUTCOMES

Setelah menyelesaikan Tahap 10, siswa memahami:

### 1. Time Tracking Concepts
- **Checkin vs Checkout**: Difference and use cases
- **Duration calculation**: Using java.time.Duration API
- **Work hours**: Converting minutes to hours + minutes
- **Overtime detection**: Business logic for > 8 hours

### 2. Java Time API
```java
// LocalTime: Time of day (07:15:00)
LocalTime jamMasuk = LocalTime.now();
LocalTime jamPulang = LocalTime.of(15, 0, 0);

// Duration: Time span between two instants
Duration duration = Duration.between(jamMasuk, jamPulang);
long minutes = duration.toMinutes();  // 465

// Calculation
long hours = minutes / 60;    // 7 (integer division)
long mins = minutes % 60;     // 45 (modulo/remainder)
```

### 3. Validation Logic
- **Before checkout**: Must checkin first
- **Duplicate prevention**: Cannot checkout twice
- **Data completeness**: jamMasuk and jamPulang must exist for calculation

### 4. Public API Design
- **RFID/Barcode/Face**: Public endpoints (no JWT)
- **Auto-detect user**: From rfidCardId/barcodeId/face
- **Consistent pattern**: Same logic as checkin, but update jamPulang

### 5. Analytics Methods
- **Average calculation**: Stream API + mapToLong + average
- **Filtering**: Only completed checkouts (jamPulang != null)
- **Percentage formula**: (count * 100.0 / total)

---

## ✅ CHECKLIST

- [x] WorkHoursResponse DTO created
- [x] PresensiService checkout methods added (manual, RFID, barcode, face)
- [x] PresensiService calculateWorkHours() implemented
- [x] PresensiController work hours endpoint added
- [x] RfidController checkout endpoint added
- [x] BarcodeController checkout endpoint added
- [x] FaceController checkout endpoint added
- [x] LaporanService analytics methods added
- [x] Build success (63 files compiled)
- [x] Documentation complete (TASK-10.md)

**Status**: ✅ SELESAI

**Next**: Tahap 11 - Export to CSV/PDF or Mobile App Development

---

## 🔗 RELATED TOPICS

- **blog11.md**: Time Tracking & Duration API concepts
- **TASK-4.md**: Original checkin implementation (reference)
- **TASK-9.md**: Reporting & analytics foundation

---

**Author**: Copilot Assistant  
**Last Updated**: 17 November 2025
