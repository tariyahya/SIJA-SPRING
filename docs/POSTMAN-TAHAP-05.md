# POSTMAN TESTING GUIDE - TAHAP 05 (RFID Integration)

## 📋 OVERVIEW

Panduan lengkap testing RFID Presensi dengan Postman.

**Tahap 5 Scope:**
- RFID checkin (public endpoint, no JWT)
- Auto-detect user from rfidCardId
- Auto-detect tipe (SISWA/GURU)

**Base URL:** `http://localhost:8081`

---

## ⚙️ PERSIAPAN

### 1. Start Application

```bash
cd backend
mvn spring-boot:run
```

Wait sampai muncul: `Started PresensiApplication`

### 2. Setup Data Siswa/Guru dengan rfidCardId

**Option A: Via H2 Console**

1. Buka: http://localhost:8081/h2-console
2. Login:
   - JDBC URL: `jdbc:h2:mem:presensidb`
   - User: `sa`
   - Password: (kosong)
3. Run query:

```sql
-- Cek data siswa yang ada
SELECT * FROM siswa;

-- Update rfidCardId untuk siswa ID 1
UPDATE siswa SET rfid_card_id = 'RF001234' WHERE id = 1;

-- Update rfidCardId untuk guru ID 1
UPDATE guru SET rfid_card_id = 'RF999888' WHERE id = 1;

-- Verify
SELECT s.id, s.nama, s.nis, s.rfid_card_id, u.username
FROM siswa s
LEFT JOIN users u ON s.user_id = u.id;

SELECT g.id, g.nama, g.nip, g.rfid_card_id, u.username
FROM guru g
LEFT JOIN users u ON g.user_id = u.id;
```

**Option B: Via API (jika sudah ada endpoint update Siswa/Guru)**

```bash
PUT /api/siswa/1
Body: {
  ...,
  "rfidCardId": "RF001234"
}
```

---

## 🧪 TEST SCENARIOS

### ✅ SCENARIO 1: Test RFID Endpoint Accessibility

**Purpose:** Cek apakah RFID endpoint bisa diakses tanpa JWT (public)

**Request:**
```
GET http://localhost:8081/api/presensi/rfid/test
```

**Headers:** (KOSONG - no Authorization!)

**Expected Response (200 OK):**
```
RFID endpoint is working!
```

**Validation:**
- ✅ No 401 Unauthorized error
- ✅ No 403 Forbidden error
- ✅ Response adalah plain text

---

### ✅ SCENARIO 2: RFID Checkin - Siswa (Success)

**Purpose:** Checkin siswa via RFID card

**Pre-requisite:**
- Siswa dengan rfidCardId='RF001234' sudah ada di database
- Siswa belum checkin hari ini

**Request:**
```
POST http://localhost:8081/api/presensi/rfid/checkin
Content-Type: application/json

{
  "rfidCardId": "RF001234"
}
```

**Headers:**
```
Content-Type: application/json
(NO Authorization header!)
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "userId": 2,
  "username": "budi_siswa",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "21:40:15",
  "jamPulang": null,
  "status": "TERLAMBAT",
  "method": "RFID",
  "latitude": null,
  "longitude": null,
  "keterangan": "Checkin via RFID: RF001234"
}
```

**Validation:**
- ✅ tipe = "SISWA" (auto-detect dari tabel siswa)
- ✅ method = "RFID" (bukan MANUAL)
- ✅ status = "HADIR" atau "TERLAMBAT" (based on jam masuk)
- ✅ keterangan berisi rfidCardId
- ✅ latitude/longitude = null (RFID reader fixed location)

---

### ✅ SCENARIO 3: RFID Checkin - Guru (Success)

**Purpose:** Checkin guru via RFID card

**Pre-requisite:**
- Guru dengan rfidCardId='RF999888' sudah ada di database
- Guru belum checkin hari ini

**Request:**
```
POST http://localhost:8081/api/presensi/rfid/checkin
Content-Type: application/json

{
  "rfidCardId": "RF999888"
}
```

**Expected Response (200 OK):**
```json
{
  "id": 2,
  "userId": 3,
  "username": "pak_guru",
  "tipe": "GURU",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:05:00",
  "jamPulang": null,
  "status": "HADIR",
  "method": "RFID",
  "latitude": null,
  "longitude": null,
  "keterangan": "Checkin via RFID: RF999888"
}
```

**Validation:**
- ✅ tipe = "GURU" (auto-detect dari tabel guru)
- ✅ method = "RFID"
- ✅ Response sama seperti siswa, hanya tipe berbeda

---

### ❌ SCENARIO 4: RFID Checkin - Kartu Tidak Terdaftar

**Purpose:** Error handling untuk rfidCardId yang tidak ada di database

