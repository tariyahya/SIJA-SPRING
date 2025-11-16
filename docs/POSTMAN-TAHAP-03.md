# Postman Testing Guide - Tahap 03: JWT Authentication

**Author:** SIJA Spring Boot Training Team  
**Date:** November 16, 2025  
**Base URL:** `http://localhost:8081`

---

## Prerequisites

1. ✅ Spring Boot aplikasi sudah running (port 8081)
2. ✅ DataSeeder sudah run (roles & admin user created)
3. ✅ Postman installed

---

## Test Flow Overview

```
1. POST /api/auth/register → Register user baru
2. POST /api/auth/login → Login & get JWT token
3. GET /api/siswa → Access protected endpoint (TANPA token) → 401 Unauthorized
4. GET /api/siswa → Access protected endpoint (DENGAN token) → 200 OK
```

---

## Test 1: Register User Baru

### Request

**Method:** `POST`  
**URL:** `http://localhost:8081/api/auth/register`  
**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "username": "siswa01",
  "email": "siswa01@smk.sch.id",
  "password": "password123",
  "role": "ROLE_SISWA"
}
```

### Expected Response

**Status:** `201 Created`

**Response Body:**
```json
{
  "message": "User registered successfully"
}
```

### Validation Tests

**Test 1.1: Username kosong (Error)**

```json
{
  "username": "",
  "email": "test@smk.sch.id",
  "password": "password123"
}
```

**Expected:** `400 Bad Request` - "Username tidak boleh kosong"

---

**Test 1.2: Username terlalu pendek (Error)**

```json
{
  "username": "ab",
  "email": "test@smk.sch.id",
  "password": "password123"
}
```

**Expected:** `400 Bad Request` - "Username harus 3-20 karakter"

---

**Test 1.3: Email format salah (Error)**

```json
{
  "username": "siswa02",
  "email": "invalid-email",
  "password": "password123"
}
```

**Expected:** `400 Bad Request` - "Format email tidak valid"

---

**Test 1.4: Password terlalu pendek (Error)**

```json
{
  "username": "siswa02",
  "email": "siswa02@smk.sch.id",
  "password": "12345"
}
```

**Expected:** `400 Bad Request` - "Password harus 6-40 karakter"

---

**Test 1.5: Username sudah dipakai (Error)**

```json
{
  "username": "siswa01",
  "email": "duplicate@smk.sch.id",
  "password": "password123"
}
```

**Expected:** `400 Bad Request` - "Username sudah digunakan!"

---

## Test 2: Login User

### Request

**Method:** `POST`  
**URL:** `http://localhost:8081/api/auth/login`  
**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Expected Response

**Status:** `200 OK`

**Response Body:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDEyMzQ1NiwiZXhwIjoxNzAwMjA5ODU2LCJyb2xlcyI6IlJPTEVfQURNSU4ifQ.signature_here",
  "type": "Bearer",
  "username": "admin",
  "roles": [
    "ROLE_ADMIN"
  ]
}
```

**PENTING:** Copy value `token` untuk dipakai di Test 3 & 4!

### Decode JWT Token (Optional - Educational)

1. Copy token dari response
2. Buka https://jwt.io/
3. Paste token di kotak "Encoded"
4. Lihat decoded payload:

```json
{
  "sub": "admin",
  "iat": 1700123456,
  "exp": 1700209856,
  "roles": "ROLE_ADMIN"
}
```

**Penjelasan:**
- `sub` (subject): Username
- `iat` (issued at): Waktu token dibuat (Unix timestamp)
- `exp` (expiration): Waktu token expired (iat + 24 jam)
- `roles`: Roles user

### Validation Tests

**Test 2.1: Username salah (Error)**

```json
{
  "username": "wronguser",
  "password": "admin123"
}
```

**Expected:** `401 Unauthorized` - "Bad credentials"

---

**Test 2.2: Password salah (Error)**

```json
{
  "username": "admin",
  "password": "wrongpassword"
}
```

**Expected:** `401 Unauthorized` - "Bad credentials"

---

**Test 2.3: Username kosong (Error)**

```json
{
  "username": "",
  "password": "admin123"
}
```

**Expected:** `400 Bad Request` - "Username tidak boleh kosong"

---

## Test 3: Access Protected Endpoint (TANPA Token)

### Request

**Method:** `GET`  
**URL:** `http://localhost:8081/api/siswa`  
**Headers:** (NONE - intentionally tidak kirim Authorization header)

### Expected Response

**Status:** `401 Unauthorized`

**Response Body:**
```json
{
  "timestamp": "2025-11-16T09:15:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/siswa"
}
```

**Explanation:** Endpoint `/api/siswa` requires authentication. Karena tidak ada JWT token, request ditolak.

---

## Test 4: Access Protected Endpoint (DENGAN Token)

### Request

**Method:** `GET`  
**URL:** `http://localhost:8081/api/siswa`  
**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**PENTING:** 
- Ganti `eyJhbGciOiJIUzUxMiJ9...` dengan token yang didapat dari Test 2
- Harus ada spasi setelah "Bearer"
- Format: `Bearer <token>`

### Di Postman:

1. Pilih tab **"Authorization"**
2. Type: **"Bearer Token"**
3. Token: Paste token dari Test 2 (tanpa "Bearer" prefix)
4. Postman otomatis add header: `Authorization: Bearer <token>`

### Expected Response

