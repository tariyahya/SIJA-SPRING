# PLAN KELENGKAPAN SISTEM PRESENSI SMK

**Tanggal Pembuatan**: 18 November 2025  
**Project**: SIJA Spring - Sistem Presensi SMK  
**Target**: Sistem presensi yang komprehensif untuk SMK

---

## OVERVIEW

Dokumen ini berisi rencana lengkap untuk menambahkan fitur-fitur penting yang masih kurang dalam sistem presensi SMK, dengan fokus khusus pada:

1. Perizinan dengan role Guru Piket & Pembimbing PKL  
2. Lokasi kantor untuk unit DUDI (Dunia Usaha Dunia Industri)  
3. Manajemen PKL (Praktik Kerja Lapangan)  
4. Fitur-fitur pendukung lainnya (dashboard, laporan, notifikasi, dsb.)

---

## FASE IMPLEMENTASI

### FASE 1: FOUNDATION (2–3 Minggu)
**Priority**: CRITICAL

#### 1.1 Manajemen Kelas & Jurusan

**Status per 18 November 2025**
- [x] CRUD Jurusan (backend + desktop)
- [x] CRUD Kelas (backend + desktop)
- [x] Assignment siswa ke kelas (endpoint backend + wiring desktop)
- [x] Capacity management per kelas
- [x] Laporan per kelas/jurusan

**Complexity**: SEDANG  
**Estimated Time**: 3–4 hari

**Features yang direncanakan**
- CRUD Jurusan (AKL, TKR, TBO, SIJA, SIJA+, dll.)
- CRUD Kelas (dengan wali kelas, tingkat, tahun ajaran)
- Assignment siswa ke kelas:
  - Pilih beberapa siswa lalu assign ke kelas tertentu
  - Sinkron jurusan siswa dengan jurusan kelas
- Capacity management:
  - Batas maksimal siswa per kelas
  - Peringatan jika kapasitas terlampaui
- Reports:
  - Rekap siswa per kelas
  - Rekap siswa per jurusan

**Deliverables**
- Backend:
  - `Jurusan` entity, repository, service, controller
  - `Kelas` entity, repository, service, controller
  - Endpoint assignment siswa -> kelas  
    `POST /api/kelas/{id}/assign-siswa`
- Desktop:
  - `JurusanManagementController` + `jurusan-management.fxml`
  - `KelasManagementController` + `kelas-management.fxml`
  - Dialog assign siswa ke kelas (multi-select)
- Database:
  - `jurusan` (id, kode, nama, durasi_tahun, ketua_jurusan_id)
  - `kelas` (id, nama, jurusan_id / jurusan, tingkat, tahun_ajaran, wali_kelas_id, kapasitas)

#### 1.2 Role Management Enhancement

**Status**: BELUM DIKERJAKAN  
**Complexity**: TINGGI  
**Estimated Time**: 2–3 hari

**New Roles**
- ROLE_GURU_PIKET (Guru yang bertugas piket hari itu)
- ROLE_GURU_PEMBIMBING (Pembimbing PKL)
- ROLE_GURU_BK (Bimbingan Konseling)
- ROLE_WAKAKURIKULUM (Wakil Kepala Kurikulum)
- ROLE_WAKAHUBIN (Wakil Kepala Hubungan Industri)
- ROLE_KAPROG (Ketua Program Keahlian/Jurusan)

**Garis besar permission matrix (ringkas)**
- Input presensi: Admin, Guru, Guru Piket
- Approve izin: Admin, Guru Piket, Wali Kelas, BK, Pembimbing PKL, Wakakurikulum (sesuai jenis izin)
- Manage PKL: Admin, Wakahubin, Pembimbing PKL
- View all data: Admin, Wakakurikulum, BK, Kaprog (read-only tertentu)
- Generate report: Admin, Wakakurikulum, Kaprog
- Surat peringatan: Admin, BK, Wali Kelas

**Deliverables**
- Update `Role.RoleName` enum dengan roles baru
- Update `SecurityConfig` / konfigurasi Spring Security
- Permission service layer (helper untuk cek izin)
- UI assignment role ke user (desktop)
- Dokumentasi: Role & Permission Guide

---

### FASE 2: PERIZINAN & PKL (3–4 Minggu)
**Priority**: CRITICAL

#### 2.1 Modul Perizinan (Izin & Dispensasi)

