package com.smk.presensi.dto.pkl;

import java.time.LocalDate;

/**
 * DTO response untuk penempatan PKL.
 */
public record PenempatanPklResponse(
        Long id,
        Long siswaId,
        String siswaNama,
        String kelas,
        String jurusan,
        Long dudiId,
        String dudiNama,
        LocalDate tanggalMulai,
        LocalDate tanggalSelesai,
        String keterangan
) {
}

