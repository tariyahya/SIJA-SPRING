# User Management API - Testing dengan Postman

## Prerequisites
1. Backend Spring Boot sudah running di `http://localhost:8081`
2. PostgreSQL database `presensi_sija` sudah dibuat
3. Sudah login dan mendapat JWT token (role ADMIN)

---

## 1. Login Sebagai Admin

Sebelum test User API, kita harus login dulu untuk mendapatkan JWT token.

### Request

```
POST http://localhost:8081/api/auth/login
Content-Type: application/json
```

**Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Response (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "role": "ROLE_ADMIN"
}
```

**PENTING:** Copy `token` dari response. Gunakan untuk request berikutnya.

---

## 2. Setup Authorization Header di Postman

Untuk semua request User API, tambahkan header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Cara di Postman:
1. Buka tab "Authorization"
2. Type: "Bearer Token"
3. Token: Paste token dari login response

---

## 3. GET All Users

Get list semua users (admin only).

### Request

```
GET http://localhost:8081/api/users
Authorization: Bearer <your-token>
```

### Response (200 OK)

```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@smk.sch.id",
    "enabled": true,
    "roles": ["ROLE_ADMIN"],
    "siswaId": null,
    "guruId": null
  },
  {
    "id": 2,
    "username": "guru01",
    "email": "guru01@smk.sch.id",
    "enabled": true,
    "roles": ["ROLE_GURU"],
    "siswaId": null,
    "guruId": 1
  }
]
```

### Test di Postman:
1. Method: **GET**
2. URL: `http://localhost:8081/api/users`
3. Headers: Authorization Bearer Token
4. Klik **Send**
5. Status harus **200 OK**

---

## 4. GET User by ID

Get detail user berdasarkan ID.

### Request

```
GET http://localhost:8081/api/users/1
Authorization: Bearer <your-token>
```

### Response (200 OK)

```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@smk.sch.id",
  "enabled": true,
  "roles": ["ROLE_ADMIN"],
  "siswaId": null,
  "guruId": null
}
```

### Response (404 NOT FOUND) - User tidak ditemukan

```json
{
  "timestamp": "2025-11-18T22:45:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "User dengan ID 999 tidak ditemukan",
  "path": "/api/users/999"
}
```

### Test di Postman:
1. Method: **GET**
2. URL: `http://localhost:8081/api/users/1`
3. Headers: Authorization Bearer Token
4. Klik **Send**

---

## 5. POST Create New User

Buat user baru.

### Request

```
POST http://localhost:8081/api/users
Authorization: Bearer <your-token>
Content-Type: application/json
```

**Body:**
```json
{
  "username": "siswa001",
  "password": "password123",
  "email": "siswa001@smk.sch.id",
  "role": "ROLE_SISWA"
}
```

### Response (201 CREATED)

```json
{
  "id": 5,
  "username": "siswa001",
  "email": "siswa001@smk.sch.id",
  "enabled": true,
  "roles": ["ROLE_SISWA"],
  "siswaId": null,
  "guruId": null
}
```

### Response (400 BAD REQUEST) - Username sudah ada

```json
{
  "timestamp": "2025-11-18T22:45:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Username siswa001 sudah digunakan",
  "path": "/api/users"
}
```

### Test di Postman:
1. Method: **POST**
2. URL: `http://localhost:8081/api/users`
3. Headers: 
   - Authorization: Bearer Token
   - Content-Type: application/json
4. Body: raw JSON (lihat contoh di atas)
5. Klik **Send**

### Contoh User Lain:

**Guru:**
```json
{
  "username": "guru_ipa",
  "password": "guru123",
  "email": "guru_ipa@smk.sch.id",
  "role": "ROLE_GURU"
}
```

**Admin:**
```json
{
  "username": "admin2",
  "password": "admin456",
  "email": "admin2@smk.sch.id",
  "role": "ROLE_ADMIN"
}
```

---

## 6. PUT Update User

