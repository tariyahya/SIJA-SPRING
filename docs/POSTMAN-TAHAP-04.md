# POSTMAN TESTING - TAHAP 04 PRESENSI MANUAL

## 📋 OVERVIEW

Testing untuk sistem presensi manual dengan flow:
1. **Checkin** (pagi) → Insert record presensi dengan jam masuk
2. **Checkout** (sore) → Update record dengan jam pulang
3. **History** → Lihat presensi history sendiri
4. **Get All** → Admin/Guru lihat semua presensi

---

## 🔐 PREPARATION

### 1. Login untuk dapat Token

Gunakan admin atau register user baru dengan role SISWA/GURU.

**Request:**
```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

**Save token** untuk request selanjutnya.

### 2. Register User SISWA (opsional)

```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "username": "budi_siswa",
  "email": "budi@student.smk.sch.id",
  "password": "siswa123",
  "role": "SISWA"
}
```

### 3. Register User GURU (opsional)

```http
POST {{base_url}}/api/auth/register
Content-Type: application/json

{
  "username": "pak_budi",
  "email": "budi@teacher.smk.sch.id",
  "password": "guru123",
  "role": "GURU"
}
```

---

## 📝 TEST SCENARIOS

### SCENARIO 1: Checkin Normal (Tepat Waktu)

User checkin sebelum jam 07:15 → Status HADIR

**Request:**
```http
POST {{base_url}}/api/presensi/checkin
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "tipe": "SISWA",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Checkin pagi hari"
}
```

**Expected Response: 201 Created**
```json
{
  "id": 1,
  "userId": 1,
  "username": "budi_siswa",
  "tipe": "SISWA",
  "tanggal": "2024-01-15",
  "jamMasuk": "07:05:30",
  "jamPulang": null,
  "status": "HADIR",
  "method": "MANUAL",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Checkin pagi hari"
}
```

**Validation:**
- ✅ Status = "HADIR" (karena < 07:15)
- ✅ jamMasuk ada, jamPulang null
- ✅ method = "MANUAL"
- ✅ ID auto-generated

---

### SCENARIO 2: Checkin Terlambat

User checkin setelah jam 07:15 → Status TERLAMBAT

**Setup:** Ubah jam sistem atau tunggu sampai lewat 07:15

**Request:**
```http
POST {{base_url}}/api/presensi/checkin
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "tipe": "SISWA",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Ban bocor di jalan"
}
```

**Expected Response: 201 Created**
```json
{
  "id": 2,
  "userId": 1,
  "username": "budi_siswa",
  "tipe": "SISWA",
  "tanggal": "2024-01-15",
  "jamMasuk": "07:20:15",
  "jamPulang": null,
  "status": "TERLAMBAT",
  "method": "MANUAL",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Ban bocor di jalan"
}
```

**Validation:**
- ✅ Status = "TERLAMBAT" (karena > 07:15)
- ✅ Keterangan bisa diisi alasan terlambat

---

### SCENARIO 3: Checkin Duplikasi (Should FAIL)

User sudah checkin hari ini, checkin lagi → Error

**Request:**
```http
POST {{base_url}}/api/presensi/checkin
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "tipe": "SISWA"
}
```

**Expected Response: 500 Internal Server Error**
```json
{
  "timestamp": "2024-01-15T07:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Anda sudah checkin hari ini",
  "path": "/api/presensi/checkin"
}
```

**Validation:**
- ✅ Error message jelas
- ✅ Tidak insert record duplikat
- ⚠️ Status code 500 → nanti Tahap 5 improve jadi 400 Bad Request

---

### SCENARIO 4: Checkout (Normal)

User checkout setelah checkin

**Request:**
```http
POST {{base_url}}/api/presensi/checkout
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "latitude": -6.200100,
  "longitude": 106.816766,
  "keterangan": "Pulang tepat waktu"
}
```

**Expected Response: 200 OK**
```json
{
  "id": 1,
  "userId": 1,
  "username": "budi_siswa",
  "tipe": "SISWA",
  "tanggal": "2024-01-15",
  "jamMasuk": "07:05:30",
  "jamPulang": "15:05:45",
  "status": "HADIR",
  "method": "MANUAL",
  "latitude": -6.200100,
  "longitude": 106.816766,
  "keterangan": "Checkin pagi hari | Checkout: Pulang tepat waktu"
}
```

**Validation:**
- ✅ jamPulang sudah terisi
- ✅ Status tetap "HADIR"
- ✅ Keterangan append (checkin + checkout)
- ✅ GPS bisa beda (checkin vs checkout location)

---

### SCENARIO 5: Checkout Tanpa Checkin (Should FAIL)

User checkout tapi belum checkin → Error

**Request:**
```http
POST {{base_url}}/api/presensi/checkout
Authorization: Bearer {{token_user_belum_checkin}}
Content-Type: application/json

