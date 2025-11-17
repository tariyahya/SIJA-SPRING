# 📝 BLOG 4: Memahami Sistem Presensi Manual (TAHAP 4)

**Penulis**: Copilot Assistant  
**Tanggal**: 17 November 2024  
**Topik**: Presensi Manual dengan GPS Tracking  
**Target**: Siswa SMK SIJA (Sistem Informasi Jaringan dan Aplikasi)

---

## 🎯 APA YANG AKAN KITA PELAJARI?

Di blog ini, kita akan belajar:
1. **Konsep Presensi Digital** vs absensi kertas
2. **Status Presensi** (HADIR, TERLAMBAT, ALFA)
3. **GPS Tracking** untuk validasi lokasi
4. **Enum Pattern** di Java untuk konstanta
5. **Business Logic** (checkin, checkout, validasi)
6. **Audit Trail** untuk tracking aktivitas

---

## 📖 CERITA: DARI ABSENSI KERTAS KE DIGITAL

### Dulu (Absensi Kertas):

Bayangkan kamu siswa di tahun 1990-an. Setiap pagi:

```
1. Guru bawa kertas absensi ke kelas
2. Siswa dipanggil satu-satu: "Andi? Hadir! Budi? Hadir! ..."
3. Guru tulis tangan di kertas
4. Kalau ada yang terlambat, tulis manual + waktu datang
5. Akhir bulan: guru hitung manual berapa kali hadir/alfa
```

**Masalah**:
- ⏰ **Lama**: 5-10 menit per kelas (waste time)
- 📄 **Kertas bisa hilang**: Kehilangan data 1 bulan!
- ❌ **Rawan salah hitung**: Manual counting error
- 🤝 **Titip absen**: Teman bisa titip bilang "hadir" padahal bolos

---

### Sekarang (Presensi Digital):

Dengan sistem digital:

```
1. Siswa buka app di smartphone
2. Tap tombol "Checkin"
3. Sistem auto-detect: user, waktu, lokasi (GPS)
4. Sistem auto-calculate: HADIR atau TERLAMBAT
5. Data langsung masuk database
6. Laporan bisa digenerate otomatis
```

**Benefit**:
- ⚡ **Cepat**: < 5 detik per orang
- 💾 **Aman**: Data tersimpan di database (backup)
- ✅ **Akurat**: Auto-calculate, no human error
- 🌍 **GPS Tracking**: Tahu lokasi checkin (prevent titip absen)

---

## 🧩 KONSEP DASAR: STATUS PRESENSI

### 1. Status Presensi (3 jenis)

**HADIR** ✅:
- Checkin **sebelum** jam batas (misal: 07:15)
- Contoh: Checkin jam 07:00 → **HADIR**

**TERLAMBAT** ⏰:
- Checkin **setelah** jam batas
- Contoh: Checkin jam 07:30 → **TERLAMBAT**

**ALFA** ❌:
- **Tidak checkin sama sekali**
- Sistem tidak create record presensi
- Laporan akan show "ALFA" untuk tanggal tersebut

### Analogi: Kereta Api

```
Kereta berangkat jam 08:00

- Kamu datang jam 07:45 → ✅ Naik kereta (HADIR)
- Kamu datang jam 08:05 → ⏰ Kereta sudah jalan (TERLAMBAT)
- Kamu tidak datang → ❌ Ketinggalan (ALFA)
```

---

## 🕒 BUSINESS LOGIC: HITUNG STATUS

Dalam code kita (PresensiService.java):

```java
private StatusPresensi hitungStatus(LocalTime jamMasuk) {
    LocalTime JAM_MASUK_BATAS = LocalTime.of(7, 15); // 07:15
    
    if (jamMasuk.isBefore(JAM_MASUK_BATAS)) {
        return StatusPresensi.HADIR;        // ✅ Before 07:15
    } else {
        return StatusPresensi.TERLAMBAT;    // ⏰ After 07:15
    }
}
```

**Cara kerja**:
1. Ambil waktu checkin user (misal: `07:10`)
2. Compare dengan jam batas (`07:15`)
3. If `07:10` < `07:15` → `HADIR` ✅
4. If `07:20` > `07:15` → `TERLAMBAT` ⏰

### Example:

