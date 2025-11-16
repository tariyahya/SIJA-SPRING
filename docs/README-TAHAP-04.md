# README TAHAP 04 - PRESENSI MANUAL

## 🎯 OVERVIEW

Tahap 04 mengimplementasikan **sistem presensi manual** dengan flow checkin/checkout. Ini adalah fondasi sebelum integrasi hardware (RFID/Barcode/Face) di tahap selanjutnya.

**Status:** ✅ **COMPLETE**  
**Branch:** `tahap-04-presensi-basic`

---

## 📋 FEATURES IMPLEMENTED

### Core Features
- ✅ **Checkin** - User tap/input presensi pagi hari
- ✅ **Checkout** - User tap/input presensi sore hari
- ✅ **Status Auto-Calculate** - HADIR vs TERLAMBAT based on jam masuk
- ✅ **Validation** - Prevent duplicate checkin/checkout
- ✅ **History** - View personal attendance history
- ✅ **Admin View** - Admin/Guru can see all attendance
- ✅ **GPS Tracking** - Save latitude/longitude for location validation (future use)

### Business Rules
- **Jam Masuk:** 07:00 (configurable)
- **Toleransi:** 15 menit (configurable)
- **Status Logic:**
  - Checkin ≤ 07:15 → **HADIR**
  - Checkin > 07:15 → **TERLAMBAT**
- **Validation:**
  - 1 user max 1 checkin per day
  - Must checkin before checkout
  - Cannot checkout twice

---

## 🏗️ ARCHITECTURE

### Database Schema

```sql
-- Table: presensi
CREATE TABLE presensi (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,              -- FK to users table
    tipe VARCHAR(10) NOT NULL,            -- SISWA or GURU
    tanggal DATE NOT NULL,                -- 2024-01-15
    jam_masuk TIME,                       -- 07:05:30
    jam_pulang TIME,                      -- 15:05:45
    status VARCHAR(15) NOT NULL,          -- HADIR, TERLAMBAT, IZIN, SAKIT, ALPHA
    method VARCHAR(10) NOT NULL,          -- MANUAL, RFID, BARCODE, FACE
    latitude DOUBLE,                      -- -6.200000
    longitude DOUBLE,                     -- 106.816666
    keterangan VARCHAR(500),              -- Optional notes
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Indexes
CREATE INDEX idx_presensi_user_tanggal ON presensi(user_id, tanggal);
CREATE INDEX idx_presensi_tanggal ON presensi(tanggal);
```

### Enums

```java
// TipeUser.java
public enum TipeUser {
    SISWA,
    GURU
}

// StatusPresensi.java
public enum StatusPresensi {
    HADIR,       // Checkin on time
    TERLAMBAT,   // Checkin late
    IZIN,        // Excused absence (manual input by admin)
    SAKIT,       // Sick leave (manual input by admin)
    ALPHA        // Absent without excuse (auto-set end of day)
}

// MethodPresensi.java
public enum MethodPresensi {
    MANUAL,      // Manual input (Tahap 4)
    RFID,        // RFID card tap (Tahap 5)
    BARCODE,     // Barcode/QR scan (Tahap 6)
    FACE         // Face recognition (Tahap 7)
}
```

### Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        USER LOGIN                            │
│                  (Get JWT Token)                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   CHECKIN (Pagi)                             │
│  POST /api/presensi/checkin                                  │
│  ├─ Validate: sudah checkin hari ini? → Error               │
│  ├─ Insert record: tanggal, jamMasuk = now()                │
│  ├─ Calculate status:                                        │
│  │   - jamMasuk ≤ 07:15 → HADIR                             │
│  │   - jamMasuk > 07:15 → TERLAMBAT                         │
│  └─ Save GPS (lat/lon) if provided                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   CHECKOUT (Sore)                            │
│  POST /api/presensi/checkout                                 │
│  ├─ Validate: sudah checkin? → Error if not                 │
│  ├─ Validate: sudah checkout? → Error if yes                │
│  ├─ Update record: jamPulang = now()                        │
│  └─ Update GPS if provided                                  │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   VIEW HISTORY                               │
│  GET /api/presensi/histori                                   │
│  └─ Return: User's own attendance records                   │
│                                                              │
│                   ADMIN VIEW ALL                             │
│  GET /api/presensi                                           │
│  └─ Return: All users' attendance (ADMIN/GURU only)         │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 FILES CREATED

