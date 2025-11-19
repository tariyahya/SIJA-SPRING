package com.smk.presensi.dto.izin;

import com.smk.presensi.enums.IzinJenis;
import com.smk.presensi.enums.IzinStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO response untuk data izin.
 */
public record IzinResponse(
        Long id,
        Long siswaId,
        String siswaNama,
        String kelas,
        String jurusan,
        IzinJenis jenis,
        LocalDate tanggalMulai,
        LocalDate tanggalSelesai,
        String alasan,
        IzinStatus status,
        String approvalNote,
        String approverUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

