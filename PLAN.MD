Oke, ini keren banget buat dijadiin *capstone* anak SIJA 🔥
Aku bagi jadi dua hal:

1. **Tahapan belajar backend & frontend** (step-by-step, gaya “level game”).
2. **Strategi GitHub** supaya kode rapi dan meminimalisir error saat *build up*.

---

## 1. Struktur Proyek (Gambaran Besar)

Disarankan 1 monorepo GitHub:

```text
presensi-siswa-guru/
├─ backend/         -> Spring Boot (REST API)
├─ mobile-app/      -> Android (Java)
└─ desktop-app/     -> JavaFX (desktop admin / operator)
```

Nanti tiap folder punya tahapan sendiri, tapi sinkron dengan tahapan pembelajaran.

---

## 2. Tahapan Belajar (Backend & Frontend)

Anggap ini untuk ± 10–12 pertemuan besar. Di kelas bisa dipecah lagi jadi beberapa JP.

### Tahap 0 – Persiapan Lingkungan & Git

**Tujuan:** Siswa siap tools dan paham alur kerja Git dasar.

* Install: JDK, Maven/Gradle, IntelliJ/VS Code, Android Studio, SceneBuilder (kalau pakai JavaFX).
* Buat akun GitHub, *clone* repo template dari Bapak/Ibu.
* Belajar:

  * `git clone`, `git checkout`, `git pull`, `git commit`, `git push`.
* **Branch referensi guru:** `tahap-00-setup`

---

### Tahap 1 – Backend Dasar: Spring Boot Skeleton

**Tujuan:** Siswa bisa menjalankan API sederhana.

* Buat project `backend/` dengan Spring Initializr (Web, JPA, H2 / MySQL).
* Endpoint test: `GET /api/hello`.
* Struktur paket:

  * `controller`, `service`, `repository`, `entity`, `dto`.
* **Branch guru:** `tahap-01-backend-skeleton`

> Frontend: belum, cukup Postman / curl dulu.

---

### Tahap 2 – Model Data: Siswa, Guru, Kelas

**Tujuan:** Siswa paham model basis data & CRUD dasar.

* Entity:

  * `Siswa` (nis, nama, kelas, rfid, barcode, faceId, dll)
  * `Guru` (nip, nama, mapel, dll)
  * `Kelas` (nama, waliKelas, dll)
* Buat repository + service + controller:

  * `GET /siswa`, `POST /siswa`, `PUT`, `DELETE`.
* Gunakan DTO + mapping (biar dari awal rapi).
* **Branch guru:** `tahap-02-domain-crud`

> Tugas siswa:
> Tambahkan field/relasi baru (misal: `jurusan`) lalu update endpoint.

---

### Tahap 3 – Auth & Role (Admin, Guru, Siswa) ✅ COMPLETED

**Tujuan:** Siswa paham otentikasi & otorisasi sederhana.

* Tambah `User` + `Role` (ADMIN, GURU, SISWA). ✅
* Login pakai JWT sederhana (`/auth/login`). ✅
* Proteksi endpoint: ✅

  * Admin: manage siswa/guru (CRUD all).
  * Guru: lihat data siswa/guru (read-only).
  * Siswa: no access to siswa/guru endpoints.
* User relationship: OneToOne dengan Siswa/Guru. ✅
* Role-based access control dengan @PreAuthorize. ✅
* **Branch guru:** `tahap-03-auth-role`
* **Status:** ✅ COMPLETE (All Tasks 1-15 done, build SUCCESS)
* **Files created:** 23 files (19 code + 4 docs) = ~8,500 lines total
* **Documentation:**
  - `docs/TASK-3.md` (implementation guide)
  - `docs/blog3.md` (concept explanation with analogies)
  - `docs/POSTMAN-TAHAP-03.md` (API testing guide)
  - `docs/README-TAHAP-03.md` (overview & summary)
  - `docs/COMPLETED-TASK-12-13.md` (Tasks 12-13 completion report)

---

### Tahap 4 – Model Presensi & Flow Manual (Tanpa RFID dkk dulu) ✅ COMPLETED

**Tujuan:** Paham alur presensi dulu, baru masuk hardware.

* Entity `Presensi`: ✅
  * `userId`, `tipe` (SISWA / GURU), `tanggal`, `jamMasuk`, `jamPulang`, `status`, `latitude`, `longitude`, `method` (MANUAL/RFID/QR/FACE).
* Enum: TipeUser, StatusPresensi, MethodPresensi ✅
* Endpoint: ✅
  * `POST /api/presensi/checkin` - User checkin (SISWA, GURU)
  * `POST /api/presensi/checkout` - User checkout (SISWA, GURU)
  * `GET /api/presensi/histori` - View own history
  * `GET /api/presensi` - View all (ADMIN, GURU)
* Validasi: ✅
  * Tidak boleh checkin dua kali per hari
  * Harus checkin dulu sebelum checkout
  * Tidak boleh checkout dua kali
* Status auto-calculate: HADIR vs TERLAMBAT based on jam masuk ✅
* GPS tracking (latitude/longitude) ✅
* Role-based access control ✅
* **Branch guru:** `tahap-04-presensi-basic`
* **Status:** ✅ COMPLETE (11 code files, 3 docs, build SUCCESS)
* **Documentation:**
  - `docs/TASK-4.md` (implementation guide)
  - `docs/POSTMAN-TAHAP-04.md` (API testing)
  - `docs/README-TAHAP-04.md` (overview & summary)

---

### Tahap 5 – Integrasi RFID (Level 1: Simulasi, Level 2: Real Device)

**Tujuan:** Siswa paham bahwa RFID hanyalah pengenal (ID).

