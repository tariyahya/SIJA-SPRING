package com.smk.presensi.controller;

import com.smk.presensi.dto.uh.UlanganHarianRequest;
import com.smk.presensi.dto.uh.UlanganHarianResponse;
import com.smk.presensi.service.UlanganHarianService;
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
@RequestMapping("/api/akademik/uh")
public class UlanganHarianController {

    private final UlanganHarianService ulanganHarianService;

    public UlanganHarianController(UlanganHarianService ulanganHarianService) {
        this.ulanganHarianService = ulanganHarianService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GURU')")
    public ResponseEntity<UlanganHarianResponse> create(@Valid @RequestBody UlanganHarianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ulanganHarianService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GURU')")
    public List<UlanganHarianResponse> list(
            @RequestParam(required = false) Long guruId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ulanganHarianService.list(guruId, startDate, endDate);
    }
}
