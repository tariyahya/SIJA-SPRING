package com.smk.presensi.dto;

// Import anotasi validasi
import jakarta.validation.constraints.NotBlank;  // Untuk validasi field tidak boleh kosong

/**
 * SISWA REQUEST DTO - Data Transfer Object untuk INPUT
 * 
 * DTO (Data Transfer Object) adalah object untuk transfer data antar layer.
 * SiswaRequest digunakan untuk MENERIMA data dari client (Postman, mobile app, dll).
 * 
 * Kenapa perlu DTO? Kenapa tidak langsung pakai Entity?
 * 1. SECURITY: Entity punya field yang tidak boleh di-set client (id, createdAt, dll)
 * 2. VALIDATION: DTO bisa tambah validasi khusus untuk input
 * 3. FLEXIBILITY: Struktur input bisa beda dengan struktur database
 * 4. CLEANER: Pisahkan concern - Entity untuk database, DTO untuk API
 * 
 * Contoh:
 * Client kirim JSON via POST /api/siswa:
 * {
 *   "nis": "12345",
 *   "nama": "Budi",
 *   "kelas": "XII RPL 1",
 *   "jurusan": "RPL"
 * }
 * 
 * Spring Boot otomatis convert JSON itu jadi object SiswaRequest.
 * 
 * Analogi: SiswaRequest seperti "formulir pendaftaran" yang harus diisi client.
 */
public record SiswaRequest(
    // Record parameter = field yang harus diisi saat create object
    // Setiap parameter bisa diberi anotasi validasi
    
    /**
     * NIS - Nomor Induk Siswa
     * WAJIB diisi (tidak boleh kosong/blank)
     */
    @NotBlank(message = "NIS tidak boleh kosong")  
         // @NotBlank = anotasi validasi dari Jakarta Bean Validation
         // Artinya: field ini TIDAK BOLEH:
         // ❌ null (tidak ada nilai)
         // ❌ "" (string kosong)
         // ❌ "   " (string hanya spasi)
         // 
         // message = pesan error kalau validasi gagal
         // Pesan ini akan dikirim ke client jika NIS kosong
    String nis,  // Field pertama: NIS (wajib)
    
    /**
     * NAMA - Nama lengkap siswa
     * WAJIB diisi (tidak boleh kosong/blank)
     */
    @NotBlank(message = "Nama tidak boleh kosong")  // Validasi: WAJIB diisi
    String nama,  // Field kedua: Nama (wajib)
    
    /**
     * KELAS - Kelas siswa
     * TIDAK WAJIB (boleh kosong)
     * Tidak ada anotasi validasi = boleh null/kosong
     */
    String kelas,  // Field ketiga: Kelas (opsional)
    
    /**
     * JURUSAN - Jurusan siswa
     * TIDAK WAJIB (boleh kosong)
     */
    String jurusan  // Field keempat: Jurusan (opsional)
) {
    // Body record kosong
    // Record otomatis generate:
    // 1. Constructor: new SiswaRequest(nis, nama, kelas, jurusan)
    // 2. Getter: nis(), nama(), kelas(), jurusan()
    // 3. toString(), equals(), hashCode()
    
    // CATATAN PENTING:
    // Validasi @NotBlank akan dijalankan saat controller terima request dengan @Valid.
    // Contoh di controller:
    // @PostMapping
    // public ResponseEntity<SiswaResponse> create(@RequestBody @Valid SiswaRequest request) {
    //     // @Valid memicu validasi @NotBlank
    //     // Kalau validasi gagal, Spring otomatis return 400 Bad Request
    // }
}
