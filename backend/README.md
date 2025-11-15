# Backend (Spring Boot)

Modul backend menyajikan REST API utama untuk seluruh ekosistem presensi. Gunakan Maven/Gradle + Spring Boot dengan struktur paket standar (`controller`, `service`, `repository`, `entity`, `dto`).

## Tahap 01 – Backend Skeleton

**Branch:** `tahap-01-backend-skeleton`

### Tujuan
- Menjalankan proyek Spring Boot sederhana.
- Memahami struktur dasar modul backend.
- Membuat endpoint `GET /api/hello` dan `GET /api/info`.

### Langkah Ringkas
1. Buka folder `backend/` di IntelliJ IDEA atau VS Code.
2. Jalankan `mvn spring-boot:run` atau `./mvnw spring-boot:run`.
3. Akses `http://localhost:8080/api/hello`.
4. Pelajari struktur paket yang sudah disiapkan.

### Tugas
- Ubah response `/api/hello` menjadi `{ "message": "Presensi SMK – Tahap 1" }`.
- Tambahkan endpoint `/api/info` yang mengembalikan metadata aplikasi.

### Checklist
- [ ] Aplikasi berjalan tanpa error.
- [ ] Endpoint `/api/hello` aktif.
- [ ] Endpoint `/api/info` mengembalikan JSON sesuai tugas.

---

## Tahap 02 – Domain & CRUD

**Branch:** `tahap-02-domain-crud`

### Tujuan
- Mendesain entity `Siswa`, `Guru`, `Kelas`.
- Mengimplementasi CRUD `Siswa` lengkap dengan DTO.

### Langkah Ringkas
1. Buat entity:
   - `Siswa`: `id`, `nis`, `nama`, `kelas`, `rfidCardId`, `barcodeId`, `faceId`.
   - `Guru`: `id`, `nip`, `nama`, `mapel`.
   - `Kelas`: `id`, `nama`, `waliKelas`.
2. Buat repository berbasis `JpaRepository`.
3. Implementasi service dan controller untuk operasi CRUD `Siswa`.
4. Tambahkan DTO agar response konsisten.
5. Uji menggunakan Postman.

### Tugas
- Tambahkan field `jurusan` di entity `Siswa`.
- Pastikan field baru ikut tersimpan dan muncul di response.
- Buat endpoint `GET /api/siswa/kelas/{namaKelas}` untuk filter per kelas.

### Checklist
- [ ] Entity & repository selesai.
- [ ] CRUD `Siswa` berfungsi.
- [ ] Field `jurusan` ikut terbawa.
- [ ] Endpoint filter kelas berjalan.

---

## Tahap 05 – Presensi RFID (Simulasi)

**Branch:** `tahap-05-rfid-basic`

### Tujuan
- Menambahkan dukungan presensi berbasis RFID.
- Menghubungkan `rfidCardId` dengan entitas `Siswa/Guru`.

### Langkah Ringkas
1. Pastikan entity `Siswa` dan `Guru` memiliki field `rfidCardId`.
2. Tambahkan method repository `findByRfidCardId`.
3. Buat DTO `RfidPresensiRequest { String rfidCardId; }`.
4. Tambahkan endpoint `POST /api/presensi/rfid/checkin`.
5. Logika service:
   - Cari siswa/guru berdasarkan `rfidCardId`.
   - Tolak jika kartu belum terdaftar atau sudah check-in hari ini.
   - Buat record `Presensi` baru dengan `method = "RFID"`.

### Tugas
- Tambahkan endpoint `POST /api/presensi/rfid/checkout` untuk mengisi `jamPulang`.
- Tambahkan validasi anti double check-in dalam satu hari.

### Checklist
- [ ] Field `rfidCardId` tersimpan.
- [ ] Endpoint check-in RFID berfungsi.
- [ ] Validasi double check-in aktif.
- [ ] Endpoint checkout RFID tersedia.

---

## Folder Tambahan

```
backend/
├─ README.md
├─ docs/
│  └─ (tempat ERD, flow, Postman collection tingkat backend)
└─ src/
   └─ (kode akan diisi per tahap)
```

Letakkan artefak pendukung seperti ERD bergambar, flow, dan koleksi Postman di `backend/docs/`. Saat kode mulai ditulis, gunakan branch sesuai tahap agar siswa memiliki checkpoint yang konsisten.
