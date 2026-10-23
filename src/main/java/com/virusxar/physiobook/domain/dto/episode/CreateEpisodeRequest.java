package com.virusxar.physiobook.domain.dto.episode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateEpisodeRequest(
        @NotBlank @Size(max = 200) String title,
        String diagnosis,
        String goal,
        LocalDate startDate
) {}