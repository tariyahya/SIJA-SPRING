package com.smk.presensi;

// Import adalah cara Java menggunakan kelas dari library lain
// SpringApplication dan SpringBootApplication adalah kelas dari Spring Boot framework
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * KELAS UTAMA APLIKASI SPRING BOOT
 * 
 * Kelas ini adalah "pintu masuk" aplikasi kita.
 * Saat kita jalankan "mvn spring-boot:run", method main() di kelas ini yang pertama dipanggil.
 * 
 * Analogi: Seperti saklar lampu utama di rumah - kalau ini dinyalakan, semua sistem aplikasi akan hidup.
 */
@SpringBootApplication  // Anotasi (annotation) adalah "penanda" khusus untuk Spring
                        // @SpringBootApplication memberitahu Spring: "Ini adalah aplikasi Spring Boot!"
                        // Anotasi ini sebenarnya gabungan dari 3 anotasi:
                        // 1. @Configuration: Menandai kelas ini sebagai sumber konfigurasi
                        // 2. @EnableAutoConfiguration: Spring otomatis setup database, server, dll
                        // 3. @ComponentScan: Spring otomatis mencari controller, service, dll di package ini
public class PresensiApplication {

    /**
     * METHOD MAIN - TITIK AWAL APLIKASI
     * 
     * Method main() adalah method khusus di Java yang SELALU dipanggil pertama kali.
     * Semua aplikasi Java HARUS punya method main().
     * 
     * @param args = argument yang bisa dikirim saat menjalankan aplikasi (jarang dipakai)
     */
    public static void main(String[] args) {
        // SpringApplication.run() adalah method yang melakukan SEMUA pekerjaan startup:
        // 
        // Yang dilakukan method ini:
        // 1. Membuat "Spring Container" (tempat menyimpan semua component seperti controller, service)
        // 2. Memulai embedded Tomcat server (web server) di port 8081
        // 3. Scan semua kelas dengan @Component, @Service, @Repository, @Controller
        // 4. Koneksi ke database (H2)
        // 5. Setup semua endpoint REST API agar bisa diakses
        // 
        // Parameter pertama: PresensiApplication.class = kelas ini sendiri
        // Parameter kedua: args = argument dari command line
        SpringApplication.run(PresensiApplication.class, args);
        
        // Setelah baris di atas selesai:
        // ✅ Server web sudah hidup di http://localhost:8081
        // ✅ Semua endpoint API sudah bisa diakses (GET, POST, PUT, DELETE)
        // ✅ Database H2 sudah siap digunakan
        // ✅ H2 Console bisa diakses di http://localhost:8081/h2-console
        // 
        // Aplikasi akan terus berjalan sampai kita stop (Ctrl+C di terminal)
    }
}
