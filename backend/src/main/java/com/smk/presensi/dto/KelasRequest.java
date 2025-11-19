package com.smk.presensi.dto;

import jakarta.validation.constraints.NotBlank;

public record KelasRequest(
        @NotBlank(message = "Nama kelas tidak boleh kosong")
        String nama,

        String tingkat,

        String jurusan,

        Long waliKelasId,

        Integer kapasitas
) {
}