**Backend:**

* ✅ Field `rfidCardId` sudah ada di `Siswa` dan `Guru` (dari Tahap 2).
* ✅ Endpoint: `POST /api/presensi/rfid/checkin` dengan body `{ "rfidCardId": "xxxx" }`.
* ✅ Backend mencari data siswa/guru berdasarkan `rfidCardId`.
* ✅ Auto-detect tipe (SISWA/GURU) dari tabel yang ditemukan.
* ✅ Public endpoint (no JWT) - whitelist di SecurityConfig.
* ✅ Method = RFID untuk tracking.

**Simulasi di kelas (tanpa alat dulu):**

* ✅ Input `rfidCardId` manual via Postman/form (simulasi).
* ✅ Nanti bisa diganti dengan input dari hardware RFID reader.

**Branch:** `tahap-05-rfid-basic`
**Status:** ✅ COMPLETE (6 changes, 2 new files, 4 updates, build SUCCESS)
**Documentation:**
- `docs/TASK-5.md` (implementation guide)
- `docs/POSTMAN-TAHAP-05.md` (API testing)
- `docs/README-TAHAP-05.md` (overview & summary)

---

### Tahap 6 – Integrasi Barcode / QR-Code

**Tujuan:** Siswa paham barcode/QR juga cuma ID string.

**Backend:**

* Bisa pakai field `barcodeId` (atau reuse `nis` yang dikodekan jadi QR).
* Endpoint:

  * `POST /presensi/barcode/checkin` body: `{ "barcode": "..." }`.
* Logika sama dengan RFID (cari siswa + buat record presensi).

**Frontend mobile:**

* Integrasi library scanner (ZXing, dsb) → panggil API.
* **Branch guru backend:** `tahap-06-barcode-basic`
* **Branch guru mobile:** `tahap-01-mobile-qr-scanner`

---

### Tahap 7 – Geolocation (Lokasi Saat Presensi)

**Tujuan:** Siswa paham validasi berbasis lokasi.

**Backend:**

* Tambah field `latitude`, `longitude`, `accRadius` di `Presensi`.
* Simpan dari request body.
* Validasi jarak (opsional, advanced):

  * Koordinat sekolah disimpan di tabel `LokasiKantor`.
  * Hitung jarak (rumus Haversine simple).
  * Kalau > batas (misal 200 meter) → tolak presensi.

**Frontend mobile:**

* Ambil GPS (`FusedLocationProviderClient` / API lokasi).

* Kirim lat/long ke endpoint `checkin`.

* **Branch guru backend:** `tahap-07-geolocation`

* **Branch guru mobile:** `tahap-02-mobile-geolocation`

---

### Tahap 8 – Pencocokan Wajah (Konsep & Integrasi)

**Tujuan:** Siswa paham arsitektur, tidak harus “ahli AI”.

Level implementasi bisa bertahap:

1. **Level 1 (Paling mudah untuk SMK):**

   * Frontend mobile kirim foto (atau URL) ke backend.
   * Backend belum benar-benar *face recognition*, hanya simpan foto saat presensi.
   * Fokus: upload file → simpan di storage → simpan path di database.

2. **Level 2 (Lebih advanced, opsional):**

   * Backend panggil service eksternal (API face recognition, atau service Python) untuk:

     * Mendaftarkan wajah (enroll).
     * Mencocokkan wajah pada saat presensi.

**Backend:**

* Endpoint:

  * `POST /presensi/face/checkin` dengan `multipart/form-data` (image + userId + lokasi).
* Simpan file + data presensi.

**Frontend mobile:**

* Ambil foto dengan kamera → kirim ke API.

* **Branch guru backend:** `tahap-08-face-basic-upload`

* **Branch guru mobile:** `tahap-03-mobile-camera-face`

---

### Tahap 9 – Reporting & Dashboard

**Tujuan:** Menarik data & visualisasi.

**Backend:**

* Endpoint:

  * `GET /laporan/presensi/harian?kelas=...`
  * `GET /laporan/presensi/guru?rangeTanggal=...`
* Bisa pakai DTO yang sudah diringkas.

**Frontend desktop (JavaFX):**

* Tampilkan tabel presensi.

* Filter per tanggal, kelas, guru.

* Export CSV (opsional).

* **Branch guru backend:** `tahap-09-reporting`

* **Branch guru desktop:** `tahap-01-desktop-dashboard`

---

### Tahap 10 – Hardening & Deployment Dasar (Opsional)

**Tujuan:** Siswa paham bahwa aplikasi harus siap runtime.

* Konfigurasi profile `dev` vs `prod`.
* `.env` / `application.properties` untuk DB, JWT secret, dll.
* Konsep sederhana deploy di:

  * VPS / Docker (kalau mau sekilas saja).

---

## 3. Pola GitHub untuk Mengajar (Supaya Minim Error)

Supaya rapi dan mudah dipakai untuk pengajaran, saya sarankan pola berikut:

### A. Satu Repo “Master” untuk Guru

Repo: `presensi-siswa-guru-master` (hanya Bapak/Ibu yang commit).

* Branch utama: `main` → berisi **versi akhir** (lengkap).
* Setiap tahap ada branch sendiri, misal:

  * `tahap-00-setup`
  * `tahap-01-backend-skeleton`
  * `tahap-02-domain-crud`
  * ...
* Optional: pakai juga **tag**:

  * `v0.1-setup`, `v0.2-domain-crud`, dst.

**Alur guru:**

1. Kerjakan dulu di branch `dev` pribadi.
2. Kalau sudah stabil → merge ke `tahap-0X-...`.
3. Branch `tahap-0X` **jangan diubah lagi**, supaya siswa selalu punya “checkpoint” yang pasti.

