# POSTMAN TESTING GUIDE - TAHAP 9 (Reporting & Analytics)

**Tahap**: 9  
**Feature**: Daily/Monthly Reports & Statistics API  
**Tanggal**: 17 November 2025

---

## 📋 OVERVIEW

Tahap 9 menambahkan **sistem pelaporan dan analitik** untuk monitoring presensi. Fitur:
- **Laporan Harian**: Daftar presensi per tanggal (hari ini, kemarin, custom date)
- **Laporan Bulanan**: Rekap presensi per bulan (bulan ini, bulan lalu, custom month)
- **Statistik**: Persentase kehadiran, keterlambatan, alpha, breakdown per method

**New Endpoints**:
1. `GET /api/laporan/harian?tanggal=2025-11-17` - Daily report
2. `GET /api/laporan/bulanan?bulan=11&tahun=2025` - Monthly report
3. `GET /api/laporan/statistik?start=...&end=...` - Statistics with date range

---

## 🚀 SETUP

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run
```

### 2. Import Postman Collection
- File: `SIJA-SPRING-Tahap-09.postman_collection.json` (jika ada)
- Atau copy-paste requests dari guide ini

### 3. Variables
Set environment variables:
```
BASE_URL = http://localhost:8081
ADMIN_TOKEN = <dari login admin>
GURU_TOKEN = <dari login guru>
SISWA_TOKEN = <dari login siswa>
TODAY_DATE = 2025-11-17
THIS_MONTH = 11
THIS_YEAR = 2025
```

### 4. Pre-requisites
**Pastikan ada data presensi**:
- Minimal 5-10 checkin untuk hari ini
- Minimal 50+ checkin untuk bulan ini
- Variasi status: HADIR, TERLAMBAT, ALPHA
- Variasi method: MANUAL, RFID, BARCODE, FACE

**Cara generate sample data** (optional):
```bash
# Checkin 10 siswa via RFID
for i in {1..10}; do
  curl -X POST http://localhost:8081/api/presensi/rfid/checkin \
       -H "Content-Type: application/json" \
       -d "{\"rfidCardId\": \"RFID-000$i\"}"
