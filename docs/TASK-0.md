# TASK-0: KEBUTUHAN APLIKASI & SETUP AWAL

**Target**: Persiapan tools, environment, dan pemahaman kebutuhan sistem  
**Estimasi Waktu**: 2-3 JP (Jam Pelajaran)  
**Level**: Beginner  
**Status**: ✅ COMPLETED (sudah ada di Tahap 1-3)

---

## 🎯 TUJUAN PEMBELAJARAN

Setelah menyelesaikan Tahap 0, siswa diharapkan:

1. **Memahami Kebutuhan Fungsional** sistem presensi siswa-guru
2. **Memahami Kebutuhan Non-Fungsional** (security, performance, scalability)
3. **Install Tools Development** (JDK, IDE, database, Postman)
4. **Setup Git & GitHub** untuk version control
5. **Memahami Arsitektur Aplikasi** (3-tier: backend, mobile, desktop)

---

## 📋 KEBUTUHAN APLIKASI

### 1. Kebutuhan Fungsional

#### A. User Management
- [x] **Register** user baru (ADMIN, GURU, SISWA)
- [x] **Login** dengan username & password
- [x] **JWT Authentication** untuk secure API
- [x] **Role-Based Access Control** (RBAC)
  - ADMIN: Full access (CRUD siswa, guru, presensi)
  - GURU: Read-only access (view siswa, presensi)
  - SISWA: Self-service only (view own presensi history)

#### B. Data Master
- [x] **CRUD Siswa** (NIS, nama, kelas, jurusan, RFID, barcode, face)
- [x] **CRUD Guru** (NIP, nama, mata pelajaran, RFID, barcode, face)
- [x] **CRUD Kelas** (nama kelas, wali kelas)
- [ ] **CRUD Mata Pelajaran** (kode, nama mapel) - *future*
- [ ] **CRUD Lokasi Kantor** (koordinat sekolah, radius valid) - *Tahap 8*

#### C. Presensi (Core Feature)

**4 Method Presensi**:
1. **MANUAL** ✅ (Tahap 4)
   - User checkin via smartphone app
   - Input: JWT token (auto user dari token)
   - GPS tracking (latitude, longitude)
   - Status: HADIR / TERLAMBAT (auto by jam masuk)
   - Validasi: no duplicate checkin per hari

2. **RFID** ✅ (Tahap 5)
   - User tap RFID card di reader
   - Input: rfidCardId (string unique)
   - Auto-detect tipe (SISWA / GURU)
   - Public endpoint (no JWT)
   - Validasi: rfidCardId harus exist di database

3. **BARCODE** ✅ (Tahap 6)
   - User scan barcode di ID card
   - Input: barcodeId (string unique)
   - Auto-detect tipe (SISWA / GURU)
   - Public endpoint (no JWT)
   - Validasi: barcodeId harus exist di database

4. **FACE RECOGNITION** ✅ (Tahap 7)
   - User scan wajah di kamera
   - Input: imageBase64 (foto wajah)
   - Auto-detect user via face matching
   - Two-phase: Enrollment + Recognition
   - Public endpoint (no JWT)
   - Validasi: face encoding harus enrolled

**Flow Presensi**:
```
Checkin → Checkout (opsional)
```

**Status Presensi**:
- **HADIR**: Checkin sebelum jam batas (misal 07:15)
- **TERLAMBAT**: Checkin setelah jam batas
- **ALFA**: Tidak checkin sama sekali (report)

**Data yang Disimpan**:
- userId (relasi ke User)
- tipe (SISWA / GURU)
- method (MANUAL / RFID / BARCODE / FACE)
- tanggal (LocalDate)
- jamMasuk (LocalTime)
- jamPulang (LocalTime, nullable)
- status (HADIR / TERLAMBAT)
- latitude, longitude (GPS)
- keterangan (audit trail)

