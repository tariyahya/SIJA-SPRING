# Membangun Sistem Presensi Siswa & Guru: Tahap 1 – Pondasi Backend dengan Spring Boot

Halo teman-teman siswa SMK jurusan Sistem Informasi Jaringan dan Aplikasi (SIJA)! Hari ini kita mulai petualangan besar: membangun sistem presensi yang canggih menggunakan RFID, barcode, geolocation, dan bahkan foto wajah. Tapi jangan khawatir, kita mulai dari dasar-dasarnya. Di artikel ini, kita bahas **Tahap 1: Backend Skeleton**, di mana kita siapkan pondasi API menggunakan Spring Boot dan REST API.

## Kenapa Kita Mulai dari Backend?

Bayangkan rumah yang sedang dibangun. Kita tidak langsung pasang atap atau cat dinding, kan? Kita mulai dari fondasi dan struktur dasar. Begitu juga di sini. Backend adalah "otak" sistem presensi—tempat semua data disimpan dan diproses. Dengan REST API, backend bisa "berbicara" dengan aplikasi mobile (Android) dan desktop (JavaFX) yang akan kita buat nanti.

Spring Boot memudahkan kita karena tidak perlu ribet setup server manual. Cukup jalankan satu perintah, dan API sudah hidup! Ini cocok untuk siswa awam seperti kalian, karena fokusnya pada logika bisnis, bukan konfigurasi rumit.

## File yang Sudah Dibuat di Proyek Ini

Sebelum kita mulai coding, proyek ini sudah disiapkan dengan struktur lengkap. Berikut file-file utama yang sudah ada (kalian bisa lihat di repo GitHub):

- **Root Level**:
  - `README.md`: Overview proyek monorepo, struktur folder, dan tahapan belajar.
  - `PLAN.MD`: Rencana detail semua tahap, ERD, flow proses, dan strategi Git.

- **Backend (`backend/`)**:
  - `pom.xml`: Konfigurasi Maven dengan Spring Boot 3.2.5.
  - `src/main/java/com/smk/presensi/PresensiApplication.java`: Titik masuk aplikasi.
  - `src/main/java/com/smk/presensi/controller/HelloController.java`: Endpoint `/api/hello` dan `/api/info`.
  - `src/main/resources/application.properties`: Konfigurasi port 8081.
  - `src/test/java/com/smk/presensi/PresensiApplicationTests.java`: Test dasar.
  - `.gitignore`: Mengabaikan file build dan IDE.

- **Dokumentasi (`docs/`)**:
  - `ERD.md`: Diagram relasi tabel (ROLE, USER, SISWA, dll.).
  - `Flows.md`: Alur proses presensi (login, RFID, barcode, dll.).
  - `GitStrategy.md`: Cara menggunakan GitHub untuk pengajaran.
  - `Levels.md`: Daftar tahap dengan checklist.
  - `TASK-1.md`: Panduan detail Tahap 1 untuk siswa.
  - `blog1.md`: Artikel ini (untuk blog).

- **Mobile & Desktop** (struktur kosong siap diisi):
  - `mobile-app/README.md`: Panduan Android.
  - `desktop-app/README.md`: Panduan JavaFX.

Dengan file-file ini, kalian tidak perlu mulai dari nol. Guru sudah siapkan pondasi, kalian tinggal tambah fitur per tahap!

## Apa yang Kita Buat di Tahap 1?

Di tahap ini, kita buat proyek Spring Boot sederhana dengan dua endpoint dasar:
- `GET /api/hello`: Mengembalikan pesan sederhana.
- `GET /api/info`: Memberikan info aplikasi.

Tujuannya: Pastikan Spring Boot berjalan lancar, dan kalian paham struktur folder serta kode dasar. Setelah ini, kita lanjut ke entity (model data) dan CRUD di Tahap 2.

## Langkah Demi Langkah: Dari Nol ke API Hidup

### 1. Persiapan Tools

Pastikan kalian punya:
- Java 17 atau lebih baru (cek dengan `java -version`).
- Maven (biasanya sudah ada di lab sekolah).
- Editor seperti IntelliJ IDEA atau VS Code.

### 2. Alternatif: Generate Proyek Sendiri via Spring Initializr

Jika kalian ingin tahu cara membuat proyek Spring Boot dari nol (untuk latihan atau eksplorasi pribadi), ikuti langkah ini. Di proyek ini, file-file sudah disiapkan, jadi kalian bisa langsung pakai yang ada. Tapi ini bagus untuk paham prosesnya.

