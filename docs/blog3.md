# Blog 3: Memahami Authentication & Authorization dengan JWT - Panduan untuk Pemula

**Author:** SIJA Spring Boot Training Team  
**Date:** November 16, 2025  
**Target:** Siswa SMK yang baru belajar Java & Spring Boot  
**Topik:** JWT Authentication, Spring Security, Password Hashing

---

## Pendahuluan: Kenapa Butuh Keamanan?

Bayangkan kamu punya aplikasi presensi sekolah. Tanpa sistem keamanan:
- ❌ Siapa saja bisa hapus data siswa
- ❌ Siswa bisa edit nilai presensi mereka sendiri
- ❌ Orang luar bisa lihat data pribadi siswa
- ❌ Tidak ada cara untuk tahu siapa yang sedang login

**Solusinya?** Kita butuh sistem **Authentication** dan **Authorization**!

---

## Part 1: Authentication vs Authorization - Apa Bedanya?

### Analogi: Masuk ke Sekolah

Bayangkan kamu mau masuk ke sekolah:

**1. Authentication (Autentikasi) = "Siapa kamu?"**

Kamu tiba di gerbang sekolah. Satpam minta lihat **kartu pelajar** (KTP Pelajar). Satpam cek:
- ✅ Nama di kartu cocok dengan wajah kamu?
- ✅ Foto di kartu cocok dengan orangnya?
- ✅ Kartu masih berlaku (tidak expired)?

Jika cocok → **Kamu teridentifikasi sebagai siswa yang sah** → Boleh masuk.

**Di aplikasi:**
- Kartu pelajar = **Username & Password**
- Cek foto = **Verify password dengan database**
- Satpam = **Spring Security**

**2. Authorization (Otorisasi) = "Kamu boleh ngapain?"**

Setelah masuk sekolah, tidak semua tempat bisa kamu akses:
- 🚪 Ruang kelas → Siswa boleh masuk
- 🚪 Ruang guru → Hanya guru boleh masuk
- 🚪 Ruang kepala sekolah → Hanya kepala sekolah boleh masuk

Meskipun kamu sudah **authenticated** (punya kartu pelajar), kamu tidak **authorized** untuk masuk ruang guru.

**Di aplikasi:**
- Ruang kelas = Endpoint `/api/siswa` (siswa bisa akses data sendiri)
- Ruang guru = Endpoint `/api/guru` (hanya guru bisa akses)
- Ruang kepala sekolah = Endpoint `/api/admin` (hanya admin bisa akses)
- Kartu akses = **Role** (ROLE_SISWA, ROLE_GURU, ROLE_ADMIN)

### Kesimpulan Perbedaan

| Aspek | Authentication | Authorization |
|-------|---------------|---------------|
| **Pertanyaan** | Siapa kamu? | Kamu boleh ngapain? |
| **Proses** | Verify identity (username + password) | Check permissions (role) |
| **Output** | User information (username, roles) | Access decision (allow/deny) |
| **Analogi** | Cek kartu pelajar di gerbang | Cek akses ruangan setelah masuk |
| **Teknologi** | Login with password | Role-based access control |

**PENTING:** Authentication HARUS dilakukan dulu sebelum authorization!

---

## Part 2: Masalah dengan Session - Kenapa Pakai JWT?

### Traditional Session-Based Authentication

**Cara Lama (Session):**

1. User login → Server create session
2. Server simpan session di memory: `Map<sessionId, UserInfo>`
3. Server kirim session ID ke client (cookie)
4. Client kirim session ID di setiap request
5. Server lookup session dari memory

**Masalahnya:**

❌ **Tidak scalable:**
- Bayangkan 10,000 user login → 10,000 session di memory
- Server restart → Semua session hilang → User harus login ulang

❌ **Sulit di-distribute:**
- Load balancer kirim request ke server A → Session ada
- Request berikutnya ke server B → Session tidak ada (different server!)
- Solution: Shared session storage (Redis, database) → Kompleks!

❌ **CORS problem:**
- Cookie tidak dikirim untuk cross-origin request (default browser security)
- Frontend di `http://localhost:3000`, API di `http://localhost:8081` → Cookie blocked

### Modern JWT-Based Authentication

**Cara Baru (JWT):**

1. User login → Server generate **JWT token**
2. Server **TIDAK simpan** token (stateless!)
3. Server kirim token ke client
4. Client simpan token (localStorage atau cookie)
5. Client kirim token di **header** setiap request: `Authorization: Bearer <token>`
6. Server **verify signature** token → Extract user info dari token

