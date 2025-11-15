# TASK 0: Persiapan Tools & Lingkungan Development

## Tujuan Pembelajaran
Setelah menyelesaikan tahap ini, siswa mampu:
- Memahami tools yang diperlukan untuk development Spring Boot
- Menginstall dan mengkonfigurasi JDK, IDE, dan tools pendukung
- Memahami konsep Version Control dengan Git & GitHub
- Membuat repository dan melakukan operasi dasar Git
- Mempersiapkan environment untuk development backend, mobile, dan desktop

---

## A. Tools yang Diperlukan

### 1. **Java Development Kit (JDK) 17 atau 21**

#### Apa itu JDK?
JDK (Java Development Kit) adalah paket software yang berisi compiler, runtime, dan tools untuk mengembangkan aplikasi Java. Tanpa JDK, kita tidak bisa compile dan run program Java.

#### Kenapa JDK 17 atau 21?
- **JDK 17**: Long Term Support (LTS) version, stabil, didukung hingga 2029
- **JDK 21**: LTS terbaru (September 2023), fitur modern, performa lebih baik
- Spring Boot 3.x minimal memerlukan JDK 17

#### Download JDK:
**Pilihan 1: Oracle JDK**
- URL: https://www.oracle.com/java/technologies/downloads/
- Pilih: Java 17 atau Java 21
- OS: Windows x64 Installer (.exe)

**Pilihan 2: Eclipse Temurin (Recommended untuk pembelajaran)**
- URL: https://adoptium.net/
- Pilih: Temurin 17 (LTS) atau Temurin 21 (LTS)
- OS: Windows x64 (.msi installer)
- Keuntungan: Gratis, open source, tidak perlu akun Oracle

#### Instalasi JDK:
1. Download installer sesuai OS (Windows .msi atau .exe)
2. Jalankan installer
3. Ikuti wizard instalasi (Next → Next → Install)
4. **PENTING**: Centang option "Set JAVA_HOME variable" jika ada
5. Klik Finish

#### Verifikasi Instalasi:
Buka **PowerShell** atau **Command Prompt**, ketik:

```powershell
java -version
```

Output yang diharapkan:
```
openjdk version "17.0.9" 2023-10-17 LTS
OpenJDK Runtime Environment Temurin-17.0.9+9 (build 17.0.9+9-LTS)
OpenJDK 64-Bit Server VM Temurin-17.0.9+9 (build 17.0.9+9-LTS, mixed mode, sharing)
```

Jika muncul error `'java' is not recognized`, berarti JDK belum masuk PATH. Ikuti langkah setting JAVA_HOME di bawah.

#### Setting JAVA_HOME (Manual jika belum otomatis):

**Windows:**
1. Klik kanan **This PC** → Properties → Advanced system settings
2. Klik **Environment Variables**
3. Di **System variables**, klik **New**
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot` (sesuaikan dengan path instalasi JDK Anda)
4. Edit variable **Path**, klik **New**, tambahkan: `%JAVA_HOME%\bin`
5. Klik OK semua dialog
6. **Restart PowerShell/Command Prompt**
7. Test lagi: `java -version`

---

### 2. **Maven (Build Tool)**

#### Apa itu Maven?
Maven adalah build automation tool untuk Java. Fungsinya:
- Mengelola dependencies (library yang dibutuhkan project)
- Compile kode Java
- Running tests
- Packaging aplikasi (JAR/WAR)

#### Kenapa Maven?
- Spring Boot secara default menggunakan Maven atau Gradle
- Maven lebih mudah untuk pemula (XML configuration)
- Ecosystem Java mayoritas pakai Maven

#### Download Maven:
- URL: https://maven.apache.org/download.cgi
- Pilih: **Binary zip archive** (apache-maven-3.9.6-bin.zip atau versi terbaru)

#### Instalasi Maven:

**Windows:**
1. Extract file zip ke folder tanpa spasi, contoh: `C:\Program Files\apache-maven-3.9.6`
2. Setting Environment Variable:
   - Buka **Environment Variables** (seperti setting JAVA_HOME)
   - **System variables** → **New**:
     - Variable name: `MAVEN_HOME`
     - Variable value: `C:\Program Files\apache-maven-3.9.6`
   - Edit variable **Path**, tambahkan: `%MAVEN_HOME%\bin`
3. Restart terminal
4. Test: `mvn -version`

#### Verifikasi Maven:
```powershell
mvn -version
```

Output yang diharapkan:
```
Apache Maven 3.9.6 (...)
Maven home: C:\Program Files\apache-maven-3.9.6
Java version: 17.0.9, vendor: Eclipse Adoptium
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

