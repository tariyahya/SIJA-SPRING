package com.smk.presensi.desktop.service;

import com.smk.presensi.desktop.model.Presensi;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached wrapper for PresensiService
 * Implements in-memory cache with TTL (Time-To-Live)
 */
public class CachedPresensiService {
    private final PresensiService presensiService;
    private final LocalCacheService localCache;
    private final Map<String, CacheEntry<List<Presensi>>> memoryCache;
    private static final long DEFAULT_TTL = 5 * 60 * 1000; // 5 minutes

    public CachedPresensiService(PresensiService presensiService) {
        this.presensiService = presensiService;
        this.localCache = LocalCacheService.getInstance();
        this.memoryCache = new ConcurrentHashMap<>();
    }

    /**
     * Get presensi by date range with caching
     * Try: Memory Cache -> API -> Local Cache (fallback)
     */
    public List<Presensi> getPresensiByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        String cacheKey = startDate + "_" + endDate;

        // 1. Check memory cache
        CacheEntry<List<Presensi>> cached = memoryCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            System.out.println("✓ Returning data from memory cache");
            return cached.getData();
        }

        // 2. Try fetch from API
        try {
            List<Presensi> data = presensiService.getPresensiByDateRange(startDate, endDate);
            
            // Cache to memory
            memoryCache.put(cacheKey, new CacheEntry<>(data, DEFAULT_TTL));
            
            // Cache to local database (async)
            new Thread(() -> {
                try {
                    localCache.cachePresensi(data);
                } catch (Exception e) {
                    System.err.println("Failed to cache to local DB: " + e.getMessage());
                }
            }).start();
            
            System.out.println("✓ Fetched fresh data from API");
            return data;
            
        } catch (Exception e) {
            // 3. Fallback to local cache
            System.err.println("⚠ API failed, using local cache: " + e.getMessage());
            
            List<Presensi> cachedData = localCache.getCachedPresensi(startDate, endDate);
            
            if (cachedData.isEmpty()) {
                throw new Exception("No cached data available (offline mode)");
            }
            
            // Cache to memory for next access
            memoryCache.put(cacheKey, new CacheEntry<>(cachedData, DEFAULT_TTL));
            
            return cachedData;
        }
    }

    /**
     * Get today's presensi with caching
     */
    public List<Presensi> getTodayPresensi() throws Exception {
        LocalDate today = LocalDate.now();
        return getPresensiByDateRange(today, today);
    }

    /**
     * Clear memory cache
     */
    public void clearMemoryCache() {
        memoryCache.clear();
        System.out.println("Memory cache cleared");
    }

    /**
     * Clear local cache
     */
    public void clearLocalCache() {
        localCache.clearCache();
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        clearMemoryCache();
        clearLocalCache();
        System.out.println("All caches cleared");
    }

    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        LocalCacheService.CacheStats stats = localCache.getStats();
        return String.format("Memory: %d entries | Local: %s", memoryCache.size(), stats.toString());
    }

    /**
     * Create presensi with offline support
     * Try: API -> Local Cache (offline)
     */
    public Presensi createPresensi(Presensi presensi) throws Exception {
        try {
            // Try API first
            Presensi created = presensiService.createPresensi(presensi);
            
            // If success, cache it (synced=1)
            new Thread(() -> localCache.cachePresensi(List.of(created))).start();
            
            // Invalidate memory cache
            clearMemoryCache();
            
            return created;
        } catch (Exception e) {
            System.err.println("⚠ API failed, saving offline: " + e.getMessage());
            
            // Save offline (synced=0)
            localCache.saveOfflinePresensi(presensi);
            
            // Invalidate memory cache
            clearMemoryCache();
            
            // Return the object (it won't have a real ID yet)
            return presensi;
        }
    }

    /**
     * Cache entry with TTL
     */
    private static class CacheEntry<T> {
        private final T data;
        private final long timestamp;
        private final long ttl;

        public CacheEntry(T data, long ttl) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }

        public T getData() {
            return data;
        }

        public long getAge() {
            return System.currentTimeMillis() - timestamp;
        }
    }
}