**Keuntungan:**

✅ **Stateless:**
- Server tidak perlu simpan session
- Token berisi semua info yang diperlukan (username, roles, expiration)
- Server restart → Token masih valid (selama belum expired)

✅ **Scalable:**
- Load balancer kirim request ke server mana saja → Tidak masalah!
- Semua server bisa verify token dengan secret key yang sama

✅ **Cross-origin friendly:**
- Token di header, bukan cookie
- Tidak terkena CORS restriction

✅ **Mobile-friendly:**
- Mobile app tidak support cookie dengan baik
- Token di header cocok untuk mobile app

---

## Part 3: Struktur JWT Token - Apa Isinya?

### JWT = JSON Web Token

Token JWT terdiri dari **3 bagian** dipisahkan dengan titik (`.`):

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ.signature_here
```

```
Header.Payload.Signature
```

### Analogi: Boarding Pass Pesawat

Bayangkan JWT seperti **boarding pass** di pesawat:

**1. Header (Kepala Surat) = "Jenis Dokumen"**

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

Ini seperti tulisan di atas boarding pass: **"BOARDING PASS - GARUDA INDONESIA"**

- `alg`: Algoritma untuk sign (HS256 = HMAC SHA-256)
- `typ`: Type dokumen (JWT)

**2. Payload (Isi Surat) = "Data Penumpang"**

```json
{
  "sub": "admin",
  "iat": 1700000000,
  "exp": 1700086400,
  "roles": ["ROLE_ADMIN"]
}
```

Ini seperti isi boarding pass:
- **Nama:** Admin (subject = username)
- **Waktu cetak:** 15 Nov 2024, 12:00 (issued at)
- **Waktu kedaluwarsa:** 16 Nov 2024, 12:00 (expiration - 24 jam)
- **Kelas:** Business (roles = ROLE_ADMIN)

**3. Signature (Tanda Tangan & Stempel) = "Bukti Keaslian"**

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

Ini seperti:
- Barcode di boarding pass (tidak bisa dipalsukan)
- Stempel maskapai (hanya maskapai punya stempel asli)
- Tanda tangan petugas (verify keaslian)

**Cara kerja signature:**
1. Ambil Header + Payload
2. Hash dengan secret key (hanya server yang tahu)
3. Hasilnya = Signature (bukti bahwa token dibuat oleh server)

**Keamanan:**
- ✅ User bisa **baca** isi token (decode base64)
- ❌ User **TIDAK BISA** ubah isi token (signature akan invalid)
- ❌ User **TIDAK BISA** buat token palsu (tidak tahu secret key)

**Contoh serangan:**

User coba ubah role dari `ROLE_SISWA` ke `ROLE_ADMIN`:

```json
// Token asli (valid)
{
  "sub": "siswa01",
  "roles": ["ROLE_SISWA"]
}

