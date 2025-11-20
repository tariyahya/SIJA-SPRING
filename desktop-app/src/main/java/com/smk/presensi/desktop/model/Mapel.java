package com.smk.presensi.desktop.model;

/**
 * Model sederhana untuk Mata Pelajaran (desktop side).
 * Belum terhubung ke backend; sementara memakai mock/local data.
 */
public class Mapel {
    private Long id;
    private String kode;
    private String nama;
    private String deskripsi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    @Override
    public String toString() {
        return kode + " - " + nama;
    }
}

