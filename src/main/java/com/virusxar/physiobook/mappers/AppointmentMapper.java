package com.virusxar.physiobook.mappers;

import com.virusxar.physiobook.domain.dto.appointment.AppointmentResponse;
import com.virusxar.physiobook.domain.entities.AppointmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "patientId", source = "patientEntity.id")
    @Mapping(target = "patientName", expression = "java(entity.getPatientEntity().getFirstName() + \" \" + entity.getPatientEntity().getLastName())")
    @Mapping(target = "physiotherapistId", source = "physiotherapistEntity.id")
    @Mapping(target = "physiotherapistName", expression = "java(entity.getPhysiotherapistEntity().getFirstName() + \" \" + entity.getPhysiotherapistEntity().getLastName())")
    @Mapping(target = "episodeId", source = "episodeEntity.id")
    AppointmentResponse toResponse(AppointmentEntity entity);
}