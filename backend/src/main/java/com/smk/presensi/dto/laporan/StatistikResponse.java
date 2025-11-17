package com.smk.presensi.dto.laporan;

/**
 * Response DTO untuk statistik kehadiran.
 * 
 * General statistics across all time or specific period.
 * 
 * Contains:
 * - totalPresensi: Total all checkins
 * - totalHadir: Count of HADIR
 * - totalTerlambat: Count of TERLAMBAT
 * - totalAlfa: Count of ALFA
 * - persentaseHadir: HADIR percentage
 * - persentaseTerlambat: TERLAMBAT percentage
 * - persentaseAlfa: ALFA percentage
 * - totalManual: MANUAL method count
 * - totalRfid: RFID method count
 * - totalBarcode: BARCODE method count
 * - totalFace: FACE method count
 * - persentaseManual: MANUAL percentage
 * - persentaseRfid: RFID percentage
 * - persentaseBarcode: BARCODE percentage
 * - persentaseFace: FACE percentage
 * 
 * Example:
 * {
 *   "totalPresensi": 5000,
 *   "totalHadir": 3750,
 *   "totalTerlambat": 1000,
 *   "totalAlfa": 250,
 *   "persentaseHadir": 75.0,
 *   "persentaseTerlambat": 20.0,
 *   "persentaseAlfa": 5.0,
 *   "totalManual": 2000,
 *   "totalRfid": 1500,
 *   "totalBarcode": 1000,
 *   "totalFace": 500,
 *   "persentaseManual": 40.0,
 *   "persentaseRfid": 30.0,
 *   "persentaseBarcode": 20.0,
 *   "persentaseFace": 10.0
 * }
 * 
 * @author Copilot Assistant
 * @since Tahap 9 (Reporting & Analytics)
 */
public record StatistikResponse(
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
        long totalFace,
        double persentaseManual,
        double persentaseRfid,
        double persentaseBarcode,
        double persentaseFace
) {
}
