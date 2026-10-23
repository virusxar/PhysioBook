package com.virusxar.physiobook.domain.dto.patient;

import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String phone,
        String email
) {}
