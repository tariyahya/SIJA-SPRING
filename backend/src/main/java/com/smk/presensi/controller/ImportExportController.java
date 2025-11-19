package com.smk.presensi.controller;

import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.dto.pkl.DudiResponse;
import com.smk.presensi.service.DudiService;
import com.smk.presensi.service.ExcelService;
import com.smk.presensi.service.SiswaService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/export")
public class ImportExportController {

    private final ExcelService excelService;
    private final SiswaService siswaService;
    private final DudiService dudiService;

    public ImportExportController(ExcelService excelService, SiswaService siswaService, DudiService dudiService) {
        this.excelService = excelService;
        this.siswaService = siswaService;
        this.dudiService = dudiService;
    }

    @GetMapping("/siswa")
    @PreAuthorize("hasAnyRole('ADMIN', 'KAPROG', 'WAKAKURIKULUM')")
    public ResponseEntity<InputStreamResource> exportSiswa() {
        List<SiswaResponse> siswaList = siswaService.findAll();
        ByteArrayInputStream in = excelService.exportSiswaToExcel(siswaList);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=siswa.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/dudi")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAHUBIN', 'GURU_PEMBIMBING')")
    public ResponseEntity<InputStreamResource> exportDudi() {
        List<DudiResponse> dudiList = dudiService.findAll();
        ByteArrayInputStream in = excelService.exportDudiToExcel(dudiList);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=dudi.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
