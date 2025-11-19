package com.smk.presensi.desktop.model;

/**
 * Model class representing a class (Kelas)
 * Matches backend Kelas entity for API communication
 */
public class Kelas {
    private Long id;
    private String nama;
    private String tingkat;
    private String jurusan;
    private Long waliKelasId;
    private Integer kapasitas;

    public Kelas() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getTingkat() {
        return tingkat;
    }

    public void setTingkat(String tingkat) {
        this.tingkat = tingkat;
    }

    public String getJurusan() {
        return jurusan;
    }

    public void setJurusan(String jurusan) {
        this.jurusan = jurusan;
    }

    public Long getWaliKelasId() {
        return waliKelasId;
    }

    public void setWaliKelasId(Long waliKelasId) {
        this.waliKelasId = waliKelasId;
    }

    public Integer getKapasitas() {
        return kapasitas;
    }

    public void setKapasitas(Integer kapasitas) {
        this.kapasitas = kapasitas;
    }

    @Override
    public String toString() {
        return nama != null ? nama : "Kelas";
    }
}
