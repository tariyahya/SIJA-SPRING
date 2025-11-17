# TASK 4 - IMPLEMENTASI PRESENSI MANUAL

## 🎯 TUJUAN TAHAP 4

Membuat sistem presensi dasar dengan flow **checkin → checkout**, tanpa hardware dulu (RFID/Barcode/Face nanti di Tahap 5-7). 

**Yang akan dipelajari:**
- Entity Presensi dengan berbagai status
- Business logic: validasi duplikasi checkin, hitung status (hadir/terlambat)
- History presensi
- Role-based access: Siswa checkin sendiri, Guru bisa checkin + lihat semua, Admin manage semua

---

## 📋 STEP-BY-STEP IMPLEMENTATION

### STEP 1: Buat Enum untuk Presensi

Kita butuh beberapa enum untuk tipe data yang fixed.

#### 1.1 TipeUser Enum

**File:** `backend/src/main/java/com/smk/presensi/enums/TipeUser.java`

```java
package com.smk.presensi.enums;

/**
 * ENUM TIPE USER - Jenis user yang bisa presensi.
 * 
 * Apa itu Enum?
 * - Tipe data KHUSUS untuk nilai yang FIX/TETAP (tidak berubah)
 * - Contoh: hari dalam seminggu (SENIN, SELASA, dst)
 * - Lebih aman daripada String (typo-proof, IDE bisa autocomplete)
 * 
 * Kenapa pakai Enum, bukan String?
 * - String: "SISWA", "siswa", "Siswa" → 3 value berbeda (error prone!)
 * - Enum: TipeUser.SISWA → hanya 1 value, tidak bisa salah
 * - Validasi otomatis oleh Java compiler
 * 
 * Analogi:
 * - Enum = Pilihan ganda A/B/C/D (pilihan terbatas)
 * - String = Essay (bisa tulis apa saja, risiko typo)
 * 
 * Use case:
 * - Saat insert presensi, kita harus tahu ini presensi SISWA atau GURU
 * - Dengan enum, tidak bisa salah ketik "SSIWA" atau "gURU"
 */
public enum TipeUser {
    /**
     * SISWA - Presensi untuk siswa/pelajar
     */
    SISWA,
    
    /**
     * GURU - Presensi untuk guru/pengajar
     */
    GURU
}
```

#### 1.2 StatusPresensi Enum

**File:** `backend/src/main/java/com/smk/presensi/enums/StatusPresensi.java`

```java
package com.smk.presensi.enums;

/**
 * ENUM STATUS PRESENSI - Status kehadiran.
 * 
 * Penjelasan setiap status:
 * 
 * 1. HADIR - Checkin tepat waktu (sebelum atau pas jam masuk)
 *    Contoh: Jam masuk 07:00, siswa tap kartu jam 06:55 → HADIR
 * 
 * 2. TERLAMBAT - Checkin setelah jam masuk + toleransi
 *    Contoh: Jam masuk 07:00, toleransi 15 menit, tap jam 07:20 → TERLAMBAT
 * 
 * 3. IZIN - Tidak hadir tapi ada surat izin (approved)
 *    Contoh: Sakit, ada surat dokter → status IZIN
 * 
 * 4. SAKIT - Tidak hadir karena sakit (dengan surat)
 *    Contoh: Demam, ada surat dari ortu/dokter → status SAKIT
 * 
 * 5. ALPHA - Tidak hadir tanpa keterangan (Absent)
 *    Contoh: Tidak tap kartu, tidak ada surat → status ALPHA
 * 
 * Alur status:
 * - Jika checkin → sistem otomatis set HADIR atau TERLAMBAT (based on jam)
 * - Jika tidak checkin + ada surat → admin set manual IZIN atau SAKIT
 * - Jika tidak checkin + tidak ada surat → sistem auto set ALPHA (end of day)
 */
public enum StatusPresensi {
    HADIR,
    TERLAMBAT,
    IZIN,
    SAKIT,
    ALPHA
}
```

#### 1.3 MethodPresensi Enum

**File:** `backend/src/main/java/com/smk/presensi/enums/MethodPresensi.java`

```java
package com.smk.presensi.enums;

/**
 * ENUM METHOD PRESENSI - Cara checkin dilakukan.
 * 
 * Penjelasan setiap method:
 * 
 * 1. MANUAL - Checkin lewat form (admin input manual)
 *    Use case: Siswa lupa bawa kartu, admin input manual
 * 
 * 2. RFID - Tap kartu RFID
 *    Use case: Siswa tap kartu RFID di reader
 * 
 * 3. BARCODE - Scan barcode/QR code
 *    Use case: Scan barcode di ID card atau QR di HP
 * 
 * 4. FACE - Face recognition
 *    Use case: Kamera scan wajah, sistem cocokkan dengan database
 * 
 * Tahap 4 ini kita fokus MANUAL dulu.
 * Tahap 5-7 baru tambah RFID, BARCODE, FACE.
 */
public enum MethodPresensi {
    MANUAL,
    RFID,
    BARCODE,
    FACE
}
```

---

