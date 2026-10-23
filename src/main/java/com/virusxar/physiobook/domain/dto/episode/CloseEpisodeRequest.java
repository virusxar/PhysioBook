package com.virusxar.physiobook.domain.dto.episode;

import com.virusxar.physiobook.domain.enums.EpisodeStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CloseEpisodeRequest(
        @NotNull EpisodeStatus status,
        LocalDate endDate
) {}
