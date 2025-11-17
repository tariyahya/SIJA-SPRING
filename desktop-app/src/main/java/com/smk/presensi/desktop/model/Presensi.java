package com.smk.presensi.desktop.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Model untuk data Presensi
 * Merepresentasikan 1 record presensi dari backend API
 */
public class Presensi {
    private Long id;
    private Long userId;
    private String username;
    private String tipe; // SISWA atau GURU
    private LocalDate tanggal;
    private LocalTime jamMasuk;
    private LocalTime jamPulang;
    private String status; // HADIR, TERLAMBAT, ALPHA
    private String method; // MANUAL, RFID, BARCODE, FACE
    private String keterangan;

    // Constructors
    public Presensi() {}

    public Presensi(Long id, Long userId, String username, String tipe, 
                    LocalDate tanggal, LocalTime jamMasuk, LocalTime jamPulang,
                    String status, String method, String keterangan) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.tipe = tipe;
        this.tanggal = tanggal;
        this.jamMasuk = jamMasuk;
        this.jamPulang = jamPulang;
        this.status = status;
        this.method = method;
        this.keterangan = keterangan;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    @Override
    public String toString() {
        return "Presensi{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", tanggal=" + tanggal +
                ", status='" + status + '\'' +
                '}';
    }
}
