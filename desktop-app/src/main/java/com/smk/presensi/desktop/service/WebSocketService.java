package com.smk.presensi.desktop.service;

import com.smk.presensi.desktop.model.DashboardStats;
import com.smk.presensi.desktop.model.Presensi;

import java.util.function.Consumer;

/**
 * WebSocket Service untuk real-time updates dari backend
 * 
 * NOTE: WebSocket implementation is currently disabled due to complexity with Jakarta WebSocket API
 * Alternative: Using polling mechanism via DashboardViewModel's auto-refresh
 * 
 * TODO untuk Tahap 04: Implement proper WebSocket dengan Spring STOMP atau SockJS client
 */
public class WebSocketService {
    
    private final String wsUrl;
    private boolean connected = false;
    
    // Callbacks untuk UI updates
    private Consumer<Presensi> onPresensiCreated;
    private Consumer<Presensi> onPresensiUpdated;
    private Consumer<DashboardStats> onStatsUpdate;
    private Consumer<String> onConnectionStatusChanged;
    
    // Settings
    private boolean autoReconnect = true;
    
    public WebSocketService(String wsUrl) {
        this.wsUrl = wsUrl;
    }
    
    /**
     * Connect to WebSocket server dengan JWT token
     * Currently not implemented - using polling instead
     */
    public void connect(String jwtToken) throws Exception {
        System.out.println("[WebSocketService] Connection not implemented yet - using polling");
        System.out.println("[WebSocketService] Target URL: " + wsUrl);
        
        connected = false;
        notifyConnectionStatus("Polling mode (WebSocket not implemented)");
    }
    
    /**
     * Disconnect dari WebSocket server
     */
    public void disconnect() {
        connected = false;
        System.out.println("[WebSocketService] Disconnected");
    }
    
    /**
     * Check if WebSocket is connected
     */
    public boolean isConnected() {
        return connected;
    }
    
    private void notifyConnectionStatus(String status) {
        if (onConnectionStatusChanged != null) {
            onConnectionStatusChanged.accept(status);
        }
    }
    
    // ===== Setters for callbacks =====
    
    public void setOnPresensiCreated(Consumer<Presensi> callback) {
        this.onPresensiCreated = callback;
    }
    
    public void setOnPresensiUpdated(Consumer<Presensi> callback) {
        this.onPresensiUpdated = callback;
    }
    
    public void setOnStatsUpdate(Consumer<DashboardStats> callback) {
        this.onStatsUpdate = callback;
    }
    
    public void setOnConnectionStatusChanged(Consumer<String> callback) {
        this.onConnectionStatusChanged = callback;
    }
    
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }
}
