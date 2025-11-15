# Flow Proses Presensi

## 1. Setup Data oleh Admin
1. Admin login sebagai role ADMIN.
2. Input master kelas, guru, siswa.
3. Hubungkan guru dengan kelas (wali) jika perlu.
4. Isi `rfid_card_id`, `barcode_id`, `face_id` jika tersedia.
5. Tambahkan `LOKASI_KANTOR` beserta radius valid.
6. (Opsional) Daftarkan perangkat (`DEVICE`) yang terhubung pada lokasi.

## 2. Login User (JWT)
1. Pengguna membuka aplikasi, mengisi username & password.
2. Aplikasi memanggil `POST /auth/login`.
3. Backend validasi kredensial, lalu mengirim JWT + info role.
4. Aplikasi menyimpan token dan mengirim header `Authorization: Bearer <token>` pada request berikutnya.

## 3. Presensi via RFID
1. Operator menjalankan aplikasi desktop pada menu RFID.
2. Pengguna menempelkan kartu dan aplikasi membaca `rfidCardId`.
3. Aplikasi memanggil `POST /api/presensi/rfid/checkin`.
4. Backend mencari siswa/guru berdasarkan `rfidCardId`.
5. Jika valid dan belum check-in hari itu, backend membuat record `Presensi` baru dengan `method = "RFID"`.
6. Checkout dilakukan oleh endpoint `POST /api/presensi/rfid/checkout` yang mengisi `jam_pulang`.

## 4. Presensi via Barcode / QR (Mobile)
1. User login di aplikasi Android dan membuka menu scanner.
2. Kamera membaca barcode/QR → menghasilkan `barcode`.
3. Aplikasi memanggil `POST /api/presensi/barcode/checkin`.
4. Backend mencari siswa/guru berdasarkan `barcode_id` dan menyimpan presensi dengan `method = "BARCODE"`.
5. Flow checkout mengikuti endpoint barcode checkout (analog RFID).

## 5. Presensi via Foto Wajah + Geolocation
1. User membuka menu presensi wajah di aplikasi Android.
2. Aplikasi mengambil lokasi (GPS) dan foto wajah.
3. Kirim `multipart/form-data` ke `POST /api/presensi/face/checkin` berisi file + lat/long.
4. Backend memverifikasi token, mengecek radius dari `LOKASI_KANTOR`, dan menyimpan foto ke storage.
5. Jika lokasi sah, backend mencatat presensi dengan `method = "FACE"`, menyimpan koordinat dan `foto_path`.
6. Level lanjut: integrasi layanan face recognition eksternal untuk verifikasi otomatis.

## 6. Guru Melihat Rekap Presensi Kelas
1. Guru login dan membuka menu rekap.
2. Aplikasi memanggil `GET /api/rekap/kelas?kelasId=...&tanggal=...`.
3. Backend memvalidasi bahwa guru adalah wali kelas terkait.
4. Backend mengambil daftar siswa + status presensi.
5. Aplikasi menampilkan tabel rekap.

## 7. Admin Melihat Laporan Harian/Bulanan
1. Admin login dan membuka menu laporan.
2. Isi filter tanggal/kelas/metode.
3. Aplikasi memanggil `GET /api/laporan/presensi` dengan parameter filter.
4. Backend menghitung ringkasan (hadir, telat, izin, sakit, alfa) dan mengembalikan JSON.
5. Aplikasi menampilkan grafik/tabel dan menyediakan opsi ekspor CSV/PDF.
