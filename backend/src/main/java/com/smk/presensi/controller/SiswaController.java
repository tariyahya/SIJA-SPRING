package com.smk.presensi.controller;

import com.smk.presensi.dto.SiswaRequest;
import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.service.SiswaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER LAYER - Lapisan yang menerima HTTP REQUEST dan mengirim HTTP RESPONSE.
 * 
 * Apa itu Controller?
 * - Layer paling LUAR yang berhadapan langsung dengan CLIENT (browser, mobile app, Postman, etc)
 * - Menerima HTTP request (GET, POST, PUT, DELETE)
 * - Memanggil Service layer untuk business logic
 * - Mengirim HTTP response (biasanya dalam format JSON)
 * 
 * Tanggung jawab Controller:
 * 1. HTTP request mapping (menentukan endpoint mana yang handle request)
 * 2. Request parameter/body extraction (ambil data dari request)
 * 3. Input validation (trigger validasi dengan @Valid)
 * 4. Call service layer (delegate logika bisnis ke service)
 * 5. HTTP response formatting (set status code, return data)
 * 
 * Yang TIDAK dilakukan di Controller:
 * - Business logic (logika bisnis → harus di Service)
 * - Database access (akses database → harus di Repository)
 * - Data transformation (transform Entity ↔ DTO → harus di Service)
 * 
 * Analogi sederhana:
 * - Controller = RESEPSIONIS hotel
 *   * Terima tamu (HTTP request)
 *   * Arahkan ke bagian yang tepat (panggil service)
 *   * Kasih respons ke tamu (HTTP response)
 * - Service = MANAGER hotel (handle logika bisnis)
 * - Repository = SISTEM DATABASE hotel (simpan/ambil data)
 * 
 * RESTful API pattern yang dipakai:
 * - GET /api/siswa → ambil semua siswa
 * - GET /api/siswa/{id} → ambil siswa berdasarkan id
 * - POST /api/siswa → tambah siswa baru
 * - PUT /api/siswa/{id} → update siswa
 * - DELETE /api/siswa/{id} → hapus siswa
 * - GET /api/siswa/kelas/{namaKelas} → ambil siswa berdasarkan kelas
 */
@RestController // Gabungan @Controller + @ResponseBody (semua method return JSON, bukan HTML)
@RequestMapping("/api/siswa") // Base URL untuk semua endpoint di controller ini
// Semua endpoint dimulai dengan /api/siswa
// Contoh: GET /api/siswa, POST /api/siswa, GET /api/siswa/1
public class SiswaController {

    // Dependency: butuh SiswaService untuk akses business logic
    // 'final' = tidak bisa diubah setelah di-inject (best practice)
    private final SiswaService siswaService;

    /**
     * CONSTRUCTOR INJECTION - Spring otomatis inject SiswaService.
     * 
     * Sama seperti di SiswaService, kita pakai Constructor Injection:
     * - Dependency wajib ada (kalau tidak ada, app tidak start)
     * - Immutable (final field)
     * - Easy to test (bisa inject mock)
     * 
     * Spring akan otomatis cari Bean SiswaService dan inject ke sini.
     * Kita tidak perlu `new SiswaService()` secara manual.
     * 
     * @param siswaService Service layer untuk business logic siswa
     */
    public SiswaController(SiswaService siswaService) {
        this.siswaService = siswaService;
    }

