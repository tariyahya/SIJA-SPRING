package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.Izin;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service desktop untuk modul perizinan.
 */
public class IzinService {

    private final ApiClient apiClient;
    private final Gson gson;

    public IzinService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()))
                .create();
    }

    /**
     * Ambil daftar izin pending untuk hari ini.
     */
    public List<Izin> getPendingToday() throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/izin/pending");

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch pending izin: " + response.statusCode());
        }

        Type listType = new TypeToken<List<Izin>>() {}.getType();
        List<Izin> list = gson.fromJson(response.body(), listType);
        return list != null ? list : new ArrayList<>();
    }

    /**
     * Buat pengajuan izin baru.
     */
    public Izin createIzin(Long siswaId,
                           String jenis,
                           LocalDate tanggalMulai,
                           LocalDate tanggalSelesai,
                           String alasan) throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("siswaId", siswaId);
        body.put("jenis", jenis);
        body.put("tanggalMulai", tanggalMulai != null ? tanggalMulai.toString() : null);
        body.put("tanggalSelesai", tanggalSelesai != null ? tanggalSelesai.toString() : null);
        body.put("alasan", alasan);

        String json = gson.toJson(body);
        HttpResponse<String> response = apiClient.post("/izin", json);

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new IOException("Failed to create izin: " + response.statusCode() + " - " + response.body());
        }

        return gson.fromJson(response.body(), Izin.class);
    }

    /**
     * Approve / reject izin.
     */
    public Izin approveIzin(Long izinId, String status, String catatan) throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("catatan", catatan);

        String json = gson.toJson(body);
        HttpResponse<String> response = apiClient.post("/izin/" + izinId + "/approve", json);

        if (response.statusCode() != 200) {
            throw new IOException("Failed to approve/reject izin: " + response.statusCode() + " - " + response.body());
        }

        return gson.fromJson(response.body(), Izin.class);
    }
}

