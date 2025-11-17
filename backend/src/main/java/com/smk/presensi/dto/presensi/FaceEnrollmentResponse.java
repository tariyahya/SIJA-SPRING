package com.smk.presensi.dto.presensi;

import java.time.LocalDateTime;

/**
 * Response setelah enrollment sukses.
 * 
 * Menginformasikan ke client bahwa face berhasil di-enroll.
 * 
 * Example Response:
 * ```json
 * {
 *   "userId": 2,
 *   "username": "budi_001",
 *   "faceEncodingLength": 128,
 *   "enrolledAt": "2024-11-17T10:30:00",
 *   "message": "Face berhasil di-enroll untuk user budi_001"
 * }
 * ```
 * 
 * faceEncodingLength berguna untuk debugging:
 * - Should always be 128 characters
 * - If different, ada bug di FaceRecognitionService
 * 
 * @param userId ID user yang enrolled
 * @param username Username user
 * @param faceEncodingLength Panjang encoding (untuk debugging)
 * @param enrolledAt Timestamp enrollment
 * @param message Success message
 */
public record FaceEnrollmentResponse(
    Long userId,
    String username,
    Integer faceEncodingLength,
    LocalDateTime enrolledAt,
    String message
) {}
