# Mobile App (Android)

Aplikasi Android mendukung presensi barcode/QR, geolocation, dan foto wajah. Gunakan Java + Android Studio dengan struktur paket `auth`, `presensi`, dan `utils` seperti pada plan.

## Tahapan Implementasi

| Tahap | Fokus | Highlight |
|-------|-------|-----------|
| `tahap-01-mobile-qr-scanner` | Integrasi scanner QR/barcode | Pakai ZXing/ML Kit, kirim kode ke API `/presensi/barcode/*`. |
| `tahap-02-mobile-geolocation` | Ambil GPS dan kirim ke backend | Gunakan `FusedLocationProviderClient`, tampilkan validasi radius. |
| `tahap-03-mobile-camera-face` | Foto wajah & unggah multipart | Integrasi kamera + preview, upload ke `/presensi/face/checkin`. |

### Struktur Direkomendasikan

```
mobile-app/
├─ app/
│  ├─ build.gradle
│  └─ src/main/java/com/smk/presensi/mobile/
│     ├─ MainActivity.java
│     ├─ auth/
│     ├─ presensi/
│     └─ utils/
└─ docs/
   ├─ ui-wireframe.png
   └─ flow-qr-checkin.png
```

### Checklist Umum per Tahap
- [ ] Seluruh permission (camera, location) sudah diset di `AndroidManifest`.
- [ ] Token JWT tersimpan aman (SharedPreferences/Datastore).
- [ ] Error API ditampilkan jelas agar siswa mudah debugging.
- [ ] Setiap tahap memiliki README mini menjelaskan tujuan & tugas siswa.

## Implementasi Saat Ini (Generated)

Aplikasi dasar telah dibuat dengan fitur:
- **Login**: Menggunakan endpoint `/api/auth/login`.
- **Dashboard**: Menampilkan nama user.
- **Checkin**: Mengirim data lokasi (dummy) ke `/api/presensi/checkin`.
- **History**: Menampilkan list presensi dari `/api/presensi/histori`.

### Cara Menjalankan
1. Buka folder `mobile-app` di Android Studio.
2. Sync Gradle.
3. Jalankan di Emulator (pastikan backend running di port 8080).
