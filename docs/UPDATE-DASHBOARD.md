# Dokumentasi Pengembangan Dashboard Presensi & Manajemen Sekolah (Tahap 05)

Dokumen ini disusun sebagai bahan informasi resmi untuk manajemen sekolah mengenai perkembangan **Dashboard Aplikasi Presensi & Manajemen Sekolah (Desktop App)**. Isinya merangkum tujuan pengembangan, manfaat bagi sekolah, fitur yang sudah tersedia, status pengerjaan saat ini, serta rencana pengembangan ke depan.

---

## 1. Ringkasan Eksekutif

- Dashboard baru dirancang sebagai **"satu pintu"** bagi pimpinan sekolah untuk memantau kehadiran, data siswa/guru, kegiatan PKL, serta informasi pendukung lainnya secara cepat dan ringkas.
- Tampilan awal (home) menampilkan **Executive Summary** berupa statistik kunci dan grafik, sehingga pimpinan dapat segera melihat kondisi terkini tanpa harus membuka banyak menu.
- Struktur menu telah diatur ulang agar **lebih terstruktur, mudah dipahami**, dan siap dikembangkan untuk kebutuhan jangka panjang (scalable).
- Pengembangan saat ini fokus pada:
  - Stabilitas tampilan dan navigasi.
  - Integrasi data nyata dari backend (bukan lagi data simulasi).
  - Persiapan modul-modul prioritas: **Data Master** dan **Presensi**.

---

## 2. Tujuan Pengembangan Dashboard

- **Meningkatkan kualitas pengambilan keputusan** dengan menyediakan data yang cepat, akurat, dan mudah dibaca.
- **Memperkuat pengawasan kehadiran** siswa dan guru secara harian, mingguan, dan bulanan.
- **Mendukung tata kelola sekolah** melalui integrasi data akademik, kesiswaan, PKL, sarpras, dan keuangan dalam satu aplikasi.
- **Mengurangi pekerjaan manual** (rekap di Excel, pencarian data manual) sehingga waktu tenaga kependidikan lebih banyak untuk pelayanan.
- **Menjaga akuntabilitas dan transparansi data**, terutama terkait presensi, PKL, dan pembayaran.

---

## 3. Gambaran Fitur Utama (Saat Ini)

### 3.1 Struktur Navigasi & Menu

Top menu lama telah diganti dengan **Sidebar Accordion** yang lebih rapi dan mudah dikembangkan. Menu dikelompokkan menjadi beberapa bagian utama:

- **Dashboard Utama**
  - Halaman landing berisi ringkasan statistik penting dan grafik.
- **Data Master**
  - Pengelolaan data Siswa, Guru, Kelas, Jurusan, DUDI, dan Instansi.
- **Akademik & Jadwal**
  - Pengelolaan Mata Pelajaran dan Jadwal Mengajar.
- **Presensi**
  - Presensi Siswa, Rekap Kehadiran, dan Presensi Guru.
- **PKL / Prakerin**
  - Penempatan, log harian, dan penilaian PKL.
- **Kesiswaan**
  - Pendataan pelanggaran dan prestasi siswa.
- **Sarpras & Inventaris**
  - Inventaris barang dan peminjaman sarana/prasarana.
- **Keuangan**
  - Master pembayaran, transaksi keuangan, dan rekapitulasi.
- **Utilitas Sistem**
  - Import/Export, manajemen pengguna (user management), dan pengaturan sistem.

Struktur ini dapat disesuaikan lebih lanjut sesuai kebutuhan kebijakan sekolah.

### 3.2 Dashboard Home / Executive Summary

Halaman `Dashboard Home` dirancang sebagai tampilan ringkas untuk manajemen, berisi:

- **Kartu Statistik (Statistic Cards)**:
  - Total siswa aktif.
  - Total guru aktif.
  - Ringkasan kehadiran hari ini (hadir, izin, sakit, alfa).
- **Grafik & Visualisasi**:
  - Line chart: tren kehadiran mingguan.
  - Bar chart: distribusi siswa per jurusan.
- **Tabel Monitoring Kehadiran**:
  - Daftar siswa dengan tingkat kehadiran rendah (early warning) untuk membantu wali kelas, BK, dan manajemen melakukan tindak lanjut.

### 3.3 Kerangka Modul & Halaman Pendukung

Untuk memastikan seluruh menu dapat diakses tanpa error, telah disiapkan **lebih dari 16 kerangka halaman (placeholder)**. Setiap menu yang belum terimplementasi penuh akan menampilkan status **"Under Construction"** atau tampilan dasar, sehingga:

