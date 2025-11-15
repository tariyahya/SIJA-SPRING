# Sistem Presensi Tahap 2 – Belajar Entity & CRUD ala SIJA

Selamat datang di Level 2! Setelah API dasar di Tahap 1 berjalan, saatnya kita memberi "isi" pada aplikasi. Pada Tahap 2 kita fokus pada **entity** (model data) dan **CRUD** (Create, Read, Update, Delete) untuk data utama: Siswa, Guru, dan Kelas. Artikel ini membahas konsep, langkah tenaga, contoh kode, hingga tugas siswa.

## Kenapa Entity & CRUD Penting?

- **Entity** adalah representasi tabel database di kode Java. Tanpa ini, data siswa/guru tidak bisa disimpan.
- **CRUD** memastikan admin bisa menambah, melihat, mengubah, dan menghapus data.
- Tahap ini menjadi pondasi untuk fitur presensi yang lebih kompleks (auth, RFID, dsb.) di tahap berikutnya.

## Outline Materi Tahap 2

1. Recap struktur project backend.
2. Membuat entity `Siswa`, `Guru`, `Kelas` (+ field wajib).
3. Menyiapkan repository berbasis `JpaRepository`.
4. Membangun service layer untuk logika CRUD.
5. Membuat controller REST (`/api/siswa`, `/api/guru`, `/api/kelas`).
6. Mengenalkan DTO sederhana agar response rapi.
7. Checklist tugas siswa dan tantangan lanjutan.

## Struktur Folder yang Dipakai

```
backend/
├─ src/main/java/com/smk/presensi/
│  ├─ entity/
│  │  ├─ Siswa.java
│  │  ├─ Guru.java
│  │  └─ Kelas.java
│  ├─ repository/
│  │  ├─ SiswaRepository.java
│  │  ├─ GuruRepository.java
│  │  └─ KelasRepository.java
│  ├─ service/
│  │  ├─ SiswaService.java
│  │  └─ ...
│  └─ controller/
│     └─ SiswaController.java
└─ src/main/resources/application.properties
```

> Folder `entity`, `repository`, `service`, `controller` sudah disebut sejak Tahap 1. Kini kita mulai mengisinya.

## 1. Membuat Entity (Model Data)

Contoh entity `Siswa`:

```java
@Entity
@Table(name = "siswa")
public class Siswa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nis;

    private String nama;
    private String kelas;
    private String jurusan;
    private String rfidCardId;
    private String barcodeId;

    // getter + setter
}
```

> Gunakan anotasi JPA: `@Entity`, `@Id`, `@GeneratedValue`, `@Column`. Untuk `Guru` dan `Kelas` tinggal menyesuaikan fieldnya.

## 2. Repository – Jalan ke Database

```java
public interface SiswaRepository extends JpaRepository<Siswa, Long> {
    Optional<Siswa> findByNis(String nis);
    List<Siswa> findByKelas(String kelas);
}
```

Repository membuat kita tidak perlu menulis SQL manual. Spring Data JPA mengerti nama method seperti `findByKelas` dan otomatis membuat query.

## 3. Service Layer – Logika Bisnis

```java
@Service
public class SiswaService {

    private final SiswaRepository siswaRepository;

    public SiswaService(SiswaRepository siswaRepository) {
        this.siswaRepository = siswaRepository;
    }

    public List<SiswaResponse> getAll() {
        return siswaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
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

    private SiswaResponse toResponse(Siswa siswa) {
        return new SiswaResponse(siswa.getId(), siswa.getNis(), siswa.getNama(), siswa.getKelas(), siswa.getJurusan());
    }
}
```

> Service bertugas memproses request sebelum masuk database. Kalau nanti ada validasi tambahan (cek NIS unik, dsb) tempatnya di sini.

## 4. DTO – Data Transfer Object

DTO menjaga agar data yang dilepas ke frontend rapi dan aman. Kita gunakan `record` agar hemat kode.

```java
public record SiswaRequest(String nis, String nama, String kelas, String jurusan) {}
public record SiswaResponse(Long id, String nis, String nama, String kelas, String jurusan) {}
```

## 5. Controller CRUD

```java
@RestController
@RequestMapping("/api/siswa")
public class SiswaController {

    private final SiswaService siswaService;

    public SiswaController(SiswaService siswaService) {
        this.siswaService = siswaService;
    }

    @GetMapping
    public List<SiswaResponse> list() {
        return siswaService.getAll();
    }

    @PostMapping
    public ResponseEntity<SiswaResponse> create(@RequestBody @Valid SiswaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siswaService.create(request));
    }

    // TODO: tambahkan GET by ID, PUT, DELETE, dan filter kelas
}
```

Endpoint minimal yang harus ada:
- `GET /api/siswa`
- `GET /api/siswa/{id}`
- `POST /api/siswa`
- `PUT /api/siswa/{id}`
- `DELETE /api/siswa/{id}`
- `GET /api/siswa/kelas/{namaKelas}`

## 6. Checklist Siswa Tahap 2

- [ ] Entity `Siswa`, `Guru`, `Kelas` selesai dengan field wajib.
- [ ] Repository dan service sudah bisa menyimpan & membaca data.
- [ ] Endpoint CRUD Siswa aktif (uji dengan Postman).
- [ ] DTO Request/Response dipakai di controller.
- [ ] Tambahkan field `jurusan` dan endpoint filter kelas (tugas di PLAN.MD).

## 7. Tantangan Opsional

1. Tambahkan validasi `@NotBlank` pada field penting di DTO.
2. Buat data dummy otomatis dengan `data.sql` di `src/main/resources`.
3. Implementasikan pagination sederhana (`Pageable`) pada `GET /api/siswa`.

## 8. Langkah Berikutnya

Setelah CRUD berfungsi, repositori siap untuk Tahap 3: **Authentication & Role**. Pastikan branch `tahap-02-domain-crud` sudah di-commit dan dites (`mvn test`). Lalu buat branch baru `tahap-03-auth-role` untuk fitur login JWT.

Selamat belajar! Kalau bingung, diskusikan dengan teman sekelompok atau cek branch referensi guru.
