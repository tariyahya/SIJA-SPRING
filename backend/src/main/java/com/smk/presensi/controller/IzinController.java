package com.smk.presensi.controller;

import com.smk.presensi.dto.izin.IzinApprovalRequest;
import com.smk.presensi.dto.izin.IzinRequest;
import com.smk.presensi.dto.izin.IzinResponse;
import com.smk.presensi.service.IzinService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller untuk modul perizinan (MVP).
 *
 * Endpoint utama:
 * - POST /api/izin        : pengajuan izin baru (SISWA / GURU)
 * - GET  /api/izin/pending: daftar izin pending hari ini (GURU_PIKET / ADMIN)
 * - POST /api/izin/{id}/approve : approve / reject izin
 */
@RestController
@RequestMapping("/api/izin")
public class IzinController {

    private final IzinService izinService;

    public IzinController(IzinService izinService) {
        this.izinService = izinService;
    }

    /**
     * Pengajuan izin baru.
     *
     * Access:
     * - SISWA: mengajukan izin sendiri (melalui operator / petugas)
     * - GURU : mengajukan izin untuk siswa
     * - ADMIN: boleh juga menginput manual
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SISWA', 'GURU', 'ADMIN')")
    public ResponseEntity<IzinResponse> create(@RequestBody @Valid IzinRequest request) {
        IzinResponse created = izinService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Daftar izin pending untuk hari ini.
     *
     * Access: ADMIN, GURU_PIKET
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU_PIKET')")
    public List<IzinResponse> getPendingToday() {
        return izinService.getPendingToday();
    }

    /**
     * Daftar izin milik user yang login.
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<IzinResponse> getMine() {
        return izinService.getMine();
    }

    /**
     * Pencarian izin dengan filter siswaId/status.
     *
     * Access: ADMIN, GURU_PIKET, GURU_BK, WAKAKURIKULUM
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','GURU_PIKET','GURU_BK','WAKAKURIKULUM')")
    public List<IzinResponse> search(
            @RequestParam(required = false) Long siswaId,
            @RequestParam(required = false) String status
    ) {
        return izinService.search(siswaId, status);
    }

    /**
     * Daftar semua izin (opsional, untuk admin).
     *
     * Access: ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<IzinResponse> getAll() {
        return izinService.getAll();
    }

    /**
     * Approve / reject izin.
     *
     * Body:
     * {
     *   "status": "APPROVED" / "REJECTED",
     *   "catatan": "Optional note"
     * }
     *
     * Access: ADMIN, GURU_PIKET
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU_PIKET')")
    public IzinResponse approve(
            @PathVariable Long id,
            @RequestBody @Valid IzinApprovalRequest request
    ) {
        return izinService.approve(id, request);
    }
}

