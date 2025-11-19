package com.smk.presensi.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entity PenempatanPkl - mapping siswa ke DUDI.
 */
@Entity
@Table(name = "penempatan_pkl")
public class PenempatanPkl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "siswa_id", nullable = false)
    private Siswa siswa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dudi_id", nullable = false)
    private Dudi dudi;

    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDate tanggalMulai;

    @Column(name = "tanggal_selesai", nullable = false)
    private LocalDate tanggalSelesai;

    @Column(length = 255)
    private String keterangan;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Siswa getSiswa() {
        return siswa;
    }

    public void setSiswa(Siswa siswa) {
        this.siswa = siswa;
    }

    public Dudi getDudi() {
        return dudi;
    }

    public void setDudi(Dudi dudi) {
        this.dudi = dudi;
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

