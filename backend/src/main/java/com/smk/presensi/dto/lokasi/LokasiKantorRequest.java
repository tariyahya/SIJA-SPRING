package com.smk.presensi.dto.lokasi;

import jakarta.validation.constraints.*;

/**
 * Request DTO untuk create/update LokasiKantor.
 * 
 * Validations:
 * - nama: required, not blank
 * - latitude: required, range -90 to +90
 * - longitude: required, range -180 to +180
 * - radiusValidasi: required, minimum 10 meters
 * - alamat: optional
 * - keterangan: optional
 * 
 * Example:
 * {
 *   "nama": "SMK Negeri 1 Jakarta",
 *   "latitude": -6.200000,
 *   "longitude": 106.816666,
 *   "radiusValidasi": 200,
 *   "alamat": "Jl. Budi Utomo No.7, Jakarta Pusat",
 *   "keterangan": "Kampus utama"
 * }
 * 
 * @author Copilot Assistant
 * @since Tahap 8 (Geolocation Validation)
 */
public record LokasiKantorRequest(
        
        @NotBlank(message = "Nama lokasi wajib diisi")
        String nama,
        
        @NotNull(message = "Latitude wajib diisi")
        @DecimalMin(value = "-90.0", message = "Latitude minimal -90 degrees")
        @DecimalMax(value = "90.0", message = "Latitude maksimal +90 degrees")
        Double latitude,
        
        @NotNull(message = "Longitude wajib diisi")
        @DecimalMin(value = "-180.0", message = "Longitude minimal -180 degrees")
        @DecimalMax(value = "180.0", message = "Longitude maksimal +180 degrees")
        Double longitude,
        
        @NotNull(message = "Radius validasi wajib diisi")
        @Min(value = 10, message = "Radius validasi minimal 10 meter")
        Integer radiusValidasi,
        
        String alamat,
        
        String keterangan
) {
}
