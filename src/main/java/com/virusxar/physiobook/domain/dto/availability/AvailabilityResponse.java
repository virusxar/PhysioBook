package com.virusxar.physiobook.domain.dto.availability;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotMinutes) {}

