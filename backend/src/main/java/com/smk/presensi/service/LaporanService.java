package com.smk.presensi.service;

import com.smk.presensi.dto.laporan.LaporanBulananResponse;
import com.smk.presensi.dto.laporan.LaporanHarianResponse;
import com.smk.presensi.dto.laporan.StatistikResponse;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.entity.Presensi;
import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.repository.PresensiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service untuk generate laporan presensi dan statistik.
 * 
 * Features:
 * - Laporan harian (per tanggal)
 * - Laporan bulanan (rekapitulasi per bulan)
 * - Statistik kehadiran (HADIR%, TERLAMBAT%, ALFA%)
 * - Method usage analytics (MANUAL, RFID, BARCODE, FACE)
 * 
 * @author Copilot Assistant
 * @since Tahap 9 (Reporting & Analytics)
 */
@Service
public class LaporanService {
    
    @Autowired
    private PresensiRepository presensiRepository;
    
    /**
     * Generate laporan harian untuk tanggal tertentu.
     * 
     * Logic:
     * 1. Query all presensi on that date
     * 2. Count by status (HADIR, TERLAMBAT, ALFA)
     * 3. Calculate percentages
     * 4. Return detailed report
     * 
     * Example:
     * - Date: 2025-01-17
     * - Total: 100 siswa
     * - HADIR: 75 (75%)
     * - TERLAMBAT: 20 (20%)
     * - ALFA: 5 (5%)
     * 
     * @param tanggal Date to generate report
     * @return LaporanHarianResponse with statistics
     */
    public LaporanHarianResponse getLaporanHarian(LocalDate tanggal) {
        // 1. Get all presensi records for this date
        List<Presensi> presensiList = presensiRepository.findByTanggal(tanggal);
        
        // 2. Count by status
        long totalHadir = presensiList.stream()
                .filter(p -> p.getStatus() == StatusPresensi.HADIR)
                .count();
        
        long totalTerlambat = presensiList.stream()
                .filter(p -> p.getStatus() == StatusPresensi.TERLAMBAT)
                .count();
        
        long totalAlfa = presensiList.stream()
                .filter(p -> p.getStatus() == StatusPresensi.ALPHA)
                .count();
        
        int totalPresensi = presensiList.size();
        
        // 3. Calculate percentages
        double persentaseHadir = totalPresensi > 0 ? (totalHadir * 100.0 / totalPresensi) : 0.0;
        double persentaseTerlambat = totalPresensi > 0 ? (totalTerlambat * 100.0 / totalPresensi) : 0.0;
        double persentaseAlfa = totalPresensi > 0 ? (totalAlfa * 100.0 / totalPresensi) : 0.0;
        
        // 4. Convert to DTO
        List<PresensiResponse> daftarPresensi = presensiList.stream()
                .map(this::toPresensiResponse)
                .collect(Collectors.toList());
        
        return new LaporanHarianResponse(
                tanggal,
                totalPresensi,
                (int) totalHadir,
                (int) totalTerlambat,
                (int) totalAlfa,
                Math.round(persentaseHadir * 100.0) / 100.0,  // Round to 2 decimal places
                Math.round(persentaseTerlambat * 100.0) / 100.0,
                Math.round(persentaseAlfa * 100.0) / 100.0,
                daftarPresensi
        );
    }
    
