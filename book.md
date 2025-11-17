
    
---

## 1. Kerangka Buku: _“Membangun Backend SIM Presensi Sekolah dengan Java Spring Boot (RFID, Face Recognition, Geolokasi)”_

### Bab 1 – Konsep Dasar SIM Presensi Sekolah Modern

- Masalah presensi manual di sekolah (buku tulis, tanda tangan).
    
- Kebutuhan presensi real-time dan akurat.
    
- Konsep presensi berbasis:
    
    - Kartu RFID
        
    - Face Recognition
        
    - Geolokasi (HP guru/siswa atau petugas)
        
- Gambaran umum arsitektur sistem:
    
    - Device layer (RFID reader, kamera, HP)
        
    - Backend (Spring Boot REST API)
        
    - Database
        
    - Dashboard web & mobile.
        

---

### Bab 2 – Fondasi Java dan Spring Boot untuk Backend

- Review singkat Java yang dibutuhkan (OOP, package, Maven/Gradle).
    
- Konsep Spring:
    
    - IoC Container, Bean, @Component, @Service, @Repository.
        
    - Spring Boot auto-configuration.
        
- Struktur proyek Spring Boot:
    
    - `src/main/java`
        
    - `src/main/resources`
        
- Menyiapkan project:
    
    - Spring Initializr (Web, JPA, Validation, Security, Lombok, dll).
        

---

### Bab 3 – Desain Arsitektur Sistem Presensi

- Pendekatan monolit modular vs microservices (pilihan untuk sekolah).
    
- Arsitektur logis:
    
    - Modul `user & auth`
        
    - Modul `master-data` (siswa, guru, kelas, mapel)
        
    - Modul `device` (RFID reader, kamera, mobile app)
        
    - Modul `attendance`
        
    - Modul `face-recognition` (service external / microservice lain)
        
    - Modul `geolocation`
        
    - Modul `reporting`.
        
- Layering:
    
    - Controller → Service → Repository → Database.
        
- DTO, Mapper, dan Entity.
    

---

### Bab 4 – Desain Basis Data untuk SIM Presensi

- Identifikasi entitas utama:
    
    - `Student`, `Teacher`, `User`, `Role`
        
    - `ClassRoom`, `Subject`, `Schedule`
        
    - `Device` (RFID terminal, kamera, mobile)
        
    - `AttendanceRecord`
        
    - `FaceTemplate`
        
    - `LocationZone` (geofence sekolah).
        
- Relasi:
    
    - Student–ClassRoom
        
    - Teacher–Subject
        
    - Schedule–ClassRoom–Teacher
        
- ERD dan skema tabel + tipe data.
    

---

### Bab 5 – Implementasi Master Data di Spring Boot

- Entity & Repository untuk:
    
    - Student, Teacher, ClassRoom, Subject.
        
- Service layer:
    
    - CRUD siswa, guru, kelas, mapel.
        
- REST API:
    
    - Endpoint contoh: `/api/students`, `/api/teachers`.
        
- Validasi input (Bean Validation).
    
- Mapping Entity ↔ DTO.
    

---

### Bab 6 – Modul User Management, Otentikasi, dan Otorisasi

- Konsep role (Admin Sekolah, Operator, Guru, Siswa).
    
- Spring Security dasar:
    
    - Filter chain
        
    - Authentication & Authorization.
        
- JWT:
    
    - Struktur JWT
        
    - Generate token & validate token.
        
- API:
    
    - `/api/auth/login`
        
    - `/api/auth/me`
        
- Proteksi endpoint berdasarkan role:
    
    - Admin: kelola master data
        
    - Guru: melihat presensi kelasnya
        
    - Siswa: melihat histori presensinya.
        

---

### Bab 7 – Integrasi RFID untuk Presensi

- Konsep dasar RFID:
    
    - Kartu/tag → UID/ID → dikirim ke backend.
        
- Pola komunikasi:
    
    - RFID terminal → Backend (HTTP REST / MQTT gateway).
        
- Desain API:
    
    - `POST /api/attendance/rfid`
        
        - body: `{deviceId, rfidTag, timestamp}`
            
- Logika backend:
    
    - Cocokkan `rfidTag` ↔ `Student` / `Teacher`.
        
    - Cek jadwal (Schedule).
        
    - Simpan `AttendanceRecord`.
        
- Penanganan kasus:
    
    - Tag tidak terdaftar.
        
    - Presensi di luar jam pelajaran.
        

---

### Bab 8 – Integrasi Face Recognition

- Arsitektur:
    
    - Face recognition engine (Python/C++/cloud service) sebagai service terpisah.
        
    - Spring Boot memanggil API face-service.
        
- Data:
    
    - `FaceTemplate` (feature vector / ID template eksternal).
        
- Alur:
    
    - Enrollment: simpan template wajah per student/teacher.
        
    - Verification:
        
        - Client kirim `faceImage` atau `faceToken` ke backend
            
        - Backend kirim ke face-service
            
        - face-service balas: `matchedUserId` + skor.
            
- API contoh:
    
    - `POST /api/face/enroll`
        
    - `POST /api/attendance/face`
        
- Strategi anti-spoofing (high level, bukan implementasi CV detail).
    

---

### Bab 9 – Presensi Berbasis Geolokasi (Geofencing)

- Konsep geofencing:
    
    - Lokasi sekolah: lat, long, radius (meter).
        
    - Lokasi PKL: beberapa zona.
        
- Entity:
    
    - `LocationZone` dengan `lat`, `lng`, `radius`.
        
- API:
    
    - `POST /api/attendance/geo`
        
        - body: `{userId, lat, lng, timestamp, deviceInfo}`
            
- Fungsi backend:
    
    - Hitung jarak (Haversine).
        
    - Validasi: di dalam radius zona aktif?
        
    - Simpan `AttendanceRecord` jika valid.
        
- Kasus penggunaan:
    
    - Presensi guru via HP ketika mengajar.
        
    - Presensi siswa di lokasi PKL.
        

---

### Bab 10 – Pengelolaan Aturan Presensi & Bisnis Proses

- Aturan kehadiran:
    
    - On time, telat, tidak hadir.
        
- Pengaturan jam masuk/keluar per kelas/mapel.
    
- Logika perhitungan status:
    
    - Menggunakan schedule + toleransi keterlambatan.
        
- Penanganan presensi double, batal, dan koreksi manual.
    

---

### Bab 11 – Laporan, Dashboard, dan Integrasi

- Endpoint laporan:
    
    - Rekap harian, mingguan, bulanan per kelas/guru/siswa.
        
- Export Excel / PDF.
    
- API untuk frontend:
    
    - Dashboard kehadiran real-time.
        
    - Statistik keterlambatan, absensi.
        
- Integrasi ke sistem lain:
    
    - Sistem nilai, HR (guru/karyawan).
        

---

### Bab 12 – Keamanan, Logging, dan Deployment

- Keamanan:
    
    - Proteksi data siswa (privacy).
        
    - HTTPS, CORS, rate limiting.
        
- Logging dan audit:
    
    - Siapa mengubah data presensi.
        
- Deployment:
    
    - Mode on-premise di server sekolah (VM/Container).
        
    - Docker + docker-compose untuk Spring Boot + DB.
        
    - Backup & restore database.
        
- Best practice pemeliharaan sistem.
    

---

## 2. Implementasi ke Kode: Skeleton Spring Boot SIM Presensi

Berikut contoh struktur paket dan beberapa potongan kode kunci (bisa Anda kembangkan jadi modul lengkap).

### 2.1. Struktur Paket

```text
com.sekolah.presensi
 ├─ PresensiApplication.java
 ├─ config
 ├─ security
 ├─ masterdata
 │   ├─ student
 │   ├─ teacher
 │   ├─ classroom
 │   └─ subject
 ├─ attendance
 ├─ device
 ├─ face
 ├─ geo
 └─ reporting
```

---

### 2.2. Contoh Entity Inti

#### `Student`

```java
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String nis;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private ClassRoom classRoom;

    @Column(name = "rfid_tag", unique = true)
    private String rfidTag;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private FaceTemplate faceTemplate;
}
```

#### `AttendanceRecord`

```java
@Entity
@Table(name = "attendance_records",
       indexes = {
           @Index(name = "idx_student_date", columnList = "student_id,attendance_date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    private AttendanceMethod method; // RFID, FACE, GEO

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status; // ON_TIME, LATE, ABSENT

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;
}
```

#### Enum pendukung

```java
public enum AttendanceMethod {
    RFID, FACE, GEO
}

public enum AttendanceStatus {
    ON_TIME, LATE, ABSENT
}
```

---

### 2.3. Endpoint Presensi via RFID

#### DTO Request

```java
@Data
public class RfidAttendanceRequest {
    @NotBlank
    private String deviceId;

    @NotBlank
    private String rfidTag;

    @NotNull
    private LocalDateTime timestamp;
}
```

#### Service Logic (sederhana)

```java
@Service
@RequiredArgsConstructor
public class RfidAttendanceService {

    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ScheduleService scheduleService; // untuk cek jam & mapel

    @Transactional
    public AttendanceRecord handleRfidAttendance(RfidAttendanceRequest req) {
        Student student = studentRepository
                .findByRfidTag(req.getRfidTag())
                .orElseThrow(() -> new IllegalArgumentException("RFID tidak terdaftar"));

        LocalDate date = req.getTimestamp().toLocalDate();

        // cek apakah sudah ada record hari ini
        AttendanceRecord record = attendanceRecordRepository
                .findByStudentAndDate(student, date)
                .orElseGet(() -> AttendanceRecord.builder()
                        .student(student)
                        .date(date)
                        .build()
                );

        // tentukan status (ON_TIME / LATE) berdasarkan schedule
        AttendanceStatus status = scheduleService
                .determineStatus(student, req.getTimestamp());

        if (record.getCheckInTime() == null) {
            record.setCheckInTime(req.getTimestamp());
            record.setStatus(status);
            record.setMethod(AttendanceMethod.RFID);
            record.setDeviceId(req.getDeviceId());
        } else {
            // anggap ini checkout
            record.setCheckOutTime(req.getTimestamp());
        }

        return attendanceRecordRepository.save(record);
    }
}
```

#### Controller

```java
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final RfidAttendanceService rfidAttendanceService;

    @PostMapping("/rfid")
    public ResponseEntity<AttendanceRecordResponse> handleRfid(
            @Valid @RequestBody RfidAttendanceRequest request) {

        AttendanceRecord record = rfidAttendanceService.handleRfidAttendance(request);
        return ResponseEntity.ok(AttendanceRecordResponse.fromEntity(record));
    }
}
```

---

### 2.4. Endpoint Presensi via Face Recognition

#### DTO Request

```java
@Data
public class FaceAttendanceRequest {
    @NotBlank
    private String deviceId;

    // bisa berupa token/ID template yang sudah di-generate di client/engine
    @NotBlank
    private String faceToken;

    @NotNull
    private LocalDateTime timestamp;
}
```

#### Service (menghubungi face-recognition service eksternal)

```java
@Service
@RequiredArgsConstructor
public class FaceAttendanceService {

    private final FaceRecognitionClient faceRecognitionClient; // pakai RestTemplate/WebClient
    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ScheduleService scheduleService;

    @Transactional
    public AttendanceRecord handleFaceAttendance(FaceAttendanceRequest req) {
        // panggil service eksternal
        FaceMatchResult match = faceRecognitionClient.verify(req.getFaceToken());

        if (!match.isMatched() || match.getConfidence() < 0.85) {
            throw new IllegalArgumentException("Wajah tidak terverifikasi");
        }

        Student student = studentRepository.findById(match.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student tidak ditemukan"));

        LocalDate date = req.getTimestamp().toLocalDate();

        AttendanceRecord record = attendanceRecordRepository
                .findByStudentAndDate(student, date)
                .orElseGet(() -> AttendanceRecord.builder()
                        .student(student)
                        .date(date)
                        .build()
                );

        AttendanceStatus status = scheduleService
                .determineStatus(student, req.getTimestamp());

        if (record.getCheckInTime() == null) {
            record.setCheckInTime(req.getTimestamp());
            record.setStatus(status);
            record.setMethod(AttendanceMethod.FACE);
            record.setDeviceId(req.getDeviceId());
        } else {
            record.setCheckOutTime(req.getTimestamp());
        }

        return attendanceRecordRepository.save(record);
    }
}
```

---

### 2.5. Endpoint Presensi via Geolokasi (Geofence)

#### Entity `LocationZone`

```java
@Entity
@Table(name = "location_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // "SMKN 1 Punggelan", "PKL - Toko A"

    private Double lat;
    private Double lng;

    private Double radiusMeter;
}
```

#### DTO Request

```java
@Data
public class GeoAttendanceRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    @NotNull
    private LocalDateTime timestamp;

    private String deviceInfo;
}
```

#### Service: cek radius (Haversine)

```java
@Service
@RequiredArgsConstructor
public class GeoAttendanceService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LocationZoneRepository locationZoneRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ScheduleService scheduleService;

    @Transactional
    public AttendanceRecord handleGeoAttendance(GeoAttendanceRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));

        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("User bukan siswa"));

        LocationZone zone = locationZoneRepository
                .findActiveZoneForTimestamp(req.getTimestamp())
                .orElseThrow(() -> new IllegalArgumentException("Zona presensi aktif tidak ditemukan"));

        double distance = haversine(zone.getLat(), zone.getLng(), req.getLat(), req.getLng());

        if (distance > zone.getRadiusMeter()) {
            throw new IllegalArgumentException("Lokasi berada di luar zona presensi");
        }

        LocalDate date = req.getTimestamp().toLocalDate();

        AttendanceRecord record = attendanceRecordRepository
                .findByStudentAndDate(student, date)
                .orElseGet(() -> AttendanceRecord.builder()
                        .student(student)
                        .date(date)
                        .build()
                );

        AttendanceStatus status = scheduleService
                .determineStatus(student, req.getTimestamp());

        if (record.getCheckInTime() == null) {
            record.setCheckInTime(req.getTimestamp());
            record.setStatus(status);
            record.setMethod(AttendanceMethod.GEO);
            record.setLat(req.getLat());
            record.setLng(req.getLng());
        } else {
            record.setCheckOutTime(req.getTimestamp());
        }

        return attendanceRecordRepository.save(record);
    }

    // rumus Haversine (km → meter)
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // radius bumi dalam meter
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
```

---

# **BAB 1 – Konsep Dasar SIM Presensi Sekolah Modern**


## **1.1 Pendahuluan**

Perkembangan teknologi informasi dalam beberapa dekade terakhir telah menghadirkan perubahan besar dalam berbagai aspek kehidupan manusia, termasuk dalam dunia pendidikan. Sekolah—sebagai institusi formal yang bertanggung jawab membentuk karakter, kompetensi, dan budaya kerja generasi muda—tidak lagi dapat dipisahkan dari pemanfaatan teknologi digital. Salah satu aspek administratif yang sangat terdampak oleh transformasi ini adalah proses _presensi_ atau pencatatan kehadiran siswa dan guru.

Presensi merupakan elemen fundamental dalam manajemen sekolah. Kehadiran siswa menjadi indikator kedisiplinan, keterlibatan dalam pembelajaran, dan juga menjadi salah satu instrumen evaluasi tingkat keberhasilan kegiatan belajar mengajar. Bagi guru dan tenaga pendidikan, kehadiran menjadi bagian penting dalam penilaian kinerja, tata tertib kepegawaian, serta administrasi sekolah secara keseluruhan.

Meski tampak sederhana, presensi adalah proses yang berpengaruh terhadap banyak aspek, seperti:

- Perencanaan pembelajaran
    
- Rekapitulasi absensi harian, mingguan, dan bulanan
    
- Laporan administratif sekolah
    
- Penilaian kinerja guru
    
- Komunikasi antara sekolah dan wali murid
    
- Analisis perilaku siswa dan intervensi dini
    

Di banyak sekolah di Indonesia—termasuk SMK, SMA, SMP, hingga perguruan tinggi—pencatatan kehadiran masih dilakukan dengan metode _manual_, seperti buku tanda tangan, kartu presensi, lembar absen kelas, dan metode tradisional lainnya.

Sayangnya, sistem presensi manual ini menyimpan berbagai permasalahan mendasar yang membuat akurasi data menjadi kurang dapat diandalkan. Oleh sebab itu, sekolah membutuhkan sistem presensi modern yang mampu mendukung proses pencatatan kehadiran secara otomatis, real-time, terintegrasi, dan akurat.

Bab pertama ini akan mengupas secara mendalam mengenai:

1. Masalah presensi manual di sekolah
    
2. Kebutuhan presensi real-time dan akurat
    
3. Konsep presensi berbasis **RFID**, **Face Recognition**, dan **Geolokasi**
    
4. Gambaran arsitektur umum Sistem Informasi Manajemen Presensi Berbasis Spring Boot
    

Pembahasan ini akan menjadi pondasi sebelum masuk ke bab-bab selanjutnya yang bersifat teknis dan implementatif.

---

## **1.2 Masalah Presensi Manual di Sekolah**

Sebelum memahami presensi modern, kita perlu meninjau terlebih dahulu masalah-masalah yang muncul pada proses presensi tradisional.

### **1.2.1 Rentan Manipulasi**

Di banyak sekolah, presensi siswa menggunakan:

- Buku daftar hadir
    
- Lembar absen
    
- Tanda tangan
    
- Pengisian manual oleh wali kelas
    

Metode ini sangat rentan terhadap **manipulasi**, misalnya:

- Titip tanda tangan
    
- Titip absen
    
- Pengisian ulang setelah terlambat
    
- Guru atau petugas lupa mencatat kehadiran
    

Manipulasi ini terjadi karena tidak adanya verifikasi identitas otomatis.

### **1.2.2 Human Error**

Presensi manual mengandalkan:

- Ingatan guru
    
- Ketelitian petugas
    
- Ketepatan waktu pencatatan
    

Human error mudah terjadi, seperti:

- Salah menulis nama
    
- Tertukar kelas
    
- Data tertinggal
    
- Lembaran hilang
    
- Rekapitulasi tidak sinkron
    

Saat guru mengelola 30-36 siswa per kelas, risiko kesalahan meningkat signifikan.

### **1.2.3 Pencatatan Tidak Real-Time**

Presensi manual menyebabkan:

- Data kehadiran tidak langsung tersedia
    
- Rekapitulasi baru dibuat di akhir hari atau akhir minggu
    
- Orang tua tidak mendapatkan notifikasi jika siswa tidak hadir
    
- Kepala sekolah tidak dapat memantau situasi kelas secara langsung
    

Ketiadaan data real-time membuat pengawasan kedisiplinan menjadi lemah.

### **1.2.4 Sulit Dievaluasi dan Dilaporkan**

Ketika data presensi tersebar dalam banyak buku:

- Proses rekapitulasi menjadi lambat
    
- Pencarian data lama memakan waktu
    
- Penyusunan laporan semester menjadi rumit
    
- Analisis tren absensi tidak dapat dilakukan
    

Dalam konteks pembinaan kedisiplinan, sekolah membutuhkan data yang:

- Terpusat
    
- Mudah dicari
    
- Mudah dianalisis
    
- Mudah divisualisasikan
    

Presensi manual tidak mampu menyediakan hal ini.

### **1.2.5 Tidak Mendukung Era Digital**

Sekolah modern harus beradaptasi dengan:

- Sistem akademik digital
    
- Dashboard manajemen
    
- Kebutuhan monitoring online
    
- Transparansi data untuk orang tua
    
- Keperluan reporting ke dinas pendidikan
    

Presensi manual bertolak belakang dengan kebutuhan ini karena sifatnya statis dan tidak dapat diintegrasikan.

---

## **1.3 Kebutuhan Presensi Real-Time dan Akurat**

Melihat berbagai kekurangan presensi manual, sekolah membutuhkan sistem presensi yang mampu menjawab tuntutan zaman. Setidaknya ada tiga kebutuhan pokok dari presensi modern:

### **1.3.1 Kecepatan dan Otomatisasi**

Siswa dan guru tidak boleh menghabiskan waktu terlalu lama hanya untuk proses presensi. Sistem harus:

- Berjalan otomatis
    
- Cepat
    
- Tidak menimbulkan antrian panjang
    
- Dapat beroperasi secara serentak
    

Teknologi seperti **RFID** atau **Face Recognition** sangat ideal untuk ini.

### **1.3.2 Akurasi Tinggi**

Data kehadiran harus benar-benar mencerminkan kondisi nyata. Dengan teknologi modern:

- Identitas siswa diverifikasi otomatis
    
- Tidak ada titip absen
    
- Tidak ada duplikasi
    
- Jam masuk dan pulang tercatat otomatis
    

Akurasi ini meningkatkan kualitas pengawasan dan evaluasi sekolah.

### **1.3.3 Real-Time Monitoring**

Bagi sekolah dan orang tua, real-time monitoring memungkinkan:

- Notifikasi otomatis ke orang tua
    
- Dashboard kehadiran harian
    
- Deteksi siswa yang tidak masuk sejak pagi
    
- Pengambilan keputusan cepat jika ada siswa bermasalah
    

Teknologi cloud, server API, dan aplikasi mobile mendukung proses ini.

### **1.3.4 Integrasi dengan Sistem Lain**

Sistem presensi modern harus mudah diintegrasikan dengan:

- Sistem akademik
    
- Sistem nilai
    
- Manajemen guru dan karyawan
    
- Sistem BK
    
- Sistem monitoring wali kelas
    
- Aplikasi orang tua
    

API berbasis Spring Boot dapat menjadi _backbone_ integrasi ini.

---

## **1.4 Konsep Presensi Berbasis Teknologi Modern**

Ada tiga teknologi utama yang banyak digunakan dalam presensi sekolah modern: **RFID**, **Face Recognition**, dan **Geolocation**. Masing-masing memiliki karakteristik dan kelebihan yang saling melengkapi.

---

## **1.4.1 Presensi Menggunakan Kartu RFID**

RFID (_Radio Frequency Identification_) adalah teknologi identifikasi menggunakan gelombang radio. RFID terdiri dari:

1. **Tag/Kartu**: berisi UID unik
    
2. **Reader**: alat pembaca
    
3. **Backend server**: memverifikasi dan mencatat data
    

### **Kelebihan RFID di sekolah**

- Sangat cepat (0,2 detik per scanning)
    
- Murah
    
- Cocok untuk presensi pintu masuk sekolah
    
- Tidak perlu antri lama
    
- Tidak membutuhkan kamera atau internet kuat
    

### **Contoh skenario penggunaan**

- Siswa menempelkan kartu pada reader di gerbang.
    
- Reader mengirim data UID ke backend.
    
- Backend mencocokkan UID dengan data siswa.
    
- Sistem otomatis menandai _hadir_.
    

Teknologi ini sangat cocok untuk:

- Gerbang sekolah
    
- Meja piket
    
- Presensi masuk/keluar guru
    
- Presensi laboratorium
    

---

## **1.4.2 Presensi Menggunakan Face Recognition**

Face Recognition adalah teknologi untuk mengenali wajah seseorang menggunakan:

- Kamera
    
- Model AI (CV/Deep Learning)
    
- Backend server
    

Teknologi ini sangat akurat karena:

- Tidak bisa dipalsukan
    
- Tidak bisa titip absen
    
- Tidak memerlukan kartu
    
- Tidak memerlukan kontak fisik
    

### **Kelebihan untuk sekolah**

- Identifikasi siswa otomatis
    
- Tidak ada kartu yang hilang
    
- Validasi lebih akurat dibanding RFID
    
- Cocok untuk memonitor wajah siswa saat masuk kelas
    
- Bisa digabung dengan anti-spoofing (deteksi foto/layar)
    

