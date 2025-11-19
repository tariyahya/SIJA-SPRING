package com.smk.presensi.controller;

import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.dto.pkl.DudiResponse;
import com.smk.presensi.dto.GuruResponse;
import com.smk.presensi.dto.KelasResponse;
import com.smk.presensi.dto.JurusanResponse;
import com.smk.presensi.dto.UserResponse;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.service.DudiService;
import com.smk.presensi.service.ExcelService;
import com.smk.presensi.service.SiswaService;
import com.smk.presensi.service.GuruService;
import com.smk.presensi.service.KelasService;
import com.smk.presensi.service.JurusanService;
import com.smk.presensi.service.UserService;
import com.smk.presensi.service.PresensiService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final GuruService guruService;
    private final KelasService kelasService;
    private final JurusanService jurusanService;
    private final UserService userService;
    private final PresensiService presensiService;

    public ImportExportController(ExcelService excelService, SiswaService siswaService, DudiService dudiService,
                                  GuruService guruService, KelasService kelasService, JurusanService jurusanService,
                                  UserService userService, PresensiService presensiService) {
        this.excelService = excelService;
        this.siswaService = siswaService;
        this.dudiService = dudiService;
        this.guruService = guruService;
        this.kelasService = kelasService;
        this.jurusanService = jurusanService;
        this.userService = userService;
        this.presensiService = presensiService;
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

    @GetMapping("/guru")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAKURIKULUM')")
    public ResponseEntity<InputStreamResource> exportGuru() {
        List<GuruResponse> guruList = guruService.findAll();
        ByteArrayInputStream in = excelService.exportGuruToExcel(guruList);
        return createExcelResponse(in, "guru.xlsx");
    }

    @GetMapping("/kelas")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAKURIKULUM')")
    public ResponseEntity<InputStreamResource> exportKelas() {
        List<KelasResponse> kelasList = kelasService.findAll();
        ByteArrayInputStream in = excelService.exportKelasToExcel(kelasList);
        return createExcelResponse(in, "kelas.xlsx");
    }

    @GetMapping("/jurusan")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAKAKURIKULUM')")
    public ResponseEntity<InputStreamResource> exportJurusan() {
        List<JurusanResponse> jurusanList = jurusanService.findAll();
        ByteArrayInputStream in = excelService.exportJurusanToExcel(jurusanList);
        return createExcelResponse(in, "jurusan.xlsx");
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> exportUsers() {
        List<UserResponse> userList = userService.findAll();
        ByteArrayInputStream in = excelService.exportUserToExcel(userList);
        return createExcelResponse(in, "users.xlsx");
    }

    @GetMapping("/presensi")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU_PIKET', 'WAKAKURIKULUM')")
    public ResponseEntity<InputStreamResource> exportPresensi() {
        List<PresensiResponse> presensiList = presensiService.findAll();
        ByteArrayInputStream in = excelService.exportPresensiToExcel(presensiList);
        return createExcelResponse(in, "presensi.xlsx");
    }

    @GetMapping("/template/{type}")
    public ResponseEntity<InputStreamResource> downloadTemplate(@PathVariable String type) {
        ByteArrayInputStream in = excelService.generateTemplate(type);
        return createExcelResponse(in, "template_" + type + ".xlsx");
    }

    private ResponseEntity<InputStreamResource> createExcelResponse(ByteArrayInputStream in, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
