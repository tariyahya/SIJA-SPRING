package com.smk.presensi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kelas")
public class Kelas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    private String tingkat;
    private String jurusan;
    private Long waliKelasId;

    // Constructors
    public Kelas() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getTingkat() { return tingkat; }
    public void setTingkat(String tingkat) { this.tingkat = tingkat; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public Long getWaliKelasId() { return waliKelasId; }
    public void setWaliKelasId(Long waliKelasId) { this.waliKelasId = waliKelasId; }
}
