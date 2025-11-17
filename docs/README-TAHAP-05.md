# README - TAHAP 05: RFID INTEGRATION

## 🎯 OVERVIEW

Tahap 5 menambahkan fitur **RFID (Radio Frequency Identification)** untuk presensi otomatis tanpa perlu login.

**Key Features:**
- ✅ RFID checkin via public endpoint (no JWT authentication)
- ✅ Auto-detect user from rfidCardId
- ✅ Auto-detect tipe (SISWA/GURU) from database table
- ✅ Method tracking: MANUAL vs RFID vs BARCODE vs FACE
- ✅ Security whitelist for RFID endpoints

**Use Case:**
- Siswa/guru tap kartu RFID di mesin presensi
- RFID reader kirim rfidCardId ke backend
- Backend auto-checkin tanpa perlu login

---

## 🏗️ ARCHITECTURE

### System Flow

```
┌─────────────────┐
│  RFID Reader    │ Hardware baca kartu
│  (Hardware)     │
└────────┬────────┘
         │ HTTP POST
         │ { "rfidCardId": "RF001234" }
         ↓
┌─────────────────────────────┐
│ POST /api/presensi/rfid/    │ Public endpoint
│      checkin                 │ (no JWT)
└────────┬────────────────────┘
         │
         ↓
┌─────────────────┐
│ RfidController  │
└────────┬────────┘
         │
         ↓
┌──────────────────────┐
│ PresensiService      │
│ - checkinRfid()      │
└────────┬─────────────┘
         │
         ├──→ SiswaRepository.findByRfidCardId()
         │    ↓
         │    Found? → user = siswa.getUser(), tipe = SISWA
         │
         ├──→ GuruRepository.findByRfidCardId()
         │    ↓
         │    Found? → user = guru.getUser(), tipe = GURU
         │
         ├──→ Not found? → Error "Kartu tidak terdaftar"
         │
         ├──→ PresensiRepository.existsByUserAndTanggal()
         │    ↓
         │    Sudah checkin? → Error "Sudah checkin hari ini"
         │
         └──→ PresensiRepository.save()
              ↓
              Success: Return PresensiResponse
```

### Database Schema

**No changes** - Field `rfidCardId` already exists from Tahap 2:

```sql
-- Table siswa
CREATE TABLE siswa (
  id BIGINT PRIMARY KEY,
  nis VARCHAR(20) UNIQUE,
  nama VARCHAR(100),
  rfid_card_id VARCHAR(50) UNIQUE,  -- ✅ Already exists
  user_id BIGINT,
  ...
);

-- Table guru
CREATE TABLE guru (
  id BIGINT PRIMARY KEY,
  nip VARCHAR(20) UNIQUE,
  nama VARCHAR(100),
  rfid_card_id VARCHAR(50) UNIQUE,  -- ✅ Already exists
  user_id BIGINT,
  ...
);

-- Table presensi
CREATE TABLE presensi (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  tipe VARCHAR(10) NOT NULL,        -- SISWA or GURU
  tanggal DATE NOT NULL,
  jam_masuk TIME,
  jam_pulang TIME,
  status VARCHAR(15) NOT NULL,      -- HADIR, TERLAMBAT, etc
  method VARCHAR(10) NOT NULL,      -- MANUAL, RFID, BARCODE, FACE
  ...
);
```

---

## 📁 FILES CREATED/UPDATED

### New Files (2)

1. **RfidCheckinRequest.java** (DTO)
   - Path: `dto/presensi/RfidCheckinRequest.java`
   - Lines: ~20
   - Purpose: Request DTO dengan 1 field: `rfidCardId`

2. **RfidController.java** (Controller)
   - Path: `controller/RfidController.java`
   - Lines: ~120
   - Endpoints:
     - `POST /api/presensi/rfid/checkin` - RFID checkin
     - `GET /api/presensi/rfid/test` - Test endpoint accessibility

### Updated Files (4)

