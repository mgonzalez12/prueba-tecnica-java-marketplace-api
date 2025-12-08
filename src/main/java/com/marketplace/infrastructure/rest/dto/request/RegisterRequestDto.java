package com.marketplace.infrastructure.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank
        @Size(max = 50)
        String firstName,

        @NotBlank
        @Size(max = 50)
        String lastName,

        @NotBlank
        @Size(max = 100)
        @Email
        String email,

        @NotBlank
        @Size(min = 6, max = 100)
        String password
) {
}
