package com.smk.presensi.entity;

import com.smk.presensi.enums.MethodPresensi;
import com.smk.presensi.enums.StatusPresensi;
import com.smk.presensi.enums.TipeUser;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "presensi")
public class Presensi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipeUser tipe;

    @Column(nullable = false)
    private LocalDate tanggal;

    @Column(name = "jam_masuk")
    private LocalTime jamMasuk;

    @Column(name = "jam_pulang")
    private LocalTime jamPulang;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusPresensi status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MethodPresensi method;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 500)
    private String keterangan;

    public Presensi() {
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TipeUser getTipe() {
        return tipe;
    }

    public void setTipe(TipeUser tipe) {
        this.tipe = tipe;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public LocalTime getJamMasuk() {
        return jamMasuk;
    }

    public void setJamMasuk(LocalTime jamMasuk) {
        this.jamMasuk = jamMasuk;
    }

    public LocalTime getJamPulang() {
        return jamPulang;
    }

    public void setJamPulang(LocalTime jamPulang) {
        this.jamPulang = jamPulang;
    }

    public StatusPresensi getStatus() {
        return status;
    }

    public void setStatus(StatusPresensi status) {
        this.status = status;
    }

    public MethodPresensi getMethod() {
        return method;
    }

    public void setMethod(MethodPresensi method) {
        this.method = method;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
}
