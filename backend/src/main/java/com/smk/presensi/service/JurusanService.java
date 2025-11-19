package com.smk.presensi.service;

import com.smk.presensi.dto.JurusanRequest;
import com.smk.presensi.dto.JurusanResponse;
import com.smk.presensi.entity.Jurusan;
import com.smk.presensi.repository.JurusanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JurusanService {

    private final JurusanRepository jurusanRepository;

    public JurusanService(JurusanRepository jurusanRepository) {
        this.jurusanRepository = jurusanRepository;
    }

    public List<JurusanResponse> findAll() {
        return jurusanRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public JurusanResponse findById(Long id) {
        Jurusan jurusan = jurusanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jurusan dengan ID " + id + " tidak ditemukan"));
        return toResponse(jurusan);
    }

    public JurusanResponse create(JurusanRequest request) {
        if (jurusanRepository.existsByKode(request.kode())) {
            throw new RuntimeException("Kode jurusan " + request.kode() + " sudah digunakan");
        }

        Jurusan jurusan = new Jurusan();
        jurusan.setKode(request.kode());
        jurusan.setNama(request.nama());
        jurusan.setDurasiTahun(request.durasiTahun());
        jurusan.setKetuaJurusanId(request.ketuaJurusanId());

        Jurusan saved = jurusanRepository.save(jurusan);
        return toResponse(saved);
    }

    public JurusanResponse update(Long id, JurusanRequest request) {
        Jurusan jurusan = jurusanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jurusan dengan ID " + id + " tidak ditemukan"));

        if (!jurusan.getKode().equals(request.kode())
                && jurusanRepository.existsByKode(request.kode())) {
            throw new RuntimeException("Kode jurusan " + request.kode() + " sudah digunakan");
        }

        jurusan.setKode(request.kode());
        jurusan.setNama(request.nama());
        jurusan.setDurasiTahun(request.durasiTahun());
        jurusan.setKetuaJurusanId(request.ketuaJurusanId());

        Jurusan updated = jurusanRepository.save(jurusan);
        return toResponse(updated);
    }

    public void delete(Long id) {
        if (!jurusanRepository.existsById(id)) {
            throw new RuntimeException("Jurusan dengan ID " + id + " tidak ditemukan");
        }
        jurusanRepository.deleteById(id);
    }

    private JurusanResponse toResponse(Jurusan jurusan) {
        return new JurusanResponse(
                jurusan.getId(),
                jurusan.getKode(),
                jurusan.getNama(),
                jurusan.getDurasiTahun(),
                jurusan.getKetuaJurusanId()
        );
    }
}

