# TASK 3: Implementasi Authentication & Authorization dengan JWT

## Tujuan Pembelajaran
Setelah menyelesaikan tahap ini, siswa mampu:
- Memahami konsep Authentication (siapa Anda?) vs Authorization (apa yang boleh Anda lakukan?)
- Mengimplementasikan JWT (JSON Web Token) untuk stateless authentication
- Menggunakan Spring Security untuk protect endpoints
- Membuat sistem role-based access control (ADMIN, GURU, SISWA)
- Hash password dengan BCrypt untuk keamanan
- Membuat login & register endpoints

---

## A. Pengenalan Konsep

### 1. Authentication vs Authorization

**Authentication (Autentikasi)** = **SIAPA Anda?**
- Proses verifikasi identitas user
- Contoh: Login dengan username & password
- Output: Token yang membuktikan identitas

**Authorization (Otorisasi)** = **APA yang boleh Anda lakukan?**
- Proses verifikasi hak akses user
- Contoh: Admin bisa delete, Siswa cuma bisa read
- Output: Allowed atau Denied

**Analogi:**
- **Authentication** = Menunjukkan KTP di gerbang masuk sekolah (membuktikan Anda siswa disini)
- **Authorization** = Setelah masuk, siswa tidak boleh masuk ruang guru (cek hak akses)

### 2. Kenapa JWT?

**Masalah Session-based Authentication:**
```
Client login → Server buat session → Simpan di memory
Client request → Kirim session ID → Server cek session di memory
```

**Masalah:**
- Server harus simpan session semua user (memory intensive)
- Susah scale ke multiple servers (session tidak ter-share)
- Server harus "ingat" setiap user (stateful)

**Solusi: JWT (Stateless Authentication)**
```
Client login → Server generate JWT token → Kirim ke client
Client request → Kirim JWT token → Server verify token (tanpa cek database)
```

**Keuntungan:**
- Server tidak perlu simpan session (stateless)
- Token bisa di-verify tanpa database query
- Mudah scale ke multiple servers
- Token bisa contain data (user id, roles, dll)

### 3. Struktur JWT Token

JWT token terdiri dari 3 bagian: `Header.Payload.Signature`

**Contoh JWT token:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ.K8JxJPkk5YVK5Rh7jXz8N9qH5f8lW3mR2Yw5Nz0Qz1M
```

**Decoded:**
```json
{
  "header": {
    "alg": "HS256",  // Algorithm: HMAC SHA256
    "typ": "JWT"     // Type: JWT
  },
  "payload": {
    "sub": "admin",              // Subject: username
    "iat": 1700000000,           // Issued At: kapan token dibuat
    "exp": 1700086400,           // Expiration: kapan token kadaluarsa
    "roles": ["ROLE_ADMIN"]      // Custom data: roles user
  },
  "signature": "K8Jx..."  // Signature untuk verify token tidak diubah
}
```

**Cara kerja:**
1. Server generate token dengan SECRET_KEY
2. Client simpan token (localStorage atau cookie)
3. Setiap request, client kirim token di header: `Authorization: Bearer <token>`
4. Server verify signature dengan SECRET_KEY yang sama
5. Jika valid, extract username & roles dari payload
6. Set authentication context Spring Security
7. Endpoint bisa access user info dari SecurityContext

### 4. Password Hashing dengan BCrypt

**PENTING**: Jangan PERNAH simpan password plain text!

**Salah:**
```
Username: admin
Password: admin123   ← Kalau database bocor, password ketahuan!
```

**Benar:**
```
Username: admin
Password: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
         ↑ BCrypt hash (irreversible, tidak bisa di-decode balik)
```

**Cara kerja BCrypt:**
```
Input: "admin123"
BCrypt hash → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

**Verifikasi password:**
```java
String inputPassword = "admin123";
String storedHash = "$2a$10$N9qo8uLO...";
boolean match = BCrypt.checkpw(inputPassword, storedHash);  // true
```

**Keuntungan:**
- Tidak bisa di-decode (one-way hash)
- Sama password, beda hash (karena salt random)
- Lambat (by design, prevent brute force)

---

## B. Step 1: Tambah Dependencies

Buka `backend/pom.xml`, tambahkan dependencies berikut di dalam `<dependencies>`:

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT Library (jjwt) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

**Penjelasan dependencies:**
- `spring-boot-starter-security`: Framework security Spring (filter, authentication, authorization)
- `jjwt-api`: Interface JWT (classes & methods)
- `jjwt-impl`: Implementation JWT (logic generate & verify token)
- `jjwt-jackson`: JSON parser untuk JWT payload

**Setelah tambah, reload Maven:**
- IntelliJ: Klik icon Maven ⟳ (Reload All Maven Projects)
- VS Code: Tunggu auto-reload atau Ctrl+Shift+P → "Java: Clean Java Language Server Workspace"

---

## C. Step 2: Buat Entity User & Role

### 2.1. Role Entity

Buat file `backend/src/main/java/com/smk/presensi/entity/Role.java`:

```java
package com.smk.presensi.entity;

import jakarta.persistence.*;

/**
 * Entity ROLE - Representasi peran/hak akses user dalam sistem.
 * 
 * Dalam sistem presensi ada 3 role:
 * - ADMIN: Full access (CRUD semua data, manage users)
 * - GURU: Baca semua data, manage presensi kelas yang diajar
 * - SISWA: Baca data sendiri, melakukan presensi
 * 
 * Role ini akan dipakai untuk authorization (cek hak akses).
 * Contoh: Endpoint delete siswa hanya bisa diakses ADMIN.
 */
@Entity
@Table(name = "roles")
public class Role {
    
    /**
     * ID role (Primary Key, auto-increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nama role, menggunakan ENUM.
     * Harus unique (tidak boleh ada 2 role dengan nama sama).
     * 
     * Nilai yang diperbolehkan: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA
     * Prefix "ROLE_" adalah konvensi Spring Security (wajib!).
     */
    @Enumerated(EnumType.STRING)  // Simpan sebagai string, bukan integer
    @Column(nullable = false, unique = true, length = 20)
    private RoleName name;
    
    // Constructors
    
    /**
     * No-args constructor (wajib untuk JPA).
     * JPA butuh constructor kosong untuk create instance saat query.
     */
    public Role() {}
    
    /**
     * Constructor dengan parameter name (untuk kemudahan create role).
     */
    public Role(RoleName name) {
        this.name = name;
    }
    
    // Getters & Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public RoleName getName() {
        return name;
    }
    
    public void setName(RoleName name) {
        this.name = name;
    }
    
    /**
     * ENUM untuk nama role.
     * 
     * Kenapa pakai ENUM, bukan String biasa?
     * - Type-safe: Tidak bisa salah ketik (IDE auto-complete)
     * - Validation: Hanya nilai yang ada di ENUM yang diperbolehkan
     * - Maintainable: Kalau tambah role baru, cukup tambah di ENUM
     * 
     * Prefix ROLE_ adalah konvensi Spring Security (jangan dihapus!).
     */
    public enum RoleName {
        ROLE_ADMIN,   // Administrator sistem
        ROLE_GURU,    // Guru/pengajar
        ROLE_SISWA    // Siswa
    }
}
```

### 2.2. User Entity

Buat file `backend/src/main/java/com/smk/presensi/entity/User.java`:

```java
package com.smk.presensi.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity USER - Representasi user yang bisa login ke sistem.
 * 
 * User ini akan dipakai untuk authentication (login).
 * Setiap Siswa/Guru akan punya 1 User untuk login.
 * 
 * Relasi:
 * - User <-> Siswa (One-to-One, opsional)
 * - User <-> Guru (One-to-One, opsional)
 * - User <-> Role (Many-to-Many, 1 user bisa punya banyak role)
 */
@Entity
@Table(name = "users")
public class User {
    
    /**
     * ID user (Primary Key, auto-increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Username untuk login (unique, tidak boleh duplikat).
     * Contoh: "admin", "guru_budi", "siswa_2024001"
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * Password yang sudah di-hash dengan BCrypt.
     * JANGAN PERNAH simpan password plain text!
     * 
     * Contoh BCrypt hash:
     * Plain: "admin123"
     * Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * 
     * Length 100 karena BCrypt hash lumayan panjang.
     */
    @Column(nullable = false, length = 100)
    private String password;
    
    /**
     * Email user (opsional, untuk reset password nanti).
     */
    @Column(length = 100)
    private String email;
    
    /**
     * Status aktif/non-aktif user.
     * false = user di-disable (tidak bisa login).
     * true = user aktif (bisa login).
     */
    @Column(nullable = false)
    private boolean enabled = true;
    
    /**
     * Relasi Many-to-Many dengan Role.
     * 
     * 1 User bisa punya banyak Role (contoh: user "admin" punya ROLE_ADMIN + ROLE_GURU)
     * 1 Role bisa dimiliki banyak User (contoh: ROLE_SISWA dimiliki semua siswa)
     * 
     * @ManyToMany: Relasi many-to-many
     * @JoinTable: Buat tabel join (user_roles) untuk simpan relasi
     * - name: Nama tabel join
     * - joinColumns: Foreign key ke tabel User (user_id)
     * - inverseJoinColumns: Foreign key ke tabel Role (role_id)
     * 
     * FetchType.EAGER: Load roles sekalian saat load user
     * (Biasanya pakai LAZY, tapi untuk roles kita perlu eager karena sering dipakai)
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",  // Nama tabel join
        joinColumns = @JoinColumn(name = "user_id"),  // FK ke User
        inverseJoinColumns = @JoinColumn(name = "role_id")  // FK ke Role
    )
    private Set<Role> roles = new HashSet<>();
    
    // Constructors
    
    /**
     * No-args constructor (wajib untuk JPA).
     */
    public User() {}
    
    /**
     * Constructor untuk kemudahan create user baru.
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    // Getters & Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    /**
     * Helper method untuk tambah role ke user.
     * Ini lebih convenient daripada getRoles().add()
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }
}
```

---

## D. Step 3: Buat Repository

