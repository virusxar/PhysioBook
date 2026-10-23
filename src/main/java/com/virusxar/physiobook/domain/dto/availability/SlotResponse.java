package com.virusxar.physiobook.domain.dto.availability;

import java.time.OffsetDateTime;

public record SlotResponse(
        OffsetDateTime start,
        OffsetDateTime end
) {}
