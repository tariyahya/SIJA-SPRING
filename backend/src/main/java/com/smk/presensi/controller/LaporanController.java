package com.smk.presensi.controller;

import com.smk.presensi.dto.laporan.LaporanBulananResponse;
import com.smk.presensi.dto.laporan.LaporanHarianResponse;
import com.smk.presensi.dto.laporan.StatistikResponse;
import com.smk.presensi.service.LaporanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller untuk reporting dan analytics.
 * 
 * Base URL: /api/laporan
 * Security: All endpoints require ADMIN or GURU role
 * 
 * Endpoints:
 * - GET /api/laporan/harian?tanggal=2025-01-17 → Daily report
 * - GET /api/laporan/bulanan?bulan=1&tahun=2025 → Monthly report
 * - GET /api/laporan/statistik → Overall statistics
 * - GET /api/laporan/statistik?start=2025-01-01&end=2025-01-31 → Period statistics
 * 
 * @author Copilot Assistant
 * @since Tahap 9 (Reporting & Analytics)
 */
@RestController
@RequestMapping("/api/laporan")
public class LaporanController {
    
    @Autowired
    private LaporanService laporanService;
    
    /**
     * GET /api/laporan/harian?tanggal=2025-01-17
     * Get daily report for specific date.
     * 
     * Access: ADMIN or GURU
     * 
     * Query Parameters:
     * - tanggal (optional): Date in format yyyy-MM-dd (default: today)
     * 
     * Response:
     * {
     *   "message": "Laporan harian berhasil diambil",
     *   "data": {
     *     "tanggal": "2025-01-17",
     *     "totalPresensi": 100,
     *     "totalHadir": 75,
     *     "totalTerlambat": 20,
     *     "totalAlfa": 5,
     *     "persentaseHadir": 75.0,
     *     "persentaseTerlambat": 20.0,
     *     "persentaseAlfa": 5.0,
     *     "daftarPresensi": [...]
     *   }
     * }
     */
    @GetMapping("/harian")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public ResponseEntity<Map<String, Object>> getLaporanHarian(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate tanggal
    ) {
        // Default to today if no date provided
        if (tanggal == null) {
            tanggal = LocalDate.now();
        }
        
        LaporanHarianResponse laporan = laporanService.getLaporanHarian(tanggal);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Laporan harian berhasil diambil");
        response.put("data", laporan);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/laporan/bulanan?bulan=1&tahun=2025
     * Get monthly report (rekapitulasi).
     * 
     * Access: ADMIN or GURU
     * 
     * Query Parameters:
     * - bulan (optional): Month 1-12 (default: current month)
     * - tahun (optional): Year (default: current year)
     * 
     * Response:
     * {
     *   "message": "Laporan bulanan berhasil diambil",
     *   "data": {
     *     "bulan": 1,
     *     "tahun": 2025,
     *     "periodeAwal": "2025-01-01",
     *     "periodeAkhir": "2025-01-31",
     *     "totalPresensi": 2000,
     *     "totalHadir": 1500,
     *     "totalTerlambat": 400,
     *     "totalAlfa": 100,
     *     "persentaseHadir": 75.0,
     *     "persentaseTerlambat": 20.0,
     *     "persentaseAlfa": 5.0,
     *     "totalManual": 800,
     *     "totalRfid": 600,
     *     "totalBarcode": 400,
     *     "totalFace": 200
     *   }
     * }
     */
    @GetMapping("/bulanan")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public ResponseEntity<Map<String, Object>> getLaporanBulanan(
            @RequestParam(required = false) Integer bulan,
            @RequestParam(required = false) Integer tahun
    ) {
        // Default to current month/year if not provided
        LocalDate now = LocalDate.now();
        if (bulan == null) {
            bulan = now.getMonthValue();
        }
        if (tahun == null) {
            tahun = now.getYear();
        }
        
        // Validate month range
        if (bulan < 1 || bulan > 12) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Bulan harus antara 1-12");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        LaporanBulananResponse laporan = laporanService.getLaporanBulanan(bulan, tahun);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Laporan bulanan berhasil diambil");
        response.put("data", laporan);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/laporan/statistik
     * GET /api/laporan/statistik?start=2025-01-01&end=2025-01-31
     * Get statistics (overall or for specific period).
     * 
     * Access: ADMIN or GURU
     * 
     * Query Parameters:
     * - start (optional): Start date yyyy-MM-dd
     * - end (optional): End date yyyy-MM-dd
     * 
     * If no parameters: Return all-time statistics
     * If both provided: Return statistics for that period
     * 
     * Response:
     * {
     *   "message": "Statistik berhasil diambil",
     *   "data": {
     *     "totalPresensi": 5000,
     *     "totalHadir": 3750,
     *     "totalTerlambat": 1000,
     *     "totalAlfa": 250,
     *     "persentaseHadir": 75.0,
     *     "persentaseTerlambat": 20.0,
     *     "persentaseAlfa": 5.0,
     *     "totalManual": 2000,
     *     "totalRfid": 1500,
     *     "totalBarcode": 1000,
     *     "totalFace": 500,
     *     "persentaseManual": 40.0,
     *     "persentaseRfid": 30.0,
     *     "persentaseBarcode": 20.0,
     *     "persentaseFace": 10.0
     *   }
     * }
     */
    @GetMapping("/statistik")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public ResponseEntity<Map<String, Object>> getStatistik(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate start,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate end
    ) {
        StatistikResponse statistik = laporanService.getStatistik(start, end);
        
        Map<String, Object> response = new HashMap<>();
        if (start != null && end != null) {
            response.put("message", "Statistik periode " + start + " hingga " + end + " berhasil diambil");
        } else {
            response.put("message", "Statistik keseluruhan berhasil diambil");
        }
        response.put("data", statistik);
        
        return ResponseEntity.ok(response);
    }
}
