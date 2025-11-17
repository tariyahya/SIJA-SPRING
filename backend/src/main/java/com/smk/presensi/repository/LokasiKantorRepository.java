package com.smk.presensi.repository;

import com.smk.presensi.entity.LokasiKantor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity LokasiKantor.
 * 
 * Query methods menggunakan Spring Data JPA convention.
 * 
 * @author Copilot Assistant
 * @since Tahap 8 (Geolocation Validation)
 */
@Repository
public interface LokasiKantorRepository extends JpaRepository<LokasiKantor, Long> {
    
    /**
     * Cari semua lokasi yang aktif.
     * 
     * Generated SQL:
     * SELECT * FROM lokasi_kantor WHERE is_active = true
     * 
     * Use case:
     * - Get active school location untuk validasi GPS
     * - Seharusnya hanya 1 active location (business rule)
     * 
     * @return List of active locations (should be 1)
     */
    List<LokasiKantor> findByIsActive(Boolean isActive);
    
    /**
     * Cari lokasi aktif pertama (convenience method).
     * 
     * Generated SQL:
     * SELECT * FROM lokasi_kantor WHERE is_active = true LIMIT 1
     * 
     * Use case:
     * - Quick access untuk active location
     * - Digunakan di GeolocationService untuk validasi GPS
     * 
     * @return Optional<LokasiKantor> - active location atau empty jika belum setup
     */
    Optional<LokasiKantor> findFirstByIsActive(Boolean isActive);
    
    /**
     * Cari lokasi by nama (case-insensitive).
     * 
     * Generated SQL:
     * SELECT * FROM lokasi_kantor WHERE LOWER(nama) = LOWER(?)
     * 
     * Use case:
     * - Cek apakah lokasi dengan nama tertentu sudah exist
     * - Prevent duplicate nama
     * 
     * @param nama - nama lokasi (e.g., "SMK Example Jakarta")
     * @return Optional<LokasiKantor>
     */
    Optional<LokasiKantor> findByNamaIgnoreCase(String nama);
    
    /**
     * Cek apakah ada lokasi aktif.
     * 
     * Generated SQL:
     * SELECT COUNT(*) > 0 FROM lokasi_kantor WHERE is_active = true
     * 
     * Use case:
     * - Validasi sebelum enable geolocation feature
     * - Display warning "Belum ada lokasi aktif, GPS validation disabled"
     * 
     * @return true jika ada lokasi aktif
     */
    boolean existsByIsActive(Boolean isActive);
}
