package com.smk.presensi.controller;

import com.smk.presensi.dto.UserRequest;
import com.smk.presensi.dto.UserResponse;
import com.smk.presensi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk User Management API
 * Endpoint: /api/users
 * 
 * CRUD operations untuk manage users (Admin only)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * GET /api/users
     * Get all users
     * 
     * @PreAuthorize: Only ADMIN can access this endpoint
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.findAll();
    }
    
    /**
     * GET /api/users/{id}
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    /**
     * POST /api/users
     * Create new user
     * 
     * Request body:
     * {
     *   "username": "newuser",
     *   "password": "password123",
     *   "email": "user@example.com",
     *   "role": "ROLE_SISWA"
     * }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * PUT /api/users/{id}
     * Update existing user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody @Valid UserRequest request) {
        return userService.update(id, request);
    }
    
    /**
     * DELETE /api/users/{id}
     * Delete user by ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
