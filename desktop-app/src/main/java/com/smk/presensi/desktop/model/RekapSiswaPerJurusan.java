package com.smk.presensi.desktop.model;

/**
 * Model untuk rekap jumlah siswa per jurusan.
 * Cocok dengan DTO backend RekapSiswaPerJurusanResponse.
 */
public class RekapSiswaPerJurusan {
    private String jurusan;
    private long totalSiswa;

    public RekapSiswaPerJurusan() {
    }

    public RekapSiswaPerJurusan(String jurusan, long totalSiswa) {
        this.jurusan = jurusan;
        this.totalSiswa = totalSiswa;
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

