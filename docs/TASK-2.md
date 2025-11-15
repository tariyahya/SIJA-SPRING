# TASK 2 – Domain Model & CRUD (Tahap 02)

Dokumen ini memandu siswa untuk membuat **entity** (model data) dan **CRUD** (Create, Read, Update, Delete) lengkap untuk data utama: Siswa, Guru, dan Kelas. Bacalah berurutan dan ikuti checklist di akhir.

---

## 1. Kenapa Kita Perlu Entity & CRUD?

Pada Tahap 1, kita hanya buat endpoint sederhana tanpa data asli. Di Tahap 2, kita mulai simpan data siswa, guru, dan kelas ke database. Ini penting karena:

1. **Entity** adalah representasi tabel database di kode Java. Tanpa entity, tidak ada tempat simpan data.
2. **CRUD** membuat admin bisa mengelola data (tambah, lihat, ubah, hapus).
3. Ini fondasi untuk fitur presensi yang akan datang (auth, RFID, QR, dll).

> Analogi: Entity seperti formulir data, repository seperti lemari arsip, service seperti petugas yang proses formulir, dan controller seperti loket yang terima permintaan.

---

## 2. Target Task

- Membuat 3 entity: `Siswa`, `Guru`, `Kelas` dengan field lengkap.
- Membuat repository berbasis `JpaRepository`.
- Membuat service untuk logika bisnis CRUD.
- Membuat controller REST API dengan endpoint CRUD.
- Memahami konsep DTO (Data Transfer Object).
- Menguji semua endpoint dengan Postman.

Checklist awal:
- [ ] Branch `tahap-02-domain-crud` sudah dibuat dari `tahap-01-backend-skeleton`.
- [ ] Dependency JPA dan H2 database sudah ditambahkan di `pom.xml`.
- [ ] Struktur folder `entity/`, `repository/`, `service/`, `dto/` sudah ada.

---

## 3. Persiapan Dependencies

Tambahkan dependency berikut ke `pom.xml` (jika belum ada):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