---

### B. Repo Template Khusus Siswa

Buat repo lain: `presensi-siswa-guru-template`.

**Isi:**

* Hanya skeleton awal (misal `tahap-01-backend-skeleton`).
* Atau bisa pakai [GitHub Template Repository] supaya siswa bisa klik “Use this template”.

**Alur siswa:**

1. *Create from template* → repo milik masing-masing siswa/kelompok.
2. Setiap pertemuan:

   * Guru bilang:
     “Hari ini kita mulai dari tahap 03, silakan *compare* repo kalian dengan branch `tahap-03-auth-role` di repo master.”
   * Siswa boleh:

     * *Copy paste* bagian tertentu, atau
     * Belajar *implement* sendiri dengan panduan LKPD.

**Kalau mau lebih rapi lagi:**

* Siswa pakai branch:

  * `tahap-03-nama-siswa`
  * Setelah selesai → merge ke `main` repo mereka sendiri.

---

### C. Tips Tambahan untuk Meminimalisir Error

* **Lock branch `main` di repo guru** (protected branch) → tidak ada yang bisa push sembarangan.
* Selalu tulis **README per tahap**:

  * Tujuan
  * Fitur yang harus jadi
  * Perintah menjalankan (`mvn spring-boot:run`, dsb).
* Di setiap branch tahap, buat folder `docs/` berisi:

  * Gambar ERD
  * Flow presensi (RFID, QR, Face, Geolocation)
  * Contoh request/response (Postman JSON).

---

## 4. Kalau Mau Lebih “Game-like” untuk Siswa

Bapak/Ibu bisa beri nama setiap tahap seperti “Level”:

1. Level 1 – API Dasar
2. Level 2 – Data Siswa & Guru
3. Level 3 – Login & Role
4. Level 4 – Presensi Manual
5. Level 5 – Kartu RFID
6. Level 6 – Barcode / QR
7. Level 7 – Lokasi Presensi
8. Level 8 – Foto Wajah
9. Level 9 – Laporan
10. Level 10 – Publikasi & Demo

Di LKPD bisa ditulis:

> “Untuk menyelesaikan Level 5, kalian harus bisa melakukan presensi menggunakan RFID (simulasi). Lanjut ke Level 6 kalau API sudah lulus uji Postman.”

---

Sip, kita lanjut ya. Kita bikin ini seolah-olah siap langsung di-*push* ke GitHub dan dipakai di kelas. 😄

---

## 1. Struktur Folder Monorepo

Direkomendasikan 1 repo induk:

```text
presensi-siswa-guru/
├─ README.md
├─ backend/
│  ├─ README.md
│  └─ src/...
├─ mobile-app/
│  ├─ README.md
│  └─ app/...
└─ desktop-app/
   ├─ README.md
   └─ src/...
```

### 1.1. Struktur `backend/` (Spring Boot)

```text
backend/
├─ pom.xml
├─ src/
│  ├─ main/
│  │  ├─ java/com/smk/presensi/
│  │  │  ├─ PresensiApplication.java
│  │  │  ├─ config/
│  │  │  ├─ controller/
│  │  │  ├─ dto/
│  │  │  ├─ entity/
│  │  │  ├─ repository/
│  │  │  └─ service/
│  │  └─ resources/
│  │     ├─ application.properties
│  │     └─ data.sql  (opsional demo)
│  └─ test/
└─ docs/
   ├─ ERD.png
   ├─ flow-presensi-rfid.png
   ├─ flow-presensi-geolocation.png
   └─ postman-collection.json
```

> Folder `docs/` sangat membantu buat siswa: mereka bisa lihat ERD dan flow tanpa bingung.

---

### 1.2. Struktur `mobile-app/` (Android Java)

```text
mobile-app/
├─ README.md
├─ app/
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/com/smk/presensi/mobile/
│  │  │  │  ├─ MainActivity.java
│  │  │  │  ├─ auth/...
│  │  │  │  ├─ presensi/...
│  │  │  │  └─ utils/...
│  │  │  └─ res/...
│  │  └─ androidTest/...
│  └─ build.gradle
└─ docs/
   ├─ ui-wireframe.png
   └─ flow-qr-checkin.png
```

---

### 1.3. Struktur `desktop-app/` (JavaFX)

```text
desktop-app/
├─ README.md
├─ pom.xml (atau gradle)
├─ src/
│  ├─ main/java/com/smk/presensi/desktop/
│  │  ├─ DesktopApp.java
│  │  ├─ controller/
│  │  ├─ viewmodel/
│  │  └─ model/
│  └─ main/resources/
│     ├─ fxml/
│     └─ css/
└─ docs/
   └─ mockup-dashboard.png
```

---

## 2. Pola Penamaan Branch di Repo Guru

Repo guru: `presensi-siswa-guru-master`

### 2.1. Branch Utama

* `main` → versi final/komplit (untuk demo & dokumentasi).
* `dev`  → area kerja guru, sebelum dipecah ke branch per-tahap.

### 2.2. Branch Per Tahap (Disarankan)

Gunakan pola:

```text
tahap-00-setup
tahap-01-backend-skeleton
tahap-02-domain-crud
tahap-03-auth-role
tahap-04-presensi-basic
tahap-05-rfid-basic
tahap-06-barcode-basic
tahap-07-geolocation
tahap-08-face-basic-upload
tahap-09-reporting
tahap-10-deploy-basic (opsional)
```

Kalau mau “nama level” yang lebih fun:

```text
level-01-api-dasar
level-02-data-siswa-guru
level-03-login-role
...
```

