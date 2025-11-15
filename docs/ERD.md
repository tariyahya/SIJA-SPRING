# ERD (Versi Teks)

## Entitas & Atribut Utama

### ROLE
- `id_role` (PK)
- `nama_role` (ADMIN, GURU, SISWA)

### USER
- `id_user` (PK)
- `username`
- `password_hash`
- `email`
- `status_aktif`
- `role_id` (FK → ROLE.id_role)

### KELAS
- `id_kelas` (PK)
- `nama_kelas`
- `tingkat`
- `jurusan`
- `wali_kelas_id` (FK → GURU.id_guru)

### SISWA
- `id_siswa` (PK)
- `nis`
- `nama_lengkap`
- `jenis_kelamin`
- `kelas_id` (FK → KELAS.id_kelas)
- `jurusan`
- `rfid_card_id`
- `barcode_id`
- `face_id`
- `user_id` (FK → USER.id_user)
- `status_aktif`

### GURU
- `id_guru` (PK)
- `nip`
- `nama_lengkap`
- `mapel_utama`
- `rfid_card_id`
- `barcode_id`
- `face_id`
- `user_id` (FK → USER.id_user)
- `status_aktif`

### LOKASI_KANTOR / AREA_PRESENSI
- `id_lokasi` (PK)
- `nama_lokasi`
- `alamat`
- `latitude`
- `longitude`
- `radius_meter`

### DEVICE
- `id_device` (PK)
- `nama_device`
- `tipe_device` (RFID_READER / MOBILE / DESKTOP)
- `lokasi_id` (FK → LOKASI_KANTOR.id_lokasi)
- `serial_number`

### PRESENSI
- `id_presensi` (PK)
- `user_id` (FK → USER.id_user)
- `tipe_user` (SISWA / GURU)
- `tanggal`
- `jam_masuk`
- `jam_pulang`
- `status` (HADIR / TERLAMBAT / IZIN / SAKIT / ALFA)
- `method` (MANUAL / RFID / BARCODE / FACE)
- `lokasi_id` (FK → LOKASI_KANTOR.id_lokasi)
- `latitude`
- `longitude`
- `foto_path`
- `device_id` (FK → DEVICE.id_device)
- `keterangan`

## Relasi (Ringkasan)

```
ROLE 1 ----- N USER 1 ----- N PRESENSI
                     |           |
                     |0..1       |N
                     |           |
                   SISWA       LOKASI_KANTOR 1 ----- N PRESENSI
                     |
                     |0..1
                     |
                   GURU

KELAS 1 ----- N SISWA
GURU 1 ----- N KELAS (wali kelas)
LOKASI_KANTOR 1 ----- N DEVICE
```

Gunakan dokumen ini sebagai referensi saat menggambar ERD di tools visual seperti draw.io atau dbdiagram.io.
