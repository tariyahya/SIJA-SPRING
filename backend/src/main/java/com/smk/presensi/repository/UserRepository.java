package com.smk.presensi.repository;

import com.smk.presensi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository untuk entity User.
 * 
 * Interface ini extends JpaRepository, jadi otomatis punya method:
 * - save(User) → Insert/update user
 * - findById(Long) → Cari user by ID
 * - findAll() → Get semua users
 * - deleteById(Long) → Hapus user by ID
 * - count() → Hitung jumlah users
 * - dll
 * 
 * Plus custom methods yang kita define:
 * - findByUsername(String) → Cari user by username
 * - existsByUsername(String) → Cek apakah username sudah dipakai
 * 
 * Spring Data JPA akan auto-implement methods ini!
 * Kita cukup define method signature, Spring generate SQL-nya.
 * 
 * @Repository: Tandai sebagai repository component (Spring bean)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Cari user berdasarkan username.
     * 
     * Dipakai untuk:
     * 1. Login (load user by username)
     * 2. JWT validation (load user by username dari token)
     * 3. Check username availability (untuk register)
     * 
     * Query SQL yang di-generate Spring:
     * SELECT * FROM users WHERE username = ?
     * 
     * Method naming convention Spring Data JPA:
     * - findBy: SELECT query
     * - Username: Field name di entity User
     * 
     * Contoh penggunaan:
     * Optional<User> user = userRepository.findByUsername("admin");
     * if (user.isPresent()) {
     *     User u = user.get();
     *     // Do something...
     * }
     * 
     * Atau dengan orElseThrow:
     * User user = userRepository.findByUsername("admin")
     *         .orElseThrow(() -> new RuntimeException("User not found"));
     * 
     * PENTING: Return Optional karena bisa tidak ditemukan!
     * 
     * @param username Username untuk dicari (case-sensitive)
     * @return Optional<User> (bisa empty jika tidak ditemukan)
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Cek apakah username sudah dipakai.
     * 
     * Dipakai untuk validasi saat register:
     * - Jika true → Username sudah dipakai → Reject registration
     * - Jika false → Username tersedia → Allow registration
     * 
     * Query SQL yang di-generate Spring:
     * SELECT COUNT(*) > 0 FROM users WHERE username = ?
     * 
     * Method naming convention Spring Data JPA:
     * - existsBy: Boolean query (return true/false)
     * - Username: Field name di entity User
     * 
     * Contoh penggunaan:
     * if (userRepository.existsByUsername("admin")) {
     *     throw new RuntimeException("Username sudah digunakan!");
     * }
     * 
     * Kenapa tidak pakai findByUsername() untuk cek existence?
     * - existsByUsername() lebih efisien (hanya count, tidak load object)
     * - findByUsername() load full object (termasuk password, roles, dll)
     * - Untuk cek existence, kita tidak perlu load object
     * 
     * @param username Username untuk dicek (case-sensitive)
     * @return true jika username sudah dipakai, false jika tersedia
     */
    Boolean existsByUsername(String username);
}
