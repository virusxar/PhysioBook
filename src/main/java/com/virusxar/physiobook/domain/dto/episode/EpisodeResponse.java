package com.virusxar.physiobook.domain.dto.episode;

import com.virusxar.physiobook.domain.enums.EpisodeStatus;

import java.time.LocalDate;

public record EpisodeResponse(
        Long id,
        Long patientId,
        Long physiotherapistId,
        String physiotherapistName,
        String title,
        String diagnosis,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        EpisodeStatus status
) {}
