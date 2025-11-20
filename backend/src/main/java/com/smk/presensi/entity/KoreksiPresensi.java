package com.smk.presensi.entity;

import com.smk.presensi.enums.KoreksiStatus;
import com.smk.presensi.enums.StatusPresensi;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity permintaan koreksi presensi.
 */
@Entity
@Table(name = "koreksi_presensi")
public class KoreksiPresensi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User yang dikoreksi datanya.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    /**
     * Relasi ke presensi yang akan dikoreksi (opsional).
     */
    @ManyToOne
    @JoinColumn(name = "presensi_id")
    private Presensi presensi;

    @Column(nullable = false)
    private LocalDate tanggal;

    @Column(name = "jam_masuk_baru")
    private LocalTime jamMasukBaru;

    @Column(name = "jam_pulang_baru")
    private LocalTime jamPulangBaru;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_baru", length = 20, nullable = false)
    private StatusPresensi statusBaru;

    @Column(nullable = false, length = 500)
    private String alasan;

    @Column(name = "bukti_url", length = 500)
    private String buktiUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KoreksiStatus status = KoreksiStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approver;

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

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public Presensi getPresensi() {
        return presensi;
    }

    public void setPresensi(Presensi presensi) {
        this.presensi = presensi;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public LocalTime getJamMasukBaru() {
        return jamMasukBaru;
    }

    public void setJamMasukBaru(LocalTime jamMasukBaru) {
        this.jamMasukBaru = jamMasukBaru;
    }

    public LocalTime getJamPulangBaru() {
        return jamPulangBaru;
    }

    public void setJamPulangBaru(LocalTime jamPulangBaru) {
        this.jamPulangBaru = jamPulangBaru;
    }

    public StatusPresensi getStatusBaru() {
        return statusBaru;
    }

    public void setStatusBaru(StatusPresensi statusBaru) {
        this.statusBaru = statusBaru;
    }

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
    }

    public String getBuktiUrl() {
        return buktiUrl;
    }

    public void setBuktiUrl(String buktiUrl) {
        this.buktiUrl = buktiUrl;
    }

    public KoreksiStatus getStatus() {
        return status;
    }

    public void setStatus(KoreksiStatus status) {
        this.status = status;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
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
