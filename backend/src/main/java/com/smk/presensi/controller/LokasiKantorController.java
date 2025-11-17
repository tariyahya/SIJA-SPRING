package com.smk.presensi.controller;

import com.smk.presensi.dto.lokasi.LokasiKantorRequest;
import com.smk.presensi.dto.lokasi.LokasiKantorResponse;
import com.smk.presensi.service.LokasiKantorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller untuk manage lokasi kantor/sekolah (GPS validation).
 * 
 * Base URL: /api/lokasi-kantor
 * Security: All endpoints require ADMIN role
 * 
 * Endpoints:
 * - GET /api/lokasi-kantor → List all locations
 * - GET /api/lokasi-kantor/{id} → Get by ID
 * - GET /api/lokasi-kantor/active → Get active location
 * - POST /api/lokasi-kantor → Create new location
 * - PUT /api/lokasi-kantor/{id} → Update existing
 * - DELETE /api/lokasi-kantor/{id} → Delete location
 * - POST /api/lokasi-kantor/{id}/activate → Set as active
 * 
 * @author Copilot Assistant
 * @since Tahap 8 (Geolocation Validation)
 */
@RestController
@RequestMapping("/api/lokasi-kantor")
public class LokasiKantorController {
    
    @Autowired
    private LokasiKantorService lokasiKantorService;
    
    /**
     * GET /api/lokasi-kantor
     * List all locations (active and inactive).
     * 
     * Access: ADMIN only
     * 
     * Response:
     * {
     *   "message": "Daftar lokasi berhasil diambil",
     *   "data": [
     *     { "id": 1, "nama": "SMK Campus A", "isActive": true, ... },
     *     { "id": 2, "nama": "SMK Campus B", "isActive": false, ... }
     *   ]
     * }
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAll() {
        List<LokasiKantorResponse> locations = lokasiKantorService.getAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Daftar lokasi berhasil diambil");
        response.put("data", locations);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/lokasi-kantor/{id}
     * Get location by ID.
     * 
     * Access: ADMIN only
     * 
     * Response:
     * {
     *   "message": "Detail lokasi berhasil diambil",
     *   "data": { "id": 1, "nama": "SMK Campus A", ... }
     * }
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        LokasiKantorResponse location = lokasiKantorService.getById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Detail lokasi berhasil diambil");
        response.put("data", location);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/lokasi-kantor/active
     * Get active location (currently used for GPS validation).
     * 
     * Access: ADMIN only
     * 
     * Response (if exists):
     * {
     *   "message": "Lokasi aktif berhasil diambil",
     *   "data": { "id": 1, "nama": "SMK Campus A", "isActive": true, ... }
     * }
     * 
     * Response (if none):
     * {
     *   "message": "Tidak ada lokasi aktif. Validasi GPS belum dikonfigurasi.",
     *   "data": null
     * }
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getActive() {
        LokasiKantorResponse location = lokasiKantorService.getActiveLocation();
        
        Map<String, Object> response = new HashMap<>();
        if (location != null) {
            response.put("message", "Lokasi aktif berhasil diambil");
            response.put("data", location);
        } else {
            response.put("message", "Tidak ada lokasi aktif. Validasi GPS belum dikonfigurasi.");
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/lokasi-kantor
     * Create new location.
     * 
     * Access: ADMIN only
     * 
     * Request Body:
     * {
     *   "nama": "SMK Negeri 1 Jakarta",
     *   "latitude": -6.200000,
     *   "longitude": 106.816666,
     *   "radiusValidasi": 200,
     *   "alamat": "Jl. Budi Utomo No.7",
     *   "keterangan": "Kampus utama"
     * }
     * 
     * Response:
     * {
     *   "message": "Lokasi berhasil ditambahkan",
     *   "data": { "id": 1, ... }
     * }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody LokasiKantorRequest request) {
        LokasiKantorResponse created = lokasiKantorService.create(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lokasi berhasil ditambahkan");
        response.put("data", created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/lokasi-kantor/{id}
     * Update existing location.
     * 
     * Access: ADMIN only
     * 
     * Request Body: Same as POST
     * 
     * Response:
     * {
     *   "message": "Lokasi berhasil diupdate",
     *   "data": { "id": 1, ... }
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody LokasiKantorRequest request
    ) {
        LokasiKantorResponse updated = lokasiKantorService.update(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lokasi berhasil diupdate");
        response.put("data", updated);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/lokasi-kantor/{id}
     * Delete location.
     * 
     * Access: ADMIN only
     * 
     * Business rule:
     * - Cannot delete active location (must deactivate first)
     * 
     * Response:
     * {
     *   "message": "Lokasi berhasil dihapus"
     * }
     * 
     * Error response (if active):
     * {
     *   "message": "Tidak dapat menghapus lokasi yang sedang aktif. Silakan aktifkan lokasi lain terlebih dahulu."
     * }
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        lokasiKantorService.delete(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lokasi berhasil dihapus");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/lokasi-kantor/{id}/activate
     * Set location as active (automatically deactivate others).
     * 
     * Access: ADMIN only
     * 
     * Business rule:
     * - Only 1 location can be active at a time
     * - GPS validation will use this active location
     * 
     * Response:
     * {
     *   "message": "Lokasi 'SMK Campus A' berhasil diaktifkan",
     *   "data": { "id": 1, "isActive": true, ... }
     * }
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable Long id) {
        LokasiKantorResponse activated = lokasiKantorService.activate(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lokasi '" + activated.nama() + "' berhasil diaktifkan");
        response.put("data", activated);
        
        return ResponseEntity.ok(response);
    }
}
