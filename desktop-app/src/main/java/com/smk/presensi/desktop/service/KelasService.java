package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.Kelas;
import com.smk.presensi.desktop.model.Siswa;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Kelas CRUD operations.
 * Communicates with backend REST API /api/kelas.
 */
public class KelasService {

    private final ApiClient apiClient;
    private final Gson gson;

    public KelasService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new GsonBuilder().create();
    }

    public List<Kelas> getAllKelas() {
        try {
            HttpResponse<String> response = apiClient.get("/kelas");

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Kelas>>() {}.getType();
                return gson.fromJson(response.body(), listType);
            } else {
                System.err.println("Error getting kelas list: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting kelas list: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Kelas getKelasById(Long id) {
        try {
            HttpResponse<String> response = apiClient.get("/kelas/" + id);

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Kelas.class);
            } else {
                System.err.println("Error getting kelas: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting kelas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Kelas> getKelasByJurusan(String jurusan) {
        try {
            HttpResponse<String> response = apiClient.get("/kelas/jurusan/" + jurusan);

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Kelas>>() {}.getType();
                return gson.fromJson(response.body(), listType);
            } else {
                System.err.println("Error getting kelas by jurusan: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting kelas by jurusan: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Kelas createKelas(Kelas kelas) {
        try {
            String jsonBody = gson.toJson(kelas);
            HttpResponse<String> response = apiClient.post("/kelas", jsonBody);

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), Kelas.class);
            } else {
                System.err.println("Error creating kelas: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error creating kelas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Kelas updateKelas(Long id, Kelas kelas) {
        try {
            String jsonBody = gson.toJson(kelas);
            HttpResponse<String> response = apiClient.put("/kelas/" + id, jsonBody);

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Kelas.class);
            } else {
                System.err.println("Error updating kelas: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error updating kelas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteKelas(Long id) {
        try {
            HttpResponse<String> response = apiClient.delete("/kelas/" + id);

            if (response.statusCode() == 204 || response.statusCode() == 200) {
                return true;
            } else {
                System.err.println("Error deleting kelas: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting kelas: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Assign beberapa siswa ke kelas tertentu.
     * Memanggil endpoint backend: POST /api/kelas/{kelasId}/assign-siswa
     */
    public List<Siswa> assignSiswaToKelas(Long kelasId, List<Long> siswaIds) {
        try {
            AssignRequest payload = new AssignRequest(siswaIds);
            String jsonBody = gson.toJson(payload);

            HttpResponse<String> response = apiClient.post("/kelas/" + kelasId + "/assign-siswa", jsonBody);

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Siswa>>() {}.getType();
                return gson.fromJson(response.body(), listType);
            } else {
                System.err.println("Error assigning siswa to kelas: " + response.statusCode() + " - " + response.body());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error assigning siswa to kelas: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static class AssignRequest {
        @SuppressWarnings("unused")
        private final List<Long> siswaIds;

        private AssignRequest(List<Long> siswaIds) {
            this.siswaIds = siswaIds;
        }
    }
}
