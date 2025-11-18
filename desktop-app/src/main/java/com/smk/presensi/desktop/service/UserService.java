package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smk.presensi.desktop.model.User;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;

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
        HttpResponse<String> response = apiClient.get("/api/users");
        
        if (response.statusCode() == 200) {
            Type listType = new TypeToken<List<User>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        } else {
            throw new IOException("Failed to fetch users: " + response.statusCode());
        }
    }
    
    /**
     * Create new user
     */
    public User createUser(User user) throws IOException, InterruptedException {
        String json = gson.toJson(user);
        HttpResponse<String> response = apiClient.post("/api/users", json);
        
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return gson.fromJson(response.body(), User.class);
        } else {
            throw new IOException("Failed to create user: " + response.statusCode());
        }
    }
    
    /**
     * Update existing user
     */
    public User updateUser(Long id, User user) throws IOException, InterruptedException {
        String json = gson.toJson(user);
        HttpResponse<String> response = apiClient.put("/api/users/" + id, json);
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), User.class);
        } else {
            throw new IOException("Failed to update user: " + response.statusCode());
        }
    }
    
    /**
     * Delete user
     */
    public void deleteUser(Long id) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.delete("/api/users/" + id);
        
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new IOException("Failed to delete user: " + response.statusCode());
        }
    }
}
