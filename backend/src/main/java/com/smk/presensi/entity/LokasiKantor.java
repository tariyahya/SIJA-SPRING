package com.smk.presensi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity untuk menyimpan lokasi kantor/sekolah.
 * Digunakan untuk validasi GPS saat checkin.
 * 
 * Contoh:
 * - Nama: "SMK Example Jakarta"
 * - Latitude: -6.200000 (Jakarta Pusat)
 * - Longitude: 106.816666
 * - Radius: 200 (meter) - user harus dalam radius 200m dari koordinat sekolah
 * 
 * @author Copilot Assistant
 * @since Tahap 8 (Geolocation Validation)
 */
@Entity
@Table(name = "lokasi_kantor")
public class LokasiKantor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nama lokasi (e.g., "SMK Example Jakarta", "Kantor Pusat")
     */
    @Column(nullable = false, length = 255)
    private String nama;
    
    /**
     * Latitude koordinat sekolah/kantor.
     * Range: -90 to +90 (South to North)
     * Contoh: -6.200000 (Jakarta Pusat)
     */
    @Column(nullable = false)
    private Double latitude;
    
    /**
     * Longitude koordinat sekolah/kantor.
     * Range: -180 to +180 (West to East)
     * Contoh: 106.816666 (Jakarta Pusat)
     */
    @Column(nullable = false)
    private Double longitude;
    
    /**
     * Radius validasi dalam METER.
     * User harus checkin dalam radius ini dari koordinat sekolah.
     * 
     * Contoh:
     * - 100 meter: Strict (hanya di dalam gedung)
     * - 200 meter: Moderate (termasuk parkiran/halaman)
     * - 500 meter: Loose (termasuk area sekitar sekolah)
     */
    @Column(name = "radius_validasi", nullable = false)
    private Integer radiusValidasi; // dalam meter
    
    /**
     * Flag untuk menentukan apakah lokasi ini aktif.
     * Hanya 1 lokasi yang boleh aktif pada satu waktu (untuk satu sekolah).
     * 
     * Use case: Jika sekolah pindah gedung, buat lokasi baru dan set yang lama isActive=false.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /**
     * Alamat lengkap (optional, untuk display purposes)
     */
    @Column(length = 500)
    private String alamat;
    
    /**
     * Keterangan tambahan (optional)
     */
    @Column(length = 500)
    private String keterangan;
    
    /**
     * Timestamp pembuatan record
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp update terakhir
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ==================== JPA Callbacks ====================
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== Constructors ====================
    
    public LokasiKantor() {
    }
    
    public LokasiKantor(String nama, Double latitude, Double longitude, Integer radiusValidasi) {
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusValidasi = radiusValidasi;
        this.isActive = true;
    }
    
    // ==================== Getters & Setters ====================
    
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
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Integer getRadiusValidasi() {
        return radiusValidasi;
    }
    
    public void setRadiusValidasi(Integer radiusValidasi) {
        this.radiusValidasi = radiusValidasi;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getAlamat() {
        return alamat;
    }
    
    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
    
    public String getKeterangan() {
        return keterangan;
    }
    
    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
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
    
    // ==================== toString ====================
    
    @Override
    public String toString() {
        return "LokasiKantor{" +
                "id=" + id +
                ", nama='" + nama + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", radiusValidasi=" + radiusValidasi +
                ", isActive=" + isActive +
                '}';
    }
}
