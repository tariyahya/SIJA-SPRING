package com.smk.presensi.dto.presensi;

import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.enums.TipeUser;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO RESPONSE untuk Presensi - Data yang dikirim server ke client.
 */
public record PresensiResponse(
        Long id,
        Long userId,
        String username,
        TipeUser tipe,
        LocalDate tanggal,
        LocalTime jamMasuk,
        LocalTime jamPulang,
        StatusPresensi status,
        MethodPresensi method,
        Double latitude,
        Double longitude,
        String keterangan,
        Long kelasId,
        String kelasNama,
        String mapel,
        String materi
) {
}
