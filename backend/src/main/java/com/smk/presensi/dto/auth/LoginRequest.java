package com.smk.presensi.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk request login.
 * 
 * Client kirim username & password untuk authentication.
 * 
 * Dipakai di endpoint: POST /api/auth/login
 */
public record LoginRequest(
        /**
         * Username untuk login.
         * Wajib diisi (@NotBlank).
         */
        @NotBlank(message = "Username tidak boleh kosong")
        String username,
        
        /**
         * Password untuk login (plain text).
         * Wajib diisi (@NotBlank).
         * 
         * Password akan di-compare dengan hash di database.
         * TIDAK disimpan, hanya dipakai untuk verify.
         */
        @NotBlank(message = "Password tidak boleh kosong")
        String password
) {
    // Record auto-generate:
    // - Constructor
    // - Getter: username(), password()
    // - toString(), equals(), hashCode()
}
