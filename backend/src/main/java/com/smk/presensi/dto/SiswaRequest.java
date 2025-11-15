package com.smk.presensi.dto;

import jakarta.validation.constraints.NotBlank;

public record SiswaRequest(
    @NotBlank(message = "NIS tidak boleh kosong") String nis,
    @NotBlank(message = "Nama tidak boleh kosong") String nama,
    String kelas,
    String jurusan
) {}
