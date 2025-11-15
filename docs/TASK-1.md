# TASK 1 – Backend Skeleton (Tahap 01)

Dokumen ini memandu siswa sangat pemula agar memahami **apa yang harus dibuat terlebih dahulu** pada Tahap 01. Bacalah berurutan dan tandai checklist setelah sukses.

---

## 1. Kenapa Kita Mulai dari Spring Boot + REST API?

1. **Spring Boot mempermudah**: kita tidak perlu mengatur server Java secara manual; cukup jalankan `mvn spring-boot:run`, aplikasi API langsung hidup.
2. **REST API itu bahasa universal**: mobile Android, desktop JavaFX, bahkan mesin RFID bisa berbicara lewat HTTP (GET/POST). Jadi sebelum ke hardware, kita pastikan "otak" backendnya siap.
3. **Skeleton = pondasi**: dengan struktur paket (`controller`, `service`, dll) sejak awal, tahap berikutnya tinggal menambah fitur tanpa merombak ulang.

> Analogi: bikin rumah dimulai dari pondasi dan jalur listrik/air. REST API adalah jalur komunikasi bagi semua device di proyek presensi.

---

## 2. Target Task

- Menjalankan proyek di folder `backend/` hingga endpoint `/api/hello` dan `/api/info` muncul di browser/Postman.
- Memahami baris kode utama agar tidak hanya menyalin.
- Menulis catatan kecil (misal di buku/LKPD) bahwa Tahap 01 sudah selesai.

Checklist awal:
- [ ] Branch `tahap-01-backend-skeleton` sudah dibuat dari `main`.
- [ ] Perintah `mvn spring-boot:run` berjalan tanpa error.
- [ ] `GET /api/hello` dan `/api/info` mengembalikan JSON sesuai instruksi.

---

## 3. Persiapan Tools

1. **Java 17** atau lebih baru.
2. **Maven** (sudah disediakan via wrapper nanti, tapi di lab biasanya sudah ada).
3. Editor: IntelliJ IDEA / VS Code.
4. Postman atau browser untuk mengetes endpoint.

---

## 4. Langkah Kerja Detail

### Langkah 1 – Checkout Branch Tahap 1

```powershell
git checkout -b tahap-01-backend-skeleton
```

> Kenapa? Supaya pekerjaan Tahap 01 terpisah. Setelah lulus, branch ini di-merge ke `main`, lalu kita buat branch baru untuk Tahap 02.

### Langkah 2 – Jalankan Proyek

1. Buka terminal di folder `backend/`.
2. Jalankan:

   ```powershell
   mvn spring-boot:run
   ```

3. Tunggu sampai muncul log `Started PresensiApplication`. Server siap di `http://localhost:8080`.

### Langkah 3 – Tes Endpoint

1. Buka browser atau Postman.
2. Akses `http://localhost:8081/api/hello` → harus muncul `{"message":"Presensi SMK – Tahap 1"}`.
3. Akses `http://localhost:8081/api/info` → muncul metadata aplikasi.
4. Jika sukses, hentikan server dengan `Ctrl + C` di terminal.

---

## 5. Penjelasan Struktur Folder

```
backend/
├─ pom.xml                 --> Konfigurasi Maven & dependency Spring Boot
├─ src/main/java/...       --> Kode utama aplikasi
│  └─ PresensiApplication  --> Titik masuk (main method)
├─ src/main/resources/     --> File konfigurasi (application.properties)
└─ src/test/java/...       --> Unit test dasar
```

- **pom.xml**: mendeklarasikan library yang digunakan (starter web, validation, test). Spring Boot versi 3.2.5 dipakai karena stabil & modern.
- **application.properties**: saat ini hanya memberi nama aplikasi (`spring.application.name`).

---

## 6. Penjelasan Baris Kode (Step-by-Step)

### 6.1 `PresensiApplication.java`

```java
@SpringBootApplication
public class PresensiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PresensiApplication.class, args);
    }
}
```

