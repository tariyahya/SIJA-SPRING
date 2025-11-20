package com.smk.presensi.dto;

/**
 * Response DTO untuk Mapel.
 */
public record MapelResponse(
        Long id,
        String kode,
        String nama,
        String deskripsi
) {}