- Navigasi bisa diuji dari awal sampai akhir.
- Mengurangi risiko error teknis saat demo maupun pelatihan.
- Memudahkan pengembangan bertahap per modul.

---

## 4. Status Pengerjaan Teknis

### 4.1 Selesai (Completed)

- Restrukturisasi navigasi dengan **Sidebar Accordion** sesuai kelompok fungsi utama sekolah.
- Implementasi tampilan **Dashboard Home / Executive Summary** dengan kartu statistik, grafik, dan tabel monitoring kehadiran.
- Pengaturan arsitektur tampilan sehingga konten halaman dapat berganti **tanpa me-reload seluruh jendela aplikasi**, mendukung pengalaman pengguna yang lebih mulus.
- Penyediaan kerangka (placeholder) untuk modul-modul yang akan dikembangkan, sehingga jalur pengembangan berikutnya lebih jelas.
- **Perbaikan error loading dashboard setelah login.** Berkas `dashboard.fxml` telah dibersihkan dari duplikasi node sehingga JavaFX berhasil memuat shell utama dan otomatis menampilkan `dashboard-home.fxml` saat aplikasi dibuka.
- Build desktop terbaru berhasil melewati tahap `mvn -DskipTests package`, sehingga eksekusi aplikasi dapat dilanjutkan untuk uji navigasi dan demo.

### 4.2 Baru Selesai (19 Nov 2025)

**A. Stabilitas & Keandalan Tampilan**
- [x] **Tombol navigasi dan Logout sudah berfungsi normal** (diverifikasi melalui screenshot dashboard Settings—user berhasil logout ke halaman login).

**B. Integrasi Data Nyata ke Dashboard**
- [x] **Controller `DashboardHomeController` kini terhubung ke backend API** melalui `ApiClient.getInstance()`.
- [x] **Mekanisme auto-refresh setiap 30 detik** telah diimplementasikan menggunakan JavaFX `Timeline`, sehingga data presensi harian, total siswa, dan total guru diperbarui otomatis tanpa intervensi manual.
- [x] **Data statistik ditampilkan secara asinkron** menggunakan `CompletableFuture` agar UI tetap responsif saat mengambil data dari server.

### 4.3 Sedang Dikerjakan (In Progress)

- [x] Penyempurnaan tampilan chart (Line Chart & Bar Chart) untuk menampilkan data tren kehadiran mingguan dan distribusi per jurusan.
- [x] Integrasi tabel "Siswa dengan Kehadiran Rendah" dengan data nyata dari endpoint backend.

---

## 5. Rencana Pengembangan Selanjutnya

### 5.1 Prioritas Jangka Pendek (0-2 Minggu)

- **Modul Data Master**
  - [x] **Tambah dropdown kelas XIII** untuk mendukung program SMK 4 tahun (19 Nov 2025).
  - [x] **Tambah modul Tahun Ajaran & Semester Aktif** dengan CRUD lengkap dan fungsi set aktif (19 Nov 2025).
  - [x] Implementasi penuh CRUD (Tambah, Ubah, Hapus, Cari) untuk data Siswa terhubung ke API backend melalui `SiswaController` (REST API) dan `SiswaManagementController` (desktop app).
  - [x] Implementasi penuh CRUD untuk data Guru terhubung ke API backend melalui `GuruController` (REST API) dan `GuruManagementController` (desktop app).
- **Modul Presensi**
  - [ ] Form input presensi manual untuk penyesuaian atau koreksi data.
  - [ ] Integrasi event handler **RFID Reader** pada halaman Presensi (bila perangkat tersedia), sehingga presensi dapat dilakukan otomatis melalui kartu.

### 5.2 Pengembangan Fitur Lanjutan (2-4 Minggu)

- **Modul PKL / Prakerin**
  - [ ] Form penempatan siswa ke DUDI (perusahaan mitra).
  - [ ] Upload dan pemantauan log harian siswa PKL.
- **Modul Keuangan**
  - [ ] Pencatatan transaksi SPP/pembayaran lainnya.
  - [ ] Fitur cetak kwitansi sederhana untuk keperluan administrasi.

### 5.3 Penyempurnaan & Optimasi

- [x] **Integrasi Leaflet.js untuk Office Location Management**
  - Menambahkan peta interaktif berbasis Leaflet.js di dialog pengelolaan lokasi kantor, sehingga admin dapat mencari alamat dan mendapatkan koordinat lat/long secara otomatis dengan menggeser pin di peta.
  - Integrasi penuh dengan JavaFX `WebView` dan bridge `JavaScriptBridge` di `OfficeManagementController` (19 Nov 2025).
