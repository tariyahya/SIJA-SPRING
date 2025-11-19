package com.smk.presensi.service;

import com.smk.presensi.dto.pkl.PenempatanPklRequest;
import com.smk.presensi.dto.pkl.PenempatanPklResponse;
import com.smk.presensi.entity.Dudi;
import com.smk.presensi.entity.PenempatanPkl;
import com.smk.presensi.entity.Siswa;
import com.smk.presensi.repository.DudiRepository;
import com.smk.presensi.repository.PenempatanPklRepository;
import com.smk.presensi.repository.SiswaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PenempatanPklService {

    private final PenempatanPklRepository penempatanRepository;
    private final SiswaRepository siswaRepository;
    private final DudiRepository dudiRepository;

    public PenempatanPklService(PenempatanPklRepository penempatanRepository,
                                SiswaRepository siswaRepository,
                                DudiRepository dudiRepository) {
        this.penempatanRepository = penempatanRepository;
        this.siswaRepository = siswaRepository;
        this.dudiRepository = dudiRepository;
    }

    public PenempatanPklResponse create(PenempatanPklRequest request) {
        Siswa siswa = siswaRepository.findById(request.siswaId())
                .orElseThrow(() -> new RuntimeException("Siswa dengan ID " + request.siswaId() + " tidak ditemukan"));

        Dudi dudi = dudiRepository.findById(request.dudiId())
                .orElseThrow(() -> new RuntimeException("DUDI dengan ID " + request.dudiId() + " tidak ditemukan"));

        if (request.tanggalMulai().isAfter(request.tanggalSelesai())) {
            throw new RuntimeException("Tanggal mulai tidak boleh setelah tanggal selesai");
        }

        PenempatanPkl penempatan = new PenempatanPkl();
        penempatan.setSiswa(siswa);
        penempatan.setDudi(dudi);
        penempatan.setTanggalMulai(request.tanggalMulai());
        penempatan.setTanggalSelesai(request.tanggalSelesai());
        penempatan.setKeterangan(request.keterangan());

        PenempatanPkl saved = penempatanRepository.save(penempatan);
        return toResponse(saved);
    }

    public List<PenempatanPklResponse> findByDudi(Long dudiId) {
        return penempatanRepository.findByDudi_Id(dudiId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PenempatanPklResponse> findBySiswa(Long siswaId) {
        return penempatanRepository.findBySiswa_Id(siswaId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PenempatanPklResponse toResponse(PenempatanPkl penempatan) {
        Siswa siswa = penempatan.getSiswa();
        Dudi dudi = penempatan.getDudi();

        return new PenempatanPklResponse(
                penempatan.getId(),
                siswa != null ? siswa.getId() : null,
                siswa != null ? siswa.getNama() : null,
                siswa != null ? siswa.getKelas() : null,
                siswa != null ? siswa.getJurusan() : null,
                dudi != null ? dudi.getId() : null,
                dudi != null ? dudi.getNama() : null,
                penempatan.getTanggalMulai(),
                penempatan.getTanggalSelesai(),
                penempatan.getKeterangan()
        );
    }
}

