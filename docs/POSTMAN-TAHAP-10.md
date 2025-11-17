# POSTMAN TESTING GUIDE - TAHAP 10 (Checkout & Work Hours)

**Tahap**: 10  
**Feature**: Checkout Functionality & Work Hours Calculation  
**Tanggal**: 17 November 2025

---

## 📋 OVERVIEW

Tahap 10 menambahkan **checkout system** dan **work hours calculation**. Fitur:
- **Checkout**: Manual, RFID, Barcode, Face Recognition
- **Work Hours Calculation**: Duration API untuk hitung jam kerja/belajar
- **Overtime Detection**: Flag jika > 8 jam (480 menit)
- **Analytics**: Average hours, overtime count, completion rate

**New Endpoints**:
1. `POST /api/presensi/checkout` - Manual checkout (JWT required)
2. `POST /api/presensi/rfid/checkout` - RFID checkout (public)
3. `POST /api/presensi/barcode/checkout` - Barcode checkout (public)
4. `POST /api/presensi/face/checkout` - Face checkout (public)
5. `GET /api/presensi/{id}/work-hours` - Calculate work hours
6. `GET /api/laporan/average-hours?start=...&end=...` - Average work hours
7. `GET /api/laporan/overtime?start=...&end=...` - Count overtime
8. `GET /api/laporan/completion-rate?tanggal=...` - Checkout completion %

---

## 🚀 SETUP

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run
```

### 2. Variables
```
BASE_URL = http://localhost:8081
ADMIN_TOKEN = <dari login admin>
SISWA_TOKEN = <dari login siswa>
PRESENSI_ID = <dari checkin response>
```

### 3. Pre-requisites
**PENTING**: Checkout hanya bisa dilakukan setelah checkin!

**Create sample checkin**:
```bash
# Manual checkin (07:00)
curl -X POST http://localhost:8081/api/presensi/checkin \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"tipe":"SISWA","latitude":-6.175392,"longitude":106.827153}'
```

Save `presensiId` dari response!

---

## 🧪 TEST SCENARIOS

### SCENARIO 1: Manual Checkout (JWT Required)

**Purpose**: Checkout secara manual dengan GPS coordinates

**Pre-condition**: User sudah checkin hari ini

**Request**:
```
POST {{BASE_URL}}/api/presensi/checkout
Authorization: Bearer {{SISWA_TOKEN}}
Content-Type: application/json

{
  "latitude": -6.175392,
  "longitude": 106.827153,
  "keterangan": "Pulang sekolah jam 15:00"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 5,
  "userId": 2,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:05:30",
  "jamPulang": "15:00:45",
  "status": "HADIR",
  "method": "MANUAL",
  "latitude": -6.175392,
  "longitude": 106.827153,
  "keterangan": "Checkin jam 07:05:30 | Checkout jam 15:00:45 - Pulang sekolah jam 15:00"
}
```

**Validation**:
- ✅ Status: 200 OK
- ✅ `jamPulang` now filled (not null)
- ✅ `keterangan` appended with checkout info
- ✅ GPS coordinates updated

**Save**: `PRESENSI_ID` from response

---

### SCENARIO 2: RFID Checkout (No JWT)

**Purpose**: Checkout via RFID reader (public endpoint)

**Pre-condition**: User checkin via RFID today

**Request**:
```
POST {{BASE_URL}}/api/presensi/rfid/checkout
Content-Type: application/json

{
  "rfidCardId": "RFID-0001"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 6,
  "userId": 3,
  "username": "12346",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:10:00",
  "jamPulang": "15:05:30",
  "status": "HADIR",
  "method": "RFID",
  "keterangan": "Checkin via RFID reader | Checkout via RFID reader jam 15:05:30"
}
```

**Validation**:
- ✅ No Authorization header needed
- ✅ `jamPulang` updated
- ✅ RFID method preserved

---

### SCENARIO 3: Barcode Checkout (No JWT)

**Purpose**: Checkout via barcode/QR code scanner

**Pre-condition**: User checkin via barcode today

**Request**:
```
POST {{BASE_URL}}/api/presensi/barcode/checkout
Content-Type: application/json

{
  "barcodeId": "QR-12345"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 7,
  "userId": 2,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:08:00",
  "jamPulang": "15:10:00",
  "status": "HADIR",
  "method": "BARCODE",
  "keterangan": "Checkin via QR Code | Checkout via QR Code jam 15:10:00"
}
```

---

### SCENARIO 4: Face Checkout (No JWT)

**Purpose**: Checkout via face recognition

**Pre-condition**: User enrolled & checkin via face today

**Request**:
```
POST {{BASE_URL}}/api/presensi/face/checkout
Content-Type: application/json

