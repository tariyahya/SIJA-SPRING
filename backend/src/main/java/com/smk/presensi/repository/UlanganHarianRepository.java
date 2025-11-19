package com.smk.presensi.repository;

import com.smk.presensi.entity.UlanganHarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UlanganHarianRepository extends JpaRepository<UlanganHarian, Long> {
    List<UlanganHarian> findByGuru_IdAndTanggalBetween(Long guruId, LocalDate start, LocalDate end);
}
