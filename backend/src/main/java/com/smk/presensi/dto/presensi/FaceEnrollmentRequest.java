package com.smk.presensi.dto.presensi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO untuk enrollment (daftar wajah).
 * 
 * Admin/User upload foto wajah untuk di-register.
 * 
 * Flow:
 * 1. Upload foto via form (file picker)
 * 2. Convert ke base64: "data:image/jpeg;base64,/9j/4AAQ..."
 * 3. Kirim ke API dengan userId
 * 4. Backend generate face encoding dan simpan
 * 
 * Example Request:
 * ```json
 * {
 *   "userId": 2,
 *   "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABA..."
 * }
 * ```
 * 
 * Note:
 * - userId WAJIB (tahu siapa yang mau dienroll)
 * - imageBase64 bisa include data URL prefix atau tidak
 * - Image size: min 1 KB, max 10 MB
 * 
 * @param userId ID user yang mau enroll face (siswa atau guru)
 * @param imageBase64 Foto wajah dalam format base64 (include data URL prefix)
 */
public record FaceEnrollmentRequest(
    @NotNull(message = "User ID wajib diisi")
    Long userId,
    
    @NotBlank(message = "Image wajib diisi")
    String imageBase64
) {}