{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD..."
}
```

**Expected Response** (200 OK):
```json
{
  "id": 8,
  "userId": 2,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:12:00",
  "jamPulang": "15:15:30",
  "status": "HADIR",
  "method": "FACE",
  "keterangan": "Checkin via Face Recognition | Checkout via Face Recognition jam 15:15:30"
}
```

**Validation**:
- ✅ Face similarity > threshold (0.6)
- ✅ `jamPulang` updated

---

### SCENARIO 5: Calculate Work Hours (Normal)

**Purpose**: Calculate jam kerja/belajar (< 8 jam)

**Pre-condition**: User already checked in AND checked out

**Request**:
```
GET {{BASE_URL}}/api/presensi/{{PRESENSI_ID}}/work-hours
Authorization: Bearer {{SISWA_TOKEN}}
```

**Example**: Checkin 07:05, Checkout 15:00 → Duration: 7h 55m

**Expected Response** (200 OK):
```json
{
  "presensiId": 5,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:05:00",
  "jamPulang": "15:00:00",
  "totalMinutes": 475,
  "hours": 7,
  "minutes": 55,
  "isOvertime": false,
  "status": "HADIR"
}
```

**Calculation**:
```
jamMasuk:  07:05:00
jamPulang: 15:00:00

Duration = Duration.between(07:05, 15:00)
         = 7h 55m
         = 475 minutes

hours = 475 / 60 = 7 (integer division)
minutes = 475 % 60 = 55 (modulo)

isOvertime = 475 > 480? → false
```

**Validation**:
- ✅ `totalMinutes` = 475
- ✅ `hours` = 7 (integer part)
- ✅ `minutes` = 55 (remainder)
- ✅ `isOvertime` = false (< 480 minutes)

---

### SCENARIO 6: Calculate Work Hours (Overtime)

**Purpose**: Calculate jam kerja > 8 jam (overtime)

**Setup**: Checkin 07:00, Checkout 18:30 → Duration: 11h 30m

**Request**:
```
GET {{BASE_URL}}/api/presensi/{{PRESENSI_ID}}/work-hours
Authorization: Bearer {{SISWA_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "presensiId": 10,
  "username": "guru001",
  "tipe": "GURU",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:00:00",
  "jamPulang": "18:30:00",
  "totalMinutes": 690,
  "hours": 11,
  "minutes": 30,
  "isOvertime": true,
  "status": "HADIR"
}
```

**Calculation**:
```
Duration = 11h 30m = 690 minutes

hours = 690 / 60 = 11
minutes = 690 % 60 = 30

isOvertime = 690 > 480? → true ✅
```

**Validation**:
- ✅ `totalMinutes` = 690
- ✅ `hours` = 11
- ✅ `minutes` = 30
- ✅ `isOvertime` = true (> 480 minutes = 8 hours)

---

### SCENARIO 7: Calculate Work Hours (Exact 8 Hours)

**Purpose**: Test boundary condition (exactly 480 minutes)

**Setup**: Checkin 07:00, Checkout 15:00 → Duration: 8h 0m

**Expected Response**:
```json
{
  "totalMinutes": 480,
  "hours": 8,
  "minutes": 0,
  "isOvertime": false
}
```

**Validation**:
- ✅ 480 minutes = NOT overtime (threshold is > 480)

---

### SCENARIO 8: Error - Checkout Before Checkin

**Purpose**: Attempt checkout tanpa checkin dulu (hari ini)

**Pre-condition**: User belum checkin hari ini

**Request**:
```
POST {{BASE_URL}}/api/presensi/checkout
Authorization: Bearer {{SISWA_TOKEN}}
Content-Type: application/json

{
  "latitude": -6.175392,
  "longitude": 106.827153,
  "keterangan": "Pulang (belum checkin)"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Anda belum melakukan checkin hari ini"
}
```

**Validation**:
- ✅ Status: 400 Bad Request
- ✅ Clear error message
- ✅ No presensi created

---

### SCENARIO 9: Error - Duplicate Checkout

**Purpose**: Attempt checkout twice (already checked out)

**Pre-condition**: User already checked out today

**Request**:
```
POST {{BASE_URL}}/api/presensi/checkout
Authorization: Bearer {{SISWA_TOKEN}}
Content-Type: application/json

{
  "latitude": -6.175392,
  "longitude": 106.827153,
  "keterangan": "Pulang lagi (duplicate)"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Anda sudah melakukan checkout hari ini"
}
```

**Validation**:
- ✅ Status: 400 Bad Request
- ✅ `jamPulang` NOT updated (prevent overwrite)

---

### SCENARIO 10: Error - Work Hours Without Checkout

**Purpose**: Calculate hours when `jamPulang` is null

**Pre-condition**: User checked in but NOT checked out

**Request**:
```
GET {{BASE_URL}}/api/presensi/{{PRESENSI_ID}}/work-hours
Authorization: Bearer {{SISWA_TOKEN}}
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Presensi belum lengkap. Jam pulang belum diisi."
}
```

**Validation**:
- ✅ Status: 400 Bad Request
- ✅ Cannot calculate duration without both times

---

### SCENARIO 11: Get Average Work Hours

**Purpose**: Calculate rata-rata jam kerja/belajar dalam periode

**Request**:
```
GET {{BASE_URL}}/api/laporan/average-hours?start=2025-11-01&end=2025-11-17
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Rata-rata jam kerja berhasil dihitung",
  "data": {
    "periode": "2025-11-01 to 2025-11-17",
    "totalPresensi": 850,
    "completedCheckout": 820,
    "averageHours": 7.85,
    "interpretation": "Rata-rata siswa/guru bekerja 7.85 jam per hari"
  }
}
```

**Calculation**:
```
totalMinutes (all presensi) = 6437 minutes
completedCheckout = 820

