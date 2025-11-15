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

Tambahkan screenshot, wireframe, dan flow khusus mobile pada folder `docs/` untuk memudahkan siswa memahami UI/UX yang diinginkan.