- [x] **Role-Based Menu** (Hak Akses Berbasis Peran)
  - Menyembunyikan menu tertentu (misalnya Keuangan, Settings) bagi pengguna dengan peran Guru atau Siswa, demi keamanan dan keteraturan hak akses.
- [x] **Loading & Feedback Pengguna**
  - Indikator loading (`ProgressIndicator`) dan label status telah diterapkan di modul-modul utama (Dashboard, Manajemen Siswa, Manajemen Guru, Tahun Ajaran, Presensi Guru) sehingga pengguna mendapat umpan balik saat aplikasi mengambil data dari backend.
- [x] **Penanganan Error yang Lebih Ramah**
  - Notifikasi in-app (`InAppNotification`) menampilkan pesan error yang jelas di area tampilan terkait, dengan tombol **Refresh/Coba Lagi** untuk memicu pemanggilan ulang API tanpa menutup jendela.

### 5.4 Permintaan Baru (19 Nov 2025)

- **Data Master Tahun Ajaran & Semester**
  - Lanjutkan modul yang sudah dibuat dengan memastikan alur CRUD + penetapan aktif tersambung ke backend.
  - Tambahkan _linked data_ Semester untuk keperluan jadwal dan jurnal guru.
- **Akademik & Jadwal**
  - Implementasi penuh pengelolaan Mata Pelajaran, Jadwal Mengajar, dan relasi guru-kelas sehingga guru dapat mengabsen langsung dari kelasnya.
- **Presensi Guru di Kelas**
  - Form presensi manual guru harus menyediakan keterangan baru: **Izin**, **Sakit**, **Alpha**, dan **Dispensasi** (terkait materi khusus hari itu).
  - Setiap presensi guru otomatis men-generate entri di **Jurnal Guru** tanpa input tambahan.
- **Penilaian UH (Ulangan Harian)**
  - Tambahkan modul nilai UH tulis maupun praktik yang terhubung ke jadwal mengajar.
- **Quiz Interaktif (Quizizz-style)**
  - Sediakan ruang untuk guru membuat kuis fleksibel dengan fitur import bank soal dari Excel, manajemen token peserta, dan QR/barcode scan untuk membuka tautan ujian.

Catatan implementasi per 20 Nov 2025 (backend):
- **Data Master Tahun Ajaran & Semester** sudah memiliki REST API dan integrasi desktop melalui `TahunAjaranService` dan `TahunAjaranManagementController`.
- **Akademik & Jadwal Mengajar** sudah tersedia melalui `JadwalMengajarController` dan `JadwalMengajarService` (`/api/guru/jadwal`), dan akan menjadi basis relasi untuk presensi, jurnal guru, dan UH.
- **Presensi Guru di Kelas & Auto Jurnal Guru** sudah berjalan end-to-end: `PresensiGuruController` (desktop) menyimpan presensi guru dan langsung memanggil `JournalService` yang terhubung ke `GuruJurnalController` di backend.
- **Penilaian UH (Ulangan Harian)** sudah memiliki entity, service, dan controller (`UlanganHarian`, `UlanganHarianService`, `UlanganHarianController` pada endpoint `/api/akademik/uh`).
- **Quiz Interaktif** sudah memiliki API dasar melalui `QuizController` dan `QuizService` (`/api/quiz`) yang mendukung pembuatan sesi, penambahan soal, dan generate token + QR URL.
Sisa pekerjaan adalah membangun UI desktop yang nyaman untuk guru (form input, import bank soal dari Excel, serta tampilan QR/token dan rekapan nilai), yang tetap dimasukkan ke backlog sprint berikutnya beserta estimasi effort dan dependensi tambahan.

#### 5.4.1 API Quiz Interaktif (Backend Ready – 20 Nov 2025)

| Endpoint | Method | Body (Ringkas) | Keterangan |
| --- | --- | --- | --- |
| `/api/quiz/sessions` | POST | `{ "guruId", "kelasId", "judul", "mapel", "materi", "tanggal" }` | Membuat sesi kuis baru. `guruId` mengacu ke `Guru.id`, `kelasId` ke `Kelas.id`, `tanggal` opsional (default hari ini). |
| `/api/quiz/sessions/{id}` | GET | - | Mengambil detail sesi berikut daftar soal yang terhubung. |
| `/api/quiz/sessions/{id}/questions` | POST | `{ "soal", "opsiA", "opsiB", "opsiC", "opsiD", "jawabanBenar", "bobot" }` | Menambahkan soal ke sesi. `jawabanBenar` memakai kode A/B/C/D, bobot default 1. |
| `/api/quiz/sessions/{id}/token` | POST | - | Mengaktifkan sesi: backend membuat token 8 karakter dan `qrCodeUrl`, status berubah ke `ACTIVE`. |