// User edit jadi (INVALID - signature tidak cocok!)
{
  "sub": "siswa01",
  "roles": ["ROLE_ADMIN"]  // ❌ Diubah
}
```

Server verify signature → **Tidak cocok** → Token **ditolak**! 🛑

---

## Part 4: Password Hashing - Kenapa Tidak Simpan Plain Text?

### Masalah Password Plain Text

**Scenario 1: Database Leak**

Bayangkan database sekolah kamu di-hack:

```sql
SELECT * FROM users;
```

```
| username | password   |
|----------|------------|
| admin    | admin123   | ← 😱 Password terlihat!
| guru01   | password   | ← 😱 Password terlihat!
| siswa01  | 123456     | ← 😱 Password terlihat!
```

Hacker dapat **semua password**! Bahaya karena:
- ❌ Bisa login ke akun siapa saja
- ❌ User pakai password yang sama di situs lain (email, bank)
- ❌ Reputasi sekolah rusak

### Solution: Password Hashing

**Hash = One-Way Encryption (Tidak bisa di-decode)**

```
Password: "admin123"
         ↓ (BCrypt hash)
Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
```

**Properties:**
1. **One-way:** Hash → Password (TIDAK BISA)
2. **Same input, different output:** Hash pakai random salt
3. **Slow by design:** Prevent brute force attack

**Analogi: Mesin Penghancur Kertas**

Bayangkan password seperti dokumen rahasia:
- Plain text = Dokumen utuh (bisa dibaca)
- Hash = Dokumen sudah dihancur jadi potongan kecil (tidak bisa dibaca lagi)

**Cara kerja:**
1. User register: Password "admin123" → Hash → Simpan hash di database
2. User login: Input password "admin123" → Hash → Compare dengan hash di database
3. Jika **hash cocok** → Password benar ✅
4. Jika **hash tidak cocok** → Password salah ❌

**Database setelah hashing:**

```
| username | password                                           |
|----------|---------------------------------------------------|
| admin    | $2a$10$N9qo8uLOickgx2ZMRZoMye...                     |
| guru01   | $2a$10$abcdefghijklmnopqrstuv...                     |
| siswa01  | $2a$10$1234567890ABCDEFGHIJK...                     |
```

Hacker dapat database → **Tidak bisa tahu password** karena hash tidak bisa di-decode!

### BCrypt - Algoritma Hashing yang Aman

**Kenapa BCrypt?**

1. **Random Salt:** Password yang sama, hash-nya berbeda

```
Hash("admin123") → $2a$10$ABC...
Hash("admin123") → $2a$10$XYZ... ← Beda!
```

2. **Slow by Design:** 1 hash butuh ~100ms (cegah brute force)

Hacker coba 1 juta password → Butuh **~27 jam**!

3. **Adaptive:** Bisa adjust "cost factor" (seberapa lambat)

```java
BCryptPasswordEncoder(10)  // 2^10 = 1024 rounds
BCryptPasswordEncoder(12)  // 2^12 = 4096 rounds (lebih lambat, lebih aman)
```

**Format BCrypt hash:**

```
$2a$10$N9qo8uLOickgx2ZMRZoMye...
 │  │  │                        │
 │  │  └─ Salt (random)         └─ Hash (password + salt)
 │  └─ Cost factor (2^10 rounds)
 └─ Algoritma version (BCrypt 2a)
```

---

## Part 5: Spring Security Filter Chain - Flow Authentication

### Analogi: Security Check di Bandara

Bayangkan request HTTP seperti penumpang di bandara:

```
Penumpang (HTTP Request)
    ↓
1. Check-in Counter (DispatcherServlet)
    ↓
2. Security Check 1 (CorsFilter)
    ↓
3. Security Check 2 (JwtAuthenticationFilter) ← KITA BUAT INI!
    ↓
4. Security Check 3 (UsernamePasswordAuthenticationFilter)
    ↓
5. Final Check (FilterSecurityInterceptor)
    ↓
6. Boarding Gate (Controller)
```

### Filter Chain - Urutan Penting!

**1. CorsFilter:**
- Check apakah request dari domain yang diizinkan
- Reject jika cross-origin tidak diizinkan

**2. JwtAuthenticationFilter (Custom - Kita Buat):**
- Extract JWT token dari header `Authorization: Bearer <token>`
- Validate token (signature, expiration)
- Extract username dari token
- Load user dari database
- Set `Authentication` object ke `SecurityContext`

**3. FilterSecurityInterceptor:**
- Check apakah user punya permission untuk akses endpoint
- Lihat `@PreAuthorize("hasRole('ADMIN')")` di controller
- Allow/deny berdasarkan role

### Flow Lengkap Authentication

**Scenario 1: Login (Dapat Token)**

```
Client                          Server
  │                               │
  ├─ POST /api/auth/login ───────>│
  │  {username, password}          │
  │                                │
  │                        [1] AuthController
  │                                │
  │                        [2] AuthService.login()
  │                                │
  │                        [3] AuthenticationManager
  │                                │
  │                        [4] DaoAuthenticationProvider
  │                                │
  │                        [5] UserDetailsService.loadUserByUsername()
  │                                │
  │                        [6] Database query: SELECT * FROM users
  │                                │
  │                        [7] PasswordEncoder.matches(input, hash)
  │                                │
  │                        [8] JwtUtil.generateToken()
  │                                │
  │<─── Response { token } ────────┤
  │                                │
```

**Scenario 2: Access Protected Endpoint (Pakai Token)**

```
Client                          Server
  │                               │
  ├─ GET /api/siswa ─────────────>│
  │  Authorization: Bearer <token> │
  │                                │
  │                        [1] JwtAuthenticationFilter
  │                                │
  │                        [2] Extract token from header
  │                                │
  │                        [3] JwtUtil.validateToken(token)
  │                                │
  │                        [4] JwtUtil.getUsernameFromToken(token)
  │                                │
  │                        [5] UserDetailsService.loadUserByUsername()
  │                                │
  │                        [6] Set Authentication to SecurityContext
  │                                │
  │                        [7] FilterSecurityInterceptor
  │                                │
  │                        [8] Check @PreAuthorize("hasRole('ADMIN')")
  │                                │
  │                        [9] SiswaController.getAllSiswa()
  │                                │
  │<─── Response [siswa data] ─────┤
  │                                │
