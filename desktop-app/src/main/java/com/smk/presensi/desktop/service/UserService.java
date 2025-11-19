package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.User;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service untuk User Management (CRUD operations)
 * Admin-only feature untuk manage users
 */
public class UserService {
    
    private final ApiClient apiClient;
    private final Gson gson;
    
    public UserService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new Gson();
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() throws IOException, InterruptedException {
        // Backend base URL sudah /api, jadi endpoint cukup /users
        HttpResponse<String> response = apiClient.get("/users");

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch users: " + response.statusCode());
        }

        Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
        List<Map<String, Object>> rawList = gson.fromJson(response.body(), listType);

        List<User> users = new ArrayList<>();
        if (rawList != null) {
            for (Map<String, Object> item : rawList) {
                users.add(mapToUser(item));
            }
        }
        return users;
    }
    
    /**
     * Create new user
     */
    public User createUser(User user) throws IOException, InterruptedException {
        String json = gson.toJson(user);
        HttpResponse<String> response = apiClient.post("/users", json);
        
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> map = gson.fromJson(response.body(), type);
            return mapToUser(map);
        }

        throw new IOException("Failed to create user: " + response.statusCode());
    }
    
    /**
     * Update existing user
     */
    public User updateUser(Long id, User user) throws IOException, InterruptedException {
        String json = gson.toJson(user);
        HttpResponse<String> response = apiClient.put("/users/" + id, json);
        
        if (response.statusCode() == 200) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> map = gson.fromJson(response.body(), type);
            return mapToUser(map);
        }

        throw new IOException("Failed to update user: " + response.statusCode());
    }
    
    /**
     * Delete user
     */
    public void deleteUser(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.delete("/users/" + id);
        
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new IOException("Failed to delete user: " + response.statusCode());
        }
    }

    /**
     * Map JSON response (UserResponse) ke model User desktop.
     */
    @SuppressWarnings("unchecked")
    private User mapToUser(Map<String, Object> json) {
        User user = new User();

        Object idObj = json.get("id");
        if (idObj instanceof Number number) {
            user.setId(number.longValue());
        }

        user.setUsername((String) json.get("username"));
        user.setEmail((String) json.get("email"));

        Object enabledObj = json.get("enabled");
        if (enabledObj instanceof Boolean bool) {
            user.setEnabled(bool);
        }

        // Ambil primary role dari array roles (jika ada)
        Object rolesObj = json.get("roles");
        if (rolesObj instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first != null) {
                user.setRole(first.toString());
            }
        }

        // Optional: map createdAt jika backend menambahkannya di masa depan
        Object createdAtObj = json.get("createdAt");
        if (createdAtObj instanceof String createdAtStr) {
            try {
                user.setCreatedAt(LocalDateTime.parse(createdAtStr));
            } catch (DateTimeParseException ignored) {
                // biarkan null jika format tidak cocok
            }
        }

        return user;
    }
}
