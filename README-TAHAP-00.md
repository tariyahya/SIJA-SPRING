# Tahap 00: Persiapan Environment Development

## Status: ✅ COMPLETED

Branch ini berisi dokumentasi lengkap untuk persiapan environment development sebelum memulai coding.

## 📚 Materi Pembelajaran

### 1. TASK-0.md
Panduan lengkap instalasi dan konfigurasi tools:
- JDK 17/21 (Java Development Kit)
- Maven (Build Tool)
- IDE (IntelliJ IDEA atau VS Code)
- Git & GitHub
- Postman (API Testing)

### 2. blog0.md
Penjelasan konsep dan mindset:
- Mengapa perlu persiapan tools
- Fungsi masing-masing tools
- Mindset programmer yang benar
- Tips sukses belajar programming

## 🎯 Tujuan Pembelajaran

Setelah menyelesaikan tahap ini, siswa mampu:
- ✅ Menginstall semua tools yang diperlukan
- ✅ Memahami fungsi masing-masing tools
- ✅ Mengkonfigurasi environment dengan benar
- ✅ Membuat GitHub account dan setup SSH key
- ✅ Melakukan operasi Git dasar (clone, add, commit, push)
- ✅ Menjalankan aplikasi Spring Boot sederhana

## 📋 Checklist Keberhasilan

### Installation
- [ ] JDK 17 atau 21 terinstall
- [ ] Maven terinstall
- [ ] Git terinstall
- [ ] IDE (IntelliJ IDEA atau VS Code) terinstall
- [ ] Postman atau Thunder Client terinstall

### Configuration
- [ ] JAVA_HOME dan PATH sudah di-set
- [ ] MAVEN_HOME dan PATH sudah di-set
- [ ] Git config (user.name dan user.email) sudah di-set
- [ ] GitHub account sudah dibuat
- [ ] SSH key sudah di-setup dan ditambahkan ke GitHub

### Verification
- [ ] `java -version` menampilkan JDK 17 atau 21
- [ ] `mvn -version` menampilkan Maven 3.9.x
- [ ] `git --version` menampilkan Git 2.x.x
- [ ] `ssh -T git@github.com` berhasil authenticate
- [ ] IDE bisa membuka project Spring Boot
- [ ] Aplikasi Spring Boot bisa di-run dari IDE

### Git Skills
- [ ] Paham command: `git status`, `git add`, `git commit`
- [ ] Paham command: `git push`, `git pull`
- [ ] Paham command: `git branch`, `git checkout`
- [ ] Bisa clone repository dari GitHub
- [ ] Bisa push perubahan ke GitHub

## 🚀 Cara Menggunakan Materi Ini

### Untuk Siswa:

1. **Baca blog0.md dulu** untuk memahami konsep dan mindset
2. **Ikuti TASK-0.md** step-by-step untuk instalasi tools
3. **Verifikasi setiap langkah** dengan menjalankan command yang ada
4. **Centang checklist** setelah berhasil menyelesaikan setiap item
5. **Tanya guru/teman** jika mengalami kesulitan

### Untuk Guru:

1. **Review materi** sebelum mengajar
2. **Siapkan troubleshooting** untuk issue umum (lihat TASK-0.md bagian Troubleshooting)
3. **Dampingi siswa** saat instalasi (terutama untuk setting PATH dan environment variables)
4. **Verifikasi checklist** setiap siswa sebelum lanjut ke Tahap 01

## 🔧 Tools yang Diperlukan

| Tool | Version | Purpose | Required |
|------|---------|---------|----------|
| JDK | 17 atau 21 LTS | Compile & run Java | ✅ Wajib |
| Maven | 3.9.x | Build automation | ✅ Wajib |
| Git | 2.x | Version control | ✅ Wajib |
| IntelliJ IDEA CE | Latest | IDE (recommended) | ✅ Wajib (pilih 1) |
| VS Code | Latest | IDE (alternative) | ✅ Wajib (pilih 1) |
| Postman | Latest | API testing | ✅ Wajib |
| MySQL | 8.x | Database (production) | ⏱️ Nanti (Tahap 10) |