### Enums (3 files)
1. `backend/.../enums/TipeUser.java`
2. `backend/.../enums/StatusPresensi.java`
3. `backend/.../enums/MethodPresensi.java`

### Entity (1 file)
4. `backend/.../entity/Presensi.java`

### Repository (1 file)
5. `backend/.../repository/PresensiRepository.java`

### DTOs (3 files)
6. `backend/.../dto/presensi/CheckinRequest.java`
7. `backend/.../dto/presensi/CheckoutRequest.java`
8. `backend/.../dto/presensi/PresensiResponse.java`

### Service (1 file)
9. `backend/.../service/PresensiService.java`

### Controller (1 file)
10. `backend/.../controller/PresensiController.java`

### Config (1 file - updated)
11. `backend/src/main/resources/application.properties` (added presensi config)

### Documentation (3 files)
12. `docs/TASK-4.md` - Implementation guide
13. `docs/POSTMAN-TAHAP-04.md` - API testing scenarios
14. `docs/README-TAHAP-04.md` - This file

**Total:** 14 files (11 code + 3 docs)

---

## 🔌 API ENDPOINTS

### Presensi Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/presensi/checkin` | SISWA, GURU | Checkin presensi (pagi) |
| POST | `/api/presensi/checkout` | SISWA, GURU | Checkout presensi (sore) |
| GET | `/api/presensi/histori` | SISWA, GURU | View own attendance history |
| GET | `/api/presensi/histori?startDate=...&endDate=...` | SISWA, GURU | View history with date filter |
| GET | `/api/presensi` | ADMIN, GURU | View all attendance |
| GET | `/api/presensi?tanggal=...` | ADMIN, GURU | View all attendance by date |

### Request/Response Examples

**Checkin Request:**
```json
POST /api/presensi/checkin
Authorization: Bearer <token>

{
  "tipe": "SISWA",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Checkin pagi"
}
```

**Checkin Response:**
```json
{
  "id": 1,
  "userId": 2,
  "username": "budi_siswa",
  "tipe": "SISWA",
  "tanggal": "2024-01-15",
  "jamMasuk": "07:05:30",
  "jamPulang": null,
  "status": "HADIR",
  "method": "MANUAL",
  "latitude": -6.200000,
  "longitude": 106.816666,
  "keterangan": "Checkin pagi"
}
```

**Checkout Request:**
```json
POST /api/presensi/checkout
Authorization: Bearer <token>

{
  "latitude": -6.200100,
  "longitude": 106.816766,
  "keterangan": "Pulang tepat waktu"
}
```

---

## ⚙️ CONFIGURATION

**application.properties:**
```properties
# Jam masuk normal (format HH:mm:ss)
presensi.jam-masuk=07:00:00

# Toleransi keterlambatan (menit)
presensi.toleransi-menit=15

# Jam pulang normal (reference only, not validated in Tahap 4)
presensi.jam-pulang=15:00:00
```

**Customize:**
- Change `presensi.jam-masuk` to school's start time
- Change `presensi.toleransi-menit` for late tolerance
- Values are injected to PresensiService via `@Value`

---

## 🔐 ROLE-BASED ACCESS CONTROL

| Action | ADMIN | GURU | SISWA |
|--------|-------|------|-------|
| Checkin sendiri | ✅ | ✅ | ✅ |
| Checkout sendiri | ✅ | ✅ | ✅ |
| Lihat history sendiri | ✅ | ✅ | ✅ |
| Lihat semua presensi | ✅ | ✅ | ❌ |
| Input manual IZIN/SAKIT | ✅ | ❌ | ❌ |

**Note:** 
- Semua user dengan role SISWA atau GURU bisa checkin/checkout
- Hanya ADMIN/GURU bisa lihat presensi semua orang
- SISWA hanya bisa lihat history sendiri

---

## 🧪 TESTING GUIDE

### Quick Test Flow

**1. Login:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**2. Checkin:**
```bash
curl -X POST http://localhost:8081/api/presensi/checkin \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"tipe":"GURU","latitude":-6.2,"longitude":106.8}'
```

**3. Checkout:**
```bash
curl -X POST http://localhost:8081/api/presensi/checkout \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"latitude":-6.2,"longitude":106.8}'
```

**4. View History:**
```bash
curl -X GET http://localhost:8081/api/presensi/histori \
  -H "Authorization: Bearer <token>"
```

**See full testing guide:** `docs/POSTMAN-TAHAP-04.md`

---

## 📊 STATISTICS

