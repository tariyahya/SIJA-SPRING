package com.smk.presensi.dto.izin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO untuk pengajuan izin baru.
 */
public record IzinRequest(
        @NotNull(message = "siswaId wajib diisi")
        @Min(value = 1, message = "siswaId harus lebih dari 0")
        Long siswaId,

        @NotBlank(message = "Jenis izin wajib diisi (SAKIT/IZIN/DISPENSASI)")
        String jenis,

        @NotNull(message = "Tanggal mulai wajib diisi")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate tanggalMulai,

        @NotNull(message = "Tanggal selesai wajib diisi")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate tanggalSelesai,

        @NotBlank(message = "Alasan izin wajib diisi")
        String alasan
) {
}

