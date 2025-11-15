# Panduan Pengujian API Tahap 02 dengan Postman

## Pendahuluan

Dokumen ini adalah alternatif dari video Postman testing untuk Tahap 02. Berisi langkah-langkah detail pengujian semua endpoint CRUD Siswa menggunakan Postman.

## Persiapan

1. **Pastikan aplikasi berjalan:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   - Server akan berjalan di: `http://localhost:8081`
   - H2 Console: `http://localhost:8081/h2-console`

2. **Buka Postman:**
   - Download dari [postman.com/downloads](https://www.postman.com/downloads/) jika belum punya
   - Buat collection baru bernama "Presensi API - Tahap 02"

---

## Test 1: Create Siswa (POST)

**Endpoint:** `POST http://localhost:8081/api/siswa`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nis": "12345",
  "nama": "Budi Santoso",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}
```

**Expected Response: `201 CREATED`**
```json
{
  "id": 1,
  "nis": "12345",
  "nama": "Budi Santoso",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}
```

**Cara Test di Postman:**
1. Klik tombol "+ New Request"
2. Set method ke **POST**
3. Masukkan URL: `http://localhost:8081/api/siswa`
4. Pilih tab **Body** → pilih **raw** → pilih **JSON**
5. Copy paste JSON body di atas
6. Klik **Send**
7. Pastikan status code: `201 Created`
8. Pastikan response body ada field `id` (auto generated)

**Screenshot yang diharapkan:**
- Status: 201 Created (hijau)
- Response body dengan id = 1
- Time: biasanya < 100ms

---

## Test 2: Create Siswa Kedua

**Endpoint:** `POST http://localhost:8081/api/siswa`

**Body:**
```json
{
  "nis": "12346",
  "nama": "Siti Nurhaliza",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}
```

**Expected Response: `201 CREATED`**
```json
{
  "id": 2,
  "nis": "12346",
  "nama": "Siti Nurhaliza",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}
```

**Catatan:** ID akan increment otomatis menjadi 2.

---

## Test 3: Validation Error (NIS kosong)

**Endpoint:** `POST http://localhost:8081/api/siswa`

**Body (NIS kosong):**
```json
{
  "nis": "",
  "nama": "Invalid Student",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}
```

**Expected Response: `400 BAD REQUEST`**
```json
{
  "timestamp": "2024-05-01T12:34:56.789+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/siswa"
}
```

**Catatan:** 
- Spring validation akan reject request karena `@NotBlank` pada field nis
- Status code 400 menandakan client error
- Ini adalah behaviour yang **benar** (data tidak boleh tersimpan)

---

## Test 4: Get All Siswa (GET)

**Endpoint:** `GET http://localhost:8081/api/siswa`

**Headers:** (tidak perlu, GET biasanya tanpa header khusus)

**Expected Response: `200 OK`**
```json
[
  {
    "id": 1,
    "nis": "12345",
    "nama": "Budi Santoso",
    "kelas": "XII RPL 1",
    "jurusan": "RPL"
  },
  {
    "id": 2,
    "nis": "12346",
    "nama": "Siti Nurhaliza",
    "kelas": "XII RPL 1",
    "jurusan": "RPL"
  }
]
```

**Cara Test:**
1. Buat request baru
2. Set method ke **GET**
3. URL: `http://localhost:8081/api/siswa`
4. Klik **Send**
5. Pastikan dapat array dengan 2 siswa

**Catatan:** Jika masih kosong `[]`, berarti database H2 (in-memory) sudah di-reset karena restart aplikasi.

---

## Test 5: Get Siswa by ID

**Endpoint:** `GET http://localhost:8081/api/siswa/1`

**Expected Response: `200 OK`**
```json
{
  "id": 1,
  "nis": "12345",
  "nama": "Budi Santoso",
  "kelas": "XII RPL 1",
  "jurusan": "RPL"
}
```

**Test dengan ID tidak ada:**
`GET http://localhost:8081/api/siswa/999`

**Expected Response: `500 INTERNAL SERVER ERROR`**
```json
{
  "timestamp": "...",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Siswa dengan id 999 tidak ditemukan",
  "path": "/api/siswa/999"
}
```

**Catatan:** Error 500 karena kita throw RuntimeException di service. Nanti di Tahap berikutnya akan kita perbaiki jadi 404 Not Found dengan custom exception handler.

---

## Test 6: Update Siswa (PUT)

**Endpoint:** `PUT http://localhost:8081/api/siswa/1`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "nis": "12345",
  "nama": "Budi Santoso (Updated)",
  "kelas": "XII RPL 2",
  "jurusan": "RPL"
}
```

**Expected Response: `200 OK`**
```json
{
  "id": 1,
  "nis": "12345",
  "nama": "Budi Santoso (Updated)",
  "kelas": "XII RPL 2",
  "jurusan": "RPL"
}
```

**Cara Test:**
1. Method: **PUT**
2. URL: `http://localhost:8081/api/siswa/1` (ganti ID sesuai data yang ada)
3. Body: raw JSON seperti di atas
4. Klik **Send**
5. Verifikasi nama dan kelas sudah berubah