### **Contoh penggunaan**

- Di depan ruang kelas
    
- Di gerbang sekolah
    
- Di lorong sekolah dengan kamera statis
    
- Di ruang guru
    

Face recognition sangat relevan bagi SMK yang ingin:

- Mendorong teknologi modern
    
- Mengajarkan AI dan CV
    
- Implementasi presensi otomatis 100%
    

---

## **1.4.3 Presensi Menggunakan Geolokasi (HP Android/iOS)**

Presensi berbasis geolokasi menggunakan:

- GPS pada HP
    
- Zona geofence
    
- Aplikasi mobile
    

Ini sangat cocok untuk:

- Presensi guru saat mengajar
    
- Presensi siswa saat PKL/Prakerin
    
- Presensi petugas kebersihan atau satpam
    

### **Cara kerja geofence**

- Set titik GPS sekolah
    
- Tentukan radius, misalnya 70 meter
    
- Jika lokasi HP berada di dalam radius, presensi dianggap valid
    

### **Kelebihan geolocation**

- Mobile dan fleksibel
    
- Cocok untuk kegiatan di luar kelas
    
- Cocok untuk sekolah besar (luas area > 1 hektare)
    
- Cocok untuk siswa PKL di lokasi berbeda-beda
    

---

## **1.5 Gambaran Umum Arsitektur Sistem SIM Presensi Modern**

Untuk membangun sistem presensi modern berbasis **Spring Boot**, kita harus memahami struktur arsitektur umum sistem ini. Arsitektur yang baik memungkinkan:

- Sistem mudah diperluas
    
- Sistem mudah diintegrasikan
    
- Sistem aman dan stabil
    
- Sistem mampu menampung banyak request
    

Arsitektur terdiri dari beberapa lapisan, yaitu:

---

## **1.5.1 Device Layer (RFID Reader, Kamera, HP)**

Device layer adalah lapisan tempat data kehadiran berasal. Setiap perangkat memiliki cara komunikasi berbeda:

### **Perangkat dalam device layer**

1. **RFID Reader**
    
    - Komunikasi via HTTP/REST, TCP/IP, atau MQTT
        
    - Bisa dipasang di gerbang
        
    - Mengirim UID ke backend
        
2. **Kamera (Face Recognition)**
    
    - Kamera biasa + software AI
        
    - Atau kamera AI terintegrasi
        
    - Mengirim _face token_ atau _face embedding_
        
3. **HP Android/iOS**
    
    - Mengirim data lokasi + userId
        
    - Mengirim foto wajah untuk verifikasi tambahan
        

Device layer inilah yang menjadi sumber data presensi.

---

## **1.5.2 Backend Spring Boot REST API**

Backend adalah otak sistem. Backend:

- Memvalidasi identitas siswa
    
- Menghitung apakah siswa terlambat
    
- Melakukan verifikasi data wajah
    
- Mengecek lokasi berdasarkan titik GPS
    
- Menyimpan data ke database
    
- Menyediakan API untuk dashboard
    

Backend berbasis Spring Boot memungkinkan:

- High performance
    
- Security (JWT, RBAC)
    
- Modular design
    
- Integrasi mudah dengan database & cloud
    

API umum misalnya:

- POST `/api/attendance/rfid`
    
- POST `/api/attendance/face`
    
- POST `/api/attendance/geo`
    
- GET `/api/report/daily`
    
- GET `/api/student/{id}`
    

Backend memastikan seluruh proses berjalan mulus.

---

## **1.5.3 Database Layer**

Database adalah tempat penyimpanan semua data:

- Data siswa
    
- Data guru
    
- Data kelas
    
- Data jadwal
    
- Data presensi
    
- Data perangkat
    
- Data geofence
    

Database yang digunakan bisa:

- PostgreSQL
    
- MySQL
    
- MariaDB
    

Struktur database harus mendukung:

- Relasi antar tabel
    
- Query cepat untuk laporan
    
- Integritas data
    

Indeks dan relasi sangat penting dalam modul presensi.

---

## **1.5.4 Dashboard Web & Mobile**

Setelah data terkumpul, sistem harus menampilkan informasi ke:

- Kepala sekolah
    
- Wali kelas
    
- Guru
    
- Siswa
    
- Orang tua
    

Dashboard web memberikan:

- Statistik kehadiran
    
- Grafik tren
    
- Rekap harian/mingguan/bulanan
    
- Detail siswa
    
- Notifikasi
    

Aplikasi mobile memberikan:

- Presensi berbasis lokasi
    
- Notifikasi orang tua
    
- Status kehadiran siswa
    
- Informasi jadwal
    

Dashboard memanfaatkan API yang disediakan backend.

---

## **1.6 Penutup Bab 1**

Pada bab pertama ini, kita telah membahas konsep fundamental dari Sistem Informasi Manajemen (SIM) Presensi Sekolah Modern. Bab ini memberikan gambaran utuh mengenai:

- Masalah presensi manual yang masih digunakan di banyak sekolah
    
- Kebutuhan era digital: presensi cepat, akurat, dan real-time
    
- Tiga teknologi utama: RFID, Face Recognition, dan Geolocation
    
- Gambaran arsitektur modern yang menjadi dasar implementasi teknis
    

Pemahaman bab ini penting sebelum melangkah ke bab-bab berikutnya yang lebih teknis, seperti desain database, implementasi backend Spring Boot, integrasi perangkat, dan pengembangan API presensi.

---

Berikut **BAB 2** sesuai permintaan Anda, saya susun sebagai naskah buku dengan gaya penjelasan runtut, teknis, tapi tetap enak dibaca siswa SMK maupun guru/ praktisi. Panjangnya saya buat kira-kira setara 3.000 kata.

---

# **BAB 2 – Fondasi Java & Spring Boot untuk Backend SIM Presensi**

## 2.1 Pendahuluan

Sebelum membangun _backend_ Sistem Informasi Manajemen (SIM) Presensi Sekolah berbasis RFID, _face recognition_, dan geolokasi, kita perlu memiliki fondasi yang kuat pada dua hal utama:

1. **Bahasa pemrograman Java**
    
2. **Framework Spring & Spring Boot**
    

Java menjadi pilihan yang sangat populer untuk aplikasi backend karena:

- Stabil, matang, dan digunakan luas di industri
    
- Banyak digunakan pada sistem enterprise (perbankan, pemerintahan, korporasi)
    
- Ekosistem pustaka yang sangat kaya
    
- Didukung oleh Spring Framework yang mempermudah pengembangan aplikasi skala besar
    

Sementara itu, **Spring Boot** hadir sebagai “jalan cepat” untuk membangun aplikasi berbasis Spring tanpa harus melakukan konfigurasi rumit secara manual. Dengan Spring Boot, kita bisa:

- Membuat REST API dengan cepat
    
- Menghubungkan ke database
    
- Menambah modul keamanan (Spring Security)
    
- Menyusun arsitektur aplikasi yang rapi (Controller, Service, Repository)
    

Bab ini akan membahas:

- Konsep Java yang diperlukan untuk backend
    
- Konsep dasar Spring Framework: IoC, DI, Bean, Annotation
    
- Apa itu Spring Boot dan mengapa cocok untuk SIM Presensi
    
- Struktur proyek Spring Boot yang rapi
    
- Persiapan alat kerja (_tooling_): Maven/Gradle, IDE, dan konfigurasi dasar
    

Fokus bab ini bukan untuk mengajari Java dari nol, tetapi memberikan fondasi minimal yang _harus dikuasai_ sebelum masuk ke implementasi modul presensi di bab-bab berikutnya.

---

## 2.2 Fondasi Java untuk Backend

### 2.2.1 Java Sebagai Bahasa Backend

Java adalah bahasa pemrograman _object-oriented_ yang berjalan di atas JVM (Java Virtual Machine). Slogan Java yang terkenal adalah:

> “Write once, run anywhere.”

Artinya, kode Java yang dikompilasi menjadi _bytecode_ dapat dijalankan pada berbagai sistem operasi yang memiliki JVM (Windows, Linux, macOS), sehingga sangat cocok untuk server aplikasi.

Dalam konteks **SIM Presensi Sekolah**:

- Java akan digunakan untuk mengolah logika bisnis: siapa yang hadir, siapa yang terlambat, bagaimana aturan jadwal, dan sebagainya.
    
- Java akan memanggil komponen lain seperti database, layanan _face recognition_, dan modul geolokasi.
    
- Java menyediakan kerangka kerja (Spring) untuk membangun REST API.
    

### 2.2.2 Konsep OOP yang Penting

Tidak semua konsep Java perlu dibahas di sini. Kita fokus pada hal-hal yang paling relevan untuk backend:

#### a. Class dan Object

- **Class** adalah cetak biru (blueprint) dari objek.
    
- **Object** adalah instance dari class.
    

Dalam sistem presensi:

- Kita bisa punya class `Student`, `Teacher`, `AttendanceRecord`, `Device`, dan seterusnya.
    
- Setiap siswa adalah object (instance) dari class `Student`.
    

Contoh sederhana:

```java
public class Student {
    private String nis;
    private String name;

    public Student(String nis, String name) {
        this.nis = nis;
        this.name = name;
    }

    public String getNis() {
        return nis;
    }

    public String getName() {
        return name;
    }
}
```

#### b. Enkapsulasi (Encapsulation)

Enkapsulasi adalah praktik menyembunyikan data internal dan hanya membuka akses lewat _getter/setter_ atau metode publik.

- Tujuannya menjaga integritas data.
    
- Dalam konteks presensi, kita bisa membatasi siapa yang boleh mengubah status presensi.
    

#### c. Pewarisan (Inheritance)

Pewarisan memungkinkan kita membuat class baru berdasarkan class lain.

Misal:

- `User` → class umum yang punya username, password
    
- `Student` dan `Teacher` mewarisi `User` jika kita mau desain seperti itu.
    

#### d. Polimorfisme (Polymorphism)

Polimorfisme memungkinkan objek dengan tipe yang berbeda diperlakukan seolah-olah mempunyai interface yang sama.

Misalnya:

- `Student` dan `Teacher` sama-sama punya metode `getDisplayName()`.
    
- Di level logika tertentu kita hanya peduli bahwa dia `User`, bukan `Student` atau `Teacher` secara spesifik.
    

#### e. Interface dan Abstraksi

- **Interface** mendefinisikan kontrak: apa yang harus dilakukan, bukan bagaimana caranya.
    
- **Abstraksi** menutup detail implementasi agar kode lebih mudah dibaca dan dipelihara.
    

Dalam modul presensi, misalnya:

- Kita bisa punya `AttendanceService` sebagai interface.
    
- Implementasinya bisa berbeda: `RfidAttendanceService`, `FaceAttendanceService`, `GeoAttendanceService`.
    

Ini memudahkan kita mengganti implementasi tanpa mengubah bagian lain dari sistem.

---

## 2.3 Manajemen Proyek dengan Maven dan Gradle

Dalam pengembangan Java modern, hampir semua proyek backend menggunakan **Maven** atau **Gradle** untuk:

- Mengelola dependensi (library pihak ketiga)
    
- Mengatur proses kompilasi
    
- Menjalankan aplikasi
    
- Membuat paket (JAR/ WAR)
    

Spring Boot secara default banyak menggunakan **Maven**, namun Gradle juga sangat didukung.

### 2.3.1 Apa Itu Maven?

Maven adalah alat _build automation_ yang menggunakan file konfigurasi bernama `pom.xml`. Di dalam `pom.xml` kita mendefinisikan:

- Nama proyek
    
- Versi Java
    
- Dependensi (Spring Web, Spring Data JPA, Lombok, dll.)
    
- Plugin build
    

Contoh (yang akan sering kita lihat di proyek SIM Presensi):

```xml
<dependencies>
    <!-- Web / REST API -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JPA & Hibernate untuk akses database -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Driver PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok untuk mengurangi boilerplate (getter/setter, dll.) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 2.3.2 Apa Itu Gradle?

Gradle adalah alternatif Maven dengan pendekatan skrip yang lebih fleksibel dan ringkas (`build.gradle`). Banyak proyek modern juga beralih ke Gradle karena dianggap lebih cepat.

Namun, dalam konteks buku ini, kita dapat fokus pada Maven agar siswa dan pembaca yang baru masuk dunia backend tidak bingung terlalu banyak pilihan di awal.

---

## 2.4 Mengenal Spring & Konsep Intinya

Spring adalah salah satu framework Java paling populer di dunia. Awalnya muncul untuk:

- Mengurangi kerumitan JEE (Java Enterprise Edition)
    
- Menyediakan cara yang lebih enak untuk membuat aplikasi enterprise skala besar
    

Untuk bisa memanfaatkan Spring Boot dengan maksimal, kita perlu memahami beberapa konsep dasar Spring:

1. **IoC Container (Inversion of Control)**
    
2. **Dependency Injection (DI)**
    
3. **Bean**
    
4. **Annotation**
    

Mari kita bahas satu per satu.

### 2.4.1 IoC (Inversion of Control)

Dalam pemrograman biasa, kita yang mengontrol kapan sebuah objek dibuat dan bagaimana ia digunakan. Misalnya:

```java
StudentService service = new StudentService(new StudentRepository());
```

Di Spring, kontrol ini dibalik (_inverted_). Kita tidak lagi membuat objek secara manual, tetapi:

- Kita mendefinisikan class yang perlu dikelola Spring.
    
- Spring yang akan membuat, mengatur, dan menyuntikkan objek ke class lain.
    

Objek yang dikelola oleh Spring ini disebut **Bean**.

### 2.4.2 Dependency Injection (DI)

DI adalah teknik di mana dependensi (ketergantungan) sebuah objek disediakan dari luar, bukan dibuat sendiri di dalam class.

Contoh tanpa DI:

```java
public class AttendanceService {
    private StudentRepository studentRepository = new StudentRepository();
}
```

Contoh dengan DI versi Spring:

```java
@Service
public class AttendanceService {

    private final StudentRepository studentRepository;

    public AttendanceService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
}
```

`StudentRepository` disediakan (di-_inject_) oleh Spring. Manfaatnya:

- Kode lebih mudah diuji (unit test)
    
- Mengurangi _coupling_ antar class
    
- Memudahkan penggantian implementasi
    

DL di SIM Presensi, kita akan sering melihat pola ini antara:

- `AttendanceController` ↔ `AttendanceService`
    
- `AttendanceService` ↔ `AttendanceRecordRepository`
    

### 2.4.3 Bean

**Bean** adalah objek yang dikelola oleh Spring Container.

Kita bisa memberi tahu Spring bahwa suatu class adalah Bean dengan beberapa annotation, seperti:

- `@Component`
    
- `@Service`
    
- `@Repository`
    
- `@Controller`
    
- `@RestController`
    

Misal:

```java
@Service
public class RfidAttendanceService {
    // Spring akan membuat bean dari class ini
}
```

Bean adalah “pemain utama” di dalam aplikasi Spring. Mereka berinteraksi satu sama lain melalui DI.

### 2.4.4 Annotation (Anotasi) dalam Spring

**Annotation** adalah cara menempelkan metadata pada kode. Spring memanfaatkan annotation untuk:

- Menandai Bean
    
- Menandai Controller
    
- Menentukan mapping endpoint REST
    
- Mengatur transaksi, validasi, keamanan, dll.
    

Contoh annotation yang akan sering kita gunakan di buku ini:

- `@RestController` → Mengubah class jadi REST Controller
    
- `@RequestMapping` / `@GetMapping` / `@PostMapping` → Mengatur URL endpoint
    
- `@Service` → Menandai class logika bisnis
    
- `@Repository` → Menandai class akses database
    
- `@Entity` → Menandai class sebagai tabel database
    

Annotation ini akan kita pakai berkali-kali ketika membangun modul presensi RFID, Face, dan Geo.

---

## 2.5 Apa Itu Spring Boot?

Spring Boot adalah “kemasan praktis” dari Spring Framework. Tujuannya:

- Mengurangi konfigurasi manual
    
- Menyediakan _default configuration_ yang masuk akal
    
- Mempercepat proses _bootstrapping_ aplikasi
    

Dengan Spring Boot, kita tidak perlu lagi:

- Menulis file XML konfigurasi panjang
    
- Mengatur server aplikasi manual
    

Cukup:

- Buat proyek Spring Boot (via Spring Initializr)
    
- Tambahkan dependensi
    
- Tulis kode bisnis
    

Spring Boot mengusung beberapa konsep kunci:

### 2.5.1 Convention over Configuration

Spring Boot menggunakan _konvensi_ bawaan yang umum dipakai, sehingga kita tidak perlu mengkonfigurasi hal-hal dasar berulang kali.

Misal:

- Secara default aplikasi akan berjalan di port `8080`.
    
- Spesifik nama properti di `application.properties` sudah dikenali Spring Boot.
    
- Struktur direktori standar otomatis dikenali.
    

### 2.5.2 Auto-Configuration

Spring Boot mencoba secara otomatis mengatur konfigurasi berdasarkan:

- Dependensi yang ada di `pom.xml`
    
- Properti di `application.properties` atau `application.yml`
    

Contoh:

- Jika kita menambahkan `spring-boot-starter-web`, Spring Boot akan mengkonfigurasi:
    
    - DispatcherServlet
        
    - Jackson (JSON)
        
    - Konfigurasi default untuk REST
        
- Jika kita menambahkan `spring-boot-starter-data-jpa` dan driver database (PostgreSQL), Spring Boot akan:
    
    - Mengatur JPA, Hibernate
        
    - Menghubungkan ke database berdasarkan properti URL, username, password yang kita isikan
        

### 2.5.3 Embedded Server

Spring Boot menyediakan server ter-embedded (Tomcat/Jetty):

- Tidak perlu pasang Tomcat terpisah
    
- Cukup jalankan aplikasi dengan perintah `mvn spring-boot:run` atau jalankan class `main`
    
- Sangat cocok untuk pengembangan cepat dan deployment dengan Docker
    

Ini sangat berguna untuk SIM Presensi yang nantinya akan:

- Dijalankan di server sekolah (on-premise)
    
- Atau di server VPS/cloud
    

---

## 2.6 Struktur Proyek Spring Boot

Struktur standar proyek Spring Boot biasanya seperti berikut:

```text
src
 └─ main
     ├─ java
     │   └─ com.sekolah.presensi
     │        ├─ PresensiApplication.java
     │        ├─ config
     │        ├─ security
     │        ├─ masterdata
     │        │    ├─ student
     │        │    ├─ teacher
     │        │    └─ classroom
     │        ├─ attendance
     │        ├─ device
     │        ├─ face
     │        ├─ geo
     │        └─ reporting
     └─ resources
         ├─ application.properties
         └─ static / templates (jika perlu)
```

### 2.6.1 Class Utama: `PresensiApplication.java`

Class ini berisi metode `main()` yang menjadi titik masuk aplikasi.

```java
@SpringBootApplication
public class PresensiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PresensiApplication.class, args);
    }
}
```

Annotation `@SpringBootApplication` adalah gabungan dari:

- `@Configuration`
    
- `@EnableAutoConfiguration`
    
- `@ComponentScan`
    

Artinya:

- Class ini mendefinisikan konfigurasi utama aplikasi
    
- Spring Boot akan melakukan auto-configuration
    
- Spring akan melakukan _scan_ terhadap package di bawah `com.sekolah.presensi` untuk mencari Bean
    

### 2.6.2 Paket Berdasarkan Modul (Modular Package Structure)

Untuk SIM Presensi dengan banyak fitur, disarankan memisahkan kode berdasarkan _modul fungsional_, bukan hanya berdasarkan layer.

Contoh pembagian:

- `masterdata.student` → entity, DTO, service, controller khusus siswa
    
- `attendance` → modul untuk proses presensi
    
- `face` → modul integrasi face recognition
    
- `geo` → modul geolokasi
    
- `device` → modul pendataan device RFID/kamera
    

Struktur ini akan memudahkan kita ketika sistem berkembang, misalnya:

- Menambah modul presensi untuk kegiatan ekstrakurikuler
    
- Menambah integrasi dengan sistem e-Raport
    
- Membuat microservice terpisah jika suatu modul menjadi sangat besar
    

---

## 2.7 Konfigurasi Dasar Spring Boot untuk SIM Presensi

Sebelum kita melangkah ke implementasi teknis, kita perlu tahu apa saja konfigurasi dasar yang wajib ada di sebuah aplikasi Spring Boot untuk backend SIM Presensi.

### 2.7.1 Pengaturan `application.properties` atau `application.yml`

File ini berada di `src/main/resources`. Di sini kita mengatur:

- Koneksi database
    
- Port server
    
- Pengaturan JPA / Hibernate
    
- Pengaturan log
    

Contoh minimal:

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/presensi_db
spring.datasource.username=presensi_user
spring.datasource.password=rahasia_smk

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Penjelasan singkat:

- `server.port` → port aplikasi (default 8080)
    
- `spring.datasource.*` → koneksi ke database Postgres
    
- `spring.jpa.hibernate.ddl-auto=update` → Hibernate akan menyesuaikan skema tabel berdasarkan entity (untuk development; pada produksi, sebaiknya pakai `validate` atau migrasi database terkontrol)
    

### 2.7.2 Entity dan JPA Repository

Untuk berinteraksi dengan database, kita biasanya:

1. Membuat **Entity** (class Java yang direlasikan ke tabel database)
    
2. Membuat **Repository** (interface) untuk akses data
    

Contoh entity sederhana:

```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String nis;

    @Column(nullable = false, length = 100)
    private String name;

    // getter, setter, constructor, dll.
}
```

Repository:

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByNis(String nis);
}
```

Spring Data JPA akan otomatis memberikan implementasi dasar (CRUD) tanpa kita menulis SQL secara manual.

### 2.7.3 Controller, Service, dan Repository

Untuk setiap fitur (misalnya presensi RFID), idealnya kita punya:

- **Controller** → menangani HTTP request/response
    
- **Service** → mengelola logika bisnis
    
- **Repository** → interaksi dengan database
    

Contoh pola standar (akan kita dalami di bab lain):

```java
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public List<StudentResponse> getAll() {
        return studentService.getAllStudents();
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(StudentResponse::fromEntity)
                .toList();
    }
}
```

---

## 2.8 Alat Bantu (Tooling) Pengembangan

Agar pengembangan SIM Presensi dengan Spring Boot lebih nyaman, kita memerlukan beberapa alat:

### 2.8.1 IDE (Integrated Development Environment)

Beberapa pilihan IDE yang umum dipakai:

- **IntelliJ IDEA** (Community Edition cukup untuk mulai)
    
- **Eclipse**
    
- **VS Code** dengan ekstensi Java
    

IDE mempermudah:

- Auto-complete kode
    
- Navigasi class dan file
    
- Debugging
    
- Refactoring (rename, extract method)
    

Dalam konteks pembelajaran di SMK, penggunaan IntelliJ IDEA atau VS Code sering lebih menarik karena UI modern dan mudah dipahami siswa.

### 2.8.2 Spring Initializr

Spring Initializr adalah generator proyek Spring Boot yang bisa diakses lewat:

- Website (start.spring.io)
    
- Langsung dari IDE (IntelliJ, Spring Tools Suite, dsb.)
    

Di sini kita menentukan:

- Group → misalnya: `com.sekolah.presensi`
    
- Artifact → misalnya: `sim-presensi`
    
- Bahasa → Java
    
- Build → Maven
    
- Dependensi:
    
    - Spring Web
        
    - Spring Data JPA
        
    - PostgreSQL Driver
        
    - Lombok
        
    - Spring Security (untuk bab keamanan)
        

Proyek akan ter-generate dengan struktur yang sudah siap dikembangkan.

---

