package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.Jurusan;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Jurusan CRUD operations.
 * Communicates with backend REST API /api/jurusan.
 */
public class JurusanService {

    private final ApiClient apiClient;
    private final Gson gson;

    public JurusanService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new GsonBuilder().create();
    }

    public List<Jurusan> getAllJurusan() {
        try {
            HttpResponse<String> response = apiClient.get("/jurusan");

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Jurusan>>() {}.getType();
                return gson.fromJson(response.body(), listType);
            } else {
                System.err.println("Error getting jurusan list: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting jurusan list: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Jurusan getJurusanById(Long id) {
        try {
            HttpResponse<String> response = apiClient.get("/jurusan/" + id);

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Jurusan.class);
            } else {
                System.err.println("Error getting jurusan: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting jurusan: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Jurusan createJurusan(Jurusan jurusan) {
        try {
            String jsonBody = gson.toJson(jurusan);
            HttpResponse<String> response = apiClient.post("/jurusan", jsonBody);

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), Jurusan.class);
            } else {
                System.err.println("Error creating jurusan: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error creating jurusan: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Jurusan updateJurusan(Long id, Jurusan jurusan) {
        try {
            String jsonBody = gson.toJson(jurusan);
            HttpResponse<String> response = apiClient.put("/jurusan/" + id, jsonBody);

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Jurusan.class);
            } else {
                System.err.println("Error updating jurusan: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error updating jurusan: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteJurusan(Long id) {
        try {
            HttpResponse<String> response = apiClient.delete("/jurusan/" + id);

            if (response.statusCode() == 204 || response.statusCode() == 200) {
                return true;
            } else {
                System.err.println("Error deleting jurusan: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting jurusan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

