# TASK-9: Reporting & Analytics

**Tahap**: 9  
**Branch**: `tahap-09-reporting`  
**Tanggal**: 17 November 2025  
**Status**: ✅ SELESAI

---

## 🎯 TUJUAN

Menambahkan fitur reporting dan analytics untuk memantau kehadiran siswa/guru. Admin dan guru dapat melihat:
- **Laporan harian** (per tanggal)
- **Laporan bulanan** (rekapitulasi)
- **Statistik kehadiran** (HADIR%, TERLAMBAT%, ALPHA%)
- **Method usage analytics** (MANUAL vs RFID vs BARCODE vs FACE)

**Use Cases**:
- Admin: Monitor attendance trends
- Guru: View class attendance report
- Analysis: Compare method effectiveness
- Export: CSV/PDF (future enhancement)

---

## 📊 FEATURES

### 1. Laporan Harian (Daily Report)

**Endpoint**: `GET /api/laporan/harian?tanggal=2025-01-17`

**Response**:
```json
{
  "message": "Laporan harian berhasil diambil",
  "data": {
    "tanggal": "2025-01-17",
    "totalPresensi": 100,
    "totalHadir": 75,
    "totalTerlambat": 20,
    "totalAlfa": 5,
    "persentaseHadir": 75.0,
    "persentaseTerlambat": 20.0,
    "persentaseAlfa": 5.0,
    "daftarPresensi": [
      {
        "id": 1,
        "userId": 1,
        "username": "12345",
        "tipe": "SISWA",
        "tanggal": "2025-01-17",
        "jamMasuk": "07:15:00",
        "jamPulang": null,
        "status": "HADIR",
        "method": "RFID",
        "latitude": null,
        "longitude": null,
        "keterangan": null
      },
      ...
    ]
  }
}
```

**Features**:
- Total presensi count
- Status breakdown (HADIR, TERLAMBAT, ALPHA)
- Percentage calculations
- Full presensi list with details

### 2. Laporan Bulanan (Monthly Report)

**Endpoint**: `GET /api/laporan/bulanan?bulan=1&tahun=2025`

**Response**:
```json
{
  "message": "Laporan bulanan berhasil diambil",
  "data": {
    "bulan": 1,
    "tahun": 2025,
    "periodeAwal": "2025-01-01",
    "periodeAkhir": "2025-01-31",
    "totalPresensi": 2000,
    "totalHadir": 1500,
    "totalTerlambat": 400,
    "totalAlfa": 100,
    "persentaseHadir": 75.0,
    "persentaseTerlambat": 20.0,
    "persentaseAlfa": 5.0,
    "totalManual": 800,
    "totalRfid": 600,
    "totalBarcode": 400,
    "totalFace": 200
  }
}
```

**Features**:
- Aggregated statistics for entire month
- Status breakdown with percentages
- Method usage analytics
- Date range (first to last day of month)

### 3. Statistik (Statistics)

**Endpoint**: 
- All-time: `GET /api/laporan/statistik`
- Period: `GET /api/laporan/statistik?start=2025-01-01&end=2025-01-31`

**Response**:
```json
{
  "message": "Statistik keseluruhan berhasil diambil",
  "data": {
    "totalPresensi": 5000,
    "totalHadir": 3750,
    "totalTerlambat": 1000,
    "totalAlfa": 250,
    "persentaseHadir": 75.0,
    "persentaseTerlambat": 20.0,
    "persentaseAlfa": 5.0,
    "totalManual": 2000,
    "totalRfid": 1500,
    "totalBarcode": 1000,
    "totalFace": 500,
    "persentaseManual": 40.0,
    "persentaseRfid": 30.0,
    "persentaseBarcode": 20.0,
    "persentaseFace": 10.0
  }
}
```

**Features**:
- Overall statistics (all-time or specific period)
- Status percentages
- Method usage percentages
- Flexible date filtering

---

## 🗂️ STRUKTUR FILE BARU

### 1. Repository: PresensiRepository (UPDATE)

**File**: `PresensiRepository.java`  
**Changes**: Add query methods for reporting

```java
@Repository
public interface PresensiRepository extends JpaRepository<Presensi, Long> {
    
    // Existing methods...
    
    // ===== TAHAP 9: REPORTING & ANALYTICS =====
    
    /**
     * Get all presensi records within date range.
     */
    List<Presensi> findByTanggalBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Count presensi by status within date range.
     */
    long countByStatusAndTanggalBetween(StatusPresensi status, LocalDate startDate, LocalDate endDate);
    
    /**
     * Count presensi by method within date range.
     */
    long countByMethodAndTanggalBetween(MethodPresensi method, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get all presensi by status within date range.
     */
    List<Presensi> findByStatusAndTanggalBetween(StatusPresensi status, LocalDate startDate, LocalDate endDate);
    
    /**
     * Custom query: Count total records within date range.
     */
    @Query("SELECT COUNT(p) FROM Presensi p WHERE p.tanggal BETWEEN :startDate AND :endDate")
    long countByTanggalBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
```

