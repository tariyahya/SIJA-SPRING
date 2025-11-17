# 📮 POSTMAN TESTING - TAHAP 06: BARCODE INTEGRATION

**Dokumen**: Panduan testing Barcode Checkin  
**Tanggal**: 17 November 2024  
**Versi**: 1.0  
**Status**: ✅ COMPLETE  
**Source Files**: 45 files compiled  

---

## 🎯 TUJUAN TESTING

Memastikan sistem barcode bekerja dengan benar:

1. **Endpoint Connectivity** - Barcode endpoint bisa diakses tanpa JWT
2. **Siswa Checkin** - Siswa bisa checkin dengan barcode
3. **Guru Checkin** - Guru bisa checkin dengan barcode
4. **Validation** - Error handling untuk barcode tidak terdaftar
5. **Duplicate Prevention** - Tidak bisa checkin 2x dalam sehari
6. **Status Calculation** - HADIR vs TERLAMBAT otomatis
7. **Method Tracking** - Method = BARCODE tercatat
8. **History Verification** - Data tersimpan dengan benar

---

## 📋 SKENARIO TESTING

### **Skenario 1: Test Endpoint Connectivity** ✅

**Tujuan**: Verifikasi barcode endpoint bisa diakses tanpa authentication.

**Request**:
```http
GET http://localhost:8080/api/presensi/barcode/test
Content-Type: application/json
```

**Expected Response** (200 OK):
```json
"Barcode endpoint is working!"
```

**Validasi**:
- ✅ Status 200 (bukan 403 Forbidden)
- ✅ Response berupa string
- ✅ Tidak perlu JWT token

---

### **Skenario 2: Siswa Checkin via Barcode - HADIR** ✅

**Prerequisite**: 
- Siswa harus sudah terdaftar dengan `barcodeId` (cek via GET /api/siswa atau H2 Console)
- Contoh: `budi_001` dengan barcode `QR001`

**Request**:
```http
POST http://localhost:8080/api/presensi/barcode/checkin
Content-Type: application/json

{
  "barcodeId": "QR001"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 1,
  "userId": 2,
  "username": "budi_001",
  "tipe": "SISWA",
  "tanggal": "2024-11-17",
  "jamMasuk": "07:10:00",
  "jamPulang": null,
  "status": "HADIR",
  "method": "BARCODE",
  "latitude": null,
  "longitude": null,
  "keterangan": "Checkin via Barcode: QR001"
}
```

**Validasi**:
- ✅ Status 200
- ✅ `tipe` = "SISWA"
- ✅ `method` = "BARCODE" (bukan MANUAL/RFID)
- ✅ `status` = "HADIR" (jika jam masuk ≤ 07:15)
- ✅ `keterangan` include barcodeId
- ✅ `latitude`/`longitude` = null (barcode reader fixed location)
- ✅ `jamPulang` = null (belum checkout)

---

### **Skenario 3: Guru Checkin via Barcode - TERLAMBAT** ✅

**Prerequisite**:
- Guru harus sudah terdaftar dengan `barcodeId`
- Contoh: `pak_agus` dengan barcode `QR101`
- Test saat jam > 07:15 (atau ubah jam-masuk di application.properties)

**Request**:
```http
POST http://localhost:8080/api/presensi/barcode/checkin
Content-Type: application/json

{
  "barcodeId": "QR101"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 2,
  "userId": 10,
  "username": "pak_agus",
  "tipe": "GURU",
  "tanggal": "2024-11-17",
  "jamMasuk": "07:30:00",
  "jamPulang": null,
  "status": "TERLAMBAT",
  "method": "BARCODE",
  "latitude": null,
  "longitude": null,
  "keterangan": "Checkin via Barcode: QR101"
}
```

**Validasi**:
- ✅ Status 200
- ✅ `tipe` = "GURU"
- ✅ `method` = "BARCODE"
- ✅ `status` = "TERLAMBAT" (jika jam masuk > 07:15)
- ✅ `keterangan` include barcodeId

---

### **Skenario 4: Barcode Tidak Terdaftar** ❌

**Tujuan**: Validasi error handling untuk barcode tidak terdaftar.

**Request**:
```http
POST http://localhost:8080/api/presensi/barcode/checkin
Content-Type: application/json

{
  "barcodeId": "QR999"
}
```

**Expected Response** (500 Internal Server Error):
```json
{
  "timestamp": "2024-11-17T07:15:30.123+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Barcode tidak terdaftar: QR999",
  "path": "/api/presensi/barcode/checkin"
}
```