**Request:**
```
POST http://localhost:8081/api/presensi/rfid/checkin
Content-Type: application/json

{
  "rfidCardId": "RF_INVALID_123"
}
```

**Expected Response (500 Internal Server Error):**
```json
{
  "timestamp": "2025-11-17T21:45:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Kartu RFID tidak terdaftar: RF_INVALID_123",
  "path": "/api/presensi/rfid/checkin"
}
```

**Validation:**
- ✅ Error message jelas: "Kartu RFID tidak terdaftar"
- ✅ Menyebutkan rfidCardId yang tidak valid

**Note:** Nanti bisa diperbaiki dengan custom exception handler (return 400 Bad Request instead of 500)

---

### ❌ SCENARIO 5: RFID Checkin - Duplikasi (Sudah Checkin Hari Ini)

**Purpose:** Validasi duplikasi checkin

**Pre-requisite:**
- Siswa dengan rfidCardId='RF001234' SUDAH checkin hari ini

**Request:**
```
POST http://localhost:8081/api/presensi/rfid/checkin
Content-Type: application/json

{
  "rfidCardId": "RF001234"
}
```

**Expected Response (500 Internal Server Error):**
```json
{
  "timestamp": "2025-11-17T21:50:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "User dengan kartu RF001234 sudah checkin hari ini",
  "path": "/api/presensi/rfid/checkin"
}
```

**Validation:**
- ✅ Error message jelas: "sudah checkin hari ini"
- ✅ Mencegah duplikasi presensi

---

### ❌ SCENARIO 6: RFID Checkin - rfidCardId Kosong

**Purpose:** Validasi input (NotBlank)

**Request:**
```
POST http://localhost:8081/api/presensi/rfid/checkin
Content-Type: application/json

{
  "rfidCardId": ""
}
```

**Expected Response (400 Bad Request):**
```json
{
  "timestamp": "2025-11-17T21:55:00",
  "status": 400,
  "error": "Bad Request",
  "message": "RFID Card ID harus diisi",
  "path": "/api/presensi/rfid/checkin"
}
```

**Validation:**
- ✅ Validation error (400 Bad Request)
- ✅ Message dari @NotBlank annotation

---

### ✅ SCENARIO 7: Verify Presensi via Histori (After RFID Checkin)

**Purpose:** Cek apakah presensi via RFID tercatat dengan benar

**Pre-requisite:**
- Siswa sudah checkin via RFID (Scenario 2)
- Siswa punya akun dan sudah login (dapat JWT token)

**Request:**
```
GET http://localhost:8081/api/presensi/histori
Authorization: Bearer <JWT_TOKEN>
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 2,
    "username": "budi_siswa",
    "tipe": "SISWA",
    "tanggal": "2025-11-17",
    "jamMasuk": "21:40:15",
    "jamPulang": null,
    "status": "TERLAMBAT",
    "method": "RFID",
    "latitude": null,
    "longitude": null,
    "keterangan": "Checkin via RFID: RF001234"
  }
]
```

**Validation:**
- ✅ method = "RFID" (bukan MANUAL)
- ✅ Data presensi RFID muncul di histori
- ✅ Sama seperti presensi manual, hanya method berbeda

---

### ✅ SCENARIO 8: Admin Lihat Semua Presensi (Include RFID)

**Purpose:** Cek apakah presensi RFID visible untuk admin

**Pre-requisite:**
- Admin sudah login (dapat JWT token)
- Ada presensi RFID dan MANUAL di database

**Request:**
```
GET http://localhost:8081/api/presensi?tanggal=2025-11-17
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 2,
    "username": "budi_siswa",
    "tipe": "SISWA",
    "tanggal": "2025-11-17",
    "jamMasuk": "21:40:15",
    "jamPulang": null,
    "status": "TERLAMBAT",
    "method": "RFID",
    ...
  },
  {
    "id": 2,
    "userId": 3,
    "username": "pak_guru",
    "tipe": "GURU",
    "tanggal": "2025-11-17",
    "jamMasuk": "07:05:00",
    "jamPulang": null,
    "status": "HADIR",
    "method": "MANUAL",
    ...
  }
]
```

**Validation:**
- ✅ Presensi RFID dan MANUAL muncul semua
- ✅ Bisa dibedakan dari field `method`

---

## 🔄 COMPLETE FLOW TEST

### Flow: RFID Checkin → Checkout Manual → Verify

**Step 1: RFID Checkin (Pagi)**
```
POST /api/presensi/rfid/checkin
Body: { "rfidCardId": "RF001234" }
→ Success: method=RFID, jamMasuk=07:05, jamPulang=null
```

**Step 2: Login (untuk checkout nanti)**
```
POST /api/auth/login
Body: { "username": "budi_siswa", "password": "password123" }
→ Success: Dapat JWT token
```

