package com.smk.presensi.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Utility Class - Helper untuk generate, validate, dan extract JWT token.
 * 
 * Fungsi utama:
 * 1. generateToken() → Create JWT token setelah user login
 * 2. validateToken() → Verify signature dan expiration
 * 3. getUsernameFromToken() → Extract username dari token
 * 
 * JWT Token Structure:
 * Header.Payload.Signature
 * 
 * Example token:
 * eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ.signature
 * 
 * @Component: Tandai sebagai Spring component (bean)
 */
@Component
public class JwtUtil {
    
    /**
     * Logger untuk logging error dan debug info.
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    /**
     * Secret key untuk sign JWT token.
     * 
     * Di-inject dari application.properties: app.jwt.secret
     * 
     * PENTING: Secret key harus:
     * - Panjang minimal 256 bit (32 byte) untuk HS256
     * - Random dan tidak predictable
     * - JANGAN commit ke git (pakai environment variable di production)
     * 
     * Contoh generate secret:
     * openssl rand -base64 64
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    /**
     * Expiration time JWT token (dalam milidetik).
     * 
     * Di-inject dari application.properties: app.jwt.expiration
     * 
     * Default: 86400000 ms = 24 jam
     * 
     * Setelah expiration time, token tidak valid lagi.
     * User harus login ulang untuk dapat token baru.
     */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;
    
    /**
     * Generate JWT token dari Authentication object.
     * 
     * Dipanggil setelah user berhasil login.
     * 
     * Flow:
     * 1. Extract username dari Authentication
     * 2. Extract roles dari Authentication
     * 3. Create JWT payload (claims):
     *    - sub: Username (subject)
     *    - iat: Issued at (waktu token dibuat)
     *    - exp: Expiration (waktu token expired)
     *    - roles: Roles user (custom claim)
     * 4. Sign dengan secret key
     * 5. Return token string
     * 
     * Token structure:
     * {
     *   "sub": "admin",
     *   "iat": 1700000000,
     *   "exp": 1700086400,
     *   "roles": ["ROLE_ADMIN", "ROLE_GURU"]
     * }
     * 
     * @param authentication Authentication object (dari login success)
     * @return JWT token string (eyJhbGciOiJIUzUxMiJ9...)
     */
    public String generateToken(Authentication authentication) {
        // 1. Extract UserDetails dari Authentication
        // Spring Security simpan user info dalam principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 2. Extract username
        String username = userDetails.getUsername();
        
        // 3. Extract roles
        // authorities adalah collection of GrantedAuthority (roles)
        // Convert ke string list: ["ROLE_ADMIN", "ROLE_GURU"]
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        
        // 4. Get current timestamp
        Date now = new Date();
        
        // 5. Calculate expiration date
        // Current time + expiration duration
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        // 6. Create secret key untuk signing
        // Convert string secret ke SecretKey object
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // 7. Build JWT token
        return Jwts.builder()
                // Set subject (username)
                .subject(username)
                
                // Set issued at time (waktu token dibuat)
                .issuedAt(now)
                
                // Set expiration time (waktu token expired)
                .expiration(expiryDate)
                
                // Set custom claim: roles
                // Claim adalah additional data dalam JWT payload
                .claim("roles", roles)
                
                // Sign dengan secret key (algoritma HS512)
                // HS512 = HMAC SHA-512 (symmetric key algorithm)
                .signWith(key, Jwts.SIG.HS512)
                
                // Compact (convert ke string)
                .compact();
    }
    
    /**
     * Extract username dari JWT token.
     * 
     * Dipanggil di JwtAuthenticationFilter untuk get username dari token.
     * 
     * Flow:
     * 1. Parse JWT token
     * 2. Verify signature (jika signature tidak valid, throw exception)
     * 3. Extract subject claim (username)
     * 4. Return username
     * 
     * @param token JWT token string
     * @return Username (subject claim)
     * @throws JwtException Jika token invalid atau expired
     */
    public String getUsernameFromToken(String token) {
        // 1. Create secret key untuk verify signature
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // 2. Parse JWT token
        // verifyWith(key): Verify signature dengan secret key
        // build(): Build parser
        // parseSignedClaims(token): Parse token dan extract claims
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        // 3. Get subject claim (username)
        return claims.getSubject();
    }
    
    /**
     * Validate JWT token.
     * 
     * Cek apakah token valid:
     * 1. Signature cocok (tidak diubah)
     * 2. Belum expired
     * 3. Format token benar
     * 
     * Dipanggil di JwtAuthenticationFilter sebelum set authentication.
     * 
     * @param token JWT token string
     * @return true jika valid, false jika invalid
     */
    public boolean validateToken(String token) {
        try {
            // Create secret key
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            // Parse dan verify token
            // Jika ada error, akan throw exception (caught di catch block)
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            
            // Jika sampai sini, token valid
            return true;
            
        } catch (SignatureException e) {
            // Signature tidak cocok (token diubah atau secret key salah)
            logger.error("Invalid JWT signature: {}", e.getMessage());
            
        } catch (MalformedJwtException e) {
            // Format token salah (bukan JWT yang valid)
            logger.error("Invalid JWT token: {}", e.getMessage());
            
        } catch (ExpiredJwtException e) {
            // Token sudah expired (melewati expiration time)
            logger.error("JWT token is expired: {}", e.getMessage());
            
        } catch (UnsupportedJwtException e) {
            // Token type tidak didukung
            logger.error("JWT token is unsupported: {}", e.getMessage());
            
        } catch (IllegalArgumentException e) {
            // Token string kosong atau null
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        
        // Jika ada exception, return false (token invalid)
        return false;
    }
    
    /**
     * Extract roles dari JWT token.
     * 
     * Helper method untuk get roles dari custom claim.
     * 
     * @param token JWT token string
     * @return Roles string (comma-separated: "ROLE_ADMIN,ROLE_GURU")
     */
    public String getRolesFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        // Get custom claim "roles"
        return claims.get("roles", String.class);
    }
}