### STEP 2: Buat Entity Presensi

Entity ini adalah **core** dari sistem presensi.

**File:** `backend/src/main/java/com/smk/presensi/entity/Presensi.java`

```java
package com.smk.presensi.entity;

import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.enums.TipeUser;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ENTITY PRESENSI - Representasi tabel presensi di database.
 * 
 * Apa itu Presensi?
 * - Record kehadiran seseorang (siswa/guru) di satu hari
 * - Minimal ada: siapa, kapan, jam berapa, statusnya apa
 * 
 * Alur presensi lengkap:
 * 1. User checkin (pagi) → insert record dengan jamMasuk
 * 2. Sistem hitung: tepat waktu? → set status HADIR atau TERLAMBAT
 * 3. User checkout (sore) → update record, isi jamPulang
 * 4. End of day: yang tidak checkin → sistem buat record status ALPHA
 * 
 * Analogi:
 * - Entity Presensi = Buku absensi kelas
 * - Tiap baris = 1 record presensi (1 siswa, 1 hari)
 * - Kolom: nama, tanggal, jam masuk, jam pulang, status
 * 
 * Field-field penting:
 * - userId: Siapa yang presensi (FK ke tabel User)
 * - tipe: SISWA atau GURU
 * - tanggal: Hari apa (LocalDate: 2024-01-15)
 * - jamMasuk: Jam checkin (LocalTime: 07:05:30)
 * - jamPulang: Jam checkout (LocalTime: 15:30:00)
 * - status: HADIR/TERLAMBAT/IZIN/SAKIT/ALPHA
 * - method: MANUAL/RFID/BARCODE/FACE
 * - latitude/longitude: GPS location (opsional, untuk validasi geolocation)
 * - keterangan: Catatan tambahan (opsional)
 */
@Entity
@Table(name = "presensi")
public class Presensi {

    /**
     * PRIMARY KEY - ID unik untuk setiap record presensi.
     * 
     * @GeneratedValue(IDENTITY):
     * - Database auto-generate nilai (auto-increment)
     * - Tiap insert, database kasih ID baru: 1, 2, 3, dst
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * USER ID - Foreign key ke tabel User.
     * 
     * Relasi: ManyToOne (banyak presensi → 1 user)
     * - 1 user bisa punya banyak record presensi (1 record per hari)
     * - Contoh: User "Budi" punya presensi 1 Jan, 2 Jan, 3 Jan, dst
     * 
     * @ManyToOne: Definisi relasi many-to-one
     * @JoinColumn: Nama kolom FK di tabel presensi
     *   - name = "user_id": Kolom FK yang nyimpan ID user
     *   - nullable = false: Wajib diisi (presensi harus terkait user)
     * 
     * Query impact:
     * - Bisa join: SELECT * FROM presensi p JOIN users u ON p.user_id = u.id
     * - Bisa filter: WHERE p.user_id = 123
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * TIPE USER - Apakah ini presensi SISWA atau GURU.
     * 
     * Kenapa perlu field ini padahal User sudah punya Role?
     * - User bisa punya multiple roles (ADMIN + GURU)
     * - Presensi perlu tahu: yang checkin ini sebagai SISWA atau GURU?
     * - Contoh: Pak Budi (role ADMIN + GURU) checkin sebagai GURU
     * 
     * @Enumerated(EnumType.STRING):
     * - Simpan value sebagai STRING di database ("SISWA" atau "GURU")
     * - Alternatif: EnumType.ORDINAL (simpan index 0, 1) → tidak recommended
     * - STRING lebih readable di database, dan aman kalau urutan enum berubah
     * 
     * @Column:
     * - nullable = false: Wajib diisi
     * - length = 10: Max 10 karakter (cukup untuk "SISWA" atau "GURU")
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipeUser tipe;

    /**
     * TANGGAL - Tanggal presensi (hari apa).
     * 
     * LocalDate: Tipe data Java 8+ untuk tanggal (tanpa jam)
     * - Format: YYYY-MM-DD (2024-01-15)
     * - Bisa compare: tanggal1.isBefore(tanggal2)
     * - Bisa manipulate: tanggal.plusDays(1)
     * 
     * Use case:
     * - Filter presensi per hari: WHERE tanggal = '2024-01-15'
     * - Filter range: WHERE tanggal BETWEEN '2024-01-01' AND '2024-01-31'
     * - Cek duplikasi: SELECT * WHERE user_id=1 AND tanggal='2024-01-15'
     * 
     * Validasi:
     * - 1 user hanya boleh punya 1 presensi per hari
     * - Tidak boleh checkin 2x di hari yang sama (kecuali checkout)
     */
    @Column(nullable = false)
    private LocalDate tanggal;

    /**
     * JAM MASUK - Waktu checkin (pukul berapa).
     * 
     * LocalTime: Tipe data Java 8+ untuk waktu (tanpa tanggal)
     * - Format: HH:mm:ss (07:05:30)
     * - Bisa compare: jam1.isBefore(jam2)
     * - Bisa hitung durasi: Duration.between(jamMasuk, jamPulang)
     * 
     * Use case:
     * - Hitung status: jika jamMasuk > 07:15 → TERLAMBAT
     * - Hitung lama kerja: jamPulang - jamMasuk
     * - Validasi: jamMasuk harus sebelum jamPulang
     * 
     * Nullable = true (opsional):
     * - Karena ada kasus IZIN/SAKIT/ALPHA (tidak checkin)
     * - Record dibuat manual oleh admin tanpa jamMasuk
     */
    @Column(name = "jam_masuk")
    private LocalTime jamMasuk;

    /**
     * JAM PULANG - Waktu checkout (pukul berapa).
     * 
     * Nullable = true (opsional):
     * - User bisa checkin tapi belum checkout
     * - Checkout dilakukan terpisah (nanti update record ini)
     * 
     * Alur:
     * 1. Checkin → insert record dengan jamMasuk, jamPulang = null
     * 2. Checkout → update record, set jamPulang
     * 
     * Validasi:
     * - jamPulang harus setelah jamMasuk
     * - Jika jamPulang masih null → user belum checkout
     */
    @Column(name = "jam_pulang")
    private LocalTime jamPulang;

    /**
     * STATUS PRESENSI - Hasil kehadiran (HADIR/TERLAMBAT/dll).
     * 
     * Status ditentukan oleh:
     * 1. Checkin tepat waktu → HADIR
     * 2. Checkin terlambat → TERLAMBAT
     * 3. Tidak checkin + ada surat → IZIN atau SAKIT (admin set manual)
     * 4. Tidak checkin + tidak ada surat → ALPHA (sistem auto set)
     * 
     * Business logic untuk auto-set status:
     * ```java
     * if (jamMasuk.isBefore(JAM_MASUK_NORMAL + TOLERANSI)) {
     *     status = HADIR;
     * } else {
     *     status = TERLAMBAT;
     * }
     * ```
     * 
     * @Enumerated(STRING): Simpan "HADIR", "TERLAMBAT", dst sebagai string
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusPresensi status;

    /**
     * METHOD PRESENSI - Cara checkin dilakukan.
     * 
     * Tahap 4 (sekarang): Semua MANUAL
     * Tahap 5: Tambah RFID
     * Tahap 6: Tambah BARCODE
     * Tahap 7: Tambah FACE
     * 
     * Use case:
     * - Statistik: berapa % presensi pakai RFID vs FACE?
     * - Audit: user ini checkin pakai apa?
     * - Troubleshoot: jika ada masalah, cek method-nya
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MethodPresensi method;

    /**
     * LATITUDE - Koordinat GPS (lokasi utara-selatan).
     * 
     * Use case: Validasi lokasi checkin
     * - Siswa harus checkin di area sekolah (radius tertentu)
     * - Jika GPS jauh dari sekolah → reject atau flag warning
     * 
     * Nullable = true (opsional):
     * - Tidak semua device punya GPS
     * - Desktop app tidak punya GPS
     * - Mobile app wajib kirim GPS
     * 
     * Contoh value: -6.200000 (Jakarta)
     */
    @Column
    private Double latitude;

    /**
     * LONGITUDE - Koordinat GPS (lokasi barat-timur).
     * 
     * Contoh value: 106.816666 (Jakarta)
     * 
     * Validasi lokasi:
     * ```java
     * double distance = calculateDistance(
     *     latSekolah, lonSekolah,
     *     latCheckin, lonCheckin
     * );
     * if (distance > MAX_RADIUS_METER) {
     *     throw new Exception("Lokasi terlalu jauh dari sekolah");
     * }
     * ```
     */
    @Column
    private Double longitude;

    /**
     * KETERANGAN - Catatan tambahan (opsional).
     * 
     * Use case:
     * - IZIN: "Sakit demam"
     * - SAKIT: "Rawat inap RS"
     * - TERLAMBAT: "Ban bocor di jalan"
     * - ALPHA: (kosong atau "Tidak ada kabar")
     * 
     * Nullable = true: Tidak wajib diisi
     */
    @Column(length = 500)
    private String keterangan;

    /**
     * CONSTRUCTOR KOSONG - Wajib untuk JPA.
     * 
     * JPA butuh constructor tanpa parameter untuk:
     * - Instantiate object saat fetch dari database
     * - Reflection & proxying
     * 
     * Jangan dihapus!
     */
    public Presensi() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TipeUser getTipe() {
        return tipe;
    }

    public void setTipe(TipeUser tipe) {
        this.tipe = tipe;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public LocalTime getJamMasuk() {
        return jamMasuk;
    }

    public void setJamMasuk(LocalTime jamMasuk) {
        this.jamMasuk = jamMasuk;
    }

    public LocalTime getJamPulang() {
        return jamPulang;
    }

    public void setJamPulang(LocalTime jamPulang) {
        this.jamPulang = jamPulang;
    }

    public StatusPresensi getStatus() {
        return status;
    }

    public void setStatus(StatusPresensi status) {
        this.status = status;
    }

    public MethodPresensi getMethod() {
        return method;
    }

    public void setMethod(MethodPresensi method) {
        this.method = method;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
}
```