**Step 3: Checkout Manual (Sore)**
```
POST /api/presensi/checkout
Authorization: Bearer <TOKEN>
Body: { "keterangan": "Pulang ke rumah" }
→ Success: jamPulang=15:30
```

**Step 4: Verify Histori**
```
GET /api/presensi/histori
Authorization: Bearer <TOKEN>
→ Success: Ada 1 record dengan jamMasuk (RFID) dan jamPulang (updated)
```

**Validation:**
- ✅ Checkin via RFID (no login)
- ✅ Checkout via manual (need login)
- ✅ Data konsisten di histori

---

## 📊 COMPARISON: Manual vs RFID Checkin

| Aspek | Manual Checkin | RFID Checkin |
|-------|----------------|--------------|
| **Endpoint** | POST /api/presensi/checkin | POST /api/presensi/rfid/checkin |
| **Authentication** | ✅ Required (JWT) | ❌ Not required (public) |
| **User identification** | From SecurityContext | From rfidCardId |
| **Request body** | tipe, lat, long, keterangan | rfidCardId only |
| **Tipe detection** | Client specify (SISWA/GURU) | Server auto-detect |
| **Method value** | MANUAL | RFID |
| **GPS tracking** | ✅ Optional (lat/long) | ❌ No (null) |
| **Keterangan** | User input | Auto-generated |
| **Use case** | Checkin via app (mobile/web) | Tap card di mesin |

---

## 🚨 TROUBLESHOOTING

### Problem 1: 401 Unauthorized saat test RFID endpoint

**Symptom:**
```json
{
  "status": 401,
  "error": "Unauthorized"
}
```

**Cause:** Endpoint belum di-whitelist di SecurityConfig

**Solution:**
Cek `SecurityConfig.java`:
```java
.requestMatchers("/api/presensi/rfid/**").permitAll()
```

### Problem 2: "Kartu RFID tidak terdaftar"

**Symptom:**
```json
{
  "message": "Kartu RFID tidak terdaftar: RF001234"
}
```

**Cause:** Belum ada data siswa/guru dengan rfidCardId tersebut

**Solution:**
```sql
UPDATE siswa SET rfid_card_id = 'RF001234' WHERE id = 1;
```

### Problem 3: User null di Siswa/Guru

**Symptom:**
```
NullPointerException at siswa.getUser()
```

**Cause:** Field `user_id` di tabel siswa/guru adalah NULL

**Solution:**
```sql
-- Cek user_id
SELECT id, nama, user_id FROM siswa WHERE id = 1;

-- Update jika null
UPDATE siswa SET user_id = 2 WHERE id = 1;
```

### Problem 4: rfidCardId null di database

**Symptom:** Error "Kartu RFID tidak terdaftar" padahal siswa ada

**Cause:** Field rfid_card_id masih NULL

**Solution:**
```sql
-- Cek rfid_card_id
SELECT id, nama, nis, rfid_card_id FROM siswa;

-- Update
UPDATE siswa SET rfid_card_id = 'RF001234' WHERE id = 1;
```

---

## 📚 TIPS & BEST PRACTICES

### 1. Test Order

Selalu test dengan urutan:
1. Test endpoint accessibility dulu (GET /test)
2. Persiapkan data (update rfidCardId)
3. Test success case
4. Test error cases
5. Test integration dengan endpoint lain

### 2. Data Preparation

Sebelum test RFID:
- Pastikan ada user (siswa/guru)
- Pastikan user punya relasi OneToOne dengan User entity
- Pastikan rfidCardId sudah diset (not null)
- Pastikan rfidCardId unique

### 3. Error Handling

Error saat ini return 500 Internal Server Error.
Improvement: Buat custom exception handler untuk return 400 Bad Request dengan message lebih jelas.

### 4. Security

- RFID endpoint public (no JWT) → aman karena rfidCardId harus terdaftar
- Tidak ada password bypass → user tetap perlu account untuk checkout/view history
- RFID hanya untuk checkin, fitur lain tetap perlu authentication

---

## ✅ CHECKLIST

Before moving to Tahap 6:
- [x] Test RFID endpoint accessibility (no 401/403)
- [x] Test RFID checkin siswa (success)
- [x] Test RFID checkin guru (success)
- [x] Test error: kartu tidak terdaftar
- [x] Test error: duplikasi checkin
- [x] Test validation: rfidCardId kosong
- [x] Verify presensi RFID muncul di histori
- [x] Verify admin bisa lihat presensi RFID
- [x] Test complete flow: RFID checkin → manual checkout

---

## 🚀 NEXT: Tahap 6 - Barcode Integration

Similar flow dengan RFID:
- POST /api/presensi/barcode/checkin
- Body: { "barcodeId": "BC123456" }
- Auto-detect user from barcodeId
- Method = BARCODE

**Stay tuned!** 🎉
