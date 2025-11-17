# 📮 POSTMAN TESTING - TAHAP 07: FACE RECOGNITION INTEGRATION

**Dokumen**: Panduan testing Face Recognition (Simplified Version)  
**Tanggal**: 17 November 2024  
**Versi**: 1.0  
**Status**: ✅ COMPLETE  
**Source Files**: 50 files compiled  

---

## 🎯 TUJUAN TESTING

Memastikan sistem face recognition bekerja dengan benar:

1. **Endpoint Connectivity** - Face endpoints bisa diakses tanpa JWT
2. **Enrollment Phase** - Admin bisa enroll face user (siswa/guru)
3. **Recognition Phase** - User bisa checkin dengan face recognition
4. **Validation** - Error handling untuk face tidak enrolled, duplicate, dll
5. **Method Tracking** - Method = FACE tercatat
6. **Similarity Algorithm** - Face matching dengan threshold 60%

⚠️ **CATATAN PENTING**:
Ini adalah **SIMULASI** face recognition untuk pembelajaran. Bukan real face detection. Menggunakan SHA-256 hash dari image bytes untuk "encoding". Foto yang identik 100% akan match, tapi perubahan kecil (crop, rotate, compress) akan gagal match.

---

## 📋 SKENARIO TESTING

### **Skenario 1: Test Endpoint Connectivity** ✅

**Tujuan**: Verifikasi face endpoints bisa diakses tanpa authentication.

**Request**:
```http
GET http://localhost:8080/api/presensi/face/test
Content-Type: application/json
```

**Expected Response** (200 OK):
```json
"Face Recognition endpoint is working!"
```

**Validasi**:
- ✅ Status 200 (bukan 403 Forbidden)
- ✅ Response berupa string
- ✅ Tidak perlu JWT token

---

### **Skenario 2: Enrollment - Siswa** ✅

**Prerequisite**: 
- Siswa harus sudah terdaftar dengan user_id
- Contoh: userId=2 (budi_001)

**Cara get image base64**:
1. Ambil foto (JPEG/PNG)
2. Convert ke base64 online: https://www.base64-image.de/
3. Copy hasil (include prefix "data:image/jpeg;base64,...")

**Request**:
```http
POST http://localhost:8080/api/presensi/face/enroll
Content-Type: application/json

{
  "userId": 2,
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD..."
}
```

**Expected Response** (200 OK):
```json
{
  "userId": 2,
  "username": "budi_001",
  "faceEncodingLength": 128,
  "enrolledAt": "2024-11-17T10:30:00",
  "message": "Face berhasil di-enroll untuk siswa budi_001"
}
```

**Validasi**:
- ✅ Status 200
- ✅ faceEncodingLength = 128 (always)
- ✅ enrolledAt adalah timestamp sekarang
- ✅ Message clear dan informatif

**Verify di H2 Console**:
```sql
SELECT username, face_encoding, face_enrolled_at 
FROM siswa 
WHERE user_id = 2;

-- Should show:
-- username: budi_001
-- face_encoding: "a3f5b2c9..." (128 chars)
-- face_enrolled_at: 2024-11-17 10:30:00
```

---

### **Skenario 3: Enrollment - Guru** ✅

**Prerequisite**: 
- Guru harus sudah terdaftar dengan user_id
- Contoh: userId=10 (pak_agus)

**Request**:
```http
POST http://localhost:8080/api/presensi/face/enroll
Content-Type: application/json

{
  "userId": 10,
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD..."
}
```

**Expected Response** (200 OK):
```json
{
  "userId": 10,
  "username": "pak_agus",
  "faceEncodingLength": 128,
  "enrolledAt": "2024-11-17T10:35:00",
  "message": "Face berhasil di-enroll untuk guru pak_agus"
}
```

**Validasi**:
- ✅ Status 200
- ✅ Message berbeda: "untuk guru" (bukan "untuk siswa")

---

### **Skenario 4: Face Recognition - Siswa Checkin HADIR** ✅

**Prerequisite**:
- Siswa sudah enroll face (Skenario 2)
- Gunakan **IMAGE YANG SAMA PERSIS** dengan enrollment
- Test sebelum jam 07:15 (atau sesuaikan jam-masuk di application.properties)

**Request**:
```http
POST http://localhost:8080/api/presensi/face/checkin
Content-Type: application/json

{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD..."
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
  "method": "FACE",
  "latitude": null,
  "longitude": null,
  "keterangan": "Checkin via Face Recognition: budi_001"
}
```

