package com.smk.presensi.desktop.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.smk.presensi.desktop.model.Presensi;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service untuk export laporan presensi ke PDF dan CSV
 */
public class ExportService {
    
    private final PresensiService presensiService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public ExportService(PresensiService presensiService) {
        this.presensiService = presensiService;
    }
    
    /**
     * Export laporan presensi ke PDF
     * 
     * @param startDate Tanggal awal periode
     * @param endDate Tanggal akhir periode
     * @param outputFile File tujuan export
     * @return File PDF yang telah dibuat
     */
    public File exportToPdf(LocalDate startDate, LocalDate endDate, File outputFile) throws Exception {
        // 1. Fetch data dari API
        List<Presensi> dataList = presensiService.getPresensiByDateRange(startDate, endDate);
        
        // 2. Create PDF document (Landscape orientation)
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        
        document.open();
        
        // 4. Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
        Paragraph title = new Paragraph("LAPORAN PRESENSI SISWA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        // 5. Add subtitle (school name, etc.)
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Paragraph subtitle = new Paragraph("SMK Negeri 1 Bandung", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
        
        // 6. Add metadata
        Font metaFont = new Font(Font.FontFamily.HELVETICA, 10);
        Paragraph meta = new Paragraph(
            "Periode: " + startDate.format(dateFormatter) + " s/d " + endDate.format(dateFormatter) + "\n" +
            "Tanggal Cetak: " + LocalDate.now().format(dateFormatter) + "\n" +
            "Total Data: " + dataList.size() + " records",
            metaFont
        );
        meta.setSpacingAfter(20);
        document.add(meta);
        
        // 7. Create table
        PdfPTable table = new PdfPTable(7); // 7 columns
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 1.2f, 2.5f, 1.5f, 1f, 1.2f, 1.2f});
        
        // Header (with background color)
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        String[] headers = {"No", "Tanggal", "Username", "Tipe", "Status", 
                           "Jam Masuk", "Jam Pulang"};
        
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setBackgroundColor(new BaseColor(52, 73, 94)); // Dark blue
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(8);
            table.addCell(headerCell);
        }
        
        // Data rows
        Font dataFont = new Font(Font.FontFamily.HELVETICA, 9);
        int no = 1;
        
        for (Presensi p : dataList) {
            // No
            PdfPCell noCell = new PdfPCell(new Phrase(String.valueOf(no++), dataFont));
            noCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noCell.setPadding(5);
            table.addCell(noCell);
            
            // Tanggal
            table.addCell(createDataCell(p.getTanggal().format(dateFormatter), dataFont, Element.ALIGN_CENTER));
            
            // Nama (username)
            table.addCell(createDataCell(p.getUsername(), dataFont, Element.ALIGN_LEFT));
            
            // Tipe (SISWA/GURU)
            table.addCell(createDataCell(p.getTipe(), dataFont, Element.ALIGN_CENTER));
            
            // Status with color
            PdfPCell statusCell = new PdfPCell(new Phrase(p.getStatus(), dataFont));
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setPadding(5);
            
            String status = p.getStatus();
            if ("HADIR".equals(status)) {
                statusCell.setBackgroundColor(new BaseColor(200, 255, 200)); // Light green
            } else if ("TERLAMBAT".equals(status)) {
                statusCell.setBackgroundColor(new BaseColor(255, 255, 200)); // Light yellow
            } else if ("ALPHA".equals(status)) {
                statusCell.setBackgroundColor(new BaseColor(255, 200, 200)); // Light red
            }
            table.addCell(statusCell);
            
            // Waktu Masuk
            String waktuMasuk = p.getJamMasuk() != null ? p.getJamMasuk().toString() : "-";
            table.addCell(createDataCell(waktuMasuk, dataFont, Element.ALIGN_CENTER));
            
            // Waktu Keluar
            String waktuKeluar = p.getJamPulang() != null ? p.getJamPulang().toString() : "-";
            table.addCell(createDataCell(waktuKeluar, dataFont, Element.ALIGN_CENTER));
        }
        
        document.add(table);
        
        // 8. Add summary section
        document.add(new Paragraph("\n")); // Spacing
        
        Font summaryTitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Paragraph summaryTitle = new Paragraph("RINGKASAN", summaryTitleFont);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);
        
        // Calculate summary statistics
        Map<String, Long> summary = dataList.stream()
            .collect(Collectors.groupingBy(Presensi::getStatus, Collectors.counting()));
        
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(40);
        summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        summaryTable.setWidths(new float[]{3, 1});
        
        Font summaryFont = new Font(Font.FontFamily.HELVETICA, 10);
        
        // Add summary rows
        addSummaryRow(summaryTable, "Total Hadir", summary.getOrDefault("HADIR", 0L), summaryFont, new BaseColor(200, 255, 200));
        addSummaryRow(summaryTable, "Total Terlambat", summary.getOrDefault("TERLAMBAT", 0L), summaryFont, new BaseColor(255, 255, 200));
        addSummaryRow(summaryTable, "Total Alpha", summary.getOrDefault("ALPHA", 0L), summaryFont, new BaseColor(255, 200, 200));
        
        // Total
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", totalFont));
        totalLabelCell.setPadding(5);
        totalLabelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        summaryTable.addCell(totalLabelCell);
        
        PdfPCell totalValueCell = new PdfPCell(new Phrase(String.valueOf(dataList.size()), totalFont));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalValueCell.setPadding(5);
        totalValueCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        summaryTable.addCell(totalValueCell);
        
        document.add(summaryTable);
        
        // 9. Add footer
        document.add(new Paragraph("\n\n"));
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC);
        Paragraph footer = new Paragraph(
            "Dokumen ini dibuat secara otomatis oleh Sistem Presensi SIJA",
            footerFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        // 10. Close document
        document.close();
        
        System.out.println("PDF exported successfully: " + outputFile.getAbsolutePath());
        return outputFile;
    }
    
    /**
     * Export laporan presensi ke CSV
     * 
     * @param startDate Tanggal awal periode
     * @param endDate Tanggal akhir periode
     * @param outputFile File tujuan export
     * @return File CSV yang telah dibuat
     */
    public File exportToCsv(LocalDate startDate, LocalDate endDate, File outputFile) throws Exception {
        // 1. Fetch data dari API
        List<Presensi> dataList = presensiService.getPresensiByDateRange(startDate, endDate);
        
        // 2. Create CSV file
        try (FileWriter writer = new FileWriter(outputFile);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT
                 .withHeader("Tanggal", "User ID", "Username", "Tipe", "Status", 
                            "Jam Masuk", "Jam Pulang", "Keterangan"))) {
            
            // 4. Write data rows
            for (Presensi p : dataList) {
                csv.printRecord(
                    p.getTanggal().format(dateFormatter),
                    p.getUserId(),
                    p.getUsername(),
                    p.getTipe(),
                    p.getStatus(),
                    p.getJamMasuk() != null ? p.getJamMasuk().toString() : "",
                    p.getJamPulang() != null ? p.getJamPulang().toString() : "",
                    p.getKeterangan() != null ? p.getKeterangan() : ""
                );
            }
        }
        
        System.out.println("CSV exported successfully: " + outputFile.getAbsolutePath());
        return outputFile;
    }
    
    // ===== Helper Methods =====
    
    private PdfPCell createDataCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }
    
    private void addSummaryRow(PdfPTable table, String label, Long value, Font font, BaseColor color) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(String.valueOf(value), font));
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setPadding(5);
        valueCell.setBackgroundColor(color);
        table.addCell(valueCell);
    }
}
