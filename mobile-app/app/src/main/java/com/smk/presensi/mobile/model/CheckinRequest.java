package com.smk.presensi.mobile.model;

public class CheckinRequest {
    private TipeUser tipe;
    private Double latitude;
    private Double longitude;
    private String keterangan;

    public CheckinRequest(TipeUser tipe, Double latitude, Double longitude, String keterangan) {
        this.tipe = tipe;
        this.latitude = latitude;
        this.longitude = longitude;
        this.keterangan = keterangan;
    }

    public TipeUser getTipe() {
        return tipe;
    }

    public void setTipe(TipeUser tipe) {
        this.tipe = tipe;
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
