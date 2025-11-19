package com.smk.presensi.service;

import com.smk.presensi.dto.pkl.DudiRequest;
import com.smk.presensi.dto.pkl.DudiResponse;
import com.smk.presensi.entity.Dudi;
import com.smk.presensi.repository.DudiRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DudiService {

    private final DudiRepository dudiRepository;

    public DudiService(DudiRepository dudiRepository) {
        this.dudiRepository = dudiRepository;
    }

    public List<DudiResponse> findAll() {
        return dudiRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DudiResponse findById(Long id) {
        Dudi dudi = dudiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DUDI dengan ID " + id + " tidak ditemukan"));
        return toResponse(dudi);
    }

    public DudiResponse create(DudiRequest request) {
        Dudi dudi = new Dudi();
        dudi.setNama(request.nama());
        dudi.setBidangUsaha(request.bidangUsaha());
        dudi.setAlamat(request.alamat());
        dudi.setContactPerson(request.contactPerson());
        dudi.setContactPhone(request.contactPhone());
        dudi.setKuotaSiswa(request.kuotaSiswa());
        dudi.setAktif(request.aktif() == null || request.aktif());

        Dudi saved = dudiRepository.save(dudi);
        return toResponse(saved);
    }

    public DudiResponse update(Long id, DudiRequest request) {
        Dudi dudi = dudiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DUDI dengan ID " + id + " tidak ditemukan"));

        dudi.setNama(request.nama());
        dudi.setBidangUsaha(request.bidangUsaha());
        dudi.setAlamat(request.alamat());
        dudi.setContactPerson(request.contactPerson());
        dudi.setContactPhone(request.contactPhone());
        dudi.setKuotaSiswa(request.kuotaSiswa());
        if (request.aktif() != null) {
            dudi.setAktif(request.aktif());
        }

        Dudi saved = dudiRepository.save(dudi);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!dudiRepository.existsById(id)) {
            throw new RuntimeException("DUDI dengan ID " + id + " tidak ditemukan");
        }
        dudiRepository.deleteById(id);
    }

    private DudiResponse toResponse(Dudi dudi) {
        return new DudiResponse(
                dudi.getId(),
                dudi.getNama(),
                dudi.getBidangUsaha(),
                dudi.getAlamat(),
                dudi.getContactPerson(),
                dudi.getContactPhone(),
                dudi.getKuotaSiswa(),
                dudi.isAktif()
        );
    }
}