| Baris | Penjelasan |
|-------|------------|
| `@SpringBootApplication` | Mengaktifkan auto-configuration dan component scan. Artinya Spring mencari controller/service secara otomatis. |
| `public class PresensiApplication` | Kelas utama proyek. |
| `public static void main...` | Titik start aplikasi Java. Saat dijalankan, Spring Boot menyalakan server embedded (Tomcat) di port 8080. |
| `SpringApplication.run...` | Perintah untuk menjalankan Spring Boot beserta semua konfigurasi defaultnya. |

### 6.2 `HelloController.java`

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

| Bagian | Penjelasan |
|--------|------------|
| `@RestController` | Kelas ini melayani HTTP request dan otomatis mengubah return value jadi JSON. |
| `@RequestMapping("/api")` | Semua endpoint di kelas ini dimulai dengan `/api`. |
| `@GetMapping("/hello")` | Mendefinisikan URL `GET /api/hello`. |
| `return Map.of(...)` | Mengirim JSON sederhana berisi pesan. `Map.of` cepat untuk data kecil. |
| `AppInfo record` | `record` adalah fitur Java untuk membuat class data singkat. Dipakai agar response `/api/info` punya struktur jelas (`app`, `version`, `developer`). |

> Siswa boleh mengganti nilai `developer` dengan nama kelompoknya.

---

## 7. Tugas Siswa Setelah Membaca

1. **Catat** di buku atau file catatan: apa itu Spring Boot, apa itu REST API (versi kalian sendiri).
2. **Modifikasi** `HelloController` supaya `/api/info` menampilkan data sekolah sendiri (misal versi `0.1-sija`).
3. **Screenshot** hasil request `/api/hello` dan `/api/info` sebagai bukti.
4. **Commit** perubahan dengan pesan `feat: selesai tahap 01 backend skeleton`.

---

## 8. FAQ Singkat

**Q: Kenapa pakai JSON?**  
A: JSON mudah dibaca manusia & mesin. Android/desktop juga mudah parsing JSON.

**Q: Kalau error merah di terminal?**  
1. Pastikan Java 17 terpasang (`java -version`).
2. Cek apakah sudah di folder `backend/` saat menjalankan Maven.
3. Jika masih error, baca log dari atas (biasanya ada baris "Caused by").

---

Setelah semua checklist terpenuhi, Tahap 01 dianggap selesai dan kalian siap lanjut ke Tahap 02 (membuat entity & CRUD).

1. **Biar paham prosesnya** – Dengan menjalankan `mvn spring-boot:run` dan melihat struktur folder secara lokal, siswa tahu apa saja yang dihasilkan (mirip gaya belajar [in28minutes/spring-boot-master-class](https://github.com/in28minutes/spring-boot-master-class) yang menekankan praktek tahap demi tahap).
2. **Stabil di lab sekolah** – Kadang internet terbatas. Jika semua file sudah ada di repo, tidak perlu koneksi lagi untuk generate proyek baru.
3. **Konsisten antar tahap** – Guru cukup menyiapkan branch tiap tahap; siswa tinggal checkout tanpa takut versi Spring/Java berubah karena template online diperbarui.

> Catatan: Spring Initializr web tetap bagus untuk eksplorasi pribadi. Di kelas, kita pilih workflow yang mudah dikontrol dan bisa diulang kapan pun.

## 10. File yang Harus Ada Setelah Tahap 1

Gunakan daftar ini saat mengecek repo masing-masing:

- `backend/pom.xml`
- `backend/.gitignore`
- `backend/src/main/java/com/smk/presensi/PresensiApplication.java`
- `backend/src/main/java/com/smk/presensi/controller/HelloController.java`
- `backend/src/main/resources/application.properties`
- `backend/src/test/java/com/smk/presensi/PresensiApplicationTests.java`

Tambahan opsional (jika guru menyediakan): `mvnw`, `mvnw.cmd`, dan folder `.mvn/` untuk Maven Wrapper.