## 2.9 Menghubungkan Fondasi ke Studi Kasus: SIM Presensi Sekolah

Setelah memahami konsep dasar Java, Spring, dan Spring Boot, kita perlu mulai membayangkan bagaimana fondasi ini digunakan dalam konteks nyata, yaitu:

> _“Membangun backend SIM Presensi Sekolah berbasis RFID, Face Recognition, dan Geolokasi.”_

Secara garis besar, alur implementasinya adalah sebagai berikut:

1. **Mendefinisikan entitas utama dalam bentuk class Java**
    
    - `Student`, `Teacher`, `ClassRoom`, `User`, `AttendanceRecord`, `Device`, dsb.
        
    - Menggunakan annotation `@Entity`, `@Table`, dan mapping relasi.
        
2. **Membuat Repository untuk masing-masing entitas**
    
    - `StudentRepository`, `AttendanceRecordRepository`, `LocationZoneRepository`, dsb.
        
    - Menggunakan `JpaRepository`.
        
3. **Menyusun Service yang mengimplementasikan logika bisnis presensi**
    
    - `RfidAttendanceService` untuk memproses UID kartu RFID
        
    - `FaceAttendanceService` untuk menerima hasil verifikasi wajah
        
    - `GeoAttendanceService` untuk memvalidasi lokasi HP dengan geofence
        
4. **Membuat Controller yang menyediakan REST API**
    
    - Endpoint-endpoint seperti:
        
        - `POST /api/attendance/rfid`
            
        - `POST /api/attendance/face`
            
        - `POST /api/attendance/geo`
            
    - Controller akan memanggil Service, dan Service memanggil Repository.
        
5. **Mengatur konfigurasi aplikasi di `application.properties`**
    
    - Koneksi ke database sekolah
        
    - `ddl-auto` untuk pengaturan skema
        
    - Pengaturan port server
        

Dengan fondasi Java & Spring Boot yang kokoh, seluruh langkah ini akan terasa jauh lebih mudah karena pola dan “bahasa” yang digunakan selalu sama:

- Controller → Service → Repository → Database
    
- Entity → DTO → Response Model
    
- Konfigurasi → dikelola lewat property dan auto-configuration
    

---

## 2.10 Penutup Bab 2

Pada bab ini, kita telah membahas fondasi penting yang dibutuhkan sebelum menyentuh implementasi teknis SIM Presensi Sekolah:

1. **Java untuk Backend**
    
    - Konsep OOP yang relevan: class, object, enkapsulasi, inheritance, polymorphism, interface, dan abstraksi.
        
2. **Manajemen Proyek dengan Maven/Gradle**
    
    - Peran `pom.xml` dalam mengelola dependensi dan build.
        
3. **Konsep Inti Spring**
    
    - IoC, Dependency Injection, Bean, dan Annotation sebagai fondasi cara kerja Spring.
        
4. **Spring Boot**
    
    - Auto-configuration, embedded server, dan pendekatan _convention over configuration_ untuk mempercepat pengembangan.
        
5. **Struktur Proyek**
    
    - Pembagian paket berdasarkan modul, class utama `PresensiApplication`, dan file konfigurasi `application.properties`.
        
6. **Alat Pendukung**
    
    - IDE, Spring Initializr, serta tool lain yang membantu proses coding.
        

Fondasi inilah yang akan kita gunakan secara intensif di bab-bab berikutnya saat:

- Mendesain arsitektur sistem presensi (Bab 3)
    
- Membuat desain database dan ERD (Bab 4)
    
- Mengimplementasikan modul master data, presensi RFID, face recognition, dan geolokasi (Bab 5 dan seterusnya)
    

---


---

# **BAB 3 – Desain Arsitektur Sistem Presensi Berbasis Spring Boot**

## 3.1 Pendahuluan

Pada Bab 1, kita sudah membahas **konsep dasar SIM Presensi Sekolah modern**: masalah presensi manual, kebutuhan real-time, serta penggunaan **RFID, face recognition, dan geolokasi**.

Pada Bab 2, kita memperkuat **fondasi teknis**: Java, OOP, Spring, dan Spring Boot sebagai _tool utama_ untuk membangun backend.

Bab 3 ini adalah “jembatan” antara konsep dan implementasi:

> Bagaimana semua konsep itu disusun dalam **sebuah arsitektur sistem yang rapi, modular, dan siap diimplementasikan dengan Spring Boot?**

Di bab ini, kita akan:

- Menentukan **gaya arsitektur** yang cocok untuk sistem presensi sekolah.
    
- Menjelaskan **layered architecture** yang akan dipakai.
    
- Mendefinisikan **modul-modul utama** (auth, master data, attendance, device, face, geo, reporting).
    
- Menjelaskan **alur request** dari device (RFID reader, kamera, HP) ke backend hingga ke database dan dashboard.
    
- Menyisipkan **diagram teks** yang bisa dengan mudah dikonversi ke diagram grafis (misalnya dengan draw.io, Mermaid, atau Visio).
    

---

## 3.2 Kebutuhan Arsitektur Sistem Presensi

Sebelum memilih arsitektur, kita perlu memahami **kebutuhan** sistem, baik dari sisi fungsional maupun non-fungsional.

### 3.2.1 Kebutuhan Fungsional (Functional Requirements)

Secara garis besar, SIM Presensi Sekolah modern harus mampu:

1. **Mencatat presensi siswa/guru/karyawan** melalui:
    
    - Kartu RFID
        
    - Face recognition
        
    - Geolokasi (HP siswa/guru/petugas)
        
2. **Mengelola master data**:
    
    - Data siswa, guru, kelas, mapel
        
    - Jadwal pelajaran
        
    - Zona lokasi (geofence) sekolah/PKL
        
    - Perangkat (device) RFID/kamera
        
3. **Mengelola user dan otentikasi**:
    
    - Admin sekolah, operator, guru, siswa, orang tua
        
    - Login dengan username/password
        
    - Penggunaan JWT untuk API
        
4. **Menyimpan dan mengolah data presensi**:
    
    - Jam masuk/keluar
        
    - Keterangan (on time, terlambat, izin, sakit, alfa)
        
    - Metode presensi (RFID/FACE/GEO)
        
5. **Menyediakan laporan dan dashboard**:
    
    - Rekap harian/mingguan/bulanan
        
    - Rekap per kelas, per siswa, per guru
        
    - Statistik keterlambatan dan ketidakhadiran
        
6. **Berkomunikasi dengan sistem lain**:
    
    - Integrasi ke SIM akademik atau sistem nilai
        
    - Ekspor data (Excel, CSV, PDF)
        

### 3.2.2 Kebutuhan Non-Fungsional (Non-Functional Requirements)

Di luar “fitur”, ada kebutuhan non-fungsional yang mempengaruhi arsitektur:

1. **Keandalan (Reliability)**  
    Sistem harus stabil, terutama saat jam masuk sekolah ketika banyak siswa melakukan presensi hampir bersamaan.
    
2. **Kinerja (Performance)**
    
    - Harus mampu memproses scan RFID dengan sangat cepat.
        
    - Latensi face recognition harus tetap wajar.
        
3. **Keamanan (Security)**
    
    - Data siswa adalah data sensitif.
        
    - Endpoint API perlu dilindungi (JWT, role-based access).
        
    - Komunikasi harus menggunakan HTTPS jika diakses dari luar LAN.
        
4. **Kemudahan Pengembangan & Pemeliharaan (Maintainability)**
    
    - Kode harus modular dan terstruktur (Controller–Service–Repository).
        
    - Mudah menambah fitur baru (misalnya presensi fingerprint di masa depan).
        
5. **Skalabilitas (Scalability)**
    
    - Di awal mungkin cukup satu server.
        
    - Ke depan bisa dipisah menjadi beberapa service (modular monolith → microservice).
        
6. **Keterbatasan Lingkungan Sekolah**
    
    - Jaringan LAN kadang tidak sempurna.
        
    - Terkadang sekolah belum memiliki banyak server.
        
    - Sumber daya teknis (admin IT) terbatas.
        

Dengan konteks ini, pilihan arsitektur harus **realistis** untuk lingkungan SMK/ sekolah, namun tetap mengikuti praktik baik industri.

---

## 3.3 Pemilihan Gaya Arsitektur: Monolit Modular vs Microservices

Secara umum, ada dua pendekatan arsitektur backend yang sering dibahas:

1. **Monolithic Application**
    
2. **Microservices Architecture**
    

### 3.3.1 Microservices (Sekilas)

Microservices adalah pendekatan di mana aplikasi dibagi menjadi banyak service kecil yang:

- Berjalan sendiri-sendiri (proses terpisah)
    
- Berkomunikasi via HTTP/REST atau messaging
    
- Dapat di-deploy dan diskalakan independen
    

Contoh:

- Service Attendance
    
- Service Face Recognition
    
- Service Master Data
    
- Service Auth
    

Pendekatan ini sangat kuat, tetapi:

- Kompleks dari sisi deployment
    
- Butuh monitoring dan orkestrasi (Docker, Kubernetes)
    
- Kurang cocok untuk sekolah yang baru mulai dan tim kecil
    

### 3.3.2 Monolit Modular (Pilihan yang Direkomendasikan)

Untuk **SIM Presensi Sekolah**, pendekatan yang lebih cocok adalah:

> **Monolith Modular**: satu aplikasi Spring Boot, tapi di dalamnya modul-modul dipisahkan dengan jelas.

Artinya:

- Secara deployment: 1 file JAR/1 container → mudah di-manage oleh tim sekolah.
    
- Secara kode: dipisah ke dalam paket (package) yang rapi:
    
    - `auth`
        
    - `masterdata`
        
    - `attendance`
        
    - `device`
        
    - `face`
        
    - `geo`
        
    - `reporting`
        

Struktur modul ini akan sangat membantu jika suatu hari ingin di-_split_ menjadi microservice.

---

## 3.4 Layered Architecture untuk SIM Presensi

Setelah memilih monolith modular, kita perlu mendesain **layer** di dalam aplikasi.

Secara umum, kita gunakan arsitektur berlapis:

1. **Presentation / API Layer (Controller)**
    
2. **Application / Service Layer**
    
3. **Domain / Model Layer**
    
4. **Infrastructure / Persistence Layer (Repository & DB)**
    

Secara tekstual:

```text
[ Device Layer (RFID, Kamera, Mobile) ]
                |
                v
[ API Layer (Controller / REST) ]
                |
                v
[ Service Layer (Business Logic) ]
                |
                v
[ Repository / Persistence Layer ]
                |
                v
[ Database & External Services ]
```

Mari kita bahas satu per satu.

---

### 3.4.1 Device Layer (Client / External)

**Device layer** berada di luar Spring Boot tetapi ikut dalam arsitektur sistem. Isinya:

- RFID reader (mini computer, Arduino/ESP32 + modul RFID + koneksi HTTP/MQTT)
    
- Kamera + face recognition client
    
- HP Android/iOS untuk geolokasi
    
- Browser (web dashboard guru/admin)
    

Mereka mengirim HTTP request ke backend, dan backend merespons dalam format JSON.

---

### 3.4.2 API / Presentation Layer (Controller)

Layer ini adalah pintu masuk ke Spring Boot:

- Berisi class beranotasi `@RestController`
    
- Menangani HTTP method (GET, POST, PUT, DELETE)
    
- Menggunakan DTO (Data Transfer Object) untuk request/response
    

Contoh modul:

- `AttendanceController`
    
- `AuthController`
    
- `StudentController`
    
- `ReportController`
    

Tugasnya:

- Validasi input dasar (misalnya dengan `@Valid`)
    
- Memanggil service yang sesuai
    
- Mengubah hasil service menjadi response JSON
    

Controller **tidak** menyimpan logika bisnis yang rumit; logika dipindahkan ke layer Service.

---

### 3.4.3 Service / Application Layer

Inilah **jantung logika bisnis**.

Tanggung jawab:

- Memproses aturan presensi
    
- Mengelola transaksi data (save, update, delete)
    
- Berkoordinasi dengan beberapa repository sekaligus
    
- Berkomunikasi dengan service eksternal (face recognition service, misalnya)
    

Contoh:

- `RfidAttendanceService` → logika presensi via kartu
    
- `FaceAttendanceService` → logika presensi via wajah
    
- `GeoAttendanceService` → logika presensi via geolokasi
    
- `ScheduleService` → menentukan status on time / late
    

Service akan menggunakan entity dari domain layer dan memanggil repository dari persistence layer.

---

### 3.4.4 Domain / Model Layer

Layer ini berisi definisi:

- Entity JPA (`@Entity`)
    
- Enum (misalnya `AttendanceStatus`, `AttendanceMethod`)
    
- Value Object jika diperlukan (misalnya `GeoPoint`)
    

Domain layer merepresentasikan **data dan aturan dasar** yang melekat pada data.

Contoh entity:

- `Student`
    
- `Teacher`
    
- `ClassRoom`
    
- `AttendanceRecord`
    
- `LocationZone`
    
- `Device`
    

Di sinilah struktur data sistem presensi “tinggal”.

---

### 3.4.5 Persistence / Infrastructure Layer

Layer ini menangani interaksi dengan:

- Database (PostgreSQL, MySQL, dll.)
    
- External service (via REST client)
    
- Message broker (kalau di masa depan pakai MQTT, RabbitMQ, dsb.)
    

Komponen utama di sini adalah:

- `Repository` (interface) yang extends `JpaRepository`
    
- `EntityManager` jika perlu query khusus
    
- Client HTTP (misalnya `WebClient` atau `RestTemplate`) untuk memanggil face recognition service
    

Peran layer ini adalah menyediakan operasi CRUD yang bersih untuk service layer.

---

## 3.5 Modul-Modul Utama di Dalam Arsitektur

Pada monolith modular, kita menyusun package berdasarkan **domain fungsional**, misalnya:

```text
com.sekolah.presensi
 ├─ auth
 ├─ masterdata
 │   ├─ student
 │   ├─ teacher
 │   ├─ classroom
 │   └─ subject
 ├─ schedule
 ├─ attendance
 ├─ device
 ├─ face
 ├─ geo
 ├─ reporting
 └─ common
```

Mari kita jelaskan fungsi masing-masing modul.

### 3.5.1 Modul Auth

Berisi:

- Entity `User`, `Role`
    
- Endpoint login (`/api/auth/login`)
    
- Penerbitan dan validasi JWT
    
- Konfigurasi Spring Security
    

Peran: mengamankan semua endpoint presensi, master data, dan reporting.

---

### 3.5.2 Modul Master Data

Terdiri atas sub modul:

- `student` → data siswa
    
- `teacher` → data guru
    
- `classroom` → kelas
    
- `subject` → mapel
    

Setiap modul memiliki:

- `Entity` → tabel di database
    
- `Repository` → akses data
    
- `Service` → logika sederhana
    
- `Controller` → API CRUD
    

Master data menjadi basis seluruh operasi presensi.

---

### 3.5.3 Modul Schedule (Jadwal)

Mengelola jadwal pelajaran:

- Jam mulai & selesai
    
- Guru
    
- Kelas
    
- Mapel
    

Modul ini dibutuhkan oleh modul attendance untuk menentukan:

- Apakah siswa datang tepat waktu?
    
- Apakah presensi masih dalam jam pelajaran yang sah?
    

---

### 3.5.4 Modul Device

Mengelola:

- Daftar device RFID
    
- Daftar kamera / terminal wajah
    
- Informasi device HP (jika perlu)
    

Setiap perangkat bisa memiliki:

- `deviceId`
    
- Tipe device (RFID, CAMERA, MOBILE)
    
- Lokasi fisik (misalnya: Gerbang Utama, Lab RPL 1, dsb.)
    

Backend memverifikasi `deviceId` ketika menerima request presensi dari RFID/kamera.

---

### 3.5.5 Modul Face

Modul ini fokus pada integrasi face recognition:

- Penyimpanan `FaceTemplate` (ID referensi ke face engine)
    
- Endpoint untuk **enrollment** wajah siswa/guru
    
- Integrasi dengan face recognition engine eksternal
    

Modul face **tidak harus** melakukan deteksi wajah di Spring Boot; biasanya:

- Spring Boot hanya mengirim foto/embedding ke service AI eksternal
    
- Service AI yang melakukan komputasi berat (deep learning)
    
- Hasilnya (ID siswa/guru + confidence) dikembalikan ke backend
    

---

### 3.5.6 Modul Geo (Geolokasi)

Mengelola:

- `LocationZone` (geofence sekolah dan lokasi PKL)
    
- Validasi jarak HP pengguna terhadap zona
    
- Logika khusus presensi saat di dalam/outside geofence
    

Modul ini digunakan besar-besaran untuk:

- Presensi guru via HP ketika masuk area sekolah
    
- Presensi siswa saat PKL
    

---

### 3.5.7 Modul Attendance

Modul sentral untuk:

- Mencatat presensi harian
    
- Menandai check-in, check-out
    
- Menghitung status hadir/on time/late
    

Menggunakan kombinasi:

- Data dari masterdata (siswa/guru)
    
- Jadwal dari modul schedule
    
- Zona dari modul geo
    
- Hasil identifikasi dari modul face
    

Di modul inilah “aturan presensi” diformalisasikan.

---

### 3.5.8 Modul Reporting

Menangani:

- Rekap presensi per siswa/per kelas/per guru
    
- Filter berdasarkan tanggal
    
- Export Excel/CSV/PDF
    
- Menyediakan API untuk dashboard frontend
    

---

## 3.6 Alur Request dari Device ke Backend

Setelah memahami layer dan modul, kini kita fokus ke **alur request**—bagaimana sebuah presensi terjadi dari perangkat hingga tersimpan di database.

Kita bahas tiga skenario utama:

1. Alur presensi RFID
    
2. Alur presensi Face Recognition
    
3. Alur presensi Geolokasi
    

---

### 3.6.1 Alur Presensi Menggunakan RFID

**Skenario**: Siswa datang ke sekolah, menempelkan kartu RFID ke reader di gerbang.

**Langkah-langkah:**

1. **Siswa menempelkan kartu ke RFID reader**
    
    - Modul RFID reader membaca UID kartu.
        
    - Device memiliki konfigurasi `deviceId` (misal: GATE-1).
        
2. **Device mengirim data ke backend**
    
    Request HTTP ke backend, misalnya:
    
    - Method: `POST`
        
    - URL: `/api/attendance/rfid`
        
    - Body JSON:
        
        ```json
        {
          "deviceId": "GATE-1",
          "rfidTag": "04A1B2C3D4",
          "timestamp": "2025-11-17T06:58:23"
        }
        ```
        
3. **API Layer – `AttendanceController`**
    
    - Endpoint `/rfid` menerima request.
        
    - Melakukan validasi (`@Valid`) pada DTO `RfidAttendanceRequest`.
        
    - Memanggil `rfidAttendanceService.handleRfidAttendance(request)`.
        
4. **Service Layer – `RfidAttendanceService`**
    
    Di sinilah logika presensi berjalan:
    
    - Mencari siswa berdasarkan `rfidTag` melalui `StudentRepository`.
        
    - Mengecek jadwal hari ini melalui `ScheduleService`.
        
    - Menentukan status (`ON_TIME` / `LATE`) berdasarkan jam.
        
    - Mencari apakah sudah ada `AttendanceRecord` untuk siswa di tanggal tersebut.
        
    - Jika belum: buat record baru → set `checkInTime`.
        
    - Jika sudah ada dan `checkInTime` sudah terisi: anggap sebagai `checkOutTime`.
        
5. **Persistence Layer – `AttendanceRecordRepository`**
    
    - Memanggil `save(record)`.
        
    - Hibernate/JPA menerjemahkan ke SQL `INSERT` atau `UPDATE`.
        
    - Data tersimpan di tabel `attendance_records`.
        
6. **Response ke Device**
    
    - Service mengembalikan `AttendanceRecord`.
        
    - Controller mengubah ke `AttendanceRecordResponse` (DTO response).
        
    - Response JSON dikirim ke device:
        
        ```json
        {
          "studentName": "Budi",
          "className": "XI SIJA 2",
          "status": "ON_TIME",
          "method": "RFID",
          "checkInTime": "2025-11-17T06:58:23"
        }
        ```
        

Secara diagram teks:

```text
[RFID Card] 
    -> [RFID Reader (deviceId=GATE-1)]
        -> HTTP POST /api/attendance/rfid
            -> [AttendanceController]
                -> [RfidAttendanceService]
                    -> [StudentRepository]
                    -> [ScheduleService]
                    -> [AttendanceRecordRepository]
                        -> [Database]
                <- AttendanceRecord
            <- JSON Response
        <- Tampilkan status di layar device (opsional)
```

---

### 3.6.2 Alur Presensi Menggunakan Face Recognition

**Skenario**: Kamera terpasang di pintu kelas. Siswa lewat, wajahnya terekam dan diverifikasi.

Terdapat dua model arsitektur:

1. **Face engine di client** (kamera/mini PC)
    
2. **Face engine di server terpisah**
    

Di sini kita ambil skenario ke-2: face recognition dijalankan di service terpisah.

**Langkah-langkah:**

1. **Kamera menangkap gambar wajah**
    
    - Client (misalnya aplikasi Python di mini PC) menangkap frame wajah.
        
    - Mengirim data (bisa berupa gambar atau face embedding) ke backend.
        
2. **Client mengirim request ke backend**
    
    - Method: `POST`
        
    - URL: `/api/attendance/face`
        
    - Body JSON:
        
        ```json
        {
          "deviceId": "CLASS-11-RPL-1-CAM",
          "faceToken": "abc123def456",
          "timestamp": "2025-11-17T07:00:00"
        }
        ```
        
    
    `faceToken` adalah representasi wajah (id dari engine atau embedding yang sudah dikompres).
    
3. **API Layer – `AttendanceController`**
    
    - Endpoint `/face` menerima request.
        
    - Memvalidasi input.
        
    - Memanggil `faceAttendanceService.handleFaceAttendance(request)`.
        
4. **Service Layer – `FaceAttendanceService`**
    
    Langkah di service:
    
    - Memanggil `faceRecognitionClient.verify(faceToken)`  
        → ini adalah HTTP client yang menghubungi face recognition engine (bukan ke database).
        
    - Engine mengembalikan:
        
        ```json
        {
          "matched": true,
          "studentId": 123,
          "confidence": 0.93
        }
        ```
        
    - Jika `matched == false` atau `confidence` < ambang batas → presensi ditolak.
        
    - Jika cocok: ambil `Student` dari `StudentRepository`.
        
    - Langkah selanjutnya mirip dengan RFID:
        
        - Cek jadwal.
            
        - Cek record attendance hari ini.
            
        - Set `checkInTime` atau `checkOutTime`.
            
        - Set `method = FACE`.
            
5. **Persistence Layer**
    
    - `AttendanceRecordRepository.save(record)` → data tersimpan di database.
        
6. **Response ke client**
    
    - Controller mengembalikan data presensi sebagai JSON.
        

Diagram teks:

```text
[Camera] 
    -> [Face Client (mini PC)]
        -> HTTP POST /api/attendance/face
            -> [AttendanceController]
                -> [FaceAttendanceService]
                    -> [FaceRecognitionClient]
                        -> [Face Engine Service]
                        <- Match Result (studentId, confidence)
                    -> [StudentRepository]
                    -> [ScheduleService]
                    -> [AttendanceRecordRepository]
                        -> [Database]
                <- AttendanceRecord
            <- JSON Response
        <- Tampilkan status di layar / log
```

---

### 3.6.3 Alur Presensi Menggunakan Geolokasi (HP)

**Skenario**: Guru membuka aplikasi mobile, menekan tombol “Presensi Masuk” ketika tiba di sekolah.

**Langkah-langkah:**

