package com.smk.presensi.repository;

import com.smk.presensi.entity.Kelas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KelasRepository extends JpaRepository<Kelas, Long> {
    
    List<Kelas> findByJurusan(String jurusan);
}
