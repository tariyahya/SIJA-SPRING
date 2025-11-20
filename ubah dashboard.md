
# 🧱 MODUL INTI SISTEM PRESENSI (Server / Web Backend)

### 1. Manajemen Data Dasar

* Data Siswa
* Data Orang Tua / Wali
* Data Guru & Staff
* Data Kelas / Rombel
* Data Mata Pelajaran
* Data Tahun Ajaran & Semester
* Data Kalender Akademik

### 2. Manajemen Perangkat & Metode Presensi

* **Metode Presensi Aktif** (bisa pilih / kombinasi):

  * Presensi Otomatis Mobile (GPS / WiFi / Face)
  * Presensi RFID (Gate / Kiosk)
  * Presensi Kelas oleh Guru (per jam pelajaran)
  * Presensi QR Code
* **Perangkat Presensi**

  * Device RFID (lokasi: gerbang, lab, kantin, dll)
  * Kiosk Presensi (tablet + reader)
  * Mapping perangkat → lokasi fisik

### 3. Engine Presensi

* Log presensi (semua sumber: RFID, Mobile, Guru)
* Aturan penentuan status:

  * Hadir, Terlambat, Pulang Cepat, Alfa, Sakit, Izin
* Integrasi:

  * Penggabungan presensi masuk (gate) + presensi kelas (guru)
* Deteksi anomali:

  * Tap berulang / tidak wajar
  * Fake GPS (untuk mobile)

---

# 📱 APLIKASI SISWA (Mobile)

### A. Dashboard Siswa

* Status kehadiran hari ini
* Jam masuk & pulang
* Jumlah keterlambatan bulan ini
* Ringkasan presensi minggu ini
* Pengumuman dari sekolah

### B. Presensi Otomatis (Mobile)

* **Presensi Datang**

  * Validasi GPS (radius sekolah)
  * Validasi WiFi sekolah (opsional)
  * Foto selfie / face recognition (opsional)
* **Presensi Pulang**

  * Berdasarkan jam pulang + lokasi
* Informasi jika presensi hari ini diambil dari:

  * RFID Gate
  * Mobile
  * Koreksi Admin/Guru

### C. Informasi Presensi RFID

* Riwayat tap RFID (tanggal, jam, lokasi gate)
* Info kartu: NIS, ID kartu, status aktif/nonaktif
* Notifikasi jika kartu tidak terbaca / bermasalah

### D. Izin & Ketidakhadiran

* Ajukan izin (sakit, keperluan keluarga, lomba, dll)
* Upload bukti (foto surat dokter, dll)
* Status: menunggu – disetujui – ditolak

### E. Riwayat Presensi & Rapor Kehadiran

* Filter per bulan / semester
* Grafik kehadiran
* Rekap: Hadir / Sakit / Izin / Alfa / Telat

### F. Profil Siswa

* Biodata
* Info kelas & wali kelas
* Informasi orang tua/wali
* Ganti PIN / password

---

# 📱 APLIKASI GURU (Mobile + Web)

### A. Dashboard Guru

* Jadwal mengajar hari ini
* Kelas yang belum diisi presensi
* Rekap presensi kelas hari ini
* Notifikasi izin siswa & permintaan koreksi

### B. Presensi Kelas per Jam Pelajaran

* Pilih:

  * Tanggal
  * Jam pelajaran
  * Kelas & mapel
* Daftar siswa otomatis muncul
* Input status tiap siswa:

  * Hadir (bisa auto dari RFID/Mobile)
  * Sakit
  * Izin
  * Alfa
  * Terlambat
  * Tugas luar/PKL
* Catatan khusus per siswa (opsional)
* Simpan & kunci presensi

> **Integrasi RFID**
>
> * Siswa yang sudah tap RFID di gate → otomatis ditandai “hadir di sekolah”
> * Guru tetap mengonfirmasi kehadiran di kelas (misalnya siswa ada di sekolah tapi tidak masuk kelas → masih bisa ditandai Alfa/Terlambat di mapel tertentu).

### C. Presensi Berbasis QR (Opsional)

* Generate QR kode untuk sesi pelajaran
* Siswa scan dengan aplikasi mobile
* Guru hanya verifikasi & simpan

### D. Manajemen Izin & Koreksi Presensi

