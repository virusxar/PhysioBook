package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.availability.AvailabilityRequest;
import com.virusxar.physiobook.domain.dto.availability.AvailabilityResponse;
import com.virusxar.physiobook.domain.dto.availability.UpdateAvailabilityRequest;
import com.virusxar.physiobook.domain.entities.AvailabilityEntity;
import com.virusxar.physiobook.domain.entities.PhysiotherapistEntity;
import com.virusxar.physiobook.exceptions.BadRequestException;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.mappers.AvailabilityMapper;
import com.virusxar.physiobook.repositories.AvailabilityRepository;
import com.virusxar.physiobook.repositories.PhysiotherapistRepository;
import com.virusxar.physiobook.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final AvailabilityMapper availabilityMapper;
    private final AccessGuard accessGuard;

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> get(Long physiotherapistId) {
        if (!physiotherapistRepository.existsById(physiotherapistId)) {
            throw new NotFoundException("Nie znaleziono fizjoterapeuty o id " + physiotherapistId);
        }
        return availabilityRepository.findByPhysiotherapistEntityIdOrderByDayOfWeekAscStartTimeAsc(physiotherapistId)
                .stream()
                .map(availabilityMapper::toResponse)
                .toList();
    }

    // Podmienia cały tygodniowy grafik: stare wpisy usuwa, nowe zapisuje.
    @Transactional
    public List<AvailabilityResponse> replace(Long physiotherapistId, UpdateAvailabilityRequest request) {
        accessGuard.checkPhysiotherapistAccess(physiotherapistId);
        PhysiotherapistEntity physiotherapist = physiotherapistRepository.findById(physiotherapistId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono fizjoterapeuty o id " + physiotherapistId));

        validate(request.entries());

        availabilityRepository.deleteByPhysiotherapistEntityId(physiotherapistId);

        List<AvailabilityEntity> entities = request.entries().stream()
                .map(entry -> {
                    AvailabilityEntity entity = availabilityMapper.toEntity(entry);
                    entity.setPhysiotherapistEntity(physiotherapist);
                    return entity;
                })
                .toList();

        return availabilityRepository.saveAll(entities).stream()
                .sorted(Comparator.comparing(AvailabilityEntity::getDayOfWeek)
                        .thenComparing(AvailabilityEntity::getStartTime))
                .map(availabilityMapper::toResponse)
                .toList();
    }

    private void validate(List<AvailabilityRequest> entries) {
        for (AvailabilityRequest e : entries) {
            if (!e.endTime().isAfter(e.startTime())) {
                throw new BadRequestException("Godzina końca musi być po godzinie początku (" + e.dayOfWeek() + ")");
            }
        }
        // Dwa przedziały w tym samym dniu nie mogą na siebie nachodzić
        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                AvailabilityRequest a = entries.get(i);
                AvailabilityRequest b = entries.get(j);
                if (a.dayOfWeek() == b.dayOfWeek()
                        && a.startTime().isBefore(b.endTime())
                        && a.endTime().isAfter(b.startTime())) {
                    throw new BadRequestException("Godziny pracy nachodzą na siebie (" + a.dayOfWeek() + ")");
                }
            }
        }
    }
}
