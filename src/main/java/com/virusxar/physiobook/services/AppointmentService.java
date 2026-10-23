package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.appointment.AppointmentResponse;
import com.virusxar.physiobook.domain.dto.appointment.CreateAppointmentRequest;
import com.virusxar.physiobook.domain.entities.*;
import com.virusxar.physiobook.domain.enums.AppointmentStatus;
import com.virusxar.physiobook.exceptions.BadRequestException;
import com.virusxar.physiobook.exceptions.ConflictException;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.mappers.AppointmentMapper;
import com.virusxar.physiobook.repositories.*;
import com.virusxar.physiobook.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    // Statusy, które blokują termin. Odwołana wizyta zwalnia miejsce.
    private static final Set<AppointmentStatus> ACTIVE_STATUSES =
            Set.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED);

    private static final ZoneId CLINIC_ZONE = ZoneId.of("Europe/Warsaw");

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final AppointmentMapper appointmentMapper;
    private final TreatmentEpisodeRepository episodeRepository;
    private final AvailabilityRepository availabilityRepository;
    private final AccessGuard accessGuard;

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        accessGuard.checkPatientAccess(request.patientId());
        PatientEntity patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new NotFoundException("Nie znaleziono pacjenta o id " + request.patientId()));
        PhysiotherapistEntity physiotherapist = physiotherapistRepository.findById(request.physiotherapistId())
                .orElseThrow(() -> new NotFoundException("Nie znaleziono fizjoterapeuty o id " + request.physiotherapistId()));

        OffsetDateTime start = request.startTime();
        OffsetDateTime end = start.plusMinutes(request.durationMinutes());

        if (!isWithinWorkingHours(physiotherapist.getId(), start, end)) {
            throw new BadRequestException("Termin jest poza godzinami pracy fizjoterapeuty");
        }

        if (appointmentRepository.existsOverlapping(physiotherapist.getId(), start, end, ACTIVE_STATUSES)) {
            throw new ConflictException("Fizjoterapeuta ma już wizytę w tym terminie");
        }

        TreatmentEpisodeEntity episode = null;
        if (request.episodeId() != null) {
            episode = episodeRepository.findById(request.episodeId())
                    .orElseThrow(() -> new NotFoundException("Nie znaleziono epizodu o id " + request.episodeId()));
            if (!episode.getPatientEntity().getId().equals(patient.getId())) {
                throw new BadRequestException("Epizod " + request.episodeId() + " należy do innego pacjenta");
            }
            if (!episode.isActive()) {
                throw new BadRequestException("Nie można dodać wizyty do zamkniętego epizodu");
            }
        }

        AppointmentEntity appointment = AppointmentEntity.builder()
                .patientEntity(patient)
                .physiotherapistEntity(physiotherapist)
                .startTime(start)
                .endTime(end)
                .reason(request.reason())
                .episodeEntity(episode)
                .build();

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        AppointmentEntity appointment = findOrThrow(id);
        accessGuard.checkPatientAccess(appointment.getPatientEntity().getId());
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getForPatient(Long patientId) {
        accessGuard.checkPatientAccess(patientId);
        return appointmentRepository.findByPatientEntityIdOrderByStartTimeDesc(patientId).stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDayForPhysiotherapist(Long physiotherapistId, LocalDate date) {
        OffsetDateTime from = date.atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
        OffsetDateTime to = date.plusDays(1).atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
        return appointmentRepository
                .findByPhysiotherapistEntityIdAndStartTimeBetweenOrderByStartTime(physiotherapistId, from, to).stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse confirm(Long id) {
        AppointmentEntity appointment = findOrThrow(id);
        appointment.confirm();
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(Long id, String reason) {
        AppointmentEntity appointment = findOrThrow(id);
        accessGuard.checkPatientAccess(appointment.getPatientEntity().getId());
        appointment.cancel(reason);
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse complete(Long id) {
        AppointmentEntity appointment = findOrThrow(id);
        appointment.complete();
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse markNoShow(Long id) {
        AppointmentEntity appointment = findOrThrow(id);
        appointment.markNoShow();
        return appointmentMapper.toResponse(appointment);
    }

    private AppointmentEntity findOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono wizyty o id " + id));
    }

    private boolean isWithinWorkingHours(Long physiotherapistId, OffsetDateTime start, OffsetDateTime end) {
        LocalDateTime localStart = start.atZoneSameInstant(CLINIC_ZONE).toLocalDateTime();
        LocalDateTime localEnd = end.atZoneSameInstant(CLINIC_ZONE).toLocalDateTime();
        if (!localStart.toLocalDate().equals(localEnd.toLocalDate())) {
            return false;
        }
        short dayOfWeek = (short) localStart.getDayOfWeek().getValue();
        for (AvailabilityEntity hours : availabilityRepository.findByPhysiotherapistEntityIdAndDayOfWeek(physiotherapistId, dayOfWeek)) {
            if (!localStart.toLocalTime().isBefore(hours.getStartTime())
                    && !localEnd.toLocalTime().isAfter(hours.getEndTime())) {
                return true;
            }
        }
        return false;
    }
}