1. **Aplikasi mobile mendapatkan lokasi GPS**
    
    - HP mengambil `lat`, `lng` dari GPS.
        
    - User sudah login, sehingga aplikasi mengetahui `userId`.
        
2. **Mobile app mengirim request ke backend**
    
    - Method: `POST`
        
    - URL: `/api/attendance/geo`
        
    - Header: `Authorization: Bearer <JWT>`
        
    - Body:
        
        ```json
        {
          "userId": 555,
          "lat": -7.475123,
          "lng": 109.567890,
          "timestamp": "2025-11-17T06:45:10",
          "deviceInfo": "Android-12-POCO-X3"
        }
        ```
        
3. **API Layer – `AttendanceController`**
    
    - Endpoint `/geo` menerima request.
        
    - Spring Security memverifikasi JWT terlebih dahulu.
        
    - Jika token valid: request diteruskan ke controller.
        
    - Controller memanggil `geoAttendanceService.handleGeoAttendance(request)`.
        
4. **Service Layer – `GeoAttendanceService`**
    
    Langkah di service:
    
    - Ambil `User` dari `UserRepository`.
        
    - Mapping ke `Student` atau `Teacher` (tergantung role).
        
    - Ambil `LocationZone` yang aktif (misalnya “Zona Sekolah Utama”).
        
    - Hitung jarak antara (`lat`, `lng` HP) dengan (`lat`, `lng` zona) menggunakan rumus Haversine.
        
    - Jika `distance <= radiusZona`: presensi valid.
        
    - Cek `AttendanceRecord` untuk hari tersebut.
        
    - Tentukan `checkInTime`/`checkOutTime` dan status ON_TIME/LATE berdasarkan jadwal.
        
5. **Persistence Layer**
    
    - Simpan record ke database.
        
6. **Response ke aplikasi mobile**
    
    - Aplikasi bisa menampilkan status: “Presensi Berhasil – ON TIME”
        
    - Jika di luar zona: “Gagal, Anda di luar area presensi yang diizinkan.”
        

Diagram teks:

```text
[HP Guru/Siswa]
    -> HTTP POST /api/attendance/geo (JWT)
        -> [Spring Security] (cek token)
        -> [AttendanceController]
            -> [GeoAttendanceService]
                -> [UserRepository]
                -> [Student/TeacherRepository]
                -> [LocationZoneRepository]
                -> [ScheduleService]
                -> [AttendanceRecordRepository]
                    -> [Database]
            <- AttendanceRecord
        <- JSON Response
    <- Tampilkan hasil di aplikasi mobile
```

---

### 3.6.4 Alur Akses Dashboard Web

Selain presensi, admin/guru juga ingin melihat laporan presensi.

**Alurnya:**

1. Admin login via web frontend.
    
2. Frontend menyimpan JWT di browser (misal localStorage).
    
3. Frontend memanggil endpoint:
    
    - `GET /api/report/attendance?date=2025-11-17&classId=10`
        
4. Request masuk ke:
    
    - `ReportController` → `ReportService` → `AttendanceRecordRepository`
        
5. Hasilnya berupa:
    
    - List siswa + status hadir/terlambat/izin/alfa
        
6. Frontend menampilkan dalam bentuk tabel/grafik.
    

Diagram ringkas:

```text
[Browser Admin]
    -> HTTP GET /api/report/attendance (JWT)
        -> [Spring Security]
        -> [ReportController]
            -> [ReportService]
                -> [AttendanceRecordRepository]
                -> [StudentRepository]
                -> [ClassRoomRepository]
                    -> [Database]
            <- Rekap Presensi
        <- JSON Response
    <- Tampilkan di dashboard
```

---

## 3.7 Diagram Layered Architecture (Versi Teks yang Mudah Digambar)

Berikut contoh diagram arsitektur sistem presensi versi teks, yang bisa diubah menjadi diagram visual:

```text
+---------------------------------------------------------+
|                    Client / Device Layer                |
|---------------------------------------------------------|
| - RFID Reader (Gerbang, Ruang Lab)                     |
| - Camera + Face Client                                 |
| - Mobile App (Android/iOS)                             |
| - Web Browser (Admin, Guru, Wali Kelas, Siswa)         |
+---------------------------|-----------------------------+
                            |
                            v
+---------------------------------------------------------+
|                Spring Boot Backend (API Layer)          |
|---------------------------------------------------------|
|  @RestController (AuthController, AttendanceController, |
|                  StudentController, ReportController)   |
+---------------------------|-----------------------------+
                            |
                            v
+---------------------------------------------------------+
|              Application / Service Layer                |
|---------------------------------------------------------|
|  AuthService            | AttendanceService             |
|  RfidAttendanceService  | FaceAttendanceService         |
|  GeoAttendanceService   | ScheduleService               |
|  StudentService         | ReportService                 |
|  DeviceService          | FaceRecognitionClient         |
+---------------------------|-----------------------------+
                            |
                            v
+---------------------------------------------------------+
|                  Domain & Persistence Layer             |
|---------------------------------------------------------|
|  Entities (@Entity):                                    |
|   - Student, Teacher, ClassRoom, Subject, User, Role    |
|   - AttendanceRecord, Device, LocationZone, FaceTemplate|
|                                                         |
|  Repositories (@Repository):                            |
|   - StudentRepository                                   |
|   - AttendanceRecordRepository                          |
|   - LocationZoneRepository                              |
|   - UserRepository, RoleRepository                      |
+---------------------------|-----------------------------+
                            |
                            v
+---------------------------------------------------------+
|                Database & External Services             |
|---------------------------------------------------------|
|  - PostgreSQL / MySQL                                   |
|  - Face Recognition Service (Python, Cloud, dsb.)       |
|  - (Opsional) Message Broker (MQTT/RabbitMQ)            |
+---------------------------------------------------------+
```

---

## 3.8 Cross-Cutting Concerns: Security, Logging, Validasi, Transaksi

Selain modul dan alur bisnis, ada aspek “melintang” yang harus dipikirkan dalam arsitektur:

### 3.8.1 Security (Spring Security + JWT)

- Semua endpoint presensi dan master data harus dilindungi.
    
- Device seperti RFID reader bisa:
    
    - Memakai _device secret_
        
    - Atau token khusus yang dikonfigurasi di server.
        
- User (guru/admin/siswa) menggunakan JWT untuk akses API.
    

### 3.8.2 Validasi

- Menggunakan Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, dll.)
    
- Validasi dilakukan di DTO request.
    
- Memastikan data yang masuk ke service sudah bersih.
    

### 3.8.3 Transaksi (Transaction)

- Operasi yang menyentuh lebih dari satu tabel dibungkus dalam anotasi `@Transactional`.
    
- memastikan data konsisten (misalnya ketika insert attendance + update summary).
    

### 3.8.4 Logging & Audit

- Setiap presensi bisa disimpan log-nya:
    
    - DeviceId, waktu, IP, user agent.
        
- Logging untuk debugging (misalnya presensi gagal karena RFID tidak terdaftar).
    

---

## 3.9 Pertimbangan Deployment di Lingkungan Sekolah

Arsitektur juga harus mempertimbangkan kenyataan di lapangan:

1. **Server Sekolah (On-Premise)**
    
    - Spring Boot berjalan di server lokal (Ubuntu Server/Windows Server).
        
    - Database berada di LAN sekolah.
        
    - RFID reader dan kamera terhubung via LAN/WiFi.
        
2. **Akses dari Luar Sekolah**
    
    - Jika ingin orang tua mengakses dari luar:
        
        - Gunakan domain publik + SSL
            
        - Pertimbangkan reverse proxy (Nginx, Caddy)
            
    - Atau gunakan VPN antar sekolah dan server pusat.
        
3. **Mode Offline (Opsional)**
    
    - RFID reader bisa menyimpan buffer presensi sementara jika koneksi ke server putus, lalu sinkron ketika kembali online.
        
    - Mobile app bisa menahan request beberapa saat jika internet lambat.
        
4. **Backup & Redundansi**
    
    - Database presensi harus di-backup berkala.
        
    - Bisa menggunakan backup otomatis harian ke penyimpanan lain.
        

---

## 3.10 Penutup Bab 3

Di Bab 3 ini, kita telah membangun **gambaran arsitektur lengkap** untuk SIM Presensi Sekolah berbasis Spring Boot, dengan fokus pada:

- Pemilihan **monolith modular** yang realistis untuk sekolah namun tetap scalable.
    
- Penerapan **layered architecture** (Controller–Service–Repository–Database).
    
- Pembagian modul fungsional: auth, masterdata, schedule, attendance, device, face, geo, reporting.
    
- Penjelasan **alur request** dari device (RFID, kamera, mobile) menuju backend hingga database.
    
- Diagram arsitektur teks yang siap dipetakan menjadi diagram visual.
    
- Pertimbangan cross-cutting: security, logging, validasi, dan aspek deployment di lingkungan sekolah.
    



Oke, kita lanjut ke **Bab 4: Desain Database dan ERD SIM Presensi Sekolah**. Saya buat tetap nyambung dengan bab-bab sebelumnya dan dengan kode modul Attendance yang sudah kita susun.

---

# **BAB 4 – Desain Database dan ERD SIM Presensi Sekolah**

## 4.1 Pendahuluan

Setelah kita memahami:

- **Konsep SIM Presensi modern** (Bab 1),
    
- **Fondasi Java & Spring Boot** (Bab 2),
    
- **Desain arsitektur & alur sistem** (Bab 3),
    
- dan bahkan sudah membuat **modul Attendance** (bab praktis),
    

langkah berikutnya adalah merapikan dan memformalkan **desain basis data (database)**.

Database adalah tempat semua data disimpan, diorganisasi, dan dipertahankan konsistensinya. Desain database yang baik akan:

- Memudahkan implementasi di Spring Data JPA
    
- Mempercepat query untuk laporan
    
- Mengurangi duplikasi data
    
- Menjaga integritas data (foreign key, constraint)
    
- Memudahkan pengembangan di masa depan
    

Pada bab ini kita akan:

1. Menentukan **entitas utama** dalam SIM Presensi Sekolah.
    
2. Menjelaskan **tabel-tabel inti** beserta kolom, tipe data, dan kunci (PK/FK).
    
3. Menjelaskan **relasi antar tabel** dalam bentuk ERD (Entity Relationship Diagram) versi teks.
    
4. Memberikan contoh **DDL (SQL)** yang bisa langsung dijalankan di PostgreSQL/MySQL.
    
5. Mengaitkan desain database dengan **entity di Spring Boot** yang sudah kita buat (Student, AttendanceRecord, LocationZone, dll).
    

---

## 4.2 Prinsip Desain Database yang Digunakan

Sebelum masuk ke tabel, kita sepakati beberapa prinsip desain:

1. **Relational Database**
    
    - Menggunakan PostgreSQL atau MySQL.
        
    - Data disusun dalam tabel dengan relasi PK–FK.
        
2. **Normalisasi Ringan (hingga 3NF)**
    
    - Menghindari data yang berulang (redundan).
        
    - Contoh: nama kelas tidak ditulis di tabel presensi, tapi direlasikan lewat `student -> classroom`.
        
3. **Penamaan Konsisten & Jelas**
    
    - Nama tabel: jamak dan snake_case, misal: `students`, `attendance_records`.
        
    - Primary key: `id` (tipe `BIGINT`/`SERIAL`/`BIGSERIAL`).
        
    - Foreign key: `<nama_entity>_id`, misalnya `student_id`, `classroom_id`.
        
4. **Mendukung Kebutuhan Presensi Multi-Device**
    
    - RFID, Face, Geo → semua diakomodasi di tabel `attendance_records` + pendukung.
        
5. **Mudah Dipetakan ke Entity JPA**
    
    - Setiap tabel utama memiliki entity di Spring Boot.
        
    - Relasi 1–N dan N–1 menggunakan anotasi `@OneToMany`, `@ManyToOne`, `@OneToOne` di Java.
        

---

## 4.3 Identifikasi Entitas Utama

Berdasarkan analisis di bab-bab sebelumnya, kita punya entitas utama:

1. **User & Role**
    
    - `users`
        
    - `roles`
        
    - `user_roles` (jika menggunakan many-to-many)
        
2. **Master Data Akademik**
    
    - `students`
        
    - `teachers`
        
    - `classrooms`
        
    - `subjects`
        
3. **Jadwal & Kegiatan Belajar**
    
    - `schedules` (jadwal pelajaran / jam masuk)
        
4. **Presensi & Metode**
    
    - `attendance_records`
        
5. **Perangkat & Lokasi**
    
    - `devices` (RFID reader, kamera, terminal)
        
    - `location_zones` (geofence sekolah/PKL)
        
6. **Face Recognition**
    
    - `face_templates` (ID template wajah siswa/guru)
        

Di luar itu, bisa dibuat tabel pendukung lain seperti `schools`, `departments`, dll, tapi untuk fokus buku ini, kita batasi pada tabel yang langsung terkait dengan sistem presensi.

---

## 4.4 Desain Tabel Autentikasi: `users`, `roles`, `user_roles`

### 4.4.1 Tabel `roles`

Tabel ini menyimpan daftar peran (role):

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|name|VARCHAR(50)|Nama role (ADMIN, GURU, SISWA)|
|description|VARCHAR(255)|Deskripsi singkat (opsional)|

Contoh isi:

- ADMIN
    
- GURU
    
- SISWA
    
- ORANG_TUA
    

### 4.4.2 Tabel `users`

Tabel ini menyimpan akun login sistem.

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|username|VARCHAR(50)|Unik, digunakan untuk login|
|password|VARCHAR(255)|Password yang sudah di-_hash_|
|full_name|VARCHAR(100)|Nama lengkap|
|email|VARCHAR(100)|Email (opsional)|
|phone|VARCHAR(20)|Nomor HP (opsional)|
|enabled|BOOLEAN|Apakah akun aktif|
|created_at|TIMESTAMP|Waktu pembuatan akun|
|updated_at|TIMESTAMP|Waktu update terakhir|

### 4.4.3 Tabel `user_roles` (jika Many-to-Many)

Menghubungkan `users` dengan `roles`.

|Kolom|Tipe|Keterangan|
|---|---|---|
|user_id|BIGINT (FK)|Mengacu ke `users.id`|
|role_id|BIGINT (FK)|Mengacu ke `roles.id`|

PK bisa **(user_id, role_id)**.

---

## 4.5 Tabel Master Data: `students`, `teachers`, `classrooms`, `subjects`

### 4.5.1 Tabel `classrooms`

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|name|VARCHAR(50)|Nama kelas: “XI SIJA 2”|
|level|VARCHAR(10)|Tingkat: X, XI, XII|
|major|VARCHAR(50)|Jurusan: “SIJA”, “TKJ”, dll|
|active|BOOLEAN|Kelas masih aktif atau tidak|

Relasi:

- Satu `classroom` punya banyak `students`. (1–N)
    

### 4.5.2 Tabel `students`

Kita sesuaikan dengan entity `Student` yang sudah dipakai di modul Attendance.

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|user_id|BIGINT (FK)|Relasi ke `users.id` (akun login siswa)|
|nis|VARCHAR(20)|Nomor Induk Siswa, unik|
|name|VARCHAR(100)|Nama siswa|
|classroom_id|BIGINT (FK)|Relasi ke `classrooms.id`|
|rfid_tag|VARCHAR(50)|UID kartu RFID (boleh null)|
|active|BOOLEAN|Siswa aktif/tidak|
|created_at|TIMESTAMP|Waktu input|
|updated_at|TIMESTAMP|Waktu update|

Relasi:

- `students.classroom_id` → FK ke `classrooms.id`
    
- `students.user_id` → FK ke `users.id`
    

Di JPA sudah kita representasikan kurang lebih seperti:

```java
@ManyToOne
@JoinColumn(name = "classroom_id")
private ClassRoom classRoom;

@OneToOne
@JoinColumn(name = "user_id")
private User user;
```

_(kode ini bisa disesuaikan di bab implementasi master data)_

### 4.5.3 Tabel `teachers`

Struktur mirip `students`, tapi khusus guru:

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|user_id|BIGINT (FK)|Relasi ke `users.id` (akun login guru)|
|nip|VARCHAR(30)|Nomor Induk Pegawai (jika ada)|
|name|VARCHAR(100)|Nama guru|
|phone|VARCHAR(20)|Nomor HP (opsional)|
|email|VARCHAR(100)|Email (opsional)|
|rfid_tag|VARCHAR(50)|UID kartu RFID guru (opsional)|
|active|BOOLEAN|Guru masih aktif atau tidak|

### 4.5.4 Tabel `subjects`

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|code|VARCHAR(20)|Kode mapel: “SKJ-11”, “MTK-10”|
|name|VARCHAR(100)|Nama mapel|
|description|VARCHAR(255)|Deskripsi (opsional)|

---

## 4.6 Tabel Jadwal: `schedules`

Tabel `schedules` memuat informasi jadwal pelajaran atau jam masuk.

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|classroom_id|BIGINT (FK)|Kelas yang menerima pelajaran|
|subject_id|BIGINT (FK)|Mapel|
|teacher_id|BIGINT (FK)|Guru pengampu|
|day_of_week|SMALLINT|1=Senin, 2=Selasa, ..., 7=Minggu|
|start_time|TIME|Jam mulai, misalnya 07:00|
|end_time|TIME|Jam selesai|
|description|VARCHAR(255)|Catatan (opsional)|

Relasi:

- `classroom_id` → `classrooms.id`
    
- `subject_id` → `subjects.id`
    
- `teacher_id` → `teachers.id`
    

Di bab modul Attendance, `ScheduleService` bisa menggunakan tabel ini untuk menentukan ON_TIME/LATE berdasarkan jadwal yang berlaku.

---

## 4.7 Tabel Perangkat: `devices`

Tabel ini menyimpan data perangkat yang berhubungan dengan presensi:

- RFID reader di gerbang
    
- Terminal wajah di pintu kelas
    
- Perangkat absen lain
    

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|device_id|VARCHAR(50)|ID unik digunakan di request (misal “GATE-1”)|
|name|VARCHAR(100)|Nama, misal: “Gerbang Utama”|
|type|VARCHAR(20)|“RFID”, “CAMERA”, “OTHER”|
|location|VARCHAR(100)|Lokasi fisik (Gerbang, Lab RPL 1)|
|active|BOOLEAN|Perangkat aktif/tidak|
|secret_key|VARCHAR(100)|Token rahasia (opsional, untuk otentikasi device)|

Relasi dengan `attendance_records` bersifat **tidak wajib** (deviceId cukup disimpan sebagai string), tetapi sebagai improvement, kita bisa tambahkan FK:

- `attendance_records.device_id` → `devices.device_id` (atau `devices.id`).
    

---

## 4.8 Tabel Geolokasi: `location_zones`

Tabel **geofence** untuk area presensi:

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|name|VARCHAR(100)|Nama zona: “SMKN 1 Punggelan”, “PKL Toko A”|
|lat|DOUBLE|Titik latitude pusat zona|
|lng|DOUBLE|Titik longitude|
|radius_meter|DOUBLE|Radius (meter)|
|active|BOOLEAN|Apakah zona sedang aktif|

Dalam implementasi sederhana, kita bisa menggunakan satu zona aktif (`findFirstByActiveTrue`). Untuk versi lanjutan, kita bisa menerapkan:

- Zona berdasarkan kelas/jurusan
    
- Zona berdasarkan jenis kegiatan (PKL, upacara, dll.)
    

---

## 4.9 Tabel Face Recognition: `face_templates`

Tabel ini menyimpan _referensi_ template wajah (bukan gambar mentah).

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|student_id|BIGINT (FK)|Relasi ke `students.id`|
|external_id|VARCHAR(100)|ID di engine face recognition (jika dipisah)|
|created_at|TIMESTAMP|Waktu pendaftaran wajah (enrollment)|

Relasi:

- Satu siswa bisa memiliki satu `face_templates` (One-to-One), atau lebih dari satu jika versi history diizinkan.  
    Di entity, misalnya:
    

```java
@OneToOne
@JoinColumn(name = "student_id")
private Student student;
```

---

## 4.10 Tabel Inti Presensi: `attendance_records`

Ini sudah kita gunakan di modul Attendance, sekarang kita formal-kan sebagai desain database.

|Kolom|Tipe|Keterangan|
|---|---|---|
|id|BIGINT (PK)|Primary key|
|student_id|BIGINT (FK)|Relasi ke `students.id`|
|attendance_date|DATE|Tanggal presensi (tanpa jam)|
|check_in_time|TIMESTAMP|Waktu check-in (boleh null)|
|check_out_time|TIMESTAMP|Waktu check-out (boleh null)|
|method|VARCHAR(10)|“RFID”, “FACE”, “GEO”|
|status|VARCHAR(10)|“ON_TIME”, “LATE”, “ABSENT”, “UNKNOWN”|
|device_id|VARCHAR(50)|ID perangkat (string, bisa di-relasikan ke `devices`)|
|lat|DOUBLE|Lokasi check-in/out (jika via geo)|
|lng|DOUBLE||
|note|VARCHAR(255)|Catatan (opsional)|

Index penting:

- Index gabungan `(student_id, attendance_date)`  
    → mempercepat cek apakah siswa sudah punya presensi hari itu.
    

Inilah yang persis kita pakai di entity:

```java
@Table(
    name = "attendance_records",
    indexes = @Index(name = "idx_att_student_date", columnList = "student_id, attendance_date")
)
```

---

## 4.11 ERD (Entity Relationship Diagram) – Versi Teks

Berikut gambaran ERD dalam bentuk teks (bisa Anda ubah ke diagram grafis):

```text
+----------------+            +----------------+
|     roles      |            |     users      |
+----------------+            +----------------+
| id (PK)        |<--------+  | id (PK)        |
| name           |         |  | username      |
+----------------+         |  | password      |
                           |  +----------------+
                           |
                           |   +-------------------+
                           +---|   user_roles      |
                               +-------------------+
                               | user_id (FK)      |
                               | role_id (FK)      |
                               +-------------------+


+----------------+      1    N  +----------------+
|  classrooms    |--------------|   students     |
+----------------+              +----------------+
| id (PK)        |              | id (PK)        |
| name           |              | user_id (FK)   -> users.id
| level          |              | classroom_id(FK)-> classrooms.id
| major          |              | nis (UNIQUE)   |
+----------------+              | name           |
                                | rfid_tag       |
                                +----------------+


+----------------+      1    N  +----------------+
|   teachers     |--------------|   schedules    |
+----------------+              +----------------+
| id (PK)        |              | id (PK)        |
| user_id (FK)   |              | classroom_idFK |
| nip            |              | subject_id FK  |
| name           |              | teacher_id FK  |
+----------------+              | day_of_week    |
                                | start_time     |
                                | end_time       |
                                +----------------+


+----------------+      1    N  +------------------------+
|   students     |--------------|   attendance_records   |
+----------------+              +------------------------+
| id (PK)        |              | id (PK)                |
| ...            |              | student_id (FK)        |
+----------------+              | attendance_date        |
                                | check_in_time          |
                                | check_out_time         |
                                | method                 |
                                | status                 |
                                | device_id              |
                                | lat, lng               |
                                | note                   |
                                +------------------------+


+----------------+      1    1  +-----------------+
|   students     |--------------|  face_templates |
+----------------+              +-----------------+
| id (PK)        |              | id (PK)         |
| ...            |              | student_id (FK) |
+----------------+              | external_id     |
                                +-----------------+


+----------------+      1    N  +----------------+
| location_zones |--------------| geo attendance*|
+----------------+              +----------------+
| id (PK)        |              | (implisit via  |
| name           |              |  attendance_records |
| lat, lng       |              +----------------+
| radius_meter   |
+----------------+


+----------------+
|    devices     |
+----------------+
| id (PK)        |
| device_id      |
| type           |
| location       |
+----------------+
   (bisa di-relasikan secara opsional dengan attendance_records.device_id)
```

