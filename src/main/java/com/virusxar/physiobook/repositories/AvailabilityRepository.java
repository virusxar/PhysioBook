package com.virusxar.physiobook.repositories;

import com.virusxar.physiobook.domain.entities.AvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, Long> {

    List<AvailabilityEntity> findByPhysiotherapistEntityIdOrderByDayOfWeekAscStartTimeAsc(Long physiotherapistId);

    List<AvailabilityEntity> findByPhysiotherapistEntityIdAndDayOfWeek(Long physiotherapistId, short dayOfWeek);

    void deleteByPhysiotherapistEntityId(Long physiotherapistId);
}
