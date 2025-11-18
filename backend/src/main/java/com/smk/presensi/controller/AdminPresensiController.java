package com.smk.presensi.controller;

import com.smk.presensi.dto.MessageResponse;
import com.smk.presensi.dto.presensi.AdminPresensiRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.service.AdminPresensiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ADMIN PRESENSI CONTROLLER
 *
 * Endpoint khusus ADMIN untuk mengelola data presensi secara manual.
 * Base URL: /api/admin/presensi
 *
 * Fitur:
 * - List presensi dengan filter sederhana (tanggal optional)
 * - Create presensi manual
 * - Update presensi
 * - Delete presensi
 */
@RestController
@RequestMapping("/api/admin/presensi")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPresensiController {

    private final AdminPresensiService adminPresensiService;

    public AdminPresensiController(AdminPresensiService adminPresensiService) {
        this.adminPresensiService = adminPresensiService;
    }

    /**
     * GET /api/admin/presensi?tanggal=2025-01-17
     * List presensi (optional filter tanggal).
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPresensi(
            @RequestParam(required = false) LocalDate tanggal
    ) {
        List<PresensiResponse> list = adminPresensiService.getPresensi(tanggal);

        Map<String, Object> data = new HashMap<>();
        data.put("tanggal", tanggal);
        data.put("totalPresensi", list.size());
        data.put("daftarPresensi", list);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Daftar presensi berhasil diambil");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/presensi
     * Create presensi manual.
     */
    @PostMapping
    public ResponseEntity<PresensiResponse> createPresensi(
            @Valid @RequestBody AdminPresensiRequest request
    ) {
        PresensiResponse created = adminPresensiService.createPresensi(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/admin/presensi/{id}
     * Update presensi.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PresensiResponse> updatePresensi(
            @PathVariable Long id,
            @Valid @RequestBody AdminPresensiRequest request
    ) {
        PresensiResponse updated = adminPresensiService.updatePresensi(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/admin/presensi/{id}
     * Delete presensi.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deletePresensi(@PathVariable Long id) {
        adminPresensiService.deletePresensi(id);
        return ResponseEntity.ok(new MessageResponse("Presensi dengan ID " + id + " berhasil dihapus"));
    }
}

