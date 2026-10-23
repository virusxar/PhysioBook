package com.virusxar.physiobook.repositories;

import com.virusxar.physiobook.domain.entities.TreatmentEpisodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreatmentEpisodeRepository extends JpaRepository<TreatmentEpisodeEntity, Long> {
    List<TreatmentEpisodeEntity> findByPatientEntityIdOrderByStartDateDesc(Long patientId);
}
