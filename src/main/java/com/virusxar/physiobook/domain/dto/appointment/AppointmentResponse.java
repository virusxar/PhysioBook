package com.virusxar.physiobook.domain.dto.appointment;

import com.virusxar.physiobook.domain.enums.AppointmentStatus;

import java.time.OffsetDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        Long physiotherapistId,
        String physiotherapistName,
        Long episodeId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        AppointmentStatus status,
        String reason,
        String cancellationReason
) {}
