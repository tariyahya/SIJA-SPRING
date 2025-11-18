package com.smk.presensi.desktop.model;

import java.time.LocalDateTime;

/**
 * Model untuk User (Admin/Guru/Siswa)
 */
public class User {
    private Long id;
    private String username;
    private String nama;
    private String email;
    private String role; // ADMIN, USER
    private String tipe; // GURU, SISWA
    private String rfidCardId;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public User() {}

    public User(Long id, String username, String nama, String email, String role) {
        this.id = id;
        this.username = username;
        this.nama = nama;
        this.email = email;
        this.role = role;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public String getRfidCardId() {
        return rfidCardId;
    }

    public void setRfidCardId(String rfidCardId) {
        this.rfidCardId = rfidCardId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nama='" + nama + '\'' +
                ", role='" + role + '\'' +
                ", tipe='" + tipe + '\'' +
                '}';
    }
}
