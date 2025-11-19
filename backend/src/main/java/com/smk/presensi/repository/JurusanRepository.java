package com.smk.presensi.repository;

import com.smk.presensi.entity.Jurusan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JurusanRepository extends JpaRepository<Jurusan, Long> {

    Optional<Jurusan> findByKode(String kode);

    boolean existsByKode(String kode);
}

