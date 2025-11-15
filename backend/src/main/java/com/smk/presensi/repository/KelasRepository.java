package com.smk.presensi.repository;

import com.smk.presensi.entity.Kelas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository untuk mengakses data KELAS dari database.
 * 
 * Sama seperti SiswaRepository dan GuruRepository, ini adalah INTERFACE.
 * Spring Data JPA akan otomatis generate implementasi class-nya!
 * 
 * Fokus repository ini:
 * - Mengelola data kelas (nama kelas, tingkat, jurusan, wali kelas)
 * - Menyediakan query method untuk cari kelas berdasarkan kriteria tertentu
 * 
 * Method gratis dari JpaRepository<Kelas, Long>:
 * - save(kelas) → tambah atau update kelas
 * - findById(id) → cari kelas berdasarkan id
 * - findAll() → ambil semua kelas
 * - deleteById(id) → hapus kelas
 * - count() → hitung total kelas
 * Dan masih banyak lagi...
 */
@Repository // Tandai sebagai komponen Repository (lapisan akses database)
public interface KelasRepository extends JpaRepository<Kelas, Long> {
    // JpaRepository<Kelas, Long> artinya:
    // - Kelas: Entity yang dikelola
    // - Long: Tipe data Primary Key (id)
    
    /**
     * Cari semua kelas berdasarkan JURUSAN.
     * 
     * Cara kerja Spring Data JPA:
     * 1. Lihat nama method: findByJurusan
     * 2. Parse "findBy" = query untuk cari data
     * 3. Parse "Jurusan" = field 'jurusan' di Entity Kelas
     * 4. Otomatis generate SQL: SELECT * FROM KELAS WHERE jurusan = ?
     * 
     * Return type: List<Kelas>
     * - List = koleksi/daftar yang bisa berisi banyak Kelas
     * - Bisa return list kosong [] jika tidak ada kelas dengan jurusan tersebut
     * - Bisa return list dengan 1 atau lebih Kelas
     * 
     * Perbedaan dengan findByNip/findByNis:
     * - findByNip/findByNis return Optional<T> karena NIP/NIS UNIQUE (max 1 hasil)
     * - findByJurusan return List<T> karena jurusan TIDAK UNIQUE (bisa banyak kelas dengan jurusan sama)
     * 
     * Contoh penggunaan:
     * List<Kelas> kelasRPL = kelasRepository.findByJurusan("RPL");
     * System.out.println("Total kelas RPL: " + kelasRPL.size());
     * for (Kelas kelas : kelasRPL) {
     *     System.out.println(kelas.getNama()); // Output: XII RPL 1, XII RPL 2, XI RPL 1, dst
     * }
     * 
     * Cara modern (Java 8+ Stream):
     * kelasRepository.findByJurusan("TKJ")
     *     .forEach(kelas -> System.out.println(kelas.getNama()));
     * 
     * @param jurusan Kode jurusan yang dicari, contoh: "RPL", "TKJ", "MM", "TBSM"
     * @return List berisi semua kelas dengan jurusan tersebut (bisa kosong jika tidak ada)
     */
    List<Kelas> findByJurusan(String jurusan);
    
    // Query method lain yang mungkin berguna (belum kita buat):
    // List<Kelas> findByTingkat(String tingkat); → cari kelas berdasarkan tingkat (X, XI, XII)
    // List<Kelas> findByTingkatAndJurusan(String tingkat, String jurusan); → cari kelas berdasarkan tingkat DAN jurusan
    //     Contoh: findByTingkatAndJurusan("XII", "RPL") → cari kelas XII RPL
    // Optional<Kelas> findByNama(String nama); → cari kelas berdasarkan nama (XII RPL 1)
    // List<Kelas> findByWaliKelasId(Long waliKelasId); → cari kelas berdasarkan wali kelas tertentu
    // long countByJurusan(String jurusan); → hitung jumlah kelas per jurusan
    
    // Semua query method ini TIDAK PERLU kita implementasi!
    // Cukup tulis nama method sesuai pattern, Spring Data JPA handle sisanya.
}