* Lihat pengajuan izin siswa (kelas yang diampu)
* Setujui / tolak izin
* Ajukan koreksi presensi (misal: tadi siswa hadir, tapi belum sempat tap, dsb.)

### E. Rekap Presensi Kelas (Per-Guru)

* Rekap kehadiran per mapel
* Rekap kehadiran per kelas yang diajar
* Export PDF/Excel (untuk laporan dan Dapodik jika diperlukan)

### F. Profil Guru

* Data pribadi
* Mata pelajaran yang diampu
* Jadwal mengajar

---

# 📱 APLIKASI ORANG TUA / WALI (Mobile)

### A. Dashboard Orang Tua

* Status kehadiran anak hari ini:

  * Sudah datang / belum
  * Sudah pulang / belum
  * Metode presensi: RFID / Mobile / Kelas
* Ringkasan presensi minggu berjalan
* Pengumuman sekolah terkait kehadiran/disiplin

### B. Notifikasi Real-Time

* **Saat Anak Tap RFID di Gerbang**

  * Notif: “Ananda [Nama] telah masuk sekolah pukul 06:57 melalui RFID Gate 1.”
* **Saat Anak Pulang (RFID/Mobile)**

  * Notif pulang
* **Saat Anak Tidak Hadir / Alfa**

  * Notif pagi: “Ananda [Nama] belum tercatat hadir hingga pukul …”
* **Saat Izin Disetujui/Ditolak**

  * Info status izin dan catatan sekolah

### C. Lihat Riwayat Presensi Anak

* Harian / Mingguan / Bulanan / Semester
* Detail:

  * Tanggal & jam masuk/pulang
  * Sumber presensi (RFID, Mobile, Guru)
  * Status: Hadir, Sakit, Izin, Alfa, Telat
* Grafik kehadiran dan keterlambatan

### D. Pengajuan Izin dari Orang Tua

* Form pengajuan izin

  * Pilih tanggal
  * Jenis izin (sakit, izin keluarga, dll)
  * Tambah keterangan
  * Upload bukti (opsional)
* Tracking status izin

### E. Multi Anak (Opsional)

* Tampilan daftar anak (jika 1 orang tua punya lebih dari 1 siswa)
* Ganti profil anak dengan mudah

### F. Profil Orang Tua

* Data kontak (HP, email)
* Relasi dengan siswa (ayah, ibu, wali)

---

# 🌐 APLIKASI ADMIN / TU (Web)

### A. Dashboard Admin

* Total siswa hadir / tidak hadir hari ini
* Grafik kehadiran per kelas/jurusan
* Log presensi terakhir (RFID & Mobile)
* Kelas yang belum diisi presensi oleh guru
* Tiket koreksi presensi

### B. Manajemen Presensi RFID

* Registrasi & aktivasi kartu RFID
* Mapping kartu RFID → Siswa
* Riwayat tap per perangkat/gate
* Monitoring real-time tap masuk/pulang
* Status perangkat RFID (online/offline)

### C. Manajemen Presensi Mobile

* Log presensi GPS / WiFi / QR
* Peta lokasi (opsional)
* Deteksi anomali (lokasi jauh dari sekolah, jam tidak wajar, dll)

### D. Pengelolaan Presensi Kelas Guru

* Monitoring pengisian presensi per mata pelajaran
* Daftar guru yang belum mengisi
* Koreksi manual (dengan riwayat perubahan)

### E. Pengajuan Izin & Koreksi

* Daftar izin siswa (dari Siswa & Orang Tua)
* Persetujuan izin (jika wewenang TU/Waka Kesiswaan)
* Log siapa yang menyetujui dan kapan

### F. Laporan & Rekap

* Laporan presensi per siswa
* Laporan per kelas/rombel
* Laporan per guru mapel
* Laporan per jurusan
* Rekap bulanan & semester
* Export PDF / Excel

### G. Pengaturan Sistem

* Jam presensi masuk/pulang setiap jenjang
* Radius GPS & daftar WiFi resmi sekolah
* Aturan keterlambatan
* Pengaturan notifikasi (email, SMS, WhatsApp gateway, dsb.)
* Hak akses role (Admin, TU, Wali Kelas, Waka, Kepsek)

---

