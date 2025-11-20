package com.smk.presensi.controller;

import com.smk.presensi.dto.koreksi.KoreksiPresensiApprovalRequest;
import com.smk.presensi.dto.koreksi.KoreksiPresensiRequest;
import com.smk.presensi.dto.koreksi.KoreksiPresensiResponse;
import com.smk.presensi.enums.KoreksiStatus;
import com.smk.presensi.service.KoreksiPresensiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API untuk permintaan koreksi presensi.
 */
@RestController
@RequestMapping("/api/presensi/koreksi")
public class KoreksiPresensiController {

    private final KoreksiPresensiService koreksiPresensiService;

    public KoreksiPresensiController(KoreksiPresensiService koreksiPresensiService) {
        this.koreksiPresensiService = koreksiPresensiService;
    }

    /**
     * Ajukan koreksi presensi.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SISWA','GURU','ADMIN','GURU_PIKET','GURU_BK','WAKAKURIKULUM','WAKAHUBIN','KAPROG')")
    public ResponseEntity<KoreksiPresensiResponse> create(
            @RequestBody @Valid KoreksiPresensiRequest request
    ) {
        KoreksiPresensiResponse created = koreksiPresensiService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Koreksi milik user yang login.
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<KoreksiPresensiResponse> myRequests() {
        return koreksiPresensiService.findMine();
    }

    /**
     * Daftar koreksi PENDING.
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','GURU_PIKET','GURU_BK','WAKAKURIKULUM','WAKAHUBIN')")
    public List<KoreksiPresensiResponse> pending() {
        return koreksiPresensiService.findPending();
    }

    /**
     * Approve / reject koreksi.
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','GURU_PIKET','GURU_BK','WAKAKURIKULUM','WAKAHUBIN')")
    public KoreksiPresensiResponse approve(
            @PathVariable Long id,
            @RequestBody @Valid KoreksiPresensiApprovalRequest request
    ) {
        return koreksiPresensiService.approve(id, request);
    }

    /**
     * Daftar semua koreksi dengan filter status (opsional).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GURU_PIKET','GURU_BK','WAKAKURIKULUM','WAKAHUBIN')")
    public List<KoreksiPresensiResponse> getAll(
            @RequestParam(value = "status", required = false) String status
    ) {
        KoreksiStatus filter = null;
        if (status != null && !status.isBlank()) {
            filter = KoreksiStatus.valueOf(status.toUpperCase());
        }
        return koreksiPresensiService.findAll(filter);
    }
}
