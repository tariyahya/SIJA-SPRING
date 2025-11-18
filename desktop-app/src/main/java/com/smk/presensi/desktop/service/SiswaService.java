package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.Siswa;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Siswa CRUD operations
 * Communicates with backend REST API
 */
public class SiswaService {
    private final ApiClient apiClient;
    private final Gson gson;
    
    public SiswaService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }
    
    /**
     * Get all siswa from backend
     */
    public List<Siswa> getAllSiswa() {
        try {
            HttpResponse<String> response = apiClient.get("/siswa");
            
            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Siswa>>(){}.getType();
                return gson.fromJson(response.body(), listType);
            } else {
                System.err.println("Error getting siswa list: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting siswa list: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get siswa by ID
     */
    public Siswa getSiswaById(Long id) {
        try {
            HttpResponse<String> response = apiClient.get("/siswa/" + id);
            
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Siswa.class);
            } else {
                System.err.println("Error getting siswa: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting siswa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get siswa by kelas
     */
    public List<Siswa> getSiswaByKelas(String kelas) {
        try {
            HttpResponse<String> response = apiClient.get("/siswa/kelas/" + kelas);
            
            if (response.statusCode() == 200) {
                Type listType = new TypeToken<List<Siswa>>(){}.getType();
                return gson.fromJson(response.body(), listType);
            } else {
                System.err.println("Error getting siswa by kelas: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error getting siswa by kelas: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Create new siswa
     */
    public Siswa createSiswa(Siswa siswa) {
        try {
            String jsonBody = gson.toJson(siswa);
            HttpResponse<String> response = apiClient.post("/siswa", jsonBody);
            
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return gson.fromJson(response.body(), Siswa.class);
            } else {
                System.err.println("Error creating siswa: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error creating siswa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Update existing siswa
     */
    public Siswa updateSiswa(Long id, Siswa siswa) {
        try {
            String jsonBody = gson.toJson(siswa);
            HttpResponse<String> response = apiClient.put("/siswa/" + id, jsonBody);
            
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), Siswa.class);
            } else {
                System.err.println("Error updating siswa: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error updating siswa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Delete siswa by ID
     */
    public boolean deleteSiswa(Long id) {
        try {
            HttpResponse<String> response = apiClient.delete("/siswa/" + id);
            
            if (response.statusCode() == 204 || response.statusCode() == 200) {
                return true;
            } else {
                System.err.println("Error deleting siswa: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting siswa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get mock siswa data for testing
     */
    public List<Siswa> getMockData() {
        List<Siswa> mockList = new ArrayList<>();
        
        mockList.add(createMockSiswa(1L, "12345", "Ahmad Rizki", "XII RPL 1", "RPL"));
        mockList.add(createMockSiswa(2L, "12346", "Siti Nurhaliza", "XII RPL 1", "RPL"));
        mockList.add(createMockSiswa(3L, "12347", "Budi Santoso", "XII TKJ 1", "TKJ"));
        mockList.add(createMockSiswa(4L, "12348", "Dewi Lestari", "XII TKJ 1", "TKJ"));
        mockList.add(createMockSiswa(5L, "12349", "Eko Prasetyo", "XII MM 1", "MM"));
        mockList.add(createMockSiswa(6L, "12350", "Fitri Handayani", "XII MM 1", "MM"));
        mockList.add(createMockSiswa(7L, "12351", "Gilang Ramadan", "XI RPL 1", "RPL"));
        mockList.add(createMockSiswa(8L, "12352", "Hana Safitri", "XI RPL 1", "RPL"));
        mockList.add(createMockSiswa(9L, "12353", "Indra Gunawan", "XI TKJ 1", "TKJ"));
        mockList.add(createMockSiswa(10L, "12354", "Joko Widodo", "XI TKJ 1", "TKJ"));
        
        return mockList;
    }
    
    private Siswa createMockSiswa(Long id, String nis, String nama, String kelas, String jurusan) {
        Siswa siswa = new Siswa();
        siswa.setId(id);
        siswa.setNis(nis);
        siswa.setNama(nama);
        siswa.setKelas(kelas);
        siswa.setJurusan(jurusan);
        siswa.setRfidCardId("RFID" + nis);
        siswa.setBarcodeId("BC" + nis);
        siswa.setCreatedAt(LocalDateTime.now().minusDays(id));
        siswa.setUpdatedAt(LocalDateTime.now());
        return siswa;
    }
}
