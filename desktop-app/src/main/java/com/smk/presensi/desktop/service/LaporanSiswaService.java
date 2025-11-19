package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.RekapSiswaPerJurusan;
import com.smk.presensi.desktop.model.RekapSiswaPerKelas;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service untuk memanggil endpoint laporan rekap siswa
 * (per kelas dan per jurusan).
 */
public class LaporanSiswaService {

    private final ApiClient apiClient;
    private final Gson gson;

    public LaporanSiswaService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new Gson();
    }

    /**
     * Ambil rekap siswa per kelas dari backend.
     */
    public List<RekapSiswaPerKelas> getRekapPerKelas() throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/laporan/rekap-siswa/kelas");

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch rekap siswa per kelas: " + response.statusCode());
        }

        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response.body(), type);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) map.get("data");
        List<RekapSiswaPerKelas> result = new ArrayList<>();

        if (dataList != null) {
            for (Map<String, Object> item : dataList) {
                String kelas = (String) item.get("kelas");
                String jurusan = (String) item.get("jurusan");
                Number total = (Number) item.get("totalSiswa");
                long totalSiswa = total != null ? total.longValue() : 0L;

                result.add(new RekapSiswaPerKelas(kelas, jurusan, totalSiswa));
            }
        }

        return result;
    }

    /**
     * Ambil rekap siswa per jurusan dari backend.
     */
    public List<RekapSiswaPerJurusan> getRekapPerJurusan() throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/laporan/rekap-siswa/jurusan");

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch rekap siswa per jurusan: " + response.statusCode());
        }

        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response.body(), type);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) map.get("data");
        List<RekapSiswaPerJurusan> result = new ArrayList<>();

        if (dataList != null) {
            for (Map<String, Object> item : dataList) {
                String jurusan = (String) item.get("jurusan");
                Number total = (Number) item.get("totalSiswa");
                long totalSiswa = total != null ? total.longValue() : 0L;

                result.add(new RekapSiswaPerJurusan(jurusan, totalSiswa));
            }
        }

        return result;
    }
}

