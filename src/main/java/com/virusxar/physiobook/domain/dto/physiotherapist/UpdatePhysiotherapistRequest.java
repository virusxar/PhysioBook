package com.virusxar.physiobook.domain.dto.physiotherapist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePhysiotherapistRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 30) String phone,
        @NotBlank @Size(max = 30) String licenseNumber,
        @NotBlank @Size(max = 150) String specialization,
        String bio
) {}