**Status:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "nis": "2024001",
    "nama": "Budi Santoso",
    "kelas": "XII RPL 1",
    "jurusan": "RPL"
  },
  {
    "id": 2,
    "nis": "2024002",
    "nama": "Siti Nurhaliza",
    "kelas": "XII RPL 1",
    "jurusan": "RPL"
  }
]
```

**Explanation:** JWT token valid, user authenticated, akses granted!

---

## Test 5: Login dengan User Siswa

### Step 5.1: Register Siswa User

**Method:** `POST`  
**URL:** `http://localhost:8081/api/auth/register`

**Body:**
```json
{
  "username": "siswa01",
  "email": "siswa01@smk.sch.id",
  "password": "password123",
  "role": "ROLE_SISWA"
}
```

**Expected:** `201 Created`

### Step 5.2: Login dengan Siswa User

**Method:** `POST`  
**URL:** `http://localhost:8081/api/auth/login`

**Body:**
```json
{
  "username": "siswa01",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "siswa01",
  "roles": [
    "ROLE_SISWA"
  ]
}
```

**Note:** Token ini punya role ROLE_SISWA (bukan ROLE_ADMIN)

### Step 5.3: Access Endpoint dengan Siswa Token

**Method:** `GET`  
**URL:** `http://localhost:8081/api/siswa`  
**Headers:**
```
Authorization: Bearer <siswa_token>
```

**Expected Response:** `200 OK` (untuk sekarang, karena belum ada @PreAuthorize)

**Note:** Di Task 13, kita akan add role-based access control. Setelah itu, ROLE_SISWA mungkin tidak bisa akses endpoint tertentu.

---

## Test 6: Token Expired

### Scenario

JWT token punya expiration time (default: 24 jam).

### Test Steps

1. Login → Get token
2. Tunggu sampai token expired (24 jam)
3. Atau, ubah `app.jwt.expiration` di application.properties jadi 60000 (1 menit) untuk testing
4. Restart aplikasi
5. Login → Get token
6. Tunggu 1 menit
7. Access protected endpoint dengan token expired

### Expected Response

**Status:** `401 Unauthorized`

**Logs (di console):**
```
ERROR [JwtAuthenticationFilter] : Cannot set user authentication: JWT token is expired
```

**Solution:** User harus login ulang untuk dapat token baru.

---

## Test 7: Invalid Token (Diubah)

### Scenario

User coba ubah token (contoh: ubah role dari SISWA jadi ADMIN)

### Test Steps

1. Login → Get token
2. Copy token
3. Decode di https://jwt.io/
4. Ubah payload (contoh: ubah username atau roles)
5. Copy "encoded" token (yang sudah diubah)
6. Access protected endpoint dengan token yang diubah

### Expected Response

**Status:** `401 Unauthorized`

**Logs (di console):**
```
ERROR [JwtAuthenticationFilter] : Cannot set user authentication: Invalid JWT signature
```

**Explanation:** Signature tidak cocok karena payload diubah. JWT validation failed.

---

## Common Errors & Solutions

### Error 1: "Full authentication is required"

**Penyebab:**
- Token tidak dikirim
- Token format salah

**Solusi:**
```
✅ Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
❌ Authorization: eyJhbGciOiJIUzUxMiJ9...  (missing "Bearer")
❌ Authorization Bearer eyJhbGciOiJIUzUxMiJ9...  (missing ":")
```

### Error 2: "Bad credentials"

**Penyebab:**
- Username atau password salah

**Solusi:**
- Cek username (case-sensitive!)
- Cek password (case-sensitive!)
- Test dengan admin default: username=admin, password=admin123

### Error 3: "Username sudah digunakan"

**Penyebab:**
- Username sudah dipakai user lain

**Solusi:**
- Pilih username lain
- Atau hapus user existing dari database

### Error 4: "Role tidak ditemukan"

**Penyebab:**
- Role name salah (harus: ROLE_ADMIN, ROLE_GURU, atau ROLE_SISWA)

**Solusi:**
- Cek typo di role name
- Pastikan role name pakai prefix "ROLE_"

---

## Postman Collection Export

### Create Postman Collection

1. Buat collection baru: "SIJA Presensi - Tahap 03 (JWT Auth)"
2. Add request untuk setiap test di atas
3. Organize dengan folders:
   - 📁 Authentication
     - POST Register
     - POST Login
   - 📁 Protected Endpoints
     - GET Siswa (No Token)
     - GET Siswa (With Token)
4. Add environment variables:
   - `base_url`: http://localhost:8081
   - `jwt_token`: (will be set from login response)

### Use Variables in Postman

**1. Set JWT token variable after login:**

Di tab "Tests" untuk endpoint POST /api/auth/login:

```javascript
// Extract token from response
var jsonData = pm.response.json();
var token = jsonData.token;

// Save to environment variable
pm.environment.set("jwt_token", token);

console.log("JWT Token saved: " + token);
```

**2. Use token variable in subsequent requests:**

Di tab "Authorization" untuk protected endpoints:
- Type: Bearer Token
- Token: `{{jwt_token}}` (reference variable)

---

## Next Steps

Setelah semua tests berhasil:

✅ **Authentication & Authorization dasar sudah jalan!**

**Tahap selanjutnya (Task 12-13):**
1. Update Siswa/Guru entity dengan User relation
2. Add @PreAuthorize untuk role-based access control:
   - ADMIN: Full access
   - GURU: Read all, manage presensi
   - SISWA: Read own data only

---

**Author:** SIJA Spring Boot Training Team  
**Last Updated:** November 16, 2025  
**Version:** 1.0

**Happy Testing! 🚀🔐**