    /**
     * Generate laporan bulanan (rekapitulasi).
     * 
     * Logic:
     * 1. Calculate date range (start and end of month)
     * 2. Query all presensi in that month
     * 3. Count by status (HADIR, TERLAMBAT, ALFA)
     * 4. Count by method (MANUAL, RFID, BARCODE, FACE)
     * 5. Calculate percentages
     * 
     * Example:
     * - Month: January 2025
     * - Total: 2000 checkins
     * - HADIR: 1500 (75%), TERLAMBAT: 400 (20%), ALFA: 100 (5%)
     * - MANUAL: 800 (40%), RFID: 600 (30%), BARCODE: 400 (20%), FACE: 200 (10%)
     * 
     * @param bulan Month (1-12)
     * @param tahun Year (e.g., 2025)
     * @return LaporanBulananResponse with aggregated statistics
     */
    public LaporanBulananResponse getLaporanBulanan(int bulan, int tahun) {
        // 1. Calculate date range
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDate periodeAwal = yearMonth.atDay(1);  // First day of month
        LocalDate periodeAkhir = yearMonth.atEndOfMonth();  // Last day of month
        
        // 2. Count by status
        long totalHadir = presensiRepository.countByStatusAndTanggalBetween(
                StatusPresensi.HADIR, periodeAwal, periodeAkhir);
        
        long totalTerlambat = presensiRepository.countByStatusAndTanggalBetween(
                StatusPresensi.TERLAMBAT, periodeAwal, periodeAkhir);
        
        long totalAlfa = presensiRepository.countByStatusAndTanggalBetween(
                StatusPresensi.ALPHA, periodeAwal, periodeAkhir);
        
        long totalPresensi = presensiRepository.countByTanggalBetween(periodeAwal, periodeAkhir);
        
        // 3. Count by method
        long totalManual = presensiRepository.countByMethodAndTanggalBetween(
                MethodPresensi.MANUAL, periodeAwal, periodeAkhir);
        
        long totalRfid = presensiRepository.countByMethodAndTanggalBetween(
                MethodPresensi.RFID, periodeAwal, periodeAkhir);
        
        long totalBarcode = presensiRepository.countByMethodAndTanggalBetween(
                MethodPresensi.BARCODE, periodeAwal, periodeAkhir);
        
        long totalFace = presensiRepository.countByMethodAndTanggalBetween(
                MethodPresensi.FACE, periodeAwal, periodeAkhir);
        
        // 4. Calculate percentages
        double persentaseHadir = totalPresensi > 0 ? (totalHadir * 100.0 / totalPresensi) : 0.0;
        double persentaseTerlambat = totalPresensi > 0 ? (totalTerlambat * 100.0 / totalPresensi) : 0.0;
        double persentaseAlfa = totalPresensi > 0 ? (totalAlfa * 100.0 / totalPresensi) : 0.0;
        
        return new LaporanBulananResponse(
                bulan,
                tahun,
                periodeAwal,
                periodeAkhir,
                totalPresensi,
                totalHadir,
                totalTerlambat,
                totalAlfa,
                Math.round(persentaseHadir * 100.0) / 100.0,
                Math.round(persentaseTerlambat * 100.0) / 100.0,
                Math.round(persentaseAlfa * 100.0) / 100.0,
                totalManual,
                totalRfid,
                totalBarcode,
                totalFace
        );
    }
    
    /**
     * Get statistik kehadiran untuk periode tertentu atau semua data.
     * 
     * Logic:
     * 1. Query presensi within date range (or all if no range)
     * 2. Count by status and method
     * 3. Calculate percentages
     * 
     * Use cases:
     * - Dashboard: Show overall statistics
     * - Admin: Monitor attendance trends
     * - Analysis: Compare method effectiveness
     * 
     * @param startDate Start date (optional, null = all time)
     * @param endDate End date (optional, null = all time)
     * @return StatistikResponse with comprehensive statistics
     */
    public StatistikResponse getStatistik(LocalDate startDate, LocalDate endDate) {
        // If no date range provided, use all time
        if (startDate == null || endDate == null) {
            startDate = LocalDate.of(2000, 1, 1);  // Far past
            endDate = LocalDate.of(2100, 12, 31);  // Far future
        }
        
        // 1. Count by status
        long totalHadir = presensiRepository.countByStatusAndTanggalBetween(
                StatusPresensi.HADIR, startDate, endDate);
        
        long totalTerlambat = presensiRepository.countByStatusAndTanggalBetween(
                StatusPresensi.TERLAMBAT, startDate, endDate);
        
        long totalAlfa = presensiRepository.countByStatusAndTanggalBetween(
                StatusPresensi.ALPHA, startDate, endDate);
        
        long totalPresensi = presensiRepository.countByTanggalBetween(startDate, endDate);
        
        // 2. Count by method
        long totalManual = presensiRepository.countByMethodAndTanggalBetween(
                MethodPresensi.MANUAL, startDate, endDate);
        
        long totalRfid = presensiRepository.countByMethodAndTanggalBetween(
                MethodPresensi.RFID, startDate, endDate);
        
        long totalBarcode = presensiRepository.countByMethodAndTanggalBetween(
                MethodPresensi.BARCODE, startDate, endDate);
        
        long totalFace = presensiRepository.countByMethodAndTanggalBetween(
                MethodPresensi.FACE, startDate, endDate);
        
        // 3. Calculate percentages for status
        double persentaseHadir = totalPresensi > 0 ? (totalHadir * 100.0 / totalPresensi) : 0.0;
        double persentaseTerlambat = totalPresensi > 0 ? (totalTerlambat * 100.0 / totalPresensi) : 0.0;
        double persentaseAlfa = totalPresensi > 0 ? (totalAlfa * 100.0 / totalPresensi) : 0.0;
        
        // 4. Calculate percentages for method
        double persentaseManual = totalPresensi > 0 ? (totalManual * 100.0 / totalPresensi) : 0.0;
        double persentaseRfid = totalPresensi > 0 ? (totalRfid * 100.0 / totalPresensi) : 0.0;
        double persentaseBarcode = totalPresensi > 0 ? (totalBarcode * 100.0 / totalPresensi) : 0.0;
        double persentaseFace = totalPresensi > 0 ? (totalFace * 100.0 / totalPresensi) : 0.0;
        
        return new StatistikResponse(
                totalPresensi,
                totalHadir,
                totalTerlambat,
                totalAlfa,
                Math.round(persentaseHadir * 100.0) / 100.0,
                Math.round(persentaseTerlambat * 100.0) / 100.0,
                Math.round(persentaseAlfa * 100.0) / 100.0,
                totalManual,
                totalRfid,
                totalBarcode,
                totalFace,
                Math.round(persentaseManual * 100.0) / 100.0,
                Math.round(persentaseRfid * 100.0) / 100.0,
                Math.round(persentaseBarcode * 100.0) / 100.0,
                Math.round(persentaseFace * 100.0) / 100.0
        );
    }
    
