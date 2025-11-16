package com.smk.presensi.controller;

import com.smk.presensi.dto.presensi.CheckinRequest;
import com.smk.presensi.dto.presensi.CheckoutRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.service.PresensiService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * CONTROLLER untuk Presensi - Handle HTTP request presensi.
 * 
 * Endpoints:
 * - POST /api/presensi/checkin → User checkin (SISWA, GURU)
 * - POST /api/presensi/checkout → User checkout (SISWA, GURU)
 * - GET /api/presensi/histori → Lihat history presensi sendiri (SISWA, GURU)
 * - GET /api/presensi → Lihat semua presensi (ADMIN, GURU)
 */
@RestController
@RequestMapping("/api/presensi")
public class PresensiController {

    private final PresensiService presensiService;

    public PresensiController(PresensiService presensiService) {
        this.presensiService = presensiService;
    }

    /**
     * CHECKIN - User checkin presensi (pagi).
     * 
     * Access: SISWA, GURU
     * URL: POST /api/presensi/checkin
     */
    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('SISWA', 'GURU')")
    public ResponseEntity<PresensiResponse> checkin(@RequestBody @Valid CheckinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(presensiService.checkin(request));
    }

    /**
     * CHECKOUT - User checkout presensi (sore).
     * 
     * Access: SISWA, GURU
     * URL: POST /api/presensi/checkout
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('SISWA', 'GURU')")
    public ResponseEntity<PresensiResponse> checkout(@RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(presensiService.checkout(request));
    }

    /**
     * GET HISTORI - Lihat history presensi sendiri.
     * 
     * Access: SISWA, GURU
     * URL: GET /api/presensi/histori?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/histori")
    @PreAuthorize("hasAnyRole('SISWA', 'GURU')")
    public List<PresensiResponse> getHistori(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return presensiService.getHistori(startDate, endDate);
    }

    /**
     * GET ALL PRESENSI - Lihat semua presensi (untuk admin/guru).
     * 
     * Access: ADMIN, GURU
     * URL: GET /api/presensi?tanggal=2024-01-15
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<PresensiResponse> getAllPresensi(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal
    ) {
        return presensiService.getAllPresensi(tanggal);
    }
}
