package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.TahunAjaran;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TahunAjaranService {
    private final ApiClient apiClient;
    private final Gson gson;

    public TahunAjaranService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new Gson();
    }

    public List<TahunAjaran> getAll() throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/tahun-ajaran");
        if (response.statusCode() == 200) {
            Type listType = new TypeToken<ArrayList<TahunAjaran>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        }
        throw new IOException("Failed to fetch tahun ajaran: " + response.statusCode());
    }

    public TahunAjaran getById(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/tahun-ajaran/" + id);
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), TahunAjaran.class);
        }
        throw new IOException("Failed to fetch tahun ajaran: " + response.statusCode());
    }

    public TahunAjaran getActive() throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/tahun-ajaran/active");
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), TahunAjaran.class);
        }
        throw new IOException("Failed to fetch active tahun ajaran: " + response.statusCode());
    }

    public TahunAjaran create(TahunAjaran tahunAjaran) throws IOException, InterruptedException {
        String jsonBody = gson.toJson(tahunAjaran);
        HttpResponse<String> response = apiClient.post("/tahun-ajaran", jsonBody);
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return gson.fromJson(response.body(), TahunAjaran.class);
        }
        throw new IOException("Failed to create tahun ajaran: " + response.statusCode());
    }

    public TahunAjaran update(Long id, TahunAjaran tahunAjaran) throws IOException, InterruptedException {
        String jsonBody = gson.toJson(tahunAjaran);
        HttpResponse<String> response = apiClient.put("/tahun-ajaran/" + id, jsonBody);
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), TahunAjaran.class);
        }
        throw new IOException("Failed to update tahun ajaran: " + response.statusCode());
    }

    public void setActive(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.put("/tahun-ajaran/" + id + "/set-active", "{}");
        if (response.statusCode() != 200) {
            throw new IOException("Failed to set active tahun ajaran: " + response.statusCode());
        }
    }

    public void delete(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.delete("/tahun-ajaran/" + id);
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new IOException("Failed to delete tahun ajaran: " + response.statusCode());
        }
    }
}
