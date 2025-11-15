package com.smk.presensi.service;

import com.smk.presensi.dto.SiswaRequest;
import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.entity.Siswa;
import com.smk.presensi.repository.SiswaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SERVICE LAYER - Lapisan BUSINESS LOGIC (logika bisnis).
 * 
 * Apa itu Service Layer?
 * - Layer yang berisi LOGIKA BISNIS aplikasi
 * - Berada di TENGAH antara Controller (penerima request) dan Repository (akses database)
 * - Menangani proses-proses seperti validasi, transformasi data, orchestration
 * 
 * Kenapa perlu Service Layer? Kenapa tidak langsung Controller → Repository?
 * - SEPARATION OF CONCERNS: Setiap layer punya tanggung jawab sendiri
 *   * Controller: terima HTTP request, kirim HTTP response
 *   * Service: logika bisnis, transformasi, validasi kompleks
 *   * Repository: akses database
 * - REUSABILITY: Service bisa dipanggil dari berbagai controller
 * - TESTABILITY: Mudah di-test karena tidak terikat dengan HTTP
 * - MAINTAINABILITY: Perubahan logika bisnis tidak affect controller/repository
 * 
 * Contoh analogi:
 * - Controller = Resepsionis hotel (terima tamu, arahkan)
 * - Service = Manager hotel (atur kamar, cek availability, proses payment)
 * - Repository = Sistem database hotel (simpan/ambil data kamar)
 * 
 * Yang dilakukan SiswaService:
 * 1. Terima data dari Controller (dalam bentuk DTO: SiswaRequest)
 * 2. Transformasi DTO → Entity atau Entity → DTO
 * 3. Panggil Repository untuk operasi database
 * 4. Handle error (contoh: siswa tidak ditemukan)
 * 5. Return hasil ke Controller (dalam bentuk DTO: SiswaResponse)
 */
@Service // Tandai sebagai komponen Service (Spring akan otomatis create instance-nya)
public class SiswaService {
    // @Service membuat class ini menjadi Spring Bean yang bisa di-inject ke class lain

    // Dependency: butuh SiswaRepository untuk akses database
    // 'final' = nilai tidak bisa diubah setelah di-set (best practice untuk dependency)
    private final SiswaRepository siswaRepository;

    /**
     * CONSTRUCTOR INJECTION - cara inject dependency yang recommended.
     * 
     * Apa itu Dependency Injection?
     * - Spring akan OTOMATIS isi parameter siswaRepository dengan instance SiswaRepository
     * - Kita tidak perlu `new SiswaRepository()` secara manual
     * - Spring Container yang atur semuanya
     * 
     * Kenapa pakai Constructor Injection?
     * - Dependency WAJIB ada (kalau tidak ada, aplikasi tidak akan start)
     * - Immutable (final field, tidak bisa diubah setelah object dibuat)
     * - Lebih mudah di-test (bisa inject mock object saat testing)
     * 
     * Catatan: Tidak perlu @Autowired jika hanya ada 1 constructor (Spring auto-detect)
     * 
     * @param siswaRepository Repository untuk akses data siswa (di-inject oleh Spring)
     */
    public SiswaService(SiswaRepository siswaRepository) {
        this.siswaRepository = siswaRepository; // Simpan dependency untuk dipakai di method lain
    }

