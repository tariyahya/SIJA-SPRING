# Setup PostgreSQL untuk SIJA Presensi

## Prerequisites
1. PostgreSQL harus sudah terinstall
2. PostgreSQL service harus running
3. Default user `postgres` dengan password `postgres`

## Cara Setup Database

### Option 1: Menggunakan psql (Command Line)

```powershell
# 1. Masuk ke PostgreSQL shell sebagai postgres user
psql -U postgres

# 2. Di dalam psql shell, jalankan:
CREATE DATABASE presensi_sija;

# 3. Keluar dari psql
\q
```

### Option 2: Menggunakan pgAdmin (GUI)

1. Buka pgAdmin
2. Connect ke PostgreSQL server
3. Right-click pada "Databases" → Create → Database
4. Nama database: `presensi_sija`
5. Owner: `postgres`
6. Click "Save"

### Option 3: Menggunakan SQL Script

```powershell
# Jalankan SQL script yang sudah disediakan
psql -U postgres -f src/main/resources/db-setup.sql
```

## Verifikasi

Setelah database dibuat, verifikasi dengan:

```powershell
psql -U postgres -d presensi_sija -c "\dt"
```

Jika berhasil, akan muncul list tables (kosong jika baru dibuat).

## Konfigurasi Connection

File: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/presensi_sija
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Ganti `username` dan `password` sesuai dengan setup PostgreSQL Anda.

## Troubleshooting

### Error: "password authentication failed"
- Pastikan password postgres benar
- Ganti password di `application.properties`

### Error: "database does not exist"
- Buat database dulu dengan salah satu cara di atas
- Pastikan nama database: `presensi_sija`

### Error: "connection refused"
- Pastikan PostgreSQL service running
- Check dengan: `pg_isready`
- Start service: `pg_ctl start` atau melalui Services Windows
