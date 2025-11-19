package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.DashboardStats;
import com.smk.presensi.desktop.model.Presensi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
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
     * DTO untuk admin create/update presensi (desktop).
     */
    public static class AdminPresensiPayload {
        public Long userId;
        public String tipe;
        public String tanggal;
        public String jamMasuk;
        public String jamPulang;
        public String status;
        public String method;
        public Double latitude;
        public Double longitude;
        public String keterangan;
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
            if (data == null) {
                throw new IOException("Invalid response: 'data' field is missing in /laporan/harian");
            }

            DashboardStats stats = new DashboardStats();

            stats.setTotalPresensi(asInt(data.get("totalPresensi")));
            stats.setTotalHadir(asInt(data.get("totalHadir")));
            stats.setTotalTerlambat(asInt(data.get("totalTerlambat")));
            stats.setTotalAlpha(asInt(data.get("totalAlfa")));
            stats.setPersentaseHadir(asDouble(data.get("persentaseHadir")));

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
        String[] statuses = {"HADIR", "TERLAMBAT", "IZIN", "SAKIT", "ALPHA", "DISPENSASI"};
        
        for (int i = 1; i <= 20; i++) {
            Presensi p = new Presensi();
            p.setId((long) i);
            p.setUserId((long) i);
            p.setUsername("1234" + i);
            p.setTipe(i % 5 == 0 ? "GURU" : "SISWA");
            p.setTanggal(LocalDate.now());
            p.setJamMasuk(java.time.LocalTime.of(7, 5 + (i % 30), 0));
            p.setJamPulang(i % 3 == 0 ? java.time.LocalTime.of(15, 0, 0) : null);
            p.setStatus(statuses[i % statuses.length]);
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
     * ADMIN: Create presensi record.
     */
    public Presensi createPresensi(Presensi presensi) throws IOException, InterruptedException {
        AdminPresensiPayload payload = toAdminPayload(presensi);
        String jsonBody = gson.toJson(payload);
        HttpResponse<String> response = apiClient.post("/admin/presensi", jsonBody);

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return parseSinglePresensi(response.body());
        }
        throw new IOException("Failed to create presensi: " + response.statusCode() + " - " + response.body());
    }

    /**
     * ADMIN: Update presensi record.
     */
    public Presensi updatePresensi(Presensi presensi) throws IOException, InterruptedException {
        if (presensi.getId() == null) {
            throw new IllegalArgumentException("Presensi ID is required for update");
        }
        AdminPresensiPayload payload = toAdminPayload(presensi);
        String jsonBody = gson.toJson(payload);
        HttpResponse<String> response = apiClient.put("/admin/presensi/" + presensi.getId(), jsonBody);

        if (response.statusCode() == 200) {
            return parseSinglePresensi(response.body());
        }
        throw new IOException("Failed to update presensi: " + response.statusCode() + " - " + response.body());
    }

    /**
     * ADMIN: Delete presensi record.
     */
    public boolean deletePresensi(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.delete("/admin/presensi/" + id);
        return response.statusCode() == 200 || response.statusCode() == 204;
    }

    /**
     * Helper: safely convert Object number ke int (default 0 kalau null / bukan Number).
     */
    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    /**
     * Helper: safely convert Object number ke double (default 0.0 kalau null / bukan Number).
     */
    private double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }

    private AdminPresensiPayload toAdminPayload(Presensi p) {
        AdminPresensiPayload payload = new AdminPresensiPayload();
        payload.userId = p.getUserId();
        payload.tipe = p.getTipe();
        payload.tanggal = p.getTanggal() != null ? p.getTanggal().toString() : null;
        payload.jamMasuk = p.getJamMasuk() != null ? p.getJamMasuk().toString() : null;
        payload.jamPulang = p.getJamPulang() != null ? p.getJamPulang().toString() : null;
        payload.status = p.getStatus();
        payload.method = p.getMethod();
        payload.latitude = null;
        payload.longitude = null;
        payload.keterangan = p.getKeterangan();
        return payload;
    }

    private Presensi parseSinglePresensi(String json) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(json, type);

        Presensi p = new Presensi();
        p.setId(((Double) map.get("id")).longValue());
        p.setUserId(((Double) map.get("userId")).longValue());
        p.setUsername((String) map.get("username"));
        p.setTipe(map.get("tipe").toString());
        p.setTanggal(LocalDate.parse((String) map.get("tanggal")));
        Object jamMasukObj = map.get("jamMasuk");
        if (jamMasukObj != null) {
            p.setJamMasuk(LocalTime.parse(jamMasukObj.toString()));
        }
        Object jamPulangObj = map.get("jamPulang");
        if (jamPulangObj != null) {
            p.setJamPulang(LocalTime.parse(jamPulangObj.toString()));
        }
        p.setStatus(map.get("status").toString());
        p.setMethod(map.get("method").toString());
        p.setKeterangan((String) map.get("keterangan"));
        return p;
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
