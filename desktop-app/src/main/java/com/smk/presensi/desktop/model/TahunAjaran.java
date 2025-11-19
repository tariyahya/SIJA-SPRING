package com.smk.presensi.desktop.model;

public class TahunAjaran {
    private Long id;
    private String nama;
    private Integer tahunMulai;
    private Integer tahunSelesai;
    private String semester; // "Ganjil" or "Genap"
    private String status; // "AKTIF" or "TIDAK_AKTIF"

    public TahunAjaran() {}

    public TahunAjaran(Long id, String nama, Integer tahunMulai, Integer tahunSelesai, 
                       String semester, String status) {
        this.id = id;
        this.nama = nama;
        this.tahunMulai = tahunMulai;
        this.tahunSelesai = tahunSelesai;
        this.semester = semester;
        this.status = status;
    }

    // Getters & Setters
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

    public Integer getTahunMulai() {
        return tahunMulai;
    }

    public void setTahunMulai(Integer tahunMulai) {
        this.tahunMulai = tahunMulai;
    }

    public Integer getTahunSelesai() {
        return tahunSelesai;
    }

    public void setTahunSelesai(Integer tahunSelesai) {
        this.tahunSelesai = tahunSelesai;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return nama + " - " + semester;
    }
}
