package com.smk.presensi.mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "presensi_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    
    private SharedPreferences prefs;
    
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public void saveToken(String token, String username) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USERNAME, username)
                .apply();
    }
    
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }
    
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "User");
    }
    
    public boolean isLoggedIn() {
        return getToken() != null;
    }
    
    public void logout() {
        prefs.edit().clear().apply();
    }
}
