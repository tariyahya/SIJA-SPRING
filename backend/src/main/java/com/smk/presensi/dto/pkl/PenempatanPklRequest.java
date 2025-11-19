package com.smk.presensi.dto.pkl;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO request untuk penempatan siswa PKL ke DUDI.
 */
public record PenempatanPklRequest(
        @NotNull(message = "siswaId wajib diisi")
        @Min(value = 1, message = "siswaId harus lebih dari 0")
        Long siswaId,

        @NotNull(message = "dudiId wajib diisi")
        @Min(value = 1, message = "dudiId harus lebih dari 0")
        Long dudiId,

        @NotNull(message = "Tanggal mulai wajib diisi")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate tanggalMulai,

        @NotNull(message = "Tanggal selesai wajib diisi")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate tanggalSelesai,

        String keterangan
) {
}

