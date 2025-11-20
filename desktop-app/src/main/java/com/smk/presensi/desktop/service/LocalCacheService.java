package com.smk.presensi.desktop.service;

import com.smk.presensi.desktop.model.Presensi;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for local data caching using SQLite
 * Provides offline mode functionality
 */
public class LocalCacheService {
    private static LocalCacheService instance;
    private Connection connection;
    private final String dbPath;
    private final Gson gson = new Gson();

    private LocalCacheService() {
        // Store database in user home directory
        String userHome = System.getProperty("user.home");
        String appDir = userHome + File.separator + ".sija";
        this.dbPath = appDir + File.separator + "cache.db";
        
        initDatabase();
    }

    public static LocalCacheService getInstance() {
        if (instance == null) {
            instance = new LocalCacheService();
        }
        return instance;
    }

    private void initDatabase() {
        try {
            // Create directory if not exists
            File dbFile = new File(dbPath);
            File parentDir = dbFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
                System.out.println("Created cache directory: " + parentDir.getAbsolutePath());
            }

            // Connect to database
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            // Create tables
            createTables();
            
            System.out.println("Local cache database initialized at: " + dbPath);
        } catch (SQLException e) {
            System.err.println("Failed to init cache database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createPresensiTable = """
            CREATE TABLE IF NOT EXISTS presensi_cache (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                username TEXT NOT NULL,
                tipe TEXT,
                tanggal TEXT NOT NULL,
                jam_masuk TEXT NOT NULL,
                jam_pulang TEXT,
                status TEXT NOT NULL,
                method TEXT,
                keterangan TEXT,
                synced INTEGER DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createIndexTanggal = """
            CREATE INDEX IF NOT EXISTS idx_tanggal ON presensi_cache(tanggal)
        """;

        String createIndexSynced = """
            CREATE INDEX IF NOT EXISTS idx_synced ON presensi_cache(synced)
        """;

        String createIndexUsername = """
            CREATE INDEX IF NOT EXISTS idx_username ON presensi_cache(username)
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createPresensiTable);
            stmt.executeUpdate(createIndexTanggal);
            stmt.executeUpdate(createIndexSynced);
            stmt.executeUpdate(createIndexUsername);
        }
    }

    /**
     * Cache list of presensi records
     */
    public void cachePresensi(List<Presensi> presensiList) {
        if (presensiList == null || presensiList.isEmpty()) {
            return;
        }

        String sql = """
            INSERT OR REPLACE INTO presensi_cache 
            (id, user_id, username, tipe, tanggal, jam_masuk, jam_pulang, status, method, keterangan, synced, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, datetime('now'))
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            for (Presensi p : presensiList) {
                pstmt.setLong(1, p.getId());
                pstmt.setLong(2, p.getUserId());
                pstmt.setString(3, p.getUsername());
                pstmt.setString(4, p.getTipe());
                pstmt.setString(5, p.getTanggal().toString());
                pstmt.setString(6, p.getJamMasuk() != null ? p.getJamMasuk().toString() : null);
                pstmt.setString(7, p.getJamPulang() != null ? p.getJamPulang().toString() : null);
                pstmt.setString(8, p.getStatus());
                pstmt.setString(9, p.getMethod());
                pstmt.setString(10, p.getKeterangan());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);

            System.out.println("Cached " + results.length + " presensi records");
        } catch (SQLException e) {
            System.err.println("Failed to cache presensi: " + e.getMessage());
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Failed to rollback: " + ex.getMessage());
            }
        }
    }

    /**
     * Save offline presensi (synced = 0)
     * Used when creating presensi while offline
     */
    public void saveOfflinePresensi(Presensi p) {
        String sql = """
            INSERT INTO presensi_cache 
            (user_id, username, tipe, tanggal, jam_masuk, jam_pulang, status, method, keterangan, synced, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, datetime('now'))
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, p.getUserId());
            pstmt.setString(2, p.getUsername());
            pstmt.setString(3, p.getTipe());
            pstmt.setString(4, p.getTanggal().toString());
            pstmt.setString(5, p.getJamMasuk() != null ? p.getJamMasuk().toString() : null);
            pstmt.setString(6, p.getJamPulang() != null ? p.getJamPulang().toString() : null);
            pstmt.setString(7, p.getStatus());
            pstmt.setString(8, p.getMethod());
            pstmt.setString(9, p.getKeterangan());
            
            pstmt.executeUpdate();
            System.out.println("Saved offline presensi for user: " + p.getUsername());
        } catch (SQLException e) {
            System.err.println("Failed to save offline presensi: " + e.getMessage());
        }
    }

    /**
     * Get cached presensi by date range
     */
    public List<Presensi> getCachedPresensi(LocalDate startDate, LocalDate endDate) {
        List<Presensi> result = new ArrayList<>();

        String sql = """
            SELECT * FROM presensi_cache
            WHERE tanggal BETWEEN ? AND ?
            ORDER BY tanggal DESC, jam_masuk DESC
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Presensi p = new Presensi();
                p.setId(rs.getLong("id"));
                p.setUserId(rs.getLong("user_id"));
                p.setUsername(rs.getString("username"));
                p.setTipe(rs.getString("tipe"));
                p.setTanggal(java.time.LocalDate.parse(rs.getString("tanggal")));
                
                String jamMasuk = rs.getString("jam_masuk");
                if (jamMasuk != null) {
                    p.setJamMasuk(java.time.LocalTime.parse(jamMasuk));
                }
                
                String jamPulang = rs.getString("jam_pulang");
                if (jamPulang != null) {
                    p.setJamPulang(java.time.LocalTime.parse(jamPulang));
                }
                
                p.setStatus(rs.getString("status"));
                p.setMethod(rs.getString("method"));
                p.setKeterangan(rs.getString("keterangan"));

                result.add(p);
            }

