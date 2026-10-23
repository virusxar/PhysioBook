package com.virusxar.physiobook.domain.dto.history;

import com.virusxar.physiobook.domain.dto.episode.EpisodeResponse;

import java.util.List;

public record PatientHistoryResponse(
        Long patientId,
        String patientName,
        List<EpisodeResponse> episodes,
        List<HistoryEntry> timeline
) {}
