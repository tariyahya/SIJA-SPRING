package com.smk.presensi.service;

import com.smk.presensi.dto.presensi.AdminPresensiRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.entity.Presensi;
import com.smk.presensi.entity.User;
import com.smk.presensi.entity.Kelas;
import com.smk.presensi.repository.KelasRepository;
import com.smk.presensi.repository.GuruRepository;
import com.smk.presensi.dto.jurnal.GuruJurnalRequest;
import com.smk.presensi.service.GuruJurnalService;
import com.smk.presensi.repository.PresensiRepository;
import com.smk.presensi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer untuk AdminPresensiController.
 * Memungkinkan ADMIN membuat/mengubah/menghapus data presensi secara manual.
 */
@Service
public class AdminPresensiService {

    private final PresensiRepository presensiRepository;
    private final UserRepository userRepository;
    private final KelasRepository kelasRepository;
    private final GuruRepository guruRepository;
    private final GuruJurnalService guruJurnalService;

    public AdminPresensiService(PresensiRepository presensiRepository,
                                UserRepository userRepository,
                                KelasRepository kelasRepository,
                                GuruRepository guruRepository,
                                GuruJurnalService guruJurnalService) {
        this.presensiRepository = presensiRepository;
        this.userRepository = userRepository;
        this.kelasRepository = kelasRepository;
        this.guruRepository = guruRepository;
        this.guruJurnalService = guruJurnalService;
    }

    /**
     * List presensi (optional filter tanggal).
     */
    @Transactional(readOnly = true)
    public List<PresensiResponse> getPresensi(LocalDate tanggal) {
        List<Presensi> list = (tanggal != null)
                ? presensiRepository.findByTanggal(tanggal)
                : presensiRepository.findAll();

        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create presensi manual.
     */
    @Transactional
    public PresensiResponse createPresensi(AdminPresensiRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User ID " + request.userId() + " tidak ditemukan"));

        Presensi presensi = new Presensi();
        applyRequestToEntity(request, presensi, user);

        Presensi saved = presensiRepository.save(presensi);
        maybeCreateGuruJurnal(saved);
        return toResponse(saved);
    }

    /**
     * Update presensi.
     */
    @Transactional
    public PresensiResponse updatePresensi(Long id, AdminPresensiRequest request) {
        Presensi presensi = presensiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presensi ID " + id + " tidak ditemukan"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User ID " + request.userId() + " tidak ditemukan"));

        applyRequestToEntity(request, presensi, user);

        Presensi saved = presensiRepository.save(presensi);
        maybeCreateGuruJurnal(saved);
        return toResponse(saved);
    }

    /**
     * Delete presensi.
     */
    @Transactional
    public void deletePresensi(Long id) {
        if (!presensiRepository.existsById(id)) {
            throw new RuntimeException("Presensi ID " + id + " tidak ditemukan");
        }
        presensiRepository.deleteById(id);
    }

    private void applyRequestToEntity(AdminPresensiRequest request, Presensi presensi, User user) {
        presensi.setUser(user);
        presensi.setTipe(request.tipe());
        presensi.setTanggal(request.tanggal());
        presensi.setJamMasuk(request.jamMasuk());
        presensi.setJamPulang(request.jamPulang());
        presensi.setStatus(request.status());
        presensi.setMethod(request.method());
        presensi.setLatitude(request.latitude());
        presensi.setLongitude(request.longitude());
        presensi.setKeterangan(request.keterangan());

        if (request.kelasId() != null) {
            Kelas kelas = kelasRepository.findById(request.kelasId())
                    .orElseThrow(() -> new RuntimeException("Kelas ID " + request.kelasId() + " tidak ditemukan"));
            presensi.setKelas(kelas);
        } else {
            presensi.setKelas(null);
        }

        presensi.setMapel(request.mapel());
        presensi.setMateri(request.materi());
    }

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
            presensi.getKeterangan(),
            presensi.getKelas() != null ? presensi.getKelas().getId() : null,
            presensi.getKelas() != null ? presensi.getKelas().getNama() : null,
            presensi.getMapel(),
            presensi.getMateri()
        );
    }

    private void maybeCreateGuruJurnal(Presensi presensi) {
        if (presensi == null) return;
        if (presensi.getTipe() == null) return;
        if (presensi.getTipe() != com.smk.presensi.enums.TipeUser.GURU) return;

        try {
            java.util.Optional<com.smk.presensi.entity.Guru> guruOpt = guruRepository.findByUser(presensi.getUser());
            if (guruOpt.isEmpty()) return;
            com.smk.presensi.entity.Guru guru = guruOpt.get();

            Long kelasId = presensi.getKelas() != null ? presensi.getKelas().getId() : null;
            java.time.LocalDate tanggal = presensi.getTanggal() != null ? presensi.getTanggal() : java.time.LocalDate.now();

            GuruJurnalRequest req = new GuruJurnalRequest(
                    guru.getId(),
                    presensi.getId(),
                    kelasId,
                    tanggal,
                    presensi.getMapel(),
                    presensi.getMateri(),
                    presensi.getStatus(),
                    presensi.getKeterangan(),
                    true
            );
            guruJurnalService.create(req);
        } catch (Exception ex) {
            // swallow errors
        }
    }
}

