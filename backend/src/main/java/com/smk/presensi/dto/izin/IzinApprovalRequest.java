package com.smk.presensi.dto.izin;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk request approval / reject izin.
 *
 * status: APPROVED atau REJECTED.
 * catatan: opsional, tapi sebaiknya diisi untuk alasan approval/penolakan.
 */
public record IzinApprovalRequest(
        @NotBlank(message = "Status wajib diisi (APPROVED/REJECTED)")
        String status,
        String catatan
) {
}

