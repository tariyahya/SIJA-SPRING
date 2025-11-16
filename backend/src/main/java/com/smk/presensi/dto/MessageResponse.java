package com.smk.presensi.dto;

/**
 * Generic DTO untuk response dengan message saja.
 * 
 * Dipakai untuk:
 * - Success message: "User registered successfully"
 * - Error message: "Username is already taken"
 * - Info message: "Password reset email sent"
 * 
 * Simple record untuk kemudahan.
 */
public record MessageResponse(
        /**
         * Message text.
         */
        String message
) {
    // Record auto-generate constructor dan getter
}
