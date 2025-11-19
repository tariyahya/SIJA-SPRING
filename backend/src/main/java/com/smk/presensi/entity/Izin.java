package com.smk.presensi.entity;

import com.smk.presensi.enums.IzinJenis;
import com.smk.presensi.enums.IzinStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity Izin - pengajuan izin / dispensasi siswa.
 *
 * Contoh use case:
 * - Siswa mengajukan izin sakit 1 hari.
 * - Siswa mengajukan dispensasi lomba 3 hari.
 *
 * Minimal field untuk MVP:
 * - siswa (relasi ke Siswa)
 * - jenis (SAKIT / IZIN / DISPENSASI)
 * - tanggalMulai, tanggalSelesai
 * - alasan
 * - status (PENDING / APPROVED / REJECTED)
 * - approvedBy (opsional, user yang menyetujui)
 */
@Entity
@Table(name = "izin")
public class Izin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "siswa_id", nullable = false)
    private Siswa siswa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IzinJenis jenis;

    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDate tanggalMulai;

    @Column(name = "tanggal_selesai", nullable = false)
    private LocalDate tanggalSelesai;

    @Column(nullable = false, length = 500)
    private String alasan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IzinStatus status = IzinStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(name = "approval_note", length = 500)
    private String approvalNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Siswa getSiswa() {
        return siswa;
    }

    public void setSiswa(Siswa siswa) {
        this.siswa = siswa;
    }

    public IzinJenis getJenis() {
        return jenis;
    }

    public void setJenis(IzinJenis jenis) {
        this.jenis = jenis;
    }

    public LocalDate getTanggalMulai() {
        return tanggalMulai;
    }

    public void setTanggalMulai(LocalDate tanggalMulai) {
        this.tanggalMulai = tanggalMulai;
    }

    public LocalDate getTanggalSelesai() {
        return tanggalSelesai;
    }

    public void setTanggalSelesai(LocalDate tanggalSelesai) {
        this.tanggalSelesai = tanggalSelesai;
    }

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
    }

    public IzinStatus getStatus() {
        return status;
    }

    public void setStatus(IzinStatus status) {
        this.status = status;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovalNote() {
        return approvalNote;
    }

    public void setApprovalNote(String approvalNote) {
        this.approvalNote = approvalNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

