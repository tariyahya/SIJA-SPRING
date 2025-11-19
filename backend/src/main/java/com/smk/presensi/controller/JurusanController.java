package com.smk.presensi.controller;

import com.smk.presensi.dto.JurusanRequest;
import com.smk.presensi.dto.JurusanResponse;
import com.smk.presensi.service.JurusanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jurusan")
public class JurusanController {

    private final JurusanService jurusanService;

    public JurusanController(JurusanService jurusanService) {
        this.jurusanService = jurusanService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<JurusanResponse> getAll() {
        return jurusanService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public JurusanResponse getById(@PathVariable Long id) {
        return jurusanService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JurusanResponse> create(@RequestBody @Valid JurusanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jurusanService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JurusanResponse update(@PathVariable Long id, @RequestBody @Valid JurusanRequest request) {
        return jurusanService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jurusanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

