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

Saya lanjutkan dengan file-file berikutnya. Apakah Anda ingin saya terus menulis dokumentasi lengkap seperti ini, atau lebih ringkas saja? Dokumentasi ini sudah ~600 baris dan baru sampai Step 3. 😅