#### D. Reporting
- [ ] **Laporan Harian**: Presensi per hari per kelas
- [ ] **Laporan Bulanan**: Rekapitulasi per bulan
- [ ] **Laporan Siswa**: History presensi siswa tertentu
- [ ] **Laporan Guru**: History presensi guru tertentu
- [ ] **Export**: CSV, Excel, PDF

---

### 2. Kebutuhan Non-Fungsional

#### A. Security
- [x] **JWT Token** untuk authentication
- [x] **Password Hashing** (BCrypt)
- [x] **Role-Based Authorization** (@PreAuthorize)
- [x] **Public Endpoints** untuk hardware (RFID, Barcode, Face)
- [ ] **HTTPS** (production deployment)
- [ ] **Rate Limiting** (prevent abuse)

#### B. Performance
- [x] **Response Time**: < 1 second untuk checkin
- [x] **Concurrent Users**: Support 100+ simultaneous checkin
- [ ] **Database Indexing** (RFID, barcode, face_encoding)
- [ ] **Caching** (Redis for frequently accessed data)

#### C. Usability
- [x] **RESTful API** dengan standar HTTP codes
- [x] **Clear Error Messages** (user-friendly)
- [x] **API Documentation** (Postman collections)
- [ ] **Mobile App** (Android, intuitive UI)
- [ ] **Desktop Admin Panel** (JavaFX, easy navigation)

#### D. Scalability
- [x] **Modular Architecture** (Controller-Service-Repository)
- [x] **DTO Pattern** (decouple entities from API)
- [ ] **Microservices** (future, split by domain)
- [ ] **Load Balancing** (production, multiple instances)

#### E. Maintainability
- [x] **Clean Code** (naming conventions, comments)
- [x] **Git Branching** (feature branches per tahap)
- [x] **Documentation** (TASK, README, POSTMAN per tahap)
- [x] **Consistent Patterns** (copy-paste RFID → Barcode → Face)

---

## 🛠️ TOOLS & ENVIRONMENT

### A. Backend Development

