package com.smk.presensi.dto.laporan;

/**
 * DTO untuk rekap jumlah siswa per kelas.
 *
 * Contoh response item:
 * {
 *   "kelas": "XII RPL 1",
 *   "jurusan": "RPL",
 *   "totalSiswa": 32
 * }
 */
public record RekapSiswaPerKelasResponse(
        String kelas,
        String jurusan,
        long totalSiswa
) {
}

