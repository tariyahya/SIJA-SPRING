package com.smk.presensi.dto.laporan;

import java.time.LocalDate;

/**
 * Response DTO untuk laporan bulanan (rekapitulasi).
 * 
 * Contains:
 * - bulan: Month (1-12)
 * - tahun: Year (e.g., 2025)
 * - periodeAwal: Start date (e.g., 2025-01-01)
 * - periodeAkhir: End date (e.g., 2025-01-31)
 * - totalPresensi: Total checkins in the month
 * - totalHadir: Count of HADIR
 * - totalTerlambat: Count of TERLAMBAT
 * - totalAlfa: Count of ALFA
 * - persentaseHadir: HADIR percentage
 * - persentaseTerlambat: TERLAMBAT percentage
 * - persentaseAlfa: ALFA percentage
 * - totalManual: Count of MANUAL method
 * - totalRfid: Count of RFID method
 * - totalBarcode: Count of BARCODE method
 * - totalFace: Count of FACE method
 * 
 * Example:
 * {
 *   "bulan": 1,
 *   "tahun": 2025,
 *   "periodeAwal": "2025-01-01",
 *   "periodeAkhir": "2025-01-31",
 *   "totalPresensi": 2000,
 *   "totalHadir": 1500,
 *   "totalTerlambat": 400,
 *   "totalAlfa": 100,
 *   "persentaseHadir": 75.0,
 *   "persentaseTerlambat": 20.0,
 *   "persentaseAlfa": 5.0,
 *   "totalManual": 800,
 *   "totalRfid": 600,
 *   "totalBarcode": 400,
 *   "totalFace": 200
 * }
 * 
 * @author Copilot Assistant
 * @since Tahap 9 (Reporting & Analytics)
 */
public record LaporanBulananResponse(
        int bulan,
        int tahun,
        LocalDate periodeAwal,
        LocalDate periodeAkhir,
        long totalPresensi,
        long totalHadir,
        long totalTerlambat,
        long totalAlfa,
        double persentaseHadir,
        double persentaseTerlambat,
        double persentaseAlfa,
        long totalManual,
        long totalRfid,
        long totalBarcode,
        long totalFace
) {
}