3. **SiswaRepository.java** (+1 method)
   - Method: `Optional<Siswa> findByRfidCardId(String rfidCardId)`
   - Purpose: Cari siswa by RFID card ID

4. **GuruRepository.java** (+1 method)
   - Method: `Optional<Guru> findByRfidCardId(String rfidCardId)`
   - Purpose: Cari guru by RFID card ID

5. **PresensiService.java** (+1 method, +2 dependencies)
   - Inject: `SiswaRepository`, `GuruRepository`
   - Method: `PresensiResponse checkinRfid(RfidCheckinRequest)`
   - Lines: ~80
   - Logic:
     - Cari user by rfidCardId (siswa/guru)
     - Auto-detect tipe
     - Validasi duplikasi
     - Insert presensi dengan method=RFID

6. **SecurityConfig.java** (+1 whitelist)
   - Line: `.requestMatchers("/api/presensi/rfid/**").permitAll()`
   - Purpose: Allow public access to RFID endpoints

---

## 🔑 KEY CONCEPTS

### 1. Public Endpoint

**Manual Checkin (Tahap 4):**
```java
@PreAuthorize("hasAnyRole('SISWA', 'GURU')")
@PostMapping("/checkin")
public ResponseEntity<...> checkin(...) {
    // Perlu JWT token
}
```

**RFID Checkin (Tahap 5):**
```java
@PostMapping("/rfid/checkin")
public ResponseEntity<...> checkinRfid(...) {
    // No @PreAuthorize = public access
}
```

