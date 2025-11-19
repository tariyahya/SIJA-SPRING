package com.smk.presensi.service;

import com.smk.presensi.dto.KelasRequest;
import com.smk.presensi.dto.KelasResponse;
import com.smk.presensi.entity.Kelas;
import com.smk.presensi.repository.KelasRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KelasService {

    private final KelasRepository kelasRepository;

    public KelasService(KelasRepository kelasRepository) {
        this.kelasRepository = kelasRepository;
    }

    public List<KelasResponse> findAll() {
        return kelasRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public KelasResponse findById(Long id) {
        Kelas kelas = kelasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kelas dengan ID " + id + " tidak ditemukan"));
        return toResponse(kelas);
    }

    public List<KelasResponse> findByJurusan(String jurusan) {
        return kelasRepository.findByJurusan(jurusan)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public KelasResponse create(KelasRequest request) {
        if (kelasRepository.existsByNama(request.nama())) {
            throw new RuntimeException("Nama kelas " + request.nama() + " sudah digunakan");
        }

        Kelas kelas = new Kelas();
        kelas.setNama(request.nama());
        kelas.setTingkat(request.tingkat());
        kelas.setJurusan(request.jurusan());
        kelas.setWaliKelasId(request.waliKelasId());
        kelas.setKapasitas(request.kapasitas());

        Kelas saved = kelasRepository.save(kelas);
        return toResponse(saved);
    }

    public KelasResponse update(Long id, KelasRequest request) {
        Kelas kelas = kelasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kelas dengan ID " + id + " tidak ditemukan"));

        if (!kelas.getNama().equals(request.nama())
                && kelasRepository.existsByNama(request.nama())) {
            throw new RuntimeException("Nama kelas " + request.nama() + " sudah digunakan");
        }

        kelas.setNama(request.nama());
        kelas.setTingkat(request.tingkat());
        kelas.setJurusan(request.jurusan());
        kelas.setWaliKelasId(request.waliKelasId());
        kelas.setKapasitas(request.kapasitas());

        Kelas updated = kelasRepository.save(kelas);
        return toResponse(updated);
    }

    public void delete(Long id) {
        if (!kelasRepository.existsById(id)) {
            throw new RuntimeException("Kelas dengan ID " + id + " tidak ditemukan");
        }
        kelasRepository.deleteById(id);
    }

    private KelasResponse toResponse(Kelas kelas) {
        return new KelasResponse(
                kelas.getId(),
                kelas.getNama(),
                kelas.getTingkat(),
                kelas.getJurusan(),
                kelas.getWaliKelasId(),
                kelas.getKapasitas()
        );
    }
}