---

### STEP 3: Buat Repository

**File:** `backend/src/main/java/com/smk/presensi/repository/PresensiRepository.java`

```java
package com.smk.presensi.repository;

import com.smk.presensi.entity.Presensi;
import com.smk.presensi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY PRESENSI - Interface untuk akses data presensi.
 * 
 * Spring Data JPA auto-implement method berdasarkan nama method.
 * - findBy<FieldName>: SELECT * WHERE field = value
 * - existsBy<FieldName>: SELECT COUNT(*) WHERE field = value > 0
 * - findBy<Field1>And<Field2>: SELECT * WHERE field1 = val1 AND field2 = val2
 */
@Repository
public interface PresensiRepository extends JpaRepository<Presensi, Long> {

    /**
     * Cari semua presensi milik 1 user.
     * 
     * Generated query:
     * SELECT * FROM presensi WHERE user_id = ?
     * 
     * Use case:
     * - Tampilkan history presensi siswa A
     * - Rekap presensi guru B bulan ini
     * 
     * @param user User yang dicari presensinya
     * @return List presensi user tersebut (bisa kosong jika belum pernah presensi)
     */
    List<Presensi> findByUser(User user);

    /**
     * Cari presensi user dalam range tanggal.
     * 
     * Generated query:
     * SELECT * FROM presensi
     * WHERE user_id = ?
     * AND tanggal BETWEEN ? AND ?
     * ORDER BY tanggal DESC
     * 
     * Use case:
     * - Rekap presensi Januari 2024
     * - Lihat presensi minggu ini
     * 
     * @param user User yang dicari
     * @param startDate Tanggal mulai (inclusive)
     * @param endDate Tanggal akhir (inclusive)
     * @return List presensi dalam range, sorted by tanggal descending
     */
    List<Presensi> findByUserAndTanggalBetweenOrderByTanggalDesc(
        User user,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Cari presensi user di tanggal tertentu.
     * 
     * Generated query:
     * SELECT * FROM presensi
     * WHERE user_id = ? AND tanggal = ?
     * 
     * Use case:
     * - Cek apakah user sudah presensi hari ini
     * - Validasi duplikasi checkin
     * 
     * @param user User yang dicari
     * @param tanggal Tanggal yang dicari
     * @return Optional<Presensi> (ada jika sudah presensi, empty jika belum)
     */
    Optional<Presensi> findByUserAndTanggal(User user, LocalDate tanggal);

    /**
     * Cek apakah user sudah presensi di tanggal tertentu.
     * 
     * Generated query:
     * SELECT COUNT(*) > 0 FROM presensi
     * WHERE user_id = ? AND tanggal = ?
     * 
     * Use case:
     * - Sebelum checkin, cek dulu: sudah checkin atau belum?
     * - Jika sudah → reject dengan error "Sudah presensi hari ini"
     * 
     * @param user User yang dicek
     * @param tanggal Tanggal yang dicek
     * @return true jika sudah ada presensi, false jika belum
     */
    boolean existsByUserAndTanggal(User user, LocalDate tanggal);

    /**
     * Cari semua presensi di tanggal tertentu.
     * 
     * Generated query:
     * SELECT * FROM presensi WHERE tanggal = ?
     * 
     * Use case:
     * - Lihat semua yang presensi hari ini
     * - Rekap harian
     * 
     * @param tanggal Tanggal yang dicari
     * @return List presensi di tanggal tersebut
     */
    List<Presensi> findByTanggal(LocalDate tanggal);
}
```

