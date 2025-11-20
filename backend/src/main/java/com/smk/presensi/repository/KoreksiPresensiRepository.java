package com.smk.presensi.repository;

import com.smk.presensi.entity.KoreksiPresensi;
import com.smk.presensi.entity.User;
import com.smk.presensi.enums.KoreksiStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KoreksiPresensiRepository extends JpaRepository<KoreksiPresensi, Long> {

    List<KoreksiPresensi> findByTargetUser(User user);

    List<KoreksiPresensi> findByStatus(KoreksiStatus status);
}
