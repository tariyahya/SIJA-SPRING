package com.smk.presensi.security;

import com.smk.presensi.security.jwt.JwtAuthenticationFilter;
import com.smk.presensi.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration - Konfigurasi keamanan aplikasi.
 * 
 * Class ini mengkonfigurasi:
 * 1. Password encoder (BCrypt)
 * 2. Authentication provider (bagaimana user di-authenticate)
 * 3. HTTP security (endpoint mana yang perlu authentication)
 * 4. JWT filter (custom filter untuk validate token)
 * 5. Method security (untuk @PreAuthorize annotation)
 * 
 * @Configuration: Tandai sebagai configuration class (diload saat startup)
 * @EnableWebSecurity: Enable Spring Security
 * @EnableMethodSecurity: Enable method-level security (@PreAuthorize, @Secured)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Enable @PreAuthorize
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * Constructor injection untuk dependencies.
     */
    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    /**
     * Password Encoder Bean - Untuk hash dan verify password.
     * 
     * BCryptPasswordEncoder menggunakan algoritma BCrypt:
     * - One-way hash (tidak bisa di-decode)
     * - Random salt (password sama, hash beda)
     * - Slow by design (prevent brute force attack)
     * 
     * Strength 10 = 2^10 rounds (default, balance antara security & performance)
     * 
     * @return PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
    
    /**
     * Authentication Provider - Bagaimana user di-authenticate.
     * 
     * DaoAuthenticationProvider:
     * - Load user dari database (via UserDetailsService)
     * - Compare password yang diinput dengan hash di database
     * - Return Authentication object jika cocok
     * 
     * @return DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        // Set UserDetailsService (untuk load user dari database)
        authProvider.setUserDetailsService(userDetailsService);
        
        // Set PasswordEncoder (untuk verify password)
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }
    
    /**
     * Authentication Manager - Manager untuk handle authentication.
     * 
     * Dipakai di AuthController untuk authenticate user saat login.
     * 
     * @param config AuthenticationConfiguration dari Spring
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Security Filter Chain - Konfigurasi HTTP security.
     * 
     * Mengkonfigurasi:
     * 1. CSRF (disable untuk REST API)
     * 2. CORS (enable untuk frontend)
     * 3. Session (stateless untuk JWT)
     * 4. Authorization rules (endpoint mana yang perlu authentication)
     * 5. JWT filter (custom filter sebelum UsernamePasswordAuthenticationFilter)
     * 
     * @param http HttpSecurity builder
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Cross-Site Request Forgery)
                // CSRF protection tidak diperlukan untuk REST API dengan JWT
                // karena JWT di header, bukan di cookie
                .csrf(csrf -> csrf.disable())
                
                // 2. Enable CORS (Cross-Origin Resource Sharing)
                // Allow frontend dari domain berbeda akses API
                .cors(cors -> cors.configure(http))
                
                // 3. Session management: STATELESS
                // Server tidak create session, semua state di JWT token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // 4. Authorization rules: Tentukan endpoint mana yang perlu auth
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (tidak perlu authentication)
                        // Auth endpoints: login (public), register khusus admin
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").hasRole("ADMIN")
                        
                        // H2 Console (untuk development)
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Hello endpoint (untuk testing)
                        .requestMatchers("/api/hello").permitAll()
                        
                        // RFID endpoints (untuk hardware RFID reader)
                        // Hardware tidak bisa login, jadi endpoint harus public
                        .requestMatchers("/api/presensi/rfid/**").permitAll()
                        
                        // Barcode endpoints (untuk barcode reader/smartphone scanner)
                        // Hardware tidak bisa login, jadi endpoint harus public
                        .requestMatchers("/api/presensi/barcode/**").permitAll()
                        
                        // Face Recognition endpoints (untuk camera/face recognition system)
                        // Hardware tidak bisa login, jadi endpoint harus public
                        .requestMatchers("/api/presensi/face/**").permitAll()
                        
                        // Semua endpoint lain PERLU authentication
                        .anyRequest().authenticated()
                )
                
                // 5. Set authentication provider
                .authenticationProvider(authenticationProvider())
                
                // 6. Add JWT filter SEBELUM UsernamePasswordAuthenticationFilter
                // Urutan filter penting!
                // JWT filter akan extract token dan set authentication
                // Baru endpoint akan cek authentication dari SecurityContext
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        
        // Special config untuk H2 Console (development only)
        // H2 Console pakai frames, jadi perlu disable frame options
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build();
    }
}
