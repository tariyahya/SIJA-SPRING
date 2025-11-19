package com.smk.presensi.controller;

import com.smk.presensi.dto.jurnal.GuruJurnalRequest;
import com.smk.presensi.dto.jurnal.GuruJurnalResponse;
import com.smk.presensi.service.GuruJurnalService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guru/jurnal")
public class GuruJurnalController {

    private final GuruJurnalService guruJurnalService;

    public GuruJurnalController(GuruJurnalService guruJurnalService) {
        this.guruJurnalService = guruJurnalService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GURU')")
    public ResponseEntity<GuruJurnalResponse> create(@Valid @RequestBody GuruJurnalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guruJurnalService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GURU')")
    public List<GuruJurnalResponse> list(
            @RequestParam(required = false) Long guruId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return guruJurnalService.list(guruId, startDate, endDate);
    }
}
