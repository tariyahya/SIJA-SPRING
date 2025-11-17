package com.smk.presensi.dto.laporan;

import com.smk.presensi.dto.presensi.PresensiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO untuk laporan harian.
 * 
 * Contains:
 * - tanggal: Date of report
 * - totalPresensi: Total checkins on that day
 * - totalHadir: Count of HADIR
 * - totalTerlambat: Count of TERLAMBAT
 * - totalAlfa: Count of ALFA (not checked in)
 * - persentaseHadir: HADIR percentage
 * - persentaseTerlambat: TERLAMBAT percentage
 * - persentaseAlfa: ALFA percentage
 * - daftarPresensi: List of all presensi records
 * 
 * Example:
 * {
 *   "tanggal": "2025-01-17",
 *   "totalPresensi": 100,
 *   "totalHadir": 75,
 *   "totalTerlambat": 20,
 *   "totalAlfa": 5,
 *   "persentaseHadir": 75.0,
 *   "persentaseTerlambat": 20.0,
 *   "persentaseAlfa": 5.0,
 *   "daftarPresensi": [...]
 * }
 * 
 * @author Copilot Assistant
 * @since Tahap 9 (Reporting & Analytics)
 */
public record LaporanHarianResponse(
        LocalDate tanggal,
        int totalPresensi,
        int totalHadir,
        int totalTerlambat,
        int totalAlfa,
        double persentaseHadir,
        double persentaseTerlambat,
        double persentaseAlfa,
        List<PresensiResponse> daftarPresensi
) {
}
