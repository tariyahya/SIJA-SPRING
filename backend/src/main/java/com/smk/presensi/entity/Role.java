package com.smk.presensi.entity;

import jakarta.persistence.*;

/**
 * Entity Role - Mewakili peran/jabatan user di sistem.
 * 
 * Setiap user bisa punya 1 atau lebih roles.
 * Role menentukan permission/akses yang dimiliki user.
 * 
 * Contoh roles:
 * - ROLE_ADMIN: Full access (CRUD semua data)
 * - ROLE_GURU: Read all siswa, manage presensi
 * - ROLE_SISWA: Read own data, submit presensi
 * 
 * Relasi:
 * - ManyToMany dengan User (1 role bisa dimiliki banyak user)
 * 
 * @Entity: Tandai sebagai JPA entity (akan jadi tabel 'roles' di database)
 * @Table: Nama tabel di database
 */
@Entity
@Table(name = "roles")
public class Role {
    
    /**
     * Primary key - Auto increment.
     * 
     * @Id: Tandai sebagai primary key
     * @GeneratedValue: Auto increment (strategy IDENTITY)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nama role (enum).
     * 
     * Pakai enum untuk restrict nilai yang allowed.
     * Tidak bisa insert role sembarangan.
     * 
     * @Enumerated(STRING): Simpan nama enum di database (bukan ordinal/angka)
     * @Column: Wajib diisi (nullable=false), unique (tidak boleh duplikat)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private RoleName name;
    
    /**
     * Enum untuk role names.
     * 
     * PENTING: Prefix "ROLE_" wajib untuk Spring Security!
     * Spring Security convention: role harus diawali "ROLE_"
     * 
     * Contoh:
     * - hasRole('ADMIN') → Check ROLE_ADMIN
     * - hasRole('GURU') → Check ROLE_GURU
     */
    public enum RoleName {
        /**
         * Administrator - Full access ke semua fitur.
         * Bisa CRUD semua data (siswa, guru, presensi, users).
         */
        ROLE_ADMIN,
        
        /**
         * Guru - Manage presensi dan lihat data siswa.
         * Bisa read all siswa, input presensi, lihat laporan.
         * Tidak bisa hapus data master (siswa, guru).
         */
        ROLE_GURU,
        
        /**
         * Siswa - Lihat data sendiri dan submit presensi.
         * Hanya bisa read own data, submit own presensi.
         * Tidak bisa lihat data siswa lain.
         */
        ROLE_SISWA,

        /**
         * Guru Piket - bertugas memantau kehadiran harian
         * dan melakukan approval/penolakan pengajuan izin.
         */
        ROLE_GURU_PIKET,

        /**
         * Guru Pembimbing PKL - membina siswa PKL dan
         * memonitor presensi serta laporan PKL.
         */
        ROLE_GURU_PEMBIMBING,

        /**
         * Guru BK (Bimbingan Konseling) - mengelola
         * kasus kedisiplinan dan surat peringatan.
         */
        ROLE_GURU_BK,

        /**
         * Wakil Kepala Kurikulum - akses laporan global,
         * rekap, dan pengaturan kebijakan presensi.
         */
        ROLE_WAKAKURIKULUM,

        /**
         * Wakil Kepala Hubungan Industri - fokus pada
         * pengelolaan PKL dan hubungan dengan DUDI.
         */
        ROLE_WAKAHUBIN,

        /**
         * Ketua Program Keahlian/Jurusan - melihat rekap
         * kehadiran dan laporan untuk jurusan tertentu.
         */
        ROLE_KAPROG
    }
    
    // ==========================================
    // Constructors
    // ==========================================
    
    /**
     * Default constructor (diperlukan JPA).
     * JPA butuh no-args constructor untuk create instance via reflection.
     */
    public Role() {
    }
    
    /**
     * Constructor dengan role name.
     * 
     * Untuk kemudahan saat create role baru:
     * Role adminRole = new Role(RoleName.ROLE_ADMIN);
     * 
     * @param name Role name (enum)
     */
    public Role(RoleName name) {
        this.name = name;
    }
    
    // ==========================================
    // Getters & Setters
    // ==========================================
    
    /**
     * Get ID role.
     * 
     * @return ID role (auto increment dari database)
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Set ID role.
     * 
     * BIASANYA TIDAK PERLU DIPANGGIL!
     * ID di-set otomatis oleh database (auto increment).
     * 
     * @param id ID role
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Get role name.
     * 
     * @return Role name (enum: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA)
     */
    public RoleName getName() {
        return name;
    }
    
    /**
     * Set role name.
     * 
     * @param name Role name (enum)
     */
    public void setName(RoleName name) {
        this.name = name;
    }
    
    // ==========================================
    // toString, equals, hashCode
    // ==========================================
    
    /**
     * Override toString untuk debugging.
     * 
     * Contoh output: "Role{id=1, name=ROLE_ADMIN}"
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name=" + name +
                '}';
    }
    
    /**
     * Override equals untuk compare roles.
     * 
     * 2 roles dianggap sama jika ID sama.
     * 
     * @param o Object untuk dibandingkan
     * @return true jika sama, false jika beda
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Role role = (Role) o;
        return id != null && id.equals(role.id);
    }
    
    /**
     * Override hashCode (wajib jika override equals).
     * 
     * @return Hash code berdasarkan ID
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
