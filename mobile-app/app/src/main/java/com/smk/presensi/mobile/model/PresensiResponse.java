package com.smk.presensi.mobile.model;

public class PresensiResponse {
    private Long id;
    private Long userId;
    private String username;
    private TipeUser tipe;
    private String tanggal;
    private String jamMasuk;
    private String jamPulang;
    private StatusPresensi status;
    private MethodPresensi method;
    private Double latitude;
    private Double longitude;
    private String keterangan;

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

    public TipeUser getTipe() {
        return tipe;
    }

    public void setTipe(TipeUser tipe) {
        this.tipe = tipe;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getJamMasuk() {
        return jamMasuk;
    }

    public void setJamMasuk(String jamMasuk) {
        this.jamMasuk = jamMasuk;
    }

    public String getJamPulang() {
        return jamPulang;
    }

    public void setJamPulang(String jamPulang) {
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
