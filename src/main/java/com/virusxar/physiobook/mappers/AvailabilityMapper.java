package com.virusxar.physiobook.mappers;

import com.virusxar.physiobook.domain.dto.availability.AvailabilityRequest;
import com.virusxar.physiobook.domain.dto.availability.AvailabilityResponse;
import com.virusxar.physiobook.domain.entities.AvailabilityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.DayOfWeek;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "physiotherapistEntity", ignore = true)
    AvailabilityEntity toEntity(AvailabilityRequest request);

    AvailabilityResponse toResponse(AvailabilityEntity entity);

    default short toShort(DayOfWeek day) {
        return (short) day.getValue();
    }

    default DayOfWeek toDayOfWeek(short value) {
        return DayOfWeek.of(value);
    }
}