averageMinutes = 6437 / 820 = 7.85 minutes
averageHours = 7.85 / 60 = 7.85 hours
```

**Validation**:
- ✅ Only count presensi with `jamPulang != null`
- ✅ Average in decimal hours (not minutes)
- ✅ Rounded to 2 decimal places

---

### SCENARIO 12: Count Overtime

**Purpose**: Count berapa banyak overtime (> 8 jam) dalam periode

**Request**:
```
GET {{BASE_URL}}/api/laporan/overtime?start=2025-11-01&end=2025-11-17
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Jumlah overtime berhasil dihitung",
  "data": {
    "periode": "2025-11-01 to 2025-11-17",
    "totalPresensi": 850,
    "completedCheckout": 820,
    "overtimeCount": 45,
    "overtimePercentage": 5.49,
    "interpretation": "5.49% presensi melebihi 8 jam"
  }
}
```

**Calculation**:
```
overtimeCount = 45 (presensi with > 480 minutes)
completedCheckout = 820

percentage = (45 / 820) × 100 = 5.49%
```

**Validation**:
- ✅ Only count presensi with `totalMinutes > 480`
- ✅ Percentage accurate

---

### SCENARIO 13: Get Checkout Completion Rate

**Purpose**: Calculate berapa persen user yang sudah checkout

**Request**:
```
GET {{BASE_URL}}/api/laporan/completion-rate?tanggal=2025-11-17
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Tingkat kelengkapan checkout berhasil dihitung",
  "data": {
    "tanggal": "2025-11-17",
    "totalPresensi": 95,
    "completedCheckout": 80,
    "completionRate": 84.21,
    "interpretation": "84.21% user sudah checkout hari ini"
  }
}
```

**Calculation**:
```
totalPresensi = 95
completedCheckout = 80 (jamPulang != null)

rate = (80 / 95) × 100 = 84.21%
```

**Validation**:
- ✅ Count all presensi for date
- ✅ Count completed (jamPulang != null)
- ✅ Percentage accurate to 2 decimals

---

## 🧮 DURATION API EXPLANATION

### Java Time API
```java
import java.time.Duration;
import java.time.LocalTime;

LocalTime jamMasuk = LocalTime.of(7, 5, 0);  // 07:05:00
LocalTime jamPulang = LocalTime.of(15, 0, 0); // 15:00:00

Duration duration = Duration.between(jamMasuk, jamPulang);
```

### Extract Hours & Minutes
```java
// Method 1: Manual calculation (RECOMMENDED)
long totalMinutes = duration.toMinutes(); // 475
long hours = totalMinutes / 60;           // 7 (integer division)
long minutes = totalMinutes % 60;         // 55 (modulo)

// Method 2: Built-in (CAUTION: toHours() truncates!)
long hours = duration.toHours();          // 7
long minutes = duration.toMinutesPart();  // 55
```

### Overtime Detection
```java
boolean isOvertime = duration.toMinutes() > 480; // > 8 hours
```

### Example Calculations

| Jam Masuk | Jam Pulang | Duration | Hours | Minutes | Overtime? |
|-----------|------------|----------|-------|---------|-----------|
| 07:00 | 15:00 | 8h 0m | 8 | 0 | ❌ (= 480) |
| 07:00 | 15:30 | 8h 30m | 8 | 30 | ✅ (> 480) |
| 07:05 | 15:00 | 7h 55m | 7 | 55 | ❌ (< 480) |
| 06:30 | 18:45 | 12h 15m | 12 | 15 | ✅ (> 480) |
| 08:00 | 12:00 | 4h 0m | 4 | 0 | ❌ (< 480) |

---

## 📊 INTEGER DIVISION vs MODULO

### Division (`/`)
Returns **quotient** (hasil bagi, integer part)
```java
475 / 60 = 7  // 7 hours (drop remainder)
480 / 60 = 8  // 8 hours (exact)
500 / 60 = 8  // 8 hours (drop 20 minutes)
```

### Modulo (`%`)
Returns **remainder** (sisa bagi)
```java
475 % 60 = 55  // 55 minutes left
480 % 60 = 0   // 0 minutes left (exact hours)
500 % 60 = 20  // 20 minutes left
```

### Combined Example
```java
long totalMinutes = 475;

