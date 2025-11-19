package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.Dudi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service desktop untuk master DUDI.
 */
public class DudiService {

    private final ApiClient apiClient;
    private final Gson gson;

    public DudiService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public List<Dudi> getAll() throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/dudi");

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch DUDI list: " + response.statusCode());
        }

        Type listType = new TypeToken<List<Dudi>>() {}.getType();
        List<Dudi> list = gson.fromJson(response.body(), listType);
        return list != null ? list : new ArrayList<>();
    }

    public Dudi create(Dudi dudi) throws IOException, InterruptedException {
        String json = gson.toJson(dudi);
        HttpResponse<String> response = apiClient.post("/dudi", json);

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new IOException("Failed to create DUDI: " + response.statusCode() + " - " + response.body());
        }

        return gson.fromJson(response.body(), Dudi.class);
    }

    public Dudi update(Long id, Dudi dudi) throws IOException, InterruptedException {
        String json = gson.toJson(dudi);
        HttpResponse<String> response = apiClient.put("/dudi/" + id, json);

        if (response.statusCode() != 200) {
            throw new IOException("Failed to update DUDI: " + response.statusCode() + " - " + response.body());
        }

        return gson.fromJson(response.body(), Dudi.class);
    }

    public void delete(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.delete("/dudi/" + id);

        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new IOException("Failed to delete DUDI: " + response.statusCode() + " - " + response.body());
        }
    }
}

