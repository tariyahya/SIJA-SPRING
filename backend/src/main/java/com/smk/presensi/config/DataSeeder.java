package com.smk.presensi.config;

import com.smk.presensi.entity.Role;
import com.smk.presensi.entity.Role.RoleName;
import com.smk.presensi.entity.User;
import com.smk.presensi.repository.RoleRepository;
import com.smk.presensi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Seeder - Insert data default saat aplikasi start.
 * 
 * CommandLineRunner: Interface yang dijalankan otomatis saat aplikasi start.
 * Method run() akan dipanggil setelah ApplicationContext ready.
 * 
 * Yang di-seed:
 * 1. 3 Roles: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA
 * 2. 1 Admin user default (username: admin, password: admin123)
 * 
 * Data ini perlu untuk testing dan development.
 * Tanpa roles, user tidak bisa register (role not found error).
 * Tanpa admin, tidak ada yang bisa manage users.
 */
@Component
public class DataSeeder implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor injection.
     */
    public DataSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Method yang dijalankan saat aplikasi start.
     * 
     * Flow:
     * 1. Seed roles (ROLE_ADMIN, ROLE_GURU, ROLE_SISWA)
     * 2. Seed admin user default
     * 
     * @param args Command line arguments (tidak dipakai)
     */
    @Override
    public void run(String... args) {
        logger.info("Starting data seeding...");
        
        // 1. Seed roles
        seedRoles();
        
        // 2. Seed admin user
        seedAdminUser();
        
        logger.info("Data seeding completed!");
    }
    
    /**
     * Seed roles ke database.
     * 
     * Create 3 roles jika belum ada:
     * - ROLE_ADMIN: Full access
     * - ROLE_GURU: Read all, manage presensi
     * - ROLE_SISWA: Read own data, submit presensi
     */
    private void seedRoles() {
        // Cek apakah roles sudah ada
        if (roleRepository.count() > 0) {
            logger.info("Roles already exist, skipping role seeding");
            return;
        }
        
        logger.info("Seeding roles...");
        
        // Create 3 roles
        Role adminRole = new Role(RoleName.ROLE_ADMIN);
        Role guruRole = new Role(RoleName.ROLE_GURU);
        Role siswaRole = new Role(RoleName.ROLE_SISWA);
        
        // Save ke database
        roleRepository.save(adminRole);
        roleRepository.save(guruRole);
        roleRepository.save(siswaRole);
        
        logger.info("Roles seeded: ROLE_ADMIN, ROLE_GURU, ROLE_SISWA");
    }
    
    /**
     * Seed admin user default ke database.
     * 
     * Create admin user dengan:
     * - Username: admin
     * - Password: admin123 (hashed)
     * - Role: ROLE_ADMIN
     * 
     * PENTING: Ganti password ini di production!
     */
    private void seedAdminUser() {
        // Cek apakah admin user sudah ada
        if (userRepository.existsByUsername("admin")) {
            logger.info("Admin user already exists, skipping admin seeding");
            return;
        }
        
        logger.info("Seeding admin user...");
        
        // Get ROLE_ADMIN
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found!"));
        
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@smk.sch.id");
        
        // Hash password dengan BCrypt
        String hashedPassword = passwordEncoder.encode("admin123");
        admin.setPassword(hashedPassword);
        
        // Add role
        admin.addRole(adminRole);
        
        // Save ke database
        userRepository.save(admin);
        
        logger.info("Admin user seeded: username=admin, password=admin123");
        logger.warn("⚠️  CHANGE ADMIN PASSWORD IN PRODUCTION!");
    }
}