**PENTING**: Maven harus detect JDK yang benar. Jika Java version salah, periksa JAVA_HOME.

---

### 3. **IDE (Integrated Development Environment)**

#### Pilihan IDE untuk Spring Boot:

**A. IntelliJ IDEA (Recommended)**

**Kenapa IntelliJ IDEA?**
- IDE terbaik untuk Spring Boot (built-in support)
- Auto-completion sangat cerdas
- Refactoring tools powerful
- Debugging mudah
- Community Edition (gratis) sudah cukup untuk pembelajaran

**Download:**
- URL: https://www.jetbrains.com/idea/download/
- Pilih: **IntelliJ IDEA Community Edition** (FREE)
- OS: Windows (.exe)

**Instalasi:**
1. Download installer
2. Jalankan, ikuti wizard
3. Pilih options:
   - ✅ Create Desktop Shortcut
   - ✅ Update PATH variable
   - ✅ Create Associations (.java, .xml, .properties)
4. Klik Install → Finish

**First Run Setup:**
1. Buka IntelliJ IDEA
2. Skip import settings (jika first time)
3. Pilih tema (Darcula atau Light)
4. Install plugin yang direkomendasikan
5. Klik **Start using IntelliJ IDEA**

---

**B. Visual Studio Code (Alternatif Ringan)**

**Kenapa VS Code?**
- Ringan dan cepat
- Extensible (banyak extension)
- Gratis dan open source
- Cocok untuk berbagai bahasa (Java, JavaScript, Python, dll)

**Download:**
- URL: https://code.visualstudio.com/
- Pilih: Windows (.exe installer)

**Instalasi:**
1. Download installer
2. Jalankan, centang semua options (Add to PATH, Create desktop icon, dll)
3. Klik Install

**Extension yang Diperlukan untuk Java:**
Setelah install VS Code, install extension berikut (Ctrl+Shift+X):
1. **Extension Pack for Java** (by Microsoft) - wajib
2. **Spring Boot Extension Pack** (by VMware) - wajib
3. **Lombok Annotations Support** (by Gabriel Basilio Brito)
4. **Material Icon Theme** (opsional, untuk icon file lebih cantik)

**Cara install extension:**
1. Buka VS Code
2. Tekan `Ctrl+Shift+X` (buka Extensions panel)
3. Search "Extension Pack for Java"
4. Klik **Install**
5. Ulangi untuk extension lainnya

---

### 4. **Git (Version Control System)**

#### Apa itu Git?
Git adalah version control system untuk melacak perubahan kode. Dengan Git, kita bisa:
- Menyimpan history semua perubahan kode
- Kembali ke versi sebelumnya jika ada bug
- Kolaborasi dengan tim (multiple developers)
- Branching untuk fitur baru tanpa merusak kode utama

#### Kenapa Git Penting?
- Industry standard untuk version control
- Semua perusahaan software pakai Git
- Membiasakan best practices development sejak awal

#### Download Git:
- URL: https://git-scm.com/download/win
- Download: Git for Windows (latest version)

#### Instalasi Git:

**Windows:**
1. Download installer (.exe)
2. Jalankan installer
3. **Penting: Pilihan saat instalasi:**
   - Editor: Pilih **Visual Studio Code** atau **Notepad++** (jangan pakai Vim jika tidak terbiasa)
   - PATH environment: Pilih **Git from the command line and also from 3rd-party software**
   - HTTPS transport backend: Pilih **Use the OpenSSL library**
   - Line ending conversions: Pilih **Checkout Windows-style, commit Unix-style**
   - Terminal emulator: Pilih **Use Windows' default console window**
   - Git Credential Manager: **Enable**
4. Klik Install → Finish

#### Verifikasi Git:
```powershell
git --version
```

Output yang diharapkan:
```
git version 2.43.0.windows.1
```

#### Konfigurasi Git (First Time Setup):

Setelah install, konfigurasi nama dan email (untuk commit author):

```powershell
git config --global user.name "Nama Anda"
git config --global user.email "email@anda.com"
```

Contoh:
```powershell
git config --global user.name "Budi Santoso"
git config --global user.email "budi.santoso@smkn1.sch.id"
```

**Verifikasi konfigurasi:**
```powershell
git config --global --list
```

Output:
```
user.name=Budi Santoso
user.email=budi.santoso@smkn1.sch.id
```

