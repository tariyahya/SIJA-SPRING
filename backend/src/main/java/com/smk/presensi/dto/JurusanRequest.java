package com.smk.presensi.dto;

import jakarta.validation.constraints.NotBlank;

public record JurusanRequest(
        @NotBlank(message = "Kode jurusan tidak boleh kosong")
        String kode,

        @NotBlank(message = "Nama jurusan tidak boleh kosong")
        String nama,

        Integer durasiTahun,

        Long ketuaJurusanId
) {
}

