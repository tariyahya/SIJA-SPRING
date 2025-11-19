package com.smk.presensi.service;

import com.smk.presensi.dto.jadwal.JadwalMengajarRequest;
import com.smk.presensi.dto.jadwal.JadwalMengajarResponse;
import com.smk.presensi.entity.Guru;
import com.smk.presensi.entity.JadwalMengajar;
import com.smk.presensi.entity.Kelas;
import com.smk.presensi.repository.GuruRepository;
import com.smk.presensi.repository.JadwalMengajarRepository;
import com.smk.presensi.repository.KelasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JadwalMengajarService {

    private final JadwalMengajarRepository jadwalMengajarRepository;
    private final GuruRepository guruRepository;
    private final KelasRepository kelasRepository;

    public JadwalMengajarService(JadwalMengajarRepository jadwalMengajarRepository,
                                 GuruRepository guruRepository,
                                 KelasRepository kelasRepository) {
        this.jadwalMengajarRepository = jadwalMengajarRepository;
        this.guruRepository = guruRepository;
        this.kelasRepository = kelasRepository;
    }

    @Transactional
    public JadwalMengajarResponse create(JadwalMengajarRequest request) {
        JadwalMengajar jadwal = new JadwalMengajar();
        apply(request, jadwal);
        return toResponse(jadwalMengajarRepository.save(jadwal));
    }

    @Transactional
    public JadwalMengajarResponse update(Long id, JadwalMengajarRequest request) {
        JadwalMengajar jadwal = jadwalMengajarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jadwal ID %d tidak ditemukan".formatted(id)));
        apply(request, jadwal);
        return toResponse(jadwalMengajarRepository.save(jadwal));
    }

    @Transactional(readOnly = true)
    public List<JadwalMengajarResponse> list(Long guruId, Long kelasId, LocalDate tanggal) {
        DayOfWeek hari = tanggal != null ? tanggal.getDayOfWeek() : null;
        List<JadwalMengajar> data;
        if (guruId != null && hari != null) {
            data = jadwalMengajarRepository.findByGuru_IdAndHariAndAktifTrue(guruId, hari);
        } else if (guruId != null) {
            data = jadwalMengajarRepository.findByGuru_IdAndAktifTrue(guruId);
        } else if (kelasId != null) {
            data = jadwalMengajarRepository.findByKelas_IdAndAktifTrue(kelasId);
        } else {
            data = jadwalMengajarRepository.findAll();
        }
        return data.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        jadwalMengajarRepository.deleteById(id);
    }

    private void apply(JadwalMengajarRequest request, JadwalMengajar jadwal) {
        Guru guru = guruRepository.findById(request.guruId())
                .orElseThrow(() -> new RuntimeException("Guru ID %d tidak ditemukan".formatted(request.guruId())));
        Kelas kelas = kelasRepository.findById(request.kelasId())
                .orElseThrow(() -> new RuntimeException("Kelas ID %d tidak ditemukan".formatted(request.kelasId())));
        jadwal.setGuru(guru);
        jadwal.setKelas(kelas);
        jadwal.setMapel(request.mapel());
        jadwal.setHari(request.hari());
        jadwal.setJamMulai(request.jamMulai());
        jadwal.setJamSelesai(request.jamSelesai());
        jadwal.setRuangan(request.ruangan());
        jadwal.setCatatan(request.catatan());
        jadwal.setAktif(request.aktif());
    }

    private JadwalMengajarResponse toResponse(JadwalMengajar jadwal) {
        return new JadwalMengajarResponse(
                jadwal.getId(),
                jadwal.getGuru().getId(),
                jadwal.getGuru().getNama(),
                jadwal.getKelas().getId(),
                jadwal.getKelas().getNama(),
                jadwal.getMapel(),
                jadwal.getHari(),
                jadwal.getJamMulai(),
                jadwal.getJamSelesai(),
                jadwal.getRuangan(),
                jadwal.getCatatan(),
                jadwal.isAktif()
        );
    }
}