---

### STEP 4: Buat DTO (Data Transfer Object)

DTO digunakan untuk transfer data antara client dan server, terpisah dari Entity.

#### 4.1 CheckinRequest DTO

**File:** `backend/src/main/java/com/smk/presensi/dto/presensi/CheckinRequest.java`

```java
package com.smk.presensi.dto.presensi;

import com.smk.presensi.enums.TipeUser;
import jakarta.validation.constraints.NotNull;

/**
 * DTO untuk request checkin.
 * 
 * Kenapa pakai DTO, tidak langsung Entity?
 * - Entity = struktur database (punya semua field)
 * - DTO = data yang dikirim client (hanya field yang diperlukan)
 * - Client tidak perlu tahu struktur lengkap database
 * 
 * Field yang dikirim client saat checkin:
 * - tipe: SISWA atau GURU (wajib)
 * - latitude, longitude: GPS location (opsional)
 * - keterangan: Catatan tambahan (opsional)
 * 
 * Field yang TIDAK dikirim (auto-generate di server):
 * - id: Auto-increment
 * - user: Ambil dari SecurityContext (yang login)
 * - tanggal: LocalDate.now()
 * - jamMasuk: LocalTime.now()
 * - status: Auto-calculate (HADIR/TERLAMBAT)
 * - method: Set MANUAL
 */
public record CheckinRequest(
    /**
     * Tipe user yang checkin (SISWA/GURU).
     * @NotNull: Wajib diisi, tidak boleh null
     */
    @NotNull(message = "Tipe user harus diisi")
    TipeUser tipe,
    
    /**
     * GPS latitude (opsional).
     * Contoh: -6.200000
     */
    Double latitude,
    
    /**
     * GPS longitude (opsional).
     * Contoh: 106.816666
     */
    Double longitude,
    
    /**
     * Keterangan tambahan (opsional).
     * Contoh: "Datang dari rumah sakit"
     */
    String keterangan
) {}
```

