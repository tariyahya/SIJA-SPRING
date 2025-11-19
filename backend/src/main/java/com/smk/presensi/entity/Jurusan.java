package com.smk.presensi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "jurusan")
public class Jurusan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String kode;

    @Column(nullable = false)
    private String nama;

    @Column(name = "durasi_tahun")
    private Integer durasiTahun;

    @Column(name = "ketua_jurusan_id")
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
}