Contoh request pembuatan sesi:

```json
POST /api/quiz/sessions
{
  "guruId": 12,
  "kelasId": 7,
  "judul": "Quiz Integral Dasar",
  "mapel": "Matematika",
  "materi": "Integral Tak Tentu",
  "tanggal": "2025-11-21"
}
```

Respons awal sebelum token digenerate:

```json
{
  "id": 3,
  "judul": "Quiz Integral Dasar",
  "mapel": "Matematika",
  "materi": "Integral Tak Tentu",
  "tanggal": "2025-11-21",
  "token": null,
  "qrCodeUrl": null,
  "status": "DRAFT",
  "guru": { "id": 12, "nama": "Bu Rina" },
  "kelas": { "id": 7, "nama": "XII RPL 1" },
  "questions": []
}
```

Setelah soal dirampungkan, desktop tinggal memanggil `POST /api/quiz/sessions/{id}/token` dan menampilkan `token` + `qrCodeUrl` pada layar/QR code. Endpoint tambahan (import Excel, validasi token peserta, rekapan nilai) dapat dibangun di atas struktur ini.

#### 5.4.2 Rencana Integrasi Desktop Quiz

1. **Lapisan Service & Model (desktop-app/src/main/java/com/smk/presensi/desktop/service)**
  - Tambah `QuizService` yang memanfaatkan `ApiClient` untuk memanggil keempat endpoint di atas (`createSession`, `getSession`, `addQuestion`, `activateSession`).
  - Tambah model `QuizSession`, `QuizQuestion`, dan DTO request sederhana agar binding JavaFX TableView lebih mudah.
  - Gunakan pola yang sama dengan `PresensiService`/`JournalService`: panggil API secara async memakai `CompletableFuture` + `InAppNotification` untuk feedback.

2. **Form & Controller Baru (`quiz-management.fxml` + `QuizManagementController`)**
  - Layout utama: panel kiri untuk form sesi (pilih guru, kelas, tanggal, mapel, materi, judul), panel kanan untuk daftar sesi + detail.
  - Setelah `createSession`, controller otomatis membuka panel soal (TableView input) dan mengaktifkan tombol `Generate Token`.
  - Notifikasi status/validasi mengikuti pattern `PresensiGuruController` agar UX konsisten.

3. **Manajemen Soal**
  - TableView dengan kolom Soal, Opsi A–D, Jawaban Benar, Bobot; baris baru bisa diedit langsung sebelum dikirim ke backend (`addQuestion`).
  - Tombol `Tambah Soal` akan mengirim setiap baris ke endpoint; setelah berhasil, table menandai status (ikon hijau) agar guru tahu mana yang sudah tersimpan.

4. **Import Excel (Bank Soal)**
  - Reuse mekanisme `ImportExportController`: buka dialog pilih file `.xlsx`, baca menggunakan Apache POI (sudah dipakai di modul ekspor) dengan struktur kolom `[No, Soal, OpsiA, OpsiB, OpsiC, OpsiD, JawabanBenar, Bobot]`.
  - Mapper akan mengubah setiap baris menjadi `CreateQuizQuestionRequest` dan memanggil `QuizService.addQuestion()` secara batch (degani 10 baris per request untuk menjaga UX).
  - Validasi: highlight baris gagal impor dan tampilkan alasan agar user bisa memperbaiki di Excel.

5. **Token & QR Display**
  - Setelah `generateToken`, tampilkan token besar + countdown masa aktif (opsional) pada panel kanan.
  - `qrCodeUrl` dapat dirender di JavaFX menggunakan library ZXing (sudah tersedia di repos toolkit) atau dengan `WebView` menampilkan QR berbasis API eksternal; tambahkan tombol "Salin Link" untuk mempermudah share.

6. **Integrasi dengan Modul Lain**
  - Mode pelaporan: simpan hasil create session ke `JournalService`/`PresensiService` bila kuis digunakan sebagai bukti pembelajaran (opsional backlog).
  - `DashboardHomeController` bisa menambahkan statistik "Jumlah Quiz Aktif Hari Ini" menggunakan endpoint `GET /api/quiz/sessions?tanggal=today` (akan ditambahkan ketika list API diperluas).

Dengan rencana ini, tim desktop dapat memulai implementasi UI tanpa menunggu perubahan tambahan di backend karena seluruh REST dasar sudah tersedia dan teruji.

