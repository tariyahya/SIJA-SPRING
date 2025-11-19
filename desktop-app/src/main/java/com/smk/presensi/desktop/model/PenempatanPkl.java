package com.smk.presensi.desktop.model;

import java.time.LocalDate;

/**
 * Model desktop untuk penempatan PKL.
 */
public class PenempatanPkl {
    private Long id;
    private Long siswaId;
    private String siswaNama;
    private String kelas;
    private String jurusan;
    private Long dudiId;
    private String dudiNama;
    private LocalDate tanggalMulai;
    private LocalDate tanggalSelesai;
    private String keterangan;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSiswaId() {
        return siswaId;
    }

    public void setSiswaId(Long siswaId) {
        this.siswaId = siswaId;
    }

    public String getSiswaNama() {
        return siswaNama;
    }

    public void setSiswaNama(String siswaNama) {
        this.siswaNama = siswaNama;
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

    public Long getDudiId() {
        return dudiId;
    }

    public void setDudiId(Long dudiId) {
        this.dudiId = dudiId;
    }

    public String getDudiNama() {
        return dudiNama;
    }

    public void setDudiNama(String dudiNama) {
        this.dudiNama = dudiNama;
    }

    public LocalDate getTanggalMulai() {
        return tanggalMulai;
    }

    public void setTanggalMulai(LocalDate tanggalMulai) {
        this.tanggalMulai = tanggalMulai;
    }

    public LocalDate getTanggalSelesai() {
        return tanggalSelesai;
    }

    public void setTanggalSelesai(LocalDate tanggalSelesai) {
        this.tanggalSelesai = tanggalSelesai;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
}

