package com.smk.presensi.service;

import com.smk.presensi.dto.koreksi.KoreksiPresensiApprovalRequest;
import com.smk.presensi.dto.koreksi.KoreksiPresensiRequest;
import com.smk.presensi.dto.koreksi.KoreksiPresensiResponse;
import com.smk.presensi.entity.Guru;
import com.smk.presensi.entity.KoreksiPresensi;
import com.smk.presensi.entity.Presensi;
import com.smk.presensi.entity.Siswa;
import com.smk.presensi.entity.User;
import com.smk.presensi.enums.KoreksiStatus;
import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.enums.TipeUser;
import com.smk.presensi.repository.GuruRepository;
import com.smk.presensi.repository.KoreksiPresensiRepository;
import com.smk.presensi.repository.PresensiRepository;
import com.smk.presensi.repository.SiswaRepository;
import com.smk.presensi.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KoreksiPresensiService {

    private final KoreksiPresensiRepository koreksiPresensiRepository;
    private final UserRepository userRepository;
    private final PresensiRepository presensiRepository;
    private final GuruRepository guruRepository;
    private final SiswaRepository siswaRepository;

    public KoreksiPresensiService(KoreksiPresensiRepository koreksiPresensiRepository,
                                  UserRepository userRepository,
                                  PresensiRepository presensiRepository,
                                  GuruRepository guruRepository,
                                  SiswaRepository siswaRepository) {
        this.koreksiPresensiRepository = koreksiPresensiRepository;
        this.userRepository = userRepository;
        this.presensiRepository = presensiRepository;
        this.guruRepository = guruRepository;
        this.siswaRepository = siswaRepository;
    }

    /**
     * Ajukan koreksi presensi.
     * - Default target user = user yang login
     * - Admin/petugas boleh mengajukan untuk user lain (targetUserId)
     */
    @Transactional
    public KoreksiPresensiResponse create(KoreksiPresensiRequest request) {
        User requester = getCurrentUser();
        User targetUser = resolveTargetUser(request, requester);

        KoreksiPresensi koreksi = new KoreksiPresensi();
        koreksi.setTargetUser(targetUser);
        koreksi.setTanggal(request.tanggal());
        koreksi.setJamMasukBaru(request.jamMasukBaru());
        koreksi.setJamPulangBaru(request.jamPulangBaru());
        koreksi.setStatusBaru(request.statusBaru());
        koreksi.setAlasan(request.alasan());
        koreksi.setBuktiUrl(request.buktiUrl());
        koreksi.setStatus(KoreksiStatus.PENDING);
        koreksi.setCreatedAt(LocalDateTime.now());

        if (request.presensiId() != null) {
            Presensi presensi = presensiRepository.findById(request.presensiId())
                    .orElseThrow(() -> new RuntimeException("Presensi dengan ID " + request.presensiId() + " tidak ditemukan"));
            koreksi.setPresensi(presensi);
            koreksi.setTanggal(presensi.getTanggal());
        } else {
            // fallback ke tanggal request jika belum ada record
            koreksi.setTanggal(Optional.ofNullable(request.tanggal()).orElse(LocalDate.now()));
        }

        KoreksiPresensi saved = koreksiPresensiRepository.save(koreksi);
        return toResponse(saved);
    }

    /**
     * Daftar koreksi milik user yang login.
     */
    public List<KoreksiPresensiResponse> findMine() {
        User current = getCurrentUser();
        return koreksiPresensiRepository.findByTargetUser(current).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Daftar koreksi pending untuk petugas/amin.
     */
    public List<KoreksiPresensiResponse> findPending() {
        return koreksiPresensiRepository.findByStatus(KoreksiStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approval/penolakan koreksi.
     */
    @Transactional
    public KoreksiPresensiResponse approve(Long id, KoreksiPresensiApprovalRequest request) {
        KoreksiPresensi koreksi = koreksiPresensiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Koreksi dengan ID " + id + " tidak ditemukan"));

        KoreksiStatus statusBaru = parseStatus(request.status());
        if (statusBaru == KoreksiStatus.PENDING) {
            throw new RuntimeException("Status approval tidak boleh PENDING");
        }

        koreksi.setStatus(statusBaru);
        koreksi.setApprovalNote(request.approvalNote());
        koreksi.setUpdatedAt(LocalDateTime.now());

        User approver = getCurrentUser();
        koreksi.setApprover(approver);

        if (statusBaru == KoreksiStatus.APPROVED) {
            applyToPresensi(koreksi);
        }

        KoreksiPresensi saved = koreksiPresensiRepository.save(koreksi);
        return toResponse(saved);
    }

    /**
     * Daftar semua koreksi dengan filter status (opsional) untuk admin/petugas.
     */
    public List<KoreksiPresensiResponse> findAll(KoreksiStatus status) {
        List<KoreksiPresensi> list;
        if (status != null) {
            list = koreksiPresensiRepository.findByStatus(status);
        } else {
            list = koreksiPresensiRepository.findAll();
        }
        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private KoreksiStatus parseStatus(String status) {
        try {
            return KoreksiStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Status koreksi tidak valid. Gunakan: APPROVED atau REJECTED");
        }
    }

    private User resolveTargetUser(KoreksiPresensiRequest request, User requester) {
        // Jika targetUserId diset, hanya boleh dilakukan oleh role petugas/guru/admin.
        if (request.targetUserId() != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!hasAnyRole(authentication, "ROLE_ADMIN", "ROLE_GURU", "ROLE_GURU_PIKET", "ROLE_GURU_BK", "ROLE_WAKAKURIKULUM")) {
                throw new RuntimeException("Anda tidak berwenang mengajukan koreksi untuk user lain");
            }
            return userRepository.findById(request.targetUserId())
                    .orElseThrow(() -> new RuntimeException("User dengan ID " + request.targetUserId() + " tidak ditemukan"));
        }
        return requester;
    }

    private KoreksiPresensiResponse toResponse(KoreksiPresensi koreksi) {
        User target = koreksi.getTargetUser();
        User approver = koreksi.getApprover();
        Presensi presensi = koreksi.getPresensi();

        return new KoreksiPresensiResponse(
                koreksi.getId(),
                target != null ? target.getId() : null,
                target != null ? target.getUsername() : null,
                presensi != null ? presensi.getId() : null,
                koreksi.getTanggal(),
                koreksi.getJamMasukBaru(),
                koreksi.getJamPulangBaru(),
                koreksi.getStatusBaru(),
                koreksi.getAlasan(),
                koreksi.getBuktiUrl(),
                koreksi.getStatus(),
                koreksi.getApprovalNote(),
                approver != null ? approver.getUsername() : null,
                koreksi.getCreatedAt(),
                koreksi.getUpdatedAt()
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Konteks authentication tidak ditemukan");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User " + authentication.getName() + " tidak ditemukan"));
    }

    private boolean hasAnyRole(Authentication auth, String... roles) {
        if (auth == null) return false;
        for (GrantedAuthority authority : auth.getAuthorities()) {
            for (String role : roles) {
                if (role.equalsIgnoreCase(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Terapkan koreksi ke tabel presensi jika status disetujui.
     */
    private void applyToPresensi(KoreksiPresensi koreksi) {
        User target = koreksi.getTargetUser();
        LocalDate tanggal = koreksi.getTanggal();

        Presensi presensi = resolvePresensi(koreksi);
        boolean isNew = presensi.getId() == null;

        presensi.setUser(target);
        presensi.setTanggal(tanggal);
        presensi.setStatus(koreksi.getStatusBaru());
        presensi.setMethod(MethodPresensi.MANUAL);

        if (presensi.getJamMasuk() == null || koreksi.getJamMasukBaru() != null) {
            presensi.setJamMasuk(koreksi.getJamMasukBaru());
        }
        if (koreksi.getJamPulangBaru() != null) {
            presensi.setJamPulang(koreksi.getJamPulangBaru());
        }

        presensi.setKeterangan(mergeKeterangan(presensi.getKeterangan(), koreksi.getAlasan()));

        if (presensi.getTipe() == null) {
            presensi.setTipe(resolveTipeUser(target));
        }

        Presensi saved = presensiRepository.save(presensi);
        koreksi.setPresensi(saved);

        // Jika presensi baru (tidak ada sebelumnya) set jam masuk default jika masih null supaya tidak invalid.
        if (isNew && saved.getJamMasuk() == null) {
            saved.setJamMasuk(java.time.LocalTime.now());
            presensiRepository.save(saved);
        }
    }

    private Presensi resolvePresensi(KoreksiPresensi koreksi) {
        if (koreksi.getPresensi() != null) {
            Optional<Presensi> fromDb = presensiRepository.findById(koreksi.getPresensi().getId());
            if (fromDb.isPresent()) {
                return fromDb.get();
            }
        }
        return presensiRepository.findByUserAndTanggal(koreksi.getTargetUser(), koreksi.getTanggal())
                .orElseGet(Presensi::new);
    }

    private TipeUser resolveTipeUser(User user) {
        Optional<Siswa> siswa = siswaRepository.findByUser(user);
        if (siswa.isPresent()) {
            return TipeUser.SISWA;
        }
        Optional<Guru> guru = guruRepository.findByUser(user);
        if (guru.isPresent()) {
            return TipeUser.GURU;
        }
        return TipeUser.SISWA;
    }

    private String mergeKeterangan(String existing, String alasan) {
        if (existing == null || existing.isBlank()) {
            return "[KOREKSI] " + alasan;
        }
        return existing + " | [KOREKSI] " + alasan;
    }
}