---

### 5. **GitHub Account**

#### Apa itu GitHub?
GitHub adalah platform hosting untuk Git repository. Fungsinya:
- Menyimpan kode di cloud (backup otomatis)
- Kolaborasi dengan tim
- Portfolio project untuk CV
- Gratis untuk repository public

#### Cara Membuat Akun GitHub:

1. Buka: https://github.com/signup
2. Masukkan email (gunakan email aktif)
3. Buat password yang kuat
4. Pilih username (contoh: budisantoso123)
5. Solve puzzle (verifikasi bukan robot)
6. Klik **Create account**
7. Verifikasi email (buka email, klik link verifikasi)
8. Pilih **Free plan** (gratis selamanya)

#### Setup SSH Key (Recommended untuk keamanan):

**Kenapa SSH Key?**
- Tidak perlu input password setiap kali push/pull
- Lebih aman daripada HTTPS password
- Best practice untuk development

**Cara Generate SSH Key:**

**Windows (PowerShell):**
```powershell
ssh-keygen -t ed25519 -C "email@anda.com"
```

Tekan **Enter** 3x (pakai default location dan no passphrase untuk kemudahan).

**Copy Public Key:**
```powershell
cat ~/.ssh/id_ed25519.pub
```

Copy output-nya (mulai dari `ssh-ed25519 ...` sampai email).

**Tambahkan ke GitHub:**
1. Login GitHub
2. Klik foto profil (kanan atas) → **Settings**
3. Klik **SSH and GPG keys** (sidebar kiri)
4. Klik **New SSH key**
5. Title: `Laptop SIJA` (atau nama komputer Anda)
6. Key: Paste public key yang tadi dicopy
7. Klik **Add SSH key**

**Test Koneksi:**
```powershell
ssh -T git@github.com
```

Ketik `yes` jika ada prompt, output yang diharapkan:
```
Hi username! You've successfully authenticated, but GitHub does not provide shell access.
```

---

### 6. **Postman (API Testing Tool)**

#### Apa itu Postman?
Postman adalah tools untuk testing REST API. Fungsinya:
- Kirim HTTP request (GET, POST, PUT, DELETE)
- Lihat response dari server
- Save request untuk testing berulang
- Export/import collection

#### Download Postman:
- URL: https://www.postman.com/downloads/
- Pilih: Windows 64-bit

#### Instalasi:
1. Download installer
2. Jalankan installer (otomatis install, tidak perlu klik apa-apa)
3. Postman akan otomatis buka setelah install
4. Sign up (buat akun gratis) atau pilih **Skip and go to the app**

#### Alternatif Postman (jika terlalu berat):

**Thunder Client (VS Code Extension)**
- Lebih ringan, langsung di VS Code
- Install: Search "Thunder Client" di VS Code Extensions
- Cara pakai mirip Postman tapi langsung di sidebar VS Code

---

### 7. **Database Tools (Opsional untuk Tahap Awal)**

Untuk tahap 02-04, kita pakai **H2 Database** (in-memory, tidak perlu install).

Tapi untuk production nanti (Tahap 10), kita akan pakai **MySQL** atau **PostgreSQL**.

#### Install MySQL (Opsional, untuk Tahap 10 nanti):

**Download:**
- URL: https://dev.mysql.com/downloads/installer/
- Pilih: **MySQL Installer for Windows** (mysql-installer-web-community)

**Instalasi:**
1. Download installer
2. Jalankan, pilih **Developer Default**
3. Klik **Execute** (akan download semua components)
4. MySQL Server Configuration:
   - Type: **Development Computer**
   - Port: **3306** (default)
   - Root Password: Buat password yang mudah diingat (contoh: `admin123`)
5. Klik **Execute** → **Finish**

**Verifikasi MySQL:**
```powershell
mysql -u root -p
```
Masukkan password, jika berhasil masuk ke MySQL shell.

---

## B. Persiapan Project Repository

### 1. Clone Repository Template

Setelah semua tools terinstall, clone repository template presensi:

```powershell
# Pindah ke folder Documents
cd ~/Documents

# Clone repository (ganti username dengan GitHub username Anda)
git clone https://github.com/username/presensi-siswa-guru.git

# Masuk ke folder project
cd presensi-siswa-guru

# Cek branch yang tersedia
git branch -a
```

### 2. Membuat Repository Sendiri dari Template

Jika guru sudah menyediakan template repository:

1. Buka repository template di GitHub
2. Klik **Use this template** (tombol hijau)
3. Nama repository: `presensi-sija-nama-anda` (ganti dengan nama Anda)
4. Pilih **Public** atau **Private** (sesuai arahan guru)
5. Klik **Create repository**
6. Clone repository yang baru dibuat:

```powershell
git clone https://github.com/username/presensi-sija-nama-anda.git
cd presensi-sija-nama-anda
```

---

## C. Verifikasi Semua Tools

### Checklist Installation:

Jalankan command berikut satu per satu di **PowerShell**:

```powershell
# 1. Java
java -version
# Output: openjdk version "17.0.x" atau "21.0.x"

# 2. Maven
mvn -version
# Output: Apache Maven 3.9.x

# 3. Git
git --version
# Output: git version 2.x.x

# 4. Git Config
git config --global user.name
git config --global user.email
# Output: Nama dan email Anda

# 5. GitHub SSH (opsional tapi recommended)
ssh -T git@github.com
# Output: Hi username! You've successfully authenticated...
```

**Jika semua command di atas berhasil**, maka environment Anda sudah siap! ✅

---

## D. Persiapan IDE (IntelliJ IDEA)

### Import Project Spring Boot:

1. Buka IntelliJ IDEA
2. Klik **Open** (atau File → Open)
3. Navigate ke folder project yang sudah di-clone
4. Pilih folder `backend/`
5. Klik **OK**
6. IntelliJ akan detect **Maven project** → Klik **Trust Project**
7. Tunggu Maven download dependencies (lihat progress bar di bawah)
   - Proses download pertama kali bisa 5-10 menit (tergantung koneksi internet)
8. Setelah selesai, akan muncul struktur project di sidebar

### Konfigurasi JDK di IntelliJ:

1. Klik **File → Project Structure** (Ctrl+Alt+Shift+S)
2. Tab **Project**:
   - SDK: Pilih JDK 17 atau 21 yang sudah diinstall
   - Language level: 17 atau 21 (sesuai JDK)
3. Tab **Modules**:
   - Pastikan `backend` terpilih
   - Language level: Same as project
4. Klik **Apply** → **OK**

### Test Run Aplikasi:

1. Buka file `PresensiApplication.java` (di `src/main/java/com/smk/presensi/`)
2. Klik kanan pada file → **Run 'PresensiApplication'** (atau tekan Shift+F10)
3. Lihat console, tunggu hingga muncul:
   ```
   Tomcat started on port 8081 (http)
   Started PresensiApplication in X seconds
   ```
4. Buka browser, akses: http://localhost:8081/api/hello
5. Jika muncul response JSON, berarti berhasil! ✅

---

## E. Persiapan IDE (Visual Studio Code)

### Open Project:

1. Buka VS Code
2. Klik **File → Open Folder**
3. Pilih folder `backend/`
4. Klik **Select Folder**
5. VS Code akan detect Java project
6. Tunggu hingga **Java Language Server** selesai loading (lihat status bar bawah)

### Konfigurasi JDK di VS Code:

1. Tekan **Ctrl+Shift+P** (Command Palette)
2. Ketik: `Java: Configure Java Runtime`
3. Pilih JDK 17 atau 21 untuk **JavaSE-17** atau **JavaSE-21**
4. Klik **Apply**

### Test Run Aplikasi:

1. Buka file `PresensiApplication.java`
2. Klik **Run** (icon ▶️ di atas method `main`)
3. Atau tekan **F5** (Debug mode)
4. Lihat **Terminal** di bawah, tunggu hingga app start
5. Buka browser: http://localhost:8081/api/hello
6. Jika muncul response JSON, berarti berhasil! ✅

---

## F. Pengenalan Git Command Dasar

### Workflow Git Sederhana:

```
Working Directory → Staging Area → Local Repository → Remote Repository
       |                |               |                    |
   (edit file)      (git add)      (git commit)        (git push)
```

### Command Git yang Sering Dipakai:

```powershell
# 1. Cek status perubahan file
git status

# 2. Tambah file ke staging area
git add namafile.java          # Tambah 1 file
git add .                      # Tambah semua file yang berubah

# 3. Commit perubahan (simpan snapshot)
git commit -m "pesan commit yang jelas"

# 4. Push ke GitHub (upload ke cloud)
git push origin nama-branch

# 5. Pull dari GitHub (download perubahan terbaru)
git pull origin nama-branch

# 6. Cek history commit
git log --oneline

# 7. Buat branch baru
git checkout -b nama-branch-baru

# 8. Pindah branch
git checkout nama-branch

# 9. Lihat daftar branch
git branch -a

# 10. Merge branch
git checkout main              # Pindah ke branch target
git merge nama-branch          # Merge branch lain ke branch saat ini
```

