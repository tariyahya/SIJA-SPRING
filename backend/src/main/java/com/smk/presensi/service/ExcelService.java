package com.smk.presensi.service;

import com.smk.presensi.dto.SiswaResponse;
import com.smk.presensi.dto.pkl.DudiResponse;
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
            String[] columns = {"NIS", "Nama", "Kelas", "Jurusan", "Email"};
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
                row.createCell(4).setCellValue(siswa.email());
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

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
