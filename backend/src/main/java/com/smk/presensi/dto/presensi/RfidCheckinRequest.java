package com.smk.presensi.dto.presensi;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk request checkin via RFID.
 * 
 * Sangat sederhana: hanya butuh rfidCardId!
 * 
 * Perbedaan dengan CheckinRequest (manual):
 * - CheckinRequest: butuh tipe (SISWA/GURU), latitude, longitude, keterangan
 * - RfidCheckinRequest: HANYA butuh rfidCardId
 * 
 * Kenapa tidak butuh field lain?
 * - tipe: Auto-detect dari tabel mana user ditemukan (Siswa/Guru)
 * - latitude/longitude: RFID reader fixed di lokasi tertentu (tidak mobile)
 * - keterangan: Tidak perlu (checkin otomatis, tidak ada input manual)
 * 
 * Use case:
 * - RFID reader baca kartu → dapat rfidCardId
 * - Kirim POST request dengan rfidCardId ini
 * - Backend handle sisanya
 */
public record RfidCheckinRequest(
    /**
     * RFID Card ID - ID unik dari kartu RFID.
     * 
     * Format: Bebas (tergantung hardware)
     * Contoh: "RF001234", "1234567890", "A1B2C3D4"
     * 
     * @NotBlank: Wajib diisi, tidak boleh kosong
     */
    @NotBlank(message = "RFID Card ID harus diisi")
    String rfidCardId
) {}