**Validasi**:
- ✅ Status 200
- ✅ `method` = "FACE" (bukan MANUAL/RFID/BARCODE)
- ✅ `tipe` = "SISWA"
- ✅ `status` = "HADIR" (jika jam masuk ≤ 07:15)
- ✅ `keterangan` include username
- ✅ `latitude`/`longitude` = null (camera fixed location)

---

### **Skenario 5: Face Recognition - Guru Checkin TERLAMBAT** ✅

**Prerequisite**:
- Guru sudah enroll face (Skenario 3)
- Gunakan **IMAGE YANG SAMA** dengan enrollment
- Test setelah jam 07:15

**Request**:
```http
POST http://localhost:8080/api/presensi/face/checkin
Content-Type: application/json

{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD..."
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
  "method": "FACE",
  "latitude": null,
  "longitude": null,
  "keterangan": "Checkin via Face Recognition: pak_agus"
}
```

**Validasi**:
- ✅ `tipe` = "GURU"
- ✅ `status` = "TERLAMBAT" (jika jam > 07:15)
- ✅ `method` = "FACE"

---

### **Skenario 6: Face Tidak Dikenali** ❌

**Tujuan**: Validasi face tidak enrolled atau image berbeda.

**Request**:
```http
POST http://localhost:8080/api/presensi/face/checkin
Content-Type: application/json

{
  "imageBase64": "data:image/jpeg;base64,/9j/DIFFERENT_IMAGE..."
}
```

**Expected Response** (404 Not Found):
```json
"Face tidak dikenali. Pastikan sudah enrollment dan foto clear."
```

**Validasi**:
- ✅ Status 404
- ✅ Message informatif
- ✅ Tidak create presensi record

**Note**: Karena ini simulasi (hash-based), maka:
- Image yang PERSIS SAMA → match 100%
- Image yang SEDIKIT berbeda → tidak match (0%)
- Real face recognition lebih robust (toleran terhadap perubahan kecil)

---

### **Skenario 7: Enrollment - User ID Tidak Ditemukan** ❌

**Tujuan**: Validasi user ID harus exist.

**Request**:
```http
POST http://localhost:8080/api/presensi/face/enroll
Content-Type: application/json

{
  "userId": 999,
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD..."
}
```

**Expected Response** (400 Bad Request):
```json
"Enrollment gagal: User ID 999 tidak ditemukan"
```

**Validasi**:
- ✅ Status 400
- ✅ Message clear: user tidak ditemukan

---

### **Skenario 8: Enrollment - Image Too Small** ❌

**Tujuan**: Validasi image size minimal 1 KB.

**Request**:
```http
POST http://localhost:8080/api/presensi/face/enroll
Content-Type: application/json

{
  "userId": 2,
  "imageBase64": "data:image/jpeg;base64,ABC"
}
```

**Expected Response** (400 Bad Request):
```json
"Enrollment gagal: Invalid base64 image: Image too small: 3 bytes (min: 1 KB)"
```

**Validasi**:
- ✅ Status 400
- ✅ Message informatif tentang size

---

### **Skenario 9: Duplicate Checkin Prevention** ❌

**Tujuan**: Validasi tidak bisa checkin 2x dalam sehari.

**Prerequisite**:
- Sudah checkin sekali (Skenario 4 atau 5)

**Request**:
```http
POST http://localhost:8080/api/presensi/face/checkin
Content-Type: application/json

{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD..."
}
```

**Expected Response** (400 Bad Request):
```json
"Checkin gagal: Siswa budi_001 sudah checkin hari ini"
```

**Validasi**:
- ✅ Status 400
- ✅ Message clear: sudah checkin
- ✅ Tidak create duplicate record

---

### **Skenario 10: Re-Enrollment (Update Face)** ✅

**Tujuan**: User bisa re-enroll dengan foto baru.

**Prerequisite**:
- User sudah pernah enroll (Skenario 2)

**Request**:
```http
POST http://localhost:8080/api/presensi/face/enroll
Content-Type: application/json

{
  "userId": 2,
  "imageBase64": "data:image/jpeg;base64,/9j/NEW_PHOTO..."
}
```

**Expected Response** (200 OK):
```json
{
  "userId": 2,
  "username": "budi_001",
  "faceEncodingLength": 128,
  "enrolledAt": "2024-11-17T14:00:00",
  "message": "Face berhasil di-enroll untuk siswa budi_001"
}
```

**Validasi**:
- ✅ Status 200
- ✅ `enrolledAt` updated (timestamp baru)
- ✅ face_encoding di database ter-replace dengan encoding baru

**Verify di H2 Console**:
```sql
SELECT username, face_encoding, face_enrolled_at 
FROM siswa 
WHERE user_id = 2;

-- face_encoding should be different
-- face_enrolled_at should be updated
```

---

