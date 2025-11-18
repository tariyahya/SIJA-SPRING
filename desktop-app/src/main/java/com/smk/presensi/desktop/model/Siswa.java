package com.smk.presensi.desktop.model;

import java.time.LocalDateTime;

/**
 * Model class representing a student (Siswa)
 * Matches backend entity for API communication
 */
public class Siswa {
    private Long id;
    private String nis;
    private String nama;
    private String kelas;
    private String jurusan;
    private String rfidCardId;
    private String barcodeId;
    private String faceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Siswa() {
    }
    
    public Siswa(Long id, String nis, String nama, String kelas, String jurusan) {
        this.id = id;
        this.nis = nis;
        this.nama = nama;
        this.kelas = kelas;
        this.jurusan = jurusan;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNis() {
        return nis;
    }
    
    public void setNis(String nis) {
        this.nis = nis;
    }
    
    public String getNama() {
        return nama;
    }
    
    public void setNama(String nama) {
        this.nama = nama;
    }
    
    public String getKelas() {
        return kelas;
    }
    
    public void setKelas(String kelas) {
        this.kelas = kelas;
    }
    
    public String getJurusan() {
        return jurusan;
    }
    
    public void setJurusan(String jurusan) {
        this.jurusan = jurusan;
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
        return "Siswa{" +
                "id=" + id +
                ", nis='" + nis + '\'' +
                ", nama='" + nama + '\'' +
                ", kelas='" + kelas + '\'' +
                ", jurusan='" + jurusan + '\'' +
                '}';
    }
}
