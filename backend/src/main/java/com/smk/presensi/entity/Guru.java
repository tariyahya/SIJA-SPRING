package com.smk.presensi.entity;

// Import anotasi JPA untuk database mapping
import jakarta.persistence.*;

/**
 * ENTITY GURU - Representasi tabel GURU di database
 * 
 * Entity Guru mirip dengan entity Siswa, tapi untuk data guru.
 * Struktur dan cara kerjanya sama, hanya field-field nya yang berbeda.
 * 
 * Perbedaan Guru vs Siswa:
 * - Guru pakai NIP (Nomor Induk Pegawai)
 * - Siswa pakai NIS (Nomor Induk Siswa)
 * - Guru punya mapel (mata pelajaran)
 * - Siswa punya kelas & jurusan
 */
@Entity  // Menandai kelas ini sebagai entity (tabel database)
@Table(name = "guru")  // Nama tabel di database = "guru"
public class Guru {

    /**
     * ID - Primary Key (kunci utama)
     * Auto increment, database yang generate otomatis
     */
    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto increment
    private Long id;

    /**
     * NIP - Nomor Induk Pegawai
     * Wajib diisi dan harus unik (tidak boleh ada guru dengan NIP sama)
     */
    @Column(nullable = false, unique = true)  // WAJIB dan UNIK
    private String nip;  // Contoh: "198501012010012001"

    /**
     * NAMA - Nama lengkap guru
     * Wajib diisi tapi boleh sama (bisa ada 2 guru bernama sama)
     */
    @Column(nullable = false)  // WAJIB diisi
    private String nama;  // Contoh: "Budi Santoso, S.Pd"

    /**
     * MAPEL - Mata Pelajaran yang diampu
     * Boleh kosong, boleh sama (bisa banyak guru ngajar mapel yang sama)
     */
    private String mapel;  // Contoh: "Matematika", "Bahasa Indonesia", "Pemrograman Web"

    /**
     * RFID CARD ID - ID kartu RFID untuk presensi
     * Sama seperti siswa, guru juga bisa presensi pakai RFID
     */
    private String rfidCardId;

    /**
     * BARCODE ID - ID barcode untuk presensi
     */
    private String barcodeId;

    /**
     * FACE ID - ID face recognition untuk presensi
     */
    private String faceId;

    /**
     * FACE ENCODING - Face recognition encoding (128 characters).
     * 
     * Ini adalah "signature" digital dari wajah guru.
     * Generated dari foto saat enrollment.
     * 
     * Format: String 128 characters (hex)
     * Contoh: "a3f5b2c9d1e4f7a8b2c5d8e1f4a7b0c3..."
     * 
     * NULL = belum enroll face
     * NOT NULL = sudah enroll, bisa pakai face recognition
     */
    @Column(name = "face_encoding", length = 500)
    private String faceEncoding;

    /**
     * FACE ENROLLED AT - Timestamp saat enroll face.
     * 
     * Mencatat kapan guru enroll face.
     * Berguna untuk audit dan re-enrollment policy.
     */
    @Column(name = "face_enrolled_at")
    private java.time.LocalDateTime faceEnrolledAt;

    /**
     * USER - Relasi ke User (akun login)
     * 
     * Tidak semua guru punya akun login.
     * Hanya guru yang perlu akses sistem yang diberi akun.
     * 
     * Relasi: OneToOne (1 guru max 1 user, 1 user max 1 guru)
     * 
     * @OneToOne: Relasi one-to-one dengan User
     * @JoinColumn: Foreign key di tabel guru
     *   - name = "user_id": Nama kolom FK di tabel guru
     *   - nullable = true: Boleh null (guru tidak wajib punya user)
     *   - unique = true: Satu user hanya boleh link ke 1 guru
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = true, unique = true)
    private User user;  // Akun login guru (opsional)

    /**
     * Constructor kosong - WAJIB untuk JPA
     * Jangan dihapus!
     */
    public Guru() {
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

    // Getter & Setter untuk NIP
    public String getNip() { 
        return nip; 
    }
    public void setNip(String nip) { 
        this.nip = nip; 
    }

    // Getter & Setter untuk NAMA
    public String getNama() { 
        return nama; 
    }
    public void setNama(String nama) { 
        this.nama = nama; 
    }

    // Getter & Setter untuk MAPEL
    public String getMapel() { 
        return mapel; 
    }
    public void setMapel(String mapel) { 
        this.mapel = mapel; 
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

    // Getter & Setter untuk FACE ENCODING
    public String getFaceEncoding() {
        return faceEncoding;
    }
    public void setFaceEncoding(String faceEncoding) {
        this.faceEncoding = faceEncoding;
    }

    // Getter & Setter untuk FACE ENROLLED AT
    public java.time.LocalDateTime getFaceEnrolledAt() {
        return faceEnrolledAt;
    }
    public void setFaceEnrolledAt(java.time.LocalDateTime faceEnrolledAt) {
        this.faceEnrolledAt = faceEnrolledAt;
    }

    // Getter & Setter untuk USER
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