## 📊 COMPARISON TABLE: MANUAL vs RFID vs BARCODE vs FACE

| Aspek | Manual | RFID | Barcode | Face Recognition |
|-------|--------|------|---------|------------------|
| **Endpoint** | `/checkin` | `/rfid/checkin` | `/barcode/checkin` | `/face/checkin` |
| **Authentication** | JWT ✅ | Public 🔓 | Public 🔓 | Public 🔓 |
| **Input** | None (JWT) | rfidCardId | barcodeId | imageBase64 |
| **User Detection** | SecurityContext | Search by ID | Search by ID | Face matching |
| **Enrollment** | No | Pre-registered | Pre-printed | POST /enroll |
| **Hardware** | Smartphone | RFID reader | Barcode scanner | Camera |
| **Speed** | Medium (5s) | Fast (1-2s) | Fast (1-3s) | Slow (2-5s) |
| **Cost** | $0 | $50-200 | $20-100 | $100-500 |
| **Security** | Medium (GPS) | Low (card lost) | Low (printable) | High (biometric) |
| **UX** | Manual input | Tap card | Scan code | Just face |
| **Spoofing Risk** | Low | Medium (steal card) | Medium (copy code) | High (photo) |
| **Method Value** | MANUAL | RFID | BARCODE | FACE |

---

## 🔧 TROUBLESHOOTING

### **Problem 1: 403 Forbidden saat akses /face/test**

**Symptom**:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

**Cause**: SecurityConfig belum whitelist face endpoints.

**Solution**:
1. Buka `SecurityConfig.java`
2. Tambahkan:
   ```java
   .requestMatchers("/api/presensi/face/**").permitAll()
   ```
3. Restart aplikasi

---

### **Problem 2: Face tidak match padahal foto sama**

**Symptom**:
```json
"Face tidak dikenali. Pastikan sudah enrollment dan foto clear."
```

**Cause**: Image byte berbeda (compression, metadata, resize).

**Solution**:
Untuk testing, pastikan:
1. **COPY-PASTE** base64 string yang EXACT SAMA
2. Jangan re-upload file (metadata bisa beda)
3. Gunakan base64 dari clipboard yang sama

**Note**: Ini limitasi simplified version. Real face recognition lebih robust.

---

### **Problem 3: "Image too small" error**

**Symptom**:
```json
"Enrollment gagal: Image too small: 500 bytes (min: 1 KB)"
```

**Cause**: Image kurang dari 1 KB.

**Solution**:
1. Gunakan foto resolusi minimal 100x100px
2. Format JPEG atau PNG
3. Size: 1 KB - 10 MB

---

### **Problem 4: Compilation Error - "cannot find symbol findByUser"**

**Symptom**:
```
[ERROR] cannot find symbol
  symbol:   method findByUser(User)
  location: variable siswaRepository
```

**Cause**: Repository belum punya method `findByUser`.

**Solution**:
1. Tambah di `SiswaRepository.java`:
   ```java
   Optional<Siswa> findByUser(User user);
   ```
2. Tambah di `GuruRepository.java`:
   ```java
   Optional<Guru> findByUser(User user);
   ```
3. Recompile: `mvn clean compile`

---

### **Problem 5: Face encoding tidak tersimpan**

**Symptom**: Enrollment sukses tapi `face_encoding` masih NULL di database.

**Cause**: Transaction tidak commit.

**Solution**:
1. Verify `@Transactional` ada di `FaceController.enroll()`
2. Check H2 Console:
   ```sql
   SELECT * FROM siswa WHERE user_id = 2;
   ```
3. Jika masih NULL, check log error di console

---

## 📈 EXPECTED STATISTICS

Setelah semua testing selesai:

- **Total Source Files**: 50 files compiled
- **New Files (Tahap 7)**: 5 files
  - `FaceEnrollmentRequest.java`
  - `FaceCheckinRequest.java`
  - `FaceEnrollmentResponse.java`
  - `FaceRecognitionService.java`
  - `FaceController.java`
- **Updated Files**: 6 files
  - `Siswa.java` (+2 fields: faceEncoding, faceEnrolledAt)
  - `Guru.java` (+2 fields: faceEncoding, faceEnrolledAt)
  - `SiswaRepository.java` (+2 methods)
  - `GuruRepository.java` (+2 methods)
  - `PresensiService.java` (+2 methods: checkinFace for Siswa & Guru)
  - `SecurityConfig.java` (+1 whitelist)
- **Compilation**: ✅ BUILD SUCCESS
- **Public Endpoints**: 4 groups
  - `/api/auth/**`
  - `/api/presensi/rfid/**`
  - `/api/presensi/barcode/**`
  - `/api/presensi/face/**`

---

