package com.smk.presensi.dto.jadwal;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record JadwalMengajarResponse(
        Long id,
        Long guruId,
        String guruNama,
        Long kelasId,
        String kelasNama,
        String mapel,
        DayOfWeek hari,
        LocalTime jamMulai,
        LocalTime jamSelesai,
        String ruangan,
        String catatan,
        boolean aktif
) {
}
