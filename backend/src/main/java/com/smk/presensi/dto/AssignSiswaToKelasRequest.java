package com.smk.presensi.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO untuk request assignment siswa ke sebuah kelas.
 * Digunakan oleh endpoint: POST /api/kelas/{id}/assign-siswa
 */
public record AssignSiswaToKelasRequest(
        @NotEmpty(message = "Daftar ID siswa tidak boleh kosong")
        List<Long> siswaIds
) {
}