### Contoh Workflow Harian:

```powershell
# Pagi: Update kode terbaru dari GitHub
git pull origin tahap-02-domain-crud

# Kerja: Edit kode, tambah fitur, fix bug
# ... edit SiswaController.java ...
# ... edit SiswaService.java ...

# Cek file mana yang berubah
git status

# Tambah file yang mau di-commit
git add src/main/java/com/smk/presensi/controller/SiswaController.java
git add src/main/java/com/smk/presensi/service/SiswaService.java

# Atau tambah semua file sekaligus
git add .

# Commit dengan pesan yang jelas
git commit -m "feat: add update and delete endpoints for Siswa"

# Push ke GitHub
git push origin tahap-02-domain-crud
```

### Best Practices Commit Message:

Format: `tipe: deskripsi singkat`

**Tipe commit:**
- `feat:` – Fitur baru (feature)
- `fix:` – Perbaikan bug
- `docs:` – Perubahan dokumentasi
- `style:` – Format kode (spasi, indent, dll)
- `refactor:` – Refactoring kode (tidak ubah fungsionalitas)
- `test:` – Tambah atau perbaiki test
- `chore:` – Maintenance (update dependency, dll)

**Contoh commit message yang baik:**
```
feat: add CRUD endpoints for Siswa entity
fix: resolve null pointer exception in findById
docs: add API documentation for Siswa endpoints
refactor: extract validation logic to separate method
```

**Contoh commit message yang BURUK (jangan ditiru):**
```
update
fix bug
asdasd
sudah selesai
```

---

## G. Troubleshooting Common Issues

### Issue 1: `'java' is not recognized`

**Penyebab:** JAVA_HOME atau PATH tidak diset dengan benar.

**Solusi:**
1. Cek apakah JDK terinstall: `dir "C:\Program Files\Eclipse Adoptium\"` atau `dir "C:\Program Files\Java\"`
2. Set JAVA_HOME manual (lihat bagian JDK Installation)
3. Restart terminal
4. Test: `java -version`

### Issue 2: `'mvn' is not recognized`

**Penyebab:** MAVEN_HOME atau PATH tidak diset.

**Solusi:**
1. Cek apakah Maven terinstall: `dir "C:\Program Files\apache-maven*"`
2. Set MAVEN_HOME manual (lihat bagian Maven Installation)
3. Restart terminal
4. Test: `mvn -version`

### Issue 3: Maven "Unable to access jarfile"

**Penyebab:** Dependencies belum terdownload atau Maven cache corrupt.

**Solusi:**
```powershell
cd backend
mvn clean install
```

Jika masih error, hapus cache Maven:
```powershell
rmdir -Recurse -Force ~/.m2/repository
mvn clean install
```

### Issue 4: IntelliJ "Cannot resolve symbol"

**Penyebab:** IntelliJ belum selesai indexing atau Maven belum sync.

**Solusi:**
1. Klik **Maven** tab (sidebar kanan)
2. Klik icon **Reload All Maven Projects** (icon ⟳)
3. Tunggu hingga selesai
4. Jika masih error: **File → Invalidate Caches → Invalidate and Restart**

### Issue 5: Port 8080 already in use

**Penyebab:** Ada aplikasi lain yang pakai port 8080 (misalnya Tomcat atau XAMPP).

**Solusi 1 (Ganti Port):**
Edit `backend/src/main/resources/application.properties`:
```properties
server.port=8081
```

**Solusi 2 (Matikan aplikasi yang pakai port 8080):**
```powershell
# Cek aplikasi yang pakai port 8080
netstat -ano | findstr :8080

# Matikan process (ganti <PID> dengan PID yang muncul)
taskkill /PID <PID> /F
```

### Issue 6: Git "Permission denied (publickey)"

**Penyebab:** SSH key belum di-setup atau belum ditambahkan ke GitHub.

**Solusi:**
1. Generate SSH key (lihat bagian GitHub Account)
2. Tambahkan public key ke GitHub
3. Test: `ssh -T git@github.com`

Atau pakai HTTPS instead of SSH:
```powershell
git remote set-url origin https://github.com/username/repo.git
```

---

## H. Checklist Keberhasilan Tahap 00