### 5.5 Implementasi 19 Nov 2025 (Desktop App)

- **Modul Presensi Guru**
  - `presensi-guru.fxml` diganti dengan layout resmi berupa form presensi dan tabel riwayat.
  - Controller baru `PresensiGuruController` menangani pemilihan guru/kelas, status presensi, jam mengajar, materi, dan menyimpan data via `PresensiService`.
  - Tambahan opsi mock data + refresh harian memudahkan QA saat backend belum lengkap.
- **Auto Jurnal Guru**
  - Library baru `JournalService` dan model `JournalEntry` menembakkan `POST /guru/jurnal` secara otomatis setelah presensi guru tersimpan.
  - UI menampilkan notifikasi sukses/gagal sehingga operator tahu apakah jurnal berhasil dibuat tanpa input tambahan.
- **Status Presensi Baru**
  - `PresensiManagementController` dan dialog CRUD kini mengenali status `IZIN`, `SAKIT`, dan `DISPENSASI` (selain Hadir/Terlambat/Alpha) untuk menyelaraskan kebutuhan guru.
  - `PresensiService` mock data ikut memakai kombinasi status baru agar tampilan tabel mencerminkan kondisi riil.

---

## 6. Dampak dan Manfaat bagi Manajemen Sekolah

- **Monitoring kehadiran lebih cepat dan akurat**, mendukung pengambilan keputusan terkait disiplin dan pembinaan siswa.
- **Data terintegrasi** antar bagian (kurikulum, kesiswaan, PKL, keuangan) sehingga laporan dan rekap dapat dibuat lebih efisien.
- **Transparansi dan akuntabilitas meningkat**, terutama pada area presensi dan keuangan, karena data terdokumentasi dalam sistem.
- **Mempermudah supervisi** oleh kepala sekolah dan wakil kepala, karena informasi penting tersedia dalam satu dashboard terpusat.

---

## 7. Kebutuhan Dukungan dari Manajemen

Agar pemanfaatan dashboard maksimal, diperlukan dukungan dari manajemen sekolah dalam bentuk:

- **Penetapan kebijakan penggunaan sistem** (misalnya: presensi wajib melalui sistem, penanggung jawab entri data per bagian).
- **Penjaminan kualitas data** melalui penugasan admin data di tiap unit (TU, Kesiswaan, Kurikulum, PKL, Keuangan).
- **Fasilitasi pelatihan singkat** untuk guru dan staf yang akan menggunakan aplikasi desktop ini.
- **Dukungan infrastruktur** (komputer, jaringan, dan perangkat pendukung seperti RFID Reader bila digunakan).

---

## 8. Lampiran Teknis (Ringkas)

Bagian ini ditujukan untuk tim teknis/IT yang terlibat dalam pengembangan dan pemeliharaan sistem.

- **Dashboard Home**
  - File tampilan: `desktop-app/src/main/resources/fxml/dashboard-home.fxml`
  - Controller: `desktop-app/src/main/java/com/smk/presensi/desktop/controller/DashboardHomeController.java`
  - **Update terbaru (19 Nov 2025)**: Controller kini menggunakan `ApiClient` untuk mengambil data real-time dari endpoint `/siswa`, `/guru`, dan `/laporan/harian`. Auto-refresh 30 detik aktif untuk memastikan data selalu terkini.
- **Controller Utama Dashboard**
  - File: `desktop-app/src/main/java/com/smk/presensi/desktop/controller/DashboardController.java`
  - Mengatur pergantian konten halaman menggunakan container utama (misal: `StackPane`).
- **Shell Dashboard (FXML)**
  - File: `desktop-app/src/main/resources/fxml/dashboard.fxml`
  - Telah diverifikasi pada 19 Nov 2025—tidak ada lagi node ganda di luar `BorderPane`, sehingga kompatibel dengan loader JavaFX.
- **Integrasi Backend**
  - Service utama yang digunakan akan terhubung dengan:
    - `backend/src/main/java/com/smk/presensi/service/PresensiService.java`
    - `desktop-app/src/main/java/com/smk/presensi/desktop/service/ApiClient.java`
- **Keamanan & Akses**
  - Otentikasi dan otorisasi di sisi backend menggunakan JWT (contoh: `backend/src/main/java/com/smk/presensi/security/jwt/JwtAuthenticationFilter.java`), yang nantinya akan diintegrasikan dengan pengaturan akses menu di aplikasi desktop.

Dokumen ini akan diperbarui secara berkala seiring dengan perkembangan fitur dan kebutuhan manajemen sekolah.
