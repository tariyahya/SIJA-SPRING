package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.smk.presensi.desktop.model.AppSettings;

import java.util.prefs.Preferences;

/**
 * Manager untuk Application Settings
 * Menggunakan Java Preferences API untuk persist settings
 */
public class SettingsManager {
    
    private static SettingsManager instance;
    private final Preferences prefs;
    private AppSettings settings;
    private final Gson gson;
    
    private SettingsManager() {
        prefs = Preferences.userRoot().node("com.smk.presensi.desktop.settings");
        gson = new Gson();
        loadSettings();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }
    
    /**
     * Load settings dari Preferences
     */
    private void loadSettings() {
        String json = prefs.get("app_settings", null);
        
        if (json != null) {
            try {
                settings = gson.fromJson(json, AppSettings.class);
                System.out.println("Settings loaded: " + settings);
            } catch (Exception e) {
                System.err.println("Error loading settings: " + e.getMessage());
                settings = AppSettings.getDefault();
            }
        } else {
            System.out.println("No saved settings found, using defaults");
            settings = AppSettings.getDefault();
        }
    }
    
    /**
     * Save settings to Preferences
     */
    public void saveSettings(AppSettings settings) {
        this.settings = settings;
        String json = gson.toJson(settings);
        prefs.put("app_settings", json);
        System.out.println("Settings saved: " + settings);
    }
    
    /**
     * Get current settings
     */
    public AppSettings getSettings() {
        return settings;
    }
    
    /**
     * Reset settings to default
     */
    public void resetToDefault() {
        settings = AppSettings.getDefault();
        saveSettings(settings);
        System.out.println("Settings reset to default");
    }
    
    /**
     * Clear all settings
     */
    public void clearSettings() {
        prefs.remove("app_settings");
        settings = AppSettings.getDefault();
        System.out.println("Settings cleared");
    }
}
