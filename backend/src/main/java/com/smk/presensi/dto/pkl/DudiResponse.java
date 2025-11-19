package com.smk.presensi.dto.pkl;

/**
 * DTO response untuk master DUDI.
 */
public record DudiResponse(
        Long id,
        String nama,
        String bidangUsaha,
        String alamat,
        String contactPerson,
        String contactPhone,
        Integer kuotaSiswa,
        Double latitude,
        Double longitude,
        Integer radiusValidasi,
        Boolean aktif
) {
}

