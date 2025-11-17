package com.smk.presensi.repository;

import com.smk.presensi.entity.Presensi;
import com.smk.presensi.entity.User;
import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresensiRepository extends JpaRepository<Presensi, Long> {

    List<Presensi> findByUser(User user);

    List<Presensi> findByUserAndTanggalBetweenOrderByTanggalDesc(
        User user,
        LocalDate startDate,
        LocalDate endDate
    );

    Optional<Presensi> findByUserAndTanggal(User user, LocalDate tanggal);

    boolean existsByUserAndTanggal(User user, LocalDate tanggal);

    List<Presensi> findByTanggal(LocalDate tanggal);
    
    // ===== TAHAP 9: REPORTING & ANALYTICS =====
    
    /**
     * Get all presensi records within date range.
     * Used for daily/monthly reports.
     */
    List<Presensi> findByTanggalBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Count presensi by status within date range.
     * Example: Count HADIR from 2025-01-01 to 2025-01-31
     */
    long countByStatusAndTanggalBetween(StatusPresensi status, LocalDate startDate, LocalDate endDate);
    
    /**
     * Count presensi by method within date range.
     * Example: Count MANUAL checkins in January
     */
    long countByMethodAndTanggalBetween(MethodPresensi method, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get all presensi by status within date range.
     * Example: Get all ALFA records in January
     */
    List<Presensi> findByStatusAndTanggalBetween(StatusPresensi status, LocalDate startDate, LocalDate endDate);
    
    /**
     * Custom query: Count total records within date range.
     */
    @Query("SELECT COUNT(p) FROM Presensi p WHERE p.tanggal BETWEEN :startDate AND :endDate")
    long countByTanggalBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
