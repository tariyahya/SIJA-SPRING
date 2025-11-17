package com.smk.presensi.controller;

import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.dto.presensi.RfidCheckinRequest;
import com.smk.presensi.service.PresensiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER RFID - REST API endpoints untuk RFID presensi.
 * 
 * Base URL: /api/presensi/rfid
 * 
 * PENTING:
 * - Endpoints ini PUBLIC (tidak perlu JWT authentication)
 * - Karena RFID reader tidak bisa login (hardware, bukan user)
 * - Autentikasi dilakukan via rfidCardId (unik per kartu)
 * 
 * Security:
 * - Endpoint /api/presensi/rfid/** di-whitelist di SecurityConfig
 * - Siapa pun bisa akses (permitAll)
 * - Validasi dilakukan di level bisnis: rfidCardId harus terdaftar
 * 
 * Simulasi RFID:
 * - Tahap 5 ini: Input rfidCardId manual via Postman/curl (simulasi)
 * - Tahap nanti: Hardware RFID reader real yang kirim request otomatis
 */
@RestController
@RequestMapping("/api/presensi/rfid")
public class RfidController {

    private final PresensiService presensiService;

    public RfidController(PresensiService presensiService) {
        this.presensiService = presensiService;
    }

    /**
     * ENDPOINT: POST /api/presensi/rfid/checkin
     * 
     * Checkin via tap kartu RFID.
     * 
     * Access: PUBLIC (no JWT required)
     * 
     * Request body:
     * {
     *   "rfidCardId": "RF001234"
     * }
     * 
     * Response: PresensiResponse
     * 
     * Success response (200 OK):
     * {
     *   "id": 1,
     *   "userId": 5,
     *   "username": "budi",
     *   "tipe": "SISWA",
     *   "tanggal": "2024-01-15",
     *   "jamMasuk": "07:05:30",
     *   "jamPulang": null,
     *   "status": "HADIR",
     *   "method": "RFID",
     *   "latitude": null,
     *   "longitude": null,
     *   "keterangan": "Checkin via RFID: RF001234"
     * }
     * 
     * Error response (400 Bad Request):
     * {
     *   "error": "Kartu RFID tidak terdaftar: RF001234"
     * }
     * 
     * {
     *   "error": "User dengan kartu RF001234 sudah checkin hari ini"
     * }
     * 
     * Use case:
     * 1. Siswa/guru tap kartu RFID di mesin presensi
     * 2. RFID reader baca rfidCardId dari kartu
     * 3. Reader kirim POST request ke endpoint ini
     * 4. Backend cari user dengan rfidCardId tersebut
     * 5. Jika ketemu → auto-checkin
     * 6. Jika tidak ketemu → error
     * 
     * Testing (Postman):
     * - Method: POST
     * - URL: http://localhost:8081/api/presensi/rfid/checkin
     * - Headers: Content-Type: application/json
     * - Body (raw JSON): { "rfidCardId": "RF001234" }
     * - NO NEED Authorization header (public endpoint)
     */
    @PostMapping("/checkin")
    public ResponseEntity<PresensiResponse> checkinRfid(@Valid @RequestBody RfidCheckinRequest request) {
        PresensiResponse response = presensiService.checkinRfid(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * ENDPOINT: GET /api/presensi/rfid/test
     * 
     * Test endpoint untuk cek apakah RFID endpoint accessible (public).
     * 
     * Access: PUBLIC (no JWT required)
     * 
     * Response: Plain text "RFID endpoint is working!"
     * 
     * Use case:
     * - Test koneksi dari RFID reader
     * - Cek apakah endpoint RFID sudah di-whitelist
     * 
     * Testing (browser/Postman):
     * - Method: GET
     * - URL: http://localhost:8081/api/presensi/rfid/test
     * - Expected: "RFID endpoint is working!" (tanpa error 401/403)
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("RFID endpoint is working!");
    }
}
