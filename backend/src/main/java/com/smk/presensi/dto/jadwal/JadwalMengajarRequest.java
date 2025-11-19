package com.smk.presensi.dto.jadwal;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record JadwalMengajarRequest(
        @NotNull(message = "guruId wajib diisi")
        Long guruId,
        @NotNull(message = "kelasId wajib diisi")
        Long kelasId,
        @NotNull(message = "mapel wajib diisi")
        String mapel,
        @NotNull(message = "hari wajib diisi")
        DayOfWeek hari,
        @NotNull(message = "jamMulai wajib diisi")
        LocalTime jamMulai,
        @NotNull(message = "jamSelesai wajib diisi")
        LocalTime jamSelesai,
        String ruangan,
        String catatan,
        boolean aktif
) {
}
