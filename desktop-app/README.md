# Desktop App (JavaFX)

Aplikasi desktop diperuntukkan bagi admin/operator TU. Fungsi utama: mengelola master data, membantu presensi RFID simulasi, serta menampilkan dashboard laporan.

## Tahapan Implementasi

| Tahap | Fokus |
|-------|-------|
| `tahap-01-desktop-dashboard` | Dashboard ringkas + tabel presensi |
| `tahap-02-desktop-rfid` (opsional) | Integrasi input RFID (serial/keyboard wedge) |
| `tahap-03-desktop-report-export` (opsional) | Export CSV/PDF laporan |

### Struktur Direktori

```
desktop-app/
├─ README.md
├─ pom.xml (atau build.gradle)
├─ src/main/java/com/smk/presensi/desktop/
│  ├─ DesktopApp.java
│  ├─ controller/
│  ├─ viewmodel/
│  └─ model/
└─ src/main/resources/
   ├─ fxml/
   └─ css/
```

### Pedoman Umum
- Gunakan pola MVVM atau MVC ringan agar mudah dipahami siswa.
- Sediakan service client untuk memanggil API backend (HTTP + JWT token).
- Siapkan mock data agar siswa bisa mencoba fitur tanpa backend saat awal tahap.
- Tambahkan `docs/mockup-dashboard.png` untuk acuan tampilan.

Tambahkan README tambahan per fitur jika dibutuhkan agar siswa tahu target output tiap tahap.