done
```

---

## 🧪 TEST SCENARIOS

### SCENARIO 1: Get Daily Report (Today - Default)

**Purpose**: Get laporan harian untuk hari ini (tanpa parameter tanggal)

**Request**:
```
GET {{BASE_URL}}/api/laporan/harian
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Laporan harian berhasil diambil",
  "data": {
    "tanggal": "2025-11-17",
    "totalPresensi": 95,
    "totalHadir": 85,
    "totalTerlambat": 8,
    "totalAlpha": 2,
    "persentaseHadir": 89.47,
    "persentaseTerlambat": 8.42,
    "persentaseAlpha": 2.11,
    "daftarPresensi": [
      {
        "id": 1,
        "userId": 2,
        "username": "12345",
        "tipe": "SISWA",
        "tanggal": "2025-11-17",
        "jamMasuk": "07:05:30",
        "jamPulang": null,
        "status": "HADIR",
        "method": "RFID",
        "keterangan": "Checkin via RFID reader"
      },
      {
        "id": 2,
        "userId": 3,
        "username": "12346",
        "tipe": "SISWA",
        "tanggal": "2025-11-17",
        "jamMasuk": "07:35:00",
        "jamPulang": null,
        "status": "TERLAMBAT",
        "method": "BARCODE",
        "keterangan": "Checkin via QR Code"
      }
      // ... 93 more
    ]
  }
}
```

**Validation**:
- ✅ Status: 200 OK
- ✅ `totalPresensi` = sum of all presensi today
- ✅ `totalHadir + totalTerlambat + totalAlpha = totalPresensi`
- ✅ `persentaseHadir` = (totalHadir / totalPresensi) × 100
- ✅ Percentages sum to 100%
- ✅ `daftarPresensi` array not empty
- ✅ All presensi have tanggal = today

---

### SCENARIO 2: Get Daily Report (Specific Date)

**Purpose**: Get laporan untuk tanggal tertentu (kemarin, minggu lalu, dst)

**Request**:
```
GET {{BASE_URL}}/api/laporan/harian?tanggal=2025-11-16
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Laporan harian berhasil diambil",
  "data": {
    "tanggal": "2025-11-16",
    "totalPresensi": 87,
    "totalHadir": 80,
    "totalTerlambat": 5,
    "totalAlpha": 2,
    "persentaseHadir": 91.95,
    "persentaseTerlambat": 5.75,
    "persentaseAlpha": 2.30,
    "daftarPresensi": [...]
  }
}
```

**Validation**:
- ✅ `tanggal` = requested date (2025-11-16)
- ✅ All presensi have tanggal = 2025-11-16

---

### SCENARIO 3: Get Daily Report (No Data)

**Purpose**: Request laporan untuk tanggal yang tidak ada presensi

**Request**:
```
GET {{BASE_URL}}/api/laporan/harian?tanggal=2025-01-01
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Laporan harian berhasil diambil",
  "data": {
    "tanggal": "2025-01-01",
    "totalPresensi": 0,
    "totalHadir": 0,
    "totalTerlambat": 0,
    "totalAlpha": 0,
    "persentaseHadir": 0.0,
    "persentaseTerlambat": 0.0,
    "persentaseAlpha": 0.0,
    "daftarPresensi": []
  }
}
```

**Validation**:
- ✅ Status: 200 OK (NOT 404)
- ✅ All counts = 0
- ✅ All percentages = 0.0
- ✅ `daftarPresensi` = empty array

---

### SCENARIO 4: Get Monthly Report (This Month - Default)

**Purpose**: Get laporan bulanan untuk bulan ini (tanpa parameter)

**Request**:
```
GET {{BASE_URL}}/api/laporan/bulanan
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Laporan bulanan berhasil diambil",
  "data": {
    "bulan": 11,
    "tahun": 2025,
    "namaBulan": "November",
    "totalPresensi": 1850,
    "totalHadir": 1650,
    "totalTerlambat": 150,
    "totalAlpha": 50,
    "persentaseHadir": 89.19,
    "persentaseTerlambat": 8.11,
    "persentaseAlpha": 2.70,
    "rekapPerHari": [
      {
        "tanggal": "2025-11-01",
        "totalPresensi": 90,
        "totalHadir": 85,
        "totalTerlambat": 4,
        "totalAlpha": 1
      },
      {
        "tanggal": "2025-11-02",
        "totalPresensi": 88,
        "totalHadir": 82,
        "totalTerlambat": 5,
        "totalAlpha": 1
      }
      // ... 15 more days (sampai tanggal 17)
    ]
  }
}
```

**Validation**:
- ✅ `bulan` = current month (11)
- ✅ `tahun` = current year (2025)
- ✅ `namaBulan` = "November"
- ✅ `totalPresensi` = sum of all days
- ✅ Percentages accurate
- ✅ `rekapPerHari` array contains daily summaries

---

### SCENARIO 5: Get Monthly Report (Specific Month)

**Purpose**: Get laporan untuk bulan tertentu (Januari, Februari, dst)

**Request**:
```
GET {{BASE_URL}}/api/laporan/bulanan?bulan=1&tahun=2025
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Laporan bulanan berhasil diambil",
  "data": {
    "bulan": 1,
    "tahun": 2025,
    "namaBulan": "Januari",
    "totalPresensi": 2200,
    "totalHadir": 2000,
    "totalTerlambat": 150,
    "totalAlpha": 50,
    "persentaseHadir": 90.91,
    "persentaseTerlambat": 6.82,
    "persentaseAlpha": 2.27,
    "rekapPerHari": [...]
  }
}
```

**Validation**:
- ✅ `bulan` = requested month (1)
- ✅ `namaBulan` = "Januari"
- ✅ `rekapPerHari` contains dates in January 2025

---

### SCENARIO 6: Get Monthly Report (Invalid Month)

**Purpose**: Request dengan bulan invalid (13, 0, -1)

**Request**:
```
GET {{BASE_URL}}/api/laporan/bulanan?bulan=13&tahun=2025
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Bulan harus antara 1-12"
}
```

**Validation**:
- ✅ Status: 400 Bad Request
- ✅ Error message clear

---

### SCENARIO 7: Get Statistics (All Time)

**Purpose**: Get statistik keseluruhan (tanpa date filter)

**Request**:
```
GET {{BASE_URL}}/api/laporan/statistik
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Statistik presensi berhasil diambil",
  "data": {
    "periode": "All Time",
    "totalPresensi": 5000,
    "totalHadir": 4500,
    "totalTerlambat": 400,
    "totalAlpha": 100,
    "persentaseHadir": 90.0,
    "persentaseTerlambat": 8.0,
    "persentaseAlpha": 2.0,
    "breakdownPerMethod": {
      "MANUAL": 1200,
      "RFID": 2000,
      "BARCODE": 1000,
      "FACE": 800
    },
    "breakdownPerTipe": {
      "SISWA": 4000,
      "GURU": 1000
    }
  }
}
```

**Validation**:
- ✅ `periode` = "All Time"
- ✅ `totalPresensi` = sum of all presensi in database
- ✅ `breakdownPerMethod`: sum = totalPresensi
- ✅ `breakdownPerTipe`: SISWA + GURU = totalPresensi
- ✅ Percentages accurate

---

### SCENARIO 8: Get Statistics (Date Range)

**Purpose**: Get statistik untuk periode tertentu (1 minggu, 1 bulan, dst)

**Request**:
```
GET {{BASE_URL}}/api/laporan/statistik?start=2025-11-01&end=2025-11-17
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Statistik presensi berhasil diambil",
  "data": {
    "periode": "2025-11-01 to 2025-11-17",
    "totalPresensi": 1850,
    "totalHadir": 1650,
    "totalTerlambat": 150,
    "totalAlpha": 50,
    "persentaseHadir": 89.19,
    "persentaseTerlambat": 8.11,
    "persentaseAlpha": 2.70,
    "breakdownPerMethod": {
      "MANUAL": 400,
      "RFID": 800,
      "BARCODE": 400,
      "FACE": 250
    },
    "breakdownPerTipe": {
      "SISWA": 1500,
      "GURU": 350
    }
  }
}
```

**Validation**:
- ✅ `periode` = "start to end"
- ✅ Only presensi within date range counted
- ✅ Breakdown sums accurate

---

### SCENARIO 9: Get Statistics (Start Date Only)

**Purpose**: Get statistik dari tanggal tertentu sampai sekarang

**Request**:
```
GET {{BASE_URL}}/api/laporan/statistik?start=2025-11-01
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Statistik presensi berhasil diambil",
  "data": {
    "periode": "2025-11-01 to Now",
    "totalPresensi": 1850,
    ...
  }
}
```

---

### SCENARIO 10: Get Statistics (End Date Only)

**Purpose**: Get statistik dari awal sampai tanggal tertentu

**Request**:
```
GET {{BASE_URL}}/api/laporan/statistik?end=2025-10-31
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Statistik presensi berhasil diambil",
  "data": {
    "periode": "Beginning to 2025-10-31",
    "totalPresensi": 3150,
    ...
  }
}
```

---

### SCENARIO 11: Access Laporan as GURU (SUCCESS)

**Purpose**: Verify GURU can access laporan endpoints

**Request**:
```
GET {{BASE_URL}}/api/laporan/harian
Authorization: Bearer {{GURU_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "message": "Laporan harian berhasil diambil",
  "data": {...}
}
```

**Validation**:
- ✅ Status: 200 OK
- ✅ GURU has permission to view reports

---

### SCENARIO 12: Access Laporan as SISWA (FORBIDDEN)

**Purpose**: Verify SISWA cannot access laporan (ADMIN/GURU only)

**Request**:
```
GET {{BASE_URL}}/api/laporan/harian
Authorization: Bearer {{SISWA_TOKEN}}
```

**Expected Response** (403 Forbidden):
```json
{
  "error": "Access Denied",
  "message": "User does not have required role"
}
```

**Validation**:
- ✅ Status: 403 Forbidden
- ✅ SISWA blocked from accessing reports

---

## 📊 PERCENTAGE CALCULATION FORMULAS

### Attendance Percentage
```
persentaseHadir = (totalHadir / totalPresensi) × 100
```

Example:
```
totalHadir = 85
totalPresensi = 95

persentaseHadir = (85 / 95) × 100 = 89.47%
```

### Late Percentage
```
persentaseTerlambat = (totalTerlambat / totalPresensi) × 100
```

### Absent Percentage
```
persentaseAlpha = (totalAlpha / totalPresensi) × 100
```

### Total Check
```
persentaseHadir + persentaseTerlambat + persentaseAlpha = 100%
```

**Rounding**: Use 2 decimal places
```java
double percentage = Math.round((count * 100.0 / total) * 100.0) / 100.0;
```

---

## 📈 DATA INTERPRETATION

### Example Report Analysis

**Daily Report (2025-11-17)**:
```
Total Presensi: 95
- Hadir: 85 (89.47%)
- Terlambat: 8 (8.42%)
- Alpha: 2 (2.11%)
```

**Interpretation**:
- ✅ **Good**: 89.47% hadir (> 85% threshold)
- ⚠️ **Warning**: 8.42% terlambat (should be < 5%)
- ✅ **Excellent**: 2.11% alpha (< 3%)

---

### Monthly Trend

**November 2025**:
```
Week 1: 92% hadir, 6% terlambat, 2% alpha
Week 2: 89% hadir, 8% terlambat, 3% alpha
Week 3: 87% hadir, 10% terlambat, 3% alpha
```

**Interpretation**:
- ⚠️ **Trend**: Kehadiran menurun dari week 1 ke week 3
- ⚠️ **Action**: Perlu investigasi penyebab keterlambatan meningkat

---

### Method Breakdown

**All Time Statistics**:
```
RFID: 2000 (40%)
MANUAL: 1200 (24%)
BARCODE: 1000 (20%)
FACE: 800 (16%)
```

**Interpretation**:
- ✅ **RFID most popular**: Fastest & easiest method
- ⚠️ **Face recognition low**: Mungkin ada issues (lighting, enrollment)

---

## 🔧 TROUBLESHOOTING

### Issue 1: Percentages Don't Sum to 100%

**Possible Causes**:
- Rounding errors
- Floating point precision

**Fix**:
```java
// Use double, not float
double percentage = (count * 100.0) / total;

// Round to 2 decimals
percentage = Math.round(percentage * 100.0) / 100.0;
```

### Issue 2: Division by Zero

**Problem**: `totalPresensi = 0` causes `NaN` or exception

**Fix**:
```java
if (totalPresensi == 0) {
    return 0.0; // or throw custom exception
}
double persentase = (totalHadir * 100.0) / totalPresensi;
```

### Issue 3: Wrong Month Name

**Problem**: `namaBulan` shows wrong month (off by 1)

**Cause**: Java months are 0-indexed (0 = January)

**Fix**:
```java
// Correct (user sends 1-12)
String[] bulanNames = {"", "Januari", "Februari", ...};
String namaBulan = bulanNames[bulan]; // bulan from request (1-12)

// Wrong (using Calendar)
Calendar cal = Calendar.getInstance();
cal.get(Calendar.MONTH); // Returns 0-11, need to add 1!
```

---

## ✅ CHECKLIST

- [ ] Can get daily report (today)
- [ ] Can get daily report (specific date)
- [ ] Can get daily report (no data → empty array)
- [ ] Can get monthly report (this month)
- [ ] Can get monthly report (specific month)
- [ ] Monthly report rejects invalid month (13, 0, -1)
- [ ] Can get statistics (all time)
- [ ] Can get statistics (date range: start & end)
- [ ] Can get statistics (start only, end only)
- [ ] Percentages sum to 100%
- [ ] Percentages accurate to 2 decimals
- [ ] Breakdown per method accurate
- [ ] Breakdown per tipe accurate
- [ ] ADMIN can access all endpoints
- [ ] GURU can access all endpoints
- [ ] SISWA blocked (403 Forbidden)

---

## 📚 SAMPLE DATA GENERATORS

### Generate 100 Random Presensi
```bash
#!/bin/bash

for i in {1..100}; do
  # Random user (RFID)
  RFID="RFID-$(printf "%04d" $((RANDOM % 50 + 1)))"
  
  curl -X POST http://localhost:8081/api/presensi/rfid/checkin \
       -H "Content-Type: application/json" \
       -d "{\"rfidCardId\": \"$RFID\"}"
  
  sleep 0.5
done
```

### Generate Presensi for Date Range
```bash
#!/bin/bash

START_DATE="2025-11-01"
END_DATE="2025-11-17"

current="$START_DATE"
while [[ "$current" != "$END_DATE" ]]; do
  echo "Generating for $current..."
  
  # Generate 50 presensi per day
  for i in {1..50}; do
    # Logic here (use date override if supported)
  done
  
  current=$(date -I -d "$current + 1 day")
done
```

---

## 📊 EXPECTED STATISTICS

**Typical School Metrics**:
- Attendance rate: 85-95%
- Late rate: 5-10%
- Absent rate: 1-5%

**Alert Thresholds**:
- 🚨 Red: < 80% attendance
- ⚠️ Yellow: 80-85% attendance
- ✅ Green: > 85% attendance

---

**Author**: Copilot Assistant  
**Last Updated**: 17 November 2025  
**Next**: POSTMAN-TAHAP-10.md (Checkout & Work Hours Testing)
