package com.marketplace.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Customer response data")
public record CustomerResponseDto(
    @Schema(description = "Customer ID", example = "1")
    Long id,
    
    @Schema(description = "First name", example = "John")
    String firstName,
    
    @Schema(description = "Last name", example = "Doe")
    String lastName,
    
    @Schema(description = "Email address", example = "john.doe@example.com")
    String email,
    
    @Schema(description = "Customer status", example = "ACTIVE")
    String status,
    
    @Schema(description = "Customer age", example = "34")
    Integer age,
    
    @Schema(description = "Birth date")
    LocalDate birthDate,
    
    @Schema(description = "Registration date (legacy Date format)")
    String registrationDate,
    
    @Schema(description = "Last activity date")
    LocalDate lastActivityDate,
    
    @Schema(description = "Renewal date")
    LocalDate renewalDate,
    
    @Schema(description = "Phone number")
    String phone,
    
    @Schema(description = "Address")
    String address,
    
    @Schema(description = "Total orders", example = "5")
    Integer totalOrders,
    
    @Schema(description = "Total amount spent")
    BigDecimal totalSpent,
    
    @Schema(description = "Seniority in days (legacy calculation)")
    Long seniorityInDaysLegacy,
    
    @Schema(description = "Seniority in years (modern calculation)")
    Integer seniorityInYearsModern
) {}
