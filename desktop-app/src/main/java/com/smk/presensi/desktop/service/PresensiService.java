package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.DashboardStats;
import com.smk.presensi.desktop.model.Presensi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service layer untuk operasi Presensi
 * Wrapper untuk API endpoints related to presensi
 */
public class PresensiService {
    private final ApiClient apiClient;
    private final Gson gson;

    public PresensiService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new Gson();
    }

    /**
     * Get laporan harian (today or specific date)
     */
    public List<Presensi> getLaporanHarian(LocalDate tanggal) throws IOException, InterruptedException {
        String endpoint = tanggal != null 
            ? "/laporan/harian?tanggal=" + tanggal.toString()
            : "/laporan/harian";

        HttpResponse<String> response = apiClient.get(endpoint);

        if (response.statusCode() == 200) {
            // Parse response: { "message": "...", "data": { "daftarPresensi": [...] } }
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> responseMap = gson.fromJson(response.body(), type);
            
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            List<Map<String, Object>> daftarPresensi = (List<Map<String, Object>>) data.get("daftarPresensi");
            
            List<Presensi> presensiList = new ArrayList<>();
            for (Map<String, Object> item : daftarPresensi) {
                Presensi p = new Presensi();
                p.setId(((Double) item.get("id")).longValue());
                p.setUserId(((Double) item.get("userId")).longValue());
                p.setUsername((String) item.get("username"));
                p.setTipe((String) item.get("tipe"));
                p.setTanggal(LocalDate.parse((String) item.get("tanggal")));
                p.setJamMasuk(item.get("jamMasuk") != null 
                    ? java.time.LocalTime.parse((String) item.get("jamMasuk")) 
                    : null);
                p.setJamPulang(item.get("jamPulang") != null
                    ? java.time.LocalTime.parse((String) item.get("jamPulang"))
                    : null);
                p.setStatus((String) item.get("status"));
                p.setMethod((String) item.get("method"));
                p.setKeterangan((String) item.get("keterangan"));
                presensiList.add(p);
            }
            
            return presensiList;
        }

        throw new IOException("Failed to fetch laporan harian: " + response.statusCode());
    }

    /**
     * Get dashboard statistics (hari ini)
     */
    public DashboardStats getDashboardStats() throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.get("/laporan/harian");

        if (response.statusCode() == 200) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> responseMap = gson.fromJson(response.body(), type);
            
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            
            DashboardStats stats = new DashboardStats();
            stats.setTotalPresensi(((Double) data.get("totalPresensi")).intValue());
            stats.setTotalHadir(((Double) data.get("totalHadir")).intValue());
            stats.setTotalTerlambat(((Double) data.get("totalTerlambat")).intValue());
            stats.setTotalAlpha(((Double) data.get("totalAlpha")).intValue());
            stats.setPersentaseHadir((Double) data.get("persentaseHadir"));
            
            return stats;
        }

        throw new IOException("Failed to fetch dashboard stats: " + response.statusCode());
    }

    /**
     * Get today's presensi (shortcut for today's date)
     */
    public List<Presensi> getTodayPresensi() throws IOException, InterruptedException {
        return getLaporanHarian(LocalDate.now());
    }

    /**
     * RFID Checkin (simulasi dari desktop app)
     */
    public boolean checkinRfid(String rfidCardId) throws IOException, InterruptedException {
        String jsonBody = String.format("{\"rfidCardId\":\"%s\"}", rfidCardId);
        HttpResponse<String> response = apiClient.post("/presensi/rfid/checkin", jsonBody);
        
        return response.statusCode() == 200 || response.statusCode() == 201;
    }

    /**
     * Get mock data (untuk development tanpa backend)
     */
    public List<Presensi> getMockData() {
        List<Presensi> mockList = new ArrayList<>();
        
        for (int i = 1; i <= 20; i++) {
            Presensi p = new Presensi();
            p.setId((long) i);
            p.setUserId((long) i);
            p.setUsername("1234" + i);
            p.setTipe(i % 5 == 0 ? "GURU" : "SISWA");
            p.setTanggal(LocalDate.now());
            p.setJamMasuk(java.time.LocalTime.of(7, 5 + (i % 30), 0));
            p.setJamPulang(i % 3 == 0 ? java.time.LocalTime.of(15, 0, 0) : null);
            p.setStatus(i % 10 == 0 ? "TERLAMBAT" : "HADIR");
            p.setMethod(i % 4 == 0 ? "RFID" : "MANUAL");
            p.setKeterangan("Presensi hari ini");
            mockList.add(p);
        }
        
        return mockList;
    }

    /**
     * Get mock dashboard stats
     */
    public DashboardStats getMockStats() {
        return new DashboardStats(95, 85, 8, 2, 89.47);
    }
    
    /**
     * Get presensi by date range (untuk export reports)
     */
    public List<Presensi> getPresensiByDateRange(LocalDate startDate, LocalDate endDate) 
            throws IOException, InterruptedException {
        String endpoint = "/laporan/periode?startDate=" + startDate + "&endDate=" + endDate;
        
        HttpResponse<String> response = apiClient.get(endpoint);
        
        if (response.statusCode() == 200) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> responseMap = gson.fromJson(response.body(), type);
            
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            List<Map<String, Object>> daftarPresensi = (List<Map<String, Object>>) data.get("daftarPresensi");
            
            List<Presensi> presensiList = new ArrayList<>();
            for (Map<String, Object> item : daftarPresensi) {
                Presensi p = new Presensi();
                p.setId(((Double) item.get("id")).longValue());
                p.setUserId(((Double) item.get("userId")).longValue());
                p.setUsername((String) item.get("username"));
                p.setTipe((String) item.get("tipe"));
                p.setTanggal(LocalDate.parse((String) item.get("tanggal")));
                p.setJamMasuk(item.get("jamMasuk") != null 
                    ? java.time.LocalTime.parse((String) item.get("jamMasuk")) 
                    : null);
                p.setJamPulang(item.get("jamPulang") != null
                    ? java.time.LocalTime.parse((String) item.get("jamPulang"))
                    : null);
                p.setStatus((String) item.get("status"));
                p.setMethod((String) item.get("method"));
                p.setKeterangan((String) item.get("keterangan"));
                presensiList.add(p);
            }
            
            return presensiList;
        }
        
        throw new IOException("Failed to fetch presensi by date range: " + response.statusCode());
    }
}
