package com.smk.presensi.dto;

public record JurusanResponse(
        Long id,
        String kode,
        String nama,
        Integer durasiTahun,
        Long ketuaJurusanId
) {
}

