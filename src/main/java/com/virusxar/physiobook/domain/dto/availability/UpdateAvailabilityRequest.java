package com.virusxar.physiobook.domain.dto.availability;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateAvailabilityRequest(
        @NotNull List<@Valid AvailabilityRequest> entries
) {}