Tapi saran saya: **nama branch tetap formal & deskriptif** (biar enak dicari), sedangkan nama “Level” dipakai di LKPD dan README.

---

## 3. Contoh README di Root Repo

**File:** `README.md` di `presensi-siswa-guru/`

```markdown
# Sistem Presensi Siswa & Guru (RFID, Barcode, Face + Geolocation)

Proyek ini adalah bahan ajar untuk siswa SMK jurusan Sistem Informasi Jaringan dan Aplikasi.
Teknologi utama:
- Backend: Java Spring Boot (REST API)
- Frontend Mobile: Android (Java)
- Frontend Desktop: JavaFX

## Struktur Monorepo

- `backend/`      → API presensi (Spring Boot)
- `mobile-app/`   → Aplikasi Android untuk presensi (QR, foto, GPS)
- `desktop-app/`  → Aplikasi desktop (JavaFX) untuk admin/operator

## Level/Tahapan Belajar

Setiap tahapan belajar disimpan dalam branch terpisah di repo ini:

1. `tahap-00-setup`          → Persiapan environment & struktur dasar
2. `tahap-01-backend-skeleton` → API hello world & struktur paket
3. `tahap-02-domain-crud`    → Entity Siswa, Guru, Kelas + CRUD
4. `tahap-03-auth-role`      → Login JWT + Role (ADMIN/GURU/SISWA)
5. `tahap-04-presensi-basic` → Model presensi & flow checkin/checkout
6. `tahap-05-rfid-basic`     → Presensi via RFID (simulasi)
7. `tahap-06-barcode-basic`  → Presensi via QR/Barcode
8. `tahap-07-geolocation`    → Validasi lokasi saat presensi
9. `tahap-08-face-basic-upload` → Upload foto wajah saat presensi
10. `tahap-09-reporting`     → Laporan & dashboard sederhana

## Cara Menggunakan Untuk Siswa

1. Guru menunjukkan branch yang digunakan untuk pertemuan hari ini.
2. Siswa:
   - Meng-*clone* repo TEMPLATE khusus siswa, **bukan** repo guru ini.
   - Boleh `git checkout` branch tertentu di repo guru ini hanya untuk referensi.
3. Setiap level memiliki README sendiri dengan:
   - Tujuan belajar
   - Langkah kerja
   - Checklist keberhasilan
```

---

## 4. Contoh README per Tahap (Backend)

Saya buat contoh untuk 3 tahap awal. Nanti pola yang sama bisa Bapak/Ibu duplikasi.

---

### 4.1. README Tahap 01 – Backend Skeleton

**Branch:** `tahap-01-backend-skeleton`
**File:** `backend/README.md`

````markdown
# Tahap 01 – Backend Skeleton (Spring Boot)

## Tujuan Pembelajaran

Setelah menyelesaikan tahap ini, kalian diharapkan:
- Mampu menjalankan proyek Spring Boot sederhana.
- Memahami struktur dasar folder di `backend/`.
- Bisa membuat endpoint REST sederhana (`/api/hello`).

## Langkah Kerja

1. Buka folder `backend/` di IntelliJ IDEA / VS Code.
2. Jalankan perintah:
   ```bash
   mvn spring-boot:run
````

3. Buka browser, akses:

   * `http://localhost:8080/api/hello`
4. Amati struktur paket:

   * `controller/` → Tempat class yang menerima request dari client.
   * `service/`    → Tempat logika bisnis.
   * `entity/`     → Nanti berisi model tabel database.
   * `repository/` → Nanti untuk akses ke database.

## Tugas Siswa

1. Ubah isi response `/api/hello` menjadi:

   ```json
   { "message": "Presensi SMK – Tahap 1" }
   ```
2. Tambahkan endpoint baru:

   * `GET /api/info` yang mengembalikan:

     ```json
     {
       "app": "Presensi Siswa & Guru",
       "version": "0.1",
       "developer": "Nama_Kelompok"
     }
     ```

## Checklist Kelulusan Tahap 01

* [ ] Aplikasi Spring Boot berhasil dijalankan tanpa error.
* [ ] Endpoint `/api/hello` muncul di browser/Postman.
* [ ] Endpoint `/api/info` berhasil dibuat dan memberikan JSON sesuai tugas.

````

---

### 4.2. README Tahap 02 – Domain & CRUD

**Branch:** `tahap-02-domain-crud`  
**File:** `backend/README.md`

