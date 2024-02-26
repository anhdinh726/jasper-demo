package org.aptech.jasper_report.controller;

import net.sf.jasperreports.engine.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aptech.jasper_report.entity.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ReportController {

    @GetMapping("/generate-pdf-report")
    public ResponseEntity<byte[]> generateReport() {
        try {
            // Load mẫu JRXML từ file report_template.jrxml
            InputStream inputStream = new ClassPathResource("report_template.jrxml").getInputStream();

            // Biên dịch JRXML thành JasperReport
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

            // dữ liệu mẫu
            Item item = new Item("Item 1", 10000);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("itemName", item.getItemName());
            parameters.put("price", item.getPrice());

            // fill dữ liệu vào JasperReport
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // Xuất ra file PDF
            byte[] pdfReport = JasperExportManager.exportReportToPdf(jasperPrint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfReport);
        } catch (JRException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/generate-excel-report")
    public ResponseEntity<InputStreamResource> generateExcel() {
        try {
            // Tạo workbook mới
            Workbook workbook = new XSSFWorkbook();

            // Tạo một sheet mới
            Sheet sheet = workbook.createSheet("Items");

            // Tạo header row (có thể set các thuộc tính thêm như bôi màu, freeze panes)
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Item Name");
            headerRow.createCell(1).setCellValue("Price");
            // ví dụ: tô màu header
            // cài đặt màu cho header
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // sau đó lặp qua các ô ở header để tô màu
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }

            // Dữ liệu mẫu
            List<Item> items = new ArrayList<>();
            items.add(new Item("Item 1", 10000));
            items.add(new Item("Item 2", 20000));
            items.add(new Item("Item 3", 30000));

            // Điền dữ liệu từ items vào các dòng tiếp theo trong sheet
            int rowNum = 1;
            for (Item item : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getItemName());
                row.createCell(1).setCellValue(item.getPrice());
            }

            // Tạo một ByteArrayOutputStream để ghi dữ liệu của workbook vào
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            workbook.close();

            // Convert ByteArrayOutputStream thành ByteArrayInputStream để truyền vào InputStreamResource
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

            // Trả về InputStreamResource để tải file Excel từ API
            InputStreamResource inputStreamResource = new InputStreamResource(byteArrayInputStream);

            // Cài đặt các header cho phản hồi HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "items.xlsx");

            // Trả về phản hồi HTTP có chứa InputStreamResource
            return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