**Status**: BELUM DIKERJAKAN  
**Complexity**: SANGAT TINGGI  
**Estimated Time**: 5–7 hari

**A. Pengajuan Izin**
- Form pengajuan izin siswa
  - Jenis: SAKIT / IZIN / DISPENSASI
  - Tanggal & durasi
  - Alasan detail
  - Upload surat pendukung (PDF / gambar)
  - Kontak orang tua/wali
  - Auto-save as draft
- Jenis izin khusus:
  - Izin sakit (dengan surat dokter)
  - Izin urusan keluarga
  - Dispensasi lomba/kompetisi
  - Dispensasi PKL (khusus siswa PKL)
  - Dispensasi kegiatan sekolah
- Flow pengajuan:
  - Siswa -> notifikasi otomatis -> Guru Piket (hari itu) -> Approval

**B. Approval Workflow**
- Dashboard Guru Piket:
  - List pengajuan izin hari ini
  - Quick approve/reject
  - View surat pendukung
  - Tambah catatan/komentar
  - Verifikasi via telepon orang tua
  - Batch approval (multi-select)
- Escalation Rules:
  - Izin > 3 hari -> butuh approval Wali Kelas
  - Izin > 7 hari -> butuh approval BK
  - Dispensasi PKL -> approval Pembimbing PKL
  - Dispensasi lomba -> approval Wakakurikulum
- Notifikasi:
  - SMS ke orang tua (approved/rejected)
  - Email
  - In-app notification
  - (Opsional) WhatsApp integration

**C. Tracking & Monitoring**
- Status: PENDING / APPROVED / REJECTED / EXPIRED
- History pengajuan per siswa
- Alert: siswa dengan frekuensi izin tinggi
- Report: izin per kelas / jurusan / bulan
- Integrasi dengan presensi (auto-mark HADIR/IZIN)

**D. Guru Piket Features**
- Manajemen jadwal piket:
  - Assign guru piket per hari
  - Shift pagi/siang (jika ada)
  - Template jadwal bulanan
  - Swap jadwal antar guru
- Dashboard piket hari ini:
  - Summary presensi real-time
  - List siswa terlambat
  - List siswa izin hari ini
  - Quick action: tandai terlambat, input izin manual
  - Export rekap harian
- Logbook piket:
  - Catatan kejadian
  - Tindakan yang diambil
  - Siswa bermasalah
  - Handover ke piket berikutnya

**Deliverables (Perizinan)**
- Backend:
  - `IzinController`, `IzinService`, DTO request/response
  - `GuruPiketController`, `JadwalPiketController`
  - `FileUploadService` (surat pendukung)
- Desktop:
  - `IzinManagementController`, `ApprovalIzinController`, `JadwalPiketController`
  - FXML: `izin-form-dialog.fxml`, `approval-izin.fxml`, `dashboard-piket.fxml`
- Database:
  - `izin`, `jadwal_piket`, `logbook_piket`
- Dokumentasi:
  - User guide perizinan & guru piket
  - API documentation untuk endpoint izin

#### 2.2 Modul PKL (Praktik Kerja Lapangan)

**Status**: BELUM DIKERJAKAN  
**Complexity**: SANGAT TINGGI  
**Estimated Time**: 7–10 hari

Garis besar:
- Manajemen DUDI (perusahaan) dan penempatan siswa PKL
- Jadwal PKL dan shift
- Presensi PKL berbasis GPS / geofence
- Dashboard pembimbing PKL
- Jurnal PKL, evaluasi, dan laporan ke sekolah

---

### SHOULD HAVE (Sangat Direkomendasikan)

- Manajemen DUDI & penempatan PKL
- Presensi PKL (GPS-based)
- Dashboard Pembimbing PKL
- Jadwal pelajaran (integrasi dengan presensi)
- Surat peringatan otomatis (berdasarkan akumulasi pelanggaran)

### NICE TO HAVE (Jika Ada Waktu)

- Jurnal PKL (harian/mingguan)
- Kunjungan industri tracking
- Custom report builder
- Advanced analytics (heatmap keterlambatan, dsb.)
- Mobile app (Android/iOS)

---

## NEXT STEPS (PLAN)

