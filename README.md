# Sistem Presensi Siswa & Guru

Repositori ini menyiapkan bahan ajar lengkap untuk proyek capstone SIJA: sistem presensi yang memanfaatkan RFID, barcode/QR, geolocation, dan bukti foto wajah. Monorepo ini sekaligus menampung backend Spring Boot, aplikasi Android, dan aplikasi desktop JavaFX sehingga setiap tahap pembelajaran bisa disinkronkan rapi.

## Struktur Monorepo

```
presensi-siswa-guru/
├─ README.md
├─ PLAN.MD
├─ backend/
├─ mobile-app/
├─ desktop-app/
└─ docs/
```

- `backend/` – REST API Spring Boot.
- `mobile-app/` – Aplikasi Android (Java) untuk presensi lapangan.
- `desktop-app/` – Aplikasi JavaFX untuk admin/operator.
- `docs/` – ERD, flow presensi, dan artefak pendukung.

## Level / Tahapan Belajar

| Tahap | Nama Branch | Fokus |
|-------|--------------|-------|
| 00 | `tahap-00-setup` | Persiapan tools & Git |
| 01 | `tahap-01-backend-skeleton` | Skeleton API & hello endpoint |
| 02 | `tahap-02-domain-crud` | Entity siswa/guru/kelas + CRUD |
| 03 | `tahap-03-auth-role` | Login JWT & role admin/guru/siswa |
| 04 | `tahap-04-presensi-basic` | Flow check-in/out manual |
| 05 | `tahap-05-rfid-basic` | Presensi via RFID (simulasi) |
| 06 | `tahap-06-barcode-basic` | Presensi via barcode/QR |
| 07 | `tahap-07-geolocation` | Validasi lokasi presensi |
| 08 | `tahap-08-face-basic-upload` | Upload foto wajah saat presensi |
| 09 | `tahap-09-reporting` | Laporan & dashboard |
| 10 | `tahap-10-deploy-basic` | Profil dev/prod & publikasi |

Gunakan branch ini sebagai checkpoint pembelajaran. Guru menjaga branch tetap stabil, sementara siswa bekerja di repo template masing-masing.

## Alur Penggunaan di Kelas

1. Guru menyiapkan repo master dengan branch per tahap (lihat tabel di atas).
2. Siswa membuat repo baru dari template, lalu mengikuti instruksi di README tiap modul.
3. Pada setiap pertemuan, guru mengarahkan siswa untuk menyelesaikan satu tahap dan memeriksa checklist keberhasilan.

Detail ERD, flow proses, dan contoh README per tahap tersedia di folder `docs/` serta masing-masing modul (`backend/`, `mobile-app/`, `desktop-app/`).
