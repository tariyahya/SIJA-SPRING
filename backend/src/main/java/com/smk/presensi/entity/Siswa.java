package com.smk.presensi.entity;

// Import anotasi JPA (Jakarta Persistence API) untuk database
import jakarta.persistence.*;  // * artinya import semua kelas di package jakarta.persistence

/**
 * ENTITY SISWA - Representasi tabel SISWA di database
 * 
 * Entity adalah kelas Java yang merepresentasikan tabel di database.
 * Satu object Siswa = satu baris data di tabel SISWA.
 * 
 * Contoh:
 * Object: new Siswa(id=1, nis="12345", nama="Budi")
 * Di database: | id | nis   | nama |
 *              | 1  | 12345 | Budi |
 * 
 * Analogi: Entity seperti "formulir" yang struktur fieldnya sama dengan kolom tabel database.
 */
@Entity  // Anotasi ini memberitahu JPA: "Kelas ini adalah entity (akan jadi tabel di database)"
         // JPA otomatis buat tabel dengan nama kelas (Siswa → tabel SISWA)
@Table(name = "siswa")  // Opsional: paksa nama tabel jadi "siswa" (huruf kecil semua)
                        // Tanpa ini, nama tabel = nama kelas (Siswa dengan huruf besar S)
public class Siswa {

    /**
     * ID - Primary Key (kunci utama)
     * 
     * Setiap tabel database WAJIB punya primary key untuk identify setiap baris data.
     * Primary key harus UNIK (tidak boleh ada yang sama).
     */
    @Id  // Anotasi ini menandai field id sebagai PRIMARY KEY
         // Primary key = kunci utama = identifier unik untuk setiap data
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
         // Anotasi ini memberitahu database: "Generate nilai id otomatis"
         // GenerationType.IDENTITY = database yang generate nilai (auto increment)
         // Contoh: data pertama id=1, kedua id=2, ketiga id=3, dst
         // Kita TIDAK perlu set id manual, database yang urus!
    private Long id;  // Long adalah tipe data untuk bilangan bulat besar
                      // Kenapa Long bukan int? Long bisa null, int tidak bisa

    /**
     * NIS - Nomor Induk Siswa
     * 
     * NIS harus UNIK (tidak boleh ada 2 siswa dengan NIS sama).
     */
    @Column(nullable = false, unique = true)  
         // @Column customize kolom di database
         // nullable = false : kolom ini WAJIB diisi (NOT NULL di SQL)
         // unique = true    : nilai harus UNIK (tidak boleh duplikat)
         // Jadi NIS:
         // ✅ WAJIB ada nilainya
         // ✅ TIDAK BOLEH sama dengan siswa lain
    private String nis;  // String = tipe data untuk teks/karakter
                         // Contoh: "12345", "20240001", dll

    /**
     * NAMA - Nama lengkap siswa
     */
    @Column(nullable = false)  // Nama WAJIB diisi tapi boleh sama dengan siswa lain
    private String nama;

    /**
     * KELAS - Kelas siswa
     * 
     * Tidak ada anotasi @Column artinya pakai default:
     * - nullable = true (boleh kosong)
     * - unique = false (boleh duplikat/sama)
     */
    private String kelas;  // Contoh: "XII RPL 1", "XI TKJ 2"

    /**
     * JURUSAN - Jurusan siswa
     */
    private String jurusan;  // Contoh: "RPL", "TKJ", "MM", "Akuntansi"

    /**
     * RFID CARD ID - ID kartu RFID untuk presensi tap card
     */
    private String rfidCardId;  // Contoh: "A1B2C3D4" (ID unik kartu RFID)

    /**
     * BARCODE ID - ID barcode untuk presensi scan barcode
     */
    private String barcodeId;  // Contoh: "123456789012" (kode barcode)

    /**
     * FACE ID - ID face recognition untuk presensi wajah
     */
    private String faceId;  // Contoh: "face_vector_12345" (data wajah yang sudah diproses)

    /**
     * CONSTRUCTOR KOSONG (No-args constructor)
     * 
     * JPA WAJIB butuh constructor tanpa parameter.
     * JPA pakai constructor ini untuk create object dari data database.
     * 
     * Jangan dihapus! Kalau dihapus, JPA akan error.
     */
    public Siswa() {
        // Constructor kosong, tidak ada kode
        // JPA yang pakai untuk create object baru
    }

    /**
     * GETTER & SETTER
     * 
     * Getter = method untuk AMBIL/BACA nilai field
     * Setter = method untuk UBAH/SET nilai field
     * 
     * Kenapa perlu getter/setter? Kenapa tidak akses field langsung?
     * - Enkapsulasi (principle OOP): field harus private, akses lewat method
     * - Validasi: di setter bisa tambah validasi sebelum set nilai
     * - Framework: Spring, JPA, Jackson butuh getter/setter
     * 
     * Pattern nama:
     * - Getter: get + NamaField (huruf besar pertama)
     *   Contoh: getId(), getNis(), getNama()
     * - Setter: set + NamaField (huruf besar pertama)
     *   Contoh: setId(Long id), setNis(String nis)
     */

    // Getter & Setter untuk ID
    public Long getId() { 
        return id;  // Return nilai field id
    }
    public void setId(Long id) { 
        this.id = id;  // this.id = field milik object
                       // id (tanpa this) = parameter method
    }

    // Getter & Setter untuk NIS
    public String getNis() { 
        return nis; 
    }
    public void setNis(String nis) { 
        this.nis = nis; 
    }

    // Getter & Setter untuk NAMA
    public String getNama() { 
        return nama; 
    }
    public void setNama(String nama) { 
        this.nama = nama; 
    }

    // Getter & Setter untuk KELAS
    public String getKelas() { 
        return kelas; 
    }
    public void setKelas(String kelas) { 
        this.kelas = kelas; 
    }

    // Getter & Setter untuk JURUSAN
    public String getJurusan() { 
        return jurusan; 
    }
    public void setJurusan(String jurusan) { 
        this.jurusan = jurusan; 
    }

    // Getter & Setter untuk RFID CARD ID
    public String getRfidCardId() { 
        return rfidCardId; 
    }
    public void setRfidCardId(String rfidCardId) { 
        this.rfidCardId = rfidCardId; 
    }

    // Getter & Setter untuk BARCODE ID
    public String getBarcodeId() { 
        return barcodeId; 
    }
    public void setBarcodeId(String barcodeId) { 
        this.barcodeId = barcodeId; 
    }

    // Getter & Setter untuk FACE ID
    public String getFaceId() { 
        return faceId; 
    }
    public void setFaceId(String faceId) { 
        this.faceId = faceId; 
    }
}
