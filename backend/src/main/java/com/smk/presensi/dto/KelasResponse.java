package com.smk.presensi.dto;

public record KelasResponse(
        Long id,
        String nama,
        String tingkat,
        String jurusan,
        Long waliKelasId,
        Integer kapasitas
) {
}
