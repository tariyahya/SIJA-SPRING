package com.smk.presensi.dto.laporan;

/**
 * DTO untuk rekap jumlah siswa per jurusan.
 *
 * Contoh response item:
 * {
 *   "jurusan": "RPL",
 *   "totalSiswa": 128
 * }
 */
public record RekapSiswaPerJurusanResponse(
        String jurusan,
        long totalSiswa
) {
}

