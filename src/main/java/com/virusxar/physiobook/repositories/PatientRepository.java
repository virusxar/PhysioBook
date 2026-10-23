package com.virusxar.physiobook.repositories;

import com.virusxar.physiobook.domain.entities.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    List<PatientEntity> findByLastNameContainingIgnoreCase(String lastName);

    Optional<PatientEntity> findByUserEntityId(Long userId);
}
