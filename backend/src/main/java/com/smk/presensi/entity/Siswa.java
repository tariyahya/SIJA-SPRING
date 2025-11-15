package com.smk.presensi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "siswa")
public class Siswa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nis;

    @Column(nullable = false)
    private String nama;

    private String kelas;
    private String jurusan;
    private String rfidCardId;
    private String barcodeId;
    private String faceId;

    // Constructors
    public Siswa() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNis() { return nis; }
    public void setNis(String nis) { this.nis = nis; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKelas() { return kelas; }
    public void setKelas(String kelas) { this.kelas = kelas; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public String getRfidCardId() { return rfidCardId; }
    public void setRfidCardId(String rfidCardId) { this.rfidCardId = rfidCardId; }

    public String getBarcodeId() { return barcodeId; }
    public void setBarcodeId(String barcodeId) { this.barcodeId = barcodeId; }

    public String getFaceId() { return faceId; }
    public void setFaceId(String faceId) { this.faceId = faceId; }
}