**Validasi**:
- ✅ Status 500 (atau 400 jika custom exception handler)
- ✅ Message jelas: "Barcode tidak terdaftar: QR999"
- ✅ Tidak create presensi record

**Note**: Di production, sebaiknya return 404 (Not Found) bukan 500.

---

### **Skenario 5: Duplicate Checkin Prevention** ❌

**Tujuan**: Validasi user tidak bisa checkin 2x dalam sehari.

**Prerequisite**:
- Sudah jalankan Skenario 2 (siswa QR001 sudah checkin hari ini)

**Request**:
```http
POST http://localhost:8080/api/presensi/barcode/checkin
Content-Type: application/json

{
  "barcodeId": "QR001"
}
```

**Expected Response** (500 Internal Server Error):
```json
{
  "timestamp": "2024-11-17T08:00:00.123+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "User dengan barcode QR001 sudah checkin hari ini",
  "path": "/api/presensi/barcode/checkin"
}
```

**Validasi**:
- ✅ Status 500
- ✅ Message jelas: "User dengan barcode QR001 sudah checkin hari ini"
- ✅ Tidak create duplicate record

---

### **Skenario 6: Empty Barcode ID** ❌

**Tujuan**: Validasi @NotBlank bekerja.

**Request**:
```http
POST http://localhost:8080/api/presensi/barcode/checkin
Content-Type: application/json

{
  "barcodeId": ""
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2024-11-17T07:15:30.123+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/presensi/barcode/checkin"
}
```

**Validasi**:
- ✅ Status 400 (bukan 200/500)
- ✅ Validation error dari @NotBlank

---

### **Skenario 7: Verify History - Admin/Guru View** ✅

**Tujuan**: Verifikasi data presensi tersimpan dengan benar.

**Request** (dengan JWT token Admin/Guru):
```http
GET http://localhost:8080/api/presensi
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "userId": 2,
    "username": "budi_001",
    "tipe": "SISWA",
    "tanggal": "2024-11-17",
    "jamMasuk": "07:10:00",
    "jamPulang": null,
    "status": "HADIR",
    "method": "BARCODE",
    "latitude": null,
    "longitude": null,
    "keterangan": "Checkin via Barcode: QR001"
  },
  {
    "id": 2,
    "userId": 10,
    "username": "pak_agus",
    "tipe": "GURU",
    "tanggal": "2024-11-17",
    "jamMasuk": "07:30:00",
    "jamPulang": null,
    "status": "TERLAMBAT",
    "method": "BARCODE",
    "latitude": null,
    "longitude": null,
    "keterangan": "Checkin via Barcode: QR101"
  }
]
```

**Validasi**:
- ✅ Status 200
- ✅ Perlu JWT token (endpoint protected)
- ✅ Data barcode checkin muncul
- ✅ `method` = "BARCODE" tercatat

---

### **Skenario 8: Verify History - Siswa View Own** ✅

**Tujuan**: Verifikasi siswa bisa lihat histori sendiri.

**Request** (dengan JWT token siswa `budi_001`):
```http
GET http://localhost:8080/api/presensi/histori
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "userId": 2,
    "username": "budi_001",
    "tipe": "SISWA",
    "tanggal": "2024-11-17",
    "jamMasuk": "07:10:00",
    "jamPulang": null,
    "status": "HADIR",
    "method": "BARCODE",
    "latitude": null,
    "longitude": null,
    "keterangan": "Checkin via Barcode: QR001"
  }
]
```

**Validasi**:
- ✅ Status 200
- ✅ Hanya data siswa sendiri
- ✅ `method` = "BARCODE" tercatat

---

## 📊 COMPARISON TABLE: MANUAL vs RFID vs BARCODE

| Aspek | Manual Checkin | RFID Checkin | Barcode Checkin |
|-------|---------------|--------------|-----------------|
| **Endpoint** | `/api/presensi/checkin` | `/api/presensi/rfid/checkin` | `/api/presensi/barcode/checkin` |
| **Authentication** | Perlu JWT ✅ | Public (no JWT) 🔓 | Public (no JWT) 🔓 |
| **Input** | None (auto dari JWT) | `rfidCardId` | `barcodeId` |
| **User Detection** | From SecurityContext | Search by rfidCardId | Search by barcodeId |
| **Tipe Detection** | Manual input | Auto (SISWA/GURU) | Auto (SISWA/GURU) |
| **GPS** | Required ✅ | Null (fixed reader) | Null (fixed reader) |
| **Method Value** | MANUAL | RFID | BARCODE |
| **Keterangan** | User input | Auto: "Checkin via RFID: {id}" | Auto: "Checkin via Barcode: {id}" |
| **Use Case** | Smartphone app | Hardware RFID reader | Barcode scanner / QR reader |
| **Speed** | Medium (user interact) | Fast (tap card) | Fast (scan code) |
| **Cost** | Free (software only) | Medium (RFID reader) | Low (barcode scanner) |