Centang ✅ jika sudah berhasil:

### Installation:
- [ ] JDK 17 atau 21 terinstall, `java -version` berhasil
- [ ] Maven terinstall, `mvn -version` berhasil
- [ ] Git terinstall, `git --version` berhasil
- [ ] Git config (user.name dan user.email) sudah di-set
- [ ] GitHub account sudah dibuat
- [ ] SSH key sudah di-setup (opsional tapi recommended)
- [ ] IDE (IntelliJ IDEA atau VS Code) terinstall
- [ ] Postman atau Thunder Client terinstall

### Git & GitHub:
- [ ] Repository template sudah di-clone atau create dari template
- [ ] Bisa push ke GitHub (test dengan edit README.md)
- [ ] Paham command: `git status`, `git add`, `git commit`, `git push`, `git pull`

### Spring Boot Project:
- [ ] Project backend bisa dibuka di IDE
- [ ] Maven dependencies berhasil didownload (tidak ada error merah)
- [ ] Aplikasi bisa di-run (Spring Boot start tanpa error)
- [ ] Bisa akses http://localhost:8081/api/hello di browser
- [ ] Response JSON muncul dengan benar

### Understanding:
- [ ] Paham apa itu JDK dan kenapa diperlukan
- [ ] Paham apa itu Maven dan fungsinya
- [ ] Paham konsep version control dengan Git
- [ ] Paham workflow Git dasar (add → commit → push)
- [ ] Paham struktur project Spring Boot (folder src, pom.xml, dll)

---

## I. Next Steps

Setelah semua checklist di atas ✅, Anda siap untuk:

1. **Tahap 01**: Membuat Backend Skeleton & Hello World Endpoint
2. **Tahap 02**: Membuat Domain Model (Entity Siswa, Guru, Kelas) & CRUD
3. Dan seterusnya...

Setiap tahap akan ada:
- **TASK-X.md**: Instruksi step-by-step
- **blogX.md**: Penjelasan konsep secara naratif
- **Branch tahap-0X**: Kode yang sudah jadi untuk referensi

**PENTING**: Jangan copy-paste kode dari branch! Ketik manual untuk muscle memory dan pemahaman.

---

## J. Resources & Links

### Official Documentation:
- Java: https://docs.oracle.com/en/java/javase/17/
- Spring Boot: https://spring.io/projects/spring-boot
- Maven: https://maven.apache.org/guides/
- Git: https://git-scm.com/doc

### Tutorial & Learning:
- Spring Boot Official Guides: https://spring.io/guides
- Git Tutorial: https://www.atlassian.com/git/tutorials
- Java Tutorial (Oracle): https://docs.oracle.com/javase/tutorial/

### Video Tutorial (Bahasa Indonesia):
- Programmer Zaman Now (Spring Boot Playlist): YouTube
- Web Programming UNPAS (Git & GitHub): YouTube

### Cheat Sheets:
- Git Cheat Sheet: https://education.github.com/git-cheat-sheet-education.pdf
- Maven Cheat Sheet: https://maven.apache.org/guides/MavenQuickReferenceCard.pdf

---

## K. Bantuan & Support

Jika mengalami kesulitan:

1. **Baca error message dengan teliti** (Google error message kalau tidak paham)
2. **Cek Troubleshooting section** di atas
3. **Tanya guru/mentor** (jangan malu bertanya!)
4. **Diskusi dengan teman** (pair programming sangat efektif)
5. **Search di StackOverflow** (99% error sudah pernah dialami orang lain)

**Prinsip Debugging:**
- Jangan panic 😊
- Baca error message dari ATAS (baris pertama biasanya root cause)
- Copy error message, Google dengan keyword: `error message + "spring boot"` atau `"maven"`
- Coba satu solusi, test, repeat

---

## Selamat Belajar! 🚀

Tahap 00 ini adalah fondasi. Jika fondasi kuat, tahap selanjutnya akan lancar.

**Tips Sukses:**
- Jangan skip tahapan (install semua tools dengan benar)
- Biasakan pakai Git sejak awal (commit often!)
- Kalau error, debugging dulu sebelum lanjut
- Belajar konsep, bukan cuma copy-paste kode
- Konsisten practice setiap hari

**Remember:** Setiap programmer hebat pernah menjadi pemula. Yang membedakan adalah konsistensi dan kemauan belajar!

---

**Author:** SIJA Spring Boot Training Team  
**Last Updated:** November 2025  
**Version:** 1.0
