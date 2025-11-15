package com.smk.presensi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "guru")
public class Guru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nip;

    @Column(nullable = false)
    private String nama;

    private String mapel;
    private String rfidCardId;
    private String barcodeId;
    private String faceId;

    // Constructors
    public Guru() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getMapel() { return mapel; }
    public void setMapel(String mapel) { this.mapel = mapel; }

    public String getRfidCardId() { return rfidCardId; }
    public void setRfidCardId(String rfidCardId) { this.rfidCardId = rfidCardId; }

    public String getBarcodeId() { return barcodeId; }
    public void setBarcodeId(String barcodeId) { this.barcodeId = barcodeId; }

    public String getFaceId() { return faceId; }
    public void setFaceId(String faceId) { this.faceId = faceId; }
}
