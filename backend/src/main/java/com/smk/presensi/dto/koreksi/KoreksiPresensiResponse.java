package com.smk.presensi.dto.koreksi;

import com.smk.presensi.enums.KoreksiStatus;
import com.smk.presensi.enums.StatusPresensi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO response koreksi presensi.
 */
public record KoreksiPresensiResponse(
        Long id,
        Long targetUserId,
        String targetUsername,
        Long presensiId,
        LocalDate tanggal,
        LocalTime jamMasukBaru,
        LocalTime jamPulangBaru,
        StatusPresensi statusBaru,
        String alasan,
        String buktiUrl,
        KoreksiStatus status,
        String approvalNote,
        String approverUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
