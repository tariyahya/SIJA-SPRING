package com.smk.presensi.controller;

import com.smk.presensi.dto.AssignSiswaToKelasRequest;
import com.smk.presensi.dto.KelasRequest;
import com.smk.presensi.dto.KelasResponse;
import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.service.KelasService;
import com.smk.presensi.service.SiswaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kelas")
public class KelasController {

    private final KelasService kelasService;
    private final SiswaService siswaService;

    public KelasController(KelasService kelasService, SiswaService siswaService) {
        this.kelasService = kelasService;
        this.siswaService = siswaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<KelasResponse> getAll() {
        return kelasService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public KelasResponse getById(@PathVariable Long id) {
        return kelasService.findById(id);
    }

    @GetMapping("/jurusan/{jurusan}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<KelasResponse> getByJurusan(@PathVariable String jurusan) {
        return kelasService.findByJurusan(jurusan);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KelasResponse> create(@RequestBody @Valid KelasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kelasService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public KelasResponse update(@PathVariable Long id, @RequestBody @Valid KelasRequest request) {
        return kelasService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        kelasService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign beberapa siswa ke kelas tertentu.
     *
     * URL: POST /api/kelas/{id}/assign-siswa
     * Body: { "siswaIds": [1, 2, 3] }
     *
     * Access: ADMIN only
     */
    @PostMapping("/{id}/assign-siswa")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SiswaResponse> assignSiswaToKelas(
            @PathVariable Long id,
            @RequestBody @Valid AssignSiswaToKelasRequest request
    ) {
        return siswaService.assignToKelas(id, request);
    }
}
