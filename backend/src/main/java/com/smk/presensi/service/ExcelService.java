package com.smk.presensi.service;

import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.dto.pkl.DudiResponse;
import com.smk.presensi.dto.GuruResponse;
import com.smk.presensi.dto.KelasResponse;
import com.smk.presensi.dto.JurusanResponse;
import com.smk.presensi.dto.UserResponse;
import com.smk.presensi.dto.presensi.PresensiResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public ByteArrayInputStream exportSiswaToExcel(List<SiswaResponse> siswaList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Siswa");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"NIS", "Nama", "Kelas", "Jurusan"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Data
            int rowIdx = 1;
            for (SiswaResponse siswa : siswaList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(siswa.nis());
                row.createCell(1).setCellValue(siswa.nama());
                row.createCell(2).setCellValue(siswa.kelas());
                row.createCell(3).setCellValue(siswa.jurusan());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Gagal export data siswa ke Excel: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportDudiToExcel(List<DudiResponse> dudiList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("DUDI");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Nama", "Bidang Usaha", "Alamat", "Kontak Person", "Telepon", "Kuota", "Latitude", "Longitude", "Radius"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Data
            int rowIdx = 1;
            for (DudiResponse dudi : dudiList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dudi.nama());
                row.createCell(1).setCellValue(dudi.bidangUsaha());
                row.createCell(2).setCellValue(dudi.alamat());
                row.createCell(3).setCellValue(dudi.contactPerson());
                row.createCell(4).setCellValue(dudi.contactPhone());
                row.createCell(5).setCellValue(dudi.kuotaSiswa() != null ? dudi.kuotaSiswa() : 0);
                row.createCell(6).setCellValue(dudi.latitude() != null ? dudi.latitude() : 0.0);
                row.createCell(7).setCellValue(dudi.longitude() != null ? dudi.longitude() : 0.0);
                row.createCell(8).setCellValue(dudi.radiusValidasi() != null ? dudi.radiusValidasi() : 0);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Gagal export data DUDI ke Excel: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportGuruToExcel(List<GuruResponse> guruList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Guru");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"NIP", "Nama", "Mapel", "RFID ID"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            int rowIdx = 1;
            for (GuruResponse guru : guruList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(guru.nip());
                row.createCell(1).setCellValue(guru.nama());
                row.createCell(2).setCellValue(guru.mapel());
                row.createCell(3).setCellValue(guru.rfidCardId());
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Gagal export data guru ke Excel: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportKelasToExcel(List<KelasResponse> kelasList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Kelas");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Nama", "Tingkat", "Jurusan", "Kapasitas"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            int rowIdx = 1;
            for (KelasResponse kelas : kelasList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(kelas.nama());
                row.createCell(1).setCellValue(kelas.tingkat());
                row.createCell(2).setCellValue(kelas.jurusan());
                row.createCell(3).setCellValue(kelas.kapasitas() != null ? kelas.kapasitas() : 0);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Gagal export data kelas ke Excel: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportJurusanToExcel(List<JurusanResponse> jurusanList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Jurusan");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Kode", "Nama", "Durasi (Tahun)"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            int rowIdx = 1;
            for (JurusanResponse jurusan : jurusanList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(jurusan.kode());
                row.createCell(1).setCellValue(jurusan.nama());
                row.createCell(2).setCellValue(jurusan.durasiTahun() != null ? jurusan.durasiTahun() : 0);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Gagal export data jurusan ke Excel: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportUserToExcel(List<UserResponse> userList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Username", "Email", "Roles", "Enabled"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            int rowIdx = 1;
            for (UserResponse user : userList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.username());
                row.createCell(1).setCellValue(user.email());
                row.createCell(2).setCellValue(user.roles() != null ? String.join(", ", user.roles()) : "");
                row.createCell(3).setCellValue(user.enabled());
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Gagal export data user ke Excel: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportPresensiToExcel(List<PresensiResponse> presensiList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Presensi");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Tanggal", "Username", "Tipe", "Jam Masuk", "Jam Pulang", "Status", "Metode", "Keterangan"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            int rowIdx = 1;
            for (PresensiResponse p : presensiList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.tanggal().toString());
                row.createCell(1).setCellValue(p.username());
                row.createCell(2).setCellValue(p.tipe().toString());
                row.createCell(3).setCellValue(p.jamMasuk() != null ? p.jamMasuk().toString() : "-");
                row.createCell(4).setCellValue(p.jamPulang() != null ? p.jamPulang().toString() : "-");
                row.createCell(5).setCellValue(p.status().toString());
                row.createCell(6).setCellValue(p.method() != null ? p.method().toString() : "-");
                row.createCell(7).setCellValue(p.keterangan());
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Gagal export data presensi ke Excel: " + e.getMessage());
        }
    }

    public ByteArrayInputStream generateTemplate(String type) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Template " + type);
            Row headerRow = sheet.createRow(0);
            String[] columns;

            switch (type.toLowerCase()) {
                case "siswa":
                    columns = new String[]{"NIS", "Nama", "Kelas", "Jurusan", "Email"};
                    break;
                case "guru":
                    columns = new String[]{"NIP", "Nama", "Mapel", "RFID ID"};
                    break;
                case "kelas":
                    columns = new String[]{"Nama", "Tingkat", "Jurusan", "Kapasitas"};
                    break;
                case "jurusan":
                    columns = new String[]{"Kode", "Nama", "Durasi (Tahun)"};
                    break;
                case "user":
                    columns = new String[]{"Username", "Password", "Email", "Role", "Tipe"};
                    break;
                case "dudi":
                    columns = new String[]{"Nama", "Bidang Usaha", "Alamat", "Kontak Person", "Telepon", "Kuota"};
                    break;
                default:
                    columns = new String[]{"Column 1", "Column 2"};
            }

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Gagal generate template Excel: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