Tambahkan di `application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:presensidb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

> H2 adalah database in-memory untuk development. Nanti bisa diganti MySQL/PostgreSQL untuk production.

---

## 4. Langkah Kerja Detail

### Langkah 1 – Buat Entity `Siswa`

Buat file `src/main/java/com/smk/presensi/entity/Siswa.java`:

```java
package com.smk.presensi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "siswa")
public class Siswa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nis;

    @Column(nullable = false)
    private String nama;

    private String kelas;
    private String jurusan;
    private String rfidCardId;
    private String barcodeId;
    private String faceId;

    // Constructors
    public Siswa() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNis() { return nis; }
    public void setNis(String nis) { this.nis = nis; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKelas() { return kelas; }
    public void setKelas(String kelas) { this.kelas = kelas; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public String getRfidCardId() { return rfidCardId; }
    public void setRfidCardId(String rfidCardId) { this.rfidCardId = rfidCardId; }

    public String getBarcodeId() { return barcodeId; }
    public void setBarcodeId(String barcodeId) { this.barcodeId = barcodeId; }

    public String getFaceId() { return faceId; }
    public void setFaceId(String faceId) { this.faceId = faceId; }
}
```

**Penjelasan:**
- `@Entity`: Menandakan class ini adalah tabel database.
- `@Table(name = "siswa")`: Nama tabel di database.
- `@Id`: Primary key.
- `@GeneratedValue`: Auto-increment ID.
- `@Column`: Konfigurasi kolom (nullable, unique).

**Tugas siswa:** Buat entity `Guru` dan `Kelas` dengan pola serupa. Field minimal:
- `Guru`: id, nip, nama, mapel, rfidCardId, barcodeId, faceId
- `Kelas`: id, nama, tingkat, jurusan, waliKelasId

---

### Langkah 2 – Buat Repository

Buat file `src/main/java/com/smk/presensi/repository/SiswaRepository.java`:

```java
package com.smk.presensi.repository;

import com.smk.presensi.entity.Siswa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiswaRepository extends JpaRepository<Siswa, Long> {
    
    Optional<Siswa> findByNis(String nis);
    
    List<Siswa> findByKelas(String kelas);
}
```

**Penjelasan:**
- `JpaRepository<Siswa, Long>`: Menyediakan method CRUD otomatis (save, findAll, findById, delete).
- `findByNis`: Spring Data JPA otomatis buat query `WHERE nis = ?`.
- `findByKelas`: Query untuk filter siswa per kelas.

**Tugas siswa:** Buat `GuruRepository` dan `KelasRepository`.

---

### Langkah 3 – Buat DTO (Data Transfer Object)

Buat folder `dto/` dan file `SiswaRequest.java` dan `SiswaResponse.java`:

```java
package com.smk.presensi.dto;

import jakarta.validation.constraints.NotBlank;

public record SiswaRequest(
    @NotBlank String nis,
    @NotBlank String nama,
    String kelas,
    String jurusan
) {}
```

```java
package com.smk.presensi.dto;

public record SiswaResponse(
    Long id,
    String nis,
    String nama,
    String kelas,
    String jurusan
) {}
```

**Penjelasan:**
- DTO memisahkan data yang dikirim/diterima dari entity database.
- `record` adalah fitur Java untuk membuat class data ringkas.
- `@NotBlank`: Validasi field tidak boleh kosong.

---

### Langkah 4 – Buat Service

Buat file `src/main/java/com/smk/presensi/service/SiswaService.java`:

```java
package com.smk.presensi.service;

import com.smk.presensi.dto.SiswaRequest;
import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.entity.Siswa;
import com.smk.presensi.repository.SiswaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiswaService {

    private final SiswaRepository siswaRepository;

    public SiswaService(SiswaRepository siswaRepository) {
        this.siswaRepository = siswaRepository;
    }

    public List<SiswaResponse> findAll() {
        return siswaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SiswaResponse findById(Long id) {
        Siswa siswa = siswaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan"));
        return toResponse(siswa);
    }

    public SiswaResponse create(SiswaRequest request) {
        Siswa siswa = new Siswa();
        siswa.setNis(request.nis());
        siswa.setNama(request.nama());
        siswa.setKelas(request.kelas());
        siswa.setJurusan(request.jurusan());
        siswaRepository.save(siswa);
        return toResponse(siswa);
    }

    public SiswaResponse update(Long id, SiswaRequest request) {
        Siswa siswa = siswaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siswa tidak ditemukan"));
        siswa.setNama(request.nama());
        siswa.setKelas(request.kelas());
        siswa.setJurusan(request.jurusan());
        siswaRepository.save(siswa);
        return toResponse(siswa);
    }

    public void delete(Long id) {
        siswaRepository.deleteById(id);
    }

    public List<SiswaResponse> findByKelas(String kelas) {
        return siswaRepository.findByKelas(kelas).stream()
                .map(this::toResponse)
                .toList();
    }

    private SiswaResponse toResponse(Siswa siswa) {
        return new SiswaResponse(
            siswa.getId(),
            siswa.getNis(),
            siswa.getNama(),
            siswa.getKelas(),
            siswa.getJurusan()
        );
    }
}
```

**Penjelasan:**
- Service berisi logika bisnis CRUD.
- Method `toResponse` convert entity ke DTO.
- Error handling dengan `orElseThrow`.

---

### Langkah 5 – Buat Controller

Buat file `src/main/java/com/smk/presensi/controller/SiswaController.java`:

```java
package com.smk.presensi.controller;

import com.smk.presensi.dto.SiswaRequest;
import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.service.SiswaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/siswa")
public class SiswaController {

    private final SiswaService siswaService;

    public SiswaController(SiswaService siswaService) {
        this.siswaService = siswaService;
    }

    @GetMapping
    public List<SiswaResponse> getAll() {
        return siswaService.findAll();
    }

    @GetMapping("/{id}")
    public SiswaResponse getById(@PathVariable Long id) {
        return siswaService.findById(id);
    }

    @PostMapping
    public ResponseEntity<SiswaResponse> create(@RequestBody @Valid SiswaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(siswaService.create(request));
    }

    @PutMapping("/{id}")
    public SiswaResponse update(@PathVariable Long id, @RequestBody @Valid SiswaRequest request) {
        return siswaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        siswaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/kelas/{namaKelas}")
    public List<SiswaResponse> getByKelas(@PathVariable String namaKelas) {
        return siswaService.findByKelas(namaKelas);
    }
}
```

**Penjelasan:**
- `@RestController`: Controller untuk REST API.
- `@RequestMapping("/api/siswa")`: Base URL.
- `@GetMapping`, `@PostMapping`, dll: Method HTTP.
- `@PathVariable`: Ambil parameter dari URL.
- `@RequestBody`: Ambil data dari body request.
- `@Valid`: Aktifkan validasi DTO.

---

## 5. Testing dengan Postman

### Test 1: POST - Buat Siswa Baru

```
POST http://localhost:8081/api/siswa
Content-Type: application/json

{
  "nis": "2025001",
  "nama": "Ahmad Fauzi",
  "kelas": "XI SIJA 1",
  "jurusan": "SIJA"
}
```

**Expected:** Status 201 Created, response berisi data siswa dengan ID.

### Test 2: GET - Lihat Semua Siswa

```
GET http://localhost:8081/api/siswa
```

**Expected:** Status 200 OK, array berisi semua siswa.

### Test 3: GET - Lihat Siswa by ID

```
GET http://localhost:8081/api/siswa/1
```

**Expected:** Status 200 OK, data siswa ID 1.

### Test 4: PUT - Update Siswa

```
PUT http://localhost:8081/api/siswa/1
Content-Type: application/json

{
  "nis": "2025001",
  "nama": "Ahmad Fauzi Updated",
  "kelas": "XI SIJA 2",
  "jurusan": "SIJA"
}
```

**Expected:** Status 200 OK, data siswa terupdate.

### Test 5: DELETE - Hapus Siswa

```
DELETE http://localhost:8081/api/siswa/1
```

**Expected:** Status 204 No Content.

### Test 6: GET - Filter by Kelas

```
GET http://localhost:8081/api/siswa/kelas/XI%20SIJA%201
```

**Expected:** Status 200 OK, array siswa di kelas XI SIJA 1.

---

## 6. Tugas Siswa Setelah Membaca

1. **Lengkapi entity `Guru` dan `Kelas`** dengan field yang disebutkan.
2. **Buat repository, service, controller** untuk Guru dan Kelas (pola sama dengan Siswa).
3. **Test semua endpoint** dengan Postman dan screenshot hasilnya.
4. **Tambahkan validasi** `@NotBlank` pada field penting di DTO.
5. **Commit** dengan pesan `feat: selesai tahap 02 domain crud`.

---

## 7. Checklist Kelulusan Tahap 02

- [ ] Entity `Siswa`, `Guru`, `Kelas` selesai dengan field lengkap.
- [ ] Repository menggunakan `JpaRepository`.
- [ ] Service layer mengimplementasi CRUD logic.
- [ ] Controller menyediakan endpoint REST lengkap.
- [ ] DTO Request/Response dipakai.
- [ ] Semua endpoint berhasil dites dengan Postman.
- [ ] Field `jurusan` ada di Siswa dan endpoint filter kelas berfungsi.

---

## 8. FAQ Singkat

**Q: Kenapa pakai H2 database?**  
A: H2 adalah database in-memory, mudah untuk development tanpa setup database eksternal. Nanti bisa diganti MySQL/PostgreSQL.

**Q: Apa beda Entity dan DTO?**  
A: Entity untuk database, DTO untuk transfer data ke/dari client. DTO lebih aman dan fleksibel.

**Q: Error `table not found`?**  
A: Pastikan `spring.jpa.hibernate.ddl-auto=update` ada di `application.properties`.

---

Setelah semua checklist terpenuhi, Tahap 02 selesai. Kalian siap lanjut ke Tahap 03: Authentication & Role (JWT).
