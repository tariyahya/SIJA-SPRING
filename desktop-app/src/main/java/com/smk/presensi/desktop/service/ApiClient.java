package com.smk.presensi.desktop.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * HTTP Client untuk komunikasi dengan Backend API
 * Handles authentication (JWT), request/response, error handling
 * Uses Singleton pattern to share JWT token across the application
 */
public class ApiClient {
    private static final String BASE_URL = "http://localhost:8081/api";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    
    private static ApiClient instance;
    
    private final HttpClient httpClient;
    private final Gson gson;
    private String jwtToken;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();

        // Custom Gson with LocalDate/LocalTime adapters
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, 
                    (JsonDeserializer<LocalDate>) (json, type, context) -> 
                        LocalDate.parse(json.getAsString()))
                .registerTypeAdapter(LocalTime.class,
                    (JsonDeserializer<LocalTime>) (json, type, context) -> 
                        LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_TIME))
                .create();
    }
    
    /**
     * Get singleton instance of ApiClient
     */
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    /**
     * Login dan simpan JWT token
     */
    public boolean login(String username, String password) throws IOException, InterruptedException {
        String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", 
                                       username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(TIMEOUT)
                .build();

        HttpResponse<String> response = httpClient.send(request, 
                                                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse token dari response
            var jsonResponse = gson.fromJson(response.body(), LoginResponse.class);
            this.jwtToken = jsonResponse.token;
            return true;
        }

        return false;
    }

    /**
     * GET request dengan JWT token
     */
    public HttpResponse<String> get(String endpoint) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .GET();

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * GET request for binary data (e.g. file download)
     */
    public HttpResponse<byte[]> getBinary(String endpoint) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(TIMEOUT)
                .GET();

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    /**
     * POST request dengan JWT token
     */
    public HttpResponse<String> post(String endpoint, String jsonBody) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * PUT request dengan JWT token
     */
    public HttpResponse<String> put(String endpoint, String jsonBody) 
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * DELETE request dengan JWT token
     */
    public HttpResponse<String> delete(String endpoint) 
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .DELETE();

        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Parse JSON response ke Java object
     */
    public <T> T parseResponse(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * Convert Java object ke JSON string
     */
    public String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
    
    public HttpClient getHttpClient() {
        return httpClient;
    }
    
    public String getBaseUrl() {
        return BASE_URL;
    }
    
    // Inner class untuk parse login response
    private static class LoginResponse {
        String token;
        String username;
        String role;
    }
}
