package com.smk.presensi.desktop.model;

/**
 * Model untuk soal quiz (desktop side), mencerminkan QuizQuestion backend.
 */
public class QuizQuestion {
    private Long id;
    private Long sessionId;
    private String soal;
    private String opsiA;
    private String opsiB;
    private String opsiC;
    private String opsiD;
    private String jawabanBenar;
    private Integer bobot;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSoal() {
        return soal;
    }

    public void setSoal(String soal) {
        this.soal = soal;
    }

    public String getOpsiA() {
        return opsiA;
    }

    public void setOpsiA(String opsiA) {
        this.opsiA = opsiA;
    }

    public String getOpsiB() {
        return opsiB;
    }

    public void setOpsiB(String opsiB) {
        this.opsiB = opsiB;
    }

    public String getOpsiC() {
        return opsiC;
    }

    public void setOpsiC(String opsiC) {
        this.opsiC = opsiC;
    }

    public String getOpsiD() {
        return opsiD;
    }

    public void setOpsiD(String opsiD) {
        this.opsiD = opsiD;
    }

    public String getJawabanBenar() {
        return jawabanBenar;
    }

    public void setJawabanBenar(String jawabanBenar) {
        this.jawabanBenar = jawabanBenar;
    }

    public Integer getBobot() {
        return bobot;
    }

    public void setBobot(Integer bobot) {
        this.bobot = bobot;
    }
}

