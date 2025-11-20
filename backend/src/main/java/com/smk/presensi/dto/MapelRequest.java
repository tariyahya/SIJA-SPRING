package com.smk.presensi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO untuk CRUD Mata Pelajaran.
 */
public record MapelRequest(
        @NotBlank(message = "Kode mapel wajib diisi")
        @Size(max = 50, message = "Kode maksimal 50 karakter")
        String kode,

        @NotBlank(message = "Nama mapel wajib diisi")
        @Size(max = 150, message = "Nama maksimal 150 karakter")
        String nama,

        @Size(max = 255, message = "Deskripsi maksimal 255 karakter")
        String deskripsi
) {}
