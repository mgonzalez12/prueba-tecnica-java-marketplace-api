package com.marketplace.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Schema(description = "Customer request data")
public record CustomerRequestDto(
    @Schema(description = "First name", example = "John", required = true)
    @NotBlank(message = "First name is required")
    String firstName,
    
    @Schema(description = "Last name", example = "Doe", required = true)
    @NotBlank(message = "Last name is required")
    String lastName,
    
    @Schema(description = "Email address", example = "john.doe@example.com", required = true)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email,
    
    @Schema(description = "Birth date", example = "1990-01-15")
    LocalDate birthDate,
    
    @Schema(description = "Phone number", example = "+1234567890")
    String phone,
    
    @Schema(description = "Address")
    String address,
    
    @Schema(description = "Renewal date")
    LocalDate renewalDate
) {}