    /**
     * Convert Presensi entity to PresensiResponse DTO.
     */
    private PresensiResponse toPresensiResponse(Presensi presensi) {
        return new PresensiResponse(
                presensi.getId(),
                presensi.getUser().getId(),
                presensi.getUser().getUsername(),
                presensi.getTipe(),
                presensi.getTanggal(),
                presensi.getJamMasuk(),
                presensi.getJamPulang(),  // UPDATED: Now includes jamPulang (Tahap 10)
                presensi.getStatus(),
                presensi.getMethod(),
                presensi.getLatitude(),
                presensi.getLongitude(),
                presensi.getKeterangan()
        );
    }

    // ===== TAHAP 10: WORK HOURS ANALYTICS =====

    /**
     * Get average work hours for a period.
     * 
     * Logic:
     * 1. Get all presensi with jamPulang (completed checkouts)
     * 2. Calculate work duration for each
     * 3. Return average
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Average work hours in decimal (e.g., 8.5 = 8 jam 30 menit)
     */
    public double getAverageWorkHours(LocalDate startDate, LocalDate endDate) {
        List<Presensi> presensiList = presensiRepository.findByTanggalBetween(startDate, endDate);
        
        // Filter only completed (has jamPulang)
        List<Presensi> completed = presensiList.stream()
                .filter(p -> p.getJamPulang() != null)
                .toList();
        
        if (completed.isEmpty()) {
            return 0.0;
        }
        
        // Calculate total minutes
        long totalMinutes = completed.stream()
                .mapToLong(p -> {
                    java.time.Duration duration = java.time.Duration.between(
                        p.getJamMasuk(), 
                        p.getJamPulang()
                    );
                    return duration.toMinutes();
                })
                .sum();
        
        // Average minutes
        double avgMinutes = (double) totalMinutes / completed.size();
        
        // Convert to hours (decimal)
        return Math.round((avgMinutes / 60.0) * 100.0) / 100.0;
    }

    /**
     * Count overtime instances (work > 8 hours).
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Number of overtime instances
     */
    public long countOvertime(LocalDate startDate, LocalDate endDate) {
        List<Presensi> presensiList = presensiRepository.findByTanggalBetween(startDate, endDate);
        
        return presensiList.stream()
                .filter(p -> p.getJamPulang() != null)
                .filter(p -> {
                    java.time.Duration duration = java.time.Duration.between(
                        p.getJamMasuk(), 
                        p.getJamPulang()
                    );
                    return duration.toMinutes() > 480; // > 8 hours
                })
                .count();
    }

    /**
     * Get percentage of users who completed checkout.
     * 
     * @param tanggal Date to check
     * @return Percentage (0-100)
     */
    public double getCheckoutCompletionRate(LocalDate tanggal) {
        List<Presensi> presensiList = presensiRepository.findByTanggal(tanggal);
        
        if (presensiList.isEmpty()) {
            return 0.0;
        }
        
        long completed = presensiList.stream()
                .filter(p -> p.getJamPulang() != null)
                .count();
        
        double rate = (completed * 100.0) / presensiList.size();
        return Math.round(rate * 100.0) / 100.0;
    }
}