**1. Java Development Kit (JDK)**
- Version: **Java 17** (LTS)
- Download: https://adoptium.net/
- Install path: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`
- Verify:
  ```powershell
  java -version
  # Output: openjdk version "17.x.x"
  ```

**2. Build Tool: Apache Maven**
- Version: **Maven 3.9.x** (atau lebih baru)
- Download: https://maven.apache.org/download.cgi

**Instalasi Maven:**

1. **Download Maven Binary**
   - Kunjungi: https://maven.apache.org/download.cgi
   - Download: `apache-maven-3.9.x-bin.zip`

2. **Extract Maven**
   - Extract ke: `C:\Program Files\Apache\maven` (atau lokasi pilihan Anda)

3. **Set Environment Variables**
   ```powershell
   # Tambahkan MAVEN_HOME
   setx MAVEN_HOME "C:\Program Files\Apache\maven"
   
   # Tambahkan Maven ke PATH
   setx PATH "%PATH%;%MAVEN_HOME%\bin"
   ```

4. **Restart Terminal** (agar environment variables aktif)

5. **Verify Installation**
   ```powershell
   mvn -version
   
   # Expected output:
   # Apache Maven 3.9.x (xxx)
   # Maven home: C:\Program Files\Apache\maven
   # Java version: 17.x.x
   ```

**Alternative: Maven Wrapper (included in project)**
- Project ini sudah include Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Tidak perlu install Maven global jika pakai wrapper
- Verify:
  ```powershell
  cd backend
  .\mvnw -version
  # Output: Apache Maven 3.9.x
  ```

**3. IDE (Integrated Development Environment)**
- Option 1: **IntelliJ IDEA** (Community or Ultimate)
  - Download: https://www.jetbrains.com/idea/download/
  - Plugins: Spring Boot, Lombok, JPA Buddy
  
- Option 2: **Visual Studio Code**
  - Download: https://code.visualstudio.com/
  - Extensions:
    - Extension Pack for Java
    - Spring Boot Extension Pack
    - REST Client

**4. Database**
- Development: **H2 Database** (embedded, in-memory)
  - No installation needed
  - Auto-start with Spring Boot
  - Console: http://localhost:8080/h2-console
  
- Production: **MySQL 8.x** or **PostgreSQL 14+**
  - MySQL: https://dev.mysql.com/downloads/
  - PostgreSQL: https://www.postgresql.org/download/

**5. API Testing**
- Tool: **Postman**
- Download: https://www.postman.com/downloads/
- Import collections dari `docs/POSTMAN-TAHAP-*.md`

---

### B. Mobile Development (Android)

**1. Android Studio**
- Version: **Ladybug (2024.2.x)** atau terbaru
- Download: https://developer.android.com/studio
- SDK: Android 8.0 (API 26) minimum

**2. Android Device/Emulator**
- Physical device (recommended): Android 8.0+
- Emulator: Pixel 5 API 30+

**3. Libraries**
- Retrofit (HTTP client)
- Gson (JSON parsing)
- ZXing (barcode scanner)
- CameraX (camera for face recognition)
- FusedLocationProvider (GPS)

---

### C. Desktop Development (JavaFX)

**1. JavaFX SDK**
- Version: **JavaFX 17+**
- Download: https://openjfx.io/
- Setup: Add to project dependencies

**2. Scene Builder**
- Tool: **Gluon Scene Builder**
- Download: https://gluonhq.com/products/scene-builder/
- Purpose: Visual UI design

---

### D. Version Control

**1. Git**
- Version: **2.40+**
- Download: https://git-scm.com/downloads
- Verify:
  ```powershell
  git --version
  # Output: git version 2.x.x
  ```

**2. GitHub Account**
- Create: https://github.com/signup
- Repository: `presensi-siswa-guru` (atau nama lain)

**3. Git Workflow**
```
main (production-ready)
 ├─ tahap-00-setup
 ├─ tahap-01-backend-skeleton
 ├─ tahap-02-domain-crud
 ├─ tahap-03-auth-role ✅
 ├─ tahap-04-presensi-basic ✅
 ├─ tahap-05-rfid-basic ✅
 ├─ tahap-06-barcode-basic ✅
 ├─ tahap-07-face-recognition ✅
 └─ tahap-08-geolocation (next)
```

---

## 🏗️ ARSITEKTUR APLIKASI

### 1. System Architecture (3-Tier)

```
┌─────────────────────────────────────────────────────────────────┐
│                       PRESENTATION LAYER                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │  Mobile App      │  │  Desktop Admin   │  │  Hardware    │ │
│  │  (Android)       │  │  (JavaFX)        │  │  (RFID/Face) │ │
│  ├──────────────────┤  ├──────────────────┤  ├──────────────┤ │
│  │ - Login          │  │ - Dashboard      │  │ - RFID Reader│ │
│  │ - Checkin (4x)   │  │ - CRUD Siswa     │  │ - Barcode    │ │
│  │ - View History   │  │ - CRUD Guru      │  │ - Face Cam   │ │
│  │ - Camera (Face)  │  │ - Enrollment     │  └──────────────┘ │
│  │ - GPS Tracking   │  │ - Reports        │                   │
│  └──────────────────┘  └──────────────────┘                   │
│           ↓                     ↓                    ↓          │
└───────────┼─────────────────────┼────────────────────┼─────────┘
            │                     │                    │
            └─────────────────────┼────────────────────┘
                                  ↓
