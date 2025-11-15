package com.smk.presensi.entity;

// Import anotasi JPA untuk database mapping
import jakarta.persistence.*;

/**
 * ENTITY KELAS - Representasi tabel KELAS di database
 * 
 * Entity Kelas menyimpan data tentang kelas-kelas yang ada di sekolah.
 * Contoh: XII RPL 1, XI TKJ 2, X Akuntansi 1, dst.
 * 
 * Setiap kelas punya:
 * - Nama kelas (XII RPL 1)
 * - Tingkat (XII)
 * - Jurusan (RPL)
 * - Wali kelas (ID guru yang jadi wali kelas)
 */
@Entity  // Menandai kelas ini sebagai entity (tabel database)
@Table(name = "kelas")  // Nama tabel di database = "kelas"
public class Kelas {

    /**
     * ID - Primary Key (kunci utama)
     * Auto increment, database yang generate otomatis
     */
    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto increment
    private Long id;

    /**
     * NAMA - Nama kelas
     * Contoh: "XII RPL 1", "XI TKJ 2", "X MM 1"
     * Wajib diisi
     */
    @Column(nullable = false)  // WAJIB diisi
    private String nama;  // Nama lengkap kelas

    /**
     * TINGKAT - Tingkat/jenjang kelas
     * Contoh: "X" (kelas 10), "XI" (kelas 11), "XII" (kelas 12)
     * Boleh kosong
     */
    private String tingkat;  // X, XI, atau XII

    /**
     * JURUSAN - Jurusan kelas
     * Contoh: "RPL" (Rekayasa Perangkat Lunak), "TKJ", "MM", "Akuntansi"
     * Boleh kosong
     */
    private String jurusan;  // RPL, TKJ, MM, Akuntansi, dll

    /**
     * WALI KELAS ID - ID guru yang jadi wali kelas
     * Menyimpan ID guru (foreign key ke tabel GURU)
     * 
     * Contoh:
     * - Guru dengan id=5 jadi wali kelas XII RPL 1
     * - Maka waliKelasId = 5
     * 
     * Nanti di tahap lebih lanjut, kita akan buat relasi @ManyToOne ke Guru
     * Untuk sekarang, kita simpan ID nya dulu saja (foreign key manual)
     */
    private Long waliKelasId;  // ID guru yang jadi wali kelas

    /**
     * Constructor kosong - WAJIB untuk JPA
     * Jangan dihapus!
     */
    public Kelas() {
        // Constructor kosong untuk JPA
    }

    /**
     * GETTER & SETTER
     * Method untuk akses dan ubah nilai field
     */

    // Getter & Setter untuk ID
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    // Getter & Setter untuk NAMA
    public String getNama() { 
        return nama; 
    }
    public void setNama(String nama) { 
        this.nama = nama; 
    }

    // Getter & Setter untuk TINGKAT
    public String getTingkat() { 
        return tingkat; 
    }
    public void setTingkat(String tingkat) { 
        this.tingkat = tingkat; 
    }

    // Getter & Setter untuk JURUSAN
    public String getJurusan() { 
        return jurusan; 
    }
    public void setJurusan(String jurusan) { 
        this.jurusan = jurusan; 
    }

    // Getter & Setter untuk WALI KELAS ID
    public Long getWaliKelasId() { 
        return waliKelasId; 
    }
    public void setWaliKelasId(Long waliKelasId) { 
        this.waliKelasId = waliKelasId; 
    }
}
