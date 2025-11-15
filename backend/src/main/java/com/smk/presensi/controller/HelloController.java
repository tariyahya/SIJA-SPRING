package com.smk.presensi.controller;

// Import yang diperlukan
import java.util.Map;  // Map adalah struktur data key-value (seperti dictionary di Python)

import org.springframework.web.bind.annotation.GetMapping;  // Untuk endpoint GET
import org.springframework.web.bind.annotation.RequestMapping;  // Untuk base URL
import org.springframework.web.bind.annotation.RestController;  // Menandai kelas ini sebagai REST API controller

/**
 * HELLO CONTROLLER - Controller pertama kita (Tahap 1)
 * 
 * Controller adalah kelas yang menangani HTTP request dari client.
 * Client bisa berupa:
 * - Browser (Chrome, Firefox, dll)
 * - Postman (untuk testing API)
 * - Mobile app (Android/iOS)
 * - Aplikasi desktop
 * 
 * Analogi: Controller seperti "resepsionis" yang menerima permintaan dan memberikan jawaban.
 */
@RestController  // Anotasi ini memberitahu Spring: "Kelas ini adalah REST Controller"
                 // REST Controller = Controller yang mengembalikan DATA (JSON/XML), bukan HTML
                 // Semua method di kelas ini otomatis convert return value jadi JSON
@RequestMapping("/api")  // Base URL untuk semua endpoint di controller ini
                         // Artinya: semua endpoint diawali dengan /api
                         // Contoh: /api/hello, /api/info, /api/test
                         // Kenapa pakai /api? Supaya mudah bedakan endpoint API dengan halaman web
public class HelloController {

    /**
     * ENDPOINT PERTAMA: GET /api/hello
     * 
     * Method ini dipanggil saat client mengakses http://localhost:8081/api/hello
     * 
     * Cara test:
     * 1. Buka browser, ketik: http://localhost:8081/api/hello
     * 2. Atau pakai Postman: GET http://localhost:8081/api/hello
     */
    @GetMapping("/hello")  // Anotasi ini mendaftarkan method sebagai endpoint GET /api/hello
                           // GET = method HTTP untuk MENGAMBIL data (tidak mengubah data di server)
                           // URL lengkap: /api (dari @RequestMapping) + /hello = /api/hello
    public Map<String, String> hello() {
        // Map.of() membuat Map (key-value) dengan cepat
        // Key: "message" → Value: "Presensi SMK – Tahap 1"
        // 
        // Spring Boot otomatis convert Map ini jadi JSON:
        // {"message": "Presensi SMK – Tahap 1"}
        // 
        // Return type Map<String, String> artinya:
        // - Key bertipe String ("message")
        // - Value bertipe String ("Presensi SMK – Tahap 1")
        return Map.of("message", "Presensi SMK – Tahap 1");
    }

    /**
     * ENDPOINT KEDUA: GET /api/info
     * 
     * Method ini mengembalikan informasi aplikasi dalam bentuk object.
     * URL: http://localhost:8081/api/info
     */
    @GetMapping("/info")  // Endpoint GET /api/info
    public AppInfo info() {
        // Membuat object AppInfo baru dengan constructor
        // new AppInfo(...) = panggil constructor dengan 3 parameter
        // 
        // Spring Boot otomatis convert object AppInfo ini jadi JSON:
        // {
        //   "app": "Presensi Siswa & Guru",
        //   "version": "0.1",
        //   "developer": "Nama_Kelompok"
        // }
        return new AppInfo(
            "Presensi Siswa & Guru",  // Parameter 1: nama aplikasi
            "0.1",                     // Parameter 2: versi
            "Nama_Kelompok"            // Parameter 3: developer/kelompok
        );
    }

    /**
     * RECORD APPINFO - Kelas data sederhana
     * 
     * Record adalah fitur Java 14+ untuk membuat kelas data dengan cepat.
     * Record otomatis generate:
     * - Constructor dengan semua field
     * - Getter untuk semua field (app(), version(), developer())
     * - toString(), equals(), hashCode()
     * 
     * Sebelum ada record, kita harus buat class dengan:
     * - private field
     * - constructor
     * - getter
     * - setter (jika perlu)
     * - toString(), equals(), hashCode() (jika perlu)
     * 
     * Record membuat semua itu otomatis dalam 1 baris!
     * 
     * Analogi: Record seperti "template form" yang sudah jadi - tinggal isi data.
     */
    public record AppInfo(
        String app,        // Field pertama: nama aplikasi
        String version,    // Field kedua: versi aplikasi
        String developer   // Field ketiga: nama developer/kelompok
    ) {
        // Tidak perlu tambah apa-apa lagi!
        // Record otomatis bikin constructor, getter, dll
    }
}
