package com.virusxar.physiobook.domain.dto.episode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateEpisodeRequest(
        @NotBlank @Size(max = 200) String title,
        String diagnosis,
        String goal
) {}