# 🧑‍💼 APLIKASI KEPALA SEKOLAH & WAKA (Web / Mobile View)

### Menu Utama:

* Ringkasan kehadiran harian
* Top 10 siswa dengan Alfa tertinggi
* Top 10 siswa telat terbanyak
* Daftar guru yang sering terlambat mengisi presensi
* Perbandingan kehadiran per jurusan / kelas
* Laporan siap cetak untuk rapat / supervisi

---

susun jadi **roadmap per sprint** yang rapi, jika sudah ada sesuaikan dengan urutan:

1. **MVP = RFID + Notifikasi Orang Tua**
2. **Presensi Kelas oleh Guru**
3. **Fitur-fitur lanjutan (izin, mobile siswa, analitik, dll)**

Silakan anggap 1 sprint = ±2 minggu atau 1 bulan (bisa disesuaikan di lapangan).

---

## 🧪 Sprint 1 – Fondasi Sistem + Core RFID (MVP Bagian 1)

**Tujuan Sprint:**
Punya backend & panel admin minimal yang sudah bisa:

* Registrasi kartu RFID siswa
* Mencatat tap masuk/pulang
* Menyimpan log presensi harian

### Modul Utama

1. **Setup Fondasi Sistem**

   * Struktur database dasar:

     * Siswa, Kelas/Rombel, Orang Tua/Wali, User, Role
   * API dasar: autentikasi (admin/TU), CRUD master siswa, kelas
   * Role minimal: `Admin/TU`, `Developer/IT`

2. **Modul Manajemen RFID**

   * Registrasi & mapping kartu RFID → siswa
   * Aktivasi/nonaktif kartu
   * List kartu per siswa (mendukung kartu ganda kalau perlu)

3. **Modul Log Tap RFID**

   * Endpoint API untuk device RFID (POST log tap)
   * Simpan:

     * ID kartu, waktu tap, lokasi/perangkat, arah (masuk/pulang)
   * Web Admin:

     * Tabel log tap real-time
     * Filter per tanggal, siswa, perangkat

4. **Aturan Sederhana Presensi Harian**

   * Konversi log tap → presensi harian:

     * Tap pertama = jam masuk
     * Tap terakhir = jam pulang
   * Status harian:

     * Hadir / Tidak Hadir (belum perlu terlambat detail, bisa nanti)

### Deliverable Sprint 1

* Database & API dasar aktif
* Device RFID bisa kirim data ke server
* Log tap terbaca di web admin
* Rekap sederhana: “Hari ini siswa X hadir/tidak”

---

## 📲 Sprint 2 – Notifikasi Orang Tua (MVP Bagian 2)

**Tujuan Sprint:**
Setiap kali anak **masuk/pulang** (via RFID), **Orang Tua dapat notifikasi** di HP.

### Modul Utama

1. **Aplikasi Mobile Orang Tua – Versi Ringan**

   * Login orang tua:

     * Berdasarkan nomor HP/username + password
   * Dashboard:

     * Status kehadiran anak hari ini (jam masuk/pulang)
   * Multi-anak (jika 1 account punya >1 anak)

2. **Notifikasi Real-time**

   * Engine notifikasi di backend:

     * Trigger saat log tap RFID diterima & berhasil di-mapping ke siswa
   * Jenis notifikasi:

     * Anak masuk sekolah
     * Anak pulang sekolah
   * Channel awal:

     * In-app notification dulu
     * (Opsional: siapkan hook untuk WA Gateway / FCM push)

3. **Riwayat Kehadiran di Aplikasi Orang Tua**

   * List kehadiran 7–30 hari terakhir:

     * Tanggal, jam masuk, jam pulang, sumber (RFID)

4. **Pengaturan Zona Waktu & Jam Sekolah**

   * Pengaturan jam sekolah per jenjang (untuk logika telat di sprint berikutnya)
   * Disimpan di modul `settings`

### Deliverable Sprint 2

* Orang tua bisa login & melihat kehadiran anak
* Notifikasi masuk/pulang berjalan (minimal in-app)
* MVP **“RFID + Ortu notif”** selesai dan bisa dipresentasikan ke sekolah

---

## 🧑‍🏫 Sprint 3 – Presensi Kelas oleh Guru

