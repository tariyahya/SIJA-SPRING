package com.smk.presensi.repository;

import com.smk.presensi.entity.Mapel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MapelRepository extends JpaRepository<Mapel, Long> {
    boolean existsByKodeIgnoreCase(String kode);

    List<Mapel> findByKodeContainingIgnoreCaseOrNamaContainingIgnoreCase(String kode, String nama);
}
