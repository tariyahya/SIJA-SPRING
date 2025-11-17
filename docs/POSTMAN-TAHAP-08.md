# POSTMAN TESTING GUIDE - TAHAP 8 (Geolocation Validation)

**Tahap**: 8  
**Feature**: GPS-based Location Validation with Haversine Formula  
**Tanggal**: 17 November 2025

---

## 📋 OVERVIEW

Tahap 8 menambahkan **validasi lokasi berbasis GPS** menggunakan **Haversine formula**. Sistem akan:
- Menyimpan koordinat kantor/sekolah (latitude, longitude, radius)
- Validasi checkin: Hanya bisa checkin jika dalam radius yang ditentukan
- Hitung jarak menggunakan Haversine formula (akurat untuk jarak di permukaan bumi)

**New Endpoints**:
1. `POST /api/lokasi-kantor` - Create lokasi kantor
2. `GET /api/lokasi-kantor` - Get all lokasi
3. `GET /api/lokasi-kantor/{id}` - Get by ID
4. `PUT /api/lokasi-kantor/{id}` - Update lokasi
5. `DELETE /api/lokasi-kantor/{id}` - Delete lokasi
6. `POST /api/presensi/checkin` - UPDATED: dengan GPS validation

---

## 🚀 SETUP

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run
```

### 2. Import Postman Collection
- File: `SIJA-SPRING-Tahap-08.postman_collection.json` (jika ada)
- Atau copy-paste requests dari guide ini

### 3. Variables
Set environment variables:
```
BASE_URL = http://localhost:8081
ADMIN_TOKEN = <dari login admin>
SISWA_TOKEN = <dari login siswa>
LOKASI_ID = <dari create lokasi kantor>
```

---

## 🧪 TEST SCENARIOS

### SCENARIO 1: Create Lokasi Kantor (ADMIN)

**Purpose**: Setup lokasi sekolah dengan koordinat dan radius

**Request**:
```
POST {{BASE_URL}}/api/lokasi-kantor
Authorization: Bearer {{ADMIN_TOKEN}}
Content-Type: application/json

{
  "nama": "SMK Negeri 1 Jakarta",
  "alamat": "Jl. Budi Utomo No. 7, Jakarta Pusat",
  "latitude": -6.175392,
  "longitude": 106.827153,
  "radius": 200.0
}
```

**Expected Response** (201 Created):
```json
{
  "id": 1,
  "nama": "SMK Negeri 1 Jakarta",
  "alamat": "Jl. Budi Utomo No. 7, Jakarta Pusat",
  "latitude": -6.175392,
  "longitude": 106.827153,
  "radius": 200.0
}
```

**Validation**:
- ✅ Status: 201 Created
- ✅ Response body contains all fields
- ✅ Latitude: -90 to 90 (valid range)
- ✅ Longitude: -180 to 180 (valid range)
- ✅ Radius: positive number (meters)

**Save**: `LOKASI_ID` from response

---

### SCENARIO 2: Get All Lokasi Kantor (ADMIN/GURU)

**Request**:
```
GET {{BASE_URL}}/api/lokasi-kantor
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "nama": "SMK Negeri 1 Jakarta",
    "alamat": "Jl. Budi Utomo No. 7, Jakarta Pusat",
    "latitude": -6.175392,
    "longitude": 106.827153,
    "radius": 200.0
  }
]
```

---

### SCENARIO 3: Get Lokasi by ID (ADMIN/GURU)

**Request**:
```
GET {{BASE_URL}}/api/lokasi-kantor/{{LOKASI_ID}}
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (200 OK):
```json
{
  "id": 1,
  "nama": "SMK Negeri 1 Jakarta",
  "alamat": "Jl. Budi Utomo No. 7, Jakarta Pusat",
  "latitude": -6.175392,
  "longitude": 106.827153,
  "radius": 200.0
}
```

---

### SCENARIO 4: Checkin Within Radius (SUCCESS)

**Purpose**: Checkin dari koordinat yang DALAM radius (< 200m dari kantor)

**Koordinat**:
- Kantor: `-6.175392, 106.827153`
- User: `-6.175500, 106.827200` (sekitar 15 meter dari kantor)
- Distance: ~15m ✅ (< 200m)

**Request**:
```
POST {{BASE_URL}}/api/presensi/checkin
Authorization: Bearer {{SISWA_TOKEN}}
Content-Type: application/json

{
  "tipe": "SISWA",
  "latitude": -6.175500,
  "longitude": 106.827200,
  "keterangan": "Checkin dari depan gerbang sekolah"
}
```