### 3.1. RoleRepository

Buat file `backend/src/main/java/com/smk/presensi/repository/RoleRepository.java`:

```java
package com.smk.presensi.repository;

import com.smk.presensi.entity.Role;
import com.smk.presensi.entity.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository untuk akses data Role.
 * 
 * Fungsi utama: Cari role berdasarkan nama (untuk assign role ke user).
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Cari role berdasarkan nama (ROLE_ADMIN, ROLE_GURU, ROLE_SISWA).
     * 
     * Dipakai saat:
     * - Register user baru → assign role default
     * - Seeding data → create roles jika belum ada
     * 
     * Return Optional karena role bisa tidak ditemukan.
     */
    Optional<Role> findByName(RoleName name);
}
```

### 3.2. UserRepository

Buat file `backend/src/main/java/com/smk/presensi/repository/UserRepository.java`:

```java
package com.smk.presensi.repository;

import com.smk.presensi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository untuk akses data User.
 * 
 * Fungsi utama:
 * - Cari user berdasarkan username (untuk login)
 * - Cek apakah username sudah dipakai (untuk register)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Cari user berdasarkan username.
     * 
     * Dipakai saat:
     * - Login: Cari user dengan username yang diinput
     * - JWT Filter: Load user dari token
     * 
     * Return Optional<User> karena bisa tidak ditemukan.
     * 
     * Spring Data JPA otomatis generate query:
     * SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Cek apakah username sudah dipakai.
     * 
     * Dipakai saat:
     * - Register: Validasi username belum ada yang pakai
     * 
     * Return boolean:
     * - true: Username sudah ada (tidak bisa dipakai)
     * - false: Username belum ada (bisa dipakai)
     * 
     * Spring Data JPA otomatis generate query:
     * SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);
}
```

---

## E. Step 4: Konfigurasi JWT Properties

Buka `backend/src/main/resources/application.properties`, tambahkan konfigurasi JWT:

```properties
# ==========================================
# JWT Configuration
# ==========================================

# Secret key untuk sign JWT token
# PENTING: Ganti ini dengan random string yang panjang di production!
# Cara generate: openssl rand -base64 64
app.jwt.secret=SIJASpringBootSecretKeyForJWTToken2024VeryLongAndSecureDoNotShareThisKey

# Expiration time JWT token (dalam milidetik)
# 86400000 ms = 24 jam
# Setelah 24 jam, token kadaluarsa dan user harus login lagi
app.jwt.expiration=86400000
```

**Penjelasan:**
- `app.jwt.secret`: Key untuk sign token (harus rahasia!)
  - Kalau orang lain tahu secret ini, mereka bisa generate token palsu
  - Di production, simpan di environment variable, bukan hardcode
- `app.jwt.expiration`: Berapa lama token valid (24 jam = 1 hari)
  - Terlalu pendek → user sering harus login lagi (annoying)
  - Terlalu panjang → security risk (kalau token dicuri, valid lama)

---

## F. Step 5: Buat JWT Utility Class

Buat package baru: `backend/src/main/java/com/smk/presensi/security/jwt/`

Buat file `JwtUtil.java` di package tersebut:

```java
package com.smk.presensi.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class untuk generate dan validate JWT token.
 * 
 * Fungsi utama:
 * 1. generateToken() - Buat token saat user login
 * 2. getUsernameFromToken() - Extract username dari token
 * 3. validateToken() - Cek apakah token valid (tidak kadaluarsa, signature benar)
 * 
 * Token structure: Header.Payload.Signature
 * - Header: Algorithm (HS512)
 * - Payload: Username, issued time, expiration time
 * - Signature: HMAC SHA-512 dengan secret key
 */
@Component
public class JwtUtil {
    
    // Logger untuk log error (jika token invalid, expired, dll)
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    /**
     * Secret key untuk sign token (dari application.properties).
     * @Value inject value dari config file ke field ini.
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    /**
     * Expiration time dalam milidetik (dari application.properties).
     * Default: 86400000 ms = 24 jam.
     */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;
    
    /**
     * Generate JWT token dari Authentication object.
     * 
     * Dipanggil saat user berhasil login.
     * 
     * Flow:
     * 1. Extract username dari Authentication
     * 2. Set subject (username)
     * 3. Set issued time (kapan token dibuat)
     * 4. Set expiration time (kapan token kadaluarsa)
     * 5. Sign dengan secret key
     * 6. Return token string
     * 
     * @param authentication Object dari Spring Security (berisi user info)
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        // Extract username dari Authentication object
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // Get current time
        Date now = new Date();
        // Calculate expiration time (current time + expiration duration)
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        // Build JWT token
        return Jwts.builder()
                .subject(username)               // Set subject (username)
                .issuedAt(now)                  // Set issued time
                .expiration(expiryDate)         // Set expiration time
                .signWith(getSigningKey())      // Sign with secret key
                .compact();                     // Build dan return token string
    }
    
    /**
     * Generate JWT token dari username (alternative method).
     * 
     * Dipakai kalau kita sudah punya username (tidak perlu Authentication object).
     * 
     * @param username Username yang akan di-embed di token
     * @return JWT token string
     */
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Extract username dari JWT token.
     * 
     * Dipanggil di JWT Filter untuk:
     * 1. Extract username dari token di request header
     * 2. Load user detail dari database
     * 3. Set authentication context
     * 
     * Flow:
     * 1. Parse token
     * 2. Verify signature dengan secret key
     * 3. Extract claims (payload)
     * 4. Get subject (username)
     * 
     * @param token JWT token string
     * @return Username dari token
     */
    public String getUsernameFromToken(String token) {
        // Parse token, verify signature, extract claims
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())    // Verify signature dengan secret key
                .build()
                .parseSignedClaims(token)        // Parse token
                .getPayload();                   // Extract payload (claims)
        
        // Get subject (username) dari claims
        return claims.getSubject();
    }
    
    /**
     * Validate JWT token.
     * 
     * Dipanggil di JWT Filter untuk cek apakah token valid.
     * 
     * Token valid jika:
     * 1. Signature benar (tidak diubah)
     * 2. Belum kadaluarsa (exp > current time)
     * 3. Format token benar
     * 
     * @param token JWT token string
     * @return true jika token valid, false jika invalid
     */
    public boolean validateToken(String token) {
        try {
            // Parse dan verify token
            // Jika ada error, throw exception dan catch di bawah
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            
            // Jika sampai sini, berarti token valid
            return true;
            
        } catch (SecurityException ex) {
            // Signature tidak cocok (token diubah atau secret key salah)
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            // Format token salah (bukan JWT yang valid)
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // Token sudah kadaluarsa (expired)
            logger.error("JWT token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // JWT token format tidak supported
            logger.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // JWT claims string kosong
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        
        // Jika ada exception, return false (token tidak valid)
        return false;
    }
    
    /**
     * Get signing key dari secret string.
     * 
     * Secret key harus dalam format SecretKey untuk jjwt library.
     * 
     * Flow:
     * 1. Decode base64 secret string
     * 2. Convert ke SecretKey (HMAC SHA-512)
     * 
     * @return SecretKey untuk sign dan verify token
     */
    private SecretKey getSigningKey() {
        // Decode base64 string ke byte array
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        // Convert byte array ke SecretKey (HMAC SHA algorithm)
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

---

## G. Step 6: Implement UserDetailsService

Buat package: `backend/src/main/java/com/smk/presensi/security/service/`

Buat file `CustomUserDetailsService.java`:

```java
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
 * Custom implementation UserDetailsService untuk Spring Security.
 * 
 * UserDetailsService adalah interface yang dipakai Spring Security
 * untuk load user dari database saat authentication.
 * 
 * Method yang harus diimplement: loadUserByUsername(String username)
 * 
 * Dipanggil saat:
 * 1. User login → AuthenticationManager akan call method ini
 * 2. JWT Filter → Load user dari username di token
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    // Dependency: UserRepository untuk query database
    private final UserRepository userRepository;
    
    /**
     * Constructor injection (best practice).
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Load user dari database berdasarkan username.
     * 
     * Method ini dipanggil oleh Spring Security saat authentication.
     * 
     * Flow:
     * 1. Cari user di database berdasarkan username
     * 2. Jika tidak ditemukan → throw UsernameNotFoundException
     * 3. Jika ditemukan → Convert Entity User ke UserDetails
     * 4. Return UserDetails ke Spring Security
     * 
     * @param username Username yang diinput user saat login
     * @return UserDetails object (interface Spring Security)
     * @throws UsernameNotFoundException Jika username tidak ditemukan
     */
    @Override
    @Transactional(readOnly = true)  // Transactional untuk load lazy collection (roles)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Cari user di database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User tidak ditemukan dengan username: " + username
                ));
        
        // 2. Convert roles ke GrantedAuthority (interface Spring Security)
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());
        
        // 3. Build UserDetails object
        // org.springframework.security.core.userdetails.User adalah implementasi UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())        // Set username
                .password(user.getPassword())        // Set password (hashed)
                .authorities(authorities)            // Set roles/authorities
                .accountExpired(false)               // Account tidak expired
                .accountLocked(false)                // Account tidak locked
                .credentialsExpired(false)           // Password tidak expired
                .disabled(!user.isEnabled())         // Enabled/disabled status
                .build();
    }
}
```

---

## H. Step 7: Buat JWT Authentication Filter

Buat file `backend/src/main/java/com/smk/presensi/security/jwt/JwtAuthenticationFilter.java`:

```java
package com.smk.presensi.security.jwt;

