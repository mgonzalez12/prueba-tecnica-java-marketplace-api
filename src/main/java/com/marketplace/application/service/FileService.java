package com.marketplace.application.service;

import com.marketplace.domain.model.Customer;
import com.marketplace.domain.model.Product;
import com.marketplace.domain.port.CustomerPersistencePort;
import com.marketplace.domain.port.ProductPersistencePort;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling file operations (CSV import/export)
 * Demonstrates:
 * - File handling
 * - CSV processing
 * - Data import/export
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final CustomerPersistencePort customerPersistencePort;
    private final ProductPersistencePort productPersistencePort;
    
    private static final String UPLOAD_DIR = "uploads";
    private static final String EXPORT_DIR = "exports";

    /**
     * Imports customers from CSV file
     */
    @Transactional
    public List<Customer> importCustomersFromCsv(MultipartFile file) throws IOException, CsvException {
        log.info("Importing customers from CSV file: {}", file.getOriginalFilename());
        
        ensureDirectoryExists(UPLOAD_DIR);
        
        // Save uploaded file temporarily
        Path tempFile = saveTempFile(file);
        
        try (CSVReader reader = new CSVReader(new FileReader(tempFile.toFile()))) {
            List<String[]> records = reader.readAll();
            
            if (records.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }
            
            // Skip header row
            List<String[]> dataRows = records.subList(1, records.size());
            
            List<Customer> importedCustomers = new ArrayList<>();
            
            for (String[] row : dataRows) {
                try {
                    Customer customer = parseCustomerFromCsvRow(row);
                    Customer saved = customerPersistencePort.save(customer);
                    importedCustomers.add(saved);
                } catch (Exception e) {
                    log.error("Error importing customer from row: {}", String.join(",", row), e);
                    // Continue with next row
                }
            }
            
            log.info("Successfully imported {} customers", importedCustomers.size());
            return importedCustomers;
            
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Imports products from CSV file
     */
    @Transactional
    public List<Product> importProductsFromCsv(MultipartFile file) throws IOException, CsvException {
        log.info("Importing products from CSV file: {}", file.getOriginalFilename());
        
        ensureDirectoryExists(UPLOAD_DIR);
        
        Path tempFile = saveTempFile(file);
        
        try (CSVReader reader = new CSVReader(new FileReader(tempFile.toFile()))) {
            List<String[]> records = reader.readAll();
            
            if (records.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }
            
            // Skip header row
            List<String[]> dataRows = records.subList(1, records.size());
            
            List<Product> importedProducts = new ArrayList<>();
            
            for (String[] row : dataRows) {
                try {
                    Product product = parseProductFromCsvRow(row);
                    Product saved = productPersistencePort.save(product);
                    importedProducts.add(saved);
                } catch (Exception e) {
                    log.error("Error importing product from row: {}", String.join(",", row), e);
                    // Continue with next row
                }
            }
            
            log.info("Successfully imported {} products", importedProducts.size());
            return importedProducts;
            
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Exports customers to CSV file
     */
    public byte[] exportCustomersToCsv() throws IOException {
        log.info("Exporting customers to CSV");
        
        List<Customer> customers = customerPersistencePort.findAll();
        
        try (StringWriter stringWriter = new StringWriter();
             CSVWriter writer = new CSVWriter(stringWriter)) {
            
            // Write header
            writer.writeNext(new String[]{
                "ID", "First Name", "Last Name", "Email", "Phone", "Address", 
                "Birth Date", "Registration Date", "Last Activity Date", 
                "Status", "Total Orders", "Total Spent"
            });
            
            // Write data rows
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Customer customer : customers) {
                writer.writeNext(new String[]{
                    String.valueOf(customer.getId()),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getAddress(),
                    customer.getBirthDate() != null ? customer.getBirthDate().format(dateFormatter) : "",
                    customer.getRegistrationDate() != null ? 
                        customer.getRegistrationDate().toString() : "",
                    customer.getLastActivityDate() != null ? 
                        customer.getLastActivityDate().format(dateFormatter) : "",
                    customer.getStatus(),
                    customer.getTotalOrders() != null ? String.valueOf(customer.getTotalOrders()) : "0",
                    customer.getTotalSpent() != null ? customer.getTotalSpent().toString() : "0"
                });
            }
            
            return stringWriter.toString().getBytes();
        }
    }

    /**
     * Exports products to CSV file
     */
    public byte[] exportProductsToCsv() throws IOException {
        log.info("Exporting products to CSV");
        
        List<Product> products = productPersistencePort.findAll();
        
        try (StringWriter stringWriter = new StringWriter();
             CSVWriter writer = new CSVWriter(stringWriter)) {
            
            // Write header
            writer.writeNext(new String[]{
                "ID", "SKU", "Name", "Description", "Price", "Promotional Price",
                "Category ID", "Active", "Is External", "External Provider ID"
            });
            
            // Write data rows
            for (Product product : products) {
                writer.writeNext(new String[]{
                    String.valueOf(product.getId()),
                    product.getSku(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice() != null ? product.getPrice().toString() : "",
                    product.getPromotionalPrice() != null ? product.getPromotionalPrice().toString() : "",
                    product.getCategory() != null ? String.valueOf(product.getCategory().getId()) : "",
                    product.getActive() != null ? String.valueOf(product.getActive()) : "true",
                    product.getIsExternal() != null ? String.valueOf(product.getIsExternal()) : "false",
                    product.getExternalProviderId()
                });
            }
            
            return stringWriter.toString().getBytes();
        }
    }

    /**
     * Writes log to file
     */
    public void writeLogToFile(String logMessage) throws IOException {
        ensureDirectoryExists("logs");
        Path logFile = Paths.get("logs", "application.log");
        
        String logEntry = String.format("[%s] %s%n", 
            java.time.LocalDateTime.now(), logMessage);
        
        Files.write(logFile, logEntry.getBytes(), 
            java.nio.file.StandardOpenOption.CREATE, 
            java.nio.file.StandardOpenOption.APPEND);
    }

    /**
     * Reads configuration from file
     */
    public String readConfigurationFromFile(String configKey) throws IOException {
        Path configFile = Paths.get("config", configKey + ".properties");
        
        if (!Files.exists(configFile)) {
            return null;
        }
        
        return Files.readString(configFile);
    }

    private Customer parseCustomerFromCsvRow(String[] row) {
        if (row.length < 4) {
            throw new IllegalArgumentException("Invalid CSV row format for customer");
        }
        
        Customer customer = new Customer();
        customer.setFirstName(row[0]);
        customer.setLastName(row[1]);
        customer.setEmail(row[2]);
        
        if (row.length > 3 && !row[3].isEmpty()) {
            customer.setPhone(row[3]);
        }
        if (row.length > 4 && !row[4].isEmpty()) {
            customer.setAddress(row[4]);
        }
        if (row.length > 5 && !row[5].isEmpty()) {
            customer.setBirthDate(LocalDate.parse(row[5]));
        }
        if (row.length > 6 && !row[6].isEmpty()) {
            customer.setStatus(row[6]);
        }
        
        // Set registration date
        customer.setRegistrationDate(new java.util.Date());
        customer.setLastActivityDate(LocalDate.now());
        
        return customer;
    }

    /**
     * Parses a product from a CSV row.
     * Expected format (aligned with export header):
     * ID, SKU, Name, Description, Price, Promotional Price, Category ID, Active, Is External, External Provider ID
     */
    private Product parseProductFromCsvRow(String[] row) {
        if (row.length < 4) {
            throw new IllegalArgumentException("Invalid CSV row format for product");
        }

        // Indexes based on export header:
        // 0: ID (ignored)
        // 1: SKU
        // 2: Name
        // 3: Description
        // 4: Price
        // 5: Promotional Price
        // 6: Category ID
        // 7: Active
        // 8: Is External
        // 9: External Provider ID

        Product product = new Product();

        int i = 0;
        String sku = row.length > 1 ? row[1] : row[0]; // allow files without ID as first column
        String name = row.length > 2 ? row[2] : (row.length > 1 ? row[1] : null);
        String description = row.length > 3 ? row[3] : null;

        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);

        if (row.length > 4 && !row[4].isEmpty()) {
            product.setPrice(new BigDecimal(row[4]));
        }
        if (row.length > 5 && !row[5].isEmpty()) {
            product.setPromotionalPrice(new BigDecimal(row[5]));
        }

        if (row.length > 6 && !row[6].isEmpty()) {
            try {
                Long categoryId = Long.parseLong(row[6]);
                com.marketplace.domain.model.Category category = com.marketplace.domain.model.Category.builder()
                        .id(categoryId)
                        .build();
                product.setCategory(category);
            } catch (NumberFormatException e) {
                log.warn("Invalid category ID '{}' in CSV row, skipping category mapping", row[6]);
            }
        }

        if (row.length > 7 && !row[7].isEmpty()) {
            product.setActive(Boolean.parseBoolean(row[7]));
        } else {
            product.setActive(true);
        }

        if (row.length > 8 && !row[8].isEmpty()) {
            product.setIsExternal(Boolean.parseBoolean(row[8]));
        }

        if (row.length > 9 && !row[9].isEmpty()) {
            product.setExternalProviderId(row[9]);
        }

        product.setCreatedAt(java.time.LocalDateTime.now());
        product.setUpdatedAt(java.time.LocalDateTime.now());

        return product;
    }

    private Path saveTempFile(MultipartFile file) throws IOException {
        ensureDirectoryExists(UPLOAD_DIR);
        Path tempFile = Paths.get(UPLOAD_DIR, 
            System.currentTimeMillis() + "_" + file.getOriginalFilename());
        Files.write(tempFile, file.getBytes());
        return tempFile;
    }

    private void ensureDirectoryExists(String dirName) throws IOException {
        Path dir = Paths.get(dirName);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
}