```

---

## Part 6: Role-Based Access Control (RBAC)

### Konsep Roles

**Role = Jabatan/Peran di sistem**

Seperti di sekolah:
- 👨‍💼 **Kepala Sekolah** → Bisa akses semua data, edit, hapus
- 👨‍🏫 **Guru** → Bisa lihat data siswa, input nilai, lihat presensi
- 👨‍🎓 **Siswa** → Hanya bisa lihat data sendiri, isi presensi sendiri

**Di aplikasi kita:**

| Role | Permissions |
|------|-------------|
| **ROLE_ADMIN** | Full access: Create, Read, Update, Delete semua data |
| **ROLE_GURU** | Read all siswa, manage presensi, manage nilai |
| **ROLE_SISWA** | Read own data, submit own presensi |

### Implementasi di Spring Security

**1. Assign Role ke User (saat register/seed)**

```java
User user = new User();
user.setUsername("admin");
user.setPassword(passwordEncoder.encode("admin123"));

Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN);
user.addRole(adminRole);  // User punya role ADMIN
```

**2. Protect Endpoint dengan @PreAuthorize**

```java
@RestController
@RequestMapping("/api/siswa")
public class SiswaController {
    
    // Hanya ADMIN bisa hapus siswa
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSiswa(@PathVariable Long id) {
        siswaService.delete(id);
    }
    
    // ADMIN dan GURU bisa lihat semua siswa
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<Siswa> getAllSiswa() {
        return siswaService.findAll();
    }
    
    // SISWA hanya bisa lihat data sendiri
    @GetMapping("/me")
    @PreAuthorize("hasRole('SISWA')")
    public Siswa getMyProfile() {
        // Get username dari SecurityContext
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        
        return siswaService.findByUsername(username);
    }
}
```

**3. Test Authorization**

```
User: admin (ROLE_ADMIN)
DELETE /api/siswa/1  → ✅ 200 OK (allowed)

User: guru01 (ROLE_GURU)
DELETE /api/siswa/1  → ❌ 403 Forbidden (denied)

User: siswa01 (ROLE_SISWA)
GET /api/siswa       → ❌ 403 Forbidden (denied)
GET /api/siswa/me    → ✅ 200 OK (allowed)
```

---

## Part 7: Security Best Practices

### DO ✅

**1. Simpan Secret Key di Environment Variable**

```bash
# JANGAN di application.properties (commit ke git!)
# app.jwt.secret=MySecretKey123

# Pakai environment variable
export JWT_SECRET=VeryLongAndRandomSecretKeyHere
```

```properties
# application.properties
app.jwt.secret=${JWT_SECRET}
```

**2. Gunakan HTTPS di Production**

- HTTP → Data dikirim plain text → Token bisa dicuri (man-in-the-middle attack)
- HTTPS → Data di-encrypt → Aman

**3. Set Token Expiration**

```properties
# 24 jam (86400000 ms)
app.jwt.expiration=86400000
```

Jika token expired → User harus login ulang → Limit window of attack

**4. Validate Input**

```java
public record LoginRequest(
    @NotBlank(message = "Username required")
    @Size(min = 3, max = 20)
    String username,
    
    @NotBlank(message = "Password required")
    @Size(min = 6)
    String password
) {}
```

**5. Log Security Events**

```java
logger.info("User {} logged in successfully", username);
logger.warn("Failed login attempt for user {}", username);
logger.error("Invalid JWT token: {}", e.getMessage());
```

### DON'T ❌

**1. JANGAN Simpan Password Plain Text**

```java
// ❌ SALAH
user.setPassword("admin123");

// ✅ BENAR
String hashed = passwordEncoder.encode("admin123");
user.setPassword(hashed);
```

**2. JANGAN Commit Secret Key ke Git**

```properties
# ❌ JANGAN
app.jwt.secret=MySecretKey123

# ✅ Pakai environment variable
app.jwt.secret=${JWT_SECRET}
```

**3. JANGAN Return Password di Response**

```java
// ❌ SALAH
public User getUser(Long id) {
    return userRepository.findById(id);  // Termasuk password!
}