**SecurityConfig:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/presensi/rfid/**").permitAll()  // NEW!
    .anyRequest().authenticated()
)
```

### 2. Auto-detect User & Tipe

**Challenge:**
- Field `rfidCardId` ada di Siswa/Guru (bukan User)
- Perlu cari di 2 tabel

**Solution:**
```java
// Cari di Siswa dulu
Optional<Siswa> siswaOpt = siswaRepository.findByRfidCardId(rfidCardId);
if (siswaOpt.isPresent()) {
    user = siswaOpt.get().getUser();  // OneToOne relationship
    tipe = TipeUser.SISWA;
} else {
    // Kalau tidak ada, cari di Guru
    Optional<Guru> guruOpt = guruRepository.findByRfidCardId(rfidCardId);
    if (guruOpt.isPresent()) {
        user = guruOpt.get().getUser();
        tipe = TipeUser.GURU;
    }
}

if (user == null) {
    throw new RuntimeException("Kartu RFID tidak terdaftar");
}
```

### 3. Method Tracking

**Purpose:** Bedakan cara checkin

```java
public enum MethodPresensi {
    MANUAL,   // Tahap 4: Checkin via app (with login)
    RFID,     // Tahap 5: Tap kartu RFID
    BARCODE,  // Tahap 6: Scan barcode
    FACE      // Tahap 7: Face recognition
}
```

**Usage:**
- Manual checkin → `method = MANUAL`
- RFID checkin → `method = RFID`

**Benefit:**
- Statistik: % checkin via RFID vs manual
- Audit: user ini checkin pakai cara apa?
- Troubleshooting: jika ada masalah, cek method-nya

---

## 🧪 TESTING

### Quick Test

**1. Test endpoint accessibility:**
```bash
curl http://localhost:8081/api/presensi/rfid/test
# Expected: "RFID endpoint is working!"
```

**2. Setup data:**
```sql
-- H2 Console: http://localhost:8081/h2-console
UPDATE siswa SET rfid_card_id = 'RF001234' WHERE id = 1;
```

**3. RFID checkin:**
```bash
curl -X POST http://localhost:8081/api/presensi/rfid/checkin \
  -H "Content-Type: application/json" \
  -d '{"rfidCardId":"RF001234"}'
```

**Expected response:**
```json
{
  "id": 1,
  "userId": 2,
  "username": "budi",
  "tipe": "SISWA",
  "tanggal": "2025-11-17",
  "jamMasuk": "07:05:30",
  "status": "HADIR",
  "method": "RFID",
  "keterangan": "Checkin via RFID: RF001234"
}
```

**Full testing guide:** See `POSTMAN-TAHAP-05.md`

---

## 📊 STATISTICS

**Code Changes:**
- Files created: 2
- Files updated: 4
- Lines of code: ~220 (excluding comments)
- Endpoints added: 2
- Repository methods: 2
- Service methods: 1

**Features:**
- ✅ RFID checkin (public endpoint)
- ✅ Auto-detect user from rfidCardId
- ✅ Auto-detect tipe (SISWA/GURU)
- ✅ Method tracking (MANUAL/RFID)
- ✅ Duplicate checkin validation
- ✅ Security whitelist

**Build:**
- Source files: 43 (was 41 in Tahap 4)
- Build status: ✅ SUCCESS
- Test status: ✅ All scenarios pass

---

## 🔄 COMPARISON: Tahap 4 vs Tahap 5

| Aspect | Tahap 4 (Manual) | Tahap 5 (RFID) |
|--------|------------------|----------------|
| **Authentication** | ✅ JWT required | ❌ No JWT (public) |
| **Endpoint** | /api/presensi/checkin | /api/presensi/rfid/checkin |
| **User ID** | From SecurityContext | From rfidCardId |
| **Tipe** | Client specify | Server auto-detect |
| **Request body** | tipe, lat, long, keterangan | rfidCardId only |
| **Method value** | MANUAL | RFID |
| **GPS tracking** | ✅ Optional | ❌ No (null) |
| **Use case** | Mobile/web app | Hardware RFID reader |

---

## 🚨 TROUBLESHOOTING

### 1. Error 401 Unauthorized

**Cause:** Endpoint belum di-whitelist

**Solution:**
```java
// SecurityConfig.java
.requestMatchers("/api/presensi/rfid/**").permitAll()
```

### 2. "Kartu RFID tidak terdaftar"

**Cause:** rfidCardId tidak ada di database

**Solution:**
```sql
UPDATE siswa SET rfid_card_id = 'RF001234' WHERE id = 1;
```

### 3. User null / NullPointerException

**Cause:** Field `user_id` di siswa/guru adalah NULL

**Solution:**
```sql
UPDATE siswa SET user_id = 2 WHERE id = 1;
```

---

## 🎓 LEARNING OUTCOMES

Setelah Tahap 5, Anda memahami:

1. **Public Endpoints** - Cara whitelist endpoint tanpa JWT
2. **Multi-table Query** - Cari user di 2 tabel berbeda
3. **Auto-detection** - Detect tipe (SISWA/GURU) dari tabel yang ditemukan
4. **Method Tracking** - Track cara checkin (MANUAL/RFID/etc)
5. **OneToOne Relationship** - Navigate dari Siswa/Guru ke User
6. **Security Consideration** - Public endpoint tapi tetap aman (validasi rfidCardId)

---

## 🚀 NEXT STEPS

**Tahap 6 - Barcode Integration:**
- POST /api/presensi/barcode/checkin
- Find user by barcodeId
- Generate barcode per user
- QR code support

**Tahap 7 - Face Recognition:**
- POST /api/presensi/face/checkin (with photo)
- Face matching algorithm
- Liveness detection
- Enrollment process

**Tahap 8 - Hardware Integration:**
- Real RFID reader (serial/USB/network)
- Arduino/Raspberry Pi
- WebSocket for real-time updates
- LED/buzzer feedback

---

## 📚 REFERENCES

- **TASK-5.md** - Step-by-step implementation guide
- **POSTMAN-TAHAP-05.md** - Complete testing scenarios
- **TASK-4.md** - Manual checkin (for comparison)
- RFID Technology: https://en.wikipedia.org/wiki/Radio-frequency_identification
- Spring Security permitAll: https://docs.spring.io/spring-security/reference/

---

**Status: ✅ TAHAP 5 COMPLETE!**

Next: `git add . && git commit -m "feat: Tahap 05 - RFID Integration" && git push`