---

## Test 7: Get Siswa by Kelas

**Endpoint:** `GET http://localhost:8081/api/siswa/kelas/XII%20RPL%201`

**Catatan:** Spasi di URL harus di-encode menjadi `%20`

**Expected Response: `200 OK`**
```json
[
  {
    "id": 2,
    "nis": "12346",
    "nama": "Siti Nurhaliza",
    "kelas": "XII RPL 1",
    "jurusan": "RPL"
  }
]
```

**Cara Test di Postman:**
1. Method: **GET**
2. URL: `http://localhost:8081/api/siswa/kelas/XII RPL 1` (Postman akan auto-encode spasi)
3. Klik **Send**
4. Harus dapat array siswa dari kelas tersebut

**Filter dengan kelas tidak ada:**
`GET http://localhost:8081/api/siswa/kelas/XI%20TKJ%201`

**Expected:** Array kosong `[]` (bukan error, karena tidak ada data)

---

## Test 8: Delete Siswa (DELETE)

**Endpoint:** `DELETE http://localhost:8081/api/siswa/2`

**Expected Response: `204 NO CONTENT`**
(Tidak ada body response, hanya status code)

**Cara Test:**
1. Method: **DELETE**
2. URL: `http://localhost:8081/api/siswa/2`
3. Klik **Send**
4. Status: `204 No Content` (artinya berhasil, tanpa response body)

**Verifikasi:**
Setelah delete, coba GET all siswa:
`GET http://localhost:8081/api/siswa`

Siswa dengan ID 2 sudah tidak ada.

---

## Tips Postman

### 1. Save Request ke Collection
- Setiap request bisa di-save dengan nama yang jelas
- Contoh nama:
  - `[POST] Create Siswa`
  - `[GET] Get All Siswa`
  - `[PUT] Update Siswa`
  - `[DELETE] Delete Siswa`

### 2. Environment Variables
Buat environment "Local" dengan variable:
```
base_url = http://localhost:8081
```

Lalu gunakan di URL:
```
{{base_url}}/api/siswa
```

### 3. Export Collection
- Klik tiga titik di collection → Export
- Pilih format Collection v2.1
- Save sebagai `Presensi-API-Tahap-02.postman_collection.json`
- Bisa di-share ke teman sekelas

### 4. Test Scripts
Tambahkan test script otomatis (tab Tests):
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has id field", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
});
```

---

## Checklist Testing

**Sebelum commit, pastikan semua test berhasil:**

- [ ] ✅ POST create siswa → 201 Created dengan id auto-generated
- [ ] ✅ POST create siswa kedua → 201 Created dengan id increment
- [ ] ✅ POST validation error (NIS kosong) → 400 Bad Request
- [ ] ✅ GET all siswa → 200 OK dengan array siswa
- [ ] ✅ GET siswa by ID → 200 OK dengan data siswa
- [ ] ✅ GET siswa by ID tidak ada → 500 Error (nanti diperbaiki jadi 404)
- [ ] ✅ PUT update siswa → 200 OK dengan data terupdate
- [ ] ✅ GET siswa by kelas → 200 OK dengan filter kelas
- [ ] ✅ DELETE siswa → 204 No Content tanpa body

---

## Common Errors dan Solusi

### Error: Connection refused
**Penyebab:** Aplikasi belum running  
**Solusi:** Jalankan `mvn spring-boot:run` dulu

### Error: 404 Not Found
**Penyebab:** Salah URL atau endpoint belum ada  
**Solusi:** Cek URL dan controller mapping

### Error: 400 Bad Request (Content-Type)
**Penyebab:** Lupa set Content-Type: application/json  
**Solusi:** Tambahkan header di Postman

### Error: 500 Internal Server Error
**Penyebab:** Biasanya validation atau data tidak sesuai  
**Solusi:** Cek log aplikasi di terminal, lihat stack trace

### Database kosong setelah restart
**Penyebab:** H2 in-memory, data hilang saat aplikasi stop  
**Solusi:** Normal behaviour, bisa tambahkan data.sql nanti untuk data dummy

---

## Next Steps

Setelah semua test berhasil:

1. **Commit ke Git:**
   ```bash
   git add .
   git commit -m "feat: implement siswa CRUD (Tahap 02)"
   ```

2. **Lanjut ke Guru dan Kelas CRUD** (menggunakan pattern yang sama)

3. **Tahap selanjutnya:** Implement Security & JWT (Tahap 05)

---

## Referensi

- [Postman Learning Center](https://learning.postman.com/)
- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- [REST API Best Practices](https://restfulapi.net/)

**Selamat mencoba!** 🚀
