# Daftar Jurusan dan Tingkat Kelas

**Tanggal Update**: 18 November 2025  
**Sistem**: Presensi SIJA Spring

---

## 📋 DAFTAR JURUSAN

### 1. **AKL** - Akuntansi dan Keuangan Lembaga
- **Durasi**: 3 Tahun (Kelas X - XII)
- **Tingkat Kelas**: 
  - Kelas X AKL
  - Kelas XI AKL
  - Kelas XII AKL
- **Jumlah Rombel per Tingkat**: 2 kelas
- **Contoh Nama Kelas**: `X AKL 1`, `X AKL 2`, `XI AKL 1`, `XI AKL 2`, `XII AKL 1`, `XII AKL 2`

### 2. **TKR** - Teknik Kendaraan Ringan
- **Durasi**: 3 Tahun (Kelas X - XII)
- **Tingkat Kelas**: 
  - Kelas X TKR
  - Kelas XI TKR
  - Kelas XII TKR
- **Jumlah Rombel per Tingkat**: 2 kelas
- **Contoh Nama Kelas**: `X TKR 1`, `X TKR 2`, `XI TKR 1`, `XI TKR 2`, `XII TKR 1`, `XII TKR 2`

### 3. **TBO** - Teknik Bodi Otomotif
- **Durasi**: 3 Tahun (Kelas X - XII)
- **Tingkat Kelas**: 
  - Kelas X TBO
  - Kelas XI TBO
  - Kelas XII TBO
- **Jumlah Rombel per Tingkat**: 1 kelas
- **Contoh Nama Kelas**: `X TBO 1`, `XI TBO 1`, `XII TBO 1`

### 4. **SIJA** - Sistem Informasi, Jaringan, dan Aplikasi
- **Durasi**: 4 Tahun (Kelas X - XIII) ⭐
- **Tingkat Kelas**: 
  - Kelas X SIJA
  - Kelas XI SIJA
  - Kelas XII SIJA
  - Kelas XIII SIJA (Program Khusus)
- **Jumlah Rombel per Tingkat**: 1 kelas
- **Contoh Nama Kelas**: `X SIJA 1`, `XI SIJA 1`, `XII SIJA 1`, `XIII SIJA 1`

---

## 📊 SUMMARY

| Jurusan | Kode | Durasi | Tingkat Kelas | Total Rombel |
|---------|------|--------|---------------|--------------|
| Akuntansi dan Keuangan Lembaga | **AKL** | 3 Tahun | X - XII | 6 kelas |
| Teknik Kendaraan Ringan | **TKR** | 3 Tahun | X - XII | 6 kelas |
| Teknik Bodi Otomotif | **TBO** | 3 Tahun | X - XII | 3 kelas |
| Sistem Informasi, Jaringan, dan Aplikasi | **SIJA** | 4 Tahun | X - XIII | 4 kelas |
| **TOTAL** | | | | **19 kelas** |

---

## 🎓 PENJELASAN PROGRAM 4 TAHUN (SIJA)

Jurusan SIJA memiliki program 4 tahun dengan alasan:

### Keunggulan Program:
1. **Materi Lebih Mendalam**: 
   - Tahun ke-4 fokus pada project-based learning
   - Pengembangan portfolio professional
   - Magang industri yang lebih intensif

2. **Sertifikasi Professional**:
   - Persiapan sertifikasi internasional (CCNA, CompTIA, Oracle)
   - Workshop dan bootcamp dengan industri
   - Proyek akhir yang lebih kompleks

3. **Daya Saing Tinggi**:
   - Lulusan lebih siap kerja
   - Skill setara dengan fresh graduate D3/S1
   - Network industri yang lebih luas

### Tingkat Kelas XIII:
- **Status**: Kelas khusus jurusan SIJA
- **Fokus**: Praktik industri, project development, sertifikasi
- **Output**: Siap kerja atau lanjut kuliah dengan advanced standing

---

## 💻 IMPLEMENTASI DI SISTEM

