# TAHAP 03 - TASK 12 & 13 COMPLETED ✅

## 📋 RINGKASAN

Tasks 12-13 telah selesai diimplementasikan:
- ✅ **Task 12**: Update Siswa/Guru entity dengan User relation (OneToOne)
- ✅ **Task 13**: Secure existing endpoints dengan @PreAuthorize (Role-Based Access Control)

---

## 🔗 TASK 12: User Relationship di Entity

### Siswa.java - Tambah User Field

```java
/**
 * USER - Relasi ke User (akun login)
 * 
 * Tidak semua siswa punya akun login.
 * Hanya siswa yang perlu akses sistem yang diberi akun.
 * 
 * Relasi: OneToOne (1 siswa max 1 user, 1 user max 1 siswa)
 * 
 * @OneToOne: Relasi one-to-one dengan User
 * @JoinColumn: Foreign key di tabel siswa
 *   - name = "user_id": Nama kolom FK di tabel siswa
 *   - nullable = true: Boleh null (siswa tidak wajib punya user)
 *   - unique = true: Satu user hanya boleh link ke 1 siswa
 * 
 * Contoh:
 * Siswa A punya user → bisa login ke sistem
 * Siswa B tidak punya user → tidak bisa login (data master saja)
 */
@OneToOne
@JoinColumn(name = "user_id", nullable = true, unique = true)
private User user;  // Akun login siswa (opsional)

// Getter & Setter
public User getUser() {
    return user;
}
public void setUser(User user) {
    this.user = user;
}
```

### Guru.java - Tambah User Field

```java
/**
 * USER - Relasi ke User (akun login)
 * 
 * Tidak semua guru punya akun login.
 * Hanya guru yang perlu akses sistem yang diberi akun.
 * 
 * Relasi: OneToOne (1 guru max 1 user, 1 user max 1 guru)
 */
@OneToOne
@JoinColumn(name = "user_id", nullable = true, unique = true)
private User user;  // Akun login guru (opsional)

// Getter & Setter
public User getUser() {
    return user;
}
public void setUser(User user) {
    this.user = user;
}
```

### Database Schema

Setelah update entity:

**Tabel siswa:**
```sql
CREATE TABLE siswa (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nis VARCHAR(255) UNIQUE NOT NULL,
    nama VARCHAR(255) NOT NULL,
    kelas VARCHAR(255),
    jurusan VARCHAR(255),
    rfid_card_id VARCHAR(255),
    barcode_id VARCHAR(255),
    face_id VARCHAR(255),
    user_id BIGINT UNIQUE,  -- NEW: FK ke tabel users
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**Tabel guru:**
```sql
CREATE TABLE guru (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nip VARCHAR(255) UNIQUE NOT NULL,
    nama VARCHAR(255) NOT NULL,
    mapel VARCHAR(255),
    rfid_card_id VARCHAR(255),
    barcode_id VARCHAR(255),
    face_id VARCHAR(255),
    user_id BIGINT UNIQUE,  -- NEW: FK ke tabel users
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Penjelasan OneToOne Relationship

1. **Optional (nullable = true)**
   - Tidak semua siswa/guru punya akun login
   - Admin bisa tambah data siswa/guru tanpa buat user dulu
   - User bisa di-link belakangan

2. **Unique (unique = true)**
   - 1 user hanya bisa link ke 1 siswa ATAU 1 guru
   - Tidak bisa 1 user untuk 2 siswa berbeda
   - Menjaga integritas data

3. **Foreign Key di Tabel siswa/guru**
   - Kolom `user_id` ada di tabel siswa/guru (bukan di users)
   - Arah relasi: Siswa → User, Guru → User
   - Query: `SELECT * FROM siswa s JOIN users u ON s.user_id = u.id`

---

## 🔒 TASK 13: Role-Based Access Control

### SiswaController - Access Rules

```java
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/siswa")
public class SiswaController {
    
    // GET ALL - ADMIN & GURU bisa lihat semua siswa
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<SiswaResponse> getAll() { ... }
    
    // GET BY ID - ADMIN & GURU bisa lihat detail siswa
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public SiswaResponse getById(@PathVariable Long id) { ... }
    
    // CREATE - Hanya ADMIN yang bisa tambah siswa
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiswaResponse> create(...) { ... }
    
    // UPDATE - Hanya ADMIN yang bisa update siswa
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SiswaResponse update(...) { ... }
    
    // DELETE - Hanya ADMIN yang bisa hapus siswa
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
    
    // GET BY KELAS - ADMIN & GURU bisa lihat per kelas
    @GetMapping("/kelas/{namaKelas}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<SiswaResponse> getByKelas(...) { ... }
}
```

### GuruController - Access Rules

**Files Baru Dibuat:**
- `GuruRequest.java` - DTO untuk create/update guru
- `GuruResponse.java` - DTO untuk response API
- `GuruService.java` - Business logic guru
- `GuruController.java` - REST endpoints guru

```java
@RestController
@RequestMapping("/api/guru")
public class GuruController {
    
    // GET ALL - ADMIN & GURU bisa lihat semua guru
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<GuruResponse> getAll() { ... }
    
    // GET BY ID - ADMIN & GURU bisa lihat detail guru
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public GuruResponse getById(@PathVariable Long id) { ... }
    
    // CREATE - Hanya ADMIN yang bisa tambah guru
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuruResponse> create(...) { ... }
    
    // UPDATE - Hanya ADMIN yang bisa update guru
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GuruResponse update(...) { ... }
    
    // DELETE - Hanya ADMIN yang bisa hapus guru
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### Role Access Matrix

| Endpoint | ADMIN | GURU | SISWA |
|----------|-------|------|-------|
| **Siswa Endpoints** |
| GET /api/siswa | ✅ | ✅ | ❌ |
| GET /api/siswa/{id} | ✅ | ✅ | ❌ |
| POST /api/siswa | ✅ | ❌ | ❌ |
| PUT /api/siswa/{id} | ✅ | ❌ | ❌ |
| DELETE /api/siswa/{id} | ✅ | ❌ | ❌ |
| GET /api/siswa/kelas/{kelas} | ✅ | ✅ | ❌ |
| **Guru Endpoints** |
| GET /api/guru | ✅ | ✅ | ❌ |
| GET /api/guru/{id} | ✅ | ✅ | ❌ |
| POST /api/guru | ✅ | ❌ | ❌ |
| PUT /api/guru/{id} | ✅ | ❌ | ❌ |
| DELETE /api/guru/{id} | ✅ | ❌ | ❌ |
| **Auth Endpoints** |
| POST /api/auth/login | ✅ | ✅ | ✅ (Public) |
| POST /api/auth/register | ✅ | ✅ | ✅ (Public) |

### Cara Kerja @PreAuthorize

1. **Request masuk ke endpoint**
   ```
   GET /api/siswa
   Authorization: Bearer <JWT_TOKEN>
   ```

2. **JwtAuthenticationFilter jalan**
   - Extract token dari header
   - Validasi token (signature, expiration)
   - Set Authentication ke SecurityContext
   - Authentication berisi username & roles

3. **@PreAuthorize dicek**
   ```java
   @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
   ```
   - Spring Security cek role user dari Authentication
   - Jika user punya role ADMIN atau GURU → LANJUT ke method
   - Jika tidak → throw AccessDeniedException → 403 Forbidden

4. **Method controller dijalankan**
   ```java
   public List<SiswaResponse> getAll() {
       return siswaService.findAll();
   }
   ```

5. **Response dikirim ke client**

### Error Responses

**401 Unauthorized** - Tidak login / token invalid:
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

**403 Forbidden** - Login tapi role tidak cukup:
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

## 🧪 TESTING

### Scenario 1: ADMIN - Full Access

```bash
# Login sebagai admin
POST http://localhost:8081/api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

# Response: dapat token
{
  "token": "eyJhbGc...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}

# Test GET siswa - ✅ SUCCESS
GET http://localhost:8081/api/siswa
Authorization: Bearer eyJhbGc...

# Test POST siswa - ✅ SUCCESS
POST http://localhost:8081/api/siswa
Authorization: Bearer eyJhbGc...
{
  "nis": "2024001",
  "nama": "Budi",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}

# Test DELETE siswa - ✅ SUCCESS
DELETE http://localhost:8081/api/siswa/1
Authorization: Bearer eyJhbGc...
```

### Scenario 2: GURU - Read Only

```bash
# Login sebagai guru (setelah register dengan role GURU)
POST http://localhost:8081/api/auth/login
{
  "username": "pak_budi",
  "password": "guru123"
}

# Test GET siswa - ✅ SUCCESS
GET http://localhost:8081/api/siswa
Authorization: Bearer <GURU_TOKEN>

# Test POST siswa - ❌ 403 FORBIDDEN
POST http://localhost:8081/api/siswa
Authorization: Bearer <GURU_TOKEN>
{
  "nis": "2024002",
  "nama": "Ani",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}

# Response:
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### Scenario 3: SISWA - No Access

```bash
# Login sebagai siswa
POST http://localhost:8081/api/auth/login
{
  "username": "budi_siswa",
  "password": "siswa123"
}

# Test GET siswa - ❌ 403 FORBIDDEN
GET http://localhost:8081/api/siswa
Authorization: Bearer <SISWA_TOKEN>

# Response:
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### Scenario 4: No Token - Unauthorized

```bash
# Akses endpoint tanpa token
GET http://localhost:8081/api/siswa

# Response: ❌ 401 UNAUTHORIZED
{
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

---

## 📊 STATISTIK

### Files Modified/Created

**Modified (Task 12):**
1. `backend/src/main/java/com/smk/presensi/entity/Siswa.java`
   - ➕ User field dengan @OneToOne
   - ➕ Getter/setter untuk user
   - ➕ ~50 baris komentar penjelasan

2. `backend/src/main/java/com/smk/presensi/entity/Guru.java`
   - ➕ User field dengan @OneToOne
   - ➕ Getter/setter untuk user
   - ➕ ~50 baris komentar penjelasan

**Modified (Task 13 - Siswa):**
3. `backend/src/main/java/com/smk/presensi/controller/SiswaController.java`
   - ➕ Import PreAuthorize
   - ➕ @PreAuthorize di 6 endpoints
   - Total: ~560 baris

**Created (Task 13 - Guru):**
4. `backend/src/main/java/com/smk/presensi/dto/GuruRequest.java` - 35 baris
5. `backend/src/main/java/com/smk/presensi/dto/GuruResponse.java` - 35 baris
6. `backend/src/main/java/com/smk/presensi/service/GuruService.java` - 120 baris
7. `backend/src/main/java/com/smk/presensi/controller/GuruController.java` - 110 baris

**Total:**
- Files modified: 3
- Files created: 4
- Total lines added: ~400 baris (dengan komentar)

### Build Status
```
[INFO] BUILD SUCCESS
[INFO] Total time: 7.136 s
[INFO] Compiling 31 source files
```

---

## ✅ SUCCESS CRITERIA

### Task 12: User Relationship
- ✅ Siswa.java punya field User dengan @OneToOne
- ✅ Guru.java punya field User dengan @OneToOne
- ✅ Relationship optional (nullable = true)
- ✅ Relationship unique (1 user = 1 siswa/guru max)
- ✅ Getter/setter ditambahkan
- ✅ Komentar lengkap untuk siswa SMK

### Task 13: Role-Based Access Control
- ✅ SiswaController secured dengan @PreAuthorize
- ✅ GuruController created dengan @PreAuthorize
- ✅ ADMIN: full access semua endpoint
- ✅ GURU: read-only access (GET only)
- ✅ SISWA: no access to siswa/guru endpoints
- ✅ 401 Unauthorized untuk request tanpa token
- ✅ 403 Forbidden untuk request dengan role insufficient
- ✅ GuruRequest, GuruResponse, GuruService created
- ✅ Build berhasil tanpa error

---

## 🎯 NEXT STEPS

Semua task Tahap 03 telah selesai! 🎉

**Summary Tahap 03:**
- ✅ Task 1-2: Documentation (TASK-3.md, blog3.md)
- ✅ Task 3-11: JWT Authentication implementation
- ✅ Task 12: User relationship di entities
- ✅ Task 13: Role-based access control
- ✅ Task 14-15: Testing documentation

**Untuk tahap berikutnya:**
1. Implementasi presensi (RFID, Barcode, Face Recognition)
2. Dashboard dan reporting
3. Real-time notification
4. Export data (Excel, PDF)

---

## 📝 CATATAN PENTING

### User Linking
Untuk link user ke siswa/guru, ada 2 cara:

**Cara 1: Saat register, sekalian link**
```java
// Di AuthService.register()
User user = new User();
user.setUsername(request.username());
// ... set fields lain

// Jika register untuk siswa, link ke siswa
if (request.siswaId() != null) {
    Siswa siswa = siswaRepository.findById(request.siswaId()).orElseThrow();
    user.setSiswa(siswa);  // Asumsi ada bidirectional relation
}

userRepository.save(user);
```

**Cara 2: Endpoint terpisah untuk linking**
```java
// PUT /api/siswa/{id}/link-user
public SiswaResponse linkUser(@PathVariable Long id, @RequestParam Long userId) {
    Siswa siswa = siswaRepository.findById(id).orElseThrow();
    User user = userRepository.findById(userId).orElseThrow();
    
    siswa.setUser(user);
    siswaRepository.save(siswa);
    
    return toResponse(siswa);
}
```

### Security Best Practices
1. Selalu gunakan HTTPS di production
2. Simpan JWT secret di environment variable
3. Set JWT expiration sesuai kebutuhan (saat ini 24 jam)
4. Implementasi refresh token untuk UX lebih baik
5. Log semua akses unauthorized untuk security audit

---

**Dokumentasi dibuat:** 2025-01-16  
**Status:** ✅ COMPLETE  
**Build:** SUCCESS  
**Tests:** READY
