package com.smk.presensi.controller;

import com.smk.presensi.dto.jadwal.JadwalMengajarRequest;
import com.smk.presensi.dto.jadwal.JadwalMengajarResponse;
import com.smk.presensi.service.JadwalMengajarService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guru/jadwal")
public class JadwalMengajarController {

    private final JadwalMengajarService jadwalMengajarService;

    public JadwalMengajarController(JadwalMengajarService jadwalMengajarService) {
        this.jadwalMengajarService = jadwalMengajarService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GURU')")
    public List<JadwalMengajarResponse> list(
            @RequestParam(required = false) Long guruId,
            @RequestParam(required = false) Long kelasId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal
    ) {
        return jadwalMengajarService.list(guruId, kelasId, tanggal);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JadwalMengajarResponse> create(@Valid @RequestBody JadwalMengajarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jadwalMengajarService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JadwalMengajarResponse update(@PathVariable Long id, @Valid @RequestBody JadwalMengajarRequest request) {
        return jadwalMengajarService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jadwalMengajarService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