import com.smk.presensi.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter - Intercept setiap HTTP request.
 * 
 * Filter ini akan:
 * 1. Extract JWT token dari request header
 * 2. Validate token
 * 3. Load user dari database
 * 4. Set authentication ke SecurityContext
 * 
 * Setelah filter ini, endpoint bisa akses user info dari SecurityContext.
 * 
 * Extends OncePerRequestFilter:
 * - Filter ini dijamin hanya dijalankan sekali per request
 * - Avoid duplicate execution
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    /**
     * Method utama filter: dijalankan untuk setiap HTTP request.
     * 
     * Flow:
     * 1. Extract JWT token dari header "Authorization: Bearer <token>"
     * 2. Validate token (signature, expiration)
     * 3. Extract username dari token
     * 4. Load user detail dari database
     * 5. Set authentication ke SecurityContext
     * 6. Lanjutkan ke filter berikutnya (filterChain.doFilter)
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Chain of filters
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
            
            // 2. Validate token dan extract username
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // Token valid, extract username
                String username = jwtUtil.getUsernameFromToken(jwt);
                
                // 3. Load user detail dari database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 4. Buat Authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,              // Principal (user info)
                                null,                     // Credentials (tidak perlu, sudah authenticated)
                                userDetails.getAuthorities()  // Authorities (roles)
                        );
                
                // Set detail request (IP address, session ID, dll)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 5. Set authentication ke SecurityContext
                // Setelah ini, endpoint bisa akses user info dengan:
                // SecurityContextHolder.getContext().getAuthentication()
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("Set authentication for user: {}", username);
            }
            
        } catch (Exception ex) {
            // Jika ada error (token invalid, user tidak ditemukan, dll)
            // Log error tapi JANGAN throw exception
            // Biar request tetap lanjut (return 401 Unauthorized nanti)
            logger.error("Cannot set user authentication: {}", ex.getMessage());
        }
        
        // 6. Lanjutkan ke filter berikutnya
        // PENTING: Jangan lupa panggil filterChain.doFilter()!
        // Kalau tidak, request tidak akan sampai ke controller
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token dari request header.
     * 
     * Token dikirim di header "Authorization" dengan format:
     * Authorization: Bearer <token>
     * 
     * Method ini akan:
     * 1. Ambil header "Authorization"
     * 2. Cek apakah ada dan dimulai dengan "Bearer "
     * 3. Extract token (substring setelah "Bearer ")
     * 
     * @param request HTTP request
     * @return JWT token string, atau null jika tidak ada
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Ambil header "Authorization"
        String bearerToken = request.getHeader("Authorization");
        
        // Cek apakah header ada dan dimulai dengan "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token (substring mulai dari index 7 = setelah "Bearer ")
            return bearerToken.substring(7);
        }
        
        // Jika header tidak ada atau format salah, return null
        return null;
    }
}
```

---

## I. Step 8: Security Configuration

Buat file `backend/src/main/java/com/smk/presensi/security/SecurityConfig.java`:

```java
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
                        // Auth endpoints: login, register
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // H2 Console (untuk development)
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Hello endpoint (untuk testing)
                        .requestMatchers("/api/hello").permitAll()
                        
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
```

**Penjelasan penting:**

**CSRF (Cross-Site Request Forgery):**
- Attack dimana attacker trick user untuk execute unwanted action
- Contoh: User login bank → Attacker kirim link → Click link → Transfer uang tanpa sadar
- Protection: CSRF token (unique token untuk setiap form submission)
- **Tidak perlu untuk REST API dengan JWT** karena:
  - JWT di header (bukan cookie)
  - Attacker tidak bisa baca header dari cross-origin request

**CORS (Cross-Origin Resource Sharing):**
- Browser block request dari domain berbeda (security feature)
- Contoh: Frontend di `http://localhost:3000`, API di `http://localhost:8081`
- Tanpa CORS config, browser block semua request
- Solution: Enable CORS di backend (allow frontend domain)

**Session Management - Stateless:**
- Traditional: Server create session, simpan di memory
- JWT: Server tidak create session, semua state di token
- Benefit: Scalable (tidak perlu shared session antar server)

---

## J. Step 9: Buat DTO untuk Authentication

### 9.1. LoginRequest DTO

Buat file `backend/src/main/java/com/smk/presensi/dto/auth/LoginRequest.java`:

```java
package com.smk.presensi.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO untuk request login.
 * 
 * Client kirim username & password untuk authentication.
 * 
 * Dipakai di endpoint: POST /api/auth/login
 */
public record LoginRequest(
        /**
         * Username untuk login.
         * Wajib diisi (@NotBlank).
         */
        @NotBlank(message = "Username tidak boleh kosong")
        String username,
        
        /**
         * Password untuk login (plain text).
         * Wajib diisi (@NotBlank).
         * 
         * Password akan di-compare dengan hash di database.
         * TIDAK disimpan, hanya dipakai untuk verify.
         */
        @NotBlank(message = "Password tidak boleh kosong")
        String password
) {
    // Record auto-generate:
    // - Constructor
    // - Getter: username(), password()
    // - toString(), equals(), hashCode()
}
```

### 9.2. LoginResponse DTO

Buat file `backend/src/main/java/com/smk/presensi/dto/auth/LoginResponse.java`:

```java
package com.smk.presensi.dto.auth;

import java.util.Set;

/**
 * DTO untuk response login.
 * 
 * Server return JWT token + user info setelah login berhasil.
 * 
 * Client simpan token ini (localStorage atau cookie)
 * dan kirim di header untuk request berikutnya:
 * Authorization: Bearer <token>
 */
