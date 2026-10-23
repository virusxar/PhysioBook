package com.virusxar.physiobook.controllers;

import com.virusxar.physiobook.domain.dto.patient.CreatePatientRequest;
import com.virusxar.physiobook.domain.dto.patient.PatientResponse;
import com.virusxar.physiobook.domain.dto.patient.UpdatePatientRequest;
import com.virusxar.physiobook.services.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request) {
        PatientResponse created = patientService.create(request);
        return ResponseEntity.created(URI.create("/api/patients/" + created.id())).body(created);
    }

    @GetMapping
    public List<PatientResponse> search(@RequestParam(required = false) String lastName) {
        return patientService.search(lastName);
    }

    @GetMapping("/{id}")
    public PatientResponse getById(@PathVariable Long id) {
        return patientService.getById(id);
    }

    @PutMapping("/{id}")
    public PatientResponse update(@PathVariable Long id, @Valid @RequestBody UpdatePatientRequest request) {
        return patientService.update(id, request);
    }
}