long hours = totalMinutes / 60;    // 7
long minutes = totalMinutes % 60;  // 55

System.out.println(hours + "h " + minutes + "m");
// Output: 7h 55m
```

---

## 🔧 TROUBLESHOOTING

### Issue 1: Hours Always 0

**Possible Causes**:
- Using `toHours()` on short durations
- Float division instead of integer

**Fix**:
```java
// Wrong
long hours = duration.toHours(); // Returns 0 if < 1 hour

// Correct
long totalMinutes = duration.toMinutes();
long hours = totalMinutes / 60;
```

### Issue 2: Overtime Always False

**Possible Causes**:
- Comparing hours instead of minutes
- Wrong threshold (8 hours = 480 minutes)

**Fix**:
```java
// Wrong
boolean isOvertime = hours > 8; // Only checks integer hours

// Correct
boolean isOvertime = duration.toMinutes() > 480;
```

### Issue 3: Negative Duration

**Possible Causes**:
- `jamPulang` before `jamMasuk` (checkout before checkin)

**Fix**:
```java
// Check order
if (jamPulang.isBefore(jamMasuk)) {
    throw new IllegalArgumentException("Jam pulang tidak boleh sebelum jam masuk");
}
```

### Issue 4: Work Hours Not Calculated

**Possible Causes**:
- `jamPulang` is null (user belum checkout)

**Fix**:
```java
if (presensi.getJamPulang() == null) {
    throw new RuntimeException("Presensi belum lengkap. Jam pulang belum diisi.");
}
```

---

## ✅ CHECKLIST

**Checkout**:
- [ ] Can checkout manually (with JWT)
- [ ] Can checkout via RFID (no JWT)
- [ ] Can checkout via Barcode (no JWT)
- [ ] Can checkout via Face (no JWT)
- [ ] `jamPulang` updated correctly
- [ ] `keterangan` appended with checkout info
- [ ] Error when checkout before checkin
- [ ] Error when duplicate checkout

**Work Hours**:
- [ ] Can calculate work hours (normal < 8h)
- [ ] Can calculate work hours (overtime > 8h)
- [ ] Hours = integer division (totalMinutes / 60)
- [ ] Minutes = modulo (totalMinutes % 60)
- [ ] `isOvertime` true when > 480 minutes
- [ ] Error when `jamPulang` is null

**Analytics**:
- [ ] Can get average work hours (decimal)
- [ ] Can count overtime instances
- [ ] Can get checkout completion rate (percentage)
- [ ] All calculations accurate to 2 decimals

---

## 📈 REAL-WORLD SCENARIOS

### Scenario A: Regular School Day
```
Checkin:  07:05 (on time)
Checkout: 15:00 (normal dismissal)
Duration: 7h 55m (475 minutes)
Overtime: No
```

### Scenario B: Teacher Overtime
```
Checkin:  07:00 (early)
Checkout: 18:30 (after extracurricular)
Duration: 11h 30m (690 minutes)
Overtime: Yes ⚠️
```

### Scenario C: Half Day
```
Checkin:  08:00
Checkout: 12:00
Duration: 4h 0m (240 minutes)
Overtime: No
```

### Scenario D: Forgot Checkout
```
Checkin:  07:10
Checkout: null (belum checkout)
Duration: Cannot calculate ❌
Action:  Reminder notification needed
```

---

## 📚 DURATION API METHODS

### Common Methods
```java
Duration duration = Duration.between(start, end);

// Get total units
duration.toMinutes()     // Total minutes
duration.toHours()       // Total hours (truncated!)
duration.toSeconds()     // Total seconds

// Get parts (Java 9+)
duration.toHoursPart()   // Hours component (0-23)
duration.toMinutesPart() // Minutes component (0-59)
duration.toSecondsPart() // Seconds component (0-59)

// Comparisons
duration.isNegative()    // true if negative
duration.isZero()        // true if 0

// Arithmetic
duration.plusHours(2)    // Add 2 hours
duration.minusMinutes(30) // Subtract 30 minutes
```

### Common Pitfalls
```java
// ❌ WRONG: toHours() truncates
Duration duration = Duration.ofMinutes(475);
long hours = duration.toHours(); // 7 (drops 55 minutes!)

// ✅ CORRECT: Manual calculation
long totalMinutes = duration.toMinutes(); // 475
long hours = totalMinutes / 60;           // 7
long minutes = totalMinutes % 60;         // 55
```

---

**Author**: Copilot Assistant  
**Last Updated**: 17 November 2025  
**Next**: Tahap 11 (Coming Soon)
