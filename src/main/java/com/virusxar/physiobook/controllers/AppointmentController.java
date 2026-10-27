package com.virusxar.physiobook.controllers;

import com.virusxar.physiobook.domain.dto.appointment.AppointmentResponse;
import com.virusxar.physiobook.domain.dto.appointment.CancelAppointmentRequest;
import com.virusxar.physiobook.domain.dto.appointment.CreateAppointmentRequest;
import com.virusxar.physiobook.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse created = appointmentService.create(request);
        return ResponseEntity.created(URI.create("/api/appointments/" + created.id())).body(created);
    }

    @GetMapping("/appointments/{id}")
    public AppointmentResponse getById(@PathVariable Long id) {
        return appointmentService.getById(id);
    }

    @GetMapping("/patients/{patientId}/appointments")
    public List<AppointmentResponse> getForPatient(@PathVariable Long patientId) {
        return appointmentService.getForPatient(patientId);
    }

    @GetMapping("/physiotherapists/{physiotherapistId}/appointments")
    public List<AppointmentResponse> getForPhysiotherapist(
            @PathVariable Long physiotherapistId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.getForPhysiotherapist(physiotherapistId, date);
    }

    @PatchMapping("/appointments/{id}/confirm")
    public AppointmentResponse confirm(@PathVariable Long id) {
        return appointmentService.confirm(id);
    }

    @PatchMapping("/appointments/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelAppointmentRequest request) {
        return appointmentService.cancel(id, request.reason());
    }

    @PatchMapping("/appointments/{id}/complete")
    public AppointmentResponse complete(@PathVariable Long id) {
        return appointmentService.complete(id);
    }

    @PatchMapping("/appointments/{id}/no-show")
    public AppointmentResponse markNoShow(@PathVariable Long id) {
        return appointmentService.markNoShow(id);
    }
}