**Tujuan Sprint:**
Guru bisa mengisi presensi **per jam pelajaran**, terintegrasi dengan presensi harian RFID.

### Modul Utama

1. **Modul Jadwal Pelajaran**

   * CRUD Jadwal:

     * Guru, Kelas, Mata Pelajaran, Jam Ke-, Hari
   * Import jadwal dari Excel (opsional di sprint ini, bisa sprint berikutnya)

2. **Aplikasi Guru (Web / Mobile)** – Versi Dasar

   * Login guru
   * Dashboard:

     * Jadwal mengajar hari ini
   * Menu **Presensi Kelas**:

     * Pilih jadwal (kelas + mapel + jam)
     * Tampil daftar siswa di kelas
     * Kolom status: Hadir / Sakit / Izin / Alfa / Telat
     * Default “Hadir” untuk siswa yang sudah punya log RFID hari itu (integrasi)

3. **Integrasi RFID → Presensi Kelas**

   * Bisnis logik:

     * Siswa yang **tidak ada tap RFID** tapi ditandai hadir di kelas → warning / flag (opsional)
     * Siswa yang tap RFID tapi tidak pernah ditandai hadir di kelas → jadi bahan laporan

4. **Rekap Per-Kelas Per-Guru**

   * Guru bisa melihat:

     * Riwayat presensi per kelas
     * Rekap presensi per mapel

### Deliverable Sprint 3

* Guru bisa isi presensi kelas via web/mobile
* Integrasi dengan data kehadiran harian (RFID)
* Laporan presensi per kelas & per guru tersedia

---

## 📱 Sprint 4 – Mobile Siswa + Izin & Koreksi

**Tujuan Sprint:**
Mulai aktifkan **aplikasi siswa** + alur **izin & koreksi presensi**.

### Modul Utama

1. **Aplikasi Siswa – Versi Dasar**

   * Login siswa
   * Dashboard:

     * Status kehadiran hari ini
     * Info apakah kehadiran hari ini dari: RFID / Guru / Koreksi admin
   * Riwayat Kehadiran:

     * List harian sederhana

2. **Pengajuan Izin Siswa**

   * Siswa (dan/atau orang tua) bisa:

     * Ajukan izin Sakit/Izin ke sekolah
     * Isi alasan, tanggal, upload bukti
   * Alur persetujuan:

     * Wali kelas / Waka Kesiswaan / TU

3. **Koreksi Presensi**

   * Siswa/guru bisa mengajukan koreksi:

     * Contoh: “Saya hadir, tapi lupa tap RFID”
   * Admin/Wali Kelas bisa:

     * Setujui / Tolak koreksi
     * Jejak audit perubahan

4. **Sinkron ke Orang Tua**

   * Orang tua bisa melihat:

     * Izin yang diajukan
     * Status izin
     * Perubahan status presensi (dari Alfa menjadi Izin, dsb.)

### Deliverable Sprint 4

* App siswa aktif (minimal lihat kehadiran + ajukan izin)
* Alur izin & koreksi presensi berjalan
* Orang tua melihat update status izin & presensi

---

## 📊 Sprint 5 – Penyempurnaan, Laporan & Analitik

**Tujuan Sprint:**
Memperkuat sisi pelaporan, analitik, dan pengamanan sistem.

### Modul Utama

1. **Laporan & Rekap Lengkap**

   * Per siswa (untuk rapor)
   * Per kelas / rombel
   * Per jurusan
   * Per guru mapel
   * Export PDF/Excel (untuk arsip & lampiran supervisi)

2. **Dashboard Kepala Sekolah & Waka**

   * Ringkasan kehadiran harian
   * Top siswa dengan Alfa/telat tertinggi
   * Guru yang sering tidak mengisi presensi
   * Grafik perbandingan per jurusan

3. **Security & Audit**

   * Log aktivitas user (admin/guru)
   * Role permission lebih detail
   * Hardening API (rate limit, token, dsb.)

4. **Polishing & UX**

   * Penyederhanaan UI menu
   * Perbaikan performa query laporan
   * Bug fixing dari feedback sekolah

### Deliverable Sprint 5

* Sekolah punya dashboard lengkap kehadiran
* Laporan siap untuk kebutuhan administrasi & supervisi
* Sistem stabil untuk pemakaian rutin

---
