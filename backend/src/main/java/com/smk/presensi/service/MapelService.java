package com.smk.presensi.service;

import com.smk.presensi.dto.MapelRequest;
import com.smk.presensi.dto.MapelResponse;
import com.smk.presensi.entity.Mapel;
import com.smk.presensi.repository.MapelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MapelService {

    private final MapelRepository mapelRepository;

    public MapelService(MapelRepository mapelRepository) {
        this.mapelRepository = mapelRepository;
    }

    @Transactional(readOnly = true)
    public List<MapelResponse> findAll() {
        return mapelRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MapelResponse findById(Long id) {
        Mapel mapel = mapelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapel dengan ID %d tidak ditemukan".formatted(id)));
        return toResponse(mapel);
    }

    @Transactional
    public MapelResponse create(MapelRequest request) {
        if (mapelRepository.existsByKodeIgnoreCase(request.kode())) {
            throw new RuntimeException("Kode mapel sudah digunakan: " + request.kode());
        }
        Mapel mapel = new Mapel();
        apply(request, mapel);
        return toResponse(mapelRepository.save(mapel));
    }

    @Transactional
    public MapelResponse update(Long id, MapelRequest request) {
        Mapel mapel = mapelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapel dengan ID %d tidak ditemukan".formatted(id)));

        if (!mapel.getKode().equalsIgnoreCase(request.kode())
                && mapelRepository.existsByKodeIgnoreCase(request.kode())) {
            throw new RuntimeException("Kode mapel sudah digunakan: " + request.kode());
        }

        apply(request, mapel);
        return toResponse(mapelRepository.save(mapel));
    }

    @Transactional
    public void delete(Long id) {
        if (!mapelRepository.existsById(id)) {
            throw new RuntimeException("Mapel dengan ID %d tidak ditemukan".formatted(id));
        }
        mapelRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MapelResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return mapelRepository.findByKodeContainingIgnoreCaseOrNamaContainingIgnoreCase(query, query)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void apply(MapelRequest request, Mapel mapel) {
        mapel.setKode(request.kode());
        mapel.setNama(request.nama());
        mapel.setDeskripsi(request.deskripsi());
    }

    private MapelResponse toResponse(Mapel mapel) {
        return new MapelResponse(
                mapel.getId(),
                mapel.getKode(),
                mapel.getNama(),
                mapel.getDeskripsi()
        );
    }
}

