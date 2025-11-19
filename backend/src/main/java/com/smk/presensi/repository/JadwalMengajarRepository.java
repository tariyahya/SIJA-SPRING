package com.smk.presensi.repository;

import com.smk.presensi.entity.JadwalMengajar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface JadwalMengajarRepository extends JpaRepository<JadwalMengajar, Long> {
    List<JadwalMengajar> findByGuru_IdAndHariAndAktifTrue(Long guruId, DayOfWeek hari);
    List<JadwalMengajar> findByGuru_IdAndAktifTrue(Long guruId);
    List<JadwalMengajar> findByKelas_IdAndAktifTrue(Long kelasId);
}
