package com.marketplace.infrastructure.rest.controller;

import com.marketplace.application.service.FileService;
import com.marketplace.domain.model.Customer;
import com.marketplace.domain.model.Product;
import com.opencsv.exceptions.CsvException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File import/export APIs")
public class FileController {

    private final FileService fileService;

    @PostMapping("/import/customers")
    @Operation(summary = "Import customers from CSV file")
    public ResponseEntity<List<Customer>> importCustomers(@RequestParam("file") MultipartFile file) {
        try {
            List<Customer> customers = fileService.importCustomersFromCsv(file);
            return ResponseEntity.ok(customers);
        } catch (IOException | CsvException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/import/products")
    @Operation(summary = "Import products from CSV file")
    public ResponseEntity<List<Product>> importProducts(@RequestParam("file") MultipartFile file) {
        try {
            List<Product> products = fileService.importProductsFromCsv(file);
            return ResponseEntity.ok(products);
        } catch (IOException | CsvException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/export/customers")
    @Operation(summary = "Export customers to CSV")
    public ResponseEntity<byte[]> exportCustomers() {
        try {
            byte[] csvData = fileService.exportCustomersToCsv();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "customers.csv");
            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/export/products")
    @Operation(summary = "Export products to CSV")
    public ResponseEntity<byte[]> exportProducts() {
        try {
            byte[] csvData = fileService.exportProductsToCsv();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "products.csv");
            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

