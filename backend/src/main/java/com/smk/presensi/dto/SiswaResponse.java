package com.smk.presensi.dto;

/**
 * DTO (Data Transfer Object) untuk MENGIRIM data siswa KE CLIENT.
 * 
 * Perbedaan dengan SiswaRequest:
 * - SiswaRequest = untuk MENERIMA data dari client (input) → tidak ada id, ada validasi
 * - SiswaResponse = untuk MENGIRIM data ke client (output) → ada id, tanpa validasi
 * 
 * Kenapa ada id disini?
 * - Client perlu tahu id untuk operasi UPDATE atau DELETE
 * - Contoh: Client tampilkan tombol "Edit" dan "Delete", butuh id untuk kirim ke server
 * 
 * Kenapa tidak pakai validasi (@NotBlank)?
 * - Data ini KELUAR dari server, sudah pasti valid (sudah tersimpan di database)
 * - Validasi hanya untuk data MASUK (input dari user yang tidak terpercaya)
 * 
 * Contoh penggunaan:
 * 1. GET /api/siswa → return List<SiswaResponse>
 * 2. GET /api/siswa/1 → return SiswaResponse
 * 3. POST /api/siswa → return SiswaResponse (data yang baru tersimpan)
 * 
 * Analogi sederhana:
 * - Request = formulir yang diisi user (bisa salah, perlu validasi)
 * - Response = struk/bukti dari kasir (sudah benar, tidak perlu validasi lagi)
 */
public record SiswaResponse(
    // id: Primary key dari database
    // Ini AUTO-INCREMENT, jadi client tidak kirim, tapi server kirim balik ke client
    // Contoh value: 1, 2, 3, 4, dst
    Long id,
    
    // nis: Nomor Induk Siswa (unique identifier untuk siswa)
    // Contoh: "2024001", "2024002"
    String nis,
    
    // nama: Nama lengkap siswa
    // Contoh: "Budi Santoso", "Siti Nurhaliza"
    String nama,
    
    // kelas: Kelas siswa saat ini
    // Contoh: "XII RPL 1", "XI TKJ 2"
    // Optional: bisa null jika siswa belum ditentukan kelasnya
    String kelas,
    
    // jurusan: Jurusan/program keahlian siswa
    // Contoh: "RPL" (Rekayasa Perangkat Lunak), "TKJ" (Teknik Komputer Jaringan)
    // Optional: bisa null
    String jurusan
) {
    // Record auto-generate:
    // - Constructor dengan 5 parameter (id, nis, nama, kelas, jurusan)
    // - Getter methods: id(), nis(), nama(), kelas(), jurusan()
    // - toString(): otomatis generate format "SiswaResponse[id=1, nis=2024001, ...]"
    // - equals() dan hashCode(): otomatis compare semua field
    // 
    // Semua ini GRATIS tanpa kita tulis kode!
}
