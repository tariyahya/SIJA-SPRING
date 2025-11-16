package com.smk.presensi.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity User - Mewakili user yang bisa login ke sistem.
 * 
 * User adalah akun untuk authentication.
 * Setiap user punya username, password, dan roles.
 * 
 * Relasi:
 * - ManyToMany dengan Role (1 user bisa punya banyak roles)
 * - OneToOne dengan Siswa (opsional - tidak semua siswa punya akun)
 * - OneToOne dengan Guru (opsional - tidak semua guru punya akun)
 * 
 * Flow penggunaan:
 * 1. User register/dibuat → username, password (hashed), roles assigned
 * 2. User login → Verify password → Generate JWT token
 * 3. User akses endpoint → Validate JWT → Check roles → Allow/deny
 * 
 * @Entity: Tandai sebagai JPA entity (akan jadi tabel 'users' di database)
 * @Table: Nama tabel + unique constraint
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        /**
         * Username harus unique.
         * Tidak boleh ada 2 users dengan username sama.
         * Database akan throw error jika coba insert username duplikat.
         */
        @UniqueConstraint(columnNames = "username")
})
public class User {
    
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
     * Username untuk login.
     * 
     * Harus unique (1 username = 1 user).
     * Case-sensitive (admin ≠ Admin ≠ ADMIN).
     * 
     * Validation:
     * - Wajib diisi (nullable=false)
     * - Unique (lihat @UniqueConstraint di @Table)
     * - Max 20 karakter
     * 
     * @Column: Konfigurasi kolom di database
     */
    @Column(nullable = false, unique = true, length = 20)
    private String username;
    
    /**
     * Password (HASHED dengan BCrypt).
     * 
     * PENTING: JANGAN PERNAH simpan password plain text!
     * Password harus di-hash dengan BCrypt sebelum disimpan.
     * 
     * Format BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye...
     * - $2a$: Algoritma version (BCrypt)
     * - 10: Cost factor (2^10 = 1024 rounds)
     * - N9q...: Salt (random)
     * - Sisa: Hash result
     * 
     * Kenapa 100 karakter?
     * BCrypt hash panjangnya ~60 karakter.
     * 100 karakter untuk antisipasi algoritma lain yang lebih panjang.
     * 
     * @Column: Wajib diisi, max 100 karakter
     */
    @Column(nullable = false, length = 100)
    private String password;
    
    /**
     * Email user (opsional).
     * 
     * Dipakai untuk:
     * - Forgot password (kirim reset link)
     * - Notifikasi (presensi reminder)
     * - Komunikasi
     * 
     * @Column: Max 50 karakter
     */
    @Column(length = 50)
    private String email;
    
    /**
     * Status enabled (aktif/nonaktif).
     * 
     * true = User bisa login
     * false = User tidak bisa login (disabled/suspended)
     * 
     * Use case:
     * - Suspend user yang melanggar aturan
     * - Nonaktifkan user yang sudah lulus/resign
     * 
     * @Column: Default true (user aktif saat dibuat)
     */
    @Column(nullable = false)
    private boolean enabled = true;
    
    /**
     * Roles user (ManyToMany relation).
     * 
     * 1 user bisa punya banyak roles.
     * Contoh: User admin bisa punya ROLE_ADMIN dan ROLE_GURU.
     * 
     * @ManyToMany:
     * - Relasi many-to-many (1 user → many roles, 1 role → many users)
     * - EAGER fetch: Load roles bersamaan dengan user (tidak lazy)
     *   Kenapa EAGER? Karena selalu butuh roles untuk authorization check.
     * 
     * @JoinTable:
     * - Create intermediate table: user_roles
     * - Columns: user_id (FK ke users), role_id (FK ke roles)
     * - JoinColumn: Column di intermediate table yang point ke User
     * - inverseJoinColumns: Column di intermediate table yang point ke Role
     * 
     * Database structure:
     * 
     * Table: users
     * | id | username | password | email | enabled |
     * |----|----------|----------|-------|---------|
     * | 1  | admin    | $2a$10.. | ...   | true    |
     * 
     * Table: roles
     * | id | name       |
     * |----|------------|
     * | 1  | ROLE_ADMIN |
     * | 2  | ROLE_GURU  |
     * 
     * Table: user_roles (join table)
     * | user_id | role_id |
     * |---------|---------|
     * | 1       | 1       |  ← admin punya ROLE_ADMIN
     * | 1       | 2       |  ← admin juga punya ROLE_GURU
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // ==========================================
    // Constructors
    // ==========================================
    
    /**
     * Default constructor (diperlukan JPA).
     * JPA butuh no-args constructor untuk create instance via reflection.
     */
    public User() {
    }
    
    /**
     * Constructor dengan username, email, password.
     * 
     * Untuk kemudahan saat create user baru:
     * User user = new User("admin", "admin@smk.sch.id", hashedPassword);
     * 
     * PENTING: Password harus sudah di-hash sebelum dipassing!
     * 
     * @param username Username untuk login
     * @param email Email user
     * @param password Password (HASHED dengan BCrypt)
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // ==========================================
    // Getters & Setters
    // ==========================================
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    // ==========================================
    // Helper Methods
    // ==========================================
    
    /**
     * Add role ke user.
     * 
     * Convenience method untuk add role tanpa perlu access roles Set directly.
     * 
     * Contoh penggunaan:
     * User user = new User();
     * Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN);
     * user.addRole(adminRole);
     * 
     * @param role Role yang mau ditambahkan
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    /**
     * Remove role dari user.
     * 
     * Contoh penggunaan:
     * User user = userRepository.findById(1L);
     * Role guruRole = roleRepository.findByName(RoleName.ROLE_GURU);
     * user.removeRole(guruRole);
     * 
     * @param role Role yang mau dihapus
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    // ==========================================
    // toString, equals, hashCode
    // ==========================================
    
    /**
     * Override toString untuk debugging.
     * 
     * PENTING: Jangan include password di toString (security risk)!
     * 
     * Contoh output: "User{id=1, username='admin', email='admin@smk.sch.id', enabled=true}"
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                '}';
    }
    
    /**
     * Override equals untuk compare users.
     * 
     * 2 users dianggap sama jika ID sama.
     * 
     * @param o Object untuk dibandingkan
     * @return true jika sama, false jika beda
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        User user = (User) o;
        return id != null && id.equals(user.id);
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
