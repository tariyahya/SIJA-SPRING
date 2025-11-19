package com.smk.presensi.controller;

import com.smk.presensi.dto.pkl.DudiRequest;
import com.smk.presensi.dto.pkl.DudiResponse;
import com.smk.presensi.service.DudiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller untuk master DUDI (perusahaan PKL).
 */
@RestController
@RequestMapping("/api/dudi")
public class DudiController {

    private final DudiService dudiService;

    public DudiController(DudiService dudiService) {
        this.dudiService = dudiService;
    }

    /**
     * GET /api/dudi
     * List semua DUDI.
     *
     * Access: ADMIN, WAKAHUBIN, GURU_PEMBIMBING
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN', 'GURU_PEMBIMBING')")
    public List<DudiResponse> getAll() {
        return dudiService.findAll();
    }

    /**
     * GET /api/dudi/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN', 'GURU_PEMBIMBING')")
    public DudiResponse getById(@PathVariable Long id) {
        return dudiService.findById(id);
    }

    /**
     * POST /api/dudi
     * Create DUDI baru.
     *
     * Access: ADMIN, WAKAHUBIN, GURU_PEMBIMBING (sesuai matrix Manage PKL).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN', 'GURU_PEMBIMBING')")
    public ResponseEntity<DudiResponse> create(@RequestBody @Valid DudiRequest request) {
        DudiResponse created = dudiService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/dudi/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN', 'GURU_PEMBIMBING')")
    public DudiResponse update(@PathVariable Long id, @RequestBody @Valid DudiRequest request) {
        return dudiService.update(id, request);
    }

    /**
     * DELETE /api/dudi/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dudiService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