┌─────────────────────────────────────────────────────────────────┐
│                       BUSINESS LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│                   Spring Boot REST API                          │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  CONTROLLERS (REST Endpoints)                           │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  - AuthController (/auth/login, /auth/register)        │   │
│  │  - SiswaController (/api/siswa/*)                       │   │
│  │  - GuruController (/api/guru/*)                         │   │
│  │  - PresensiController (/api/presensi/*)                 │   │
│  │  - RfidController (/api/presensi/rfid/*)                │   │
│  │  - BarcodeController (/api/presensi/barcode/*)          │   │
│  │  - FaceController (/api/presensi/face/*)                │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           ↓                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  SERVICES (Business Logic)                              │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  - AuthService (JWT, password hashing)                  │   │
│  │  - SiswaService (CRUD, validation)                      │   │
│  │  - GuruService (CRUD, validation)                       │   │
│  │  - PresensiService (checkin, checkout, status calc)     │   │
│  │  - RfidService (RFID card validation)                   │   │
│  │  - BarcodeService (Barcode validation)                  │   │
│  │  - FaceRecognitionService (encoding, matching)          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           ↓                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  REPOSITORIES (Data Access)                             │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  - UserRepository (findByUsername, save, etc)           │   │
│  │  - SiswaRepository (findByNis, findByRfid, etc)         │   │
│  │  - GuruRepository (findByNip, findByRfid, etc)          │   │
│  │  - PresensiRepository (findByUser, existsByDate, etc)   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└──────────────────────────────────┬──────────────────────────────┘
                                   ↓
┌─────────────────────────────────────────────────────────────────┐
│                       DATA LAYER                                │
├─────────────────────────────────────────────────────────────────┤
│                   Database (H2 / MySQL)                         │
│                                                                  │
│  Tables:                                                         │
│  - user (id, username, password, role, ...)                     │
│  - siswa (id, nis, nama, kelas, rfid, barcode, face, user_id)   │
│  - guru (id, nip, nama, mapel, rfid, barcode, face, user_id)    │
│  - presensi (id, user_id, tipe, method, tanggal, status, ...)   │
│  - kelas (id, nama, wali_kelas_id)                              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### 2. Backend Architecture (Layered Pattern)

```
┌─────────────────────────────────────────────────────────────────┐
│  LAYER 1: CONTROLLER (REST API)                                 │
│  - Handle HTTP requests                                         │
│  - Validate input (@Valid)                                      │
│  - Return ResponseEntity<T>                                     │
│  - Exception handling                                           │
└────────────────────────┬────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│  LAYER 2: SERVICE (Business Logic)                              │
│  - Implement business rules                                     │
│  - Coordinate repositories                                      │
│  - Transaction management (@Transactional)                      │
│  - DTO mapping (Entity ↔ DTO)                                   │
└────────────────────────┬────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│  LAYER 3: REPOSITORY (Data Access)                              │
│  - Extend JpaRepository<Entity, ID>                             │
│  - Query methods (findByXxx, existsByXxx)                       │
│  - Custom queries (@Query)                                      │
└────────────────────────┬────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│  LAYER 4: ENTITY (Domain Model)                                 │
│  - Map to database tables (@Entity)                             │
│  - Relationships (@OneToOne, @ManyToOne, etc)                   │
│  - No business logic (pure data)                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 PROJECT STRUCTURE

```
presensi-siswa-guru/
│
├─ backend/                        # Spring Boot API
│  ├─ src/main/java/com/smk/presensi/
│  │  ├─ controller/               # REST endpoints
│  │  │  ├─ AuthController.java
│  │  │  ├─ SiswaController.java
│  │  │  ├─ GuruController.java
│  │  │  ├─ PresensiController.java
│  │  │  ├─ RfidController.java
│  │  │  ├─ BarcodeController.java
│  │  │  └─ FaceController.java
│  │  │
│  │  ├─ service/                  # Business logic
│  │  │  ├─ AuthService.java
│  │  │  ├─ SiswaService.java
│  │  │  ├─ GuruService.java
│  │  │  ├─ PresensiService.java
│  │  │  ├─ RfidService.java
│  │  │  ├─ BarcodeService.java
│  │  │  └─ FaceRecognitionService.java
│  │  │
│  │  ├─ repository/               # Data access
│  │  │  ├─ UserRepository.java
│  │  │  ├─ SiswaRepository.java
│  │  │  ├─ GuruRepository.java
│  │  │  └─ PresensiRepository.java
│  │  │
│  │  ├─ entity/                   # Domain models
│  │  │  ├─ User.java
│  │  │  ├─ Siswa.java
│  │  │  ├─ Guru.java
│  │  │  ├─ Presensi.java
│  │  │  └─ Kelas.java
│  │  │
│  │  ├─ dto/                      # Data Transfer Objects
│  │  │  ├─ auth/
│  │  │  │  ├─ LoginRequest.java
│  │  │  │  ├─ RegisterRequest.java
│  │  │  │  └─ AuthResponse.java
│  │  │  ├─ siswa/
│  │  │  │  └─ SiswaDTO.java
│  │  │  ├─ guru/
│  │  │  │  └─ GuruDTO.java
│  │  │  └─ presensi/
│  │  │     ├─ PresensiRequest.java
│  │  │     ├─ PresensiResponse.java
│  │  │     ├─ RfidCheckinRequest.java
│  │  │     ├─ BarcodeCheckinRequest.java
│  │  │     ├─ FaceEnrollmentRequest.java
│  │  │     └─ FaceCheckinRequest.java
│  │  │
│  │  ├─ config/                   # Configuration
│  │  │  ├─ SecurityConfig.java    # JWT, CORS
│  │  │  └─ WebConfig.java
│  │  │
│  │  ├─ security/                 # Security components
│  │  │  ├─ JwtAuthenticationFilter.java
│  │  │  ├─ JwtTokenProvider.java
│  │  │  └─ UserDetailsServiceImpl.java
│  │  │
│  │  ├─ exception/                # Exception handling
│  │  │  ├─ GlobalExceptionHandler.java
│  │  │  └─ ResourceNotFoundException.java
│  │  │
│  │  └─ PresensiApplication.java  # Main class
│  │
│  ├─ src/main/resources/
│  │  ├─ application.properties    # App configuration
│  │  └─ application-prod.properties
│  │
│  ├─ pom.xml                      # Maven dependencies
│  └─ mvnw, mvnw.cmd               # Maven wrapper
│
├─ mobile-app/                     # Android app (future)
│  └─ (to be created in Tahap 8-10)
│
├─ desktop-app/                    # JavaFX admin panel (future)
│  └─ (to be created in Tahap 11-12)
│
├─ docs/                           # Documentation
│  ├─ TASK-0.md                    # Setup & requirements (this file)
│  ├─ TASK-3.md                    # Auth implementation
│  ├─ TASK-4.md                    # Presensi manual
│  ├─ TASK-5.md                    # RFID integration
│  ├─ TASK-6.md                    # Barcode integration
│  ├─ TASK-7.md                    # Face recognition
│  ├─ blog3.md                     # Auth concepts (Tahap 3)
│  ├─ blog4.md                     # Presensi concepts (Tahap 4)
│  ├─ blog5.md                     # RFID concepts (Tahap 5)
│  ├─ blog6.md                     # Barcode concepts (Tahap 6)
│  ├─ blog7.md                     # Face recognition concepts (Tahap 7)
│  ├─ README-TAHAP-03.md           # Tahap 3 overview
│  ├─ README-TAHAP-04.md           # Tahap 4 overview
│  ├─ README-TAHAP-05.md           # Tahap 5 overview
│  ├─ README-TAHAP-06.md           # Tahap 6 overview
│  ├─ README-TAHAP-07.md           # Tahap 7 overview
│  ├─ POSTMAN-TAHAP-03.md          # API testing Tahap 3
│  ├─ POSTMAN-TAHAP-04.md          # API testing Tahap 4
│  ├─ POSTMAN-TAHAP-05.md          # API testing Tahap 5
│  ├─ POSTMAN-TAHAP-06.md          # API testing Tahap 6
│  └─ POSTMAN-TAHAP-07.md          # API testing Tahap 7
│
├─ PLAN.MD                         # Overall project plan
└─ README.md                       # Project overview
```

---

## 🚀 SETUP INSTRUCTIONS

### Step 1: Install JDK 17

```powershell
# Download JDK 17 dari https://adoptium.net/
# Install ke C:\Program Files\Eclipse Adoptium\jdk-17.x.x

# Verify installation
java -version

# Expected output:
# openjdk version "17.0.x"
```

### Step 2: Install IDE (IntelliJ IDEA atau VS Code)

**Option A: IntelliJ IDEA**
```
1. Download dari https://www.jetbrains.com/idea/download/
2. Install (Community Edition gratis)
3. First run: Install plugins (Spring Boot, Lombok)
```

**Option B: VS Code**
```
1. Download dari https://code.visualstudio.com/
2. Install
3. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
```

### Step 3: Clone Repository

```powershell
# Clone from GitHub
git clone https://github.com/tariyahya/SIJA-SPRING.git
cd SIJA-SPRING

# Checkout branch tahap-03 (starting point)
git checkout tahap-03-auth-role
```

### Step 4: Run Backend

```powershell
cd backend

# Run with Maven wrapper (Windows)
.\mvnw spring-boot:run

# Or with Maven (if installed globally)
mvn spring-boot:run
```

**Expected output**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

[INFO] Started PresensiApplication in 3.245 seconds
```

### Step 5: Test API

```powershell
# Open browser
http://localhost:8080/api/hello

# Or use Postman
GET http://localhost:8080/api/hello
```

### Step 6: Install Postman

```
1. Download dari https://www.postman.com/downloads/
2. Install
3. Import collections dari docs/POSTMAN-TAHAP-*.md
```

---

## ✅ CHECKLIST SETUP

### Environment Setup
- [ ] JDK 17 installed & verified
- [ ] IDE (IntelliJ/VS Code) installed
- [ ] Git installed & configured
- [ ] GitHub account created
- [ ] Postman installed

### Project Setup
- [ ] Repository cloned
- [ ] Branch tahap-03-auth-role checked out
- [ ] Backend compiled successfully (`mvn clean compile`)
- [ ] Backend running (`mvn spring-boot:run`)
- [ ] API tested (GET /api/hello)

### Understanding
- [ ] Memahami kebutuhan fungsional sistem
- [ ] Memahami kebutuhan non-fungsional
- [ ] Memahami arsitektur 3-tier
- [ ] Memahami layered pattern (Controller-Service-Repository)
- [ ] Memahami project structure

---

## 📚 REFERENSI

### Dokumentasi Official
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **JWT**: https://jwt.io/introduction

### Tutorial
- **Baeldung Spring Boot**: https://www.baeldung.com/spring-boot
- **Spring Guides**: https://spring.io/guides
- **Java T Point**: https://www.javatpoint.com/spring-boot-tutorial

### Tools Documentation
- **Maven**: https://maven.apache.org/guides/
- **H2 Database**: https://www.h2database.com/
- **Postman**: https://learning.postman.com/

---

## 🎯 NEXT STEPS

Setelah setup selesai, lanjut ke:

1. **Tahap 1**: Backend Skeleton ✅ (completed)
2. **Tahap 2**: Domain Model & CRUD ✅ (completed)
3. **Tahap 3**: Authentication & Authorization ✅ (completed)
4. **Tahap 4**: Presensi Manual ✅ (completed)
5. **Tahap 5**: RFID Integration ✅ (completed)
6. **Tahap 6**: Barcode Integration ✅ (completed)
7. **Tahap 7**: Face Recognition ✅ (completed)
8. **Tahap 8**: Geolocation Validation (next)

---

**Last Updated**: 17 November 2024  
**Status**: ✅ COMPLETE (retrospective documentation)  
**Next**: Proceed to Tahap 8 (Geolocation)
