package com.smk.presensi.dto;

public record SiswaResponse(
    Long id,
    String nis,
    String nama,
    String kelas,
    String jurusan
) {}
