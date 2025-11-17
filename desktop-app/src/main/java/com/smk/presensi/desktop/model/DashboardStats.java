package com.smk.presensi.desktop.model;

/**
 * Model untuk Dashboard Statistics
 * Menampilkan statistik presensi hari ini
 */
public class DashboardStats {
    private int totalPresensi;
    private int totalHadir;
    private int totalTerlambat;
    private int totalAlpha;
    private double persentaseHadir;

    // Constructors
    public DashboardStats() {}

    public DashboardStats(int totalPresensi, int totalHadir, int totalTerlambat, 
                          int totalAlpha, double persentaseHadir) {
        this.totalPresensi = totalPresensi;
        this.totalHadir = totalHadir;
        this.totalTerlambat = totalTerlambat;
        this.totalAlpha = totalAlpha;
        this.persentaseHadir = persentaseHadir;
    }

    // Getters & Setters
    public int getTotalPresensi() {
        return totalPresensi;
    }

    public void setTotalPresensi(int totalPresensi) {
        this.totalPresensi = totalPresensi;
    }

    public int getTotalHadir() {
        return totalHadir;
    }

    public void setTotalHadir(int totalHadir) {
        this.totalHadir = totalHadir;
    }

    public int getTotalTerlambat() {
        return totalTerlambat;
    }

    public void setTotalTerlambat(int totalTerlambat) {
        this.totalTerlambat = totalTerlambat;
    }

    public int getTotalAlpha() {
        return totalAlpha;
    }

    public void setTotalAlpha(int totalAlpha) {
        this.totalAlpha = totalAlpha;
    }

    public double getPersentaseHadir() {
        return persentaseHadir;
    }

    public void setPersentaseHadir(double persentaseHadir) {
        this.persentaseHadir = persentaseHadir;
    }

    @Override
    public String toString() {
        return "DashboardStats{" +
                "totalPresensi=" + totalPresensi +
                ", totalHadir=" + totalHadir +
                ", persentaseHadir=" + persentaseHadir +
                '}';
    }
}
