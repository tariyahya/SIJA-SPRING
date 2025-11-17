package com.smk.presensi.repository;

import com.smk.presensi.entity.Guru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository untuk mengakses data GURU dari database.
 * 
 * Sama seperti SiswaRepository, ini adalah INTERFACE bukan CLASS.
 * Spring Data JPA akan otomatis buatkan implementasinya untuk kita!
 * 
 * Perbedaan dengan SiswaRepository:
 * - SiswaRepository menggunakan Entity Siswa
 * - GuruRepository menggunakan Entity Guru
 * - Query method disesuaikan (findByNip untuk guru, findByNis untuk siswa)
 * 
 * Selain method yang kita definisikan (findByNip), kita juga dapat GRATIS:
 * - save(guru) → insert atau update guru
 * - findById(id) → cari guru berdasarkan id
 * - findAll() → ambil semua guru
 * - deleteById(id) → hapus guru berdasarkan id
 * - count() → hitung jumlah guru
 * - existsById(id) → cek apakah guru dengan id tertentu ada
 * Dan masih banyak lagi...
 */
@Repository // Memberitahu Spring: ini adalah komponen Repository (akses database)
public interface GuruRepository extends JpaRepository<Guru, Long> {
    // JpaRepository<Guru, Long> artinya:
    // - Guru: Entity yang dikelola
    // - Long: Tipe data dari Primary Key (id)
    
    /**
     * Cari guru berdasarkan NIP (Nomor Induk Pegawai).
     * 
     * Cara kerja Spring Data JPA:
     * 1. Lihat nama method: findByNip
     * 2. Parse "findBy" = query untuk cari data
     * 3. Parse "Nip" = field 'nip' di Entity Guru
     * 4. Otomatis generate SQL: SELECT * FROM GURU WHERE nip = ?
     * 
     * Return type: Optional<Guru>
     * - Optional = wadah yang bisa berisi Guru atau kosong
     * - Jika NIP ditemukan → Optional berisi Guru
     * - Jika NIP tidak ditemukan → Optional kosong
     * 
     * Contoh penggunaan:
     * Optional<Guru> result = guruRepository.findByNip("198001012005011001");
     * if (result.isPresent()) {
     *     Guru guru = result.get();
     *     System.out.println("Guru ditemukan: " + guru.getNama());
     * } else {
     *     System.out.println("Guru tidak ditemukan");
     * }
     * 
     * Atau cara modern (Java 8+):
     * guruRepository.findByNip("198001012005011001")
     *     .ifPresent(guru -> System.out.println("Nama: " + guru.getNama()));
     * 
     * @param nip Nomor Induk Pegawai yang dicari (harus UNIQUE di database)
     * @return Optional berisi Guru jika ditemukan, Optional.empty() jika tidak
     */
    Optional<Guru> findByNip(String nip);
    
    /**
     * FIND BY RFID CARD ID - Cari guru berdasarkan RFID Card ID.
     * 
     * Generated SQL:
     * SELECT * FROM guru WHERE rfid_card_id = ?
     * 
     * Use case:
     * - RFID checkin: cari guru yang punya kartu ini
     * - Validasi kartu: apakah kartu ini terdaftar?
     * 
     * @param rfidCardId ID kartu RFID yang dicari
     * @return Optional<Guru> (ada jika terdaftar, empty jika tidak)
     */
    Optional<Guru> findByRfidCardId(String rfidCardId);
    
    /**
     * FIND BY BARCODE ID - Cari guru berdasarkan Barcode ID.
     * 
     * Generated SQL:
     * SELECT * FROM guru WHERE barcode_id = ?
     * 
     * Use case:
     * - Barcode checkin: cari guru yang punya barcode ini
     * - Validasi barcode: apakah barcode ini terdaftar?
     * 
     * @param barcodeId ID barcode yang dicari
     * @return Optional<Guru> (ada jika terdaftar, empty jika tidak)
     */
    Optional<Guru> findByBarcodeId(String barcodeId);
    
    // Query method lain yang bisa ditambahkan (belum kita pakai):
    // Optional<Guru> findByNama(String nama); → cari guru berdasarkan nama
    // List<Guru> findByMapel(String mapel); → cari semua guru yang mengajar mata pelajaran tertentu
    // List<Guru> findByNamaContaining(String keyword); → cari guru yang namanya mengandung keyword
    // boolean existsByNip(String nip); → cek apakah NIP sudah terdaftar (untuk validasi)
    
    // Semua query method ini TIDAK PERLU implementasi!
    // Spring Data JPA akan otomatis generate SQL-nya berdasarkan nama method.
}
