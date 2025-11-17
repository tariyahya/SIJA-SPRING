package com.smk.presensi.dto.presensi;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk checkin via face recognition.
 * 
 * User scan wajah → sistem cocokkan dengan enrolled faces → auto checkin.
 * 
 * Flow:
 * 1. User buka camera (smartphone/webcam)
 * 2. Ambil foto wajah
 * 3. Convert ke base64
 * 4. Kirim ke API (NO userId, sistem akan auto-detect)
 * 5. Backend cari match → checkin
 * 
 * Example Request:
 * ```json
 * {
 *   "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABA..."
 * }
 * ```
 * 
 * Bedanya dengan Enrollment:
 * - Enrollment: ada userId (tahu siapa yang mau didaftar)
 * - Checkin: TIDAK ada userId (sistem harus deteksi sendiri)
 * 
 * Process:
 * 1. Generate encoding dari image
 * 2. Compare dengan semua enrolled faces di database
 * 3. Calculate similarity (0.0 - 1.0)
 * 4. If similarity > threshold (0.6):
 *    - User identified!
 *    - Create presensi with method = FACE
 * 5. Else:
 *    - Return "Face tidak dikenali"
 * 
 * Note:
 * - NO authentication (public endpoint)
 * - Image harus clear (good lighting, frontal pose)
 * - Response time: 1-3 detik (tergantung jumlah enrolled users)
 * 
 * @param imageBase64 Foto wajah dalam format base64
 */
public record FaceCheckinRequest(
    @NotBlank(message = "Image wajib diisi")
    String imageBase64
) {}
