# Strategi GitHub untuk Pengajaran

## Repo Guru (`presensi-siswa-guru-master`)
- `main`: versi final lengkap untuk demo dan dokumentasi.
- `dev`: area kerja sebelum materi dipaketkan ke branch tahap.
- Branch per tahap: `tahap-00-setup` s.d. `tahap-10-deploy-basic`.
- Setelah branch tahap stabil, jangan diubah agar menjadi checkpoint referensi.
- Gunakan tag opsional (`v0.1-setup`, `v0.2-crud`, dst.) untuk rilis kelas.

## Repo Template Siswa (`presensi-siswa-guru-template`)
1. Jadikan Template Repository di GitHub.
2. Isi minimal skeleton tahap 01.
3. Siswa klik **Use this template** → membuat repo pribadi.
4. Setiap pertemuan, guru mengarahkan siswa agar menyelesaikan README tahap terkait.

## Branching di Repo Siswa (opsional)
- `main`: kumpulan hasil akhir tiap level.
- `tahap-0X-kerja`: branch kerja per tahap.
- Alur: checkout branch kerja → coding → tes → merge ke `main` setelah fitur selesai.

## Praktik Baik Tambahan
- Lindungi branch `main` di repo guru (protected branch).
- Sertakan README pada setiap branch tahap yang menjelaskan tujuan, langkah, dan checklist.
- Simpan artefak pendukung (ERD, flow, Postman) dalam folder `docs/`.
- Dorong siswa melakukan commit kecil dan deskriptif agar mudah review.
