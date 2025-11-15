package com.smk.presensi.service;

import com.smk.presensi.dto.SiswaRequest;
import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.entity.Siswa;
import com.smk.presensi.repository.SiswaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiswaService {

    private final SiswaRepository siswaRepository;

    public SiswaService(SiswaRepository siswaRepository) {
        this.siswaRepository = siswaRepository;
    }

    public List<SiswaResponse> findAll() {
        return siswaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SiswaResponse findById(Long id) {
        Siswa siswa = siswaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siswa dengan ID " + id + " tidak ditemukan"));
        return toResponse(siswa);
    }

    public SiswaResponse create(SiswaRequest request) {
        Siswa siswa = new Siswa();
        siswa.setNis(request.nis());
        siswa.setNama(request.nama());
        siswa.setKelas(request.kelas());
        siswa.setJurusan(request.jurusan());
        siswaRepository.save(siswa);
        return toResponse(siswa);
    }

    public SiswaResponse update(Long id, SiswaRequest request) {
        Siswa siswa = siswaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siswa dengan ID " + id + " tidak ditemukan"));
        siswa.setNama(request.nama());
        siswa.setKelas(request.kelas());
        siswa.setJurusan(request.jurusan());
        // NIS tidak diupdate karena unique identifier
        siswaRepository.save(siswa);
        return toResponse(siswa);
    }

    public void delete(Long id) {
        if (!siswaRepository.existsById(id)) {
            throw new RuntimeException("Siswa dengan ID " + id + " tidak ditemukan");
        }
        siswaRepository.deleteById(id);
    }

    public List<SiswaResponse> findByKelas(String kelas) {
        return siswaRepository.findByKelas(kelas).stream()
                .map(this::toResponse)
                .toList();
    }

    private SiswaResponse toResponse(Siswa siswa) {
        return new SiswaResponse(
            siswa.getId(),
            siswa.getNis(),
            siswa.getNama(),
            siswa.getKelas(),
            siswa.getJurusan()
        );
    }
}