// ✅ BENAR
public record UserResponse(
    Long id,
    String username,
    String email
    // Tidak ada password field!
) {}
```

**4. JANGAN Simpan Token di Cookie (jika pakai localStorage)**

- Cookie → Vulnerable to CSRF attack
- localStorage → Vulnerable to XSS attack
- **Best:** localStorage + HTTPS + CSP header

**5. JANGAN Pakai Secret Key Pendek**

```properties
# ❌ SALAH (terlalu pendek!)
app.jwt.secret=secret

# ✅ BENAR (minimal 256 bit / 32 byte)
app.jwt.secret=VeryLongAndRandomSecretKeyMinimum256BitForHS256Algorithm
```

---

## Part 8: Debugging - Cara Cek JWT Token

### Tool 1: jwt.io

1. Buka https://jwt.io/
2. Paste token kamu di kotak "Encoded"
3. Lihat decoded header & payload

**Contoh:**

```
Token:
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwLCJyb2xlcyI6WyJST0xFX0FETUlOIl19.signature

Decoded:
{
  "alg": "HS256"  // Header
}
{
  "sub": "admin",              // Username
  "iat": 1700000000,           // Issued at: 15 Nov 2024
  "exp": 1700086400,           // Expiration: 16 Nov 2024
  "roles": ["ROLE_ADMIN"]      // Roles
}
```

### Tool 2: Postman

**1. Login:**

```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**2. Copy token dari response**

**3. Set Authorization Header:**

```
GET http://localhost:8081/api/siswa
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Di Postman:
- Tab "Authorization"
- Type: "Bearer Token"
- Paste token

### Tool 3: Spring Boot Logs

Enable debug logging:

```properties
# application.properties
logging.level.org.springframework.security=DEBUG
```

Lihat log:

```
2024-11-16 12:00:00 DEBUG JwtAuthenticationFilter : JWT Token found in request
2024-11-16 12:00:00 DEBUG JwtUtil : Validating token for user: admin
2024-11-16 12:00:00 DEBUG JwtUtil : Token is valid
2024-11-16 12:00:00 DEBUG SecurityContextHolder : Set Authentication: admin [ROLE_ADMIN]
```

---

## Part 9: Common Errors & Solutions

### Error 1: "Full authentication is required"

**Penyebab:**
- Token tidak dikirim
- Token format salah

**Solusi:**
```
✅ Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
❌ Authorization: eyJhbGciOiJIUzI1NiJ9...  (missing "Bearer")
❌ Authorization Bearer eyJhbGciOiJIUzI1NiJ9...  (missing ":")
```

### Error 2: "JWT signature does not match"

**Penyebab:**
- Secret key berbeda antara generate & verify
- Token diubah oleh user

**Solusi:**
- Pastikan `app.jwt.secret` sama
- Login ulang untuk dapat token baru

### Error 3: "JWT expired"

**Penyebab:**
- Token sudah melewati waktu expiration

**Solusi:**
- Login ulang untuk dapat token baru
- Increase expiration time di `application.properties`

### Error 4: "Bad credentials"

**Penyebab:**
- Username atau password salah
- Password belum di-hash dengan benar

**Solusi:**
- Cek username (case-sensitive!)
- Cek password (case-sensitive!)
- Cek DataSeeder sudah run

### Error 5: "Access Denied" (403 Forbidden)

**Penyebab:**
- User tidak punya role yang diperlukan
- `@PreAuthorize` salah konfigurasi

**Solusi:**
```java
// Cek role user
GET /api/auth/me  // Lihat roles user

// Cek @PreAuthorize di controller
@PreAuthorize("hasRole('ADMIN')")  // User harus punya ROLE_ADMIN
```

---

## Part 10: Flow Lengkap - Dari Register sampai Access Data

### Step-by-Step Flow

**Step 1: Register User Baru**

```
POST /api/auth/register
{
  "username": "siswa01",
  "email": "siswa01@smk.sch.id",
  "password": "password123",
  "role": "ROLE_SISWA"
}

→ AuthController.register()
→ AuthService.register()
   → Check username tersedia
   → Hash password dengan BCrypt
   → Assign role ROLE_SISWA
   → Save user ke database

← Response: "User registered successfully"
```

**Step 2: Login**

```
POST /api/auth/login
{
  "username": "siswa01",
  "password": "password123"
}

