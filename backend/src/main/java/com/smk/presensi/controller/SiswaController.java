package com.smk.presensi.controller;

import com.smk.presensi.dto.SiswaRequest;
import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.service.SiswaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/siswa")
public class SiswaController {

    private final SiswaService siswaService;

    public SiswaController(SiswaService siswaService) {
        this.siswaService = siswaService;
    }

    @GetMapping
    public List<SiswaResponse> getAll() {
        return siswaService.findAll();
    }

    @GetMapping("/{id}")
    public SiswaResponse getById(@PathVariable Long id) {
        return siswaService.findById(id);
    }

    @PostMapping
    public ResponseEntity<SiswaResponse> create(@RequestBody @Valid SiswaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(siswaService.create(request));
    }

    @PutMapping("/{id}")
    public SiswaResponse update(@PathVariable Long id, @RequestBody @Valid SiswaRequest request) {
        return siswaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        siswaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/kelas/{namaKelas}")
    public List<SiswaResponse> getByKelas(@PathVariable String namaKelas) {
        return siswaService.findByKelas(namaKelas);
    }
}
