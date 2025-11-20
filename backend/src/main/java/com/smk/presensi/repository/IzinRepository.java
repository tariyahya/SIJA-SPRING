package com.smk.presensi.repository;

import com.smk.presensi.entity.Izin;
import com.smk.presensi.enums.IzinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IzinRepository extends JpaRepository<Izin, Long> {

    List<Izin> findByStatus(IzinStatus status);

    List<Izin> findByStatusAndTanggalMulaiLessThanEqualAndTanggalSelesaiGreaterThanEqual(
            IzinStatus status,
            LocalDate start,
            LocalDate end
    );

    List<Izin> findBySiswa_Id(Long siswaId);

    List<Izin> findBySiswa_IdAndStatus(Long siswaId, IzinStatus status);
}

