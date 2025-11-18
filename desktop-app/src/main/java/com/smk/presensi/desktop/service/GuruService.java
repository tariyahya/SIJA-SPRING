package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.Guru;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Guru CRUD operations
 * Communicates with backend REST API
 */
public class GuruService {
    private final ApiClient apiClient;
    private final Gson gson;
    
    public GuruService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }
    
    /**
     * Get all guru from backend
     */
    public List<Guru> getAllGuru() {
        try {
            HttpResponse<String> response = apiClient.get("/guru");
            
            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Guru>>(){}.getType();
                return gson.fromJson(response.body(), listType);
            } else {
                System.err.println("Error getting guru list: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting guru list: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get guru by ID
     */
    public Guru getGuruById(Long id) {
        try {
            HttpResponse<String> response = apiClient.get("/guru/" + id);
            
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Guru.class);
            } else {
                System.err.println("Error getting guru: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting guru: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create new guru
     */
    public Guru createGuru(Guru guru) {
        try {
            String jsonBody = gson.toJson(guru);
            HttpResponse<String> response = apiClient.post("/guru", jsonBody);
            
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), Guru.class);
            } else {
                System.err.println("Error creating guru: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error creating guru: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Update existing guru
     */
    public Guru updateGuru(Long id, Guru guru) {
        try {
            String jsonBody = gson.toJson(guru);
            HttpResponse<String> response = apiClient.put("/guru/" + id, jsonBody);
            
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Guru.class);
            } else {
                System.err.println("Error updating guru: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error updating guru: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Delete guru by ID
     */
    public boolean deleteGuru(Long id) {
        try {
            HttpResponse<String> response = apiClient.delete("/guru/" + id);
            
            if (response.statusCode() == 204 || response.statusCode() == 200) {
                return true;
            } else {
                System.err.println("Error deleting guru: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting guru: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get mock guru data for testing
     */
    public List<Guru> getMockData() {
        List<Guru> mockList = new ArrayList<>();
        
        mockList.add(createMockGuru(1L, "197001011998031001", "Drs. Agus Susanto", "Matematika"));
        mockList.add(createMockGuru(2L, "197502152000122001", "Sri Rahayu, S.Pd", "Bahasa Indonesia"));
        mockList.add(createMockGuru(3L, "198003202005011002", "Bambang Wijaya, S.Kom", "Pemrograman Web"));
        mockList.add(createMockGuru(4L, "198205102006042003", "Ani Setyaningsih, S.Pd", "Bahasa Inggris"));
        mockList.add(createMockGuru(5L, "197908152003121001", "Dedi Mulyadi, S.Kom", "Jaringan Komputer"));
        mockList.add(createMockGuru(6L, "198406202008012002", "Erna Wati, S.Pd", "Matematika"));
        mockList.add(createMockGuru(7L, "198109122009021001", "Fajar Nugraha, S.Kom", "Basis Data"));
        mockList.add(createMockGuru(8L, "198507302010122003", "Lina Marlina, S.Pd", "PKN"));
        
        return mockList;
    }
    
    private Guru createMockGuru(Long id, String nip, String nama, String mapel) {
        Guru guru = new Guru();
        guru.setId(id);
        guru.setNip(nip);
        guru.setNama(nama);
        guru.setMapel(mapel);
        guru.setRfidCardId("RFID-G" + id);
        guru.setBarcodeId("BC-G" + id);
        guru.setCreatedAt(LocalDateTime.now().minusDays(id));
        guru.setUpdatedAt(LocalDateTime.now());
        return guru;
    }
}
