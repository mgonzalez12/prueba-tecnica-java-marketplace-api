package com.marketplace.application.service;

import com.marketplace.domain.model.Inventory;
import com.marketplace.domain.model.Order;
import com.marketplace.domain.model.Product;
import com.marketplace.domain.port.InventoryPersistencePort;
import com.marketplace.domain.port.OrderPersistencePort;
import com.marketplace.domain.port.ProductPersistencePort;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating reports (PDF and Excel)
 * Demonstrates:
 * - PDF generation
 * - Excel generation
 * - Dynamic report creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ProductPersistencePort productPersistencePort;
    private final InventoryPersistencePort inventoryPersistencePort;
    private final OrderPersistencePort orderPersistencePort;

    /**
     * Generates a commercial report in PDF format
     */
    public byte[] generateCommercialReportPdf() throws DocumentException, IOException {
        log.info("Generating commercial report PDF");
        
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Marketplace Commercial Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Report date
        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph date = new Paragraph(
            "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            dateFont
        );
        date.setSpacingAfter(20);
        document.add(date);
        
        // Products section
        List<Product> products = productPersistencePort.findAll();
        addSection(document, "Products Summary", 
            String.format("Total Products: %d", products.size()));
        
        // Orders section
        List<Order> orders = orderPersistencePort.findAll();
        BigDecimal totalSales = orders.stream()
            .filter(o -> o.getTotalAmount() != null)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        addSection(document, "Sales Summary", 
            String.format("Total Orders: %d\nTotal Sales: $%.2f", 
                orders.size(), totalSales.doubleValue()));
        
        // Inventory section
        List<Inventory> inventories = inventoryPersistencePort.findAll();
        long lowStockCount = inventories.stream()
            .filter(Inventory::needsReorder)
            .count();
        
        addSection(document, "Inventory Summary", 
            String.format("Total Products in Inventory: %d\nProducts Needing Reorder: %d",
                inventories.size(), lowStockCount));
        
        document.close();
        
        return outputStream.toByteArray();
    }

    /**
     * Generates an inventory report in Excel format
     */
    public byte[] generateInventoryReportExcel() throws IOException {
        log.info("Generating inventory report Excel");
        
        List<Inventory> inventories = inventoryPersistencePort.findAll();
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Inventory Report");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Product ID", "Quantity", "Reserved", "Available", 
                "Min Level", "Max Level", "Location", "Needs Reorder"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Inventory inventory : inventories) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(inventory.getProductId());
                row.createCell(1).setCellValue(
                    inventory.getQuantity() != null ? inventory.getQuantity() : 0);
                row.createCell(2).setCellValue(
                    inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0);
                row.createCell(3).setCellValue(inventory.getAvailableQuantity());
                row.createCell(4).setCellValue(
                    inventory.getMinimumStockLevel() != null ? inventory.getMinimumStockLevel() : 0);
                row.createCell(5).setCellValue(
                    inventory.getMaximumStockLevel() != null ? inventory.getMaximumStockLevel() : 0);
                row.createCell(6).setCellValue(
                    inventory.getLocation() != null ? inventory.getLocation() : "");
                row.createCell(7).setCellValue(inventory.needsReorder() ? "Yes" : "No");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generates a sales report in Excel format
     */
    public byte[] generateSalesReportExcel() throws IOException {
        log.info("Generating sales report Excel");
        
        List<Order> orders = orderPersistencePort.findAll();
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Sales Report");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Order ID", "Order Number", "Customer ID", "Order Date", 
                "Status", "Subtotal", "Tax", "Total"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(
                    order.getOrderNumber() != null ? order.getOrderNumber() : "");
                row.createCell(2).setCellValue(order.getCustomerId());
                row.createCell(3).setCellValue(
                    order.getOrderDate() != null ? 
                        order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                row.createCell(4).setCellValue(
                    order.getStatus() != null ? order.getStatus().name() : "");
                row.createCell(5).setCellValue(
                    order.getSubtotalAmount() != null ? order.getSubtotalAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(
                    order.getTaxAmount() != null ? order.getTaxAmount().doubleValue() : 0);
                row.createCell(7).setCellValue(
                    order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generates an external products report in Excel format
     */
    public byte[] generateExternalProductsReportExcel() throws IOException {
        log.info("Generating external products report Excel");
        
        List<Product> products = productPersistencePort.findAll().stream()
            .filter(p -> p.getIsExternal() != null && p.getIsExternal())
            .toList();
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("External Products");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "SKU", "Name", "Description", "Price", 
                "Promotional Price", "Category", "External Provider ID"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getSku() != null ? product.getSku() : "");
                row.createCell(2).setCellValue(product.getName() != null ? product.getName() : "");
                row.createCell(3).setCellValue(
                    product.getDescription() != null ? product.getDescription() : "");
                row.createCell(4).setCellValue(
                    product.getPrice() != null ? product.getPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(
                    product.getPromotionalPrice() != null ? 
                        product.getPromotionalPrice().doubleValue() : 0);
                row.createCell(6).setCellValue(
                    product.getCategory() != null ? product.getCategory().getName() : "");
                row.createCell(7).setCellValue(
                    product.getExternalProviderId() != null ? product.getExternalProviderId() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void addSection(Document document, String title, String content) 
            throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph sectionTitle = new Paragraph(title, sectionFont);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph sectionContent = new Paragraph(content, contentFont);
        sectionContent.setSpacingAfter(15);
        document.add(sectionContent);
    }
}

