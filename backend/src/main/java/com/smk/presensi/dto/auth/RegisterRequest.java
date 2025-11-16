package com.smk.presensi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO untuk request register user baru.
 * 
 * Client kirim username, email, password untuk create account.
 * 
 * Dipakai di endpoint: POST /api/auth/register
 */
public record RegisterRequest(
        /**
         * Username untuk login.
         * 
         * Validation:
         * - Wajib diisi (@NotBlank)
         * - Min 3 karakter, max 20 karakter (@Size)
         * - Harus unique (dicek di service layer)
         */
        @NotBlank(message = "Username tidak boleh kosong")
        @Size(min = 3, max = 20, message = "Username harus 3-20 karakter")
        String username,
        
        /**
         * Email user.
         * 
         * Validation:
         * - Wajib diisi (@NotBlank)
         * - Harus format email yang valid (@Email)
         * - Max 50 karakter (@Size)
         */
        @NotBlank(message = "Email tidak boleh kosong")
        @Email(message = "Format email tidak valid")
        @Size(max = 50, message = "Email maksimal 50 karakter")
        String email,
        
        /**
         * Password (plain text).
         * 
         * Validation:
         * - Wajib diisi (@NotBlank)
         * - Min 6 karakter (@Size)
         * 
         * Password akan di-hash dengan BCrypt sebelum disimpan.
         * JANGAN PERNAH simpan password plain text di database!
         */
        @NotBlank(message = "Password tidak boleh kosong")
        @Size(min = 6, max = 40, message = "Password harus 6-40 karakter")
        String password,
        
        /**
         * Role yang diminta (opsional).
         * 
         * Default: ROLE_SISWA (kalau tidak diisi)
         * 
         * Untuk register ADMIN atau GURU, perlu approval
         * atau hanya bisa dilakukan oleh ADMIN existing.
         */
        String role
) {
    // Record auto-generate constructor, getters, dll
}