```markdown
# Tahap 02 – Entity Siswa, Guru, dan Kelas + CRUD

## Tujuan Pembelajaran

- Memahami cara mendesain model data (entity) untuk Siswa, Guru, dan Kelas.
- Mampu membuat API CRUD sederhana (Create, Read, Update, Delete).
- Menggunakan repository dan service dengan pola yang rapi.

## Fokus Konsep

- Entity (JPA @Entity)
- Repository (interface extends JpaRepository)
- Service layer (memisahkan logika dari controller)
- DTO (Data Transfer Object) sederhana

## Langkah Kerja (Ringkas)

1. Buat entity:
   - `Siswa`:
     - `id`, `nis`, `nama`, `kelas`, `rfidCardId`, `barcodeId`, `faceId`.
   - `Guru`:
     - `id`, `nip`, `nama`, `mapel`.
   - `Kelas`:
     - `id`, `nama`, `waliKelas`.

2. Buat repository:
   - `SiswaRepository`, `GuruRepository`, `KelasRepository`.

3. Buat service:
   - `SiswaService` dengan method:
     - `findAll()`, `findById()`, `create()`, `update()`, `delete()`.

4. Buat controller:
   - `SiswaController` dengan endpoint:
     - `GET /api/siswa`
     - `GET /api/siswa/{id}`
     - `POST /api/siswa`
     - `PUT /api/siswa/{id}`
     - `DELETE /api/siswa/{id}`

5. Jalankan dan uji pakai Postman.

## Tugas Siswa

1. Tambahkan field baru ke entity `Siswa`: `jurusan` (String).
2. Pastikan field baru tersebut:
   - Ikut tersimpan saat `POST /api/siswa`.
   - Ikut muncul di hasil `GET /api/siswa`.

3. Tambahkan endpoint:
   - `GET /api/siswa/kelas/{namaKelas}`
   - Mengembalikan daftar siswa berdasarkan nama kelas.

## Checklist Kelulusan Tahap 02

- [ ] Entity Siswa, Guru, dan Kelas sudah dibuat.
- [ ] CRUD Siswa sudah berfungsi penuh (POST, GET, PUT, DELETE).
- [ ] Field `jurusan` berfungsi dan muncul di response.
- [ ] Endpoint filter siswa per kelas berjalan baik.
````

---

### 4.3. README Tahap 05 – RFID Basic (contoh yang langsung nyambung ke metode presensi)

**Branch:** `tahap-05-rfid-basic`
**File:** `backend/README.md`

````markdown
# Tahap 05 – Presensi dengan RFID (Simulasi)

## Tujuan Pembelajaran

- Memahami bahwa RFID hanya berfungsi sebagai "ID unik" pengguna.
- Mampu memproses presensi berdasarkan `rfidCardId`.
- Mampu menghubungkan data kartu RFID dengan entitas Siswa/Guru.

## Fokus Konsep

- Field identitas tambahan di entity (`rfidCardId`).
- Mencari data berdasarkan field unik non-primary-key.
- Mencatat presensi dengan method "RFID".

## Langkah Kerja (Ringkas)

1. Pastikan entity `Siswa` dan `Guru` memiliki field:
   - `rfidCardId` (String, boleh nullable).

2. Tambahkan method di repository:
   - `Optional<Siswa> findByRfidCardId(String rfidCardId);`
   - `Optional<Guru> findByRfidCardId(String rfidCardId);`

3. Buat DTO request:
   ```java
   public class RfidPresensiRequest {
       private String rfidCardId;
   }
````

4. Buat endpoint di `PresensiController`:

   * `POST /api/presensi/rfid/checkin`
   * Body: `RfidPresensiRequest`
   * Langkah di service:

     1. Cari siswa/guru berdasarkan `rfidCardId`.
     2. Jika tidak ditemukan → return error (404).
     3. Jika ditemukan → buat record `Presensi` baru:

        * `userId` diisi dari siswa/guru.
        * `method = "RFID"`.
        * `tanggal`, `jamMasuk` diisi waktu saat ini.

5. Uji dengan Postman:

   * Isi beberapa data dummy Siswa dengan `rfidCardId` berbeda.
   * Lakukan beberapa panggilan `checkin`.

## Tugas Siswa

1. Tambahkan endpoint `POST /api/presensi/rfid/checkout`:

   * Logikanya: update `jamPulang` pada record presensi hari ini.

2. Tambahkan validasi:

   * Jika user yang sama sudah `checkin` hari ini dan belum `checkout`, maka:

     * Tidak boleh `checkin` lagi dengan RFID.

## Checklist Kelulusan Tahap 05

* [ ] Field `rfidCardId` tersimpan di database.
* [ ] Endpoint `POST /api/presensi/rfid/checkin` berfungsi.
* [ ] Siswa/guru yang tidak memiliki `rfidCardId` tidak bisa presensi via RFID.
* [ ] Validasi double checkin dalam 1 hari sudah dibuat.

````

---

## 5. Pola Kerja GitHub untuk Siswa

Supaya **minim salah dan konflik**, Bapak/Ibu bisa set pola:

### 5.1. Repo Template Siswa

Buat repo: `presensi-siswa-guru-template`  
Isinya bisa berupa:

- Hanya tahap 01 (backend skeleton), atau
- Tahap 01 + struktur kosong mobile/desktop.

Kemudian jadikan **Template Repository** di GitHub.

Siswa langkahnya:

1. Klik **Use this template** → beri nama repo: `presensi-nama-kelompok`.
2. Clone ke laptop masing-masing.
3. Setiap pertemuan:
   - Bapak/Ibu arahkan:
     - “Target kita hari ini menyelesaikan isi README di Tahap 02.”
   - Siswa tidak perlu `merge` dari repo guru (biar nggak ribet).
   - Jika ada yang error:
     - Mereka bisa membuka repo guru `presensi-siswa-guru-master` dan melihat branch terkait sebagai referensi.

### 5.2. Branch di Repo Siswa (opsional tapi rapi)

Di repo siswa, bisa pakai pola:

