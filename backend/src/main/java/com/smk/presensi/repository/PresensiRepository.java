package com.smk.presensi.repository;

import com.smk.presensi.entity.Presensi;
import com.smk.presensi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresensiRepository extends JpaRepository<Presensi, Long> {

    List<Presensi> findByUser(User user);

    List<Presensi> findByUserAndTanggalBetweenOrderByTanggalDesc(
        User user,
        LocalDate startDate,
        LocalDate endDate
    );

    Optional<Presensi> findByUserAndTanggal(User user, LocalDate tanggal);

    boolean existsByUserAndTanggal(User user, LocalDate tanggal);

    List<Presensi> findByTanggal(LocalDate tanggal);
}
