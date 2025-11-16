package com.smk.presensi.service;

import com.smk.presensi.dto.auth.LoginRequest;
import com.smk.presensi.dto.auth.LoginResponse;
import com.smk.presensi.dto.auth.RegisterRequest;
import com.smk.presensi.entity.Role;
import com.smk.presensi.entity.Role.RoleName;
import com.smk.presensi.entity.User;
import com.smk.presensi.repository.RoleRepository;
import com.smk.presensi.repository.UserRepository;
import com.smk.presensi.security.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service untuk handle authentication logic.
 * 
 * Fungsi:
 * 1. Register user baru
 * 2. Login user (authenticate & generate token)
 * 3. Validasi username availability
 */
@Service
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * Constructor injection.
     */
    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Login user.
     * 
     * Flow:
     * 1. Authenticate user (compare password dengan hash di database)
     * 2. Jika berhasil, generate JWT token
     * 3. Extract roles dari Authentication object
     * 4. Return LoginResponse (token + user info)
     * 
     * @param request LoginRequest (username + password)
     * @return LoginResponse (token + user info)
     * @throws RuntimeException Jika authentication gagal (username/password salah)
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Authenticate user
        // AuthenticationManager akan:
        // - Load user dari database (via UserDetailsService)
        // - Compare password dengan hash di database (via PasswordEncoder)
        // - Throw AuthenticationException jika gagal
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        
        // 2. Set authentication ke SecurityContext
        // (Opsional, tapi good practice)
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 3. Generate JWT token
        String token = jwtUtil.generateToken(authentication);
        
        // 4. Extract roles dari Authentication
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        // 5. Return LoginResponse
        return new LoginResponse(
                token,
                request.username(),
                roles
        );
    }
    
    /**
     * Register user baru.
     * 
     * Flow:
     * 1. Validasi username belum dipakai
     * 2. Hash password dengan BCrypt
     * 3. Assign role (default: ROLE_SISWA)
     * 4. Simpan user ke database
     * 
     * @param request RegisterRequest (username, email, password, role)
     * @throws RuntimeException Jika username sudah dipakai
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Validasi username
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username sudah digunakan!");
        }
        
        // 2. Create User entity
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        
        // 3. Hash password dengan BCrypt
        // JANGAN PERNAH simpan password plain text!
        String hashedPassword = passwordEncoder.encode(request.password());
        user.setPassword(hashedPassword);
        
        // 4. Assign role
        // Default: ROLE_SISWA
        // Jika request.role() tidak null, pakai role tersebut
        String roleName = request.role() != null ? request.role() : "ROLE_SISWA";
        RoleName roleEnum = RoleName.valueOf(roleName);
        
        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role tidak ditemukan: " + roleName));
        
        user.addRole(role);
        
        // 5. Simpan user ke database
        userRepository.save(user);
    }
    
    /**
     * Cek apakah username tersedia (belum dipakai).
     * 
     * Dipakai untuk validasi real-time di frontend.
     * 
     * @param username Username yang mau dicek
     * @return true jika tersedia, false jika sudah dipakai
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
}
