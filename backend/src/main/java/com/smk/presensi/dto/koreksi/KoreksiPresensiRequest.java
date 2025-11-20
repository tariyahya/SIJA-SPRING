package com.smk.presensi.dto.koreksi;

import com.smk.presensi.enums.StatusPresensi;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO permintaan koreksi presensi.
 */
public record KoreksiPresensiRequest(
        Long presensiId,
        Long targetUserId,

        @NotNull(message = "Tanggal wajib diisi")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate tanggal,

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime jamMasukBaru,

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime jamPulangBaru,

        @NotNull(message = "Status baru wajib diisi")
        StatusPresensi statusBaru,

        @NotBlank(message = "Alasan koreksi wajib diisi")
        String alasan,

        String buktiUrl
) {
}
