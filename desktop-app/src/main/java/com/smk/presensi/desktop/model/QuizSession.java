package com.smk.presensi.desktop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Model untuk sesi quiz (desktop side), mencerminkan QuizSession backend.
 */
public class QuizSession {
    private Long id;
    private Long guruId;
    private String guruNama;
    private Long kelasId;
    private String kelasNama;
    private String judul;
    private String mapel;
    private String materi;
    private LocalDate tanggal;
    private String token;
    private String qrCodeUrl;
    private String status;
    private List<QuizQuestion> questions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGuruId() {
        return guruId;
    }

    public void setGuruId(Long guruId) {
        this.guruId = guruId;
    }

    public String getGuruNama() {
        return guruNama;
    }

    public void setGuruNama(String guruNama) {
        this.guruNama = guruNama;
    }

    public Long getKelasId() {
        return kelasId;
    }

    public void setKelasId(Long kelasId) {
        this.kelasId = kelasId;
    }

    public String getKelasNama() {
        return kelasNama;
    }

    public void setKelasNama(String kelasNama) {
        this.kelasNama = kelasNama;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }
}