Update data user yang sudah ada.

### Request

```
PUT http://localhost:8081/api/users/5
Authorization: Bearer <your-token>
Content-Type: application/json
```

**Body:**
```json
{
  "username": "siswa001_updated",
  "password": "",
  "email": "siswa001_new@smk.sch.id",
  "role": "ROLE_SISWA"
}
```

**Note:** 
- Jika `password` kosong, password lama tidak akan diubah
- Jika `password` diisi, akan di-hash dan update

### Response (200 OK)

```json
{
  "id": 5,
  "username": "siswa001_updated",
  "email": "siswa001_new@smk.sch.id",
  "enabled": true,
  "roles": ["ROLE_SISWA"],
  "siswaId": null,
  "guruId": null
}
```

### Test di Postman:
1. Method: **PUT**
2. URL: `http://localhost:8081/api/users/5`
3. Headers: 
   - Authorization: Bearer Token
   - Content-Type: application/json
4. Body: raw JSON
5. Klik **Send**

---

## 7. DELETE User

Hapus user berdasarkan ID.

### Request

```
DELETE http://localhost:8081/api/users/5
Authorization: Bearer <your-token>
```

### Response (204 NO CONTENT)

Tidak ada body response. Status 204 berarti sukses delete.

### Response (404 NOT FOUND)

```json
{
  "timestamp": "2025-11-18T22:45:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "User dengan ID 999 tidak ditemukan",
  "path": "/api/users/999"
}
```

### Test di Postman:
1. Method: **DELETE**
2. URL: `http://localhost:8081/api/users/5`
3. Headers: Authorization Bearer Token
4. Klik **Send**
5. Status harus **204 No Content**

---

## 8. Error Responses

### 401 Unauthorized - Token tidak valid/expired

```json
{
  "timestamp": "2025-11-18T22:45:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/users"
}
```

**Solusi:** Login ulang untuk mendapat token baru.

### 403 Forbidden - Bukan admin

```json
{
  "timestamp": "2025-11-18T22:45:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/users"
}
```

**Penyebab:** User yang login bukan ROLE_ADMIN.
**Solusi:** Login sebagai admin.

---

## 9. Checklist Testing

- [ ] Login berhasil, dapat JWT token
- [ ] GET /api/users berhasil, muncul list users
- [ ] GET /api/users/{id} berhasil, muncul detail user
- [ ] POST /api/users berhasil create user SISWA
- [ ] POST /api/users berhasil create user GURU  
- [ ] POST /api/users berhasil create user ADMIN
- [ ] POST /api/users dengan username duplikat → error
- [ ] PUT /api/users/{id} berhasil update user
- [ ] DELETE /api/users/{id} berhasil hapus user
- [ ] Request tanpa Authorization header → 401 Unauthorized
- [ ] Request dengan role bukan ADMIN → 403 Forbidden

---

## 10. Tips Postman

### Save ke Collection
1. Klik "Save" setelah test berhasil
2. Nama collection: "User Management API"
3. Request names:
   - `[POST] Login Admin`
   - `[GET] Get All Users`
   - `[GET] Get User by ID`
   - `[POST] Create User`
   - `[PUT] Update User`
   - `[DELETE] Delete User`

### Use Environment Variables
1. Buat environment "Local"
2. Variables:
   ```
   base_url = http://localhost:8081
   token = <paste-token-setelah-login>
   ```
3. Gunakan di URL: `{{base_url}}/api/users`
4. Gunakan di Authorization: `{{token}}`

### Export Collection
1. Klik tiga titik di collection
2. Export → Collection v2.1
3. Save sebagai `User-API.postman_collection.json`

---

## 11. Next Steps

Setelah User API berhasil:
1. Test dengan Desktop App (User Management window)
2. Verifikasi data tersimpan di PostgreSQL
3. Test authorization (user GURU/SISWA tidak bisa akses User API)

**Selamat Testing!** 🚀