_(Diagram di atas adalah representasi konseptual; beberapa relasi bisa diperdalam di bab lanjutan.)_

---

## 4.12 Contoh DDL SQL untuk Implementasi Awal

Berikut contoh DDL (untuk PostgreSQL) yang bisa dijadikan starting point. (Bisa disesuaikan dengan engine yang Anda pakai.)

> **Catatan:** Di buku, Anda bisa potong menjadi snippet terpisah untuk tiap tabel agar mudah dipahami siswa.

### 4.12.1 Tabel Roles & Users

```sql
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(20),
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
```

### 4.12.2 Tabel Classrooms & Students

```sql
CREATE TABLE classrooms (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(50)  NOT NULL,
    level   VARCHAR(10),
    major   VARCHAR(50),
    active  BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE students (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT REFERENCES users(id),
    nis           VARCHAR(20) NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    classroom_id  BIGINT REFERENCES classrooms(id),
    rfid_tag      VARCHAR(50) UNIQUE,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP
);
```

### 4.12.3 Tabel Teachers, Subjects, Schedules

```sql
CREATE TABLE teachers (
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT REFERENCES users(id),
    nip       VARCHAR(30),
    name      VARCHAR(100) NOT NULL,
    phone     VARCHAR(20),
    email     VARCHAR(100),
    rfid_tag  VARCHAR(50) UNIQUE,
    active    BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE subjects (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE schedules (
    id            BIGSERIAL PRIMARY KEY,
    classroom_id  BIGINT NOT NULL REFERENCES classrooms(id),
    subject_id    BIGINT NOT NULL REFERENCES subjects(id),
    teacher_id    BIGINT NOT NULL REFERENCES teachers(id),
    day_of_week   SMALLINT NOT NULL,
    start_time    TIME     NOT NULL,
    end_time      TIME     NOT NULL,
    description   VARCHAR(255)
);
```

### 4.12.4 Tabel Devices & Location Zones

```sql
CREATE TABLE devices (
    id          BIGSERIAL PRIMARY KEY,
    device_id   VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(100),
    type        VARCHAR(20),
    location    VARCHAR(100),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    secret_key  VARCHAR(100)
);

CREATE TABLE location_zones (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    lat          DOUBLE PRECISION NOT NULL,
    lng          DOUBLE PRECISION NOT NULL,
    radius_meter DOUBLE PRECISION NOT NULL,
    active       BOOLEAN NOT NULL DEFAULT TRUE
);
```

### 4.12.5 Tabel Face Templates

```sql
CREATE TABLE face_templates (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT NOT NULL UNIQUE REFERENCES students(id) ON DELETE CASCADE,
    external_id VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 4.12.6 Tabel Attendance Records

```sql
CREATE TABLE attendance_records (
    id               BIGSERIAL PRIMARY KEY,
    student_id       BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    attendance_date  DATE   NOT NULL,
    check_in_time    TIMESTAMP,
    check_out_time   TIMESTAMP,
    method           VARCHAR(10),
    status           VARCHAR(10),
    device_id        VARCHAR(50),
    lat              DOUBLE PRECISION,
    lng              DOUBLE PRECISION,
    note             VARCHAR(255),
    CONSTRAINT uq_att_student_date UNIQUE (student_id, attendance_date)
);

CREATE INDEX idx_att_student_date
    ON attendance_records (student_id, attendance_date);
```

> **Catatan:** `UNIQUE (student_id, attendance_date)` memastikan satu siswa hanya punya satu record per hari (dengan check-in & check-out di field yang sama). Cocok dengan pola modul Attendance yang sudah kita implementasikan.

---

## 4.13 Menghubungkan ERD dengan Entity Spring Boot

Desain database di atas cukup mudah dipetakan ke entity JPA yang sudah kita gunakan. Misalnya:

- `students` → `Student` entity
    
- `attendance_records` → `AttendanceRecord`
    
- `location_zones` → `LocationZone`
    
- `face_templates` → `FaceTemplate`
    
- `users`, `roles`, `user_roles` → `User`, `Role`, dan relasi Many-to-Many
    

Dengan desain yang konsisten:

- Service di modul Attendance tidak perlu diubah banyak.
    
- Penambahan modul lain (misal reporting) tinggal memanfaatkan relasi yang sudah ada.
    
- Koneksi antar modul (auth, master data, presensi, geo, face) tetap rapi.
    

---

## 4.14 Penutup Bab 4

Pada bab ini kita telah:

- **Mengidentifikasi dan merinci** tabel-tabel utama untuk SIM Presensi Sekolah: `users`, `roles`, `students`, `teachers`, `classrooms`, `subjects`, `schedules`, `devices`, `location_zones`, `face_templates`, `attendance_records`.
    
- Menjelaskan **kolom, tipe data, dan relasi** di setiap tabel secara rinci.
    
- Menyusun **ERD versi teks** yang siap digambar ulang sebagai diagram visual.
    
- Menyediakan **contoh DDL SQL** yang bisa langsung digunakan sebagai fondasi database di PostgreSQL/MySQL.
    
- Menjaga agar desain database **selaras dengan entity dan service** Spring Boot yang sudah kita bangun di modul Attendance.
    

Dengan selesainya Bab 4, kita sudah punya:

> **Blueprint lengkap dari sisi data dan arsitektur** untuk SIM Presensi Sekolah.

Langkah logis berikutnya dalam buku ini bisa berupa:

- Bab implementasi **Master Data (Student, Teacher, Classroom)** dengan Spring Data JPA.
    
- Bab **Auth & Security (Spring Security + JWT)** yang menghubungkan tabel `users` dan `roles`.
    
- Bab lanjutan presensi (misalnya: perhitungan status hadir/terlambat lebih kompleks berdasarkan tabel `schedules`, atau modul laporan & dashboard).
    




Kita kerjain yang **Master Data Siswa & Guru** dulu ya — ini secara alur memang logis sebelum Security & JWT, karena `Student`, `Teacher`, `ClassRoom` dan kawan-kawan sudah dipakai oleh modul Attendance.

---

# **BAB – Modul Master Data Siswa & Guru (CRUD Lengkap + Contoh Endpoint)**

## 1. Tujuan Bab

Di bab ini kita akan membangun **modul Master Data** untuk:

- **Siswa (`Student`)**
    
- **Guru (`Teacher`)**
    
- (sekilas) **Kelas (`ClassRoom`)** sebagai relasi penting
    

Fokus:

1. Menyusun **entity JPA** untuk `Student`, `Teacher`, dan `ClassRoom` (diselaraskan dengan desain database di Bab 4).
    
2. Membuat **Repository** untuk akses data.
    
3. Membuat **DTO** (request & response) agar API rapi dan tidak langsung expose entity mentah.
    
4. Membuat **Service** dengan operasi CRUD.
    
5. Membuat **Controller (REST API)** untuk:
    
    - Menambah siswa/guru
        
    - Mengubah data
        
    - Menghapus (soft delete)
        
    - Menampilkan daftar & detail
        
6. Memberikan **contoh request/response** yang siap diuji dengan Postman/Insomnia.
    

Semua kode akan diposisikan dalam package, misalnya:

```text
com.sekolah.presensi.masterdata.student
com.sekolah.presensi.masterdata.teacher
com.sekolah.presensi.masterdata.classroom
```

Supaya rapi dan sejalan dengan arsitektur monolith modular yang sudah kita bahas.

---

## 2. Entity `ClassRoom` (Kelas)

Kelas adalah entitas penting karena setiap siswa akan selalu terkait ke satu kelas.

### 2.1 Entity `ClassRoom`

```java
package com.sekolah.presensi.masterdata.classroom;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contoh: "XI SIJA 2"
    @Column(nullable = false, length = 50)
    private String name;

    // Contoh: "X", "XI", "XII"
    @Column(length = 10)
    private String level;

    // Contoh: "SIJA", "TKJ", "RPL"
    @Column(length = 50)
    private String major;

    @Column(nullable = false)
    private Boolean active = true;

    // Tidak wajib, tapi bisa disediakan untuk navigasi dua arah
    @OneToMany(mappedBy = "classRoom")
    private List<Student> students;
}
```

> Catatan: `Student` nanti akan berada di package `student`, jadi perlu import `com.sekolah.presensi.masterdata.student.Student;` di atas.

### 2.2 Repository `ClassRoomRepository`

```java
package com.sekolah.presensi.masterdata.classroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

    List<ClassRoom> findAllByActiveTrue();

    boolean existsByName(String name);
}
```

---

## 3. Entity `Student` (Siswa)

`Student` akan digunakan oleh modul Attendance, Face, Reporting, dll.

### 3.1 Entity `Student`

```java
package com.sekolah.presensi.masterdata.student;

import com.sekolah.presensi.masterdata.classroom.ClassRoom;
import com.sekolah.presensi.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "students",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_nis", columnNames = "nis"),
        @UniqueConstraint(name = "uk_student_rfid_tag", columnNames = "rfid_tag")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi opsional ke User (akun login)
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 20)
    private String nis; // Nomor Induk Siswa

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private ClassRoom classRoom;

    // UID kartu RFID
    @Column(name = "rfid_tag", length = 50)
    private String rfidTag;

    @Column(nullable = false)
    private Boolean active = true;
}
```

> Untuk buku, Anda bisa jelaskan:
> 
> - `@UniqueConstraint` → mencegah NIS dan RFID ganda.
>     
> - `@ManyToOne` → banyak siswa bisa berada di satu kelas.
>     

### 3.2 Repository `StudentRepository`

```java
package com.sekolah.presensi.masterdata.student;

import com.sekolah.presensi.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByNis(String nis);

    Optional<Student> findByRfidTag(String rfidTag);

    Optional<Student> findByUser(User user);

    List<Student> findAllByClassRoom_Id(Long classroomId);

    List<Student> findAllByActiveTrue();
}
```

---

## 4. Entity `Teacher` (Guru)

### 4.1 Entity `Teacher`

```java
package com.sekolah.presensi.masterdata.teacher;

import com.sekolah.presensi.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "teachers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_teacher_nip", columnNames = "nip"),
        @UniqueConstraint(name = "uk_teacher_rfid_tag", columnNames = "rfid_tag")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi ke user untuk login
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 30)
    private String nip; // Bisa null jika guru honorer

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "rfid_tag", length = 50)
    private String rfidTag;

    @Column(nullable = false)
    private Boolean active = true;
}
```

### 4.2 Repository `TeacherRepository`

```java
package com.sekolah.presensi.masterdata.teacher;

import com.sekolah.presensi.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByNip(String nip);

    Optional<Teacher> findByRfidTag(String rfidTag);

    Optional<Teacher> findByUser(User user);

    List<Teacher> findAllByActiveTrue();
}
```

---

## 5. DTO untuk Student & Teacher

Agar API lebih bersih dan tidak langsung expose entity (yang sering membawa relasi penuh), kita pakai DTO.

### 5.1 DTO Student – Request & Response

#### 5.1.1 `StudentRequest`

```java
package com.sekolah.presensi.masterdata.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentRequest {

    private Long id; // untuk update, boleh null untuk create

    @NotBlank
    private String nis;

    @NotBlank
    private String name;

    @NotNull
    private Long classroomId;

    private String rfidTag;

    private Boolean active;
}
```

#### 5.1.2 `StudentResponse`

```java
package com.sekolah.presensi.masterdata.student.dto;

import com.sekolah.presensi.masterdata.student.Student;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponse {

    private Long id;
    private String nis;
    private String name;

    private Long classroomId;
    private String classroomName;
    private String classroomLevel;
    private String classroomMajor;

    private String rfidTag;

    private Boolean active;

    public static StudentResponse fromEntity(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .nis(s.getNis())
                .name(s.getName())
                .classroomId(s.getClassRoom() != null ? s.getClassRoom().getId() : null)
                .classroomName(s.getClassRoom() != null ? s.getClassRoom().getName() : null)
                .classroomLevel(s.getClassRoom() != null ? s.getClassRoom().getLevel() : null)
                .classroomMajor(s.getClassRoom() != null ? s.getClassRoom().getMajor() : null)
                .rfidTag(s.getRfidTag())
                .active(s.getActive())
                .build();
    }
}
```

---

### 5.2 DTO Teacher – Request & Response

#### 5.2.1 `TeacherRequest`

```java
package com.sekolah.presensi.masterdata.teacher.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherRequest {

    private Long id; // untuk update

    private String nip;

    @NotBlank
    private String name;

    private String phone;
    private String email;
    private String rfidTag;
    private Boolean active;
}
```

#### 5.2.2 `TeacherResponse`

```java
package com.sekolah.presensi.masterdata.teacher.dto;

import com.sekolah.presensi.masterdata.teacher.Teacher;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherResponse {

    private Long id;
    private String nip;
    private String name;
    private String phone;
    private String email;
    private String rfidTag;
    private Boolean active;

    public static TeacherResponse fromEntity(Teacher t) {
        return TeacherResponse.builder()
                .id(t.getId())
                .nip(t.getNip())
                .name(t.getName())
                .phone(t.getPhone())
                .email(t.getEmail())
                .rfidTag(t.getRfidTag())
                .active(t.getActive())
                .build();
    }
}
```

---

## 6. Service Layer: Logika CRUD Master Data

### 6.1 `StudentService`

```java
package com.sekolah.presensi.masterdata.student;

import com.sekolah.presensi.masterdata.classroom.ClassRoom;
import com.sekolah.presensi.masterdata.classroom.ClassRoomRepository;
import com.sekolah.presensi.masterdata.student.dto.StudentRequest;
import com.sekolah.presensi.masterdata.student.dto.StudentResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassRoomRepository classRoomRepository;

    @Transactional
    public StudentResponse create(StudentRequest request) {

        ClassRoom classRoom = classRoomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new EntityNotFoundException("ClassRoom tidak ditemukan"));

        Student student = Student.builder()
                .nis(request.getNis())
                .name(request.getName())
                .classRoom(classRoom)
                .rfidTag(request.getRfidTag())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        student = studentRepository.save(student);

        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student tidak ditemukan"));

        // Update data
        student.setNis(request.getNis());
        student.setName(request.getName());

        if (request.getClassroomId() != null) {
            ClassRoom classRoom = classRoomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new EntityNotFoundException("ClassRoom tidak ditemukan"));
            student.setClassRoom(classRoom);
        }

        student.setRfidTag(request.getRfidTag());
        if (request.getActive() != null) {
            student.setActive(request.getActive());
        }

        student = studentRepository.save(student);

        return StudentResponse.fromEntity(student);
    }

    public StudentResponse getById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student tidak ditemukan"));
        return StudentResponse.fromEntity(student);
    }

    public List<StudentResponse> getAllActive() {
        return studentRepository.findAllByActiveTrue()
                .stream()
                .map(StudentResponse::fromEntity)
                .toList();
    }

    public List<StudentResponse> getByClassRoom(Long classroomId) {
        return studentRepository.findAllByClassRoom_Id(classroomId)
                .stream()
                .map(StudentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void softDelete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student tidak ditemukan"));
        student.setActive(false);
        studentRepository.save(student);
    }
}
```

---

### 6.2 `TeacherService`

```java
package com.sekolah.presensi.masterdata.teacher;

