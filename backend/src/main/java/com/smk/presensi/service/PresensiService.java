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

    // Inject config dari application.properties
    @Value("${presensi.jam-masuk:07:00:00}")
    private String jamMasukConfig;

    @Value("${presensi.toleransi-menit:15}")
    private int toleransiMenit;

    public PresensiService(
            PresensiRepository presensiRepository,
            UserRepository userRepository,
            SiswaRepository siswaRepository,
            GuruRepository guruRepository
    ) {
        this.presensiRepository = presensiRepository;
        this.userRepository = userRepository;
        this.siswaRepository = siswaRepository;
        this.guruRepository = guruRepository;
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

        // 2. Validasi: cek apakah sudah checkin hari ini
        LocalDate today = LocalDate.now();
        if (presensiRepository.existsByUserAndTanggal(user, today)) {
            throw new RuntimeException("Anda sudah checkin hari ini");
        }

        // 3. Buat record presensi baru
        Presensi presensi = new Presensi();
        presensi.setUser(user);
        presensi.setTipe(request.tipe());
        presensi.setTanggal(today);
        presensi.setJamMasuk(LocalTime.now());
        presensi.setMethod(MethodPresensi.MANUAL);
        presensi.setLatitude(request.latitude());
        presensi.setLongitude(request.longitude());
        presensi.setKeterangan(request.keterangan());

        // 4. Hitung status: HADIR atau TERLAMBAT
        presensi.setStatus(hitungStatus(presensi.getJamMasuk()));

        // 5. Save ke database
        Presensi saved = presensiRepository.save(presensi);

        // 6. Convert Entity → DTO
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
}
