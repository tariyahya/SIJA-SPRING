package com.smk.presensi.desktop.model;

/**
 * Model untuk rekap jumlah siswa per kelas.
 * Cocok dengan DTO backend RekapSiswaPerKelasResponse.
 */
public class RekapSiswaPerKelas {
    private String kelas;
    private String jurusan;
    private long totalSiswa;

    public RekapSiswaPerKelas() {
    }

    public RekapSiswaPerKelas(String kelas, String jurusan, long totalSiswa) {
        this.kelas = kelas;
        this.jurusan = jurusan;
        this.totalSiswa = totalSiswa;
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

    public long getTotalSiswa() {
        return totalSiswa;
    }

    public void setTotalSiswa(long totalSiswa) {
        this.totalSiswa = totalSiswa;
    }
}