```
Checkin 06:45 → HADIR ✅ (35 menit lebih awal)
Checkin 07:00 → HADIR ✅ (15 menit lebih awal)
Checkin 07:14 → HADIR ✅ (1 menit lebih awal)
Checkin 07:15 → TERLAMBAT ⏰ (tepat jam batas)
Checkin 07:16 → TERLAMBAT ⏰ (1 menit terlambat)
Checkin 08:00 → TERLAMBAT ⏰ (45 menit terlambat)
```

---

## 🌍 GPS TRACKING: VALIDASI LOKASI

### Kenapa perlu GPS?

**Problem**: Siswa bisa **titip absen** dari rumah!

```
Scenario tanpa GPS:
1. Andi di rumah (sakit/bolos)
2. Minta teman: "Tolong absenin aku ya, login pakai akunku"
3. Teman checkin pakai account Andi
4. Sistem record: Andi HADIR ✅ (padahal bolos!)
```

**Solution dengan GPS**:

```
Scenario dengan GPS:
1. Andi minta teman checkin dari sekolah
2. Sistem record: 
   - Username: andi
   - Lat/Long: -6.200000, 106.816666 (Jakarta Pusat, sekolah) ✅
3. Besok Andi coba checkin dari rumah:
   - Username: andi
   - Lat/Long: -6.300000, 106.900000 (Depok, rumah) ❌
4. Sistem bisa validasi: "Lokasi terlalu jauh dari sekolah!"
```

### Data yang Disimpan

Setiap checkin, kita simpan:

```java
Presensi {
    userId = 2 (Andi)
    tanggal = 2024-11-17
    jamMasuk = 07:10:30
    status = HADIR
    latitude = -6.200000    // GPS: latitude
    longitude = 106.816666  // GPS: longitude
    method = MANUAL
    keterangan = "Checkin dari smartphone"
}
```

**Manfaat**:
- Admin bisa **audit**: "Checkin dari mana?"
- Laporan bisa show **peta lokasi** checkin
- Detect **anomali**: Checkin dari lokasi aneh

---

## 🔢 ENUM PATTERN: KONSTANTA DI JAVA

### Apa itu Enum?

**Enum** = **Enumeration** = daftar nilai tetap (fixed values).

### Problem tanpa Enum:

```java
// BAD: Pakai String (error-prone)
public class Presensi {
    private String status; // "HADIR", "TERLAMBAT", "ALFA"
}

// Masalah:
presensi.setStatus("hadir");       // lowercase (bug!)
presensi.setStatus("HADIRR");      // typo (bug!)
presensi.setStatus("TERLAMBATT");  // typo (bug!)
presensi.setStatus("SAKIT");       // invalid value (bug!)
```

Compiler **tidak bisa detect** error karena semuanya String valid!

### Solution dengan Enum:

```java
// GOOD: Pakai Enum (type-safe)
public enum StatusPresensi {
    HADIR,
    TERLAMBAT,
    ALFA
}

public class Presensi {
    private StatusPresensi status; // Only 3 values allowed!
}

// Usage:
presensi.setStatus(StatusPresensi.HADIR);        // ✅ Valid
presensi.setStatus(StatusPresensi.TERLAMBAT);   // ✅ Valid
presensi.setStatus(StatusPresensi.SAKIT);       // ❌ Compile error!
```

**Benefit**:
- ✅ **Type-safe**: Compiler check validitas
- ✅ **Auto-complete**: IDE suggest values
- ✅ **No typo**: Salah ketik langsung error
- ✅ **Readable**: Code lebih jelas

### Enum di Aplikasi Kita

Kita punya **3 Enum**:

**1. StatusPresensi**:
```java
public enum StatusPresensi {
    HADIR,       // ✅ On time
    TERLAMBAT,   // ⏰ Late
    ALFA         // ❌ Absent (not used in checkin, only in reports)
}
```

**2. TipeUser**:
```java
public enum TipeUser {
    SISWA,       // Student
    GURU         // Teacher
}
```

**3. MethodPresensi**:
```java
public enum MethodPresensi {
    MANUAL,      // Smartphone app (Tahap 4)
    RFID,        // RFID card (Tahap 5)
    BARCODE,     // Barcode scanner (Tahap 6)
    FACE         // Face recognition (Tahap 7)
}
```

