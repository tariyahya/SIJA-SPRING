package com.smk.presensi.service;

import com.smk.presensi.dto.GuruRequest;
import com.smk.presensi.dto.GuruResponse;
import com.smk.presensi.entity.Guru;
import com.smk.presensi.repository.GuruRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SERVICE LAYER untuk Guru - Lapisan BUSINESS LOGIC.
 * 
 * Sama seperti SiswaService, GuruService bertanggung jawab untuk:
 * - Transformasi DTO ↔ Entity
 * - Validasi business logic
 * - Orchestration (panggil repository, handle error)
 * - Return data dalam bentuk DTO ke Controller
 */
@Service
public class GuruService {

    private final GuruRepository guruRepository;

    public GuruService(GuruRepository guruRepository) {
        this.guruRepository = guruRepository;
    }

    /**
     * Ambil SEMUA guru dari database.
     * Convert Entity → DTO sebelum return ke Controller.
     */
    public List<GuruResponse> findAll() {
        return guruRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Ambil 1 guru by ID.
     * Throw exception jika tidak ditemukan.
     */
    public GuruResponse findById(Long id) {
        Guru guru = guruRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guru dengan ID " + id + " tidak ditemukan"));
        return toResponse(guru);
    }

    /**
     * CREATE guru baru.
     * Convert DTO → Entity, save ke database, convert kembali Entity → DTO.
     */
    public GuruResponse create(GuruRequest request) {
        // Convert DTO → Entity
        Guru guru = new Guru();
        guru.setNip(request.nip());
        guru.setNama(request.nama());
        guru.setMapel(request.mapel());
        guru.setRfidCardId(request.rfidCardId());
        guru.setBarcodeId(request.barcodeId());
        guru.setFaceId(request.faceId());

        // Save ke database
        Guru saved = guruRepository.save(guru);

        // Convert Entity → DTO dan return
        return toResponse(saved);
    }

    /**
     * UPDATE guru existing.
     * Cari guru by ID, update field-nya, save kembali.
     * NIP tidak diupdate (identifier tetap).
     */
    public GuruResponse update(Long id, GuruRequest request) {
        Guru guru = guruRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guru dengan ID " + id + " tidak ditemukan"));

        // Update field (kecuali NIP)
        guru.setNama(request.nama());
        guru.setMapel(request.mapel());
        guru.setRfidCardId(request.rfidCardId());
        guru.setBarcodeId(request.barcodeId());
        guru.setFaceId(request.faceId());

        Guru updated = guruRepository.save(guru);
        return toResponse(updated);
    }

    /**
     * DELETE guru by ID.
     * Throw exception jika tidak ditemukan.
     */
    public void delete(Long id) {
        if (!guruRepository.existsById(id)) {
            throw new RuntimeException("Guru dengan ID " + id + " tidak ditemukan");
        }
        guruRepository.deleteById(id);
    }

    /**
     * HELPER: Convert Entity Guru → DTO GuruResponse.
     */
    private GuruResponse toResponse(Guru guru) {
        return new GuruResponse(
                guru.getId(),
                guru.getNip(),
                guru.getNama(),
                guru.getMapel(),
                guru.getRfidCardId(),
                guru.getBarcodeId(),
                guru.getFaceId()
        );
    }
}
