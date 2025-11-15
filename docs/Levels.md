# Level / Tahap Pembelajaran

Gunakan penamaan level untuk memotivasi siswa. Setiap level berkorespondensi dengan branch guru.

| Level | Branch | Fokus Pembelajaran | Bukti Kelulusan |
|-------|--------|--------------------|------------------|
| 1 | `tahap-01-backend-skeleton` | Menjalankan skeleton API & membuat endpoint dasar | `/api/hello` & `/api/info` berjalan |
| 2 | `tahap-02-domain-crud` | Entity & CRUD siswa/guru/kelas | CRUD `Siswa` + filter kelas sukses |
| 3 | `tahap-03-auth-role` | Login JWT & role | Token JWT diterima dan endpoint terproteksi |
| 4 | `tahap-04-presensi-basic` | Model presensi & flow manual | Check-in/checkout manual validasi double |
| 5 | `tahap-05-rfid-basic` | Presensi RFID simulasi | Check-in/out RFID dengan validasi kartu |
| 6 | `tahap-06-barcode-basic` | Presensi barcode/QR | QR scanner mobile menembak ke API dan mencatat kehadiran |
| 7 | `tahap-07-geolocation` | Validasi lokasi | Backend menolak presensi di luar radius |
| 8 | `tahap-08-face-basic-upload` | Bukti foto wajah | Upload foto + simpan path/foto |
| 9 | `tahap-09-reporting` | Dashboard & laporan | Guru/admin melihat rekap per kelas / range tanggal |
| 10 | `tahap-10-deploy-basic` | Profil dev/prod & publikasi | Profile dev/prod terpisah + panduan deploy |

Tuliskan checklist lengkap pada README tiap modul sehingga siswa tahu target yang harus dicapai sebelum naik level berikutnya.