public record LoginResponse(
        /**
         * JWT token untuk authentication.
         * 
         * Format: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMD...
         * 
         * Client harus simpan token ini dan kirim di setiap request.
         */
        String token,
        
        /**
         * Type token (selalu "Bearer" untuk JWT).
         * 
         * Format header: Authorization: Bearer <token>
         * "Bearer" adalah prefix standard untuk JWT.
         */
        String type,
        
        /**
         * Username yang login.
         * Untuk display di UI (contoh: "Welcome, admin!")
         */
        String username,
        
        /**
         * Roles user (contoh: ["ROLE_ADMIN", "ROLE_GURU"])
         * Untuk frontend decision:
         * - Show/hide menu berdasarkan role
         * - Enable/disable button berdasarkan role
         */
        Set<String> roles
) {
    /**
     * Constructor dengan default type "Bearer".
     * 
     * Contoh penggunaan:
     * new LoginResponse(token, username, roles)
     * // type otomatis "Bearer"
     */
    public LoginResponse(String token, String username, Set<String> roles) {
        this(token, "Bearer", username, roles);
    }
}
```

### 9.3. RegisterRequest DTO

Buat file `backend/src/main/java/com/smk/presensi/dto/auth/RegisterRequest.java`:

```java
package com.smk.presensi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO untuk request register user baru.
 * 
 * Client kirim username, email, password untuk create account.
 * 
 * Dipakai di endpoint: POST /api/auth/register
 */
public record RegisterRequest(
        /**
         * Username untuk login.
         * 
         * Validation:
         * - Wajib diisi (@NotBlank)
         * - Min 3 karakter, max 20 karakter (@Size)
         * - Harus unique (dicek di service layer)
         */
        @NotBlank(message = "Username tidak boleh kosong")
        @Size(min = 3, max = 20, message = "Username harus 3-20 karakter")
        String username,
        
        /**
         * Email user.
         * 
         * Validation:
         * - Wajib diisi (@NotBlank)
         * - Harus format email yang valid (@Email)
         * - Max 50 karakter (@Size)
         */
        @NotBlank(message = "Email tidak boleh kosong")
        @Email(message = "Format email tidak valid")
        @Size(max = 50, message = "Email maksimal 50 karakter")
        String email,
        
        /**
         * Password (plain text).
         * 
         * Validation:
         * - Wajib diisi (@NotBlank)
         * - Min 6 karakter (@Size)
         * 
         * Password akan di-hash dengan BCrypt sebelum disimpan.
         * JANGAN PERNAH simpan password plain text di database!
         */
        @NotBlank(message = "Password tidak boleh kosong")
        @Size(min = 6, max = 40, message = "Password harus 6-40 karakter")
        String password,
        
        /**
         * Role yang diminta (opsional).
         * 
         * Default: ROLE_SISWA (kalau tidak diisi)
         * 
         * Untuk register ADMIN atau GURU, perlu approval
         * atau hanya bisa dilakukan oleh ADMIN existing.
         */
        String role
) {
    // Record auto-generate constructor, getters, dll
}
```

### 9.4. MessageResponse DTO (Generic)

Buat file `backend/src/main/java/com/smk/presensi/dto/MessageResponse.java`:

```java
package com.smk.presensi.dto;

/**
 * Generic DTO untuk response dengan message saja.
 * 
 * Dipakai untuk:
 * - Success message: "User registered successfully"
 * - Error message: "Username is already taken"
 * - Info message: "Password reset email sent"
 * 
 * Simple record untuk kemudahan.
 */
public record MessageResponse(
        /**
         * Message text.
         */
        String message
) {
    // Record auto-generate constructor dan getter
}
```

---

## K. Step 10: Buat Authentication Service

Buat file `backend/src/main/java/com/smk/presensi/service/AuthService.java`:

```java
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
```

---

## L. Step 11: Buat Authentication Controller

Buat file `backend/src/main/java/com/smk/presensi/controller/AuthController.java`:

```java
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
```

---

## M. Step 12: Data Seeding (Init Roles & Admin User)

Kita perlu create roles default dan admin user saat aplikasi pertama kali jalan.

Buat file `backend/src/main/java/com/smk/presensi/config/DataSeeder.java`:

```java
package com.smk.presensi.config;

import com.smk.presensi.entity.Role;
import com.smk.presensi.entity.Role.RoleName;
import com.smk.presensi.entity.User;
import com.smk.presensi.repository.RoleRepository;
import com.smk.presensi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Seeder - Insert data default saat aplikasi start.
 * 
 * CommandLineRunner: Interface yang dijalankan otomatis saat aplikasi start.
 * Method run() akan dipanggil setelah ApplicationContext ready.
 * 
 * Yang di-seed:
 * 1. 3 Roles: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA
 * 2. 1 Admin user default (username: admin, password: admin123)
 * 
 * Data ini perlu untuk testing dan development.
 * Tanpa roles, user tidak bisa register (role not found error).
 * Tanpa admin, tidak ada yang bisa manage users.
 */
