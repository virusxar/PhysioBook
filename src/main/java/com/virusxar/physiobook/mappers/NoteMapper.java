package com.virusxar.physiobook.mappers;

import com.virusxar.physiobook.domain.dto.note.NoteResponse;
import com.virusxar.physiobook.domain.entities.ClinicalNoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    @Mapping(target = "patientId", source = "patientEntity.id")
    @Mapping(target = "authorId", source = "physiotherapistEntity.id")
    @Mapping(target = "authorName", expression = "java(entity.getPhysiotherapistEntity().getFirstName() + \" \" + entity.getPhysiotherapistEntity().getLastName())")
    @Mapping(target = "appointmentId", source = "appointmentEntity.id")
    @Mapping(target = "episodeId", source = "episodeEntity.id")
    NoteResponse toResponse(ClinicalNoteEntity entity);
}
