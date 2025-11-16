package com.smk.presensi.controller;

import com.smk.presensi.dto.MessageResponse;
import com.smk.presensi.dto.auth.LoginRequest;
import com.smk.presensi.dto.auth.LoginResponse;
import com.smk.presensi.dto.auth.RegisterRequest;
import com.smk.presensi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk authentication endpoints.
 * 
 * Endpoints:
 * - POST /api/auth/login → Login user
 * - POST /api/auth/register → Register user baru
 * 
 * Semua endpoints di controller ini PUBLIC (tidak perlu authentication).
 * Sudah dikonfigurasi di SecurityConfig: .requestMatchers("/api/auth/**").permitAll()
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Constructor injection.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * ENDPOINT: Login user.
     * 
     * Method: POST
     * URL: /api/auth/login
     * Request Body: LoginRequest (username, password)
     * Response: LoginResponse (token, username, roles)
     * Status Code: 200 OK (success) atau 401 Unauthorized (gagal)
     * 
     * Flow:
     * 1. Client kirim username & password
     * 2. Spring trigger validation (@Valid)
     * 3. AuthService authenticate user
     * 4. Jika berhasil, generate JWT token
     * 5. Return token + user info
     * 
     * Contoh request:
     * POST /api/auth/login
     * {
     *   "username": "admin",
     *   "password": "admin123"
     * }
     * 
     * Contoh response (success):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "type": "Bearer",
     *   "username": "admin",
     *   "roles": ["ROLE_ADMIN"]
     * }
     * 
     * Error handling:
     * - Username/password salah → AuthenticationException → 401 Unauthorized
     * - Validation error (username kosong) → 400 Bad Request
     * 
     * @param request LoginRequest dengan username & password
     * @return ResponseEntity dengan LoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        // Delegate ke service layer
        LoginResponse response = authService.login(request);
        
        // Return 200 OK dengan token
        return ResponseEntity.ok(response);
    }
    
    /**
     * ENDPOINT: Register user baru.
     * 
     * Method: POST
     * URL: /api/auth/register
     * Request Body: RegisterRequest (username, email, password, role)
     * Response: MessageResponse ("User registered successfully")
     * Status Code: 201 Created (success) atau 400 Bad Request (gagal)
     * 
     * Flow:
     * 1. Client kirim data user baru
     * 2. Spring trigger validation (@Valid)
     * 3. AuthService validasi username belum dipakai
     * 4. Hash password dengan BCrypt
     * 5. Assign role (default: ROLE_SISWA)
     * 6. Simpan user ke database
     * 7. Return success message
     * 
     * Contoh request:
     * POST /api/auth/register
     * {
     *   "username": "siswa01",
     *   "email": "siswa01@smk.sch.id",
     *   "password": "password123",
     *   "role": "ROLE_SISWA"
     * }
     * 
     * Contoh response (success):
     * {
     *   "message": "User registered successfully"
     * }
     * 
     * Error handling:
     * - Username sudah dipakai → RuntimeException → 400 Bad Request
     * - Validation error → 400 Bad Request
     * - Role tidak ditemukan → RuntimeException → 400 Bad Request
     * 
     * @param request RegisterRequest dengan data user baru
     * @return ResponseEntity dengan MessageResponse
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody @Valid RegisterRequest request) {
        try {
            // Delegate ke service layer
            authService.register(request);
            
            // Return 201 Created dengan success message
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MessageResponse("User registered successfully"));
                    
        } catch (RuntimeException e) {
            // Username sudah dipakai atau error lain
            // Return 400 Bad Request dengan error message
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}