    /**
     * ENDPOINT 1: GET ALL - Ambil semua data siswa.
     * 
     * HTTP Method: GET
     * URL: GET /api/siswa
     * Request Body: tidak ada (GET tidak pakai body)
     * Response: List<SiswaResponse> (array JSON)
     * Status Code: 200 OK (default)
     * 
     * Cara kerja:
     * 1. Client kirim request: GET http://localhost:8081/api/siswa
     * 2. Spring MVC route request ke method ini
     * 3. Method ini panggil siswaService.findAll()
     * 4. Service return List<SiswaResponse>
     * 5. Spring otomatis convert List → JSON
     * 6. Client terima JSON response
     * 
     * @GetMapping artinya:
     * - Method ini handle HTTP GET request
     * - Path: base path + method path = /api/siswa + "" = /api/siswa
     * - Tidak ada path tambahan karena @GetMapping kosong
     * 
     * Return type: List<SiswaResponse>
     * - Spring otomatis convert ke JSON array
     * - Contoh JSON response:
     * [
     *   {"id": 1, "nis": "2024001", "nama": "Budi", "kelas": "XII RPL 1", "jurusan": "RPL"},
     *   {"id": 2, "nis": "2024002", "nama": "Siti", "kelas": "XI TKJ 2", "jurusan": "TKJ"}
     * ]
     * 
     * Kenapa tidak pakai ResponseEntity?
     * - Untuk endpoint sederhana yang selalu return 200 OK, cukup return data langsung
     * - Spring otomatis set status code 200
     * - Lebih simple dan clean
     * 
     * Catatan:
     * - Jika database kosong, return array kosong [] (bukan error)
     * - Response bisa sangat besar jika banyak siswa (nanti bisa tambah pagination)
     * 
     * Contoh test dengan curl:
     * curl -X GET http://localhost:8081/api/siswa
     * 
     * Atau buka di browser:
     * http://localhost:8081/api/siswa
     * 
     * @return List semua siswa dalam bentuk DTO (JSON array)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<SiswaResponse> getAll() {
        return siswaService.findAll();
    }

    /**
     * ENDPOINT 2: GET BY ID - Ambil 1 siswa berdasarkan ID.
     * 
     * HTTP Method: GET
     * URL: GET /api/siswa/{id}
     * Request Body: tidak ada
     * Response: SiswaResponse (single object JSON)
     * Status Code: 200 OK (success) atau 500 (error jika tidak ditemukan)
     * 
     * Cara kerja:
     * 1. Client kirim: GET http://localhost:8081/api/siswa/1
     * 2. Spring extract id=1 dari URL (path variable)
     * 3. Method ini panggil siswaService.findById(1)
     * 4. Jika ditemukan → return SiswaResponse, status 200 OK
     * 5. Jika tidak ditemukan → Service throw exception, status 500 (nanti kita improve jadi 404)
     * 
     * @GetMapping("/{id}") artinya:
     * - Method ini handle GET request dengan path parameter
     * - {id} adalah PLACEHOLDER untuk dynamic value
     * - Path lengkap: /api/siswa + /{id} = /api/siswa/1, /api/siswa/2, dst
     * 
     * @PathVariable Long id artinya:
     * - Ambil value dari {id} di URL
     * - Convert ke tipe Long
     * - Masukkan ke parameter 'id'
     * - Contoh: URL /api/siswa/5 → id=5
     * 
     * Return type: SiswaResponse (single object)
     * - Spring convert ke JSON object (bukan array)
     * - Contoh JSON response:
     * {
     *   "id": 1,
     *   "nis": "2024001",
     *   "nama": "Budi Santoso",
     *   "kelas": "XII RPL 1",
     *   "jurusan": "RPL"
     * }
     * 
     * Error handling (saat ini):
     * - Jika id tidak ditemukan → RuntimeException → status 500 Internal Server Error
     * - Nanti di Tahap 3 kita improve dengan @ControllerAdvice → status 404 Not Found
     * 
     * Contoh test dengan curl:
     * curl -X GET http://localhost:8081/api/siswa/1
     * 
     * Atau buka di browser:
     * http://localhost:8081/api/siswa/1
     * 
     * @param id ID siswa yang dicari (diambil dari URL path)
     * @return SiswaResponse berisi data 1 siswa
     * @throws RuntimeException jika siswa tidak ditemukan (di-handle di service layer)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public SiswaResponse getById(@PathVariable Long id) {
        return siswaService.findById(id);
    }

    /**
     * ENDPOINT 3: CREATE - Tambah siswa baru.
     * 
     * HTTP Method: POST
     * URL: POST /api/siswa
     * Request Body: JSON (SiswaRequest)
     * Response: SiswaResponse (data siswa yang baru dibuat, termasuk id)
     * Status Code: 201 CREATED (bukan 200 OK, karena resource baru dibuat)
     * 
     * Cara kerja:
     * 1. Client kirim POST request dengan JSON body
     * 2. Spring convert JSON → SiswaRequest object (@RequestBody)
     * 3. Spring trigger validasi (@Valid) → cek @NotBlank di SiswaRequest
     * 4. Jika validasi GAGAL → return 400 Bad Request dengan error message
     * 5. Jika validasi SUKSES → panggil siswaService.create(request)
     * 6. Service save ke database, return SiswaResponse (dengan id baru)
     * 7. Controller wrap response dengan status 201 CREATED
     * 8. Client terima response 201 dengan data siswa baru
     * 
     * @PostMapping artinya:
     * - Method ini handle HTTP POST request
     * - Path: /api/siswa (base path, tidak ada path tambahan)
     * - POST biasanya untuk CREATE operation (menambah data baru)
     * 
     * @RequestBody SiswaRequest request artinya:
     * - Ambil data dari HTTP request body (JSON)
     * - Convert JSON → SiswaRequest object
     * - Contoh JSON yang dikirim client:
     * {
     *   "nis": "2024003",
     *   "nama": "Andi Wijaya",
     *   "kelas": "XII RPL 2",
     *   "jurusan": "RPL"
     * }
     * 
     * @Valid artinya:
     * - TRIGGER VALIDASI sebelum method dijalankan
     * - Cek semua constraint di SiswaRequest (@NotBlank di nis dan nama)
     * - Jika ada field yang tidak valid → throw MethodArgumentNotValidException
     * - Spring otomatis return 400 Bad Request dengan error details
     * - Method ini tidak akan dijalankan jika validasi gagal
     * 
     * ResponseEntity<SiswaResponse> artinya:
     * - Wrapper untuk HTTP response yang bisa customize status code dan headers
     * - Generic type <SiswaResponse> = tipe data di response body
     * - Bedanya dengan return SiswaResponse biasa: bisa set status code custom
     * 
     * ResponseEntity.status(HttpStatus.CREATED) artinya:
     * - Set HTTP status code 201 CREATED (bukan 200 OK)
     * - 201 CREATED adalah best practice untuk endpoint yang CREATE resource baru
     * - Memberitahu client: "Resource berhasil dibuat"
     * 
     * .body(siswaService.create(request)) artinya:
     * - Set response body dengan hasil dari siswaService.create()
     * - Response body adalah SiswaResponse (data siswa yang baru dibuat)
     * 
     * Contoh response JSON:
     * Status: 201 CREATED
     * Body:
     * {
     *   "id": 3,  ← ID baru yang di-generate database
     *   "nis": "2024003",
     *   "nama": "Andi Wijaya",
     *   "kelas": "XII RPL 2",
     *   "jurusan": "RPL"
     * }
     * 
     * Contoh error validasi (jika nis kosong):
     * Status: 400 Bad Request
     * Body:
     * {
     *   "nis": "NIS tidak boleh kosong"
     * }
     * 
     * Contoh test dengan curl:
     * curl -X POST http://localhost:8081/api/siswa \
     *   -H "Content-Type: application/json" \
     *   -d '{"nis":"2024003","nama":"Andi","kelas":"XII RPL 2","jurusan":"RPL"}'
     * 
     * Atau test dengan Postman:
     * - Method: POST
     * - URL: http://localhost:8081/api/siswa
     * - Headers: Content-Type = application/json
     * - Body (raw JSON): {"nis":"2024003","nama":"Andi","kelas":"XII RPL 2","jurusan":"RPL"}
     * 
     * @param request Data siswa baru dari client (DTO, akan divalidasi)
     * @return ResponseEntity berisi SiswaResponse dengan status 201 CREATED
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiswaResponse> create(@RequestBody @Valid SiswaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(siswaService.create(request));
    }

    /**
     * ENDPOINT 4: UPDATE - Ubah data siswa yang sudah ada.
     * 
     * HTTP Method: PUT
     * URL: PUT /api/siswa/{id}
     * Request Body: JSON (SiswaRequest dengan data baru)
     * Response: SiswaResponse (data siswa yang sudah diupdate)
     * Status Code: 200 OK (success) atau 500 (error jika tidak ditemukan)
     * 
     * Cara kerja:
     * 1. Client kirim PUT request dengan id di URL dan JSON body
     * 2. Spring extract id dari URL (@PathVariable)
     * 3. Spring convert JSON → SiswaRequest (@RequestBody)
     * 4. Spring trigger validasi (@Valid)
     * 5. Jika validasi gagal → return 400 Bad Request
     * 6. Jika validasi sukses → panggil siswaService.update(id, request)
     * 7. Service cari siswa berdasarkan id, update, save ke database
     * 8. Return SiswaResponse dengan data yang sudah diupdate
     * 
     * @PutMapping("/{id}") artinya:
     * - Method ini handle HTTP PUT request
     * - Path: /api/siswa/{id} (contoh: /api/siswa/1, /api/siswa/2)
     * - PUT biasanya untuk UPDATE operation (mengubah data existing)
     * 
     * @PathVariable Long id artinya:
     * - Ambil id dari URL path
     * - Ini menentukan siswa MANA yang mau diupdate
     * 
     * @RequestBody @Valid SiswaRequest request artinya:
     * - Ambil data baru dari request body (JSON)
     * - Validasi data baru (cek @NotBlank)
     * - Ini adalah data PENGGANTI untuk siswa tersebut
     * 
     * Return type: SiswaResponse
     * - Return data siswa yang sudah diupdate
     * - Status code default 200 OK
     * 
     * Perbedaan POST (create) vs PUT (update):
     * - POST: tidak perlu id, database generate id baru → 201 CREATED
     * - PUT: perlu id di URL, update data existing → 200 OK
     * 
     * Contoh request:
     * PUT http://localhost:8081/api/siswa/1
     * Body:
     * {
     *   "nis": "2024001",  ← NIS tidak akan diupdate (diabaikan di service)
     *   "nama": "Budi Santoso UPDATE",  ← nama DIUPDATE
     *   "kelas": "XII RPL 3",  ← kelas DIUPDATE
     *   "jurusan": "RPL"  ← jurusan DIUPDATE
     * }
     * 
     * Contoh response:
     * Status: 200 OK
     * Body:
     * {
     *   "id": 1,  ← id tetap
     *   "nis": "2024001",  ← nis tetap (tidak diupdate)
     *   "nama": "Budi Santoso UPDATE",  ← nama berubah
     *   "kelas": "XII RPL 3",  ← kelas berubah
     *   "jurusan": "RPL"
     * }
     * 
     * Error scenarios:
     * - ID tidak ditemukan → RuntimeException → 500 Internal Server Error
     * - Validasi gagal (nama kosong) → 400 Bad Request
     * 
     * Contoh test dengan curl:
     * curl -X PUT http://localhost:8081/api/siswa/1 \
     *   -H "Content-Type: application/json" \
     *   -d '{"nis":"2024001","nama":"Budi UPDATE","kelas":"XII RPL 3","jurusan":"RPL"}'
     * 
     * Catatan penting:
     * - Field 'id' diambil dari URL, BUKAN dari request body
     * - Field 'nis' tidak diupdate meskipun ada di request body (lihat service layer)
     * - Hanya nama, kelas, jurusan yang bisa diubah
     * 
     * @param id ID siswa yang mau diupdate (dari URL path)
     * @param request Data baru untuk siswa (dari request body, akan divalidasi)
     * @return SiswaResponse berisi data siswa yang sudah diupdate
     * @throws RuntimeException jika siswa dengan id tersebut tidak ditemukan
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SiswaResponse update(@PathVariable Long id, @RequestBody @Valid SiswaRequest request) {
        return siswaService.update(id, request);
    }

    /**
     * ENDPOINT 5: DELETE - Hapus siswa dari database.
     * 
     * HTTP Method: DELETE
     * URL: DELETE /api/siswa/{id}
     * Request Body: tidak ada
     * Response: tidak ada body (empty response)
     * Status Code: 204 NO CONTENT (success) atau 500 (error jika tidak ditemukan)
     * 
     * Cara kerja:
     * 1. Client kirim DELETE request dengan id di URL
     * 2. Spring extract id dari URL (@PathVariable)
     * 3. Method ini panggil siswaService.delete(id)
     * 4. Service cek siswa ada atau tidak, jika ada → hapus
     * 5. Return ResponseEntity dengan status 204 NO CONTENT (tidak ada body)
     * 
     * @DeleteMapping("/{id}") artinya:
     * - Method ini handle HTTP DELETE request
     * - Path: /api/siswa/{id} (contoh: /api/siswa/1)
     * - DELETE untuk menghapus resource
     * 
     * @PathVariable Long id artinya:
     * - Ambil id dari URL path
     * - Ini menentukan siswa MANA yang mau dihapus
     * 
     * ResponseEntity<Void> artinya:
     * - Void = tidak ada response body (empty)
     * - Hanya kirim status code, tanpa data
     * - Best practice untuk DELETE endpoint
     * 
     * ResponseEntity.noContent() artinya:
     * - Set status code 204 NO CONTENT
     * - 204 artinya: "Request berhasil, tapi tidak ada content untuk dikirim"
     * - Standard response untuk DELETE yang sukses
     * 
     * .build() artinya:
     * - Build ResponseEntity tanpa body
     * - Equivalent dengan ResponseEntity.status(204).build()
     * 
     * Kenapa return 204 NO CONTENT, bukan 200 OK?
     * - 200 OK biasanya untuk response yang ada body/data
     * - 204 NO CONTENT untuk response tanpa body
     * - DELETE tidak perlu return data (siswa sudah dihapus, tidak ada yang dikembalikan)
     * - Ini adalah RESTful best practice
     * 
     * Contoh request:
     * DELETE http://localhost:8081/api/siswa/1
     * (Tidak ada request body)
     * 
     * Contoh response (success):
     * Status: 204 NO CONTENT
     * Body: (kosong, tidak ada body)
     * 
     * Contoh response (error - siswa tidak ditemukan):
     * Status: 500 Internal Server Error
     * Body:
     * {
     *   "message": "Siswa dengan ID 999 tidak ditemukan"
     * }
     * 
     * Alur di client side (JavaScript/frontend):
     * fetch('http://localhost:8081/api/siswa/1', { method: 'DELETE' })
     *   .then(response => {
     *     if (response.status === 204) {
     *       console.log('Siswa berhasil dihapus');
     *       // Refresh list siswa
     *     }
     *   });
     * 
     * Contoh test dengan curl:
     * curl -X DELETE http://localhost:8081/api/siswa/1
     * 
     * Atau dengan Postman:
     * - Method: DELETE
     * - URL: http://localhost:8081/api/siswa/1
     * - Tidak perlu set body atau headers
     * 
     * Catatan penting:
     * - DELETE adalah operasi DESTRUCTIVE (data hilang permanen)
     * - Tidak ada "undo" setelah delete
     * - Di production, lebih baik pakai "soft delete" (tandai sebagai deleted, tidak benar-benar hapus)
     * - Untuk soft delete, pakai PUT untuk update field 'deleted' = true, bukan DELETE
     * 
     * @param id ID siswa yang mau dihapus (dari URL path)
     * @return ResponseEntity kosong dengan status 204 NO CONTENT
     * @throws RuntimeException jika siswa dengan id tersebut tidak ditemukan
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        siswaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * ENDPOINT 6: GET BY KELAS - Ambil siswa berdasarkan kelas.
     * 
     * HTTP Method: GET
     * URL: GET /api/siswa/kelas/{namaKelas}
     * Request Body: tidak ada
     * Response: List<SiswaResponse> (array JSON)
     * Status Code: 200 OK
     * 
     * Cara kerja:
     * 1. Client kirim GET request dengan nama kelas di URL
     * 2. Spring extract namaKelas dari URL (@PathVariable)
     * 3. Method ini panggil siswaService.findByKelas(namaKelas)
     * 4. Service return List<SiswaResponse> (bisa kosong jika tidak ada siswa)
     * 5. Spring convert List → JSON array
     * 6. Client terima JSON response
     * 
     * @GetMapping("/kelas/{namaKelas}") artinya:
     * - Method ini handle GET request
     * - Path: /api/siswa + /kelas/{namaKelas} = /api/siswa/kelas/XII RPL 1
     * - {namaKelas} adalah placeholder untuk nama kelas
     * 
     * @PathVariable String namaKelas artinya:
     * - Ambil value dari {namaKelas} di URL
     * - Tipe data String (bukan Long seperti id)
     * - Contoh: URL /api/siswa/kelas/XII RPL 1 → namaKelas="XII RPL 1"
     * 
     * Return type: List<SiswaResponse>
     * - Return array siswa dengan kelas tertentu
     * - Jika tidak ada siswa → return array kosong [] (bukan error)
     * 
     * Use case:
     * - Tampilkan daftar siswa per kelas untuk absensi
     * - Filter siswa berdasarkan kelas
     * - Laporan presensi per kelas
     * 
     * Contoh request:
     * GET http://localhost:8081/api/siswa/kelas/XII RPL 1
     * (Spasi di URL akan di-encode jadi %20: /api/siswa/kelas/XII%20RPL%201)
     * 
     * Contoh response:
     * Status: 200 OK
     * Body:
     * [
     *   {
     *     "id": 1,
     *     "nis": "2024001",
     *     "nama": "Budi Santoso",
     *     "kelas": "XII RPL 1",
     *     "jurusan": "RPL"
     *   },
     *   {
     *     "id": 3,
     *     "nama": "Ani Wijaya",
     *     "nis": "2024003",
     *     "kelas": "XII RPL 1",
     *     "jurusan": "RPL"
     *   }
     * ]
     * 
     * Contoh response (jika tidak ada siswa di kelas tersebut):
     * Status: 200 OK
     * Body: []
     * 
     * Perbedaan dengan GET /api/siswa/{id}:
     * - GET /api/siswa/{id} → return 1 object (SiswaResponse)
     * - GET /api/siswa/kelas/{namaKelas} → return array (List<SiswaResponse>)
     * 
     * Kenapa path-nya /kelas/{namaKelas}, bukan /{namaKelas}?
     * - Untuk membedakan dengan GET /api/siswa/{id}
     * - GET /api/siswa/1 → cari siswa dengan id=1
     * - GET /api/siswa/kelas/XII RPL 1 → cari siswa dengan kelas="XII RPL 1"
     * - Tanpa prefix /kelas, Spring bingung: apakah "XII RPL 1" itu id atau kelas?
     * 
     * Contoh test dengan curl:
     * curl -X GET "http://localhost:8081/api/siswa/kelas/XII RPL 1"
     * 
     * Atau buka di browser (spasi akan auto-encode):
     * http://localhost:8081/api/siswa/kelas/XII RPL 1
     * 
     * Atau dengan Postman:
     * - Method: GET
     * - URL: http://localhost:8081/api/siswa/kelas/XII RPL 1
     * 
     * Catatan:
     * - Path variable dengan spasi akan di-encode jadi %20 (URL encoding)
     * - Spring otomatis decode %20 kembali jadi spasi
     * - Filter case-sensitive: "XII RPL 1" ≠ "xii rpl 1"
     * 
     * @param namaKelas Nama kelas yang dicari (dari URL path, contoh: "XII RPL 1")
     * @return List siswa dengan kelas tersebut (bisa kosong jika tidak ada)
     */
    @GetMapping("/kelas/{namaKelas}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GURU')")
    public List<SiswaResponse> getByKelas(@PathVariable String namaKelas) {
        return siswaService.findByKelas(namaKelas);
    }
}
