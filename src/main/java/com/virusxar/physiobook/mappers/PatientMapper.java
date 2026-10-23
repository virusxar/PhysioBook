package com.virusxar.physiobook.mappers;

import com.virusxar.physiobook.domain.dto.patient.CreatePatientRequest;
import com.virusxar.physiobook.domain.dto.patient.PatientResponse;
import com.virusxar.physiobook.domain.dto.patient.UpdatePatientRequest;
import com.virusxar.physiobook.domain.entities.PatientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userEntity", ignore = true)
    PatientEntity toEntity(CreatePatientRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userEntity", ignore = true)
    void update(@MappingTarget PatientEntity entity, UpdatePatientRequest request);

    PatientResponse toResponse(PatientEntity entity);
}
