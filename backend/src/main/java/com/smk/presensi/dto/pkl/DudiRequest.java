package com.smk.presensi.dto.pkl;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO request untuk master DUDI.
 */
public record DudiRequest(
        @NotBlank(message = "Nama DUDI wajib diisi")
        String nama,
        String bidangUsaha,
        String alamat,
        String contactPerson,
        String contactPhone,
        Integer kuotaSiswa,
        Boolean aktif
) {
}

