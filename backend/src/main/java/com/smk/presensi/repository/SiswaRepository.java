package com.smk.presensi.repository;

// Import yang diperlukan
import com.smk.presensi.entity.Siswa;  // Entity Siswa yang akan dikelola
import org.springframework.data.jpa.repository.JpaRepository;  // Interface utama JPA Repository
import org.springframework.stereotype.Repository;  // Anotasi untuk menandai sebagai repository

import java.util.List;  // List untuk return banyak data
import java.util.Optional;  // Optional untuk handle data yang mungkin tidak ada

/**
 * SISWA REPOSITORY - Interface untuk akses database Siswa
 * 
 * Repository adalah "pintu gerbang" untuk akses database.
 * Di sini kita definisikan method-method untuk CRUD (Create, Read, Update, Delete).
 * 
 * Kenapa pakai interface bukan class?
 * - Spring Data JPA yang bikin implementasinya otomatis!
 * - Kita cukup definisikan method, Spring yang buat kode SQL nya
 * 
 * Analogi: Repository seperti "kasir bank" yang handle transaksi database.
 */
@Repository  // Anotasi ini memberitahu Spring: "Ini adalah repository"
             // Spring akan otomatis create bean dari interface ini
public interface SiswaRepository extends JpaRepository<Siswa, Long> {
    // extends JpaRepository = mewarisi semua method bawaan JPA:
    // 
    // Method GRATIS yang langsung bisa dipakai (tidak perlu bikin sendiri):
    // ✅ save(entity)           - CREATE/UPDATE (kalau id ada, UPDATE; kalau id null, CREATE)
    // ✅ findById(id)           - READ by ID
    // ✅ findAll()              - READ semua data
    // ✅ deleteById(id)         - DELETE by ID
    // ✅ count()                - Hitung jumlah data
    // ✅ existsById(id)         - Cek apakah data dengan ID tertentu ada
    // Dan masih banyak lagi...
    // 
    // JpaRepository<Siswa, Long> artinya:
    // - Siswa = Entity yang dikelola (tabel SISWA)
    // - Long  = Tipe data primary key (id bertipe Long)
    
    /**
     * FIND BY NIS - Cari siswa berdasarkan NIS
     * 
     * Method ini menggunakan "Query Method" Spring Data JPA.
     * Spring otomatis generate SQL dari nama method!
     * 
     * Cara kerja:
     * - Nama method: findByNis
     * - Spring baca: "find" = SELECT, "By" = WHERE, "Nis" = kolom nis
     * - SQL yang dihasilkan: SELECT * FROM siswa WHERE nis = ?
     * 
     * Kenapa return Optional<Siswa>?
     * - Optional = "mungkin ada, mungkin tidak ada"
     * - Kalau siswa dengan NIS tersebut ada, return Optional berisi data Siswa
     * - Kalau tidak ada, return Optional kosong (empty)
     * - Ini lebih baik daripada return null (untuk avoid NullPointerException)
     * 
     * Contoh penggunaan:
     * Optional<Siswa> siswa = siswaRepository.findByNis("12345");
     * if (siswa.isPresent()) {
     *     System.out.println("Siswa ditemukan: " + siswa.get().getNama());
     * } else {
     *     System.out.println("Siswa tidak ditemukan");
     * }
     */
    Optional<Siswa> findByNis(String nis);
    
    /**
     * FIND BY KELAS - Cari semua siswa di kelas tertentu
     * 
     * Method ini juga menggunakan "Query Method".
     * Spring otomatis generate SQL: SELECT * FROM siswa WHERE kelas = ?
     * 
     * Kenapa return List<Siswa>?
     * - List = kumpulan/array data
     * - Satu kelas bisa punya banyak siswa
     * - Kalau tidak ada siswa di kelas tersebut, return List kosong [] (bukan null)
     * 
     * Contoh penggunaan:
     * List<Siswa> siswaKelas = siswaRepository.findByKelas("XII RPL 1");
     * System.out.println("Jumlah siswa: " + siswaKelas.size());
     * for (Siswa siswa : siswaKelas) {
     *     System.out.println(siswa.getNama());
     * }
     */
    List<Siswa> findByKelas(String kelas);
    
    /**
     * FIND BY RFID CARD ID - Cari siswa berdasarkan RFID Card ID.
     * 
     * Generated SQL:
     * SELECT * FROM siswa WHERE rfid_card_id = ?
     * 
     * Use case:
     * - RFID checkin: cari siswa yang punya kartu ini
     * - Validasi kartu: apakah kartu ini terdaftar?
     * 
     * @param rfidCardId ID kartu RFID yang dicari
     * @return Optional<Siswa> (ada jika terdaftar, empty jika tidak)
     */
    Optional<Siswa> findByRfidCardId(String rfidCardId);
    
    /**
     * FIND BY BARCODE ID - Cari siswa berdasarkan Barcode ID.
     * 
     * Generated SQL:
     * SELECT * FROM siswa WHERE barcode_id = ?
     * 
     * Use case:
     * - Barcode checkin: cari siswa yang punya barcode ini
     * - Validasi barcode: apakah barcode ini terdaftar?
     * 
     * @param barcodeId ID barcode yang dicari
     * @return Optional<Siswa> (ada jika terdaftar, empty jika tidak)
     */
    Optional<Siswa> findByBarcodeId(String barcodeId);
    
    /**
     * FIND ALL SISWA WITH ENROLLED FACE.
     * 
     * Generated SQL:
     * SELECT * FROM siswa WHERE face_encoding IS NOT NULL
     * 
     * Use case:
     * - Face recognition: cari semua siswa yang sudah enroll face
     * - Loop dan compare encoding dengan input
     * - Count berapa siswa yang sudah enroll
     * 
     * @return List<Siswa> yang punya face_encoding
     */
    List<Siswa> findByFaceEncodingIsNotNull();
    
    /**
     * FIND SISWA BY USER.
     * 
     * Generated SQL:
     * SELECT * FROM siswa WHERE user_id = ?
     * 
     * Use case:
     * - Face enrollment: cari siswa berdasarkan user untuk save encoding
     * - Check apakah user ini siswa atau guru
     * 
     * @param user User object
     * @return Optional<Siswa> (ada jika user adalah siswa)
     */
    Optional<Siswa> findByUser(com.smk.presensi.entity.User user);
    
    // CATATAN PENTING:
    // Method di atas TIDAK PERLU implementasi!
    // Spring Data JPA otomatis bikin SQL dan implementasinya.
    // 
    // Magic? Tidak! Spring pakai reflection dan naming convention:
    // - findBy + NamaField = WHERE namaField = ?
    // - findBy + Field1 + And + Field2 = WHERE field1 = ? AND field2 = ?
    // - findBy + Field1 + Or + Field2  = WHERE field1 = ? OR field2 = ?
    // - countBy + NamaField = SELECT COUNT(*) WHERE namaField = ?
    // - deleteBy + NamaField = DELETE WHERE namaField = ?
    // 
    // Contoh method lain yang bisa dibuat (tinggal tambah saja, tidak perlu kode):
    // Optional<Siswa> findByNama(String nama);
    // List<Siswa> findByJurusan(String jurusan);
    // List<Siswa> findByKelasAndJurusan(String kelas, String jurusan);
    long countByKelas(String kelas);
}
