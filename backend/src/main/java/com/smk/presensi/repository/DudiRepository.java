package com.smk.presensi.repository;

import com.smk.presensi.entity.Dudi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DudiRepository extends JpaRepository<Dudi, Long> {

    List<Dudi> findByNamaContainingIgnoreCase(String nama);
}

