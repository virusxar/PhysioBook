package com.virusxar.physiobook.domain.dto.physiotherapist;

public record PhysiotherapistResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String licenseNumber,
        String specialization,
        String bio
) {}