### Backend (Spring Boot)
Tidak ada validasi khusus di backend karena jurusan dan kelas adalah string bebas. 
Namun untuk data integrity, pastikan:
- Jurusan harus salah satu dari: `AKL`, `TKR`, `TBO`, `SIJA`
- Kelas format: `{Tingkat} {Jurusan} {Nomor}` (contoh: `XIII SIJA 1`)

### Desktop App (JavaFX)
**File**: `SiswaManagementController.java`

**Filter Kelas** (line 135-138):
```java
kelasFilter.setItems(FXCollections.observableArrayList(
    "X AKL 1", "X AKL 2", "XI AKL 1", "XI AKL 2", "XII AKL 1", "XII AKL 2",
    "X TKR 1", "X TKR 2", "XI TKR 1", "XI TKR 2", "XII TKR 1", "XII TKR 2",
    "X TBO 1", "XI TBO 1", "XII TBO 1",
    "X SIJA 1", "XI SIJA 1", "XII SIJA 1", "XIII SIJA 1"
));
```

**Filter Jurusan** (line 141-143):
```java
jurusanFilter.setItems(FXCollections.observableArrayList(
    "AKL", "TKR", "TBO", "SIJA"
));
```

**Form Dialog** (line 253):
```java
jurusanCombo.setItems(FXCollections.observableArrayList("AKL", "TKR", "TBO", "SIJA"));
```

---

## 📝 CONTOH DATA

### Contoh Siswa AKL (3 Tahun)
```json
{
  "nis": "2024001",
  "nama": "Budi Santoso",
  "kelas": "XII AKL 1",
  "jurusan": "AKL"
}
```

### Contoh Siswa TKR (3 Tahun)
```json
{
  "nis": "2024015",
  "nama": "Ahmad Fauzi",
  "kelas": "XI TKR 2",
  "jurusan": "TKR"
}
```

### Contoh Siswa TBO (3 Tahun)
```json
{
  "nis": "2024030",
  "nama": "Dedi Supriadi",
  "kelas": "X TBO 1",
  "jurusan": "TBO"
}
```

### Contoh Siswa SIJA (4 Tahun) ⭐
```json
{
  "nis": "2024050",
  "nama": "Rina Wati",
  "kelas": "XIII SIJA 1",
  "jurusan": "SIJA"
}
```

---

## 🧪 TESTING

### Test dengan Postman
```bash
# Create siswa SIJA kelas XIII
POST http://localhost:8081/api/siswa
Content-Type: application/json

{
  "nis": "9999999999",
  "nama": "Test Siswa SIJA XIII",
  "kelas": "XIII SIJA 1",
  "jurusan": "SIJA"
}
```

### Test dengan Desktop App
1. Buka **Manajemen Siswa**
2. Klik **+ Tambah Siswa**
3. Isi form:
   - NIS: `9999999999`
   - Nama: `Test Siswa SIJA XIII`
   - Kelas: `XIII SIJA 1`
   - Jurusan: Pilih **SIJA** dari dropdown
4. Klik **OK**
5. Verifikasi data muncul di table

### Test Filter
1. **Filter Jurusan**: Pilih `SIJA` → Hanya siswa SIJA yang muncul
2. **Filter Kelas**: Pilih `XIII SIJA 1` → Hanya siswa kelas XIII SIJA 1
3. **Search**: Ketik "XIII" → Muncul semua siswa kelas XIII

---

## 📚 REFERENCES

### Peraturan Terkait
- **Permendikbudristek No. 7 Tahun 2022**: Tentang Standar Isi SMK/MAK
- **SK Dirjen Diksi**: Program 4 Tahun untuk jurusan tertentu

### Kurikulum
- **Kurikulum Merdeka SMK**: Struktur program 3-4 tahun
- **Profil Lulusan SIJA**: Junior Network Administrator, Web Developer, System Support

---

**CATATAN PENTING**:
- ✅ Semua jurusan (AKL, TKR, TBO) = 3 tahun (Kelas X - XII)
- ✅ SIJA khusus = 4 tahun (Kelas X - XIII)
- ✅ Kelas XIII hanya untuk SIJA
- ✅ Update sudah diterapkan di Desktop App dan dokumentasi

---

**END OF DOCUMENT**
