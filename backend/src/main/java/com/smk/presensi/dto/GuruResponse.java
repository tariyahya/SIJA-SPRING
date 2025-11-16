package com.smk.presensi.dto;

/**
 * DTO RESPONSE untuk Guru - Data yang dikirim dari SERVER ke CLIENT.
 * 
 * Record ini dipakai untuk:
 * - Response GET all guru (GET /api/guru)
 * - Response GET guru by id (GET /api/guru/{id})
 * - Response CREATE guru (POST /api/guru)
 * - Response UPDATE guru (PUT /api/guru/{id})
 * 
 * Kenapa tidak pakai Entity Guru langsung?
 * - Separation of concerns (pisahkan database layer dari API layer)
 * - Bisa customize field yang dikirim ke client
 * - Lebih aman (tidak expose semua field entity)
 * 
 * @param id Primary key dari database (auto-generated)
 * @param nip Nomor Induk Pegawai
 * @param nama Nama lengkap guru
 * @param mapel Mata pelajaran yang diajarkan
 * @param rfidCardId ID RFID card untuk presensi
 * @param barcodeId ID barcode untuk presensi
 * @param faceId ID face recognition untuk presensi
 */
public record GuruResponse(
        Long id,
        String nip,
        String nama,
        String mapel,
        String rfidCardId,
        String barcodeId,
        String faceId
) {
}
