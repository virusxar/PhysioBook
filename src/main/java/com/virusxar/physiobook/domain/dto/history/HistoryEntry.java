package com.virusxar.physiobook.domain.dto.history;

import com.virusxar.physiobook.domain.enums.HistoryEntryType;

import java.time.OffsetDateTime;

public record HistoryEntry(
        HistoryEntryType type,
        OffsetDateTime at,
        Long sourceId,
        Long episodeId,
        String title,
        String content
) {}
