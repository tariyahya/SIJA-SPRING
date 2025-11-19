package com.smk.presensi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_question")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

    @Column(nullable = false, length = 500)
    private String soal;

    @Column(name = "opsi_a", length = 300)
    private String opsiA;

    @Column(name = "opsi_b", length = 300)
    private String opsiB;

    @Column(name = "opsi_c", length = 300)
    private String opsiC;

    @Column(name = "opsi_d", length = 300)
    private String opsiD;

    @Column(name = "jawaban_benar", length = 5)
    private String jawabanBenar;

    @Column
    private Integer bobot;

    public Long getId() {
        return id;
    }

    public QuizSession getSession() {
        return session;
    }

    public void setSession(QuizSession session) {
        this.session = session;
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
