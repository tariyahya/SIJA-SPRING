package com.smk.presensi.controller;

import com.smk.presensi.dto.presensi.BarcodeCheckinRequest;
import com.smk.presensi.dto.presensi.PresensiResponse;
import com.smk.presensi.service.PresensiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER BARCODE - REST API endpoints untuk Barcode/QR Code presensi.
 * 
 * Base URL: /api/presensi/barcode
 * 
 * Pattern sama dengan RfidController:
 * - Public endpoint (no JWT)
 * - Auto-detect user from barcodeId
 * - Method = BARCODE
 * 
 * Barcode vs RFID:
 * - RFID: Tap kartu (radio frequency)
 * - Barcode: Scan visual pattern (camera/scanner)
 * - Keduanya: Identifier untuk auto-checkin
 */
@RestController
@RequestMapping("/api/presensi/barcode")
public class BarcodeController {

    private final PresensiService presensiService;

    public BarcodeController(PresensiService presensiService) {
        this.presensiService = presensiService;
    }

    /**
     * ENDPOINT: POST /api/presensi/barcode/checkin
     * 
     * Checkin via scan barcode/QR code.
     * 
     * Access: PUBLIC (no JWT required)
     */
    @PostMapping("/checkin")
    public ResponseEntity<PresensiResponse> checkinBarcode(@Valid @RequestBody BarcodeCheckinRequest request) {
        PresensiResponse response = presensiService.checkinBarcode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * ENDPOINT: GET /api/presensi/barcode/test
     * 
     * Test endpoint untuk cek apakah Barcode endpoint accessible (public).
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Barcode endpoint is working!");
    }
}
