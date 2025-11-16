package com.smk.presensi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO REQUEST untuk Guru - Data yang dikirim dari CLIENT ke SERVER.
 * 
 * Record ini dipakai untuk:
 * - CREATE guru baru (POST /api/guru)
 * - UPDATE guru existing (PUT /api/guru/{id})
 * 
 * Validasi dengan Bean Validation:
 * - @NotBlank: Field tidak boleh null, empty (""), atau hanya whitespace ("   ")
 * 
 * @param nip Nomor Induk Pegawai guru (WAJIB, unique identifier)
 * @param nama Nama lengkap guru (WAJIB)
 * @param mapel Mata pelajaran yang diajarkan (OPSIONAL)
 * @param rfidCardId ID RFID card untuk presensi (OPSIONAL)
 * @param barcodeId ID barcode untuk presensi (OPSIONAL)
 * @param faceId ID face recognition untuk presensi (OPSIONAL)
 */
public record GuruRequest(
        @NotBlank(message = "NIP tidak boleh kosong")
        String nip,

        @NotBlank(message = "Nama tidak boleh kosong")
        String nama,

        String mapel,
        String rfidCardId,
        String barcodeId,
        String faceId
) {
}
