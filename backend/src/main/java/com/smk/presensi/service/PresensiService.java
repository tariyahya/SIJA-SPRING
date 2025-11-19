package com.smk.presensi.service;

import com.smk.presensi.dto.presensi.BarcodeCheckinRequest;
import com.smk.presensi.dto.presensi.CheckinRequest;
import com.smk.presensi.dto.presensi.CheckoutRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.dto.presensi.RfidCheckinRequest;
import com.smk.presensi.entity.Guru;
import com.smk.presensi.entity.Presensi;
import com.smk.presensi.entity.Siswa;
import com.smk.presensi.entity.User;
import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.enums.TipeUser;
import com.smk.presensi.repository.GuruRepository;
import com.smk.presensi.repository.PresensiRepository;
import com.smk.presensi.repository.SiswaRepository;
import com.smk.presensi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class PresensiService {

    private final PresensiRepository presensiRepository;
    private final UserRepository userRepository;
    private final SiswaRepository siswaRepository;
    private final GuruRepository guruRepository;
    private final GeolocationService geolocationService;

    // Inject config dari application.properties
    @Value("${presensi.jam-masuk:07:00:00}")
    private String jamMasukConfig;

    @Value("${presensi.toleransi-menit:15}")
    private int toleransiMenit;

    public PresensiService(
            PresensiRepository presensiRepository,
            UserRepository userRepository,
            SiswaRepository siswaRepository,
            GuruRepository guruRepository,
            GeolocationService geolocationService
    ) {
        this.presensiRepository = presensiRepository;
        this.userRepository = userRepository;
        this.siswaRepository = siswaRepository;
        this.guruRepository = guruRepository;
        this.geolocationService = geolocationService;
    }

    /**
     * CHECKIN - User checkin presensi (pagi).
     */
    @Transactional
    public PresensiResponse checkin(CheckinRequest request) {
        // 1. Ambil user yang sedang login
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // 2. Validasi GPS (jika koordinat dikirim)
        if (request.latitude() != null && request.longitude() != null) {
            geolocationService.validateLocation(request.latitude(), request.longitude());
        }

        // 3. Validasi: cek apakah sudah checkin hari ini
        LocalDate today = LocalDate.now();
        if (presensiRepository.existsByUserAndTanggal(user, today)) {
            throw new RuntimeException("Anda sudah checkin hari ini");
        }

        // 4. Buat record presensi baru
        Presensi presensi = new Presensi();
        presensi.setUser(user);
        presensi.setTipe(request.tipe());
        presensi.setTanggal(today);
        presensi.setJamMasuk(LocalTime.now());
        presensi.setMethod(MethodPresensi.MANUAL);
        presensi.setLatitude(request.latitude());
        presensi.setLongitude(request.longitude());
        presensi.setKeterangan(request.keterangan());

        // 5. Hitung status: HADIR atau TERLAMBAT
        presensi.setStatus(hitungStatus(presensi.getJamMasuk()));

        // 6. Save ke database
        Presensi saved = presensiRepository.save(presensi);

        // 7. Convert Entity → DTO
        return toResponse(saved);
    }

    /**
     * CHECKOUT - User checkout presensi (sore).
     */
    @Transactional
    public PresensiResponse checkout(CheckoutRequest request) {
        // 1. Ambil user yang sedang login
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // 2. Cari record presensi hari ini
        LocalDate today = LocalDate.now();
        Presensi presensi = presensiRepository.findByUserAndTanggal(user, today)
                .orElseThrow(() -> new RuntimeException("Anda belum checkin hari ini"));

        // 3. Validasi: sudah checkout atau belum
        if (presensi.getJamPulang() != null) {
            throw new RuntimeException("Anda sudah checkout hari ini");
        }

        // 4. Update jam pulang
        presensi.setJamPulang(LocalTime.now());
        if (request.latitude() != null) {
            presensi.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            presensi.setLongitude(request.longitude());
        }
        if (request.keterangan() != null && !request.keterangan().isEmpty()) {
            presensi.setKeterangan(presensi.getKeterangan() + " | Checkout: " + request.keterangan());
        }

        // 5. Save update
        Presensi updated = presensiRepository.save(presensi);

        // 6. Convert Entity → DTO
        return toResponse(updated);
    }

    /**
     * GET HISTORI - Ambil history presensi user yang sedang login.
     */
    public List<PresensiResponse> getHistori(LocalDate startDate, LocalDate endDate) {
        // 1. Ambil user yang sedang login
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // 2. Query presensi user dalam range
        List<Presensi> presensiList;
        if (startDate != null && endDate != null) {
            presensiList = presensiRepository.findByUserAndTanggalBetweenOrderByTanggalDesc(user, startDate, endDate);
        } else {
            presensiList = presensiRepository.findByUser(user);
        }

        // 3. Convert List<Entity> → List<DTO>
        return presensiList.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * GET ALL PRESENSI - Untuk ADMIN atau GURU (lihat semua presensi).
     */
    public List<PresensiResponse> getAllPresensi(LocalDate tanggal) {
        List<Presensi> presensiList;
        if (tanggal != null) {
            presensiList = presensiRepository.findByTanggal(tanggal);
        } else {
            presensiList = presensiRepository.findAll();
        }

        return presensiList.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get all presensi data.
     */
    public List<PresensiResponse> findAll() {
        return presensiRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * HELPER: Hitung status berdasarkan jam masuk.
     */
    private StatusPresensi hitungStatus(LocalTime jamMasuk) {
        // Parse jam masuk config: "07:00:00"
        LocalTime batasWaktu = LocalTime.parse(jamMasukConfig).plusMinutes(toleransiMenit);

        // Jika masuk sebelum/pas batas waktu → HADIR
        // Jika masuk setelah batas waktu → TERLAMBAT
        if (jamMasuk.isBefore(batasWaktu) || jamMasuk.equals(batasWaktu)) {
            return StatusPresensi.HADIR;
        } else {
            return StatusPresensi.TERLAMBAT;
        }
    }

    /**
     * HELPER: Convert Entity Presensi → DTO PresensiResponse.
     */
    private PresensiResponse toResponse(Presensi presensi) {
        return new PresensiResponse(
                presensi.getId(),
                presensi.getUser().getId(),
                presensi.getUser().getUsername(),
                presensi.getTipe(),
                presensi.getTanggal(),
                presensi.getJamMasuk(),
                presensi.getJamPulang(),
                presensi.getStatus(),
                presensi.getMethod(),
                presensi.getLatitude(),
                presensi.getLongitude(),
                presensi.getKeterangan()
        );
    }

    /**
     * CHECKIN RFID - Checkin via tap kartu RFID.
     * 
     * Perbedaan dengan checkin() manual:
     * - Tidak perlu authentication (no JWT)
     * - Tidak ambil user dari SecurityContext
     * - Cari user berdasarkan rfidCardId
     * - Auto-detect tipe (SISWA/GURU) dari tabel yang ditemukan
     */
    @Transactional
    public PresensiResponse checkinRfid(RfidCheckinRequest request) {
        String rfidCardId = request.rfidCardId();
        
        // 1. Cari user berdasarkan rfidCardId
        User user = null;
        TipeUser tipe = null;
        
        // Cari di tabel Siswa dulu
        Optional<Siswa> siswaOpt = siswaRepository.findByRfidCardId(rfidCardId);
        if (siswaOpt.isPresent()) {
            Siswa siswa = siswaOpt.get();
            user = siswa.getUser(); // Ambil User dari relasi OneToOne
            tipe = TipeUser.SISWA;
        } else {
            // Jika tidak ada di Siswa, cari di tabel Guru
            Optional<Guru> guruOpt = guruRepository.findByRfidCardId(rfidCardId);
            if (guruOpt.isPresent()) {
                Guru guru = guruOpt.get();
                user = guru.getUser(); // Ambil User dari relasi OneToOne
                tipe = TipeUser.GURU;
            }
        }
        
        // Jika tidak ketemu di Siswa maupun Guru
        if (user == null) {
            throw new RuntimeException("Kartu RFID tidak terdaftar: " + rfidCardId);
        }
        
        // 2. Validasi duplikasi
        LocalDate today = LocalDate.now();
        if (presensiRepository.existsByUserAndTanggal(user, today)) {
            throw new RuntimeException("User dengan kartu " + rfidCardId + " sudah checkin hari ini");
        }
        
        // 3. Buat record presensi baru
        Presensi presensi = new Presensi();
        presensi.setUser(user);
        presensi.setTipe(tipe); // SISWA atau GURU (auto-detect)
        presensi.setTanggal(today);
        
        LocalTime now = LocalTime.now();
        presensi.setJamMasuk(now);
        
        // 4. Hitung status (HADIR/TERLAMBAT)
        presensi.setStatus(hitungStatus(now));
        
        // 5. Set method = RFID (bukan MANUAL)
        presensi.setMethod(MethodPresensi.RFID);
        
        // GPS tidak ada (RFID reader fixed location)
        presensi.setLatitude(null);
        presensi.setLongitude(null);
        
        // Keterangan otomatis
        presensi.setKeterangan("Checkin via RFID: " + rfidCardId);
        
        // 6. Save
        Presensi saved = presensiRepository.save(presensi);
        
        // 7. Convert ke DTO
        return toResponse(saved);
    }

    /**
     * CHECKIN VIA BARCODE - untuk barcode reader/smartphone scanner.
     * 
     * Flow:
     * 1. Terima barcodeId dari scanner
     * 2. Cari user berdasarkan barcodeId (cek Siswa dulu, lalu Guru)
     * 3. Auto-detect tipe user (SISWA/GURU)
     * 4. Validasi duplikasi (sudah checkin hari ini?)
     * 5. Buat record presensi dengan method = BARCODE
     * 6. Hitung status (HADIR/TERLAMBAT)
     * 7. Return response
     * 
     * Perbedaan dengan checkin() manual:
     * - Tidak perlu authentication (no JWT)
     * - Tidak ambil user dari SecurityContext
     * - Cari user berdasarkan barcodeId
     * - Auto-detect tipe (SISWA/GURU) dari tabel yang ditemukan
     * 
     * Perbedaan dengan checkinRfid():
     * - Cari by barcodeId (bukan rfidCardId)
     * - Method = BARCODE (bukan RFID)
     * - Keterangan include barcodeId (bukan rfidCardId)
     */
    @Transactional
    public PresensiResponse checkinBarcode(BarcodeCheckinRequest request) {
        String barcodeId = request.barcodeId();
        
        // 1. Cari user berdasarkan barcodeId
        User user = null;
        TipeUser tipe = null;
        
        // Cari di tabel Siswa dulu
        Optional<Siswa> siswaOpt = siswaRepository.findByBarcodeId(barcodeId);
        if (siswaOpt.isPresent()) {
            Siswa siswa = siswaOpt.get();
            user = siswa.getUser(); // Ambil User dari relasi OneToOne
            tipe = TipeUser.SISWA;
        } else {
            // Jika tidak ada di Siswa, cari di tabel Guru
            Optional<Guru> guruOpt = guruRepository.findByBarcodeId(barcodeId);
            if (guruOpt.isPresent()) {
                Guru guru = guruOpt.get();
                user = guru.getUser(); // Ambil User dari relasi OneToOne
                tipe = TipeUser.GURU;
            }
        }
        
        // Jika tidak ketemu di Siswa maupun Guru
        if (user == null) {
            throw new RuntimeException("Barcode tidak terdaftar: " + barcodeId);
        }
        
        // 2. Validasi duplikasi
        LocalDate today = LocalDate.now();
        if (presensiRepository.existsByUserAndTanggal(user, today)) {
            throw new RuntimeException("User dengan barcode " + barcodeId + " sudah checkin hari ini");
        }
        
        // 3. Buat record presensi baru
        Presensi presensi = new Presensi();
        presensi.setUser(user);
        presensi.setTipe(tipe); // SISWA atau GURU (auto-detect)
        presensi.setTanggal(today);
        
        LocalTime now = LocalTime.now();
        presensi.setJamMasuk(now);
        
        // 4. Hitung status (HADIR/TERLAMBAT)
        presensi.setStatus(hitungStatus(now));
        
        // 5. Set method = BARCODE (bukan MANUAL/RFID)
        presensi.setMethod(MethodPresensi.BARCODE);
        
        // GPS tidak ada (Barcode reader fixed location)
        presensi.setLatitude(null);
        presensi.setLongitude(null);
        
        // Keterangan otomatis
        presensi.setKeterangan("Checkin via Barcode: " + barcodeId);
        
        // 6. Save
        Presensi saved = presensiRepository.save(presensi);
        
        // 7. Convert ke DTO
        return toResponse(saved);
    }

    /**
     * CHECKIN VIA FACE RECOGNITION - untuk face recognition camera.
     * 
     * Flow:
     * 1. Terima Siswa atau Guru object (sudah diidentifikasi di FaceController)
     * 2. Get User dari Siswa/Guru (OneToOne relation)
     * 3. Auto-detect tipe (SISWA/GURU)
     * 4. Validasi duplikasi (sudah checkin hari ini?)
     * 5. Buat record presensi dengan method = FACE
     * 6. Hitung status (HADIR/TERLAMBAT)
     * 7. Return response
     * 
     * Perbedaan dengan checkin() manual:
     * - Tidak perlu authentication (no JWT)
     * - Tidak ambil user dari SecurityContext
     * - User sudah diidentifikasi via face matching di controller
     * - Auto-detect tipe dari object type (Siswa vs Guru)
     * 
     * Perbedaan dengan checkinRfid/Barcode:
     * - Input: Siswa/Guru object (bukan ID string)
     * - Face matching sudah dilakukan sebelumnya
     * - Method = FACE (bukan RFID/BARCODE)
     * 
     * @param siswa Siswa object jika yang checkin siswa
     * @return PresensiResponse
     */
    @Transactional
    public PresensiResponse checkinFace(Siswa siswa) {
        // 1. Get User dan tipe
        User user = siswa.getUser();
        TipeUser tipe = TipeUser.SISWA;
        
        // 2. Validasi duplikasi
        LocalDate today = LocalDate.now();
        if (presensiRepository.existsByUserAndTanggal(user, today)) {
            throw new RuntimeException("Siswa " + user.getUsername() + " sudah checkin hari ini");
        }
        
        // 3. Buat record presensi baru
        Presensi presensi = new Presensi();
        presensi.setUser(user);
        presensi.setTipe(tipe);
        presensi.setTanggal(today);
        
        LocalTime now = LocalTime.now();
        presensi.setJamMasuk(now);
        
        // 4. Hitung status (HADIR/TERLAMBAT)
        presensi.setStatus(hitungStatus(now));
        
        // 5. Set method = FACE (bukan MANUAL/RFID/BARCODE)
        presensi.setMethod(MethodPresensi.FACE);
        
        // GPS tidak ada (Face camera fixed location)
        presensi.setLatitude(null);
        presensi.setLongitude(null);
        
        // Keterangan otomatis
        presensi.setKeterangan("Checkin via Face Recognition: " + user.getUsername());
        
        // 6. Save
        Presensi saved = presensiRepository.save(presensi);
        
        // 7. Convert ke DTO
        return toResponse(saved);
    }

    /**
     * CHECKIN VIA FACE RECOGNITION - overload untuk Guru.
     * 
     * @param guru Guru object jika yang checkin guru
     * @return PresensiResponse
     */
    @Transactional
    public PresensiResponse checkinFace(Guru guru) {
        // 1. Get User dan tipe
        User user = guru.getUser();
        TipeUser tipe = TipeUser.GURU;
        
        // 2. Validasi duplikasi
        LocalDate today = LocalDate.now();
        if (presensiRepository.existsByUserAndTanggal(user, today)) {
            throw new RuntimeException("Guru " + user.getUsername() + " sudah checkin hari ini");
        }
        
        // 3. Buat record presensi baru
        Presensi presensi = new Presensi();
        presensi.setUser(user);
        presensi.setTipe(tipe);
        presensi.setTanggal(today);
        
        LocalTime now = LocalTime.now();
        presensi.setJamMasuk(now);
        
        // 4. Hitung status (HADIR/TERLAMBAT)
        presensi.setStatus(hitungStatus(now));
        
        // 5. Set method = FACE (bukan MANUAL/RFID/BARCODE)
        presensi.setMethod(MethodPresensi.FACE);
        
        // GPS tidak ada (Face camera fixed location)
        presensi.setLatitude(null);
        presensi.setLongitude(null);
        
        // Keterangan otomatis
        presensi.setKeterangan("Checkin via Face Recognition: " + user.getUsername());
        
        // 6. Save
        Presensi saved = presensiRepository.save(presensi);
        
        // 7. Convert ke DTO
        return toResponse(saved);
    }

    // ===== TAHAP 10: CHECKOUT METHODS =====

    /**
     * CHECKOUT VIA RFID - User checkout menggunakan tap kartu RFID.
     * 
     * Flow:
     * 1. Cari user berdasarkan rfidCardId
     * 2. Cari presensi hari ini
     * 3. Validasi: sudah checkin? belum checkout?
     * 4. Update jam pulang
     * 5. Return response dengan work hours
     */
    @Transactional
    public PresensiResponse checkoutRfid(String rfidCardId) {
        // 1. Cari user berdasarkan rfidCardId
        User user = null;
        Optional<Siswa> siswaOpt = siswaRepository.findByRfidCardId(rfidCardId);
        if (siswaOpt.isPresent()) {
            user = siswaOpt.get().getUser();
        } else {
            Optional<Guru> guruOpt = guruRepository.findByRfidCardId(rfidCardId);
            if (guruOpt.isPresent()) {
                user = guruOpt.get().getUser();
            }
        }
        
        if (user == null) {
            throw new RuntimeException("Kartu RFID tidak terdaftar: " + rfidCardId);
        }
        
        // 2. Cari presensi hari ini
        LocalDate today = LocalDate.now();
        Presensi presensi = presensiRepository.findByUserAndTanggal(user, today)
                .orElseThrow(() -> new RuntimeException("User dengan kartu " + rfidCardId + " belum checkin hari ini"));
        
        // 3. Validasi: sudah checkout?
        if (presensi.getJamPulang() != null) {
            throw new RuntimeException("User dengan kartu " + rfidCardId + " sudah checkout hari ini");
        }
        
        // 4. Update jam pulang
        presensi.setJamPulang(LocalTime.now());
        String existingKeterangan = presensi.getKeterangan();
        presensi.setKeterangan(existingKeterangan + " | Checkout via RFID: " + rfidCardId);
        
        // 5. Save dan return
        Presensi updated = presensiRepository.save(presensi);
        return toResponse(updated);
    }

    /**
     * CHECKOUT VIA BARCODE - User checkout menggunakan scan barcode.
     */
    @Transactional
    public PresensiResponse checkoutBarcode(String barcodeId) {
        // 1. Cari user berdasarkan barcodeId
        User user = null;
        Optional<Siswa> siswaOpt = siswaRepository.findByBarcodeId(barcodeId);
        if (siswaOpt.isPresent()) {
            user = siswaOpt.get().getUser();
        } else {
            Optional<Guru> guruOpt = guruRepository.findByBarcodeId(barcodeId);
            if (guruOpt.isPresent()) {
                user = guruOpt.get().getUser();
            }
        }
        
        if (user == null) {
            throw new RuntimeException("Barcode tidak terdaftar: " + barcodeId);
        }
        
        // 2. Cari presensi hari ini
        LocalDate today = LocalDate.now();
        Presensi presensi = presensiRepository.findByUserAndTanggal(user, today)
                .orElseThrow(() -> new RuntimeException("User dengan barcode " + barcodeId + " belum checkin hari ini"));
        
        // 3. Validasi: sudah checkout?
        if (presensi.getJamPulang() != null) {
            throw new RuntimeException("User dengan barcode " + barcodeId + " sudah checkout hari ini");
        }
        
        // 4. Update jam pulang
        presensi.setJamPulang(LocalTime.now());
        String existingKeterangan = presensi.getKeterangan();
        presensi.setKeterangan(existingKeterangan + " | Checkout via Barcode: " + barcodeId);
        
        // 5. Save dan return
        Presensi updated = presensiRepository.save(presensi);
        return toResponse(updated);
    }

    /**
     * CHECKOUT VIA FACE RECOGNITION - User checkout menggunakan face recognition.
     */
    @Transactional
    public PresensiResponse checkoutFace(Siswa siswa) {
        User user = siswa.getUser();
        
        // Cari presensi hari ini
        LocalDate today = LocalDate.now();
        Presensi presensi = presensiRepository.findByUserAndTanggal(user, today)
                .orElseThrow(() -> new RuntimeException("Siswa " + user.getUsername() + " belum checkin hari ini"));
        
        // Validasi: sudah checkout?
        if (presensi.getJamPulang() != null) {
            throw new RuntimeException("Siswa " + user.getUsername() + " sudah checkout hari ini");
        }
        
        // Update jam pulang
        presensi.setJamPulang(LocalTime.now());
        String existingKeterangan = presensi.getKeterangan();
        presensi.setKeterangan(existingKeterangan + " | Checkout via Face Recognition: " + user.getUsername());
        
        // Save dan return
        Presensi updated = presensiRepository.save(presensi);
        return toResponse(updated);
    }

    /**
     * CHECKOUT VIA FACE RECOGNITION - overload untuk Guru.
     */
    @Transactional
    public PresensiResponse checkoutFace(Guru guru) {
        User user = guru.getUser();
        
        // Cari presensi hari ini
        LocalDate today = LocalDate.now();
        Presensi presensi = presensiRepository.findByUserAndTanggal(user, today)
                .orElseThrow(() -> new RuntimeException("Guru " + user.getUsername() + " belum checkin hari ini"));
        
        // Validasi: sudah checkout?
        if (presensi.getJamPulang() != null) {
            throw new RuntimeException("Guru " + user.getUsername() + " sudah checkout hari ini");
        }
        
        // Update jam pulang
        presensi.setJamPulang(LocalTime.now());
        String existingKeterangan = presensi.getKeterangan();
        presensi.setKeterangan(existingKeterangan + " | Checkout via Face Recognition: " + user.getUsername());
        
        // Save dan return
        Presensi updated = presensiRepository.save(presensi);
        return toResponse(updated);
    }

    /**
     * GET PRESENSI BY ID - Untuk mendapatkan presensi berdasarkan ID.
     */
    public Presensi getPresensiById(Long id) {
        return presensiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presensi dengan ID " + id + " tidak ditemukan"));
    }

    /**
     * CALCULATE WORK HOURS - Hitung total jam kerja dari checkin ke checkout.
     * 
     * @param presensi Presensi yang sudah ada jamMasuk dan jamPulang
     * @return WorkHoursResponse dengan detail jam kerja
     */
    public com.smk.presensi.dto.WorkHoursResponse calculateWorkHours(Presensi presensi) {
        if (presensi.getJamMasuk() == null || presensi.getJamPulang() == null) {
            throw new RuntimeException("Data jam masuk atau jam pulang belum lengkap");
        }
        
        // Hitung durasi
        java.time.Duration duration = java.time.Duration.between(
            presensi.getJamMasuk(), 
            presensi.getJamPulang()
        );
        
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        
        // Check overtime (lebih dari 8 jam = 480 menit)
        boolean isOvertime = totalMinutes > 480;
        
        return new com.smk.presensi.dto.WorkHoursResponse(
            presensi.getId(),
            presensi.getUser().getUsername(),
            presensi.getTipe().name(),
            presensi.getTanggal().toString(),
            presensi.getJamMasuk().toString(),
            presensi.getJamPulang().toString(),
            totalMinutes,
            hours,
            minutes,
            isOvertime,
            presensi.getStatus().name()
        );
    }
}
