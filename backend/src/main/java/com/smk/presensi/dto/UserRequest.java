package com.smk.presensi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk request User (Create/Update)
 */
public record UserRequest(
    @NotBlank(message = "Username tidak boleh kosong") String username,
    @NotBlank(message = "Password tidak boleh kosong") String password,
    String email,
    String role,  // ADMIN, GURU, SISWA
    Long siswaId, // optional - jika user adalah siswa
    Long guruId   // optional - jika user adalah guru
) {}
