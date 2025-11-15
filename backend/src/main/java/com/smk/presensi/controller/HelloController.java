package com.smk.presensi.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Presensi SMK – Tahap 1");
    }

    @GetMapping("/info")
    public AppInfo info() {
        return new AppInfo("Presensi Siswa & Guru", "0.1", "Nama_Kelompok");
    }

    public record AppInfo(String app, String version, String developer) {
    }
}