---

## 🔧 TROUBLESHOOTING

### **Problem 1: 403 Forbidden saat akses /api/presensi/barcode/test**

**Symptom**:
```json
{
  "timestamp": "2024-11-17T07:15:30.123+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/presensi/barcode/test"
}
```

**Cause**: SecurityConfig belum whitelist barcode endpoints.

**Solution**:
1. Buka `SecurityConfig.java`
2. Tambahkan whitelist:
   ```java
   .requestMatchers("/api/presensi/barcode/**").permitAll()
   ```
3. Restart aplikasi

---

### **Problem 2: "Barcode tidak terdaftar" padahal sudah ada di database**

**Symptom**:
```json
{
  "message": "Barcode tidak terdaftar: QR001"
}
```

**Cause**: Field `barcodeId` di tabel `siswa`/`guru` masih NULL.

**Solution**:
1. Buka H2 Console: http://localhost:8080/h2-console
2. Login (JDBC URL: `jdbc:h2:mem:presensidb`)
3. Update data:
   ```sql
   UPDATE siswa SET barcode_id = 'QR001' WHERE username = 'budi_001';
   UPDATE guru SET barcode_id = 'QR101' WHERE username = 'pak_agus';
   ```
4. Verify:
   ```sql
   SELECT username, barcode_id FROM siswa;
   SELECT username, barcode_id FROM guru;
   ```

---

### **Problem 3: Method masih MANUAL bukan BARCODE**

**Symptom**:
```json
{
  "method": "MANUAL"
}
```

**Cause**: PresensiService tidak set method = BARCODE.

**Solution**:
1. Buka `PresensiService.java`
2. Di method `checkinBarcode()`, pastikan ada:
   ```java
   presensi.setMethod(MethodPresensi.BARCODE);
   ```
3. Recompile: `mvn clean compile`

---

### **Problem 4: Compilation Error - "cannot find symbol BarcodeCheckinRequest"**

**Symptom**:
```
[ERROR] /path/to/BarcodeController.java:[10,50] cannot find symbol
  symbol:   class BarcodeCheckinRequest
  location: package com.smk.presensi.dto.presensi
```

**Cause**: `BarcodeCheckinRequest.java` belum dibuat atau salah package.

**Solution**:
1. Verify file exists:
   ```powershell
   ls backend/src/main/java/com/smk/presensi/dto/presensi/BarcodeCheckinRequest.java
   ```
2. Verify package declaration:
   ```java
   package com.smk.presensi.dto.presensi;
   ```
3. Clean build:
   ```powershell
   cd backend; mvn clean compile
   ```

---

### **Problem 5: Duplicate checkin tidak dideteksi**

**Symptom**: Bisa checkin 2x dalam sehari.

**Cause**: Validation `existsByUserAndTanggal` tidak bekerja.

**Solution**:
1. Verify PresensiRepository punya method:
   ```java
   boolean existsByUserAndTanggal(User user, LocalDate tanggal);
   ```
2. Verify checkinBarcode() call validation:
   ```java
   if (presensiRepository.existsByUserAndTanggal(user, today)) {
       throw new RuntimeException("User dengan barcode " + barcodeId + " sudah checkin hari ini");
   }
   ```

---

## 📈 EXPECTED STATISTICS

Setelah semua testing selesai:

- **Total Source Files**: 45 files compiled
- **New Files (Tahap 6)**: 2 files
  - `BarcodeCheckinRequest.java`
  - `BarcodeController.java`
- **Updated Files**: 4 files
  - `SiswaRepository.java` (+1 method)
  - `GuruRepository.java` (+1 method)
  - `PresensiService.java` (+1 method, +1 import)
  - `SecurityConfig.java` (+1 whitelist)
- **Compilation**: ✅ BUILD SUCCESS
- **Public Endpoints**: 3 endpoints
  - `/api/presensi/rfid/**`
  - `/api/presensi/barcode/**`
  - `/api/auth/**`

