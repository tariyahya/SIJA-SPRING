package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.PenempatanPkl;

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
 * Service desktop untuk penempatan PKL.
 */
public class PenempatanPklService {

    private final ApiClient apiClient;
    private final Gson gson;

    public PenempatanPklService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()))
                .create();
    }

    public PenempatanPkl create(Long siswaId,
                                Long dudiId,
                                LocalDate tanggalMulai,
                                LocalDate tanggalSelesai,
                                String keterangan) throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("siswaId", siswaId);
        body.put("dudiId", dudiId);
        body.put("tanggalMulai", tanggalMulai != null ? tanggalMulai.toString() : null);
        body.put("tanggalSelesai", tanggalSelesai != null ? tanggalSelesai.toString() : null);
        body.put("keterangan", keterangan);

        String json = gson.toJson(body);
        HttpResponse<String> response = apiClient.post("/pkl", json);

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new IOException("Failed to create penempatan PKL: " + response.statusCode() + " - " + response.body());
        }

        return gson.fromJson(response.body(), PenempatanPkl.class);
    }

    public List<PenempatanPkl> getByDudi(Long dudiId) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/pkl/dudi/" + dudiId);

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch penempatan by DUDI: " + response.statusCode());
        }

        Type listType = new TypeToken<List<PenempatanPkl>>() {}.getType();
        List<PenempatanPkl> list = gson.fromJson(response.body(), listType);
        return list != null ? list : new ArrayList<>();
    }

    public List<PenempatanPkl> getBySiswa(Long siswaId) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/pkl/siswa/" + siswaId);

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch penempatan by siswa: " + response.statusCode());
        }

        Type listType = new TypeToken<List<PenempatanPkl>>() {}.getType();
        List<PenempatanPkl> list = gson.fromJson(response.body(), listType);
        return list != null ? list : new ArrayList<>();
    }
}

