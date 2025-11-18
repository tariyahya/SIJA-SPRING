package com.smk.presensi.service;

import com.smk.presensi.dto.UserRequest;
import com.smk.presensi.dto.UserResponse;
import com.smk.presensi.entity.Role;
import com.smk.presensi.entity.User;
import com.smk.presensi.repository.RoleRepository;
import com.smk.presensi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service untuk User Management CRUD
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, 
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Get all users
     */
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }
    
    /**
     * Get user by ID
     */
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User dengan ID " + id + " tidak ditemukan"));
        return toResponse(user);
    }
    
    /**
     * Create new user
     */
    public UserResponse create(UserRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username " + request.username() + " sudah digunakan");
        }
        
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password())); // Hash password
        user.setEmail(request.email());
        user.setEnabled(true);
        
        // Assign role
        Set<Role> roles = new HashSet<>();
        if (request.role() != null) {
            Role.RoleName roleName = Role.RoleName.valueOf(request.role());
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role " + request.role() + " tidak ditemukan"));
            roles.add(role);
        } else {
            // Default role: SISWA
            Role role = roleRepository.findByName(Role.RoleName.ROLE_SISWA)
                    .orElseThrow(() -> new RuntimeException("Role ROLE_SISWA tidak ditemukan"));
            roles.add(role);
        }
        user.setRoles(roles);
        
        userRepository.save(user);
        return toResponse(user);
    }
    
    /**
     * Update existing user
     */
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User dengan ID " + id + " tidak ditemukan"));
        
        // Check if new username already exists (excluding current user)
        if (!user.getUsername().equals(request.username()) 
                && userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username " + request.username() + " sudah digunakan");
        }
        
        user.setUsername(request.username());
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setEmail(request.email());
        
        // Update role if provided
        if (request.role() != null) {
            Set<Role> roles = new HashSet<>();
            Role.RoleName roleName = Role.RoleName.valueOf(request.role());
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role " + request.role() + " tidak ditemukan"));
            roles.add(role);
            user.setRoles(roles);
        }
        
        userRepository.save(user);
        return toResponse(user);
    }
    
    /**
     * Delete user by ID
     */
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User dengan ID " + id + " tidak ditemukan");
        }
        userRepository.deleteById(id);
    }
    
    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                roleNames,
                null, // siswaId - TODO: implement if needed
                null  // guruId - TODO: implement if needed
        );
    }
}