---

## 🔐 CHECKIN FLOW: STEP-BY-STEP

Mari kita lihat **flow lengkap** checkin manual:

### 1. User Request (dari Mobile App)

```http
POST /api/presensi/checkin
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "latitude": -6.200000,
  "longitude": 106.816666
}
```

**Penjelasan**:
- **JWT Token**: Auto-identify user (dari token)
- **Latitude/Longitude**: GPS coordinates dari smartphone

---

### 2. Backend Processing (PresensiController)

```java
@PostMapping("/checkin")
public ResponseEntity<PresensiResponse> checkin(
    @RequestBody PresensiRequest request,
    @AuthenticationPrincipal UserDetails userDetails
) {
    // 1. Get user from JWT token
    String username = userDetails.getUsername();
    
    // 2. Call service
    PresensiResponse response = presensiService.checkin(
        username, 
        request.getLatitude(), 
        request.getLongitude()
    );
    
    // 3. Return response
    return ResponseEntity.ok(response);
}
```

---

### 3. Service Logic (PresensiService)

```java
public PresensiResponse checkin(String username, Double lat, Double lon) {
    // Step 1: Find user by username
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    
    // Step 2: Detect tipe user (SISWA atau GURU)
    TipeUser tipe;
    if (siswaRepository.existsByUser(user)) {
        tipe = TipeUser.SISWA;
    } else if (guruRepository.existsByUser(user)) {
        tipe = TipeUser.GURU;
    } else {
        throw new RuntimeException("User bukan siswa atau guru");
    }
    
    // Step 3: Validate duplicate (no checkin 2x per hari)
    LocalDate today = LocalDate.now();
    if (presensiRepository.existsByUserAndTanggal(user, today)) {
        throw new RuntimeException("Sudah checkin hari ini");
    }
    
    // Step 4: Get current time
    LocalTime now = LocalTime.now();
    
    // Step 5: Calculate status (HADIR or TERLAMBAT)
    StatusPresensi status = hitungStatus(now);
    
    // Step 6: Create Presensi object
    Presensi presensi = new Presensi();
    presensi.setUser(user);
    presensi.setTipe(tipe);
    presensi.setTanggal(today);
    presensi.setJamMasuk(now);
    presensi.setStatus(status);
    presensi.setLatitude(lat);
    presensi.setLongitude(lon);
    presensi.setMethod(MethodPresensi.MANUAL);
    presensi.setKeterangan("Checkin dari smartphone: " + username);
    
    // Step 7: Save to database
    Presensi saved = presensiRepository.save(presensi);
    
    // Step 8: Return response
    return new PresensiResponse(
        saved.getId(),
        saved.getUser().getUsername(),
        saved.getTipe(),
        saved.getTanggal(),
        saved.getJamMasuk(),
        saved.getStatus(),
        saved.getMethod(),
        "Checkin berhasil!"
    );
}
```

**7 Step Logic**:
1. ✅ **Find user** (dari username di JWT)
2. ✅ **Detect tipe** (SISWA atau GURU)
3. ✅ **Validate duplicate** (no 2x checkin)
4. ✅ **Get time** (current time)
5. ✅ **Calculate status** (HADIR/TERLAMBAT)
6. ✅ **Create record** (populate data)
7. ✅ **Save to DB** (persist)

---

### 4. Database Record

Setelah save, data di database:

```sql
SELECT * FROM presensi WHERE id = 1;

+----+---------+-------+------------+----------+--------+-----------+------------+--------+--------------------------------------+
| id | user_id | tipe  | tanggal    | jam_masuk| status | latitude  | longitude  | method | keterangan                           |
+----+---------+-------+------------+----------+--------+-----------+------------+--------+--------------------------------------+
| 1  | 2       | SISWA | 2024-11-17 | 07:10:30 | HADIR  | -6.200000 | 106.816666 | MANUAL | Checkin dari smartphone: andi_siswa  |
+----+---------+-------+------------+----------+--------+-----------+------------+--------+--------------------------------------+
```

---

### 5. Response to Client

