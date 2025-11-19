package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.LokasiKantor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LokasiKantorService {

    private final ApiClient apiClient;
    private final Gson gson;

    public LokasiKantorService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new Gson();
    }

    public List<LokasiKantor> getAll() throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/lokasi-kantor");
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch locations: " + response.statusCode());
        }

        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> responseMap = gson.fromJson(response.body(), mapType);
        
        Object data = responseMap.get("data");
        String jsonList = gson.toJson(data);
        
        Type listType = new TypeToken<List<LokasiKantor>>(){}.getType();
        return gson.fromJson(jsonList, listType);
    }

    public LokasiKantor create(LokasiKantor lokasi) throws IOException, InterruptedException {
        String json = gson.toJson(lokasi);
        HttpResponse<String> response = apiClient.post("/lokasi-kantor", json);
        
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> responseMap = gson.fromJson(response.body(), mapType);
            Object data = responseMap.get("data");
            return gson.fromJson(gson.toJson(data), LokasiKantor.class);
        }
        throw new IOException("Failed to create location: " + response.statusCode());
    }

    public LokasiKantor update(Long id, LokasiKantor lokasi) throws IOException, InterruptedException {
        String json = gson.toJson(lokasi);
        HttpResponse<String> response = apiClient.put("/lokasi-kantor/" + id, json);
        
        if (response.statusCode() == 200) {
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> responseMap = gson.fromJson(response.body(), mapType);
            Object data = responseMap.get("data");
            return gson.fromJson(gson.toJson(data), LokasiKantor.class);
        }
        throw new IOException("Failed to update location: " + response.statusCode());
    }

    public void delete(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.delete("/lokasi-kantor/" + id);
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new IOException("Failed to delete location: " + response.statusCode());
        }
    }
    
    public void activate(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.post("/lokasi-kantor/" + id + "/activate", "");
        if (response.statusCode() != 200) {
            throw new IOException("Failed to activate location: " + response.statusCode());
        }
    }
}