## 📖 Resources Tambahan

### Official Documentation
- [Java Documentation](https://docs.oracle.com/en/java/javase/17/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [Git Documentation](https://git-scm.com/doc)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

### Video Tutorial (Bahasa Indonesia)
- Programmer Zaman Now - Spring Boot Playlist
- Web Programming UNPAS - Git & GitHub
- Kelas Terbuka - Java OOP

### Cheat Sheets
- [Git Cheat Sheet](https://education.github.com/git-cheat-sheet-education.pdf)
- [Maven Quick Reference](https://maven.apache.org/guides/MavenQuickReferenceCard.pdf)

## ⚠️ Common Issues & Solutions

### Issue: `'java' is not recognized`
**Solution:** Set JAVA_HOME dan tambahkan %JAVA_HOME%\bin ke PATH

### Issue: `'mvn' is not recognized`
**Solution:** Set MAVEN_HOME dan tambahkan %MAVEN_HOME%\bin ke PATH

### Issue: Git "Permission denied (publickey)"
**Solution:** Generate SSH key dan tambahkan ke GitHub

### Issue: Port 8080 already in use
**Solution:** Ganti port di application.properties → `server.port=8081`

**Detail lengkap:** Lihat bagian Troubleshooting di TASK-0.md

## 🎓 Learning Outcomes

Setelah menyelesaikan Tahap 00, siswa akan:

1. **Paham ecosystem Java development**
   - Apa itu JDK dan fungsinya
   - Apa itu Maven dan kenapa diperlukan
   - Bagaimana Java compile & runtime bekerja

2. **Terbiasa dengan IDE**
   - Navigasi project structure
   - Auto-completion & error detection
   - Running dan debugging aplikasi

3. **Paham version control dengan Git**
   - Konsep repository, commit, push, pull
   - Workflow Git dasar
   - Collaboration dengan GitHub

4. **Siap untuk development**
   - Environment sudah ter-setup dengan benar
   - Tidak akan stuck karena masalah tools
   - Bisa fokus pada learning Spring Boot

## 📊 Estimasi Waktu

- **Baca blog0.md:** 30-45 menit
- **Instalasi tools:** 1-2 jam (tergantung koneksi internet)
- **Konfigurasi & verifikasi:** 30 menit
- **Explorasi IDE & Git:** 1 jam

**Total:** 3-4 jam untuk penyelesaian tahap ini dengan pemahaman yang baik.

## ➡️ Next Steps

Setelah **semua checklist ✅**, Anda siap untuk:

1. **Tahap 01: Backend Skeleton**
   - Membuat project Spring Boot dari scratch
   - Setup struktur folder
   - Membuat endpoint hello world

Branch: `tahap-01-backend-skeleton`

## 💡 Tips Penting

1. **Jangan skip tahap ini!** Fondasi yang kuat akan membuat tahap selanjutnya lebih mudah.
2. **Verifikasi setiap langkah** dengan command yang ada di TASK-0.md
3. **Catat password** yang Anda buat (MySQL root password, SSH passphrase)
4. **Backup SSH key** (copy folder ~/.ssh ke tempat aman)
5. **Biasakan commit sering** sejak tahap ini

## 🤝 Bantuan & Support

Jika mengalami kesulitan:
1. Baca error message dengan teliti
2. Cek bagian Troubleshooting di TASK-0.md
3. Google error message
4. Tanya di grup kelas
5. Konsultasi dengan guru

**Remember:** Tidak ada pertanyaan bodoh. Setiap programmer pernah stuck di setup environment!

---

**Last Updated:** November 2025  
**Maintainer:** SIJA Spring Boot Training Team  
**Status:** Ready for classroom use

**Happy Learning! 🚀**
