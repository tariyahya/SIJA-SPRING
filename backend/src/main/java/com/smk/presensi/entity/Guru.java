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
}