## 🎓 LEARNING POINTS

### **1. Biometric Authentication Concepts**

**Enrollment vs Recognition**:
- **Enrollment**: Register biometric template (one-time)
- **Recognition**: Match live biometric dengan template tersimpan

**Threshold Decision**:
- Similarity > 0.6 (60%) → MATCH
- Similarity < 0.6 → NO MATCH

**False Positive vs False Negative**:
- False Positive: Orang berbeda dianggap sama (security risk)
- False Negative: Orang sama ditolak (usability issue)

### **2. SHA-256 Hash Simulation**

Kita pakai SHA-256 untuk "simulasi" face encoding:
```
Image bytes → SHA-256 hash → Hex string (64 chars) → Take first 128 chars
```

**Benefit**:
- Simple (no ML library)
- Consistent (same input = same output)
- Fast (< 1ms)

**Limitation**:
- Not robust (slight change = different hash)
- Not real face detection

### **3. Levenshtein Distance**

Algoritma untuk hitung similarity dua string:
```
"abc" vs "adc" → distance = 1 → similarity = 67%
"abc" vs "xyz" → distance = 3 → similarity = 0%
```

Formula:
```
similarity = 1.0 - (distance / max_length)
```

### **4. Method Enum Completion**

Setelah Tahap 7, semua method checkin sudah complete:
```java
public enum MethodPresensi {
    MANUAL,     // ✅ Tahap 04
    RFID,       // ✅ Tahap 05
    BARCODE,    // ✅ Tahap 06
    FACE        // ✅ Tahap 07 (now)
}
```

**Progress**: 100% (4/4 methods)

---

## 📚 REFERENCES

1. **Face Recognition (Real)**:
   - face_recognition library: https://github.com/ageitgey/face_recognition
   - AWS Rekognition: https://aws.amazon.com/rekognition/
   - Azure Face API: https://azure.microsoft.com/services/cognitive-services/face/
   - OpenCV DNN: https://docs.opencv.org/master/d2/d58/tutorial_table_of_content_dnn.html

2. **Algorithms**:
   - SHA-256: https://en.wikipedia.org/wiki/SHA-2
   - Levenshtein distance: https://en.wikipedia.org/wiki/Levenshtein_distance
   - Cosine similarity: https://en.wikipedia.org/wiki/Cosine_similarity

3. **Related Documentation**:
   - TASK-7.md (implementation guide)
   - README-TAHAP-07.md (architecture overview)
   - POSTMAN-TAHAP-05.md (RFID - similar pattern)

---

## ✅ TESTING CHECKLIST

Pastikan semua scenario sudah di-test:

- [ ] **Skenario 1**: Test endpoint connectivity (200 OK, no JWT)
- [ ] **Skenario 2**: Enrollment siswa (encoding saved, 128 chars)
- [ ] **Skenario 3**: Enrollment guru (encoding saved)
- [ ] **Skenario 4**: Face recognition siswa HADIR (method=FACE, status=HADIR)
- [ ] **Skenario 5**: Face recognition guru TERLAMBAT (method=FACE)
- [ ] **Skenario 6**: Face tidak dikenali (404 error)
- [ ] **Skenario 7**: Enrollment user tidak ditemukan (400 error)
- [ ] **Skenario 8**: Image too small (400 error, validation message)
- [ ] **Skenario 9**: Duplicate checkin (400 error, clear message)
- [ ] **Skenario 10**: Re-enrollment (encoding updated)

**Additional Checks**:
- [ ] Compilation SUCCESS (50 files)
- [ ] SecurityConfig whitelist correct
- [ ] All repositories have required methods
- [ ] H2 Console data correct (faceEncoding not null)
- [ ] PresensiService has checkinFace methods (2 overloads)

---

## 🔜 NEXT STEPS

Setelah testing selesai:

1. **Production Upgrade** (jika deploy real):
   - Replace FaceRecognitionService dengan real library
   - Tambah liveness detection (anti-spoofing)
   - Optimize matching performance (vector database)

2. **Mobile App Integration** (Tahap 8-10):
   - Camera capture UI
   - Base64 conversion
   - API call ke `/face/checkin`

3. **Desktop Admin Panel** (Tahap 11-12):
   - Enrollment UI (upload foto user)
   - View enrolled users
   - Re-enrollment management

4. **Merge to Main**:
   ```powershell
   git add .
   git commit -m "feat: Tahap 07 - Face Recognition (Simplified)"
   git push origin tahap-07-face-recognition
   ```

---

**Document Status**: ✅ COMPLETE  
**Testing Guide**: Ready for QA  
**Last Updated**: 17 November 2024  
**Next**: README-TAHAP-07.md (Architecture Overview)
