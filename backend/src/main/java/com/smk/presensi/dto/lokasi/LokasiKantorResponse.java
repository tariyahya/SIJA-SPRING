package com.smk.presensi.dto.lokasi;

import java.time.LocalDateTime;

/**
 * Response DTO untuk LokasiKantor.
 * 
 * Contains:
 * - All entity fields
 * - Formatted coordinates for display
 * 
 * Example:
 * {
 *   "id": 1,
 *   "nama": "SMK Negeri 1 Jakarta",
 *   "latitude": -6.200000,
 *   "longitude": 106.816666,
 *   "radiusValidasi": 200,
 *   "isActive": true,
 *   "alamat": "Jl. Budi Utomo No.7, Jakarta Pusat",
 *   "keterangan": "Kampus utama",
 *   "createdAt": "2024-01-15T08:00:00",
 *   "updatedAt": "2024-01-15T08:00:00",
 *   "formattedCoordinates": "6°12'S, 106°49'E"
 * }
 * 
 * @author Copilot Assistant
 * @since Tahap 8 (Geolocation Validation)
 */
public record LokasiKantorResponse(
        Long id,
        String nama,
        Double latitude,
        Double longitude,
        Integer radiusValidasi,
        Boolean isActive,
        String alamat,
        String keterangan,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String formattedCoordinates
) {
}