---

## 🎓 LEARNING POINTS

### **1. Pattern Replication**

Barcode implementation adalah **replika exact** dari RFID:
- Same DTO structure (1 field: identifier)
- Same controller structure (2 endpoints: checkin + test)
- Same service logic (search by ID → validate → insert)
- Same security config (permitAll)

**Benefit**: Consistency, easy maintenance, less bugs.

### **2. Identifier-Based Auto-Checkin**

RFID dan Barcode sama-sama:
- **No authentication needed** (hardware tidak bisa login)
- **Auto-detect user** (search by identifier)
- **Auto-detect tipe** (SISWA vs GURU)
- **Audit trail** (keterangan include identifier)

**Pattern**: `identifier → find user → checkin`

### **3. Method Enum Value**

Setiap checkin tercatat methodnya:
- `MANUAL` - User checkin sendiri via app
- `RFID` - Tap kartu RFID
- `BARCODE` - Scan barcode/QR code
- `FACE` - Face recognition (Tahap 7)

**Benefit**: Bisa analisis method mana paling efektif.

### **4. Public Endpoint Security**

Public endpoint (`permitAll()`) tetap aman karena:
- **Validation**: Identifier harus terdaftar di database
- **Unique identifier**: Setiap user punya identifier unik
- **Audit trail**: Semua checkin tercatat dengan identifier
- **Rate limiting** (future): Bisa tambah rate limit per IP

**Note**: Ini beda dengan "open API" yang siapa saja bisa akses.

---

## 📚 REFERENCES

1. **Barcode Technology**:
   - CODE128: https://en.wikipedia.org/wiki/Code_128
   - QR Code: https://en.wikipedia.org/wiki/QR_code
   - EAN-13: https://en.wikipedia.org/wiki/International_Article_Number

2. **Barcode Scanners**:
   - USB Barcode Scanner: https://www.zebra.com/us/en/products/scanners.html
   - Smartphone Camera: https://github.com/zxing/zxing (ZXing library)

3. **Spring Security**:
   - permitAll(): https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
   - Request Matchers: https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html#match-by-pattern

4. **Related Documentation**:
   - TASK-6.md (implementation guide)
   - README-TAHAP-06.md (architecture overview)
   - POSTMAN-TAHAP-05.md (RFID testing - similar pattern)

---

## ✅ TESTING CHECKLIST

Pastikan semua scenario sudah di-test:

- [ ] **Skenario 1**: Test endpoint (200 OK, no JWT)
- [ ] **Skenario 2**: Siswa checkin HADIR (method = BARCODE, status = HADIR)
- [ ] **Skenario 3**: Guru checkin TERLAMBAT (method = BARCODE, status = TERLAMBAT)
- [ ] **Skenario 4**: Barcode tidak terdaftar (500/404 error)
- [ ] **Skenario 5**: Duplicate checkin (500 error, message clear)
- [ ] **Skenario 6**: Empty barcodeId (400 validation error)
- [ ] **Skenario 7**: Admin view all presensi (with JWT)
- [ ] **Skenario 8**: Siswa view own history (with JWT)

**Additional Checks**:
- [ ] Compilation SUCCESS (45 files)
- [ ] SecurityConfig whitelist correct
- [ ] PresensiService import BarcodeCheckinRequest
- [ ] Repository methods exist (findByBarcodeId)
- [ ] H2 Console data correct (barcodeId not null)

---

## 🔜 NEXT STEPS

Setelah testing selesai:

1. **Merge ke main**:
   ```powershell
   git add .
   git commit -m "feat: Tahap 06 - Barcode Integration"
   git push origin tahap-06-barcode-basic
   ```

2. **Create Pull Request** (jika pakai GitHub/GitLab)

3. **Update PLAN.MD**:
   - Mark Tahap 6 as COMPLETE ✅
   - Add statistics (45 files, 2 new, 4 updated)

4. **Lanjut ke Tahap 07**:
   - **Topic**: Face Recognition Integration
   - **Challenge**: Image processing, face matching
   - **Technical**: OpenCV or cloud API (AWS Rekognition, Azure Face API)
   - **Difficulty**: ⭐⭐⭐⭐ (lebih complex dari RFID/Barcode)

---

**Document Status**: ✅ COMPLETE  
**Testing Guide**: Ready for QA  
**Last Updated**: 17 November 2024  
**Next**: README-TAHAP-06.md (Architecture Overview)
