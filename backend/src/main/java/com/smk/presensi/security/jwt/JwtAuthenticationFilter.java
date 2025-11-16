package com.smk.presensi.security.jwt;

import com.smk.presensi.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter - Intercept HTTP request untuk validate JWT token.
 * 
 * Filter ini dijalankan SEBELUM request sampai ke controller.
 * 
 * Flow:
 * Request → JwtAuthenticationFilter → Controller
 *           ↓
 *           1. Extract token dari header
 *           2. Validate token
 *           3. Extract username
 *           4. Load user from database
 *           5. Set Authentication ke SecurityContext
 *           6. Continue to next filter/controller
 * 
 * Kenapa extends OncePerRequestFilter?
 * - Guarantee filter hanya dijalankan 1x per request
 * - Prevent duplicate execution (important untuk performance)
 * 
 * @Component: Tandai sebagai Spring component (bean)
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    
    /**
     * Constructor injection.
     * Spring otomatis inject dependencies.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * Method yang dijalankan untuk setiap HTTP request.
     * 
     * Flow:
     * 1. Extract JWT token dari header "Authorization: Bearer <token>"
     * 2. Jika tidak ada token → Skip (continue to next filter)
     * 3. Jika ada token:
     *    a. Validate token (signature, expiration)
     *    b. Extract username dari token
     *    c. Load user dari database
     *    d. Create Authentication object
     *    e. Set Authentication ke SecurityContext
     * 4. Continue to next filter/controller
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain (untuk continue ke next filter)
     * @throws ServletException Jika ada error di filter
     * @throws IOException Jika ada IO error
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // 1. Extract JWT token dari request header
            String jwt = getJwtFromRequest(request);
            
            // 2. Cek apakah token ada dan valid
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                
                // 3. Extract username dari token
                String username = jwtUtil.getUsernameFromToken(jwt);
                
                // 4. Load user details dari database
                // UserDetailsService.loadUserByUsername() akan query database
                // Return UserDetails dengan username, password, roles
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 5. Create Authentication object
                // UsernamePasswordAuthenticationToken adalah implementation of Authentication
                // Constructor dengan 3 parameters = authenticated (sudah verified)
                // Constructor dengan 2 parameters = not authenticated (belum verified)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // Principal (user info)
                                null,                  // Credentials (tidak perlu, sudah authenticated)
                                userDetails.getAuthorities()  // Authorities (roles)
                        );
                
                // 6. Set details dari request (IP address, session ID, dll)
                // Optional, tapi good practice untuk audit log
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 7. Set Authentication ke SecurityContext
                // SecurityContext adalah thread-local storage untuk authentication info
                // Setelah di-set, Spring Security tahu user yang sedang login
                // Controller bisa access via SecurityContextHolder.getContext().getAuthentication()
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("Set authentication for user: {}", username);
            }
            
        } catch (Exception e) {
            // Jika ada error (token invalid, user not found, dll)
            // Log error tapi JANGAN throw exception
            // Let request continue (akan ditolak oleh FilterSecurityInterceptor nanti)
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        // 8. Continue to next filter/controller
        // PENTING: Harus dipanggil untuk continue request processing!
        // Jika tidak dipanggil, request akan stuck (tidak sampai controller)
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token dari request header.
     * 
     * Expected header format:
     * Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
     * 
     * Flow:
     * 1. Get "Authorization" header
     * 2. Check apakah ada dan diawali "Bearer "
     * 3. Extract token (remove "Bearer " prefix)
     * 4. Return token
     * 
     * @param request HTTP request
     * @return JWT token string (tanpa "Bearer " prefix), atau null jika tidak ada
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Get Authorization header
        // Example: "Bearer eyJhbGciOiJIUzUxMiJ9..."
        String bearerToken = request.getHeader("Authorization");
        
        // 2. Check format: harus ada dan diawali "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 3. Extract token (remove "Bearer " prefix)
            // substring(7) → Skip 7 karakter pertama ("Bearer ")
            return bearerToken.substring(7);
        }
        
        // Jika header tidak ada atau format salah, return null
        return null;
    }
}
