package com.smk.presensi.desktop.model;

/**
 * Model untuk Application Settings
 * Menyimpan konfigurasi aplikasi yang dapat diubah user
 */
public class AppSettings {
    
    private String serverUrl;
    private int autoRefreshInterval; // seconds
    private boolean enableWebSocket;
    private boolean autoReconnect;
    private boolean showNotifications;
    private String defaultExportFormat; // "PDF" or "CSV"
    private String defaultExportPath;
    
    // Default constructor
    public AppSettings() {
    }
    
    /**
     * Create default settings
     */
    public static AppSettings getDefault() {
        AppSettings settings = new AppSettings();
        settings.serverUrl = "http://localhost:8081";
        settings.autoRefreshInterval = 30;
        settings.enableWebSocket = true;
        settings.autoReconnect = true;
        settings.showNotifications = true;
        settings.defaultExportFormat = "PDF";
        settings.defaultExportPath = System.getProperty("user.home") + "\\Documents";
        return settings;
    }
    
    // Getters & Setters
    
    public String getServerUrl() {
        return serverUrl;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public int getAutoRefreshInterval() {
        return autoRefreshInterval;
    }
    
    public void setAutoRefreshInterval(int autoRefreshInterval) {
        this.autoRefreshInterval = autoRefreshInterval;
    }
    
    public boolean isEnableWebSocket() {
        return enableWebSocket;
    }
    
    public void setEnableWebSocket(boolean enableWebSocket) {
        this.enableWebSocket = enableWebSocket;
    }
    
    public boolean isAutoReconnect() {
        return autoReconnect;
    }
    
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }
    
    public boolean isShowNotifications() {
        return showNotifications;
    }
    
    public void setShowNotifications(boolean showNotifications) {
        this.showNotifications = showNotifications;
    }
    
    public String getDefaultExportFormat() {
        return defaultExportFormat;
    }
    
    public void setDefaultExportFormat(String defaultExportFormat) {
        this.defaultExportFormat = defaultExportFormat;
    }
    
    public String getDefaultExportPath() {
        return defaultExportPath;
    }
    
    public void setDefaultExportPath(String defaultExportPath) {
        this.defaultExportPath = defaultExportPath;
    }
    
    @Override
    public String toString() {
        return "AppSettings{" +
                "serverUrl='" + serverUrl + '\'' +
                ", autoRefreshInterval=" + autoRefreshInterval +
                ", enableWebSocket=" + enableWebSocket +
                ", autoReconnect=" + autoReconnect +
                ", showNotifications=" + showNotifications +
                ", defaultExportFormat='" + defaultExportFormat + '\'' +
                ", defaultExportPath='" + defaultExportPath + '\'' +
                '}';
    }
}