```json
{
  "id": 1,
  "username": "andi_siswa",
  "tipe": "SISWA",
  "tanggal": "2024-11-17",
  "jamMasuk": "07:10:30",
  "status": "HADIR",
  "method": "MANUAL",
  "message": "Checkin berhasil!"
}
```

Mobile app bisa show:
```
✅ Checkin Berhasil!
Status: HADIR
Jam: 07:10:30
```

---

## 🚫 VALIDASI: PREVENT ERRORS

### Validasi 1: Duplicate Checkin

**Problem**: User coba checkin 2x dalam 1 hari.

```java
// Check if already checkin today
if (presensiRepository.existsByUserAndTanggal(user, today)) {
    throw new RuntimeException("Sudah checkin hari ini");
}
```

**Example**:
```
07:00 → Checkin pertama → ✅ Success
07:30 → Coba checkin lagi → ❌ Error: "Sudah checkin hari ini"
```

---

### Validasi 2: User Must Exist

**Problem**: JWT token invalid atau user sudah dihapus.

```java
User user = userRepository.findByUsername(username)
    .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
```

---

### Validasi 3: User Must Be Siswa or Guru

**Problem**: User role = ADMIN (bukan siswa/guru).

```java
if (!siswaRepository.existsByUser(user) && 
    !guruRepository.existsByUser(user)) {
    throw new RuntimeException("User bukan siswa atau guru");
}
```

**Reasoning**: Admin tidak perlu checkin (admin manage system, bukan attend sekolah).

---

### Validasi 4: Checkout Must After Checkin

**Problem**: User belum checkin tapi langsung checkout.

```java
// In checkout method:
if (presensi.getJamMasuk() == null) {
    throw new RuntimeException("Harus checkin dulu sebelum checkout");
}

if (presensi.getJamPulang() != null) {
    throw new RuntimeException("Sudah checkout hari ini");
}
```

---

## 🎓 PEMBELAJARAN DARI TAHAP 4

### 1. Business Logic itu Penting!

Code bukan cuma CRUD. Harus ada **logic**:
- Calculate status (HADIR/TERLAMBAT)
- Validate duplicate
- Detect tipe user
- GPS tracking

### 2. Enum untuk Type Safety

Jangan pakai `String` untuk nilai tetap. Gunakan `Enum`:
```java
// Bad
String status = "HADIR";

// Good
StatusPresensi status = StatusPresensi.HADIR;
```

### 3. Audit Trail

Selalu simpan **keterangan** untuk tracking:
```java
presensi.setKeterangan("Checkin dari smartphone: " + username);
```

Benefit:
- Debug: "Kenapa presensi ini statusnya HADIR?"
- Security: "Siapa yang checkin pukul 07:00?"
- Analytics: "Berapa % checkin via manual?"

### 4. DTO Pattern

Jangan return Entity langsung. Gunakan DTO:
```java
// Bad
@GetMapping
public List<Presensi> getAll() { ... } // Expose entity

// Good
@GetMapping
public List<PresensiResponse> getAll() { ... } // DTO
```

Benefit:
- **Security**: Hide sensitive fields
- **Flexibility**: Customize response structure
- **Versioning**: Change DTO without change entity

---

## 🔜 NEXT: TAHAP 5 (RFID)

Di Tahap 4, kita sudah implement **Manual Checkin** (via smartphone).

**Limitation**:
- ❌ User harus buka app (slow)
- ❌ User harus login (friction)
- ❌ User bisa lupa password

**Solution**: RFID Card! (Tahap 5)

**RFID** = Radio Frequency Identification
- User hanya **tap kartu** di reader
- **Instant** (< 1 detik)
- **No login** required
- **No app** required

Stay tuned untuk **Blog 5: RFID Integration**! 🚀

---

## 📚 REFERENSI

- **LocalDate/LocalTime**: Java 8 Date API
- **Enum in Java**: https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
- **GPS Coordinates**: Latitude/Longitude system
- **Business Logic Pattern**: Service layer in Spring Boot
- **DTO Pattern**: Data Transfer Object for API responses

---

**Penulis**: GitHub Copilot  
**Last Updated**: 17 November 2024  
**Next Blog**: Blog 5 - RFID Integration (Tahap 5)  
**Previous Blog**: Blog 3 - Authentication & Authorization (Tahap 3)
