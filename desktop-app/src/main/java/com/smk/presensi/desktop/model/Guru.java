package com.smk.presensi.desktop.model;

import java.time.LocalDateTime;

/**
 * Model class representing a teacher (Guru)
 * Matches backend entity for API communication
 */
public class Guru {
    private Long id;
    private String nip;
    private String nama;
    private String mapel;
    private String rfidCardId;
    private String barcodeId;
    private String faceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Guru() {
    }
    
    public Guru(Long id, String nip, String nama, String mapel) {
        this.id = id;
        this.nip = nip;
        this.nama = nama;
        this.mapel = mapel;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNip() {
        return nip;
    }
    
    public void setNip(String nip) {
        this.nip = nip;
    }
    
    public String getNama() {
        return nama;
    }
    
    public void setNama(String nama) {
        this.nama = nama;
    }
    
    public String getMapel() {
        return mapel;
    }
    
    public void setMapel(String mapel) {
        this.mapel = mapel;
    }
    
    public String getRfidCardId() {
        return rfidCardId;
    }
    
    public void setRfidCardId(String rfidCardId) {
        this.rfidCardId = rfidCardId;
    }
    
    public String getBarcodeId() {
        return barcodeId;
    }
    
    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }
    
    public String getFaceId() {
        return faceId;
    }
    
    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Guru{" +
                "id=" + id +
                ", nip='" + nip + '\'' +
                ", nama='" + nama + '\'' +
                ", mapel='" + mapel + '\'' +
                '}';
    }
}
