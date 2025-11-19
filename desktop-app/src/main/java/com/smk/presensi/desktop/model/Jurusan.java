package com.smk.presensi.desktop.model;

/**
 * Model class representing a major/department (Jurusan)
 * Matches backend Jurusan entity for API communication
 */
public class Jurusan {
    private Long id;
    private String kode;
    private String nama;
    private Integer durasiTahun;
    private Long ketuaJurusanId;

    public Jurusan() {
    }

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

    public Integer getDurasiTahun() {
        return durasiTahun;
    }

    public void setDurasiTahun(Integer durasiTahun) {
        this.durasiTahun = durasiTahun;
    }

    public Long getKetuaJurusanId() {
        return ketuaJurusanId;
    }

    public void setKetuaJurusanId(Long ketuaJurusanId) {
        this.ketuaJurusanId = ketuaJurusanId;
    }

    @Override
    public String toString() {
        return nama != null ? nama : (kode != null ? kode : "Jurusan");
    }
}

