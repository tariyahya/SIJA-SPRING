package com.smk.presensi.desktop.viewmodel;

import com.google.gson.Gson;
import com.smk.presensi.desktop.model.User;
import com.smk.presensi.desktop.service.ApiClient;
import com.smk.presensi.desktop.service.SessionManager;
import javafx.beans.property.*;

import java.net.http.HttpResponse;
import java.util.Map;

/**
 * LoginViewModel - Business logic untuk login screen
 */
public class LoginViewModel {
    private final ApiClient apiClient;
    private final SessionManager sessionManager;
    private final Gson gson;

    // Observable properties
    private final StringProperty username;
    private final StringProperty password;
    private final BooleanProperty rememberMe;
    private final BooleanProperty loading;
    private final StringProperty errorMessage;
    private final BooleanProperty loginSuccess;

    public LoginViewModel(ApiClient apiClient, SessionManager sessionManager) {
        this.apiClient = apiClient;
        this.sessionManager = sessionManager;
        this.gson = new Gson();

        // Initialize properties
        this.username = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
        this.rememberMe = new SimpleBooleanProperty(false);
        this.loading = new SimpleBooleanProperty(false);
        this.errorMessage = new SimpleStringProperty("");
        this.loginSuccess = new SimpleBooleanProperty(false);
    }

    /**
     * Login dengan username & password
     */
    public void login() {
        // Validation
        if (username.get().trim().isEmpty()) {
            errorMessage.set("Username tidak boleh kosong");
            return;
        }
        if (password.get().trim().isEmpty()) {
            errorMessage.set("Password tidak boleh kosong");
            return;
        }

        loading.set(true);
        errorMessage.set("");

        // Background thread untuk network request
        new Thread(() -> {
            try {
                // Call backend API
                boolean success = apiClient.login(username.get().trim(), password.get());

                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        // Get user info dari token atau API
                        User user = getUserInfo();
                        
                        // Save session
                        sessionManager.saveSession(
                            apiClient.getJwtToken(),
                            user,
                            rememberMe.get()
                        );

                        // Set success flag
                        loginSuccess.set(true);
                    } else {
                        errorMessage.set("Username atau password salah");
                    }
                    loading.set(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    errorMessage.set("Error: " + e.getMessage());
                    loading.set(false);
                });
            }
        }).start();
    }

    /**
     * Get user info dari backend API
     */
    private User getUserInfo() {
        try {
            HttpResponse<String> response = apiClient.get("/auth/me");
            
            if (response.statusCode() == 200) {
                // Parse response
                Map<String, Object> data = gson.fromJson(response.body(), Map.class);
                
                User user = new User();
                user.setId(((Double) data.get("id")).longValue());
                user.setUsername((String) data.get("username"));
                user.setNama((String) data.get("nama"));
                user.setEmail((String) data.get("email"));
                user.setRole((String) data.get("role"));
                
                return user;
            }
        } catch (Exception e) {
            System.err.println("Failed to get user info: " + e.getMessage());
        }

        // Fallback: create basic user from username
        User user = new User();
        user.setUsername(username.get());
        user.setRole("ADMIN"); // Default
        return user;
    }

    /**
     * Auto-login dengan saved session
     */
    public boolean autoLogin() {
        if (sessionManager.hasSavedSession() && !sessionManager.isTokenExpired()) {
            // Restore token ke ApiClient
            apiClient.setJwtToken(sessionManager.getJwtToken());
            loginSuccess.set(true);
            return true;
        }
        return false;
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.set("");
    }

    // Property getters
    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public BooleanProperty rememberMeProperty() {
        return rememberMe;
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public BooleanProperty loginSuccessProperty() {
        return loginSuccess;
    }

    // Convenience setters
    public void setUsername(String username) {
        this.username.set(username);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe.set(rememberMe);
    }
}
