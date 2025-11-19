package com.smk.presensi.service;

import com.smk.presensi.dto.izin.IzinApprovalRequest;
import com.smk.presensi.dto.izin.IzinRequest;
import com.smk.presensi.dto.izin.IzinResponse;
import com.smk.presensi.entity.Izin;
import com.smk.presensi.entity.Siswa;
import com.smk.presensi.entity.User;
import com.smk.presensi.enums.IzinJenis;
import com.smk.presensi.enums.IzinStatus;
import com.smk.presensi.repository.IzinRepository;
import com.smk.presensi.repository.SiswaRepository;
import com.smk.presensi.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service untuk logika bisnis perizinan.
 *
 * MVP:
 * - Pengajuan izin siswa.
 * - Daftar izin pending untuk hari ini.
 * - Approval / reject oleh Guru Piket / Admin.
 */
@Service
public class IzinService {

    private final IzinRepository izinRepository;
    private final SiswaRepository siswaRepository;
    private final UserRepository userRepository;

    public IzinService(IzinRepository izinRepository,
                       SiswaRepository siswaRepository,
                       UserRepository userRepository) {
        this.izinRepository = izinRepository;
        this.siswaRepository = siswaRepository;
        this.userRepository = userRepository;
    }

    /**
     * Pengajuan izin baru oleh siswa/guru.
     */
    public IzinResponse create(IzinRequest request) {
        Siswa siswa = siswaRepository.findById(request.siswaId())
                .orElseThrow(() -> new RuntimeException("Siswa dengan ID " + request.siswaId() + " tidak ditemukan"));

        if (request.tanggalMulai().isAfter(request.tanggalSelesai())) {
            throw new RuntimeException("Tanggal mulai tidak boleh setelah tanggal selesai");
        }

        Izin izin = new Izin();
        izin.setSiswa(siswa);
        izin.setJenis(parseJenis(request.jenis()));
        izin.setTanggalMulai(request.tanggalMulai());
        izin.setTanggalSelesai(request.tanggalSelesai());
        izin.setAlasan(request.alasan());
        izin.setStatus(IzinStatus.PENDING);
        izin.setCreatedAt(LocalDateTime.now());

        Izin saved = izinRepository.save(izin);
        return toResponse(saved);
    }

    /**
     * Daftar izin pending untuk hari ini (untuk dashboard Guru Piket).
     */
    public List<IzinResponse> getPendingToday() {
        LocalDate today = LocalDate.now();
        List<Izin> list = izinRepository
                .findByStatusAndTanggalMulaiLessThanEqualAndTanggalSelesaiGreaterThanEqual(
                        IzinStatus.PENDING,
                        today,
                        today
                );

        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Daftar semua izin (opsional, untuk admin).
     */
    public List<IzinResponse> getAll() {
        return izinRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approval / reject izin oleh Guru Piket / Admin.
     */
    public IzinResponse approve(Long id, IzinApprovalRequest request) {
        Izin izin = izinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Izin dengan ID " + id + " tidak ditemukan"));

        IzinStatus newStatus = parseStatus(request.status());
        if (newStatus == IzinStatus.PENDING) {
            throw new RuntimeException("Status approval tidak boleh PENDING");
        }

        izin.setStatus(newStatus);
        izin.setApprovalNote(request.catatan());
        izin.setUpdatedAt(LocalDateTime.now());

        // Set approver (current logged in user)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            userRepository.findByUsername(authentication.getName())
                    .ifPresent(izin::setApprovedBy);
        }

        Izin saved = izinRepository.save(izin);
        return toResponse(saved);
    }

    private IzinJenis parseJenis(String jenis) {
        try {
            return IzinJenis.valueOf(jenis.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Jenis izin tidak valid. Gunakan: SAKIT, IZIN, atau DISPENSASI");
        }
    }

    private IzinStatus parseStatus(String status) {
        try {
            return IzinStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Status izin tidak valid. Gunakan: APPROVED atau REJECTED");
        }
    }

    private IzinResponse toResponse(Izin izin) {
        Siswa siswa = izin.getSiswa();
        User approver = izin.getApprovedBy();

        return new IzinResponse(
                izin.getId(),
                siswa != null ? siswa.getId() : null,
                siswa != null ? siswa.getNama() : null,
                siswa != null ? siswa.getKelas() : null,
                siswa != null ? siswa.getJurusan() : null,
                izin.getJenis(),
                izin.getTanggalMulai(),
                izin.getTanggalSelesai(),
                izin.getAlasan(),
                izin.getStatus(),
                izin.getApprovalNote(),
                approver != null ? approver.getUsername() : null,
                izin.getCreatedAt(),
                izin.getUpdatedAt()
        );
    }
}

