# 📋 QUICK REFERENCE - TAHAP 03 JWT AUTH

## 🔑 Admin Credentials (Default)
```
Username: admin
Password: admin123
⚠️ CHANGE IN PRODUCTION!
```

## 🌐 Base URL
```
http://localhost:8081
```

## 📡 API Endpoints Quick Reference

### 🔓 Public Endpoints (No Auth)

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

→ Response:
{
  "token": "eyJhbGc...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "budi_guru",
  "email": "budi@smk.sch.id",
  "password": "guru123",
  "role": "GURU"
}

→ Response:
{
  "message": "User registered successfully!"
}
```

### 🔒 Protected Endpoints (Auth Required)

**Format:**
```http
Authorization: Bearer <your_jwt_token>
```

#### Siswa Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/siswa` | ADMIN, GURU | Get all siswa |
| GET | `/api/siswa/{id}` | ADMIN, GURU | Get siswa by ID |
| POST | `/api/siswa` | ADMIN | Create siswa |
| PUT | `/api/siswa/{id}` | ADMIN | Update siswa |
| DELETE | `/api/siswa/{id}` | ADMIN | Delete siswa |
| GET | `/api/siswa/kelas/{kelas}` | ADMIN, GURU | Get by kelas |

**Example POST:**
```http
POST /api/siswa
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "nis": "2024001",
  "nama": "Budi Santoso",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}
```

#### Guru Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/guru` | ADMIN, GURU | Get all guru |
| GET | `/api/guru/{id}` | ADMIN, GURU | Get guru by ID |
| POST | `/api/guru` | ADMIN | Create guru |
| PUT | `/api/guru/{id}` | ADMIN | Update guru |
| DELETE | `/api/guru/{id}` | ADMIN | Delete guru |

**Example POST:**
```http
POST /api/guru
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "nip": "197505012000011001",
  "nama": "Pak Budi",
  "mapel": "Pemrograman Java"
}
```

---

## 🎭 Roles & Permissions

| Role | Can Do |
|------|--------|
| **ROLE_ADMIN** | ✅ Full CRUD on Siswa & Guru<br>✅ Create/Delete users<br>✅ All operations |
| **ROLE_GURU** | ✅ Read all Siswa<br>✅ Read all Guru<br>❌ Cannot create/update/delete |
| **ROLE_SISWA** | ❌ No access to Siswa endpoints<br>❌ No access to Guru endpoints |

---

## 🔐 Security Headers

### Request Header (Protected endpoints)
```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1...
```

### No Token → 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

### Wrong Role → 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

## ⚙️ JWT Configuration

```properties
# application.properties
app.jwt.secret=yourVeryLongAndSecretKeyForJWTGenerationAndValidation123456789
app.jwt.expiration=86400000  # 24 hours in milliseconds
```

**Token Structure:**
```
eyJhbGciOiJIUzUxMiJ9           ← Header (algorithm)
.
eyJzdWIiOiJhZG1pbiIsImlhdCI... ← Payload (user data)
.
J9Fh8xB4K_xN3cGv5...           ← Signature (verification)
```

---

## 🗄️ Database Schema Quick View

### users
```sql
id | username | password (BCrypt) | email | enabled
```

### roles
```sql
id | name
1  | ROLE_ADMIN
2  | ROLE_GURU
3  | ROLE_SISWA
```

### user_roles (join table)
```sql
user_id | role_id
```

### siswa
```sql
id | nis | nama | kelas | jurusan | user_id (FK)
```

### guru
```sql
id | nip | nama | mapel | user_id (FK)
```

---

## 🧪 Testing Workflow

### 1. Login as Admin
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Copy Token
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."  ← COPY THIS
}
```

### 3. Use Token in Next Request
```bash
curl -X GET http://localhost:8081/api/siswa \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

---

## 🐛 Common Errors & Solutions

### Error 401 Unauthorized
**Cause:** No token or invalid token  
**Solution:**
- Check Authorization header exists
- Check token format: `Bearer <token>`
- Check token not expired (24h limit)
- Re-login to get new token

### Error 403 Forbidden
**Cause:** User role not sufficient  
**Solution:**
- Check endpoint requires which role
- Check user has correct role
- ADMIN can do everything
- GURU can only read
- SISWA cannot access siswa/guru endpoints