**Expected Response** (201 Created):
```json
{
  "id": 5,
  "userId": 2,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:15:30",
  "jamPulang": null,
  "status": "HADIR",
  "method": "MANUAL",
  "latitude": -6.175500,
  "longitude": 106.827200,
  "keterangan": "Checkin dari depan gerbang sekolah"
}
```

**Validation**:
- ✅ Status: 201 Created
- ✅ Presensi created successfully
- ✅ GPS coordinates saved
- ✅ No error about location

---

### SCENARIO 5: Checkin Outside Radius (ERROR)

**Purpose**: Checkin dari koordinat yang LUAR radius (> 200m dari kantor)

**Koordinat**:
- Kantor: `-6.175392, 106.827153`
- User: `-6.180000, 106.830000` (sekitar 600 meter dari kantor)
- Distance: ~600m ❌ (> 200m)

**Request**:
```
POST {{BASE_URL}}/api/presensi/checkin
Authorization: Bearer {{SISWA_TOKEN}}
Content-Type: application/json

{
  "tipe": "SISWA",
  "latitude": -6.180000,
  "longitude": 106.830000,
  "keterangan": "Checkin dari rumah (tes GPS validation)"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Lokasi Anda terlalu jauh dari kantor/sekolah. Jarak: 600.5 meter (max: 200.0 meter)"
}
```

**Validation**:
- ✅ Status: 400 Bad Request
- ✅ Error message includes distance
- ✅ Presensi NOT created
- ✅ GPS validation working

---

### SCENARIO 6: Checkin Exactly at Office (SUCCESS)

**Purpose**: Checkin dari koordinat TEPAT di kantor (distance = 0m)

**Koordinat**:
- Kantor: `-6.175392, 106.827153`
- User: `-6.175392, 106.827153` (EXACT same)
- Distance: 0m ✅ (perfect!)

**Request**:
```
POST {{BASE_URL}}/api/presensi/checkin
Authorization: Bearer {{SISWA_TOKEN}}
Content-Type: application/json

{
  "tipe": "SISWA",
  "latitude": -6.175392,
  "longitude": 106.827153,
  "keterangan": "Checkin dari ruang guru"
}
```

**Expected Response** (201 Created):
```json
{
  "id": 6,
  "userId": 2,
  "username": "12345",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:16:00",
  "jamPulang": null,
  "status": "HADIR",
  "method": "MANUAL",
  "latitude": -6.175392,
  "longitude": 106.827153,
  "keterangan": "Checkin dari ruang guru"
}
```

---

### SCENARIO 7: Checkin at Boundary (Edge Case)

**Purpose**: Checkin dari koordinat TEPAT di batas radius (distance = 200m)

**Koordinat**:
- Kantor: `-6.175392, 106.827153`
- User: `-6.177192, 106.827153` (200 meter ke selatan)
- Distance: ~200m (boundary)

**Request**:
```
POST {{BASE_URL}}/api/presensi/checkin
Authorization: Bearer {{SISWA_TOKEN}}
Content-Type: application/json

{
  "tipe": "SISWA",
  "latitude": -6.177192,
  "longitude": 106.827153,
  "keterangan": "Checkin dari warung seberang sekolah"
}
```

**Expected**: 
- ✅ SUCCESS (distance ≤ radius)
- OR ❌ ERROR (distance > radius, tergantung presisi)

---

### SCENARIO 8: Update Lokasi Kantor (ADMIN)

**Purpose**: Update koordinat atau radius kantor

**Request**:
```
PUT {{BASE_URL}}/api/lokasi-kantor/{{LOKASI_ID}}
Authorization: Bearer {{ADMIN_TOKEN}}
Content-Type: application/json

{
  "nama": "SMK Negeri 1 Jakarta (Updated)",
  "alamat": "Jl. Budi Utomo No. 7, Jakarta Pusat",
  "latitude": -6.175392,
  "longitude": 106.827153,
  "radius": 300.0
}
```

**Expected Response** (200 OK):
```json
{
  "id": 1,
  "nama": "SMK Negeri 1 Jakarta (Updated)",
  "alamat": "Jl. Budi Utomo No. 7, Jakarta Pusat",
  "latitude": -6.175392,
  "longitude": 106.827153,
  "radius": 300.0
}
```

**Validation**:
- ✅ Radius updated to 300m
- ✅ Nama updated
- ✅ Other fields unchanged

---

### SCENARIO 9: Delete Lokasi Kantor (ADMIN)