### Langkah Selanjutnya
1. Review plan dengan stakeholder (Kepala Sekolah, Wakakurikulum, Waka Hubsin, BK).  
2. Prioritaskan fitur yang paling urgent (kemungkinan besar: perizinan + PKL).  
3. Susun tim dan pembagian tugas per modul (backend, desktop, mobile).  
4. Buat sprint plan 2 minggu per fase.  
5. Mulai development dari FASE 1 (foundation) bila belum stabil, atau langsung FASE 2 jika foundation sudah cukup.

### Rekomendasi Prioritas
- Mulai dari FASE 2.1 (Perizinan) dan FASE 2.2 (PKL) karena:
  - Paling sering disentuh dalam operasi harian
  - Melibatkan role baru (Guru Piket, Pembimbing PKL)
  - Menjadi dasar untuk fitur lain (surat peringatan, laporan, dsb.)
  - Dampak langsung ke kedisiplinan dan monitoring siswa

---

## PROGRESS IMPLEMENTASI (LOG SINGKAT)

### Backend

- [x] Entity `Jurusan` + repository `JurusanRepository`
- [x] DTO `JurusanRequest` & `JurusanResponse` + `JurusanService`
- [x] REST API `JurusanController`:
  - GET /api/jurusan
  - GET /api/jurusan/{id}
  - POST /api/jurusan
  - PUT /api/jurusan/{id}
  - DELETE /api/jurusan/{id}
- [x] CRUD `Kelas`:
  - DTO `KelasRequest` & `KelasResponse`
  - `KelasService` (validasi `existsByNama`)
  - `KelasRepository` (method tambahan `findByNama`, `existsByNama`)
  - `KelasController` endpoint:
    - GET /api/kelas
    - GET /api/kelas/{id}
    - GET /api/kelas/jurusan/{jurusan}
    - POST /api/kelas
    - PUT /api/kelas/{id}
    - DELETE /api/kelas/{id}
- [x] Assignment siswa -> kelas:
  - DTO `AssignSiswaToKelasRequest` (field `siswaIds`)
  - Method `SiswaService.assignToKelas(kelasId, request)`
  - Endpoint: POST /api/kelas/{id}/assign-siswa (return List<SiswaResponse>)

### Desktop App

- [x] Model:
  - `Jurusan` (desktop model)
  - `Kelas` (desktop model)
- [x] Service:
  - `JurusanService` (GET/POST/PUT/DELETE `/api/jurusan`)
  - `KelasService` (GET/POST/PUT/DELETE `/api/kelas`, GET by jurusan)
  - `KelasService.assignSiswaToKelas(kelasId, siswaIds)` -> POST `/api/kelas/{id}/assign-siswa`
- [x] View & controller:
  - `jurusan-management.fxml` + `JurusanManagementController` (CRUD jurusan)
  - `kelas-management.fxml` + `KelasManagementController`:
    - CRUD kelas
    - Dialog assign siswa ke kelas (multi-select)
    - Panggil endpoint backend untuk assignment
- [x] Build backend dan desktop-app sukses (`mvn -DskipTests package` untuk masing-masing modul).

### Next Recommended (Belum Dikerjakan)

**Desktop & Navigasi**
- [x] Integrasi manajemen kelas/jurusan ke dashboard desktop (menu & navigasi):
  - Tambah menu/button di dashboard untuk membuka `jurusan-management.fxml` dan `kelas-management.fxml`.
  - Pastikan session / JWT tetap terbawa saat berpindah scene.

**Laporan & Analytics**
- [x] Laporan / rekap siswa per kelas & jurusan di modul laporan/analytics:
  - Endpoint backend: rekap siswa per kelas & jurusan.
  - Tampilan desktop: tabel + export (CSV/Excel) untuk per kelas/per jurusan.

**Role & Security**
- [x] Implementasi Fase 1.2 (Role Management Enhancement):
  - Tambah role-role baru di backend (`Role.RoleName`) dan seed default.
  - Update konfigurasi Spring Security + penyesuaian endpoint yang butuh role baru.
  - UI desktop untuk assignment role ke user.

**Perizinan & PKL (Start Fase 2)**
- [x] MVP Modul Perizinan (2.1):
  - Backend: minimal endpoint pengajuan izin + approval oleh Guru Piket.
  - Desktop: form pengajuan izin dan layar approval sederhana.
- [x] MVP Modul PKL (2.2):
  - Backend: master DUDI + penempatan siswa PKL.
  - Desktop: manajemen DUDI dan mapping siswa -> DUDI.