import com.sekolah.presensi.masterdata.teacher.dto.TeacherRequest;
import com.sekolah.presensi.masterdata.teacher.dto.TeacherResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        Teacher teacher = Teacher.builder()
                .nip(request.getNip())
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .rfidTag(request.getRfidTag())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        teacher = teacherRepository.save(teacher);
        return TeacherResponse.fromEntity(teacher);
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Teacher tidak ditemukan"));

        teacher.setNip(request.getNip());
        teacher.setName(request.getName());
        teacher.setPhone(request.getPhone());
        teacher.setEmail(request.getEmail());
        teacher.setRfidTag(request.getRfidTag());
        if (request.getActive() != null) {
            teacher.setActive(request.getActive());
        }

        teacher = teacherRepository.save(teacher);
        return TeacherResponse.fromEntity(teacher);
    }

    public TeacherResponse getById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Teacher tidak ditemukan"));
        return TeacherResponse.fromEntity(teacher);
    }

    public List<TeacherResponse> getAllActive() {
        return teacherRepository.findAllByActiveTrue()
                .stream()
                .map(TeacherResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void softDelete(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Teacher tidak ditemukan"));
        teacher.setActive(false);
        teacherRepository.save(teacher);
    }
}
```

---

## 7. Controller: Endpoint REST untuk Siswa & Guru

### 7.1 `StudentController`

```java
package com.sekolah.presensi.masterdata.student;

import com.sekolah.presensi.masterdata.student.dto.StudentRequest;
import com.sekolah.presensi.masterdata.student.dto.StudentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllActive(
            @RequestParam(required = false) Long classroomId) {

        if (classroomId != null) {
            return ResponseEntity.ok(studentService.getByClassRoom(classroomId));
        }
        return ResponseEntity.ok(studentService.getAllActive());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        studentService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### 7.2 `TeacherController`

```java
package com.sekolah.presensi.masterdata.teacher;

import com.sekolah.presensi.masterdata.teacher.dto.TeacherRequest;
import com.sekolah.presensi.masterdata.teacher.dto.TeacherResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse response = teacherService.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TeacherRequest request) {
        TeacherResponse response = teacherService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAllActive() {
        return ResponseEntity.ok(teacherService.getAllActive());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        teacherService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 8. Contoh Penggunaan Endpoint (Untuk Praktikum)

### 8.1 Tambah Kelas

`POST /api/classrooms` (kalau nanti Anda buat ClassroomController)

```json
{
  "name": "XI SIJA 2",
  "level": "XI",
  "major": "SIJA"
}
```

### 8.2 Tambah Siswa

`POST /api/students`

```json
{
  "nis": "12345",
  "name": "Ahmad Fikri",
  "classroomId": 1,
  "rfidTag": "04A1B2C3D4",
  "active": true
}
```

### 8.3 Lihat Daftar Siswa Aktif

`GET /api/students`

Response contoh:

```json
[
  {
    "id": 1,
    "nis": "12345",
    "name": "Ahmad Fikri",
    "classroomId": 1,
    "classroomName": "XI SIJA 2",
    "classroomLevel": "XI",
    "classroomMajor": "SIJA",
    "rfidTag": "04A1B2C3D4",
    "active": true
  }
]
```

### 8.4 Tambah Guru

`POST /api/teachers`

```json
{
  "nip": "198308042022211006",
  "name": "IdiArso",
  "phone": "08123456789",
  "email": "idiarso@example.com",
  "rfidTag": "ABC123456",
  "active": true
}
```

---

## 9. Penutup Bab: Posisi Modul Master Data dalam Sistem

Dengan modul Master Data ini:

- **Modul Attendance** sekarang punya data nyata untuk dihubungkan:
    
    - `AttendanceRecord.student_id` → `students.id`
        
- **Modul Reporting** bisa menarik data siswa/guru/kelas dengan rapi.
    
- **Modul Security & JWT** nantinya akan menghubungkan `User` ↔ `Student` / `Teacher`.
    

Secara arsitektur:

- Master Data berada di **lapisan domain utama**.
    
- Attendance, Geo, Face, dan Reporting menggantung ke Master Data sebagai referensi.
    

---

Kita gaskeun Bab Security & JWT-nya ya. Fokusnya:

- Nyambung langsung dengan tabel `users`, `roles`, `user_roles` dari Bab 4
    
- Pakai Spring Security + JWT (stateless)
    
- Lindungi endpoint `/api/attendance/*`, `/api/students`, `/api/teachers`
    
- Perbaiki **Geo Attendance** supaya `userId` diambil dari JWT, bukan dari body
    

---

# **BAB – Security & JWT di SIM Presensi Sekolah**

## 1. Pendahuluan

Sampai di bab ini, sistem kita sudah punya:

- **Arsitektur backend** (Bab 3)
    
- **Modul Attendance** (RFID, Face, Geo)
    
- **Desain Database + ERD** (Bab 4)
    
- **Master Data Siswa & Guru** (CRUD, Bab sebelumnya)
    

Semua itu **belum aman** kalau:

- Endpoint bisa diakses bebas tanpa login
    
- Device dan user tidak diautentikasi
    
- `userId` dikirim dari client (mudah dimanipulasi)
    

Karena itu, kita butuh:

> **Lapisan keamanan** menggunakan **Spring Security + JWT (JSON Web Token)**

Bab ini akan membahas:

1. Konsep dasar **Spring Security** dan **JWT** (stateless auth).
    
2. Entity & repository untuk `User` dan `Role`.
    
3. Implementasi **UserDetailsService**, **PasswordEncoder**, dan **SecurityConfig**.
    
4. Implementasi **JWT util** + **JWT filter**.
    
5. Endpoint `/api/auth/login` untuk mendapatkan token.
    
6. Konfigurasi proteksi endpoint `/api/attendance/**`, `/api/students`, `/api/teachers`.
    
7. **Refactor Geo Attendance**: `userId` diambil dari token, bukan dari body.
    

---

## 2. Konsep Dasar Spring Security & JWT

### 2.1 Spring Security (Singkat)

Spring Security adalah framework security untuk aplikasi Java/Spring. Fitur utamanya:

- Autentikasi (verifikasi siapa pengguna)
    
- Otorisasi (apa yang boleh diakses)
    
- Filter chain pada HTTP request
    
- Integrasi dengan database, JWT, OAuth2, dll.
    

### 2.2 JWT – JSON Web Token

JWT adalah token berbentuk string yang:

- Dipakai untuk autentikasi stateless (server tidak simpan session di memori)
    
- Berisi informasi user (subject, roles, expired) dalam bentuk payload yang ditandatangani (signature)
    
- Dikirim di header `Authorization: Bearer <token>`
    

Alur sederhananya:

1. User login (username & password) ke `/api/auth/login`
    
2. Server verifikasi username & password
    
3. Server membuat JWT berisi `username` + `roles` + `expired`
    
4. Client menyimpan JWT (localStorage / memory)
    
5. Setiap request berikutnya: client kirim header `Authorization: Bearer <token>`
    
6. Server memverifikasi token lalu **mengenali user** dari token, tanpa perlu session.
    

Untuk kita:  
→ JWT akan dipakai untuk:

- Semua request ke `/api/attendance/**`
    
- Semua request ke `/api/students/**` dan `/api/teachers/**`
    
- Khusus Geo: user diambil dari JWT.
    

---

## 3. Entity `User` dan `Role` (Domain Security)

Kita sudah punya desain tabel di Bab 4. Sekarang kita buat entity-nya.

### 3.1 Entity `Role`

```java
package com.sekolah.presensi.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // "ADMIN", "GURU", "SISWA", "ORANG_TUA"

    @Column(length = 255)
    private String description;
}
```

### 3.2 Entity `User`

```java
package com.sekolah.presensi.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = "username")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password; // disimpan dalam bentuk hashed (BCrypt)

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
```

> Catatan: `ManyToMany` diambil dari tabel `user_roles`.

### 3.3 Repository `UserRepository` dan `RoleRepository`

```java
package com.sekolah.presensi.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
```

```java
package com.sekolah.presensi.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
```

---

## 4. Integrasi dengan Spring Security: UserDetails & UserDetailsService

Spring Security perlu tahu:

- Bagaimana membaca user dari database
    
- Bagaimana membaca roles sebagai granted authorities
    

Untuk itu, kita buat:

- `CustomUserDetails` → implement `UserDetails`
    
- `CustomUserDetailsService` → implement `UserDetailsService`
    

### 4.1 `CustomUserDetails`

```java
package com.sekolah.presensi.security;

import com.sekolah.presensi.user.Role;
import com.sekolah.presensi.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                // ROLE_ prefix mengikuti standar Spring Security
                .map(Role::getName)
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Long getUserId() {
        return user.getId();
    }

    public User getUserEntity() {
        return user;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // bisa di-extend
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // bisa di-extend
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // bisa di-extend
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }
}
```

### 4.2 `CustomUserDetailsService`

```java
package com.sekolah.presensi.security;

import com.sekolah.presensi.user.User;
import com.sekolah.presensi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User tidak ditemukan: " + username)
                );

        return new CustomUserDetails(user);
    }
}
```

---

## 5. JWT Utility: Generate & Validate Token

Kita buat util untuk:

- Membuat token JWT setelah login
    
- Membaca username dari JWT
    
- Memvalidasi token
    

Di contoh ini, kita asumsikan pakai library `io.jsonwebtoken:jjwt-api` (detail dependency bisa disesuaikan di `pom.xml` nanti).

### 5.1 `JwtUtil`

```java
package com.sekolah.presensi.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.Date;

@Component
public class JwtUtil {

    // Untuk demo: secret simple. Di production, secret harus panjang & aman.
    private final String SECRET = "ini-rahasia-jwt-sim-presensi-smk-1234567890";
    private final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 jam

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String username, Collection<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // token kadaluarsa
        } catch (JwtException e) {
            // token tidak valid
        }
        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

---

## 6. JWT Filter: Menyisipkan User ke SecurityContext

Kita butuh filter yang:

1. Baca header `Authorization`.
    
2. Ambil token setelah kata `Bearer` .
    
3. Validasi token.
    
4. Ambil username dari token.
    
5. Panggil `CustomUserDetailsService`.
    
6. Isi `SecurityContext` dengan `UsernamePasswordAuthenticationToken`.
    

### 6.1 `JwtAuthenticationFilter`

```java
package com.sekolah.presensi.security.jwt;

import com.sekolah.presensi.security.CustomUserDetailsService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = parseJwt(request);

        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);

            var userDetails = userDetailsService.loadUserByUsername(username);

            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
```

---

## 7. SecurityConfig: Mengikat Semuanya

Di Spring Security 6 (Spring Boot 3), kita pakai `SecurityFilterChain` bean.

### 7.1 `SecurityConfig`

```java
package com.sekolah.presensi.security;

import com.sekolah.presensi.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // endpoint public (login, mungkin register, swagger, dsb.)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        // sementara: endpoint master data dan attendance harus authenticated
                        .requestMatchers("/api/students/**").authenticated()
                        .requestMatchers("/api/teachers/**").authenticated()
                        .requestMatchers("/api/attendance/**").authenticated()
                        // sisanya boleh disesuaikan
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Encoder untuk password (wajib: jangan simpan plain text)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager untuk proses login
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
```

---

## 8. AuthController: Login & Mendapatkan Token

Kini kita butuh endpoint:

- `POST /api/auth/login`  
    Input: username & password  
    Output: JWT + info user
    

### 8.1 DTO Login

```java
package com.sekolah.presensi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
```

### 8.2 DTO AuthResponse

```java
package com.sekolah.presensi.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private String username;
    private String fullName;
    private List<String> roles;
}
```

### 8.3 `AuthController`

```java
package com.sekolah.presensi.auth;

import com.sekolah.presensi.auth.dto.AuthResponse;
import com.sekolah.presensi.auth.dto.LoginRequest;
import com.sekolah.presensi.security.CustomUserDetails;
import com.sekolah.presensi.security.jwt.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Jika tidak exception → login berhasil
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        var roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(userDetails.getUsername())
                .fullName(userDetails.getUserEntity().getFullName())
                .roles(roles)
                .build();

        return ResponseEntity.ok(response);
    }
}
```

---

## 9. Melindungi Endpoint Attendance & Master Data

Karena di `SecurityConfig` kita sudah tulis:

```java
.requestMatchers("/api/students/**").authenticated()
.requestMatchers("/api/teachers/**").authenticated()
.requestMatchers("/api/attendance/**").authenticated()
```

Maka:

- Untuk mengakses **StudentController**, **TeacherController**, **AttendanceController** → user **harus login** dan menyertakan JWT.
    

Cara pakai di client:

1. `POST /api/auth/login` → dapat token
    
2. Setiap request lain, contoh:
    
    ```http
    GET /api/students HTTP/1.1
    Host: ...
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    ```
    

Jika token tidak ada atau invalid → Spring Security akan mengembalikan 401 Unauthorized.

---

## 10. Refactor Geo Attendance: Ambil User dari JWT, Bukan Body

Sebelumnya, DTO `GeoAttendanceRequest` kita bentuk seperti ini:

```java
public class GeoAttendanceRequest {
    private Long userId;
    private Double lat;
    private Double lng;
    private LocalDateTime timestamp;
    private String deviceInfo;
}
```

Ini **kurang aman** karena `userId` bisa dimanipulasi oleh client.

### 10.1 Perbaiki DTO `GeoAttendanceRequest` (hapus `userId`)

```java
package com.sekolah.presensi.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GeoAttendanceRequest {

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    @NotNull
    private LocalDateTime timestamp;

    private String deviceInfo;
}
```

### 10.2 Ubah `AttendanceController` (endpoint `/geo`)

Sekarang, di controller kita ambil user dari `SecurityContext`.

```java
package com.sekolah.presensi.attendance.controller;

import com.sekolah.presensi.attendance.dto.*;
import com.sekolah.presensi.attendance.model.AttendanceRecord;
import com.sekolah.presensi.attendance.service.*;
import com.sekolah.presensi.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final RfidAttendanceService rfidAttendanceService;
    private final FaceAttendanceService faceAttendanceService;
    private final GeoAttendanceService geoAttendanceService;

    // ... RFID & FACE tetap sama

    @PostMapping("/geo")
    public ResponseEntity<AttendanceRecordResponse> handleGeo(
            Authentication authentication,
            @Valid @RequestBody GeoAttendanceRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        AttendanceRecord record = geoAttendanceService.handleGeoAttendance(
                userDetails.getUserEntity(),
                request
        );

        return ResponseEntity.ok(AttendanceRecordResponse.fromEntity(record));
    }
}
```

> Perhatikan: sekarang `GeoAttendanceService` menerima `User` dari controller, bukan `userId` dari body.

### 10.3 Ubah `GeoAttendanceService`

Sebelumnya:

```java
public AttendanceRecord handleGeoAttendance(GeoAttendanceRequest req) {
    // userId dari req.getUserId()
}
```

Sekarang:

```java
package com.sekolah.presensi.attendance.service;

import com.sekolah.presensi.attendance.dto.GeoAttendanceRequest;
import com.sekolah.presensi.attendance.model.*;
import com.sekolah.presensi.attendance.repository.AttendanceRecordRepository;
import com.sekolah.presensi.geo.model.LocationZone;
import com.sekolah.presensi.geo.repository.LocationZoneRepository;
import com.sekolah.presensi.masterdata.student.*;
import com.sekolah.presensi.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeoAttendanceService {

    private final StudentRepository studentRepository;
    private final LocationZoneRepository locationZoneRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ScheduleService scheduleService;

    @Transactional
    public AttendanceRecord handleGeoAttendance(User user, GeoAttendanceRequest req) {

        // 1. Mapping user → student
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("User ini tidak terkait dengan data siswa"));

        // 2. Ambil zona aktif
        LocationZone zone = locationZoneRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalArgumentException("Zona presensi aktif tidak ditemukan"));

        // 3. Hitung jarak
        double distance = haversine(zone.getLat(), zone.getLng(), req.getLat(), req.getLng());
        if (distance > zone.getRadiusMeter()) {
            throw new IllegalArgumentException("Lokasi di luar zona presensi");
        }

        LocalDate date = req.getTimestamp().toLocalDate();

        // 4. Cek record hari ini
        Optional<AttendanceRecord> existingOpt =
                attendanceRecordRepository.findByStudentAndDate(student, date);

        AttendanceRecord record;
        if (existingOpt.isEmpty()) {
            AttendanceStatus status = scheduleService.determineStatus(student, req.getTimestamp());
            record = AttendanceRecord.builder()
                    .student(student)
                    .date(date)
                    .checkInTime(req.getTimestamp())
                    .status(status)
                    .method(AttendanceMethod.GEO)
                    .lat(req.getLat())
                    .lng(req.getLng())
                    .deviceId(req.getDeviceInfo())
                    .note("Check-in via GeoLocation")
                    .build();
        } else {
            record = existingOpt.get();
            record.setCheckOutTime(req.getTimestamp());
            record.setLat(req.getLat());
            record.setLng(req.getLng());
            record.setDeviceId(req.getDeviceInfo());
            record.setNote("Check-out via GeoLocation");
        }

        return attendanceRecordRepository.save(record);
    }

    // Rumus Haversine tetap sama
    private double haversine(double lat1, double lon1, double lat2, double lon2) { /* ... */ }
}
```

Sekarang:

- Tidak ada lagi `userId` di body → **lebih aman**
    
- User yang absen via HP pasti adalah user yang memegang token → **tidak bisa mengaku-ngaku user lain**
    

---

## 11. Ringkas Alur Login & Akses Endpoint

### 11.1 Alur Login

1. User (siswa/guru/admin) memanggil:
    
    `POST /api/auth/login`
    
    ```json
    {
      "username": "siswa01",
      "password": "passwordRahasia"
    }
    ```
    
2. Server:
    
    - Autentikasi user via `AuthenticationManager`
        
    - Jika berhasil → generate JWT
        
    - Return:
        
        ```json
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "tokenType": "Bearer",
          "username": "siswa01",
          "fullName": "Budi Santoso",
          "roles": ["ROLE_SISWA"]
        }
        ```
        
3. Client menyimpan token (`token`).
    

### 11.2 Akses API yang Terproteksi

- Untuk mengakses `/api/attendance/geo`, `/api/students`, dll:
    
    ```http
    POST /api/attendance/geo HTTP/1.1
    Host: ...
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    Content-Type: application/json
    
    {
      "lat": -7.475123,
      "lng": 109.56789,
      "timestamp": "2025-11-17T06:50:00",
      "deviceInfo": "Android-12-POCO-X3"
    }
    ```
    
- Filter JWT:
    
    - Validasi token
        
    - Isi `SecurityContext` dengan user
        
- Controller:
    
    - Ambil `Authentication` → `CustomUserDetails`
        
    - Lempar `userEntity` ke service
        

---

## 12. Penutup Bab Security & JWT

Di bab ini, kita telah:

1. **Mengikat tabel `users`, `roles`, `user_roles`** ke dalam entity & repository.
    
2. Membangun mekanisme **autentikasi** dengan:
    
    - `CustomUserDetails`, `CustomUserDetailsService`
        
    - `PasswordEncoder (BCrypt)`
        
    - `SecurityConfig` dengan `SecurityFilterChain`
        
3. Menyiapkan **JWT Utility** untuk membuat dan memvalidasi token.
    
4. Menambahkan **JWT Filter** untuk membaca token dari `Authorization` header dan mengisi `SecurityContext`.
    
5. Membuat endpoint **`/api/auth/login`** untuk login dan mendapatkan token JWT.
    
6. **Melindungi endpoint** `/api/attendance/**`, `/api/students/**`, `/api/teachers/**`.
    
7. **Memperbaiki Geo Attendance** supaya **`userId` tidak dikirim dari body**, tapi diambil dari JWT (jauh lebih aman).
    

Dengan ini, backend SIM Presensi Anda sudah:

- **Realistis** untuk digunakan di lingkungan sekolah
    
- Mengikuti pola **industry standard** (Spring Security + JWT)
    
- Siap di-_scale_ atau diintegrasikan ke modul lain (dashboard, reporting, PKL, dll.)
    

---

Kita gaskeun Bab Security & JWT-nya ya. Fokusnya:

- Nyambung langsung dengan tabel `users`, `roles`, `user_roles` dari Bab 4
    
- Pakai Spring Security + JWT (stateless)
    
- Lindungi endpoint `/api/attendance/*`, `/api/students`, `/api/teachers`
    
- Perbaiki **Geo Attendance** supaya `userId` diambil dari JWT, bukan dari body
    

---

# **BAB – Security & JWT di SIM Presensi Sekolah**

## 1. Pendahuluan

Sampai di bab ini, sistem kita sudah punya:

- **Arsitektur backend** (Bab 3)
    
- **Modul Attendance** (RFID, Face, Geo)
    
- **Desain Database + ERD** (Bab 4)
    
- **Master Data Siswa & Guru** (CRUD, Bab sebelumnya)
    

Semua itu **belum aman** kalau:

- Endpoint bisa diakses bebas tanpa login
    
- Device dan user tidak diautentikasi
    
- `userId` dikirim dari client (mudah dimanipulasi)
    

Karena itu, kita butuh:

> **Lapisan keamanan** menggunakan **Spring Security + JWT (JSON Web Token)**

Bab ini akan membahas:

1. Konsep dasar **Spring Security** dan **JWT** (stateless auth).
    
2. Entity & repository untuk `User` dan `Role`.
    
3. Implementasi **UserDetailsService**, **PasswordEncoder**, dan **SecurityConfig**.
    
4. Implementasi **JWT util** + **JWT filter**.
    
5. Endpoint `/api/auth/login` untuk mendapatkan token.
    
6. Konfigurasi proteksi endpoint `/api/attendance/**`, `/api/students`, `/api/teachers`.
    
7. **Refactor Geo Attendance**: `userId` diambil dari token, bukan dari body.
    

---

## 2. Konsep Dasar Spring Security & JWT

### 2.1 Spring Security (Singkat)

Spring Security adalah framework security untuk aplikasi Java/Spring. Fitur utamanya:

- Autentikasi (verifikasi siapa pengguna)
    
- Otorisasi (apa yang boleh diakses)
    
- Filter chain pada HTTP request
    
- Integrasi dengan database, JWT, OAuth2, dll.
    

### 2.2 JWT – JSON Web Token

JWT adalah token berbentuk string yang:

- Dipakai untuk autentikasi stateless (server tidak simpan session di memori)
    
- Berisi informasi user (subject, roles, expired) dalam bentuk payload yang ditandatangani (signature)
    
- Dikirim di header `Authorization: Bearer <token>`
    

Alur sederhananya:

1. User login (username & password) ke `/api/auth/login`
    
2. Server verifikasi username & password
    
3. Server membuat JWT berisi `username` + `roles` + `expired`
    
4. Client menyimpan JWT (localStorage / memory)
    
5. Setiap request berikutnya: client kirim header `Authorization: Bearer <token>`
    
6. Server memverifikasi token lalu **mengenali user** dari token, tanpa perlu session.
    

Untuk kita:  
→ JWT akan dipakai untuk:

- Semua request ke `/api/attendance/**`
    
- Semua request ke `/api/students/**` dan `/api/teachers/**`
    
- Khusus Geo: user diambil dari JWT.
    

---

## 3. Entity `User` dan `Role` (Domain Security)

Kita sudah punya desain tabel di Bab 4. Sekarang kita buat entity-nya.

### 3.1 Entity `Role`

```java
package com.sekolah.presensi.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // "ADMIN", "GURU", "SISWA", "ORANG_TUA"

    @Column(length = 255)
    private String description;
}
```

### 3.2 Entity `User`

```java
package com.sekolah.presensi.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = "username")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password; // disimpan dalam bentuk hashed (BCrypt)

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
```

> Catatan: `ManyToMany` diambil dari tabel `user_roles`.

### 3.3 Repository `UserRepository` dan `RoleRepository`

```java
package com.sekolah.presensi.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
```

```java
package com.sekolah.presensi.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
```

---

## 4. Integrasi dengan Spring Security: UserDetails & UserDetailsService

Spring Security perlu tahu:

- Bagaimana membaca user dari database
    
- Bagaimana membaca roles sebagai granted authorities
    

Untuk itu, kita buat:

- `CustomUserDetails` → implement `UserDetails`
    
- `CustomUserDetailsService` → implement `UserDetailsService`
    

### 4.1 `CustomUserDetails`

```java
package com.sekolah.presensi.security;

import com.sekolah.presensi.user.Role;
import com.sekolah.presensi.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                // ROLE_ prefix mengikuti standar Spring Security
                .map(Role::getName)
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Long getUserId() {
        return user.getId();
    }

    public User getUserEntity() {
        return user;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // bisa di-extend
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // bisa di-extend
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // bisa di-extend
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }
}
```

### 4.2 `CustomUserDetailsService`

```java
package com.sekolah.presensi.security;

import com.sekolah.presensi.user.User;
import com.sekolah.presensi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User tidak ditemukan: " + username)
                );

        return new CustomUserDetails(user);
    }
}
```

---

## 5. JWT Utility: Generate & Validate Token

Kita buat util untuk:

- Membuat token JWT setelah login
    
- Membaca username dari JWT
    
- Memvalidasi token
    

Di contoh ini, kita asumsikan pakai library `io.jsonwebtoken:jjwt-api` (detail dependency bisa disesuaikan di `pom.xml` nanti).

### 5.1 `JwtUtil`

```java
package com.sekolah.presensi.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.Date;

@Component
public class JwtUtil {

    // Untuk demo: secret simple. Di production, secret harus panjang & aman.
    private final String SECRET = "ini-rahasia-jwt-sim-presensi-smk-1234567890";
    private final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 jam

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String username, Collection<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // token kadaluarsa
        } catch (JwtException e) {
            // token tidak valid
        }
        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

---

## 6. JWT Filter: Menyisipkan User ke SecurityContext

Kita butuh filter yang:

1. Baca header `Authorization`.
    
2. Ambil token setelah kata `Bearer` .
    
3. Validasi token.
    
4. Ambil username dari token.
    
5. Panggil `CustomUserDetailsService`.
    
6. Isi `SecurityContext` dengan `UsernamePasswordAuthenticationToken`.
    

### 6.1 `JwtAuthenticationFilter`

```java
package com.sekolah.presensi.security.jwt;

import com.sekolah.presensi.security.CustomUserDetailsService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = parseJwt(request);

        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);

            var userDetails = userDetailsService.loadUserByUsername(username);

            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
```

---

## 7. SecurityConfig: Mengikat Semuanya

Di Spring Security 6 (Spring Boot 3), kita pakai `SecurityFilterChain` bean.

### 7.1 `SecurityConfig`

```java
package com.sekolah.presensi.security;

import com.sekolah.presensi.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // endpoint public (login, mungkin register, swagger, dsb.)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        // sementara: endpoint master data dan attendance harus authenticated
                        .requestMatchers("/api/students/**").authenticated()
                        .requestMatchers("/api/teachers/**").authenticated()
                        .requestMatchers("/api/attendance/**").authenticated()
                        // sisanya boleh disesuaikan
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Encoder untuk password (wajib: jangan simpan plain text)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager untuk proses login
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
```

---

## 8. AuthController: Login & Mendapatkan Token

Kini kita butuh endpoint:

- `POST /api/auth/login`  
    Input: username & password  
    Output: JWT + info user
    

### 8.1 DTO Login

```java
package com.sekolah.presensi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
```

### 8.2 DTO AuthResponse

```java
package com.sekolah.presensi.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private String username;
    private String fullName;
    private List<String> roles;
}
```

### 8.3 `AuthController`

```java
package com.sekolah.presensi.auth;

import com.sekolah.presensi.auth.dto.AuthResponse;
import com.sekolah.presensi.auth.dto.LoginRequest;
import com.sekolah.presensi.security.CustomUserDetails;
import com.sekolah.presensi.security.jwt.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Jika tidak exception → login berhasil
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        var roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(userDetails.getUsername())
                .fullName(userDetails.getUserEntity().getFullName())
                .roles(roles)
                .build();

        return ResponseEntity.ok(response);
    }
}
```

---

## 9. Melindungi Endpoint Attendance & Master Data

Karena di `SecurityConfig` kita sudah tulis:

```java
.requestMatchers("/api/students/**").authenticated()
.requestMatchers("/api/teachers/**").authenticated()
.requestMatchers("/api/attendance/**").authenticated()
```

Maka:

- Untuk mengakses **StudentController**, **TeacherController**, **AttendanceController** → user **harus login** dan menyertakan JWT.
    

Cara pakai di client:

1. `POST /api/auth/login` → dapat token
    
2. Setiap request lain, contoh:
    
    ```http
    GET /api/students HTTP/1.1
    Host: ...
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    ```
    

Jika token tidak ada atau invalid → Spring Security akan mengembalikan 401 Unauthorized.

---

## 10. Refactor Geo Attendance: Ambil User dari JWT, Bukan Body

Sebelumnya, DTO `GeoAttendanceRequest` kita bentuk seperti ini:

```java
public class GeoAttendanceRequest {
    private Long userId;
    private Double lat;
    private Double lng;
    private LocalDateTime timestamp;
    private String deviceInfo;
}
```

Ini **kurang aman** karena `userId` bisa dimanipulasi oleh client.

### 10.1 Perbaiki DTO `GeoAttendanceRequest` (hapus `userId`)

```java
package com.sekolah.presensi.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GeoAttendanceRequest {

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    @NotNull
    private LocalDateTime timestamp;

    private String deviceInfo;
}
```

### 10.2 Ubah `AttendanceController` (endpoint `/geo`)

Sekarang, di controller kita ambil user dari `SecurityContext`.

```java
package com.sekolah.presensi.attendance.controller;

import com.sekolah.presensi.attendance.dto.*;
import com.sekolah.presensi.attendance.model.AttendanceRecord;
import com.sekolah.presensi.attendance.service.*;
import com.sekolah.presensi.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final RfidAttendanceService rfidAttendanceService;
    private final FaceAttendanceService faceAttendanceService;
    private final GeoAttendanceService geoAttendanceService;

    // ... RFID & FACE tetap sama

    @PostMapping("/geo")
    public ResponseEntity<AttendanceRecordResponse> handleGeo(
            Authentication authentication,
            @Valid @RequestBody GeoAttendanceRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        AttendanceRecord record = geoAttendanceService.handleGeoAttendance(
                userDetails.getUserEntity(),
                request
        );

        return ResponseEntity.ok(AttendanceRecordResponse.fromEntity(record));
    }
}
```

> Perhatikan: sekarang `GeoAttendanceService` menerima `User` dari controller, bukan `userId` dari body.

### 10.3 Ubah `GeoAttendanceService`

Sebelumnya:

```java
public AttendanceRecord handleGeoAttendance(GeoAttendanceRequest req) {
    // userId dari req.getUserId()
}
```

Sekarang:

```java
package com.sekolah.presensi.attendance.service;

import com.sekolah.presensi.attendance.dto.GeoAttendanceRequest;
import com.sekolah.presensi.attendance.model.*;
import com.sekolah.presensi.attendance.repository.AttendanceRecordRepository;
import com.sekolah.presensi.geo.model.LocationZone;
import com.sekolah.presensi.geo.repository.LocationZoneRepository;
import com.sekolah.presensi.masterdata.student.*;
import com.sekolah.presensi.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeoAttendanceService {

    private final StudentRepository studentRepository;
    private final LocationZoneRepository locationZoneRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ScheduleService scheduleService;

    @Transactional
    public AttendanceRecord handleGeoAttendance(User user, GeoAttendanceRequest req) {

        // 1. Mapping user → student
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("User ini tidak terkait dengan data siswa"));

        // 2. Ambil zona aktif
        LocationZone zone = locationZoneRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalArgumentException("Zona presensi aktif tidak ditemukan"));

        // 3. Hitung jarak
        double distance = haversine(zone.getLat(), zone.getLng(), req.getLat(), req.getLng());
        if (distance > zone.getRadiusMeter()) {
            throw new IllegalArgumentException("Lokasi di luar zona presensi");
        }

        LocalDate date = req.getTimestamp().toLocalDate();

        // 4. Cek record hari ini
        Optional<AttendanceRecord> existingOpt =
                attendanceRecordRepository.findByStudentAndDate(student, date);

        AttendanceRecord record;
        if (existingOpt.isEmpty()) {
            AttendanceStatus status = scheduleService.determineStatus(student, req.getTimestamp());
            record = AttendanceRecord.builder()
                    .student(student)
                    .date(date)
                    .checkInTime(req.getTimestamp())
                    .status(status)
                    .method(AttendanceMethod.GEO)
                    .lat(req.getLat())
                    .lng(req.getLng())
                    .deviceId(req.getDeviceInfo())
                    .note("Check-in via GeoLocation")
                    .build();
        } else {
            record = existingOpt.get();
            record.setCheckOutTime(req.getTimestamp());
            record.setLat(req.getLat());
            record.setLng(req.getLng());
            record.setDeviceId(req.getDeviceInfo());
            record.setNote("Check-out via GeoLocation");
        }

        return attendanceRecordRepository.save(record);
    }

    // Rumus Haversine tetap sama
    private double haversine(double lat1, double lon1, double lat2, double lon2) { /* ... */ }
}
```

Sekarang:

- Tidak ada lagi `userId` di body → **lebih aman**
    
- User yang absen via HP pasti adalah user yang memegang token → **tidak bisa mengaku-ngaku user lain**
    

---

## 11. Ringkas Alur Login & Akses Endpoint

### 11.1 Alur Login

1. User (siswa/guru/admin) memanggil:
    
    `POST /api/auth/login`
    
    ```json
    {
      "username": "siswa01",
      "password": "passwordRahasia"
    }
    ```
    
2. Server:
    
    - Autentikasi user via `AuthenticationManager`
        
    - Jika berhasil → generate JWT
        
    - Return:
        
        ```json
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "tokenType": "Bearer",
          "username": "siswa01",
          "fullName": "Budi Santoso",
          "roles": ["ROLE_SISWA"]
        }
        ```
        
3. Client menyimpan token (`token`).
    

### 11.2 Akses API yang Terproteksi

- Untuk mengakses `/api/attendance/geo`, `/api/students`, dll:
    
    ```http
    POST /api/attendance/geo HTTP/1.1
    Host: ...
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
    Content-Type: application/json
    
    {
      "lat": -7.475123,
      "lng": 109.56789,
      "timestamp": "2025-11-17T06:50:00",
      "deviceInfo": "Android-12-POCO-X3"
    }
    ```
    
- Filter JWT:
    
    - Validasi token
        
    - Isi `SecurityContext` dengan user
        
- Controller:
    
    - Ambil `Authentication` → `CustomUserDetails`
        
    - Lempar `userEntity` ke service
        

---

## 12. Penutup Bab Security & JWT

Di bab ini, kita telah:

1. **Mengikat tabel `users`, `roles`, `user_roles`** ke dalam entity & repository.
    
2. Membangun mekanisme **autentikasi** dengan:
    
    - `CustomUserDetails`, `CustomUserDetailsService`
        
    - `PasswordEncoder (BCrypt)`
        
    - `SecurityConfig` dengan `SecurityFilterChain`
        
3. Menyiapkan **JWT Utility** untuk membuat dan memvalidasi token.
    
4. Menambahkan **JWT Filter** untuk membaca token dari `Authorization` header dan mengisi `SecurityContext`.
    
5. Membuat endpoint **`/api/auth/login`** untuk login dan mendapatkan token JWT.
    
6. **Melindungi endpoint** `/api/attendance/**`, `/api/students/**`, `/api/teachers/**`.
    
7. **Memperbaiki Geo Attendance** supaya **`userId` tidak dikirim dari body**, tapi diambil dari JWT (jauh lebih aman).
    

Dengan ini, backend SIM Presensi Anda sudah:

- **Realistis** untuk digunakan di lingkungan sekolah
    
- Mengikuti pola **industry standard** (Spring Security + JWT)
    
- Siap di-_scale_ atau diintegrasikan ke modul lain (dashboard, reporting, PKL, dll.)
    

---

Oke, kita susun tiga bab lanjutannya ya: **Integrasi RFID & ESP32**, **Integrasi Face Recognition**, dan **Modul Reporting & Dashboard**. Saya buat dengan gaya buku, tetap nyambung dengan bab-bab sebelumnya dan dengan struktur API yang sudah kita tetapkan.

---

# **BAB – Integrasi Perangkat RFID & ESP32 ke `/api/attendance/rfid`**

## 1. Tujuan Bab

Di bab-bab sebelumnya, kita sudah:

- Mendesain **endpoint `/api/attendance/rfid`** di Spring Boot.
    
- Menentukan format request JSON:
    
    ```json
    {
      "deviceId": "GATE-1",
      "rfidTag": "04A1B2C3D4",
      "timestamp": "2025-11-17T06:58:23"
    }
    ```
    

Sekarang kita bahas:

> Bagaimana membuat **perangkat fisik (ESP32 + RFID reader)** yang bisa mengirimkan data ini ke backend Spring Boot.

Bab ini akan membahas:

1. Gambaran umum **topologi** & arsitektur hardware.
    
2. Format request HTTP dari ESP32 ke backend.
    
3. Contoh **kode ESP32 (Arduino)** untuk:
    
    - Koneksi WiFi
        
    - Membaca RFID
        
    - Mengirim HTTP POST ke Spring Boot
        
4. Cara **meng-handle error & retry** ketika jaringan putus.
    
5. Ide pengembangan lab di SMK.
    

---

## 2. Arsitektur Integrasi RFID & ESP32

### 2.1 Komponen Hardware

Umumnya kita menggunakan:

- **ESP32**: microcontroller dengan WiFi.
    
- **RC522 RFID Reader** (atau modul lain).
    
- Kartu RFID (Mifare 13.56 MHz).
    
- Power supply (bisa dari adaptor/USB).
    

Skema sederhana:

```text
[ Kartu RFID ] 
      ↓
[ RC522 Reader ] -- SPI --> [ ESP32 ] -- WiFi --> [ Spring Boot Backend ] -- DB
```

### 2.2 Jalur Komunikasi

1. Kartu ditempel ke RC522 → UID RFID dibaca.
    
2. ESP32 mengemas UID tersebut menjadi string (misal "04A1B2C3D4").
    
3. ESP32 membuat request HTTP:
    
    `POST http://IP_SERVER:PORT/api/attendance/rfid`
    
    Body JSON:
    
    ```json
    {
      "deviceId": "GATE-1",
      "rfidTag": "04A1B2C3D4",
      "timestamp": "2025-11-17T06:58:23"
    }
    ```
    
4. Backend Spring Boot memproses (RfidAttendanceService) dan menyimpan ke database.
    
5. Backend merespon JSON hasil presensi (ON_TIME / LATE, dll).
    
6. ESP32 bisa menampilkan hasil ke LCD/LED atau buzzer.
    

---

## 3. Format Request HTTP dari ESP32

### 3.1 Endpoint & Method

- **Method**: `POST`
    
- **URL**: `http://<IP_SERVER>:<PORT>/api/attendance/rfid`
    
    - Contoh: `http://192.168.4.10:8080/api/attendance/rfid`
        

### 3.2 Header

- `Content-Type: application/json`
    
- Jika nanti ingin amankan device dengan token khusus:
    
    - `X-DEVICE-TOKEN: ....`
        

### 3.3 Body JSON

```json
{
  "deviceId": "GATE-1",
  "rfidTag": "04A1B2C3D4",
  "timestamp": "2025-11-17T06:58:23"
}
```

- `deviceId`: ID perangkat yang sudah terdaftar di tabel `devices`.
    
- `rfidTag`: UID kartu RFID yang dibaca.
    
- `timestamp`: waktu lokal server atau waktu dari ESP32 (kalau punya RTC / NTP).
    

> **Opsional:** Kalau sekolah tidak pakai RTC di ESP32, timestamp bisa diisi oleh backend (dengan `LocalDateTime.now()`), dan `timestamp` dari device diabaikan.

---

## 4. Contoh Implementasi ESP32 (Arduino)

Berikut contoh sketch sederhana dengan asumsi:

- Menggunakan **library WiFi.h**
    
- Menggunakan **HTTPClient.h**
    
- RFID membaca UID dan diubah ke string.
    

### 4.1 Konfigurasi Dasar

```cpp
#include <WiFi.h>
#include <HTTPClient.h>
#include <SPI.h>
#include <MFRC522.h>

#define SS_PIN  5   // sesuaikan dengan wiring
#define RST_PIN 22  // sesuaikan dengan wiring

MFRC522 rfid(SS_PIN, RST_PIN);

const char* ssid     = "NamaWiFiSekolah";
const char* password = "passwordwifi";

const char* serverUrl = "http://192.168.4.10:8080/api/attendance/rfid";
const char* deviceId  = "GATE-1";

void setup() {
  Serial.begin(115200);
  SPI.begin();
  rfid.PCD_Init();

  Serial.println("Inisialisasi RFID & WiFi...");

  WiFi.begin(ssid, password);
  Serial.print("Menghubungkan ke WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nTerhubung ke WiFi!");
  Serial.print("IP ESP32: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  // Cek kartu RFID
  if (!rfid.PICC_IsNewCardPresent() || !rfid.PICC_ReadCardSerial()) {
    delay(50);
    return;
  }

  // Ambil UID sebagai string hex
  String uidStr = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    uidStr += String(rfid.uid.uidByte[i] < 0x10 ? "0" : "");
    uidStr += String(rfid.uid.uidByte[i], HEX);
  }
  uidStr.toUpperCase();

  Serial.print("Kartu terdeteksi, UID: ");
  Serial.println(uidStr);

  // Kirim ke backend
  sendAttendance(uidStr);

  // Hindari pembacaan ganda terlalu cepat
  delay(1000);

  // Stop PICC
  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();
}

void sendAttendance(String rfidTag) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi tidak terhubung, tidak bisa kirim data.");
    return;
  }

  HTTPClient http;
  http.begin(serverUrl);
  http.addHeader("Content-Type", "application/json");

  // NOTE: timestamp untuk demo, bisa di-generate di server
  String jsonPayload = "{";
  jsonPayload += "\"deviceId\":\"" + String(deviceId) + "\",";
  jsonPayload += "\"rfidTag\":\"" + rfidTag + "\",";
  jsonPayload += "\"timestamp\":\"2025-11-17T07:00:00\""; // hard-coded / bisa diganti
  jsonPayload += "}";

  Serial.print("Mengirim: ");
  Serial.println(jsonPayload);

  int httpResponseCode = http.POST(jsonPayload);

  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.print("Response code: ");
    Serial.println(httpResponseCode);
    Serial.println("Response body:");
    Serial.println(response);
  } else {
    Serial.print("Error sending POST, code: ");
    Serial.println(httpResponseCode);
  }

  http.end();
}
```

**Penjelasan pedagogis untuk siswa:**

- Fungsi `sendAttendance()` adalah jembatan antara dunia **hardware** dan **rest API**.
    
- Di bagian JSON, kita membangun string manual—ini bisa dibuat lebih rapi dengan library JSON di ESP32 kalau mau.
    
- `rfidTag` diubah ke HEX uppercase agar konsisten dengan yang disimpan di database.
    

---

## 5. Penanganan Error & Retry

Di dunia nyata:

- Jaringan WiFi sekolah bisa putus sesaat.
    
- Server Spring Boot bisa restart.
    

Tips:

1. Jika `httpResponseCode <= 0`, simpan UID + waktu ke **buffer** (misal array / file SPIFFS) untuk dikirim ulang nanti.
    
2. Beri delay beberapa detik sebelum mencoba lagi.
    
3. Bisa juga tampilkan pesan di layar OLED: “Koneksi server gagal”.
    

---

## 6. Ide Praktikum di SMK

Beberapa ide tugas/proyek siswa:

1. **Membuat gate presensi** dengan ESP32 + RC522 + LED Merah/Hijau.
    
2. Menambahkan **buzzer** → bunyi berbeda untuk presensi berhasil / gagal.
    
3. Menambahkan **LCD/OLED** → tampilkan nama siswa dan status ON_TIME/LATE dari response JSON.
    
4. Menggunakan **RTC atau NTP** untuk mensinkronkan jam di ESP32.
    
5. Mengembangkan **mode offline** → data disimpan di EEPROM / SPIFFS saat offline, lalu sinkron ke Spring Boot saat WiFi kembali.
    

---

# **BAB – Integrasi Face Recognition (Service Python/AI & Spring Boot)**

## 1. Tujuan Bab

Di modul Attendance, kita sudah menyiapkan:

- Endpoint `/api/attendance/face` di Spring Boot.
    
- Service `FaceAttendanceService` yang memanggil `FaceRecognitionClient`.
    

Di bab ini kita rancang:

> **Layanan face recognition berbasis Python** (misal dengan FastAPI/Flask) yang:

- Menerima gambar wajah (base64 atau file).
    
- Mencocokkan dengan database wajah (embedding).
    
- Mengembalikan `studentId` + `confidence` ke Spring Boot.
    

---

## 2. Arsitektur Tingkat Tinggi

```text
[Camera / Client] 
    ↓ (kirim image / token)
[Python Face Engine Service]  <-->  [Database Embedding / File Storage]
    ↓ (hasil match: studentId, confidence)
[Spring Boot Backend]
    ↓
[AttendanceRecord]
```

Ada beberapa pola integrasi:

1. **Langsung Camera → Spring Boot → Python**
    
2. **Camera → Python → Spring Boot**
    

Di buku ini, kita pakai pola **Spring Boot sebagai pusat**:

- Camera (atau mini PC) mengirim gambar/faceToken ke **Spring Boot**.
    
- Spring Boot mengirim data ke **Python face service** lewat HTTP.
    
- Python mengembalikan `studentId + confidence`.
    

Ini sejalan dengan class `FaceRecognitionClient` yang sudah kita buat.

---

## 3. Kontrak API antara Spring Boot & Face Service

### 3.1 Endpoint Face Service (Python)

Misalnya:

- `POST /api/face/verify`
    

Request JSON (contoh 1 – pakai faceToken):

```json
{
  "faceToken": "token-wajah-xyz"
}
```

atau (contoh 2 – pakai base64 image):

```json
{
  "imageBase64": "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDA..."
}
```

Response:

```json
{
  "matched": true,
  "studentId": 123,
  "confidence": 0.93
}
```

Spring Boot hanya peduli:

- Kalau `matched == true` dan `confidence >= threshold`, presensi lanjut.
    
- Kalau tidak, presensi ditolak.
    

---

## 4. Contoh Implementasi Face Service dengan FastAPI (Python)

### 4.1 Struktur Sederhana

```python
# face_service/main.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI()

# Model request & response
class FaceRequest(BaseModel):
    faceToken: str  # atau imageBase64: str

class FaceResponse(BaseModel):
    matched: bool
    studentId: int | None
    confidence: float | None

# Dummy database mapping token -> studentId
FAKE_FACE_DB = {
    "token-wajah-xyz": (123, 0.95),
    "token-wajah-abc": (124, 0.88),
}

@app.post("/api/face/verify", response_model=FaceResponse)
async def verify_face(req: FaceRequest):
    # Di dunia nyata: hitung embedding, bandingkan dengan database vector
    if req.faceToken in FAKE_FACE_DB:
        student_id, conf = FAKE_FACE_DB[req.faceToken]
        return FaceResponse(
            matched=True,
            studentId=student_id,
            confidence=conf
        )
    return FaceResponse(
        matched=False,
        studentId=None,
        confidence=None
    )
```

Jalankan:

```bash
uvicorn face_service.main:app --host 0.0.0.0 --port 8000
```

---

## 5. Menghubungkan dari Spring Boot (`FaceRecognitionClient`)

Kita sudah punya (konsepnya) di bab sebelumnya, sekarang kita tegaskan kontrak URL-nya.

```java
@Component
@RequiredArgsConstructor
public class FaceRecognitionClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://face-service:8000") // bisa pakai IP, misal http://192.168.4.20:8000
            .build();

    public FaceMatchResult verify(String faceToken) {
        return webClient.post()
                .uri("/api/face/verify")
                .bodyValue(Map.of("faceToken", faceToken))
                .retrieve()
                .bodyToMono(FaceMatchResult.class)
                .block();
    }
}
```

`FaceMatchResult`:

```java
@Data
public class FaceMatchResult {
    private boolean matched;
    private Long studentId;
    private double confidence;
}
```

---

## 6. Alur Lengkap Face Attendance

1. Kamera / client menangkap wajah.
    
2. Client mengirim `faceToken` ke Spring Boot:
    
    `POST /api/attendance/face`
    
    ```json
    {
      "deviceId": "CLASS-11-RPL-1-CAM",
      "faceToken": "token-wajah-xyz",
      "timestamp": "2025-11-17T07:02:10"
    }
    ```
    
3. `AttendanceController` → `FaceAttendanceService`.
    
4. `FaceAttendanceService` memanggil `faceRecognitionClient.verify(faceToken)`.
    
5. Python Face Service mengembalikan:
    
    ```json
    { "matched": true, "studentId": 123, "confidence": 0.95 }
    ```
    
6. Spring Boot:
    
    - Cari `Student` ID 123.
        
    - Cek jadwal & record hari ini.
        
    - Simpan `AttendanceRecord` dengan `method = FACE`.
        
7. Response ke client: status presensi.
    

---

## 7. Pengembangan Lanjut: Menggunakan Embedding Wajah

Untuk versi lanjut (di luar ruang buku dasar), face engine bisa:

- Menggunakan library seperti **FaceNet**, **InsightFace**, dsb.
    
- Menyimpan **embedding** (vector float) di database face service.
    
- Proses:
    
    - Saat enrollment → ambil gambar, hitung embedding, simpan ke DB.
        
    - Saat verifikasi → ambil gambar, hitung embedding, cari vector terdekat (cosine similarity).
        

Spring Boot tetap **tidak perlu tahu detail AI**, hanya butuh API hasil `studentId + confidence`.

---

## 8. Ide Praktikum di SMK

1. Buat **simulasi** tanpa kamera dulu:
    
    - Input `faceToken` via form web / Postman.
        
    - Python service mengembalikan `studentId`.
        
2. Jika punya hardware:
    
    - Gunakan webcam + aplikasi Python untuk menghasilkan `faceToken`.
        
    - Integrasi dengan Spring Boot di LAN sekolah.
        
3. Proyek lanjutan:
    
    - Siswa membuat modul “Enroll Wajah”:
        
        - `/api/face/enroll`
            
        - Menyimpan `faceToken` ke `face_templates` tabel.
            

---

# **BAB – Modul Reporting & Dashboard (Laporan & Statistik Presensi)**

## 1. Tujuan Bab

Semua data presensi tidak ada artinya kalau tidak bisa dibaca dalam bentuk **laporan** dan **dashboard**.

Bab ini membahas:

1. Desain endpoint laporan:
    
    - Rekap harian
        
    - Rekap per kelas
        
    - Rekap per siswa
        
2. Contoh query agregasi sederhana.
    
3. DTO untuk response laporan.
    
4. Penyusunan API yang siap dipakai frontend (React/Vue/Blade, dll).
    

---

## 2. Kebutuhan Laporan SIM Presensi Sekolah

Laporan tipikal yang dibutuhkan:

1. **Rekap harian per kelas**:
    
    - Berapa siswa hadir, terlambat, izin, sakit, alfa.
        
2. **Rekap per siswa**:
    
    - Dalam rentang tanggal tertentu, total:
        
        - Hadir
            
        - Terlambat
            
        - Tidak hadir
            
        - Izin/Sakit (bila nanti ditambahkan status-detail).
            
3. **Rekap per guru** (jika guru juga presensi):
    
    - Kehadiran guru per minggu/bulan.
        
4. **Dashboard ringkas (statistik)**:
    
    - Total siswa hadir hari ini.
        
    - Top 10 siswa paling sering terlambat.
        
    - Kelas dengan tingkat kehadiran tertinggi.
        

Di bab ini, kita fokus ke:

- **Rekap harian per kelas**
    
- **Rekap per siswa**
    

---

## 3. DTO untuk Reporting

### 3.1 Rekap Harian Per Kelas

```java
package com.sekolah.presensi.reporting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyClassAttendanceSummary {

    private Long classroomId;
    private String classroomName;

    private String date; // "2025-11-17"

    private long totalStudents;
    private long presentCount;
    private long lateCount;
    private long absentCount;
}
```

### 3.2 Rekap Per Siswa (Dalam Range Tanggal)

```java
package com.sekolah.presensi.reporting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentAttendanceSummary {

    private Long studentId;
    private String nis;
    private String studentName;
    private String classroomName;

    private String startDate;
    private String endDate;

    private long presentCount;  // ON_TIME + LATE
    private long lateCount;
    private long absentCount;
}
```

---

## 4. Repository Level: Query Agregasi

Kita bisa pakai:

- **Spring Data JPA** + **@Query (JPQL/Native)**  
    atau
    
- Service yang menghitung di memory (kurang efisien untuk data besar).
    

### 4.1 Contoh: Rekap Harian Per Kelas

Kita ingin menghitung:

- Total siswa di kelas tersebut.
    
- Berapa siswa yang memiliki record presensi di `attendance_records` untuk tanggal tertentu.
    
- Dari yang punya record: berapa ON_TIME, LATE.
    
- Sisanya dianggap ABSENT.
    

#### 4.1.1 Tambah Method di `AttendanceRecordRepository` (atau buat ReportingRepository baru)

Contoh pakai native query:

```java
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    // existing...

    @Query(value = """
        SELECT 
            SUM(CASE WHEN ar.status = 'ON_TIME' THEN 1 ELSE 0 END) AS on_time_count,
            SUM(CASE WHEN ar.status = 'LATE' THEN 1 ELSE 0 END) AS late_count
        FROM attendance_records ar
        JOIN students s ON ar.student_id = s.id
        WHERE ar.attendance_date = :date
          AND s.classroom_id = :classroomId
        """, nativeQuery = true)
    Object[] aggregateDailyByClassroom(Long classroomId, LocalDate date);
}
```

> Kita hanya menghitung ON_TIME dan LATE; ABSENT akan dihitung di service sebagai:  
> `totalStudents - (on_time_count + late_count)`.

### 4.2 Mendapatkan Total Siswa per Kelas

Di `StudentRepository`:

```java
@Query("SELECT COUNT(s) FROM Student s WHERE s.classRoom.id = :classroomId AND s.active = true")
long countActiveByClassroom(Long classroomId);
```

---

## 5. Service Reporting

### 5.1 Service Rekap Harian Per Kelas

```java
package com.sekolah.presensi.reporting.service;

import com.sekolah.presensi.attendance.repository.AttendanceRecordRepository;
import com.sekolah.presensi.masterdata.classroom.ClassRoom;
import com.sekolah.presensi.masterdata.classroom.ClassRoomRepository;
import com.sekolah.presensi.masterdata.student.StudentRepository;
import com.sekolah.presensi.reporting.dto.DailyClassAttendanceSummary;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AttendanceReportService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentRepository studentRepository;
    private final ClassRoomRepository classRoomRepository;

    public DailyClassAttendanceSummary getDailySummaryByClassroom(Long classroomId, LocalDate date) {

        ClassRoom classRoom = classRoomRepository.findById(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom tidak ditemukan"));

        long totalStudents = studentRepository.countActiveByClassroom(classroomId);

        Object[] agg = attendanceRecordRepository.aggregateDailyByClassroom(classroomId, date);
        long onTimeCount = 0;
        long lateCount = 0;

        if (agg != null) {
            // agg[0] -> on_time_count, agg[1] -> late_count
            onTimeCount = agg[0] != null ? ((Number) agg[0]).longValue() : 0L;
            lateCount = agg[1] != null ? ((Number) agg[1]).longValue() : 0L;
        }

        long presentCount = onTimeCount + lateCount;
        long absentCount = totalStudents - presentCount;
        if (absentCount < 0) absentCount = 0; // jaga-jaga

        return DailyClassAttendanceSummary.builder()
                .classroomId(classRoom.getId())
                .classroomName(classRoom.getName())
                .date(date.toString())
                .totalStudents(totalStudents)
                .presentCount(presentCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .build();
    }
}
```

---

### 5.2 Service Rekap Per Siswa (Rentang Tanggal)

Kita bisa pakai query sederhana:

Di `AttendanceRecordRepository`:

```java
@Query("""
    SELECT 
        SUM(CASE WHEN ar.status = 'ON_TIME' THEN 1 ELSE 0 END),
        SUM(CASE WHEN ar.status = 'LATE' THEN 1 ELSE 0 END)
    FROM AttendanceRecord ar
    WHERE ar.student.id = :studentId
      AND ar.date BETWEEN :startDate AND :endDate
""")
Object[] aggregateByStudentAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate);
```

Di `StudentRepository` tambah:

```java
Optional<Student> findByNis(String nis);
```

Lalu di service:

```java
public StudentAttendanceSummary getStudentSummaryByNis(String nis, LocalDate startDate, LocalDate endDate) {
    Student student = studentRepository.findByNis(nis)
            .orElseThrow(() -> new EntityNotFoundException("Siswa dengan NIS " + nis + " tidak ditemukan"));

    Object[] agg = attendanceRecordRepository.aggregateByStudentAndDateRange(student.getId(), startDate, endDate);

    long onTimeCount = 0;
    long lateCount = 0;
    if (agg != null) {
        onTimeCount = agg[0] != null ? ((Number) agg[0]).longValue() : 0L;
        lateCount = agg[1] != null ? ((Number) agg[1]).longValue() : 0L;
    }

    long presentCount = onTimeCount + lateCount;
    // Untuk sementara, absentCount tidak langsung bisa dihitung tanpa data jadwal (berapa hari efektif)
    long absentCount = 0; // dapat diisi di bab lanjutan dengan mengacu ke jadwal & hari efektif

    return StudentAttendanceSummary.builder()
            .studentId(student.getId())
            .nis(student.getNis())
            .studentName(student.getName())
            .classroomName(student.getClassRoom() != null ? student.getClassRoom().getName() : null)
            .startDate(startDate.toString())
            .endDate(endDate.toString())
            .presentCount(presentCount)
            .lateCount(lateCount)
            .absentCount(absentCount)
            .build();
}
```

---

## 6. Controller Reporting

### 6.1 `ReportController`

```java
package com.sekolah.presensi.reporting.controller;

import com.sekolah.presensi.reporting.dto.*;
import com.sekolah.presensi.reporting.service.AttendanceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final AttendanceReportService attendanceReportService;

    @GetMapping("/attendance/daily/classroom/{classroomId}")
    public ResponseEntity<DailyClassAttendanceSummary> getDailyByClassroom(
            @PathVariable Long classroomId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        DailyClassAttendanceSummary summary =
                attendanceReportService.getDailySummaryByClassroom(classroomId, date);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/attendance/student")
    public ResponseEntity<StudentAttendanceSummary> getStudentSummary(
            @RequestParam("nis") String nis,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        StudentAttendanceSummary summary =
                attendanceReportService.getStudentSummaryByNis(nis, startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}
```

---

## 7. Contoh Pemanggilan untuk Dashboard

### 7.1 Rekap Harian Kelas XI SIJA 2

`GET /api/report/attendance/daily/classroom/1?date=2025-11-17`

Response:

```json
{
  "classroomId": 1,
  "classroomName": "XI SIJA 2",
  "date": "2025-11-17",
  "totalStudents": 32,
  "presentCount": 30,
  "lateCount": 5,
  "absentCount": 2
}
```

Frontend bisa menampilkan:

- Pie chart: Hadir vs Terlambat vs Alfa
    
- Bar chart: per kelas per hari
    

### 7.2 Rekap Per Siswa

`GET /api/report/attendance/student?nis=12345&startDate=2025-11-01&endDate=2025-11-30`

Response:

```json
{
  "studentId": 12,
  "nis": "12345",
  "studentName": "Ahmad Fikri",
  "classroomName": "XI SIJA 2",
  "startDate": "2025-11-01",
  "endDate": "2025-11-30",
  "presentCount": 20,
  "lateCount": 3,
  "absentCount": 0
}
```

Ini bisa ditampilkan:

- Di dashboard wali kelas
    
- Di aplikasi orang tua (untuk memantau kehadiran anak)
    

---

## 8. Penutup Bab Reporting & Dashboard

Dengan modul reporting ini, sistem:

- Tidak hanya **mencatat** presensi, tapi juga **memberi informasi yang bermakna**.
    
- Siap dihubungkan ke:
    
    - Dashboard React/Vue
        
    - Laporan cetak (export Excel/PDF)
        
    - Integrasi dengan sistem nilai / e-Raport
        

---

Yuk kita lanjut dua bab terakhir ini. Saya buat tetap nyambung dengan backend yang sudah jadi: endpoint reporting, attendance, security & JWT, dll.

---

# **BAB – Desain Frontend Dashboard Presensi Sekolah**

_(Contoh dengan React + Chart.js, dan sekilas alternatif Blade + AdminLTE)_

## 1. Tujuan Bab

Backend SIM Presensi sudah:

- Punya endpoint presensi (`/api/attendance/*`)
    
- Punya reporting:
    
    - `/api/report/attendance/daily/classroom/{classroomId}`
        
    - `/api/report/attendance/student?nis=...`
        

Sekarang waktunya membuat **frontend dashboard** yang:

- Bisa menampilkan **ringkasan kehadiran**
    
- Mudah dipakai **kepala sekolah, waka kurikulum, wali kelas, guru**
    
- Cukup ringan untuk dijalankan di **lab sekolah** atau browser HP
    

Di bab ini kita akan:

1. Mendesain **struktur tampilan dashboard** (role admin/wali kelas/siswa).
    
2. Membuat **kerangka aplikasi React** yang memanggil endpoint backend.
    
3. Menunjukkan contoh **halaman dashboard utama** dengan Chart.js.
    
4. Memberikan sekilas alternatif: **Blade + AdminLTE** untuk yang masih nyaman dengan Laravel/PHP.
    

---

## 2. Desain UX Dashboard (Peran Pengguna)

### 2.1 Role & Kebutuhan Tampilan

1. **Admin / TU / Waka Kurikulum**
    
    - Melihat total kehadiran per hari, per kelas.
        
    - Mengetahui kelas dengan kehadiran terendah.
        
    - Mengelola master data (siswa, guru, kelas).
        
2. **Wali Kelas**
    
    - Fokus pada kelas tertentu (misalnya XI SIJA 2).
        
    - Lihat rekap harian/mingguan kelasnya.
        
    - Lihat siapa yang sering terlambat.
        
3. **Siswa / Orang Tua**
    
    - Lihat histori kehadiran pribadi.
        
    - Rekap hadir/terlambat/alfa per bulan.
        

### 2.2 Struktur Menu Frontend (React)

Contoh struktur menu:

- **Dashboard**
    
    - Ringkasan hari ini
        
    - Grafik kehadiran per kelas
        
- **Kelas & Siswa**
    
    - Daftar kelas
        
    - Detail kelas (rekap per hari)
        
    - Daftar siswa & detail
        
- **Laporan**
    
    - Rekap harian per kelas
        
    - Rekap per siswa (range tanggal)
        
- **Pengaturan**
    
    - Profil user
        
    - (Opsional) Manajemen user (untuk ADMIN)
        

Secara routing (React Router):

```text
/                    -> Dashboard utama
/classrooms          -> Daftar kelas
/classrooms/:id      -> Detail kelas + rekap
/students/:nis       -> Detail siswa + rekap
/reports/daily       -> Filter & tampilan rekap harian
/profile             -> Profil user
```

---

## 3. Struktur Proyek React

Misalnya kita buat proyek dengan:

```bash
npx create-react-app presensi-dashboard
cd presensi-dashboard
npm install axios chart.js react-chartjs-2
```

Struktur folder:

```text
src/
 ├─ api/
 │   ├─ axiosClient.js
 │   └─ reportApi.js
 ├─ components/
 │   ├─ Layout/
 │   │   ├─ Sidebar.js
 │   │   └─ Topbar.js
 │   └─ charts/
 │       ├─ AttendancePieChart.js
 │       └─ AttendanceBarChart.js
 ├─ pages/
 │   ├─ DashboardPage.js
 │   ├─ ClassroomDailyReportPage.js
 │   ├─ StudentSummaryPage.js
 │   └─ LoginPage.js
 ├─ App.js
 ├─ index.js
 └─ ...
```

---

## 4. Koneksi ke Backend: Axios Client

### 4.1 Konfigurasi `axiosClient`

```javascript
// src/api/axiosClient.js
import axios from "axios";

const axiosClient = axios.create({
  baseURL: "http://192.168.4.10:8080", // IP server Spring Boot di sekolah
});

// Tambahkan interceptor untuk JWT
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("auth_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default axiosClient;
```

### 4.2 Wrapper untuk Endpoint Reporting

```javascript
// src/api/reportApi.js
import axiosClient from "./axiosClient";

const reportApi = {
  getDailyByClassroom: (classroomId, date) =>
    axiosClient.get(`/api/report/attendance/daily/classroom/${classroomId}`, {
      params: { date },
    }),

  getStudentSummary: (nis, startDate, endDate) =>
    axiosClient.get("/api/report/attendance/student", {
      params: { nis, startDate, endDate },
    }),
};

export default reportApi;
```

---

## 5. Komponen Chart: Pie & Bar (react-chartjs-2)

### 5.1 Komponen `AttendancePieChart`

```javascript
// src/components/charts/AttendancePieChart.js
import { Pie } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";

ChartJS.register(ArcElement, Tooltip, Legend);

export default function AttendancePieChart({ present, late, absent }) {
  const data = {
    labels: ["Hadir", "Terlambat", "Alfa"],
    datasets: [
      {
        data: [present, late, absent],
        // warna boleh default, Chart.js akan mengisi sendiri jika tidak diset
      },
    ],
  };

  return <Pie data={data} />;
}
```

### 5.2 Komponen `AttendanceBarChart` (opsional)

```javascript
// src/components/charts/AttendanceBarChart.js
import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

export default function AttendanceBarChart({ labels, presentData }) {
  const data = {
    labels,
    datasets: [
      {
        label: "Jumlah Hadir",
        data: presentData,
      },
    ],
  };

  return <Bar data={data} />;
}
```

---

## 6. Halaman Dashboard Utama

Tujuan: menampilkan ringkasan **kelas tertentu** (misalnya kelas wali yang sedang login) atau kelas yang dipilih.

### 6.1 Contoh `DashboardPage`

```javascript
// src/pages/DashboardPage.js
import { useEffect, useState } from "react";
import reportApi from "../api/reportApi";
import AttendancePieChart from "../components/charts/AttendancePieChart";

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);

  // contoh: classroomId hard-coded (nanti bisa diambil dari user profile)
  const classroomId = 1;
  const today = new Date().toISOString().slice(0, 10); // "YYYY-MM-DD"

  useEffect(() => {
    async function fetchSummary() {
      try {
        setLoading(true);
        const res = await reportApi.getDailyByClassroom(classroomId, today);
        setSummary(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchSummary();
  }, [classroomId, today]);

  if (loading) return <p>Memuat data...</p>;
  if (!summary) return <p>Tidak ada data.</p>;

  return (
    <div>
      <h1>Dashboard Presensi</h1>
      <h2>
        {summary.classroomName} – {summary.date}
      </h2>

      <div style={{ maxWidth: "400px" }}>
        <AttendancePieChart
          present={summary.presentCount}
          late={summary.lateCount}
          absent={summary.absentCount}
        />
      </div>

      <ul>
        <li>Total siswa: {summary.totalStudents}</li>
        <li>Hadir: {summary.presentCount}</li>
        <li>Terlambat: {summary.lateCount}</li>
        <li>Alfa: {summary.absentCount}</li>
      </ul>
    </div>
  );
}
```

Dengan ini, begitu backend terisi data presensi, dashboard langsung bisa menampilkan **pie chart kehadiran** per kelas per hari.

---

## 7. Halaman Rekap Per Siswa

### 7.1 `StudentSummaryPage`

```javascript
// src/pages/StudentSummaryPage.js
import { useState } from "react";
import reportApi from "../api/reportApi";

export default function StudentSummaryPage() {
  const [nis, setNis] = useState("");
  const [startDate, setStartDate] = useState("2025-11-01");
  const [endDate, setEndDate] = useState("2025-11-30");
  const [summary, setSummary] = useState(null);

  const handleSearch = async () => {
    try {
      const res = await reportApi.getStudentSummary(nis, startDate, endDate);
      setSummary(res.data);
    } catch (e) {
      console.error(e);
      alert("Gagal mengambil data. Periksa NIS dan tanggal.");
    }
  };

  return (
    <div>
      <h1>Rekap Presensi Siswa</h1>
      <div>
        <label>NIS:</label>
        <input value={nis} onChange={(e) => setNis(e.target.value)} />
      </div>
      <div>
        <label>Mulai:</label>
        <input
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
        />
      </div>
      <div>
        <label>Sampai:</label>
        <input
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
        />
      </div>
      <button onClick={handleSearch}>Tampilkan</button>

      {summary && (
        <div style={{ marginTop: "20px" }}>
          <h2>
            {summary.studentName} ({summary.nis}) – {summary.classroomName}
          </h2>
          <p>
            Periode: {summary.startDate} s/d {summary.endDate}
          </p>
          <ul>
            <li>Hadir: {summary.presentCount}</li>
            <li>Terlambat: {summary.lateCount}</li>
            <li>Alfa: {summary.absentCount}</li>
          </ul>
        </div>
      )}
    </div>
  );
}
```

---

## 8. Alternatif: Blade + AdminLTE

Kalau sekolah sudah nyaman dengan Laravel/PHP:

- Laravel bertindak sebagai **frontend + gateway**.
    
- Spring Boot tetap menjadi backend presensi (REST API) di belakang.
    

Pattern:

1. Laravel route → Controller Laravel.
    
2. Controller Laravel memanggil REST API Spring Boot dengan Guzzle/HTTP client.
    
3. Data ditampilkan di view Blade + AdminLTE.
    

Keuntungan:

- Guru yang sudah terbiasa dengan tampilan AdminLTE akan cepat adaptasi.
    
- Bisa digabung dengan modul lain (nilai, jadwal) yang sudah ada di Laravel.
    

Kerugian:

- Ada dua backend (Laravel & Spring Boot) → lebih kompleks.
    
- Tapi untuk transisi pelan-pelan, ini cukup realistis di lingkungan sekolah.
    

---

# **BAB – Deployment & DevOps untuk Spring Boot + Face Service + Database**

## 1. Tujuan Bab

Sistem kita sekarang terdiri dari beberapa komponen:

1. **Spring Boot Backend** (REST API, Security, Attendance, Reporting).
    
2. **Database** (PostgreSQL/MySQL).
    
3. (Opsional tapi disarankan) **Python Face Service** (FastAPI/Flask).
    
4. **Frontend Dashboard** (React static build atau Laravel/Blade).
    

Bab ini menjelaskan:

- Opsi arsitektur deployment di lingkungan sekolah / VPS.
    
- Langkah umum deploy Spring Boot ke server (Ubuntu Server).
    
- Menjalankan database dengan aman.
    
- Menjalankan Face Service Python.
    
- Konfigurasi reverse proxy Nginx (HTTPS, domain).
    
- Otomasi dasar: systemd service, backup, logging.
    

---

## 2. Arsitektur Deployment

### 2.1 Skenario On-Premise (Server di Sekolah)

Cocok untuk:

- Jaringan LAN kuat (lab, guru, TU).
    
- Akses utama dari dalam sekolah.
    

Skema:

```text
[Client (Browser / ESP32 / HP)] -- LAN --> [Server Sekolah]
                                         ├─ Spring Boot (port 8080)
                                         ├─ PostgreSQL (port 5432)
                                         └─ Face Service Python (port 8000)
```

### 2.2 Skenario VPS (Cloud)

Cocok jika:

- Ingin akses dari luar (orang tua dari rumah).
    
- Bandwidth sekolah terbatas.
    

Skema mirip, hanya servernya di internet, dengan tambahan:

- **Nginx reverse proxy** di port 80/443.
    
- Domain, misal: `presensi.smkn1punggelan.sch.id`.
    

---

## 3. Persiapan Server (Ubuntu)

Langkah umum (ringkas):

1. **Update sistem**
    

```bash
sudo apt update && sudo apt upgrade -y
```

2. **Install JDK (misal OpenJDK 17)**
    

```bash
sudo apt install -y openjdk-17-jdk
java -version
```

3. **Install Database (PostgreSQL)**
    

```bash
sudo apt install -y postgresql postgresql-contrib
sudo -u postgres psql
```

Di dalam `psql`:

```sql
CREATE DATABASE presensi_smk;
CREATE USER presensi_user WITH ENCRYPTED PASSWORD 'passwordku';
GRANT ALL PRIVILEGES ON DATABASE presensi_smk TO presensi_user;
\q
```

4. **Install Python & pip (untuk Face Service)**
    

```bash
sudo apt install -y python3 python3-pip python3-venv
```

5. **Install Nginx (untuk reverse proxy & HTTPS)**
    

```bash
sudo apt install -y nginx
```

---

## 4. Deploy Spring Boot

### 4.1 Build Aplikasi

Di laptop/PC development:

```bash
mvn clean package
```

Hasilnya: `target/presensi-backend-0.0.1-SNAPSHOT.jar` (nama contoh).

Copy ke server (pakai `scp` atau `rsync`):

```bash
scp target/presensi-backend-0.0.1-SNAPSHOT.jar user@server:/opt/presensi/
```

### 4.2 File Konfigurasi `application-prod.yml`

Buat di server (atau bundling), misalnya:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/presensi_smk
    username: presensi_user
    password: passwordku
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

jwt:
  secret: "ini-rahasia-jwt-sim-presensi-smk-1234567890"
```

Jalankan dengan profile `prod`:

```bash
java -jar presensi-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 4.3 Menjadikan Layanan systemd

Buat file service:

```bash
sudo nano /etc/systemd/system/presensi-backend.service
```

Isi:

```ini
[Unit]
Description=Presensi Sekolah Spring Boot Backend
After=network.target

[Service]
User=www-data
WorkingDirectory=/opt/presensi
ExecStart=/usr/bin/java -jar /opt/presensi/presensi-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Lalu:

```bash
sudo systemctl daemon-reload
sudo systemctl enable presensi-backend
sudo systemctl start presensi-backend
sudo systemctl status presensi-backend
```

Sekarang backend otomatis jalan saat server boot.

---

## 5. Deploy Face Service Python

### 5.1 Setup Virtualenv

```bash
sudo mkdir -p /opt/face-service
sudo chown $USER:$USER /opt/face-service
cd /opt/face-service

python3 -m venv venv
source venv/bin/activate
pip install fastapi uvicorn
# plus library face recognition jika diperlukan
```

Salin file `main.py` (service FastAPI) ke `/opt/face-service/main.py`.

### 5.2 Menjalankan dengan Uvicorn

Tes manual:

```bash
source venv/bin/activate
uvicorn main:app --host 0.0.0.0 --port 8000
```

### 5.3 Buat systemd service

```bash
sudo nano /etc/systemd/system/face-service.service
```

Isi:

```ini
[Unit]
Description=Face Recognition Service (FastAPI)
After=network.target

[Service]
User=www-data
WorkingDirectory=/opt/face-service
Environment="PATH=/opt/face-service/venv/bin"
ExecStart=/opt/face-service/venv/bin/uvicorn main:app --host 0.0.0.0 --port 8000
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Aktifkan:

```bash
sudo systemctl daemon-reload
sudo systemctl enable face-service
sudo systemctl start face-service
sudo systemctl status face-service
```

---

## 6. Konfigurasi Nginx sebagai Reverse Proxy

Untuk skenario publik (atau hanya agar port “cantik”):

### 6.1 Basic HTTP Reverse Proxy

```bash
sudo nano /etc/nginx/sites-available/presensi
```

Isi:

```nginx
server {
    listen 80;
    server_name presensi.smkn1punggelan.sch.id;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Jika React build di-serve oleh Nginx
    location / {
        root /var/www/presensi-frontend;
        try_files $uri /index.html;
    }
}
```

Aktifkan:

```bash
sudo ln -s /etc/nginx/sites-available/presensi /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 6.2 HTTPS dengan Let’s Encrypt (untuk VPS / domain publik)

Jika domain sudah mengarah ke server dan port 80 terbuka:

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d presensi.smkn1punggelan.sch.id
```

Certbot akan mengkonfigurasi SSL dan auto-renew.

---

## 7. Deploy Frontend React

### 7.1 Build Produksi

Di folder proyek React:

```bash
npm run build
```

Hasil: `build/` berisi file static (HTML, JS, CSS).

Copy ke server:

```bash
scp -r build/* user@server:/var/www/presensi-frontend/
```

Pastikan Nginx `root /var/www/presensi-frontend;` sudah diatur seperti di atas.

---

## 8. Monitoring, Log, dan Backup

### 8.1 Log Aplikasi

- Log Spring Boot diakses via:
    
    ```bash
    journalctl -u presensi-backend -f
    ```
    
- Log Face Service:
    
    ```bash
    journalctl -u face-service -f
    ```
    

### 8.2 Backup Database

Contoh backup PostgreSQL harian sederhana (cron):

```bash
sudo nano /usr/local/bin/backup-presensi.sh
```

Isi:

```bash
#!/bin/bash
DATE=$(date +%F-%H-%M)
pg_dump -U presensi_user presensi_smk > /var/backups/presensi_smk_$DATE.sql
```

Beri izin eksekusi:

```bash
sudo chmod +x /usr/local/bin/backup-presensi.sh
```

Tambahkan ke cron:

```bash
sudo crontab -e
```

Isi:

```cron
0 1 * * * /usr/local/bin/backup-presensi.sh
```

Artinya: backup tiap jam 01.00 pagi.

---

## 9. Penutup Bab Deployment & DevOps

Setelah bab ini:

- Anda punya **gambaran lengkap** bagaimana:
    
    - Spring Boot backend
        
    - Face Recognition service (Python)
        
    - Database
        
    - Frontend dashboard
        
    
    bisa hidup bersama di **satu server** atau VPS.
    
- Anda sudah punya pola:
    
    - `systemd` untuk membuat service jalan otomatis.
        
    - `nginx` untuk reverse proxy & domain.
        
    - Backup sederhana untuk database.
        

Ini sudah cukup **production-like** untuk lingkungan:

- SMK dengan lab/server Proxmox, atau
    
- Sekolah yang punya **satu VPS** untuk sistem informasi.
    

---

bab-bab _bonus_ misalnya:

- Bab: **“Mikrotik + VPN untuk Akses Aman ke Dashboard Presensi dari Luar Sekolah”**
    
- Bab: **“CI/CD sederhana (GitHub Actions) untuk Spring Boot & React”**
    
- Bab: **“Pengujian (Unit Test & Integration Test) untuk Modul Attendance”**.