package com.virusxar.physiobook.repositories;

import com.virusxar.physiobook.domain.entities.PhysiotherapistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhysiotherapistRepository extends JpaRepository<PhysiotherapistEntity, Long> {
    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long id);

    List<PhysiotherapistEntity> findByLastNameContainingIgnoreCase(String lastName);

    Optional<PhysiotherapistEntity> findByLicenseNumber(String licenseNumber);

    Optional<PhysiotherapistEntity> findByUserEntityId(Long userId);
}
