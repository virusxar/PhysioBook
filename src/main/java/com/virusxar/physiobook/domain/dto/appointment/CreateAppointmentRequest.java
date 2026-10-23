package com.virusxar.physiobook.domain.dto.appointment;

import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

public record CreateAppointmentRequest(
        @NotNull Long patientId,
        @NotNull Long physiotherapistId,
        @NotNull @Future OffsetDateTime startTime,
        @NotNull @Min(15) @Max(240) Integer durationMinutes,
        @Size(max = 500) String reason,
        Long episodeId
) {}
