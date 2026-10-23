package com.virusxar.physiobook.repositories;

import com.virusxar.physiobook.domain.entities.ClinicalNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNoteEntity, Long> {
    List<ClinicalNoteEntity> findByPatientEntityIdOrderByCreatedAtDesc(Long patientId);

    List<ClinicalNoteEntity> findByPatientEntityIdAndEpisodeEntityIdOrderByCreatedAtDesc(Long patientId, Long episodeId);
}
