package com.smk.presensi.repository;

import com.smk.presensi.entity.PenempatanPkl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenempatanPklRepository extends JpaRepository<PenempatanPkl, Long> {

    List<PenempatanPkl> findByDudi_Id(Long dudiId);

    List<PenempatanPkl> findBySiswa_Id(Long siswaId);
}