#### 4.2 CheckoutRequest DTO

**File:** `backend/src/main/java/com/smk/presensi/dto/presensi/CheckoutRequest.java`

```java
package com.smk.presensi.dto.presensi;

/**
 * DTO untuk request checkout.
 * 
 * Field minimal karena hanya update jamPulang.
 * - latitude, longitude: GPS location saat checkout (opsional)
 * - keterangan: Catatan tambahan saat checkout (opsional)
 */
public record CheckoutRequest(
    /**
     * GPS latitude saat checkout (opsional).
     */
    Double latitude,
    
    /**
     * GPS longitude saat checkout (opsional).
     */
    Double longitude,
    
    /**
     * Keterangan tambahan saat checkout (opsional).
     * Contoh: "Pulang ke rumah"
     */
    String keterangan
) {}
```

#### 4.3 PresensiResponse DTO

**File:** `backend/src/main/java/com/smk/presensi/dto/presensi/PresensiResponse.java`

```java
package com.smk.presensi.dto.presensi;

import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.enums.TipeUser;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO untuk response presensi.
 * 
 * Berisi semua informasi yang perlu ditampilkan ke client.
 * Struktur lebih flat (tidak nested) untuk kemudahan parsing.
 */
public record PresensiResponse(
    Long id,
    Long userId,
    String username,
    TipeUser tipe,
    LocalDate tanggal,
    LocalTime jamMasuk,
    LocalTime jamPulang,
    StatusPresensi status,
    MethodPresensi method,
    Double latitude,
    Double longitude,
    String keterangan
) {}
```

---

### STEP 5: Buat Service Layer

Service berisi business logic untuk presensi.

**File:** `backend/src/main/java/com/smk/presensi/service/PresensiService.java`

