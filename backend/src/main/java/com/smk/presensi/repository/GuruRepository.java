package com.smk.presensi.repository;

import com.smk.presensi.entity.Guru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuruRepository extends JpaRepository<Guru, Long> {
    
    Optional<Guru> findByNip(String nip);
}
