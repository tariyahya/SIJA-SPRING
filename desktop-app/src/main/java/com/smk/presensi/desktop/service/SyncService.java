package com.smk.presensi.desktop.service;

import com.smk.presensi.desktop.model.Presensi;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle background synchronization of offline data.
 */
public class SyncService {
    private static SyncService instance;
    private final LocalCacheService localCache;
    private final PresensiService presensiService;
    private final ScheduledExecutorService scheduler;
    private boolean isSyncing = false;

    private SyncService() {
        this.localCache = LocalCacheService.getInstance();
        this.presensiService = new PresensiService(ApiClient.getInstance());
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public static SyncService getInstance() {
        if (instance == null) {
            instance = new SyncService();
        }
        return instance;
    }

    /**
     * Start background sync scheduler (runs every 5 minutes)
     */
    public void startBackgroundSync() {
        scheduler.scheduleAtFixedRate(this::syncNow, 1, 5, TimeUnit.MINUTES);
        System.out.println("Background sync scheduler started");
    }

    /**
     * Stop scheduler
     */
    public void stop() {
        scheduler.shutdown();
    }

    /**
     * Trigger manual sync
     */
    public void syncNow() {
        if (isSyncing) return;
        
        Task<Void> syncTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                isSyncing = true;
                try {
                    List<Presensi> unsynced = localCache.getUnsyncedPresensi();
                    if (unsynced.isEmpty()) {
                        return null;
                    }

                    System.out.println("Found " + unsynced.size() + " unsynced records. Syncing...");
                    List<Long> syncedIds = new ArrayList<>();

                    for (Presensi p : unsynced) {
                        try {
                            // Try to create on server
                            // Note: We might need a specific endpoint for batch sync or handle ID conflicts
                            // For now, we treat them as new records
                            Presensi created = presensiService.createPresensi(p);
                            if (created != null) {
                                syncedIds.add(p.getId()); // Use local ID to mark as synced
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to sync record for " + p.getUsername() + ": " + e.getMessage());
                            // Continue to next record
                        }
                    }

                    if (!syncedIds.isEmpty()) {
                        localCache.markAsSynced(syncedIds);
                        NotificationService.getInstance().showInfo("Sync Complete", 
                            "Successfully synced " + syncedIds.size() + " records.");
                    }

                } finally {
                    isSyncing = false;
                }
                return null;
            }
        };

        new Thread(syncTask).start();
    }
}