```java
package com.smk.presensi.service;

import com.smk.presensi.dto.presensi.CheckinRequest;
import com.smk.presensi.dto.presensi.CheckoutRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.entity.Presensi;
import com.smk.presensi.entity.User;
import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.repository.PresensiRepository;
import com.smk.presensi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE PRESENSI - Business logic untuk presensi.
 * 
 * Tanggung jawab:
 * 1. Validasi business rules
 * 2. Perhitungan status (HADIR/TERLAMBAT)
 * 3. Orchestrate repository calls
 * 4. Convert Entity ↔ DTO
 */
@Service
public class PresensiService {

    private final PresensiRepository presensiRepository;
    private final UserRepository userRepository;

    /**
     * JAM MASUK NORMAL - Diambil dari application.properties.
     * Contoh: 07:00:00
     */
    @Value("${presensi.jam-masuk}")
    private LocalTime jamMasukNormal;

    /**
     * TOLERANSI KETERLAMBATAN (dalam menit).
     * Contoh: 15 menit → checkin sampai 07:15 masih HADIR
     */
    @Value("${presensi.toleransi-menit}")
    private int toleransiMenit;

    public PresensiService(PresensiRepository presensiRepository, UserRepository userRepository) {
        this.presensiRepository = presensiRepository;
        this.userRepository = userRepository;
    }

    /**
     * CHECKIN - User melakukan presensi masuk.
     * 
     * Alur:
     * 1. Ambil user yang sedang login (dari SecurityContext)
     * 2. Validasi: sudah checkin hari ini atau belum?
     * 3. Jika sudah → throw error
     * 4. Jika belum → insert record baru:
     *    - Set tanggal = hari ini
     *    - Set jamMasuk = sekarang
     *    - Hitung status (HADIR/TERLAMBAT)
     *    - Set method = MANUAL
     * 5. Save ke database
     * 6. Return response DTO
     */
    @Transactional
    public PresensiResponse checkin(CheckinRequest request) {
        // 1. Ambil user yang login
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // 2. Validasi duplikasi
        LocalDate today = LocalDate.now();
        if (presensiRepository.existsByUserAndTanggal(user, today)) {
            throw new RuntimeException("Anda sudah checkin hari ini");
        }

        // 3. Buat record presensi baru
        Presensi presensi = new Presensi();
        presensi.setUser(user);
        presensi.setTipe(request.tipe());
        presensi.setTanggal(today);
        
        LocalTime now = LocalTime.now();
        presensi.setJamMasuk(now);
        
        // 4. Hitung status (HADIR/TERLAMBAT)
        presensi.setStatus(hitungStatus(now));
        
        presensi.setMethod(MethodPresensi.MANUAL);
        presensi.setLatitude(request.latitude());
        presensi.setLongitude(request.longitude());
        presensi.setKeterangan(request.keterangan());

        // 5. Save
        Presensi saved = presensiRepository.save(presensi);

        // 6. Convert ke DTO
        return toResponse(saved);
    }

    /**
     * CHECKOUT - User melakukan presensi pulang.
     * 
     * Alur:
     * 1. Ambil user yang sedang login
     * 2. Cari presensi hari ini
     * 3. Validasi: sudah checkin atau belum?
     * 4. Validasi: sudah checkout atau belum?
     * 5. Update jamPulang = sekarang
     * 6. Append keterangan checkout (jika ada)
     * 7. Save update
     * 8. Return response DTO
     */
    @Transactional
    public PresensiResponse checkout(CheckoutRequest request) {
        // 1. Ambil user yang login
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // 2. Cari presensi hari ini
        LocalDate today = LocalDate.now();
        Presensi presensi = presensiRepository.findByUserAndTanggal(user, today)
                .orElseThrow(() -> new RuntimeException("Anda belum checkin hari ini"));

        // 3. Validasi: sudah checkout atau belum?
        if (presensi.getJamPulang() != null) {
            throw new RuntimeException("Anda sudah checkout hari ini");
        }

        // 4. Update jamPulang
        presensi.setJamPulang(LocalTime.now());
        
        // Update GPS (jika ada)
        if (request.latitude() != null) {
            presensi.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            presensi.setLongitude(request.longitude());
        }
        
        // Append keterangan checkout
        if (request.keterangan() != null && !request.keterangan().isBlank()) {
            String keteranganBaru = presensi.getKeterangan() == null
                    ? request.keterangan()
                    : presensi.getKeterangan() + " | Pulang: " + request.keterangan();
            presensi.setKeterangan(keteranganBaru);
        }

        // 5. Save update
        Presensi updated = presensiRepository.save(presensi);

        // 6. Convert ke DTO
        return toResponse(updated);
    }

    /**
     * GET HISTORI - Ambil history presensi user sendiri.
     * 
     * @param startDate Tanggal mulai (opsional, default: 30 hari lalu)
     * @param endDate Tanggal akhir (opsional, default: hari ini)
     * @return List presensi dalam range tanggal
     */
    public List<PresensiResponse> getHistori(LocalDate startDate, LocalDate endDate) {
        // Ambil user yang login
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Default: 30 hari terakhir
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Ambil data
        List<Presensi> list = presensiRepository.findByUserAndTanggalBetweenOrderByTanggalDesc(
                user, startDate, endDate
        );

        // Convert ke DTO
        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET ALL PRESENSI - Ambil semua presensi (admin/guru only).
     * 
     * @param tanggal Tanggal yang dicari (opsional, default: hari ini)
     * @return List semua presensi di tanggal tersebut
     */
    public List<PresensiResponse> getAllPresensi(LocalDate tanggal) {
        if (tanggal == null) {
            tanggal = LocalDate.now();
        }

        List<Presensi> list = presensiRepository.findByTanggal(tanggal);

        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * HITUNG STATUS - Tentukan HADIR atau TERLAMBAT.
     * 
     * Logic:
     * - Jika jamMasuk <= (jamMasukNormal + toleransi) → HADIR
     * - Jika jamMasuk > (jamMasukNormal + toleransi) → TERLAMBAT
     * 
     * Contoh:
     * - jamMasukNormal = 07:00
     * - toleransiMenit = 15
     * - Batas HADIR = 07:15
     * 
     * - Checkin 06:55 → HADIR
     * - Checkin 07:00 → HADIR
     * - Checkin 07:10 → HADIR
     * - Checkin 07:15 → HADIR
     * - Checkin 07:16 → TERLAMBAT
     * - Checkin 08:00 → TERLAMBAT
     */
    private StatusPresensi hitungStatus(LocalTime jamMasuk) {
        LocalTime batasHadir = jamMasukNormal.plusMinutes(toleransiMenit);
        
        if (jamMasuk.isBefore(batasHadir) || jamMasuk.equals(batasHadir)) {
            return StatusPresensi.HADIR;
        } else {
            return StatusPresensi.TERLAMBAT;
        }
    }

    /**
     * CONVERT ENTITY TO DTO.
     * 
     * Mapping field by field dari Entity ke Response DTO.
     */
    private PresensiResponse toResponse(Presensi presensi) {
        return new PresensiResponse(
                presensi.getId(),
                presensi.getUser().getId(),
                presensi.getUser().getUsername(),
                presensi.getTipe(),
                presensi.getTanggal(),
                presensi.getJamMasuk(),
                presensi.getJamPulang(),
                presensi.getStatus(),
                presensi.getMethod(),
                presensi.getLatitude(),
                presensi.getLongitude(),
                presensi.getKeterangan()
        );
    }
}
```

---

### STEP 6: Buat Controller

**File:** `backend/src/main/java/com/smk/presensi/controller/PresensiController.java`