@Component
public class DataSeeder implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor injection.
     */
    public DataSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Method yang dijalankan saat aplikasi start.
     * 
     * Flow:
     * 1. Seed roles (ROLE_ADMIN, ROLE_GURU, ROLE_SISWA)
     * 2. Seed admin user default
     * 
     * @param args Command line arguments (tidak dipakai)
     */
    @Override
    public void run(String... args) {
        logger.info("Starting data seeding...");
        
        // 1. Seed roles
        seedRoles();
        
        // 2. Seed admin user
        seedAdminUser();
        
        logger.info("Data seeding completed!");
    }
    
    /**
     * Seed roles ke database.
     * 
     * Create 3 roles jika belum ada:
     * - ROLE_ADMIN: Full access
     * - ROLE_GURU: Read all, manage presensi
     * - ROLE_SISWA: Read own data, submit presensi
     */
    private void seedRoles() {
        // Cek apakah roles sudah ada
        if (roleRepository.count() > 0) {
            logger.info("Roles already exist, skipping role seeding");
            return;
        }
        
        logger.info("Seeding roles...");
        
        // Create 3 roles
        Role adminRole = new Role(RoleName.ROLE_ADMIN);
        Role guruRole = new Role(RoleName.ROLE_GURU);
        Role siswaRole = new Role(RoleName.ROLE_SISWA);
        
        // Save ke database
        roleRepository.save(adminRole);
        roleRepository.save(guruRole);
        roleRepository.save(siswaRole);
        
        logger.info("Roles seeded: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA");
    }
    
    /**
     * Seed admin user default ke database.
     * 
     * Create admin user dengan:
     * - Username: admin
     * - Password: admin123 (hashed)
     * - Role: ROLE_ADMIN
     * 
     * PENTING: Ganti password ini di production!
     */
    private void seedAdminUser() {
        // Cek apakah admin user sudah ada
        if (userRepository.existsByUsername("admin")) {
            logger.info("Admin user already exists, skipping admin seeding");
            return;
        }
        
        logger.info("Seeding admin user...");
        
        // Get ROLE_ADMIN
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found!"));
        
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@smk.sch.id");
        
        // Hash password dengan BCrypt
        String hashedPassword = passwordEncoder.encode("admin123");
        admin.setPassword(hashedPassword);
        
        // Add role
        admin.addRole(adminRole);
        
        // Save ke database
        userRepository.save(admin);
        
        logger.info("Admin user seeded: username=admin, password=admin123");
        logger.warn("⚠️  CHANGE ADMIN PASSWORD IN PRODUCTION!");
    }
}
```

---

## N. Step 13: Update application.properties

Buka `backend/src/main/resources/application.properties`, tambahkan:

```properties
# ==========================================
# JWT Configuration
# ==========================================
# Secret key untuk sign JWT token
# PENTING: Ganti dengan random string yang panjang di production!
# Generate: openssl rand -base64 64
app.jwt.secret=SIJASpringBootSecretKeyForJWTToken2024VeryLongAndSecureDoNotShareThisKey

# Expiration time JWT token (milidetik)
# 86400000 ms = 24 jam
app.jwt.expiration=86400000

# ==========================================
# Spring Security Logging (untuk debugging)
# ==========================================
# Uncomment jika mau lihat detail security flow
# logging.level.org.springframework.security=DEBUG
```

---

## O. Step 14: Update pom.xml

Buka `backend/pom.xml`, tambahkan dependencies di dalam `<dependencies>`:

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT Library (jjwt) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

Setelah tambah, **reload Maven**:
- IntelliJ: Klik icon ⟳ di Maven sidebar
- VS Code: Tunggu auto-reload atau restart Java Language Server

---

## P. Step 15: Testing dengan Postman

### 15.1. Test Register User

**Request:**
```
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "username": "siswa01",
  "email": "siswa01@smk.sch.id",
  "password": "password123",
  "role": "ROLE_SISWA"
}
```

**Expected Response (201 Created):**
```json
{
  "message": "User registered successfully"
}
```

### 15.2. Test Login

**Request:**
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ.signature",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

**PENTING: Copy token dari response!**

### 15.3. Test Access Protected Endpoint

**Request (TANPA Token):**
```
GET http://localhost:8081/api/siswa
```

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2024-11-16T...",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required",
  "path": "/api/siswa"
}
```

**Request (DENGAN Token):**
```
GET http://localhost:8081/api/siswa
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "nis": "2024001",
    "nama": "Budi Santoso",
    "kelas": "XII RPL 1",
    "jurusan": "RPL"
  }
]
```

---

## Q. Troubleshooting Common Issues

### Issue 1: "Bad credentials" saat login

**Penyebab:**
- Username atau password salah
- Password belum di-hash dengan benar

**Solusi:**
1. Cek username benar (case-sensitive)
2. Cek password benar
3. Cek DataSeeder sudah run (lihat log saat startup)
4. Test dengan admin default: username=admin, password=admin123

### Issue 2: "ROLE_ADMIN not found" saat seed admin

**Penyebab:**
- Roles belum di-seed
- Order execution salah (seedAdminUser() dipanggil sebelum seedRoles())

