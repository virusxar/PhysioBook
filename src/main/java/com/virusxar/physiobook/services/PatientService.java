package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.patient.CreatePatientRequest;
import com.virusxar.physiobook.domain.dto.patient.PatientResponse;
import com.virusxar.physiobook.domain.dto.patient.UpdatePatientRequest;
import com.virusxar.physiobook.domain.entities.PatientEntity;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.mappers.PatientMapper;
import com.virusxar.physiobook.repositories.PatientRepository;
import com.virusxar.physiobook.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final AccessGuard accessGuard;

    @Transactional
    public PatientResponse create(CreatePatientRequest request) {
        PatientEntity saved = patientRepository.save(patientMapper.toEntity(request));
        return patientMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        accessGuard.checkPatientAccess(id);
        return patientMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> search(String lastName) {
        List<PatientEntity> patients = (lastName == null || lastName.isBlank())
                ? patientRepository.findAll()
                : patientRepository.findByLastNameContainingIgnoreCase(lastName);
        return patients.stream().map(patientMapper::toResponse).toList();
    }

    @Transactional
    public PatientResponse update(Long id, UpdatePatientRequest request) {
        PatientEntity patient = findOrThrow(id);
        patientMapper.update(patient, request);
        return patientMapper.toResponse(patient);
    }

    private PatientEntity findOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono pacjenta o id " + id));
    }
}