    /**
     * Ambil SEMUA data siswa dari database.
     * 
     * Alur kerja:
     * 1. Panggil siswaRepository.findAll() → dapat List<Siswa> (Entity)
     * 2. Convert List<Siswa> menjadi List<SiswaResponse> (DTO)
     * 3. Return List<SiswaResponse> ke Controller
     * 
     * Kenapa perlu konversi Entity → DTO?
     * - SECURITY: Entity mungkin punya field sensitif (password, internal id)
     * - FLEXIBILITY: Response bisa beda struktur dengan Entity (join data, computed field)
     * - API STABILITY: Perubahan Entity tidak langsung affect API response
     * 
     * Penjelasan kode baris per baris:
     * - siswaRepository.findAll() → ambil semua siswa dari database (return List<Siswa>)
     * - .stream() → ubah List menjadi Stream (aliran data untuk processing)
     * - .map(this::toResponse) → transform setiap Siswa menjadi SiswaResponse
     *     * this::toResponse adalah Method Reference, sama dengan: siswa -> toResponse(siswa)
     *     * Memanggil method toResponse() untuk setiap element
     * - .toList() → ubah Stream kembali menjadi List (Java 16+)
     * 
     * Contoh result:
     * [
     *   SiswaResponse(id=1, nis="2024001", nama="Budi", kelas="XII RPL 1", jurusan="RPL"),
     *   SiswaResponse(id=2, nis="2024002", nama="Siti", kelas="XI TKJ 2", jurusan="TKJ")
     * ]
     * 
     * @return List berisi semua data siswa dalam bentuk DTO (SiswaResponse)
     */
    public List<SiswaResponse> findAll() {
        return siswaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Cari siswa berdasarkan ID.
     * 
     * Alur kerja:
     * 1. Panggil siswaRepository.findById(id) → dapat Optional<Siswa>
     * 2. Jika siswa DITEMUKAN → convert ke SiswaResponse dan return
     * 3. Jika siswa TIDAK DITEMUKAN → throw RuntimeException
     * 
     * Kenapa pakai Optional?
     * - findById() return Optional karena data bisa ADA atau TIDAK ADA
     * - Optional adalah wadah yang mengatakan "mungkin ada value, mungkin tidak"
     * - Lebih aman daripada return null (menghindari NullPointerException)
     * 
     * Penjelasan kode baris per baris:
     * - siswaRepository.findById(id) → cari siswa, return Optional<Siswa>
     * - .orElseThrow(() -> ...) → jika Optional KOSONG (tidak ditemukan), lempar exception
     *     * () -> new RuntimeException(...) adalah Lambda expression
     *     * Lambda ini hanya dijalankan jika siswa tidak ditemukan
     * - return toResponse(siswa) → convert Entity Siswa → DTO SiswaResponse
     * 
     * Apa yang terjadi jika siswa tidak ditemukan?
     * - Method akan throw RuntimeException
     * - Exception ini akan di-catch oleh Spring (nanti di tahap selanjutnya kita handle dengan @ControllerAdvice)
     * - Client akan dapat response error 500 (Internal Server Error) atau 404 (Not Found)
     * 
     * Cara lain menulis kode yang sama (tanpa orElseThrow):
     * Optional<Siswa> optional = siswaRepository.findById(id);
     * if (optional.isPresent()) {
     *     Siswa siswa = optional.get();
     *     return toResponse(siswa);
     * } else {
     *     throw new RuntimeException("Siswa dengan ID " + id + " tidak ditemukan");
     * }
     * 
     * Versi orElseThrow() lebih singkat dan modern (recommended).
     * 
     * @param id ID siswa yang dicari (Primary Key)
     * @return SiswaResponse jika ditemukan
     * @throws RuntimeException jika siswa dengan id tersebut tidak ada di database
     */
    public SiswaResponse findById(Long id) {
        Siswa siswa = siswaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siswa dengan ID " + id + " tidak ditemukan"));
        return toResponse(siswa);
    }

    /**
     * TAMBAH siswa baru ke database.
     * 
     * Alur kerja:
     * 1. Terima data dari Controller dalam bentuk DTO (SiswaRequest)
     * 2. Buat object Entity Siswa baru (masih kosong)
     * 3. Isi field Entity dari data DTO menggunakan setter
     * 4. Simpan Entity ke database via Repository
     * 5. Convert Entity yang sudah tersimpan → DTO SiswaResponse
     * 6. Return SiswaResponse ke Controller
     * 
     * Kenapa tidak langsung save(request)?
     * - Repository hanya bisa save ENTITY (Siswa), bukan DTO (SiswaRequest)
     * - Kita perlu TRANSFORMASI: SiswaRequest (DTO) → Siswa (Entity)
     * 
     * Penjelasan kode baris per baris:
     * 
     * 1. Siswa siswa = new Siswa();
     *    → Buat object Entity Siswa baru (semua field masih null/default)
     * 
     * 2. siswa.setNis(request.nis());
     *    → Ambil nis dari DTO (request), set ke Entity (siswa)
     *    → request.nis() adalah getter otomatis dari record
     * 
     * 3. siswa.setNama(request.nama());
     *    → Set nama dari DTO ke Entity
     * 
     * 4. siswa.setKelas(request.kelas());
     *    → Set kelas dari DTO ke Entity (bisa null jika client tidak kirim)
     * 
     * 5. siswa.setJurusan(request.jurusan());
     *    → Set jurusan dari DTO ke Entity (bisa null)
     * 
     * 6. siswaRepository.save(siswa);
     *    → SIMPAN Entity ke database
     *    → JPA akan generate SQL: INSERT INTO SISWA (nis, nama, kelas, jurusan) VALUES (?, ?, ?, ?)
     *    → Setelah save, field 'id' akan otomatis terisi (auto-increment)
     *    → Object siswa sekarang punya id (contoh: id=1)
     * 
     * 7. return toResponse(siswa);
     *    → Convert Entity Siswa (yang sudah punya id) → DTO SiswaResponse
     *    → Return SiswaResponse ke Controller (yang akan kirim ke client sebagai JSON)
     * 
     * Contoh flow lengkap:
     * - Client kirim: {"nis": "2024001", "nama": "Budi", "kelas": "XII RPL 1", "jurusan": "RPL"}
     * - Method ini buat Entity Siswa, set semua field, save ke database
     * - Database generate id=1 untuk siswa ini
     * - Method return: {"id": 1, "nis": "2024001", "nama": "Budi", "kelas": "XII RPL 1", "jurusan": "RPL"}
     * - Client dapat response dengan id yang baru dibuat
     * 
     * Catatan penting:
     * - Field 'id' TIDAK diset karena AUTO-INCREMENT (database yang atur)
     * - Field rfidCardId, barcodeId, faceId juga tidak diset (akan null, nanti diisi saat integrasi device)
     * - Validasi @NotBlank sudah dilakukan di Controller (via @Valid), jadi disini pasti sudah valid
     * 
     * @param request Data siswa baru dari client (DTO)
     * @return SiswaResponse berisi data siswa yang baru tersimpan (termasuk id yang baru dibuat)
     */
    public SiswaResponse create(SiswaRequest request) {
        Siswa siswa = new Siswa();
        siswa.setNis(request.nis());
        siswa.setNama(request.nama());
        siswa.setKelas(request.kelas());
        siswa.setJurusan(request.jurusan());
        siswaRepository.save(siswa);
        return toResponse(siswa);
    }

    /**
     * UPDATE (ubah) data siswa yang sudah ada.
     * 
     * Alur kerja:
     * 1. Cari siswa berdasarkan ID (throw exception jika tidak ditemukan)
     * 2. Update field-field Entity dengan data baru dari DTO
     * 3. Simpan Entity yang sudah diubah ke database
     * 4. Convert Entity → DTO SiswaResponse
     * 5. Return SiswaResponse ke Controller
     * 
     * Perbedaan UPDATE vs CREATE:
     * - CREATE: buat Entity baru (new Siswa()), id masih null
     * - UPDATE: ambil Entity yang sudah ada dari database (sudah punya id)
     * 
     * Kenapa harus findById dulu? Kenapa tidak langsung update?
     * - JPA perlu tahu Entity mana yang mau diupdate (berdasarkan id)
     * - Kita perlu cek apakah siswa dengan id tersebut ADA (kalau tidak ada, throw error)
     * - Best practice: always validate before update
     * 
     * Penjelasan kode baris per baris:
     * 
     * 1. Siswa siswa = siswaRepository.findById(id).orElseThrow(...);
     *    → Cari siswa berdasarkan id
     *    → Jika TIDAK DITEMUKAN → throw RuntimeException (gagal update)
     *    → Jika DITEMUKAN → simpan ke variable 'siswa'
     * 
     * 2. siswa.setNama(request.nama());
     *    → Update field 'nama' dengan value baru dari DTO
     * 
     * 3. siswa.setKelas(request.kelas());
     *    → Update field 'kelas' dengan value baru
     * 
     * 4. siswa.setJurusan(request.jurusan());
     *    → Update field 'jurusan' dengan value baru
     * 
     * 5. // NIS tidak diupdate karena unique identifier
     *    → NIS adalah NOMOR INDUK, seperti NIK, tidak boleh diubah setelah dibuat
     *    → Kalau NIS berubah, maka itu sudah SISWA BARU, bukan update
     *    → Kita SENGAJA TIDAK PANGGIL siswa.setNis(...)
     * 
     * 6. siswaRepository.save(siswa);
     *    → Simpan perubahan ke database
     *    → Karena Entity sudah punya id, JPA tahu ini UPDATE bukan INSERT
     *    → JPA generate SQL: UPDATE SISWA SET nama=?, kelas=?, jurusan=? WHERE id=?
     * 
     * 7. return toResponse(siswa);
     *    → Convert Entity → DTO SiswaResponse
     *    → Return data yang sudah diupdate ke Controller
     * 
     * Bagaimana JPA tahu ini UPDATE bukan INSERT?
     * - Kalau Entity punya id (id != null) → UPDATE
     * - Kalau Entity tidak punya id (id == null) → INSERT
     * 
     * Contoh flow lengkap:
     * - Client kirim PUT /api/siswa/1 dengan body: {"nis": "2024001", "nama": "Budi Santoso Baru", "kelas": "XII RPL 2"}
     * - Method ini cari siswa id=1, update nama dan kelas (NIS tetap)
     * - Save perubahan ke database
     * - Return: {"id": 1, "nis": "2024001", "nama": "Budi Santoso Baru", "kelas": "XII RPL 2", "jurusan": "RPL"}
     * 
     * Catatan penting:
     * - ID tidak berubah (primary key tidak boleh diupdate)
     * - NIS tidak diupdate (unique identifier, seperti NIK)
     * - Hanya field nama, kelas, jurusan yang bisa diupdate
     * - Jika ingin update NIS, harus hapus siswa lama dan buat siswa baru
     * 
     * @param id ID siswa yang mau diupdate
     * @param request Data baru untuk siswa (DTO)
     * @return SiswaResponse berisi data siswa yang sudah diupdate
     * @throws RuntimeException jika siswa dengan id tersebut tidak ditemukan
     */
    public SiswaResponse update(Long id, SiswaRequest request) {
        Siswa siswa = siswaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siswa dengan ID " + id + " tidak ditemukan"));
        siswa.setNama(request.nama());
        siswa.setKelas(request.kelas());
        siswa.setJurusan(request.jurusan());
        // NIS tidak diupdate karena unique identifier
        siswaRepository.save(siswa);
        return toResponse(siswa);
    }

    /**
     * HAPUS siswa dari database.
     * 
     * Alur kerja:
     * 1. Cek apakah siswa dengan id tersebut ADA di database
     * 2. Jika TIDAK ADA → throw exception (gagal delete)
     * 3. Jika ADA → hapus dari database
     * 4. Method ini return void (tidak return data)
     * 
     * Kenapa cek existsById dulu? Kenapa tidak langsung deleteById?
     * - deleteById() tidak throw error jika id tidak ditemukan (silent fail)
     * - Client tidak tahu apakah delete berhasil atau data tidak ada
     * - Best practice: validate before delete, give clear error message
     * 
     * Penjelasan kode baris per baris:
     * 
     * 1. if (!siswaRepository.existsById(id))
     *    → Cek apakah siswa dengan id ini ADA di database
     *    → existsById() return boolean: true (ada) atau false (tidak ada)
     *    → ! (tanda seru) artinya NOT/negasi
     *    → Jadi if ini artinya: "Jika siswa TIDAK ADA..."
     * 
     * 2. throw new RuntimeException("Siswa dengan ID " + id + " tidak ditemukan");
     *    → Lempar exception dengan pesan error yang jelas
     *    → Client akan dapat response error 404 atau 500 (tergantung error handler)
     *    → Execution berhenti disini (tidak lanjut ke baris selanjutnya)
     * 
     * 3. siswaRepository.deleteById(id);
     *    → HAPUS siswa dari database berdasarkan id
     *    → JPA generate SQL: DELETE FROM SISWA WHERE id=?
     *    → Data siswa hilang PERMANEN dari database
     * 
     * Kenapa return type void (tidak return apa-apa)?
     * - Operasi delete biasanya tidak perlu return data
     * - Client cukup tahu: berhasil (HTTP 204 No Content) atau gagal (HTTP 404/500)
     * - Jika mau return data, bisa return message "Siswa berhasil dihapus"
     * 
     * Perbedaan existsById vs findById:
     * - existsById() → cuma cek ADA atau TIDAK (return boolean)
     *     * Lebih cepat karena tidak perlu load semua data
     *     * SQL: SELECT COUNT(*) FROM SISWA WHERE id=?
     * - findById() → ambil SEMUA DATA siswa (return Optional<Siswa>)
     *     * Lebih lambat karena load semua field
     *     * SQL: SELECT * FROM SISWA WHERE id=?
     * 
     * Untuk delete, cukup pakai existsById() karena kita hanya butuh tahu ADA atau TIDAK.
     * 
     * Contoh flow lengkap:
     * - Client kirim DELETE /api/siswa/1
     * - Method ini cek apakah siswa id=1 ada
     * - Jika ADA → hapus dari database, return success (204 No Content)
     * - Jika TIDAK ADA → throw exception, return error (404 Not Found)
     * 
     * Catatan penting:
     * - DELETE adalah operasi DESTRUCTIVE (tidak bisa undo)
     * - Data hilang PERMANEN dari database
     * - Di production, lebih baik pakai "soft delete" (tandai sebagai deleted, tidak benar-benar hapus)
     * - Soft delete: tambah field 'deleted' atau 'deletedAt', jadi data masih ada tapi tidak ditampilkan
     * 
     * @param id ID siswa yang mau dihapus
     * @throws RuntimeException jika siswa dengan id tersebut tidak ditemukan
     */
    public void delete(Long id) {
        if (!siswaRepository.existsById(id)) {
            throw new RuntimeException("Siswa dengan ID " + id + " tidak ditemukan");
        }
        siswaRepository.deleteById(id);
    }

    /**
     * Cari siswa berdasarkan KELAS.
     * 
     * Alur kerja:
     * 1. Panggil siswaRepository.findByKelas(kelas) → dapat List<Siswa> (Entity)
     * 2. Convert setiap Siswa → SiswaResponse (DTO) menggunakan Stream
     * 3. Return List<SiswaResponse> ke Controller
     * 
     * Use case:
     * - Tampilkan daftar siswa per kelas untuk absensi
     * - Filter siswa berdasarkan kelas tertentu
     * - Laporan per kelas
     * 
     * Penjelasan kode (sama dengan findAll, tapi filtered):
     * - siswaRepository.findByKelas(kelas) → ambil siswa dengan kelas tertentu
     * - .stream() → ubah List<Siswa> menjadi Stream untuk processing
     * - .map(this::toResponse) → convert setiap Siswa menjadi SiswaResponse
     * - .toList() → ubah Stream kembali menjadi List<SiswaResponse>
     * 
     * Contoh penggunaan:
     * - findByKelas("XII RPL 1") → return semua siswa kelas XII RPL 1
     * - findByKelas("XI TKJ 2") → return semua siswa kelas XI TKJ 2
     * 
     * Contoh result:
     * [
     *   SiswaResponse(id=1, nis="2024001", nama="Budi", kelas="XII RPL 1", jurusan="RPL"),
     *   SiswaResponse(id=3, nis="2024003", nama="Ani", kelas="XII RPL 1", jurusan="RPL")
     * ]
     * 
     * Catatan:
     * - Jika tidak ada siswa dengan kelas tersebut → return List kosong []
     * - Tidak throw exception (berbeda dengan findById)
     * 
     * @param kelas Nama kelas yang dicari (contoh: "XII RPL 1", "XI TKJ 2")
     * @return List berisi siswa dengan kelas tersebut (bisa kosong jika tidak ada)
     */
    public List<SiswaResponse> findByKelas(String kelas) {
        return siswaRepository.findByKelas(kelas).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * HELPER METHOD - Convert Entity Siswa → DTO SiswaResponse.
     * 
     * Kenapa perlu method ini?
     * - Kode konversi Entity → DTO dipanggil di banyak tempat (findAll, findById, create, update)
     * - Daripada tulis kode yang sama berulang-ulang, lebih baik buat 1 method
     * - Principle: DRY (Don't Repeat Yourself)
     * 
     * Apa yang dilakukan method ini?
     * - Ambil data dari Entity (Siswa)
     * - Buat object DTO baru (SiswaResponse)
     * - Isi DTO dengan data dari Entity
     * - Return DTO
     * 
     * Modifier 'private':
     * - Method ini hanya dipanggil INTERNAL di SiswaService
     * - Controller atau class lain TIDAK BISA panggil method ini
     * - Karena ini adalah implementation detail, bukan public API
     * 
     * Penjelasan kode baris per baris:
     * 
     * 1. return new SiswaResponse(...)
     *    → Buat object SiswaResponse baru menggunakan constructor
     *    → SiswaResponse adalah RECORD, jadi constructor otomatis ada
     * 
     * 2. siswa.getId()
     *    → Ambil id dari Entity Siswa menggunakan getter
     *    → Masukkan sebagai parameter pertama constructor SiswaResponse
     * 
     * 3. siswa.getNis()
     *    → Ambil nis dari Entity
     *    → Masukkan sebagai parameter kedua
     * 
     * 4. siswa.getNama()
     *    → Ambil nama dari Entity
     *    → Masukkan sebagai parameter ketiga
     * 
     * 5. siswa.getKelas()
     *    → Ambil kelas dari Entity
     *    → Masukkan sebagai parameter keempat
     * 
     * 6. siswa.getJurusan()
     *    → Ambil jurusan dari Entity
     *    → Masukkan sebagai parameter kelima
     * 
     * Mapping field (Entity → DTO):
     * Entity Siswa                    DTO SiswaResponse
     * ------------------------------------------
     * id (Long)              →        id (Long)
     * nis (String)           →        nis (String)
     * nama (String)          →        nama (String)
     * kelas (String)         →        kelas (String)
     * jurusan (String)       →        jurusan (String)
     * rfidCardId (String)    →        TIDAK DIMAPPING (tidak ada di Response)
     * barcodeId (String)     →        TIDAK DIMAPPING
     * faceId (String)        →        TIDAK DIMAPPING
     * 
     * Kenapa rfidCardId, barcodeId, faceId tidak dimapping?
     * - Field-field ini adalah TECHNICAL detail (untuk sistem presensi internal)
     * - Client/frontend tidak perlu tahu data ini
     * - API response jadi lebih CLEAN dan SIMPLE
     * - Principle: API should expose only what's necessary
     * 
     * Contoh input-output:
     * Input (Entity Siswa):
     *   id=1, nis="2024001", nama="Budi", kelas="XII RPL 1", jurusan="RPL",
     *   rfidCardId="ABC123", barcodeId="XYZ789", faceId="FACE001"
     * 
     * Output (DTO SiswaResponse):
     *   id=1, nis="2024001", nama="Budi", kelas="XII RPL 1", jurusan="RPL"
     * 
     * Field rfidCardId, barcodeId, faceId TIDAK masuk response (filtered out).
     * 
     * @param siswa Entity Siswa yang mau diconvert
     * @return SiswaResponse (DTO) berisi data siswa untuk dikirim ke client
     */
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
