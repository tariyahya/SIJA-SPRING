package com.smk.presensi.security.service;

import com.smk.presensi.entity.User;
import com.smk.presensi.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService - Load user dari database untuk Spring Security.
 * 
 * Spring Security butuh cara untuk load user data (username, password, roles).
 * Kita implement UserDetailsService interface untuk provide logic ini.
 * 
 * Interface UserDetailsService punya 1 method:
 * - loadUserByUsername(String username): Load user by username
 * 
 * Method ini dipanggil oleh:
 * 1. DaoAuthenticationProvider saat login (untuk verify password)
 * 2. JwtAuthenticationFilter saat validate token (untuk set authentication)
 * 
 * @Service: Tandai sebagai service component (Spring bean)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Constructor injection.
     * Spring otomatis inject UserRepository bean.
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Load user dari database berdasarkan username.
     * 
     * Method ini dipanggil oleh Spring Security untuk:
     * 1. Login: Load user untuk verify password
     * 2. JWT validation: Load user untuk get roles
     * 
     * Flow:
     * 1. Query database: SELECT * FROM users WHERE username = ?
     * 2. Jika tidak ditemukan → Throw UsernameNotFoundException
     * 3. Jika ditemukan → Convert entity User ke UserDetails
     * 4. Return UserDetails (Spring Security object)
     * 
     * UserDetails interface punya method:
     * - getUsername(): Username untuk login
     * - getPassword(): Password (hashed) untuk verify
     * - getAuthorities(): Roles untuk authorization
     * - isEnabled(): Apakah user aktif
     * - isAccountNonExpired(): Apakah akun expired
     * - isAccountNonLocked(): Apakah akun locked
     * - isCredentialsNonExpired(): Apakah credentials expired
     * 
     * @param username Username user yang mau di-load
     * @return UserDetails object (Spring Security)
     * @throws UsernameNotFoundException Jika user tidak ditemukan
     * 
     * @Transactional: Karena ada lazy loading (fetch roles)
     *                 Meskipun roles EAGER fetch, tetap good practice pakai @Transactional
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Query user dari database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User tidak ditemukan dengan username: " + username)
                );
        
        // 2. Convert roles ke GrantedAuthority
        // Spring Security pakai GrantedAuthority untuk represent permissions
        // Kita convert Set<Role> → Set<GrantedAuthority>
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                // Map Role entity → Role name (String)
                .map(role -> role.getName().toString())
                
                // Map Role name → SimpleGrantedAuthority
                // SimpleGrantedAuthority adalah implementation of GrantedAuthority
                .map(SimpleGrantedAuthority::new)
                
                // Collect to Set
                .collect(Collectors.toSet());
        
        // 3. Create UserDetails object
        // Spring Security provide org.springframework.security.core.userdetails.User class
        // Ini beda dengan entity User kita!
        // 
        // org.springframework.security.core.userdetails.User constructor:
        // - username: Username untuk login
        // - password: Password (hashed) untuk verify
        // - enabled: Apakah user aktif (true = bisa login)
        // - accountNonExpired: Apakah akun expired (true = tidak expired)
        // - credentialsNonExpired: Apakah credentials expired (true = tidak expired)
        // - accountNonLocked: Apakah akun locked (true = tidak locked)
        // - authorities: Roles/permissions
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),           // Username
                user.getPassword(),           // Password (hashed)
                user.isEnabled(),             // Enabled
                true,                         // Account non-expired
                true,                         // Credentials non-expired
                true,                         // Account non-locked
                authorities                   // Authorities (roles)
        );
    }
}