**Generated SQL Examples**:
```sql
-- findByTanggalBetween
SELECT * FROM presensi 
WHERE tanggal BETWEEN '2025-01-01' AND '2025-01-31';

-- countByStatusAndTanggalBetween
SELECT COUNT(*) FROM presensi 
WHERE status = 'HADIR' AND tanggal BETWEEN '2025-01-01' AND '2025-01-31';

-- countByMethodAndTanggalBetween
SELECT COUNT(*) FROM presensi 
WHERE method = 'RFID' AND tanggal BETWEEN '2025-01-01' AND '2025-01-31';
```

---

### 2. DTOs

#### 2.1. LaporanHarianResponse.java

```java
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
) {}
```

#### 2.2. LaporanBulananResponse.java

```java
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
) {}
```

#### 2.3. StatistikResponse.java

```java
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
) {}
```

---

### 3. Service: LaporanService.java

**Package**: `com.smk.presensi.service`  
**Purpose**: Business logic for report generation

**Key Methods**:

#### 3.1. getLaporanHarian()

```java
public LaporanHarianResponse getLaporanHarian(LocalDate tanggal) {
    // 1. Get all presensi for this date
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
            Math.round(persentaseHadir * 100.0) / 100.0,  // Round to 2 decimals
            Math.round(persentaseTerlambat * 100.0) / 100.0,
            Math.round(persentaseAlfa * 100.0) / 100.0,
            daftarPresensi
    );
}
```

#### 3.2. getLaporanBulanan()

```java
public LaporanBulananResponse getLaporanBulanan(int bulan, int tahun) {
    // 1. Calculate date range
    YearMonth yearMonth = YearMonth.of(tahun, bulan);
    LocalDate periodeAwal = yearMonth.atDay(1);
    LocalDate periodeAkhir = yearMonth.atEndOfMonth();
    
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
```

#### 3.3. getStatistik()

```java
public StatistikResponse getStatistik(LocalDate startDate, LocalDate endDate) {
    // If no date range, use all-time
    if (startDate == null || endDate == null) {
        startDate = LocalDate.of(2000, 1, 1);
        endDate = LocalDate.of(2100, 12, 31);
    }
    
    // Count by status
    long totalHadir = presensiRepository.countByStatusAndTanggalBetween(
            StatusPresensi.HADIR, startDate, endDate);
    
    long totalTerlambat = presensiRepository.countByStatusAndTanggalBetween(
            StatusPresensi.TERLAMBAT, startDate, endDate);
    
    long totalAlfa = presensiRepository.countByStatusAndTanggalBetween(
            StatusPresensi.ALPHA, startDate, endDate);
    
    long totalPresensi = presensiRepository.countByTanggalBetween(startDate, endDate);
    
    // Count by method
    long totalManual = presensiRepository.countByMethodAndTanggalBetween(
            MethodPresensi.MANUAL, startDate, endDate);
    
    long totalRfid = presensiRepository.countByMethodAndTanggalBetween(
            MethodPresensi.RFID, startDate, endDate);
    
    long totalBarcode = presensiRepository.countByMethodAndTanggalBetween(
            MethodPresensi.BARCODE, startDate, endDate);
    
    long totalFace = presensiRepository.countByMethodAndTanggalBetween(
            MethodPresensi.FACE, startDate, endDate);
    
    // Calculate percentages
    double persentaseHadir = totalPresensi > 0 ? (totalHadir * 100.0 / totalPresensi) : 0.0;
    double persentaseTerlambat = totalPresensi > 0 ? (totalTerlambat * 100.0 / totalPresensi) : 0.0;
    double persentaseAlfa = totalPresensi > 0 ? (totalAlfa * 100.0 / totalPresensi) : 0.0;
    
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
```

---

### 4. Controller: LaporanController.java

**Package**: `com.smk.presensi.controller`  
**Base URL**: `/api/laporan`  
**Security**: ADMIN or GURU roles

**Endpoints**:

```java
@RestController
@RequestMapping("/api/laporan")
public class LaporanController {
    
    @Autowired
    private LaporanService laporanService;
    
    /**
     * GET /api/laporan/harian?tanggal=2025-01-17
     * Daily report for specific date (default: today).
     */
    @GetMapping("/harian")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public ResponseEntity<Map<String, Object>> getLaporanHarian(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate tanggal
    ) {
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
     * Monthly report (default: current month).
     */
    @GetMapping("/bulanan")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public ResponseEntity<Map<String, Object>> getLaporanBulanan(
            @RequestParam(required = false) Integer bulan,
            @RequestParam(required = false) Integer tahun
    ) {
        LocalDate now = LocalDate.now();
        if (bulan == null) bulan = now.getMonthValue();
        if (tahun == null) tahun = now.getYear();
        
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
     * Statistics (all-time or specific period).
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
```

---

## 🧪 TESTING

