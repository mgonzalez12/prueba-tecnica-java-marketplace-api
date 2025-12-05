package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.service.ReportService;
import com.lowagie.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation APIs")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/commercial/pdf")
    @Operation(summary = "Generate commercial report in PDF format")
    public ResponseEntity<byte[]> generateCommercialReportPdf() {
        try {
            byte[] pdfData = reportService.generateCommercialReportPdf();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "commercial-report.pdf");
            return ResponseEntity.ok().headers(headers).body(pdfData);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/inventory/excel")
    @Operation(summary = "Generate inventory report in Excel format")
    public ResponseEntity<byte[]> generateInventoryReportExcel() {
        try {
            byte[] excelData = reportService.generateInventoryReportExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "inventory-report.xlsx");
            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sales/excel")
    @Operation(summary = "Generate sales report in Excel format")
    public ResponseEntity<byte[]> generateSalesReportExcel() {
        try {
            byte[] excelData = reportService.generateSalesReportExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "sales-report.xlsx");
            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/external-products/excel")
    @Operation(summary = "Generate external products report in Excel format")
    public ResponseEntity<byte[]> generateExternalProductsReportExcel() {
        try {
            byte[] excelData = reportService.generateExternalProductsReportExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "external-products-report.xlsx");
            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

