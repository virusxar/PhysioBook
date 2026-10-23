package com.virusxar.physiobook.domain.dto.note;

import com.virusxar.physiobook.domain.enums.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNoteRequest(
        @NotNull NoteType type,
        @NotBlank @Size(max = 10_000) String content,
        Long appointmentId,
        Long episodeId,
        Boolean visibleToPatient
) {}
