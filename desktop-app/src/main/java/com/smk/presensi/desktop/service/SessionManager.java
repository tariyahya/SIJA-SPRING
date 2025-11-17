package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.smk.presensi.desktop.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.prefs.Preferences;

/**
 * SessionManager - Manage user session dan JWT token
 * 
 * Features:
 * - Save/load JWT token
 * - Save/load current user info
 * - Remember me functionality (persistent login)
 * - Auto-login on app restart
 * - Logout (clear session)
 */
public class SessionManager {
    private static final String PREF_NODE = "com.smk.presensi.desktop";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_USER_DATA = "user_data";
    
    private final Preferences prefs;
    private final Gson gson;
    
    private String jwtToken;
    private User currentUser;
    private boolean rememberMe;

    public SessionManager() {
        this.prefs = Preferences.userRoot().node(PREF_NODE);
        this.gson = new Gson();
        
        // Load session dari storage jika ada
        loadSession();
    }

    /**
     * Save session setelah login
     */
    public void saveSession(String token, User user, boolean rememberMe) {
        this.jwtToken = token;
        this.currentUser = user;
        this.rememberMe = rememberMe;
        
        if (rememberMe) {
            // Persist to disk
            prefs.put(KEY_TOKEN, token);
            prefs.put(KEY_USERNAME, user.getUsername());
            prefs.put(KEY_ROLE, user.getRole());
            prefs.put(KEY_USER_DATA, gson.toJson(user));
            prefs.putBoolean(KEY_REMEMBER_ME, true);
        } else {
            // Session only (RAM) - akan hilang saat app close
            prefs.remove(KEY_TOKEN);
            prefs.remove(KEY_USERNAME);
            prefs.remove(KEY_ROLE);
            prefs.remove(KEY_USER_DATA);
            prefs.putBoolean(KEY_REMEMBER_ME, false);
        }
    }

    /**
     * Load session dari storage (auto-login)
     */
    private void loadSession() {
        this.rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false);
        
        if (rememberMe) {
            this.jwtToken = prefs.get(KEY_TOKEN, null);
            String userData = prefs.get(KEY_USER_DATA, null);
            
            if (userData != null) {
                this.currentUser = gson.fromJson(userData, User.class);
            }
        }
    }

    /**
     * Logout - clear session
     */
    public void logout() {
        this.jwtToken = null;
        this.currentUser = null;
        this.rememberMe = false;
        
        // Clear preferences
        prefs.remove(KEY_TOKEN);
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_ROLE);
        prefs.remove(KEY_USER_DATA);
        prefs.putBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Check apakah user sudah login
     */
    public boolean isLoggedIn() {
        return jwtToken != null && currentUser != null;
    }

    /**
     * Check apakah ada saved session (auto-login)
     */
    public boolean hasSavedSession() {
        return rememberMe && jwtToken != null;
    }

    /**
     * Parse JWT token untuk get expiration
     * (Simple parsing - tidak verify signature)
     */
    public boolean isTokenExpired() {
        if (jwtToken == null) return true;
        
        try {
            // JWT format: header.payload.signature
            String[] parts = jwtToken.split("\\.");
            if (parts.length != 3) return true;
            
            // Decode payload (Base64)
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            
            // Parse exp claim (unix timestamp)
            // {"sub":"admin","exp":1700123456,"iat":1700037056}
            int expIndex = payload.indexOf("\"exp\":");
            if (expIndex == -1) return false; // No expiration
            
            String expStr = payload.substring(expIndex + 6);
            int commaIndex = expStr.indexOf(',');
            if (commaIndex != -1) {
                expStr = expStr.substring(0, commaIndex);
            } else {
                expStr = expStr.substring(0, expStr.indexOf('}'));
            }
            
            long exp = Long.parseLong(expStr.trim());
            long now = System.currentTimeMillis() / 1000; // Convert to seconds
            
            return now > exp;
        } catch (Exception e) {
            // Parse error → assume expired
            return true;
        }
    }

    // Getters
    public String getJwtToken() {
        return jwtToken;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public String getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
}