```text
main                  → hasil akhir tiap level
tahap-02-kerja        → mereka coding di sini
tahap-03-kerja        → dst.
````

Alur:

1. `git checkout -b tahap-02-kerja`
2. Kerjakan semua tugas Tahap 02.
3. Setelah selesai & dites:

   * `git checkout main`
   * `git merge tahap-02-kerja`
4. Lanjut ke tahap berikutnya.

---

Siap, kita bikin dulu **ERD versi teks** lalu **flow teks** untuk tiap jenis presensi. Nanti ini bisa Bapak jadikan dasar gambar di draw.io / diagrams.net / dbdiagram.io.

---

## 1. ERD (Entity Relationship Diagram) – Versi Teks

### 1.1. Daftar Entitas & Atribut Utama

#### 1. **ROLE**

Dipakai untuk mengatur hak akses (Admin, Guru, Siswa).

* `id_role` (PK)
* `nama_role` (ADMIN, GURU, SISWA)

---

#### 2. **USER**

Akun untuk login aplikasi (baik siswa, guru, maupun admin TU).

* `id_user` (PK)
* `username`
* `password_hash`
* `email` (opsional)
* `status_aktif` (boolean)
* `role_id` (FK → ROLE.id_role)

> Catatan: relasi ke Siswa/Guru lewat FK di tabel Siswa/Guru masing-masing (bukan di USER).

---

#### 3. **KELAS**

* `id_kelas` (PK)
* `nama_kelas` (misal: XI SIJA 1)
* `tingkat` (X / XI / XII)
* `jurusan` (misal: SIJA, TKJ, dsb)
* `wali_kelas_id` (FK → GURU.id_guru, opsional)

---

#### 4. **SISWA**

* `id_siswa` (PK)
* `nis`
* `nama_lengkap`
* `jenis_kelamin`
* `kelas_id` (FK → KELAS.id_kelas)
* `jurusan` (opsional jika tidak diambil dari KELAS)
* `rfid_card_id` (nullable)
* `barcode_id` (nullable, bisa sama dengan NIS yang dikodekan)
* `face_id` (nullable, ID untuk face recognition kalau dipakai)
* `user_id` (FK → USER.id_user, nullable kalau siswa belum punya akun)
* `status_aktif`

---

#### 5. **GURU**

* `id_guru` (PK)
* `nip`
* `nama_lengkap`
* `mapel_utama`
* `rfid_card_id` (nullable)
* `barcode_id` (nullable)
* `face_id` (nullable)
* `user_id` (FK → USER.id_user)
* `status_aktif`

---

#### 6. **LOKASI_KANTOR** (atau `AREA_PRESENSI`)

Digunakan untuk validasi jarak (geolocation).

* `id_lokasi` (PK)
* `nama_lokasi` (mis: “SMKN 1 Punggelan”, “Kantor PKL PT XYZ”)
* `alamat` (opsional)
* `latitude`
* `longitude`
* `radius_meter` (misal: 200 meter area valid)

---

#### 7. **DEVICE** (opsional tapi bagus)

Untuk mencatat dari perangkat mana presensi dilakukan (mesin RFID di TU, HP guru, dll).

* `id_device` (PK)
* `nama_device` (mis: “Mesin RFID Piket”, “HP Pak Budi”)
* `tipe_device` (RFID_READER / MOBILE / DESKTOP)
* `lokasi_id` (FK → LOKASI_KANTOR.id_lokasi)
* `serial_number` (opsional)

---

#### 8. **PRESENSI**

Ini tabel paling penting: semua presensi siswa & guru masuk sini.

* `id_presensi` (PK)
* `user_id` (FK → USER.id_user)
  (*dari user mana pun, baik guru / siswa*)
* `tipe_user` (SISWA / GURU) → supaya mudah filter.
* `tanggal` (date)
* `jam_masuk` (datetime / time)
* `jam_pulang` (datetime / time, nullable)
* `status` (HADIR / TERLAMBAT / IZIN / SAKIT / ALFA)
* `method` (MANUAL / RFID / BARCODE / FACE)
* `lokasi_id` (FK → LOKASI_KANTOR.id_lokasi, nullable)
* `latitude` (nullable, jika diisi dari HP)
* `longitude` (nullable)
* `foto_path` (nullable, jika presensi dengan foto wajah)
* `device_id` (FK → DEVICE.id_device, nullable)
* `keterangan` (opsional: mis “Presensi PKL di PT XX”)

---

### 1.2. Relasi Antar Entitas

Ditulis dalam bentuk teks “1 ke banyak”:

1. **ROLE (1) — (N) USER**

   * Satu role bisa dipakai banyak user.
   * `USER.role_id` → `ROLE.id_role`

2. **KELAS (1) — (N) SISWA**

   * Satu kelas banyak siswa.
   * `SISWA.kelas_id` → `KELAS.id_kelas`

3. **GURU (1) — (N) KELAS** (opsional untuk wali kelas)

   * Satu guru bisa menjadi wali beberapa kelas (atau 1).
   * `KELAS.wali_kelas_id` → `GURU.id_guru`

4. **USER (1) — (0..1) SISWA**

   * Satu user akun bisa terhubung ke satu siswa (atau tidak sama sekali).
   * `SISWA.user_id` → `USER.id_user`

5. **USER (1) — (0..1) GURU**

   * Satu user akun bisa terhubung ke satu guru (untuk login guru).
   * `GURU.user_id` → `USER.id_user`

6. **USER (1) — (N) PRESENSI**

   * Satu user bisa punya banyak record presensi.
   * `PRESENSI.user_id` → `USER.id_user`

7. **LOKASI_KANTOR (1) — (N) DEVICE**

   * Satu lokasi bisa punya banyak perangkat.
   * `DEVICE.lokasi_id` → `LOKASI_KANTOR.id_lokasi`

8. **LOKASI_KANTOR (1) — (N) PRESENSI**

   * Satu lokasi bisa punya banyak data presensi.
   * `PRESENSI.lokasi_id` → `LOKASI_KANTOR.id_lokasi`

Secara visual kasar (versi teks):

```text
ROLE 1 ----- N USER 1 ----- N PRESENSI
                    |
                    | 0..1
                 1  |
                 SISWA (via user_id)

                    |
                    | 0..1
                 1  |
                 GURU (via user_id)

KELAS 1 ----- N SISWA

GURU 1 ----- N KELAS  (wali_kelas_id)