### Test Case 1: Daily Report (Today)

**Request**:
```bash
GET http://localhost:8080/api/laporan/harian
Authorization: Bearer <guru-token>
```

**Expected Response** (200 OK):
```json
{
  "message": "Laporan harian berhasil diambil",
  "data": {
    "tanggal": "2025-11-17",
    "totalPresensi": 50,
    "totalHadir": 40,
    "totalTerlambat": 8,
    "totalAlfa": 2,
    "persentaseHadir": 80.0,
    "persentaseTerlambat": 16.0,
    "persentaseAlfa": 4.0,
    "daftarPresensi": [...]
  }
}
```

### Test Case 2: Monthly Report (January 2025)

**Request**:
```bash
GET http://localhost:8080/api/laporan/bulanan?bulan=1&tahun=2025
Authorization: Bearer <admin-token>
```

**Expected Response** (200 OK):
```json
{
  "message": "Laporan bulanan berhasil diambil",
  "data": {
    "bulan": 1,
    "tahun": 2025,
    "periodeAwal": "2025-01-01",
    "periodeAkhir": "2025-01-31",
    "totalPresensi": 1000,
    "totalHadir": 750,
    "totalTerlambat": 200,
    "totalAlfa": 50,
    "persentaseHadir": 75.0,
    "persentaseTerlambat": 20.0,
    "persentaseAlfa": 5.0,
    "totalManual": 400,
    "totalRfid": 300,
    "totalBarcode": 200,
    "totalFace": 100
  }
}
```

### Test Case 3: All-Time Statistics

**Request**:
```bash
GET http://localhost:8080/api/laporan/statistik
Authorization: Bearer <admin-token>
```

**Expected Response** (200 OK):
```json
{
  "message": "Statistik keseluruhan berhasil diambil",
  "data": {
    "totalPresensi": 5000,
    "totalHadir": 3750,
    "totalTerlambat": 1000,
    "totalAlfa": 250,
    "persentaseHadir": 75.0,
    "persentaseTerlambat": 20.0,
    "persentaseAlfa": 5.0,
    "totalManual": 2000,
    "totalRfid": 1500,
    "totalBarcode": 1000,
    "totalFace": 500,
    "persentaseManual": 40.0,
    "persentaseRfid": 30.0,
    "persentaseBarcode": 20.0,
    "persentaseFace": 10.0
  }
}
```

### Test Case 4: Invalid Month

**Request**:
```bash
GET http://localhost:8080/api/laporan/bulanan?bulan=13&tahun=2025
Authorization: Bearer <admin-token>
```

**Expected Response** (400 Bad Request):
```json
{
  "message": "Bulan harus antara 1-12"
}
```

---

## 📊 STATISTIK

### File Count
- **Total Files**: 62 source files (up from 57 in Tahap 8)
- **New Files**: 5 files
  - LaporanHarianResponse.java (DTO)
  - LaporanBulananResponse.java (DTO)
  - StatistikResponse.java (DTO)
  - LaporanService.java (service)
  - LaporanController.java (controller)
- **Updated Files**: 1 file
  - PresensiRepository.java (add query methods)

### Lines of Code
- **LaporanHarianResponse.java**: ~50 lines
- **LaporanBulananResponse.java**: ~60 lines
- **StatistikResponse.java**: ~60 lines
- **LaporanService.java**: ~280 lines
- **LaporanController.java**: ~160 lines
- **PresensiRepository.java**: +30 lines
- **Total New Code**: ~640 lines

### Build Result
```
[INFO] Compiling 62 source files with javac [debug release 17] to target\classes
[INFO] BUILD SUCCESS
[INFO] Total time:  4.912 s
```

---

## 🎓 LEARNING OUTCOMES

Setelah menyelesaikan Tahap 9, siswa memahami:

1. **Reporting Concepts**:
   - Daily vs monthly vs all-time reports
   - Aggregated statistics
   - Percentage calculations

2. **Query Optimization**:
   - Spring Data JPA query derivation
   - Count queries (efficient)
   - Date range filtering

3. **Business Logic**:
   - Method usage analytics
   - Status breakdown
   - Percentage formulas

4. **API Design**:
   - Optional query parameters
   - Default values (today, current month)
   - Flexible date filtering

5. **Access Control**:
   - Role-based authorization (ADMIN, GURU)
   - Prevent student access to reports

---

## ✅ CHECKLIST

- [x] PresensiRepository query methods added
- [x] LaporanHarianResponse DTO created
- [x] LaporanBulananResponse DTO created
- [x] StatistikResponse DTO created
- [x] LaporanService implemented (3 methods)
- [x] LaporanController created (3 endpoints)
- [x] Build success (62 files compiled)
- [x] Documentation complete (TASK-9.md)

**Status**: ✅ SELESAI

**Next**: Tahap 10 - Export to CSV/PDF (optional) or Mobile App

---

**Author**: Copilot Assistant  
**Last Updated**: 17 November 2025
