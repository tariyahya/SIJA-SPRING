package com.smk.presensi.dto.koreksi;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO approval/penolakan koreksi presensi.
 */
public record KoreksiPresensiApprovalRequest(
        @NotBlank(message = "Status wajib diisi (APPROVED/REJECTED)")
        String status,
        String approvalNote
) {
}
