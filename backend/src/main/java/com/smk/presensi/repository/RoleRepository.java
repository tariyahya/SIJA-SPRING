package com.smk.presensi.repository;

import com.smk.presensi.entity.Role;
import com.smk.presensi.entity.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository untuk entity Role.
 * 
 * Interface ini extends JpaRepository, jadi otomatis punya method:
 * - save(Role) → Insert/update role
 * - findById(Long) → Cari role by ID
 * - findAll() → Get semua roles
 * - deleteById(Long) → Hapus role by ID
 * - count() → Hitung jumlah roles
 * - dll
 * 
 * Plus custom method yang kita define sendiri:
 * - findByName(RoleName) → Cari role by name (ROLE_ADMIN, ROLE_GURU, dll)
 * 
 * Spring Data JPA akan auto-implement method ini!
 * Kita cukup define method signature, Spring generate SQL-nya.
 * 
 * @Repository: Tandai sebagai repository component (Spring bean)
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Cari role berdasarkan name.
     * 
     * Query SQL yang di-generate Spring:
     * SELECT * FROM roles WHERE name = ?
     * 
     * Method naming convention Spring Data JPA:
     * - findBy: SELECT query
     * - Name: Field name di entity Role
     * 
     * Contoh penggunaan:
     * Optional<Role> adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN);
     * if (adminRole.isPresent()) {
     *     Role role = adminRole.get();
     *     // Do something...
     * }
     * 
     * Atau dengan orElseThrow:
     * Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
     *         .orElseThrow(() -> new RuntimeException("Role not found"));
     * 
     * @param name Role name (enum: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA)
     * @return Optional<Role> (bisa empty jika tidak ditemukan)
     */
    Optional<Role> findByName(RoleName name);
}