            System.out.println("Retrieved " + result.size() + " cached records");
        } catch (SQLException e) {
            System.err.println("Failed to get cached presensi: " + e.getMessage());
        }

        return result;
    }

    /**
     * Get all cached presensi
     */
    public List<Presensi> getAllCachedPresensi() {
        List<Presensi> result = new ArrayList<>();

        String sql = "SELECT * FROM presensi_cache ORDER BY tanggal DESC, jam_masuk DESC LIMIT 1000";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Presensi p = new Presensi();
                p.setId(rs.getLong("id"));
                p.setUserId(rs.getLong("user_id"));
                p.setUsername(rs.getString("username"));
                p.setTipe(rs.getString("tipe"));
                p.setTanggal(java.time.LocalDate.parse(rs.getString("tanggal")));
                
                String jamMasuk = rs.getString("jam_masuk");
                if (jamMasuk != null) {
                    p.setJamMasuk(java.time.LocalTime.parse(jamMasuk));
                }
                
                String jamPulang = rs.getString("jam_pulang");
                if (jamPulang != null) {
                    p.setJamPulang(java.time.LocalTime.parse(jamPulang));
                }
                
                p.setStatus(rs.getString("status"));
                p.setMethod(rs.getString("method"));
                p.setKeterangan(rs.getString("keterangan"));

                result.add(p);
            }

            System.out.println("Retrieved " + result.size() + " cached records");
        } catch (SQLException e) {
            System.err.println("Failed to get all cached presensi: " + e.getMessage());
        }

        return result;
    }

    /**
     * Get all unsynced presensi records
     */
    public List<Presensi> getUnsyncedPresensi() {
        List<Presensi> result = new ArrayList<>();
        String sql = "SELECT * FROM presensi_cache WHERE synced = 0";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Presensi p = new Presensi();
                // Note: ID might be null or local ID, handled by backend on sync
                p.setId(rs.getLong("id")); 
                p.setUserId(rs.getLong("user_id"));
                p.setUsername(rs.getString("username"));
                p.setTipe(rs.getString("tipe"));
                p.setTanggal(java.time.LocalDate.parse(rs.getString("tanggal")));
                
                String jamMasuk = rs.getString("jam_masuk");
                if (jamMasuk != null) {
                    p.setJamMasuk(java.time.LocalTime.parse(jamMasuk));
                }
                
                String jamPulang = rs.getString("jam_pulang");
                if (jamPulang != null) {
                    p.setJamPulang(java.time.LocalTime.parse(jamPulang));
                }
                
                p.setStatus(rs.getString("status"));
                p.setMethod(rs.getString("method"));
                p.setKeterangan(rs.getString("keterangan"));

                result.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get unsynced presensi: " + e.getMessage());
        }
        return result;
    }

    /**
     * Mark records as synced after successful upload
     */
    public void markAsSynced(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        String sql = "UPDATE presensi_cache SET synced = 1, updated_at = datetime('now') WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            
            for (Long id : ids) {
                pstmt.setLong(1, id);
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("Marked " + ids.size() + " records as synced");
        } catch (SQLException e) {
            System.err.println("Failed to mark as synced: " + e.getMessage());
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Clear all cache
     */
    public void clearCache() {
        try (Statement stmt = connection.createStatement()) {
            int deleted = stmt.executeUpdate("DELETE FROM presensi_cache");
            System.out.println("Cache cleared: " + deleted + " records deleted");
        } catch (SQLException e) {
            System.err.println("Failed to clear cache: " + e.getMessage());
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        CacheStats stats = new CacheStats();

        try (Statement stmt = connection.createStatement()) {
            // Total records
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM presensi_cache");
            if (rs.next()) {
                stats.totalRecords = rs.getInt("total");
            }

            // Date range
            rs = stmt.executeQuery("SELECT MIN(tanggal) as min_date, MAX(tanggal) as max_date FROM presensi_cache");
            if (rs.next()) {
                stats.oldestDate = rs.getString("min_date");
                stats.newestDate = rs.getString("max_date");
            }

            // Database size
            File dbFile = new File(dbPath);
            if (dbFile.exists()) {
                stats.dbSizeBytes = dbFile.length();
            }

        } catch (SQLException e) {
            System.err.println("Failed to get cache stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Cache database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close cache connection: " + e.getMessage());
        }
    }

    /**
     * Cache statistics model
     */
    public static class CacheStats {
        public int totalRecords;
        public String oldestDate;
        public String newestDate;
        public long dbSizeBytes;

        @Override
        public String toString() {
            return String.format("CacheStats{records=%d, dateRange=%s to %s, size=%d KB}",
                    totalRecords, oldestDate, newestDate, dbSizeBytes / 1024);
        }
    }
}