LOKASI_KANTOR 1 ----- N DEVICE
LOKASI_KANTOR 1 ----- N PRESENSI
```

---

## 2. Flow Teks (Alur Proses)

Saya buat dalam beberapa skenario penting:

1. Setup Data oleh Admin
2. Login User
3. Presensi via RFID
4. Presensi via Barcode / QR
5. Presensi via Foto Wajah + Geolocation
6. Guru melihat rekap presensi kelas
7. Admin melihat laporan harian / bulanan

Bentuknya sudah siap kalau mau dijadikan Activity Diagram / Use Case.

---

### 2.1. Flow – Setup Data oleh Admin

**Aktor:** Admin
**Tujuan:** Sistem siap digunakan (data master terisi).

**Prekondisi:**

* Admin sudah punya akun (USER ber-role ADMIN).

**Langkah:**

1. Admin login ke aplikasi desktop / web admin.
2. Sistem verifikasi `username` & `password`.
3. Sistem cek `ROLE` → pastikan ADMIN.
4. Admin membuka menu **Master Kelas**:

   * Tambah data kelas (XI SIJA 1, XI SIJA 2, dll).
5. Admin membuka menu **Master Guru**:

   * Input data guru: `nip`, `nama`, `mapel`.
   * Pilih kelas yang diwalikan (opsional).
   * Buatkan akun `USER` terhubung dengan guru (user_id diisi).
6. Admin membuka menu **Master Siswa**:

   * Input siswa per kelas (nis, nama, kelas, jurusan).
   * (Jika sudah ada kartu RFID / barcode) → isikan `rfid_card_id`, `barcode_id`.
7. Admin membuka menu **Lokasi Presensi**:

   * Tambahkan `LOKASI_KANTOR` (koordinat sekolah, radius).
8. Jika ada perangkat fisik (mesin/PC dedicated), admin bisa menambahkan **DEVICE** dan menghubungkannya dengan `LOKASI_KANTOR`.
9. Data master selesai → sistem siap untuk operasi presensi.

---

### 2.2. Flow – Login User

**Aktor:** Guru / Siswa / Admin (umum)
**Tujuan:** Mendapatkan akses token (JWT) untuk menggunakan API.

**Prekondisi:**

* Akun `USER` sudah dibuat dan aktif.

**Langkah:**

1. Pengguna membuka aplikasi (mobile atau desktop).
2. Pengguna mengisi `username` dan `password`.
3. Aplikasi mengirim request `POST /auth/login` ke backend.
4. Backend:

   * Mencari `USER` berdasarkan `username`.
   * Mengecek `password_hash`.
   * Jika cocok → generate token JWT berisi `id_user` dan `role`.
5. Backend mengirim respon:

   * `access_token`
   * Informasi user (id_user, nama, role).
6. Aplikasi menyimpan token di lokal (memory / storage).
7. Setiap request ke API berikutnya, aplikasi kirim header `Authorization: Bearer <token>`.
8. Backend memvalidasi token untuk mengizinkan akses ke resource tertentu.

---

### 2.3. Flow – Presensi via RFID (Simulasi / Real)

**Aktor:** Siswa / Guru
**Media:** Desktop app (JavaFX) yang terhubung ke reader RFID, atau simulasi form input `rfidCardId`.

**Prekondisi:**

* `rfid_card_id` sudah terdaftar di tabel SISWA/GURU.
* User sudah login (atau di mode khusus perangkat TU).

**Langkah (Check-in):**

1. Di aplikasi desktop, operator membuka menu **Presensi RFID**.
2. Siswa/Guru menempelkan kartu RFID ke reader.
3. Desktop app membaca `rfidCardId` → misalnya `"RFID12345"`.
4. Desktop app mengirim request ke API:

   * `POST /api/presensi/rfid/checkin`
   * body: `{ "rfidCardId": "RFID12345" }`
5. Backend:

   * Cari `SISWA` berdasarkan `rfidCardId`.
   * Jika tidak ditemukan, cari `GURU`.
   * Jika tetap tidak ditemukan → kirim error: “Kartu belum terdaftar”.
   * Jika ditemukan → ambil `user_id` terkait.
   * Cek di tabel `PRESENSI`:

     * Apakah user sudah punya presensi **hari ini** dengan `jam_masuk` terisi dan `jam_pulang` masih `NULL`?
     * Jika sudah ada → tolak check-in (anti double).
   * Jika belum:

     * Buat record baru di `PRESENSI`:

       * `user_id` = user terkait.
       * `tipe_user` = SISWA atau GURU.
       * `tanggal` = hari ini.
       * `jam_masuk` = waktu saat ini.
       * `status` = HADIR atau TERLAMBAT (bisa cek jam).
       * `method` = "RFID".
       * `device_id` = ID perangkat (jika diisi).
6. Backend mengirim respon “Check-in sukses”.
7. Desktop app menampilkan nama & kelas/guru di layar (opsional: suara/alert).

**Flow Check-out:**

1. Proses sama, tapi endpoint:

   * `POST /api/presensi/rfid/checkout`
2. Backend:

   * Cari presensi hari ini yang `jam_pulang` masih kosong.
   * Update `jam_pulang` dengan waktu saat ini.
   * Jika tidak ada record yang bisa di-`checkout`, beri pesan: “Belum check-in” atau “Sudah checkout”.

---

### 2.4. Flow – Presensi via Barcode / QR (Mobile App)

**Aktor:** Siswa (atau Guru)
**Media:** Android app (kamera + scanner QR/barcode).

**Prekondisi:**

* Barcode/QR sudah dikaitkan dengan `barcode_id` di Siswa/Guru (bisa sama dengan NIS yang di-encode).
* User login di aplikasi mobile.

**Langkah (Check-in):**

1. Siswa membuka aplikasi **Presensi Mobile** dan login.
2. Pilih menu **Presensi Barcode/QR**.
3. Aplikasi membuka kamera dan memulai scanner.
4. Siswa mengarahkan kamera ke barcode/QR (misalnya di kartu atau di papan presensi).
5. Aplikasi membaca nilai kode: `barcode = "NIS2025001"`.
6. Aplikasi mengirim request ke API:

   * `POST /api/presensi/barcode/checkin`
   * body: `{ "barcode": "NIS2025001" }`
7. Backend:

   * Cari `SISWA` atau `GURU` yang `barcode_id` = `"NIS2025001"`.
   * Jika tidak ketemu → error.
   * Jika ketemu → sama seperti flow RFID, buat record `PRESENSI`.
8. Backend kirim respon sukses + data ringkas (nama, kelas).
9. Aplikasi menampilkan notifikasi “Check-in berhasil”.

Check-out analog dengan RFID, hanya beda endpoint.

---

### 2.5. Flow – Presensi via Foto Wajah + Geolocation (Mobile App)

**Aktor:** Siswa / Guru
**Media:** Android app (kamera + GPS).

**Prekondisi:**

* User login di mobile app.
* Di sistem sudah ada `LOKASI_KANTOR` + radius area presensi.
* (Level awal) Sistem hanya **menyimpan foto** sebagai bukti, tidak benar-benar mencocokkan wajah dengan AI (boleh dijadikan Level Lanjut).

**Langkah (Level 1 – Tanpa AI, hanya simpan foto & lokasi):**

1. User login, buka menu **Presensi Wajah**.
2. Aplikasi:

   * Meminta akses lokasi → ambil `latitude`, `longitude`.
   * Meminta akses kamera → ambil foto wajah.
3. Aplikasi mengirim request ke API:

   * `POST /api/presensi/face/checkin`
   * body: `multipart/form-data`:

     * `file` = foto
     * `latitude`
     * `longitude`
4. Backend:

   * Verifikasi token → dapat `user_id`.
   * Cari `LOKASI_KANTOR` terdekat (misalnya yang dipakai untuk sekolah).
   * Hitung jarak antara (lat,long user) dengan (lat,long lokasi kantor) pakai rumus jarak (Haversine sederhana).
   * Jika jarak > `radius_meter` → tolak presensi: “Anda di luar area presensi”.
   * Jika dalam radius:

     * Simpan file foto ke storage (folder / cloud).
     * Buat record `PRESENSI`:

       * `user_id` = user login
       * `tipe_user` = SISWA/GURU
       * `tanggal`, `jam_masuk`
       * `method` = "FACE"
       * `latitude`, `longitude`
       * `lokasi_id` = id lokasi kantor
       * `foto_path` = path file yang disimpan
5. Backend mengirim respon sukses + pesan.
6. Aplikasi menampilkan “Presensi berhasil”.

**Level Lanjutan (AI Face Recognition, optional):**

* Tambahkan field `face_id` di tabel SISWA/GURU.
* Proses enrolment:

  * Siswa/guru kirim beberapa foto untuk didaftarkan.
  * Backend kirim ke service AI (Python/external API) dan mendapat `face_id`.
* Saat presensi:

  * Foto yang dikirim dicek dengan `face_id` yang tersimpan.
  * Jika skor similarity < threshold → tolak presensi.

---

### 2.6. Flow – Guru Melihat Rekap Presensi Kelas

**Aktor:** Guru
**Media:** Desktop app atau mobile app.

**Prekondisi:**

* Guru login dengan role GURU.
* Guru sudah di-set sebagai wali kelas atau pengampu kelas.

**Langkah:**

1. Guru login ke aplikasi.
2. Pilih menu **Rekap Presensi Kelas**.
3. Aplikasi mengirim request:

   * `GET /api/rekap/kelas?kelasId=...&tanggal=2025-11-15`
4. Backend:

   * Ambil `id_user` guru dari token.
   * Cek di tabel KELAS mana saja yang walinya guru ini.
   * Validasi: guru hanya boleh melihat rekap kelas yang sesuai.
   * Ambil siswa-siswa dari kelas tersebut.
   * Join dengan tabel `PRESENSI` berdasarkan `user_id` (user milik siswa).
   * Buat rekap:

     * Siapa saja yang Hadir/Terlambat/Sakit/Izin/Alfa.
5. Backend kirim data dalam bentuk list (JSON).
6. Aplikasi menampilkan tabel rekap:

   * Kolom: NIS, Nama, Status, Jam Masuk, Metode.

---

### 2.7. Flow – Admin Melihat Laporan Harian/Bulanan

**Aktor:** Admin
**Media:** Desktop app.

**Prekondisi:**

* Data presensi sudah masuk ke sistem.

**Langkah:**

1. Admin login sebagai ADMIN.
2. Pilih menu **Laporan Presensi**.
3. Isi filter:

   * Rentang tanggal, kelas, tipe user (SISWA / GURU), metode (RFID/QR/FACE), dsb.
4. Aplikasi mengirim request ke API:

   * `GET /api/laporan/presensi?startDate=...&endDate=...&kelasId=...`
5. Backend:

   * Query ke tabel `PRESENSI` + join dengan USER, SISWA, GURU, KELAS.
   * Hitung ringkasan:

     * Total hadir, telat, izin, sakit, alfa.
     * Grafik per hari (opsional).
6. Backend mengirim JSON laporan.
7. Aplikasi:

   * Menampilkan grafik dan tabel.
   * Menyediakan tombol **Export** (CSV / PDF) kalau mau.

---




