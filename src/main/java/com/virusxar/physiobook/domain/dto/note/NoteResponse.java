package com.virusxar.physiobook.domain.dto.note;

import com.virusxar.physiobook.domain.enums.NoteType;

import java.time.OffsetDateTime;

public record NoteResponse(
        Long id,
        Long patientId,
        Long authorId,
        String authorName,
        Long appointmentId,
        Long episodeId,
        NoteType type,
        String content,
        Boolean visibleToPatient,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
