package com.virusxar.physiobook.domain.dto.physiotherapist;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePhysiotherapistRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 30) String phone,
        @NotBlank @Size(max = 30) String licenseNumber,
        @NotBlank @Size(max = 150) String specialization,
        String bio
) {}
