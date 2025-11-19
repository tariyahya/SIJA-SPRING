package com.smk.presensi.controller;

import com.smk.presensi.dto.pkl.PenempatanPklRequest;
import com.smk.presensi.dto.pkl.PenempatanPklResponse;
import com.smk.presensi.service.PenempatanPklService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller untuk penempatan siswa PKL ke DUDI.
 */
@RestController
@RequestMapping("/api/pkl")
public class PenempatanPklController {

    private final PenempatanPklService penempatanPklService;

    public PenempatanPklController(PenempatanPklService penempatanPklService) {
        this.penempatanPklService = penempatanPklService;
    }

    /**
     * POST /api/pkl
     * Create penempatan PKL baru.
     *
     * Access: ADMIN, WAKAHUBIN, GURU_PEMBIMBING
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN', 'GURU_PEMBIMBING')")
    public ResponseEntity<PenempatanPklResponse> create(@RequestBody @Valid PenempatanPklRequest request) {
        PenempatanPklResponse created = penempatanPklService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/pkl/dudi/{dudiId}
     * Daftar siswa yang ditempatkan di DUDI tertentu.
     */
    @GetMapping("/dudi/{dudiId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN', 'GURU_PEMBIMBING')")
    public List<PenempatanPklResponse> getByDudi(@PathVariable Long dudiId) {
        return penempatanPklService.findByDudi(dudiId);
    }

    /**
     * GET /api/pkl/siswa/{siswaId}
     * Riwayat penempatan PKL untuk 1 siswa.
     */
    @GetMapping("/siswa/{siswaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN', 'GURU_PEMBIMBING')")
    public List<PenempatanPklResponse> getBySiswa(@PathVariable Long siswaId) {
        return penempatanPklService.findBySiswa(siswaId);
    }
}