```java
package com.smk.presensi.controller;

import com.smk.presensi.dto.presensi.CheckinRequest;
import com.smk.presensi.dto.presensi.CheckoutRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.service.PresensiService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * CONTROLLER PRESENSI - REST API endpoints untuk presensi.
 * 
 * Base URL: /api/presensi
 * 
 * Endpoints:
 * 1. POST /checkin - Checkin (SISWA/GURU)
 * 2. POST /checkout - Checkout (SISWA/GURU)
 * 3. GET /histori - Lihat histori sendiri (SISWA/GURU)
 * 4. GET / - Lihat semua presensi (ADMIN/GURU)
 */
@RestController
@RequestMapping("/api/presensi")
public class PresensiController {

    private final PresensiService presensiService;

    public PresensiController(PresensiService presensiService) {
        this.presensiService = presensiService;
    }

    /**
     * ENDPOINT: POST /api/presensi/checkin
     * 
     * Checkin presensi masuk.
     * 
     * Access: SISWA, GURU
     * 
     * Request body:
     * {
     *   "tipe": "SISWA",
     *   "latitude": -6.200000,
     *   "longitude": 106.816666,
     *   "keterangan": "Datang tepat waktu"
     * }
     * 
     * Response: PresensiResponse
     */
    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('SISWA', 'GURU')")
    public ResponseEntity<PresensiResponse> checkin(@Valid @RequestBody CheckinRequest request) {
        PresensiResponse response = presensiService.checkin(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ENDPOINT: POST /api/presensi/checkout
     * 
     * Checkout presensi pulang.
     * 
     * Access: SISWA, GURU
     * 
     * Request body:
     * {
     *   "latitude": -6.200000,
     *   "longitude": 106.816666,
     *   "keterangan": "Pulang ke rumah"
     * }
     * 
     * Response: PresensiResponse
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('SISWA', 'GURU')")
    public ResponseEntity<PresensiResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        PresensiResponse response = presensiService.checkout(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ENDPOINT: GET /api/presensi/histori
     * 
     * Lihat histori presensi sendiri.
     * 
     * Access: SISWA, GURU
     * 
     * Query params (opsional):
     * - startDate: Tanggal mulai (format: yyyy-MM-dd)
     * - endDate: Tanggal akhir (format: yyyy-MM-dd)
     * 
     * Contoh:
     * GET /api/presensi/histori?startDate=2024-01-01&endDate=2024-01-31
     * 
     * Response: List<PresensiResponse>
     */
    @GetMapping("/histori")
    @PreAuthorize("hasAnyRole('SISWA', 'GURU')")
    public ResponseEntity<List<PresensiResponse>> getHistori(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<PresensiResponse> list = presensiService.getHistori(startDate, endDate);
        return ResponseEntity.ok(list);
    }

    /**
     * ENDPOINT: GET /api/presensi
     * 
     * Lihat semua presensi (admin/guru only).
     * 
     * Access: ADMIN, GURU
     * 
     * Query params (opsional):
     * - tanggal: Tanggal yang dicari (format: yyyy-MM-dd, default: hari ini)
     * 
     * Contoh:
     * GET /api/presensi?tanggal=2024-01-15
     * 
     * Response: List<PresensiResponse>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public ResponseEntity<List<PresensiResponse>> getAllPresensi(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal
    ) {
        List<PresensiResponse> list = presensiService.getAllPresensi(tanggal);
        return ResponseEntity.ok(list);
    }
}
```

---

### STEP 7: Update Configuration

**File:** `backend/src/main/resources/application.properties`

Tambahkan konfigurasi jam kerja:

```properties
# ... (existing configuration)

# ==================== PRESENSI CONFIGURATION ====================
# Jam masuk normal (format: HH:mm:ss)
presensi.jam-masuk=07:00:00

# Toleransi keterlambatan (dalam menit)
# Contoh: 15 menit → checkin sampai 07:15 masih HADIR
presensi.toleransi-menit=15

# Jam pulang (reference only, tidak di-validate)
presensi.jam-pulang=15:00:00
```

---

### STEP 8: Compile & Test

#### 8.1 Compile

```bash
cd backend
mvn clean compile
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Compiling 41 source files
```

#### 8.2 Run Application

```bash
mvn spring-boot:run
```

Expected output:
```
Tomcat started on port 8081
Started PresensiApplication in 7.634 seconds
DataSeeder: Data seeding completed!
```

#### 8.3 Test dengan Postman

Ikuti panduan lengkap di **POSTMAN-TAHAP-04.md** untuk test semua skenario:

1. ✅ Checkin normal (HADIR)
2. ✅ Checkin terlambat (TERLAMBAT)
3. ✅ Checkin duplikasi (error)
4. ✅ Checkout tanpa checkin (error)
5. ✅ Checkout normal
6. ✅ Checkout duplikasi (error)
7. ✅ Lihat histori
8. ✅ SISWA akses GET /presensi (403 forbidden)
9. ✅ ADMIN akses GET /presensi (200 OK)
10. ✅ Test dengan GPS coordinates
11. ✅ Complete flow test

---

## 🎓 PEMBELAJARAN PENTING

### 1. Entity vs DTO

