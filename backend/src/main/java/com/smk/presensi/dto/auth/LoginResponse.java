package com.smk.presensi.dto.auth;

import java.util.Set;

/**
 * DTO untuk response login.
 * 
 * Server return JWT token + user info setelah login berhasil.
 * 
 * Client simpan token ini (localStorage atau cookie)
 * dan kirim di header untuk request berikutnya:
 * Authorization: Bearer <token>
 */
public record LoginResponse(
        /**
         * JWT token untuk authentication.
         * 
         * Format: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMD...
         * 
         * Client harus simpan token ini dan kirim di setiap request.
         */
        String token,
        
        /**
         * Type token (selalu "Bearer" untuk JWT).
         * 
         * Format header: Authorization: Bearer <token>
         * "Bearer" adalah prefix standard untuk JWT.
         */
        String type,
        
        /**
         * Username yang login.
         * Untuk display di UI (contoh: "Welcome, admin!")
         */
        String username,
        
        /**
         * Roles user (contoh: ["ROLE_ADMIN", "ROLE_GURU"])
         * Untuk frontend decision:
         * - Show/hide menu berdasarkan role
         * - Enable/disable button berdasarkan role
         */
        Set<String> roles
) {
    /**
     * Constructor dengan default type "Bearer".
     * 
     * Contoh penggunaan:
     * new LoginResponse(token, username, roles)
     * // type otomatis "Bearer"
     */
    public LoginResponse(String token, String username, Set<String> roles) {
        this(token, "Bearer", username, roles);
    }
}
