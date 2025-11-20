package com.smk.presensi.desktop.service;

import com.smk.presensi.desktop.model.Mapel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service mock untuk data Mata Pelajaran (belum ada endpoint backend).
 * Data disimpan di memori selama aplikasi berjalan.
 */
public class MapelService {

    private static final List<Mapel> STORE = new ArrayList<>();
    private static final AtomicLong ID_GEN = new AtomicLong(1);

    static {
        addMock("MAT-01", "Matematika", "Logaritma, Trigonometri, Kalkulus");
        addMock("BIND-01", "Bahasa Indonesia", "Teks Eksposisi, Puisi, Teks Prosedur");
        addMock("PWEB-01", "Pemrograman Web", "HTML, CSS, JavaScript, REST API");
        addMock("BD-01", "Basis Data", "SQL, Normalisasi, ERD");
    }

    private static void addMock(String kode, String nama, String deskripsi) {
        Mapel m = new Mapel();
        m.setId(ID_GEN.getAndIncrement());
        m.setKode(kode);
        m.setNama(nama);
        m.setDeskripsi(deskripsi);
        STORE.add(m);
    }

    public List<Mapel> findAll() {
        return new ArrayList<>(STORE);
    }

    public Mapel create(Mapel mapel) {
        mapel.setId(ID_GEN.getAndIncrement());
        STORE.add(mapel);
        return mapel;
    }

    public Mapel update(Mapel mapel) {
        if (mapel.getId() == null) return null;
        for (int i = 0; i < STORE.size(); i++) {
            if (STORE.get(i).getId().equals(mapel.getId())) {
                STORE.set(i, mapel);
                return mapel;
            }
        }
        return null;
    }

    public boolean delete(Long id) {
        return STORE.removeIf(m -> m.getId().equals(id));
    }
}

