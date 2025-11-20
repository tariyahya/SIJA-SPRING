package com.smk.presensi.entity;

import jakarta.persistence.*;

/**
 * Entity untuk Mata Pelajaran.
 */
@Entity
@Table(name = "mapel")
public class Mapel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String kode;

    @Column(nullable = false, length = 150)
    private String nama;

    @Column(length = 255)
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
}