1. Buka browser dan akses [https://start.spring.io/](https://start.spring.io/).
2. Pilih opsi:
   - **Project**: Maven Project.
   - **Language**: Java.
   - **Spring Boot**: 3.2.5 (atau versi terbaru).
   - **Group**: `com.smk.presensi`.
   - **Artifact**: `presensi-backend`.
   - **Name**: `presensi-backend`.
   - **Description**: Sistem presensi siswa & guru - backend Spring Boot.
   - **Package name**: `com.smk.presensi`.
   - **Packaging**: Jar.
   - **Java**: 17.
3. Di bagian **Dependencies**, cari dan tambah:
   - Spring Web (untuk REST API).
   - Spring Boot DevTools (opsional, untuk reload otomatis saat development).
4. Klik **Generate** → Download file ZIP.
5. Ekstrak ZIP ke folder `backend/` di proyek kalian.
6. Buka di editor, lalu lanjut ke langkah berikutnya.

> Catatan: Spring Initializr akan generate file `pom.xml`, `PresensiApplication.java`, dll. yang mirip dengan yang sudah ada. Kita pilih cara manual di proyek ini agar lebih terkendali untuk kelas.

## 3. Struktur Proyek

Kita gunakan Maven untuk manage dependency. File `pom.xml` mendeklarasikan Spring Boot versi 3.2.5 dan library web. Struktur foldernya seperti ini:

```
backend/
├─ pom.xml
├─ src/main/java/com/smk/presensi/
│  ├─ PresensiApplication.java  # Titik masuk aplikasi
│  └─ controller/HelloController.java  # Endpoint API
├─ src/main/resources/application.properties  # Konfigurasi
└─ src/test/java/...  # Test dasar
```

### 3. Kode Utama: PresensiApplication.java

Ini adalah pintu gerbang aplikasi. Tanpa ini, Spring Boot tidak bisa start.

```java
@SpringBootApplication
public class PresensiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PresensiApplication.class, args);
    }
}
```

Penjelasan:
- `@SpringBootApplication`: Anotasi ini mengaktifkan auto-configuration. Spring otomatis cari controller, service, dll.
- `main` method: Saat dijalankan, ini menyalakan server embedded (Tomcat) di port 8081.
- Mengapa penting? Ini membuat aplikasi Java bisa berjalan sebagai web server tanpa setup tambahan.

### 4. Controller: HelloController.java

Controller adalah tempat kita definisikan endpoint API. Ini seperti pintu rumah—request dari luar masuk sini.

```java
@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Presensi SMK – Tahap 1");
    }

    @GetMapping("/info")
    public AppInfo info() {
        return new AppInfo("Presensi Siswa & Guru", "0.1", "Nama_Kelompok");
    }

    public record AppInfo(String app, String version, String developer) {
    }
}
```

Penjelasan baris per baris:
- `@RestController`: Kelas ini menangani HTTP request dan otomatis kirim response sebagai JSON.
- `@RequestMapping("/api")`: Semua endpoint di kelas ini dimulai dengan `/api`.
- `@GetMapping("/hello")`: Mendefinisikan URL `GET /api/hello`. Saat diakses, method `hello()` dipanggil.
- `return Map.of(...)`: Mengirim data sederhana dalam format JSON. `Map.of` adalah cara cepat buat map di Java modern.
- `AppInfo record`: `record` adalah fitur Java baru untuk class data ringkas. Ini membuat response `/api/info` terstruktur tanpa boilerplate code.
- Mengapa ini keren? REST API memungkinkan komunikasi universal—mobile atau desktop tinggal panggil URL ini.

### 5. Konfigurasi: application.properties

File ini menyimpan setting aplikasi.

```properties
spring.application.name=presensi-backend
server.port=8081
```

- `spring.application.name`: Nama aplikasi untuk logging.
- `server.port=8081`: Port tempat server berjalan (diubah dari default 8080 agar tidak konflik).

### 6. File Pendukung Lain

Selain kode utama, ada file penting yang mendukung proyek:

- **`pom.xml`**: File konfigurasi Maven. Ini mendeklarasikan versi Spring Boot (3.2.5), dependencies seperti Spring Web, dan plugin untuk build. Tanpa ini, Maven tidak tahu cara compile dan run aplikasi.

- **`.gitignore`**: File ini memberitahu Git file mana yang tidak perlu di-track, seperti folder `target/` (hasil build), file IDE (`.idea/`), dan lainnya. Ini menjaga repo bersih.

- **`PresensiApplicationTests.java`**: File test dasar menggunakan JUnit. Method `contextLoads()` memastikan Spring context bisa start tanpa error. Jalankan dengan `mvn test` untuk verifikasi cepat.

File-file ini sudah disiapkan, jadi kalian tinggal fokus pada kode bisnis!

## 7. Menjalankan dan Menguji

1. Buka terminal di folder `backend/`.
2. Jalankan: `mvn spring-boot:run`.
3. Tunggu log "Started PresensiApplication".
4. Buka browser: `http://localhost:8081/api/hello` → Lihat JSON response.
5. Akses `http://localhost:8081/api/info` → Info aplikasi muncul.

Jika error, cek Java version atau port sudah digunakan. Gunakan `mvn spring-boot:run -e` untuk detail error.

## Apa yang Kita Capai di Tahap Ini?

- **Paham Spring Boot**: Kita tahu cara setup proyek cepat tanpa ribet.
- **REST API Dasar**: Endpoint sederhana sudah berjalan, siap dikembangkan.
- **Struktur Kode**: Folder `controller`, `entity` (nanti), dll. sudah ada pola.
- **Motivasi**: Ini langkah pertama menuju sistem presensi lengkap. Kalian sudah bisa bilang, "Saya buat API sendiri!"

## Kesimpulan dan Langkah Selanjutnya

Tahap 1 ini seperti belajar jalan sebelum lari. Kita fokus pada fondasi agar tahap berikutnya (CRUD entity Siswa/Guru) lebih mudah. Jika kalian berhasil jalankan API, beri tahu guru atau screenshot hasilnya!

Di artikel selanjutnya, kita bahas Tahap 2: Membuat model data dan operasi CRUD. Sampai jumpa, dan semangat coding! 🚀

*Artikel ini bagian dari seri "Bangun Sistem Presensi SMK". Jika ada pertanyaan, tulis di komentar atau tanya guru kalian.*

---

*Tags: Spring Boot, REST API, Java, SMK SIJA, Sistem Presensi*