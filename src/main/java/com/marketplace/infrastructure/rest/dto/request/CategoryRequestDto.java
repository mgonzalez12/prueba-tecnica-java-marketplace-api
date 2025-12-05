package com.marketplace.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDto(
    @NotBlank(message = "Name is required")
    String name,
    
    String description,
    
    String code,
    
    Long parentId,
    
    Boolean active
) {}