→ AuthController.login()
→ AuthService.login()
   → AuthenticationManager.authenticate()
      → DaoAuthenticationProvider
         → UserDetailsService.loadUserByUsername()
            → Database: SELECT * FROM users WHERE username = ?
         → PasswordEncoder.matches(input, hash)
            → BCrypt compare
         → ✅ Authentication success
   → JwtUtil.generateToken()
      → Create payload: {sub, iat, exp, roles}
      → Sign with secret key
      → Return token

← Response: {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "username": "siswa01",
    "roles": ["ROLE_SISWA"]
  }
```

**Step 3: Access Protected Endpoint**

```
GET /api/siswa/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

→ JwtAuthenticationFilter.doFilterInternal()
   → Extract token dari header "Authorization: Bearer ..."
   → JwtUtil.validateToken(token)
      → Check signature (verify with secret key)
      → Check expiration
      → ✅ Token valid
   → JwtUtil.getUsernameFromToken(token)
      → Extract subject claim: "siswa01"
   → UserDetailsService.loadUserByUsername("siswa01")
      → Database: SELECT * FROM users WHERE username = ?
   → Create Authentication object
   → SecurityContextHolder.getContext().setAuthentication(auth)

→ FilterSecurityInterceptor
   → Check @PreAuthorize("hasRole('SISWA')")
   → User has ROLE_SISWA
   → ✅ Access granted

→ SiswaController.getMyProfile()
   → Get username dari SecurityContext
   → Query siswa by username
   → Return siswa data

← Response: {
    "id": 1,
    "nis": "2024001",
    "nama": "Budi Santoso",
    "kelas": "XII RPL 1"
  }
```

**Step 4: Try Access Admin Endpoint (Should Fail)**

```
DELETE /api/siswa/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...  (siswa01 token)

→ JwtAuthenticationFilter.doFilterInternal()
   → Token valid ✅
   → User: siswa01, Roles: [ROLE_SISWA]

→ FilterSecurityInterceptor
   → Check @PreAuthorize("hasRole('ADMIN')")
   → User has ROLE_SISWA (not ADMIN)
   → ❌ Access denied

← Response: 403 Forbidden
   {
     "timestamp": "2024-11-16T12:00:00",
     "status": 403,
     "error": "Forbidden",
     "message": "Access Denied"
   }
```

---

## Kesimpulan

Setelah membaca blog ini, kamu seharusnya paham:

✅ **Perbedaan Authentication vs Authorization**
- Authentication = Siapa kamu? (verify identity)
- Authorization = Kamu boleh ngapain? (check permissions)

✅ **Kenapa Pakai JWT daripada Session**
- Stateless (server tidak simpan state)
- Scalable (bisa di-distribute ke multiple server)
- Mobile-friendly (token di header, bukan cookie)

✅ **Struktur JWT Token**
- Header: Algoritma & type
- Payload: User info (username, roles, expiration)
- Signature: Bukti keaslian (sign dengan secret key)

✅ **Password Hashing dengan BCrypt**
- One-way hash (tidak bisa di-decode)
- Random salt (password sama, hash beda)
- Slow by design (prevent brute force)

✅ **Spring Security Filter Chain**
- JwtAuthenticationFilter: Extract & validate token
- FilterSecurityInterceptor: Check permissions
- Urutan filter penting!

✅ **Role-Based Access Control**
- Role: ADMIN, GURU, SISWA
- @PreAuthorize: Protect endpoint berdasarkan role
- SecurityContext: Simpan authentication info

---

## Next Steps

Sekarang kamu sudah paham **konsep**-nya, saatnya **implementasi**!

Ikuti **TASK-3.md** untuk implementasi step-by-step:
1. Add dependencies (Spring Security & JWT)
2. Create entities (User & Role)
3. Create repositories
4. Create JWT utility class
5. Create security configuration
6. Create authentication controller
7. Test dengan Postman

**Selamat belajar! 🚀🔐**

---

## Referensi

- Spring Security Official Docs: https://spring.io/projects/spring-security
- JWT Introduction: https://jwt.io/introduction
- BCrypt Explained: https://en.wikipedia.org/wiki/Bcrypt
- OWASP Security Cheatsheet: https://cheatsheetseries.owasp.org/

---

**Author:** SIJA Spring Boot Training Team  
**Last Updated:** November 16, 2025  
**Version:** 1.0

**Happy Coding! 💻✨**
