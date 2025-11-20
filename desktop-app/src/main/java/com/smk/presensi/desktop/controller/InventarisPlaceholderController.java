package com.smk.presensi.desktop.controller;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Placeholder sederhana untuk Inventaris.
 */
public class InventarisPlaceholderController implements Initializable {

    @FXML private TableView<InventarisRow> inventarisTable;
    @FXML private TableColumn<InventarisRow, String> kodeColumn;
    @FXML private TableColumn<InventarisRow, String> namaColumn;
    @FXML private TableColumn<InventarisRow, String> kategoriColumn;
    @FXML private TableColumn<InventarisRow, Integer> jumlahColumn;
    @FXML private TableColumn<InventarisRow, String> lokasiColumn;
    @FXML private Label infoLabel;
    @FXML private Button importButton;
    @FXML private Button exportExcelButton;
    @FXML private Button exportCsvButton;
    @FXML private Button exportPdfButton;

    private final ObservableList<InventarisRow> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        kodeColumn.setCellValueFactory(new PropertyValueFactory<>("kode"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        kategoriColumn.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        jumlahColumn.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        lokasiColumn.setCellValueFactory(new PropertyValueFactory<>("lokasi"));

        data.addAll(
                new InventarisRow("INV-001", "Laptop Lenovo", "Elektronik", 12, "Lab RPL 1"),
                new InventarisRow("INV-002", "Router Mikrotik", "Jaringan", 6, "Lab TKJ"),
                new InventarisRow("INV-003", "Proyektor Epson", "Multimedia", 4, "Ruang Guru")
        );
        inventarisTable.setItems(data);
        infoLabel.setText("Total item: " + data.size() + " (mock data)");
    }

    @FXML
    private void handleImportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Inventaris dari Excel");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(getWindow());
        if (file == null) return;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            boolean headerSkipped = false;
            int imported = 0;

            for (Row row : sheet) {
                if (!headerSkipped) { headerSkipped = true; continue; }
                if (row == null) continue;
                String kode = formatter.formatCellValue(row.getCell(0));
                String nama = formatter.formatCellValue(row.getCell(1));
                String kategori = formatter.formatCellValue(row.getCell(2));
                String jumlahStr = formatter.formatCellValue(row.getCell(3));
                String lokasi = formatter.formatCellValue(row.getCell(4));
                if (kode == null || kode.isBlank()) continue;
                int jumlah = parseIntSafe(jumlahStr);
                data.add(new InventarisRow(kode.trim(), nama, kategori, jumlah, lokasi));
                imported++;
            }
            infoLabel.setText("Import selesai: " + imported + " baris ditambahkan");
            showAlert(Alert.AlertType.INFORMATION, "Import berhasil", "Berhasil import " + imported + " baris.");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Import gagal", ex.getMessage());
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Inventaris ke Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        chooser.setInitialFileName("inventaris-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".xlsx");
        File file = chooser.showSaveDialog(getWindow());
        if (file == null) return;

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Inventaris");
            String[] headers = {"Kode", "Nama Barang", "Kategori", "Jumlah", "Lokasi"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowIdx = 1;
            for (InventarisRow r : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getKode());
                row.createCell(1).setCellValue(r.getNama());
                row.createCell(2).setCellValue(r.getKategori());
                row.createCell(3).setCellValue(r.getJumlah());
                row.createCell(4).setCellValue(r.getLokasi());
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            infoLabel.setText("Export Excel berhasil: " + file.getName());
            showAlert(Alert.AlertType.INFORMATION, "Export berhasil", "File tersimpan di:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export gagal", e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Inventaris ke CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("inventaris-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv");
        File file = chooser.showSaveDialog(getWindow());
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Kode", "Nama", "Kategori", "Jumlah", "Lokasi"))) {
            for (InventarisRow r : data) {
                csv.printRecord(r.getKode(), r.getNama(), r.getKategori(), r.getJumlah(), r.getLokasi());
            }
            infoLabel.setText("Export CSV berhasil: " + file.getName());
            showAlert(Alert.AlertType.INFORMATION, "Export berhasil", "File tersimpan di:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export gagal", e.getMessage());
        }
    }

    @FXML
    private void handleExportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Cetak Laporan Inventaris (PDF)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        chooser.setInitialFileName("laporan-inventaris-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".pdf");
        File file = chooser.showSaveDialog(getWindow());
        if (file == null) return;

        try {
            exportPdf(file);
            infoLabel.setText("Laporan PDF berhasil: " + file.getName());
            showAlert(Alert.AlertType.INFORMATION, "Laporan berhasil", "PDF tersimpan di:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Laporan gagal", e.getMessage());
        }
    }

    private void exportPdf(File file) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Paragraph title = new Paragraph("LAPORAN INVENTARIS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Paragraph meta = new Paragraph("Tanggal cetak: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                FontFactory.getFont(FontFactory.HELVETICA, 10));
        meta.setSpacingAfter(15);
        document.add(meta);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 2.4f, 1.5f, 1f, 2f});
        String[] headers = {"Kode", "Nama Barang", "Kategori", "Jumlah", "Lokasi"};
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new BaseColor(52, 73, 94));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (InventarisRow r : data) {
            table.addCell(createCell(r.getKode(), bodyFont));
            table.addCell(createCell(r.getNama(), bodyFont));
            table.addCell(createCell(r.getKategori(), bodyFont));
            table.addCell(createCell(String.valueOf(r.getJumlah()), bodyFont, Element.ALIGN_CENTER));
            table.addCell(createCell(r.getLokasi(), bodyFont));
        }
        document.add(table);

        Paragraph total = new Paragraph("Total item: " + data.size(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
        total.setSpacingBefore(12);
        document.add(total);
        document.close();
    }

    private PdfPCell createCell(String text, Font font) {
        return createCell(text, font, Element.ALIGN_LEFT);
    }

    private PdfPCell createCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Window getWindow() {
        return inventarisTable != null && inventarisTable.getScene() != null ? inventarisTable.getScene().getWindow() : null;
    }

    public static class InventarisRow {
        private final String kode;
        private final String nama;
        private final String kategori;
        private final int jumlah;
        private final String lokasi;

        public InventarisRow(String kode, String nama, String kategori, int jumlah, String lokasi) {
            this.kode = kode;
            this.nama = nama;
            this.kategori = kategori;
            this.jumlah = jumlah;
            this.lokasi = lokasi;
        }

        public String getKode() { return kode; }
        public String getNama() { return nama; }
        public String getKategori() { return kategori; }
        public int getJumlah() { return jumlah; }
        public String getLokasi() { return lokasi; }
    }
}
