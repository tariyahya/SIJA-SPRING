package com.smk.presensi.dto;

import java.time.LocalDate;

public class CreateQuizSessionRequest {

    private Long guruId;
    private String judul;
    private Long kelasId;
    private String mapel;
    private String materi;
    private LocalDate tanggal;

    public Long getGuruId() {
        return guruId;
    }

    public void setGuruId(Long guruId) {
        this.guruId = guruId;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public Long getKelasId() {
        return kelasId;
    }

    public void setKelasId(Long kelasId) {
        this.kelasId = kelasId;
    }

    public String getMapel() {
        return mapel;
    }

    public void setMapel(String mapel) {
        this.mapel = mapel;
    }

    public String getMateri() {
        return materi;
    }

    public void setMateri(String materi) {
        this.materi = materi;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }
}