**Request**:
```
DELETE {{BASE_URL}}/api/lokasi-kantor/{{LOKASI_ID}}
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected Response** (204 No Content):
```
(empty body)
```

**Validation**:
- ✅ Status: 204 No Content
- ✅ Lokasi deleted from database

---

### SCENARIO 10: Checkin Without GPS (Optional)

**Purpose**: Checkin tanpa mengirim koordinat GPS

**Request**:
```
POST {{BASE_URL}}/api/presensi/checkin
Authorization: Bearer {{SISWA_TOKEN}}
Content-Type: application/json

{
  "tipe": "SISWA",
  "keterangan": "Checkin tanpa GPS (smartphone lama)"
}
```

**Expected**:
- **Option A**: SUCCESS (GPS validation skipped if no coordinates)
- **Option B**: ERROR (GPS required for all checkins)

*Tergantung business rule yang dipilih*

---

## 🧮 HAVERSINE FORMULA EXPLANATION

### Formula
```
a = sin²(Δφ/2) + cos(φ1) × cos(φ2) × sin²(Δλ/2)
c = 2 × atan2(√a, √(1−a))
d = R × c
```

Where:
- `φ` = latitude (in radians)
- `λ` = longitude (in radians)
- `R` = Earth radius = 6371 km
- `d` = distance (in km)

### Java Implementation
```java
public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6371; // Earth radius in km
    
    double lat1Rad = Math.toRadians(lat1);
    double lat2Rad = Math.toRadians(lat2);
    double deltaLat = Math.toRadians(lat2 - lat1);
    double deltaLon = Math.toRadians(lon2 - lon1);
    
    double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
               Math.cos(lat1Rad) * Math.cos(lat2Rad) *
               Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
    
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
    return R * c * 1000; // Convert to meters
}
```

### Example Calculation
```
Point A (Kantor): -6.175392, 106.827153
Point B (User):   -6.175500, 106.827200

Distance: ~15 meters (within 200m radius ✅)
```

---

## 📊 TEST MATRIX

| Test Case | Latitude | Longitude | Distance | Radius | Expected |
|-----------|----------|-----------|----------|--------|----------|
| 1. Exact Location | -6.175392 | 106.827153 | 0m | 200m | ✅ SUCCESS |
| 2. Very Close | -6.175400 | 106.827160 | ~10m | 200m | ✅ SUCCESS |
| 3. Within Radius | -6.175500 | 106.827200 | ~15m | 200m | ✅ SUCCESS |
| 4. At Boundary | -6.177192 | 106.827153 | ~200m | 200m | ✅/❌ Edge |
| 5. Slightly Outside | -6.177300 | 106.827153 | ~212m | 200m | ❌ ERROR |
| 6. Far Outside | -6.180000 | 106.830000 | ~600m | 200m | ❌ ERROR |
| 7. Very Far | -6.200000 | 106.850000 | ~3km | 200m | ❌ ERROR |

---

## 🔧 TROUBLESHOOTING

### Issue 1: Always Rejected (even when close)

**Possible Causes**:
- Latitude/longitude swapped
- Wrong Earth radius (should be 6371 km)
- Degrees not converted to radians

**Check**:
```java
// Correct
double lat1Rad = Math.toRadians(lat1);

// Wrong
double lat1Rad = lat1; // Forgot to convert!
```

### Issue 2: Distance Always 0

**Possible Causes**:
- Comparing wrong coordinates
- Not saving GPS in database

**Check**:
```java
// Make sure saving GPS
presensi.setLatitude(request.latitude());
presensi.setLongitude(request.longitude());
```

### Issue 3: Negative Distance

**Possible Causes**:
- Subtraction in wrong order
- Absolute value not applied

**Fix**: Distance should always be positive (use `Math.abs()` if needed)

---

## ✅ CHECKLIST

- [ ] Can create lokasi kantor (ADMIN)
- [ ] Can get all lokasi (ADMIN/GURU)
- [ ] Can get lokasi by ID
- [ ] Can update lokasi (ADMIN)
- [ ] Can delete lokasi (ADMIN)
- [ ] Checkin SUCCESS when within radius
- [ ] Checkin ERROR when outside radius
- [ ] Distance calculation accurate (Haversine)
- [ ] Error message includes actual distance
- [ ] GPS coordinates saved in presensi table

---

## 📚 REFERENCES

- Haversine Formula: https://en.wikipedia.org/wiki/Haversine_formula
- GPS Coordinates: https://www.latlong.net/
- Earth Radius: 6371 km (mean radius)

---

**Author**: Copilot Assistant  
**Last Updated**: 17 November 2025  
**Next**: POSTMAN-TAHAP-09.md (Reporting & Analytics Testing)
