package com.smk.presensi.repository;

import com.smk.presensi.entity.GuruJurnal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GuruJurnalRepository extends JpaRepository<GuruJurnal, Long> {
    List<GuruJurnal> findByGuru_IdOrderByTanggalDesc(Long guruId);
    List<GuruJurnal> findByGuru_IdAndTanggalBetweenOrderByTanggalDesc(Long guruId, LocalDate start, LocalDate end);
}
