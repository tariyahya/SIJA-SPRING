# SOLUSI ERROR DOWNLOAD GRADLE (Read timed out)

Error `java.net.SocketTimeoutException: Read timed out` terjadi karena koneksi internet lambat atau tidak stabil saat Android Studio mencoba mengunduh Gradle.

## Solusi 1: Download Manual (Paling Ampuh)

1.  **Download Gradle 8.0**:
    *   Buka browser dan download file ini: [https://services.gradle.org/distributions/gradle-8.0-bin.zip](https://services.gradle.org/distributions/gradle-8.0-bin.zip) (Ukuran sekitar 100MB).

2.  **Lokasi Penyimpanan**:
    *   Jangan di-extract.
    *   Simpan file `.zip` tersebut di folder yang mudah diakses, misalnya `C:\Users\sija_003\Documents\gradle-8.0-bin.zip`.

3.  **Update Konfigurasi Wrapper**:
    *   Buka file `mobile-app/gradle/wrapper/gradle-wrapper.properties` di VS Code.
    *   Ubah baris `distributionUrl` menjadi path ke file yang baru Anda download.
    *   Formatnya harus menggunakan `file:///` dan garis miring `/` (bukan backslash `\`).

    **Contoh:**
    ```properties
    distributionBase=GRADLE_USER_HOME
    distributionPath=wrapper/dists
    # Ganti baris ini:
    distributionUrl=file:///C:/Users/sija_003/Documents/gradle-8.0-bin.zip
    zipStoreBase=GRADLE_USER_HOME
    zipStorePath=wrapper/dists
    ```

4.  **Sync Ulang**:
    *   Kembali ke Android Studio dan klik **"Try Again"** atau **"Sync Project"**.

## Solusi 2: Gunakan Koneksi Lain
Jika memungkinkan, coba ganti ke jaringan internet yang lebih cepat atau stabil (misal: tethering HP 4G/5G sebentar) hanya untuk proses download awal ini.

## Solusi 3: Perbesar Timeout (Opsional)
Kadang membantu, tapi tidak menjamin. Tambahkan ini di `gradle.properties` (buat file jika belum ada di folder `mobile-app`):
```properties
systemProp.org.gradle.internal.http.connectionTimeout=120000
systemProp.org.gradle.internal.http.socketTimeout=120000
```
