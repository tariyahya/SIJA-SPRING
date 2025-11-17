package com.smk.presensi.dto.presensi;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk request checkin via Barcode/QR Code.
 * 
 * Sama seperti RfidCheckinRequest, hanya ganti field name.
 * 
 * Barcode bisa berupa:
 * - 1D Barcode: CODE128, CODE39, EAN13, dll (contoh: BC123456)
 * - 2D QR Code: QR matrix (contoh: QR_SISWA_001)
 * 
 * Backend tidak peduli format barcode-nya, yang penting:
 * - Unique per user
 * - Tersimpan di database
 * 
 * Use case:
 * 1. Siswa/guru punya ID card dengan barcode printed
 * 2. Scanner/camera baca barcode → dapat barcodeId
 * 3. App kirim POST request dengan barcodeId
 * 4. Backend cari user dengan barcodeId tersebut
 * 5. Auto-checkin jika ditemukan
 */
public record BarcodeCheckinRequest(
    /**
     * Barcode ID - ID unik dari barcode/QR code.
     * 
     * Format: Bebas (tergantung sistem barcode yang dipakai)
     * Contoh 1D: "BC123456", "1234567890123"
     * Contoh QR: "QR_SISWA_001", "GURU_12345"
     * 
     * @NotBlank: Wajib diisi, tidak boleh kosong
     */
    @NotBlank(message = "Barcode ID harus diisi")
    String barcodeId
) {}
