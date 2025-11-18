package com.smk.presensi.dto.presensi;

import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.enums.TipeUser;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO untuk Admin CRUD Presensi.
 * Digunakan oleh panel admin (desktop/web) untuk membuat
 * atau mengupdate record presensi secara manual.
 */
public record AdminPresensiRequest(
        @NotNull(message = "userId harus diisi")
        Long userId,

        @NotNull(message = "tipe user harus diisi")
        TipeUser tipe,

        @NotNull(message = "tanggal harus diisi")
        LocalDate tanggal,

        LocalTime jamMasuk,
        LocalTime jamPulang,

        @NotNull(message = "status harus diisi")
        StatusPresensi status,

        @NotNull(message = "method harus diisi")
        MethodPresensi method,

        Double latitude,
        Double longitude,
        String keterangan
) {
}

