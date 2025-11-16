package com.smk.presensi.controller;

import com.smk.presensi.dto.GuruRequest;
import com.smk.presensi.dto.GuruResponse;
import com.smk.presensi.service.GuruService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER untuk Guru - Handle HTTP request untuk resource Guru.
 * 
 * Endpoints dengan Role-Based Access Control:
 * - GET /api/guru → Ambil semua guru (ADMIN, GURU)
 * - GET /api/guru/{id} → Ambil 1 guru by ID (ADMIN, GURU)
 * - POST /api/guru → Tambah guru baru (ADMIN only)
 * - PUT /api/guru/{id} → Update guru (ADMIN only)
 * - DELETE /api/guru/{id} → Hapus guru (ADMIN only)
 * 
 * Role access:
 * - ROLE_ADMIN: Full access (CRUD)
 * - ROLE_GURU: Read-only (GET all, GET by ID)
 * - ROLE_SISWA: No access to guru endpoints
 */
@RestController
@RequestMapping("/api/guru")
public class GuruController {

    private final GuruService guruService;

    public GuruController(GuruService guruService) {
        this.guruService = guruService;
    }

    /**
     * GET ALL - Ambil semua guru.
     * 
     * Access: ADMIN, GURU
     * URL: GET /api/guru
     * 
     * @PreAuthorize("hasAnyRole('ADMIN', 'GURU')"):
     * - Hanya user dengan role ADMIN atau GURU yang boleh akses
     * - Jika user tidak login → 401 Unauthorized
     * - Jika user login tapi bukan ADMIN/GURU → 403 Forbidden
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<GuruResponse> getAll() {
        return guruService.findAll();
    }

    /**
     * GET BY ID - Ambil 1 guru berdasarkan ID.
     * 
     * Access: ADMIN, GURU
     * URL: GET /api/guru/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public GuruResponse getById(@PathVariable Long id) {
        return guruService.findById(id);
    }

    /**
     * CREATE - Tambah guru baru.
     * 
     * Access: ADMIN only
     * URL: POST /api/guru
     * 
     * @PreAuthorize("hasRole('ADMIN')"):
     * - Hanya ADMIN yang boleh tambah guru
     * - GURU tidak bisa tambah guru lain
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuruResponse> create(@RequestBody @Valid GuruRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guruService.create(request));
    }

    /**
     * UPDATE - Update data guru existing.
     * 
     * Access: ADMIN only
     * URL: PUT /api/guru/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GuruResponse update(@PathVariable Long id, @RequestBody @Valid GuruRequest request) {
        return guruService.update(id, request);
    }

    /**
     * DELETE - Hapus guru.
     * 
     * Access: ADMIN only
     * URL: DELETE /api/guru/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        guruService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
