package com.virusxar.physiobook.mappers;

import com.virusxar.physiobook.domain.dto.physiotherapist.CreatePhysiotherapistRequest;
import com.virusxar.physiobook.domain.dto.physiotherapist.PhysiotherapistResponse;
import com.virusxar.physiobook.domain.dto.physiotherapist.UpdatePhysiotherapistRequest;
import com.virusxar.physiobook.domain.entities.PhysiotherapistEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PhysiotherapistMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userEntity", ignore = true)
    PhysiotherapistEntity toEntity(CreatePhysiotherapistRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userEntity", ignore = true)
    void update(@MappingTarget PhysiotherapistEntity entity, UpdatePhysiotherapistRequest request);

    PhysiotherapistResponse toResponse(PhysiotherapistEntity entity);
}