{
  "keterangan": "Pulang"
}
```

**Expected Response: 500 Internal Server Error**
```json
{
  "message": "Anda belum checkin hari ini"
}
```

**Validation:**
- ✅ Validasi: harus checkin dulu sebelum checkout

---

### SCENARIO 6: Checkout Duplikasi (Should FAIL)

User sudah checkout, checkout lagi → Error

**Request:**
```http
POST {{base_url}}/api/presensi/checkout
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "keterangan": "Pulang lagi"
}
```

**Expected Response: 500 Internal Server Error**
```json
{
  "message": "Anda sudah checkout hari ini"
}
```

**Validation:**
- ✅ Tidak boleh checkout 2x

---

### SCENARIO 7: Get History Presensi Sendiri

User lihat history presensi sendiri

**Request:**
```http
GET {{base_url}}/api/presensi/histori
Authorization: Bearer {{token}}
```

**Expected Response: 200 OK**
```json
[
  {
    "id": 5,
    "userId": 1,
    "username": "budi_siswa",
    "tipe": "SISWA",
    "tanggal": "2024-01-15",
    "jamMasuk": "07:05:30",
    "jamPulang": "15:05:45",
    "status": "HADIR",
    "method": "MANUAL",
    "latitude": -6.200100,
    "longitude": 106.816766,
    "keterangan": "..."
  },
  {
    "id": 4,
    "userId": 1,
    "username": "budi_siswa",
    "tipe": "SISWA",
    "tanggal": "2024-01-14",
    "jamMasuk": "07:10:00",
    "jamPulang": "15:10:00",
    "status": "HADIR",
    "method": "MANUAL",
    "latitude": null,
    "longitude": null,
    "keterangan": null
  }
]
```

**Validation:**
- ✅ Sorted by tanggal DESC (terbaru di atas)
- ✅ Hanya presensi user sendiri
- ✅ Bisa kosong [] jika belum pernah presensi

---

### SCENARIO 8: Get History dengan Filter Tanggal

Filter presensi dalam range tanggal

**Request:**
```http
GET {{base_url}}/api/presensi/histori?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {{token}}
```

**Expected Response: 200 OK**
```json
[
  {...},
  {...}
]
```

**Validation:**
- ✅ Hanya return presensi dalam range
- ✅ Format tanggal: YYYY-MM-DD

---

### SCENARIO 9: Get All Presensi (ADMIN/GURU)

Admin atau Guru lihat semua presensi

**Request:**
```http
GET {{base_url}}/api/presensi
Authorization: Bearer {{admin_token}}
```

**Expected Response: 200 OK**
```json
[
  {
    "id": 1,
    "userId": 2,
    "username": "budi_siswa",
    "tipe": "SISWA",
    "tanggal": "2024-01-15",
    "jamMasuk": "07:05:30",
    "jamPulang": "15:05:45",
    "status": "HADIR",
    "method": "MANUAL",
    ...
  },
  {
    "id": 2,
    "userId": 3,
    "username": "ani_siswa",
    "tipe": "SISWA",
    "tanggal": "2024-01-15",
    "jamMasuk": "07:20:00",
    "jamPulang": null,
    "status": "TERLAMBAT",
    "method": "MANUAL",
    ...
  }
]
```

**Validation:**
- ✅ Return semua presensi (semua user)
- ✅ Bisa filter by tanggal: `?tanggal=2024-01-15`

---

### SCENARIO 10: Get All Presensi with Filter

Filter presensi by tanggal

**Request:**
```http
GET {{base_url}}/api/presensi?tanggal=2024-01-15
Authorization: Bearer {{admin_token}}
```

**Expected Response: 200 OK**
```json
[
  {...presensi tanggal 2024-01-15 saja...}
]
```

---

### SCENARIO 11: Access Control - SISWA cannot see all

SISWA coba akses GET all → 403 Forbidden

**Request:**
```http
GET {{base_url}}/api/presensi
Authorization: Bearer {{siswa_token}}
```

**Expected Response: 403 Forbidden**
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

**Validation:**
- ✅ SISWA tidak bisa lihat presensi orang lain
- ✅ Hanya bisa lihat history sendiri via /histori

---

## 🧪 COMPLETE FLOW TEST

### Flow: Checkin → Checkout → View History

**Step 1: Login**
```http
POST /api/auth/login
```

**Step 2: Checkin Pagi**
```http
POST /api/presensi/checkin
{
  "tipe": "SISWA",
  "latitude": -6.200000,
  "longitude": 106.816666
}
→ 201 Created, status = "HADIR"
```

**Step 3: Try Checkin Again (should fail)**
```http
POST /api/presensi/checkin
→ 500 Error: "Anda sudah checkin hari ini"
```

**Step 4: Checkout Sore**
```http
POST /api/presensi/checkout
{
  "latitude": -6.200100,
  "longitude": 106.816766
}
→ 200 OK, jamPulang filled
```

**Step 5: Try Checkout Again (should fail)**
```http
POST /api/presensi/checkout
→ 500 Error: "Anda sudah checkout hari ini"
```

**Step 6: View History**
```http
GET /api/presensi/histori
→ 200 OK, show today's presensi
```

---

## 🎯 SUCCESS CRITERIA

Checklist untuk verify implementation sukses:

- ✅ User bisa checkin (POST /checkin)
- ✅ Status auto-set HADIR jika < 07:15
- ✅ Status auto-set TERLAMBAT jika > 07:15
- ✅ Validasi: tidak boleh checkin 2x per hari
- ✅ User bisa checkout (POST /checkout)
- ✅ Validasi: harus checkin dulu sebelum checkout
- ✅ Validasi: tidak boleh checkout 2x
- ✅ User bisa lihat history sendiri (GET /histori)
- ✅ Filter history by date range
- ✅ ADMIN/GURU bisa lihat all presensi (GET /presensi)
- ✅ SISWA tidak bisa lihat all presensi (403)
- ✅ GPS latitude/longitude tersimpan
- ✅ Keterangan bisa diisi
- ✅ Method = "MANUAL" untuk semua

---

## 🐛 COMMON ERRORS

### Error 401 Unauthorized
**Cause:** Token expired atau tidak ada  
**Solution:** Login ulang untuk dapat token baru

### Error 403 Forbidden
**Cause:** Role tidak cukup (SISWA coba akses GET all)  
**Solution:** Login dengan ADMIN atau GURU

### Error 500: "Anda sudah checkin hari ini"
**Cause:** User sudah checkin hari ini  
**Solution:** Tunggu besok untuk checkin lagi (atau test dengan user berbeda)

### Error 500: "Anda belum checkin hari ini"
**Cause:** User checkout tapi belum checkin  
**Solution:** Checkin dulu sebelum checkout

---

## 📊 TEST STATISTICS

**Total Endpoints:** 4
- POST /api/presensi/checkin
- POST /api/presensi/checkout
- GET /api/presensi/histori
- GET /api/presensi

**Total Test Scenarios:** 11
- ✅ Checkin normal
- ✅ Checkin terlambat
- ✅ Checkin duplikasi (fail)
- ✅ Checkout normal
- ✅ Checkout tanpa checkin (fail)
- ✅ Checkout duplikasi (fail)
- ✅ Get history
- ✅ Get history dengan filter
- ✅ Get all presensi
- ✅ Get all dengan filter
- ✅ Access control

---

**Ready to test! 🚀**

*Last Updated: 2025-01-16*  
*Branch: tahap-04-presensi-basic*