**Solusi:**
1. Pastikan seedRoles() dipanggil dulu
2. Cek log: "Roles seeded: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA"
3. Cek database: tabel roles harus ada 3 rows

### Issue 3: "Full authentication is required" untuk semua endpoint

**Penyebab:**
- JWT token tidak dikirim di header
- Token format salah (harus "Bearer <token>")
- Token expired atau invalid

**Solusi:**
1. Pastikan header: `Authorization: Bearer <token>`
2. Pastikan ada spasi setelah "Bearer"
3. Pastikan token tidak expired (cek exp claim)
4. Cek log: "Cannot set user authentication" → ada error di JWT filter

### Issue 4: "JWT signature does not match"

**Penyebab:**
- Secret key berbeda antara generate dan verify
- Secret key berubah (restart app dengan secret baru)

**Solusi:**
1. Login ulang untuk dapat token baru
2. Pastikan app.jwt.secret tidak berubah
3. Di production, simpan secret di environment variable

### Issue 5: Maven cannot download jjwt dependencies

**Penyebab:**
- Version tidak tersedia
- Maven repository issue

**Solusi:**
1. Cek koneksi internet
2. Clean Maven cache: `mvn clean install -U`
3. Cek version di Maven Central: https://search.maven.org/
4. Pastikan version 0.12.3 (atau yang lebih baru)

---

## R. Checklist Keberhasilan Tahap 03

Centang ✅ jika sudah berhasil:

### Dependencies & Configuration:
- [ ] Dependencies Spring Security & JWT sudah ditambah di pom.xml
- [ ] Maven reload berhasil, tidak ada error
- [ ] application.properties sudah dikonfigurasi (jwt.secret & jwt.expiration)

### Entities & Repositories:
- [ ] Entity Role sudah dibuat dengan enum RoleName
- [ ] Entity User sudah dibuat dengan relasi ke Role
- [ ] RoleRepository sudah dibuat dengan findByName()
- [ ] UserRepository sudah dibuat dengan findByUsername() & existsByUsername()

### Security Components:
- [ ] JwtUtil class sudah dibuat (generate, validate, extract token)
- [ ] CustomUserDetailsService sudah dibuat (loadUserByUsername)
- [ ] JwtAuthenticationFilter sudah dibuat (intercept request)
- [ ] SecurityConfig sudah dibuat (password encoder, filter chain, authorization rules)

### Authentication:
- [ ] DTOs sudah dibuat (LoginRequest, LoginResponse, RegisterRequest, MessageResponse)
- [ ] AuthService sudah dibuat (login, register logic)
- [ ] AuthController sudah dibuat (login & register endpoints)
- [ ] DataSeeder sudah dibuat (seed roles & admin user)

### Testing:
- [ ] Aplikasi bisa start tanpa error
- [ ] DataSeeder run, log muncul: "Roles seeded", "Admin user seeded"
- [ ] Database tabel users, roles, user_roles sudah ter-create
- [ ] POST /api/auth/register berhasil (201 Created)
- [ ] POST /api/auth/login berhasil (200 OK, dapat token)
- [ ] GET /api/siswa TANPA token → 401 Unauthorized
- [ ] GET /api/siswa DENGAN token → 200 OK, dapat data
- [ ] Token bisa di-decode di https://jwt.io/ (lihat payload)

### Understanding:
- [ ] Paham perbedaan Authentication vs Authorization
- [ ] Paham cara kerja JWT (generate, send, verify)
- [ ] Paham password hashing dengan BCrypt
- [ ] Paham Spring Security filter chain
- [ ] Paham role-based access control

---

## S. Next Steps

Setelah semua checklist ✅, Anda sudah punya:
- ✅ User authentication dengan JWT
- ✅ Role-based access control (ADMIN, GURU, SISWA)
- ✅ Secure endpoints (perlu authentication)
- ✅ Password hashing dengan BCrypt

**Tahap 04**: Implementasi Presensi Basic
- Flow check-in/out manual
- Entity Presensi dengan relasi ke Siswa/Guru
- Validasi waktu presensi (jam masuk, jam pulang)
- History presensi
- Dashboard simple

**Next branch**: `tahap-04-presensi-basic`

---

## T. Resources

### Documentation:
- Spring Security: https://spring.io/projects/spring-security
- JWT (jjwt): https://github.com/jwtk/jjwt
- BCrypt: https://en.wikipedia.org/wiki/Bcrypt

### Tools:
- JWT Decoder: https://jwt.io/
- BCrypt Generator: https://bcrypt-generator.com/
- Postman: https://www.postman.com/

### Debugging:
- Enable Spring Security debug: `logging.level.org.springframework.security=DEBUG`
- Check JWT claims: Paste token di https://jwt.io/
- Check BCrypt hash: Use https://bcrypt-generator.com/

---

**Author:** SIJA Spring Boot Training Team  
**Last Updated:** November 2025  
**Version:** 1.0

**Selamat! Anda sudah menguasai Authentication & Authorization dengan JWT!** 🎉🔐