package com.virusxar.physiobook.mappers;

import com.virusxar.physiobook.domain.dto.episode.CreateEpisodeRequest;
import com.virusxar.physiobook.domain.dto.episode.EpisodeResponse;
import com.virusxar.physiobook.domain.dto.episode.UpdateEpisodeRequest;
import com.virusxar.physiobook.domain.entities.TreatmentEpisodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EpisodeMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patientEntity", ignore = true)
    @Mapping(target = "physiotherapistEntity", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "episodeStatus", ignore = true)
    @Mapping(target = "startDate", source = "startDate", defaultExpression = "java(java.time.LocalDate.now())")
    TreatmentEpisodeEntity toEntity(CreateEpisodeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patientEntity", ignore = true)
    @Mapping(target = "physiotherapistEntity", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "episodeStatus", ignore = true)
    void update(@MappingTarget TreatmentEpisodeEntity entity, UpdateEpisodeRequest request);

    @Mapping(target = "patientId", source = "patientEntity.id")
    @Mapping(target = "physiotherapistId", source = "physiotherapistEntity.id")
    @Mapping(target = "physiotherapistName",
            expression = "java(entity.getPhysiotherapistEntity().getFirstName() + \" \" + entity.getPhysiotherapistEntity().getLastName())")
    @Mapping(target = "status", source = "episodeStatus")
    EpisodeResponse toResponse(TreatmentEpisodeEntity entity);
}