### Code Metrics
- **Files created:** 11 code files
- **Lines of code:** ~800 lines (entities, services, controllers)
- **Enums:** 3 (TipeUser, StatusPresensi, MethodPresensi)
- **Entity fields:** 11 (Presensi entity)
- **API endpoints:** 4
- **Business rules:** 5+ validations

### Build Status
```
[INFO] BUILD SUCCESS
[INFO] Compiling 41 source files
[INFO] Total time: 5.989 s
```

### Test Coverage
- ✅ Checkin normal (HADIR)
- ✅ Checkin terlambat (TERLAMBAT)
- ✅ Checkin duplikasi (validation)
- ✅ Checkout normal
- ✅ Checkout tanpa checkin (validation)
- ✅ Checkout duplikasi (validation)
- ✅ History view
- ✅ History filter by date
- ✅ Admin view all
- ✅ Access control

---

## ✅ SUCCESS CRITERIA

Implementation complete when:

- ✅ User bisa checkin dengan POST /checkin
- ✅ Status auto-calculate: HADIR vs TERLAMBAT
- ✅ Validation: No duplicate checkin per day
- ✅ User bisa checkout dengan POST /checkout
- ✅ Validation: Must checkin before checkout
- ✅ Validation: No duplicate checkout
- ✅ User bisa lihat history via GET /histori
- ✅ Filter history by date range
- ✅ ADMIN/GURU bisa lihat all via GET /presensi
- ✅ SISWA cannot access GET /presensi (403)
- ✅ GPS latitude/longitude saved
- ✅ Keterangan field works
- ✅ Method = "MANUAL" for all records
- ✅ Build berhasil (mvn clean compile)
- ✅ No compilation errors

---

## 🚀 NEXT STEPS (TAHAP 5)

Dengan Tahap 4 selesai, siap untuk:

1. **RFID Integration (Tahap 5)**
   - Add RFID card reader support
   - Endpoint: POST /api/presensi/rfid/checkin
   - Match rfidCardId with Siswa/Guru
   - Auto-checkin when card tapped

2. **Barcode/QR Integration (Tahap 6)**
   - Scan barcode from ID card
   - Generate QR code for each student
   - Mobile app scan QR → checkin

3. **Face Recognition (Tahap 7)**
   - Camera capture face
   - Match with face_id in database
   - Auto-checkin if match

---

## 🐛 KNOWN LIMITATIONS

### Tahap 4 Scope
- ✅ Manual input only (no hardware yet)
- ✅ Simple status calculation (HADIR/TERLAMBAT only)
- ✅ No ALPHA auto-set (manual by admin)
- ✅ No IZIN/SAKIT workflow (manual input)
- ✅ GPS not validated (just saved)
- ✅ Error handling basic (500 errors)

### Future Improvements (Tahap 5+)
- [ ] Better error handling (custom exceptions, 400/404 status codes)
- [ ] ALPHA auto-set at end of day (scheduled job)
- [ ] IZIN/SAKIT approval workflow
- [ ] GPS geofencing validation
- [ ] Notification (push/email) when checkin/checkout
- [ ] Dashboard statistics
- [ ] Export to Excel/PDF
- [ ] Real-time attendance monitoring

---

## 📚 DOCUMENTATION REFERENCE

- **Implementation Guide:** `docs/TASK-4.md`
- **API Testing:** `docs/POSTMAN-TAHAP-04.md`
- **This Summary:** `docs/README-TAHAP-04.md`

---

## 🎓 LEARNING OUTCOMES

Students learned:

1. **Entity Design**
   - Complex entity with multiple relationships
   - Enum usage for fixed values
   - LocalDate & LocalTime for date/time handling

2. **Business Logic**
   - Status calculation based on time
   - Duplicate validation
   - Sequential workflow (checkin → checkout)

3. **Repository Methods**
   - Custom query methods (findByUserAndTanggal)
   - Exists queries for validation
   - Date range queries

4. **Service Layer**
   - Transaction management (@Transactional)
   - Get current user from SecurityContext
   - Entity ↔ DTO conversion

5. **API Design**
   - RESTful endpoints
   - Request validation
   - Date query parameters
   - Role-based access control

---

**🎉 Tahap 04 COMPLETE!**

Ready for hardware integration in Tahap 5-7! 🚀

---

*Last Updated: 2025-01-16*  
*Branch: tahap-04-presensi-basic*  
*Status: ✅ COMPLETE*
