-- Setup database PostgreSQL untuk SIJA Presensi
-- Database: presensi_sija
-- User: postgres
-- Password: postgres

-- 1. Create database (jalankan sebagai postgres superuser)
-- Jika database sudah ada, skip step ini
CREATE DATABASE presensi_sija
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- 2. Connect ke database presensi_sija
\c presensi_sija

-- 3. Grant privileges ke user postgres
GRANT ALL PRIVILEGES ON DATABASE presensi_sija TO postgres;
GRANT ALL ON SCHEMA public TO postgres;

-- Note: 
-- Tables akan dibuat otomatis oleh Hibernate dengan ddl-auto=update
-- Tidak perlu create tables manual