### Error 400 Bad Request (Validation)
**Cause:** Required field empty  
**Solution:**
- Check `nis` not empty (siswa)
- Check `nama` not empty
- Check `nip` not empty (guru)

### Error 500 Internal Server Error
**Cause:** Data not found or duplicate  
**Solution:**
- Check ID exists (for GET/PUT/DELETE by ID)
- Check NIS/NIP unique (for POST)

---

## 📊 Role Matrix Cheat Sheet

```
Endpoint              ADMIN  GURU  SISWA  Public
─────────────────────────────────────────────────
POST /auth/login       ✅     ✅    ✅     ✅
POST /auth/register    ✅     ✅    ✅     ✅
GET  /siswa            ✅     ✅    ❌     ❌
POST /siswa            ✅     ❌    ❌     ❌
PUT  /siswa/{id}       ✅     ❌    ❌     ❌
DELETE /siswa/{id}     ✅     ❌    ❌     ❌
GET  /guru             ✅     ✅    ❌     ❌
POST /guru             ✅     ❌    ❌     ❌
PUT  /guru/{id}        ✅     ❌    ❌     ❌
DELETE /guru/{id}      ✅     ❌    ❌     ❌
```

---

## 💡 Pro Tips

### 1. Postman Environment Variables
```javascript
// Save token after login (Tests tab)
pm.environment.set("jwt_token", pm.response.json().token);

// Use in Authorization header
{{jwt_token}}
```

### 2. Check Token Expiration
```bash
# Token valid for 24 hours
# After 24h → 401 Unauthorized
# Solution: Login again to get new token
```

### 3. Role Format
```
✅ Correct: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA
❌ Wrong:   ADMIN, GURU, SISWA
```

### 4. Multiple Roles
```java
// User can have multiple roles
User admin = new User();
admin.setRoles(Set.of(roleAdmin, roleGuru));
// → Can access both ADMIN and GURU endpoints
```

---

## 🚀 Start Application

### Command Line
```bash
cd backend
mvn spring-boot:run
```

### Check if Running
```bash
curl http://localhost:8081/api/hello
→ Should return: "Hello from SMK Presensi API!"
```

### Access H2 Console (Debug)
```
URL: http://localhost:8081/h2-console
JDBC URL: jdbc:h2:mem:presensidb
Username: sa
Password: (empty)
```

---

## 📚 Documentation Links

- **Implementation Guide:** `docs/TASK-3.md`
- **Concept Explanation:** `docs/blog3.md`
- **API Testing Guide:** `docs/POSTMAN-TAHAP-03.md`
- **Project Summary:** `docs/README-TAHAP-03.md`
- **Final Summary:** `docs/FINAL-SUMMARY-TAHAP-03.md`

---

## 🎯 Quick Test Scenarios

### Scenario 1: Admin Full Access
```bash
# 1. Login
POST /auth/login → Get token

# 2. Create siswa (✅ Should work)
POST /siswa + token → 201 Created

# 3. Update siswa (✅ Should work)
PUT /siswa/1 + token → 200 OK

# 4. Delete siswa (✅ Should work)
DELETE /siswa/1 + token → 204 No Content
```

### Scenario 2: Guru Read-Only
```bash
# 1. Login as guru
POST /auth/login → Get token

# 2. Read siswa (✅ Should work)
GET /siswa + token → 200 OK

# 3. Create siswa (❌ Should fail)
POST /siswa + token → 403 Forbidden

# 4. Delete siswa (❌ Should fail)
DELETE /siswa/1 + token → 403 Forbidden
```

### Scenario 3: No Token
```bash
# Try access without token
GET /siswa (no Authorization header)
→ 401 Unauthorized
```

---

## 🏆 Success Criteria

Your implementation is complete when:

- ✅ Can login with admin credentials
- ✅ Receive valid JWT token
- ✅ Can access protected endpoints with token
- ✅ Get 401 without token
- ✅ Get 403 with insufficient role
- ✅ ADMIN can CRUD everything
- ✅ GURU can only read
- ✅ Password is hashed with BCrypt
- ✅ Token expires after 24 hours
- ✅ Application starts without errors

---

**Happy Coding! 🚀**

---

*Last Updated: 2025-01-16*  
*Branch: tahap-03-auth-role*  
*Status: ✅ COMPLETE*