**Entity** (Presensi.java):
- Representasi tabel database
- Punya semua field (termasuk yang auto-generate)
- Punya relasi (@ManyToOne, @OneToMany)
- Tidak boleh diexpose langsung ke client

**DTO** (CheckinRequest, PresensiResponse):
- Data transfer object untuk API
- Hanya field yang diperlukan
- Flat structure (no nested objects)
- Aman diexpose ke client

### 2. Business Logic di Service

**Validasi:**
- ✅ Cek duplikasi checkin
- ✅ Cek sudah checkout atau belum
- ✅ Validasi user sudah login

**Perhitungan:**
- ✅ Status HADIR/TERLAMBAT (based on jam masuk)
- ✅ Auto-set tanggal & jam dari server (bukan dari client)

**Orchestration:**
- ✅ Ambil user dari SecurityContext
- ✅ Call repository untuk save/update
- ✅ Convert Entity ↔ DTO

### 3. Role-Based Access Control

**@PreAuthorize:**
- `"hasAnyRole('SISWA', 'GURU')"` → SISWA & GURU bisa akses
- `"hasAnyRole('ADMIN', 'GURU')"` → hanya ADMIN & GURU
- `"hasRole('ADMIN')"` → hanya ADMIN

**Access matrix:**
| Endpoint | SISWA | GURU | ADMIN |
|----------|-------|------|-------|
| POST /checkin | ✅ | ✅ | ❌ |
| POST /checkout | ✅ | ✅ | ❌ |
| GET /histori | ✅ (own) | ✅ (own) | ❌ |
| GET /presensi | ❌ | ✅ (all) | ✅ (all) |

### 4. LocalDate vs LocalTime

**LocalDate** (tanggal):
- Format: `2024-01-15`
- Untuk: tanggal presensi
- Method: `.now()`, `.plusDays()`, `.isBefore()`

**LocalTime** (jam):
- Format: `07:05:30`
- Untuk: jam masuk/pulang
- Method: `.now()`, `.plusMinutes()`, `.isBefore()`

### 5. Optional & Exception Handling

**Optional:**
```java
Optional<Presensi> opt = repository.findByUserAndTanggal(user, tanggal);
if (opt.isPresent()) {
    Presensi p = opt.get();
} else {
    throw new RuntimeException("Tidak ditemukan");
}

// Atau shortcut:
Presensi p = opt.orElseThrow(() -> new RuntimeException("Tidak ditemukan"));
```

**Custom Exception (nanti):**
- ResourceNotFoundException
- DuplicateCheckinException
- ValidationException

---

## 📊 STATISTIK TAHAP 4

**Files created:**
- 3 Enums (TipeUser, StatusPresensi, MethodPresensi)
- 1 Entity (Presensi)
- 1 Repository (PresensiRepository)
- 3 DTOs (CheckinRequest, CheckoutRequest, PresensiResponse)
- 1 Service (PresensiService)
- 1 Controller (PresensiController)
- **Total: 11 files**

**Lines of code:**
- Enums: ~20 lines
- Entity: ~150 lines
- Repository: ~25 lines
- DTOs: ~40 lines
- Service: ~160 lines
- Controller: ~80 lines
- **Total: ~475 lines** (excluding comments)

**Features implemented:**
- ✅ Checkin with status auto-calculation
- ✅ Checkout with validation
- ✅ History view (own records)
- ✅ Admin view (all records)
- ✅ GPS tracking (latitude/longitude)
- ✅ Duplicate checkin prevention
- ✅ Sequential checkout validation
- ✅ Role-based access control
- ✅ Configurable jam-masuk & toleransi

---

## 🚀 NEXT STEPS

**Tahap 5 - RFID Integration:**
1. Add RFID checkin endpoint: `POST /api/presensi/rfid/checkin`
2. Find user by rfidCardId (already exists in Siswa/Guru entity)
3. Auto-checkin tanpa login (karena identify by RFID)
4. Simulation mode (manual input), real device later

**Tahap 6 - Barcode/QR Integration:**
1. Generate unique barcode per user
2. Scan barcode → checkin
3. QR code support

**Tahap 7 - Face Recognition:**
1. Upload foto wajah
2. Face matching algorithm
3. Liveness detection

---

## 📚 REFERENSI

- **POSTMAN-TAHAP-04.md** - Testing guide lengkap
- **README-TAHAP-04.md** - Architecture overview
- **PLAN.MD** - Overall project plan
- Spring Boot Docs: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Java 8 Date/Time API: https://www.oracle.com/technical-resources/articles/java/jf14-date-time.html

---

## ✅ CHECKLIST COMPLETION

- [x] Step 1: Buat 3 Enums (TipeUser, StatusPresensi, MethodPresensi)
- [x] Step 2: Buat Entity Presensi
- [x] Step 3: Buat PresensiRepository
- [x] Step 4: Buat 3 DTOs (Request & Response)
- [x] Step 5: Buat PresensiService (business logic)
- [x] Step 6: Buat PresensiController (REST API)
- [x] Step 7: Update application.properties (configuration)
- [x] Step 8: Compile, Run, Test

**Status: ✅ TAHAP 4 COMPLETE!**