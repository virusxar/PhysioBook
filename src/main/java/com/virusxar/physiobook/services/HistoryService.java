package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.episode.EpisodeResponse;
import com.virusxar.physiobook.domain.dto.history.HistoryEntry;
import com.virusxar.physiobook.domain.dto.history.PatientHistoryResponse;
import com.virusxar.physiobook.domain.entities.*;
import com.virusxar.physiobook.domain.enums.AppointmentStatus;
import com.virusxar.physiobook.domain.enums.HistoryEntryType;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.mappers.EpisodeMapper;
import com.virusxar.physiobook.repositories.AppointmentRepository;
import com.virusxar.physiobook.repositories.ClinicalNoteRepository;
import com.virusxar.physiobook.repositories.PatientRepository;
import com.virusxar.physiobook.repositories.TreatmentEpisodeRepository;
import com.virusxar.physiobook.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private static final ZoneId CLINIC_ZONE = ZoneId.of("Europe/Warsaw");

    private final PatientRepository patientRepository;
    private final TreatmentEpisodeRepository episodeRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicalNoteRepository noteRepository;
    private final EpisodeMapper episodeMapper;
    private final AccessGuard accessGuard;

    @Transactional(readOnly = true)
    public PatientHistoryResponse getHistory(Long patientId) {
        accessGuard.checkPatientAccess(patientId);
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono pacjenta o id " + patientId));

        List<TreatmentEpisodeEntity> episodes = episodeRepository.findByPatientEntityIdOrderByStartDateDesc(patientId);
        List<AppointmentEntity> appointments = appointmentRepository.findByPatientEntityIdAndStatus(patientId, AppointmentStatus.COMPLETED);
        List<ClinicalNoteEntity> notes = noteRepository.findByPatientEntityIdOrderByCreatedAtDesc(patientId);
        if (accessGuard.isPatient()) {
            notes = notes.stream().filter(n -> Boolean.TRUE.equals(n.getVisibleToPatient())).toList();
        }

        List<HistoryEntry> timeline = new ArrayList<>();

        for (TreatmentEpisodeEntity e : episodes) {
            timeline.add(new HistoryEntry(HistoryEntryType.EPISODE_STARTED, startOfDay(e.getStartDate()),
                    e.getId(), e.getId(), "Rozpoczęto leczenie: " + e.getTitle(), e.getDiagnosis()));
            if (e.getEndDate() != null) {
                timeline.add(new HistoryEntry(HistoryEntryType.EPISODE_CLOSED, startOfDay(e.getEndDate()),
                        e.getId(), e.getId(), "Zakończono leczenie (" + e.getEpisodeStatus() + "): " + e.getTitle(), null));
            }
        }

        for (AppointmentEntity a : appointments) {
            timeline.add(new HistoryEntry(HistoryEntryType.APPOINTMENT, a.getStartTime(),
                    a.getId(), idOf(a.getEpisodeEntity()), "Wizyta: " + fullName(a.getPhysiotherapistEntity()), a.getReason()));
        }

        for (ClinicalNoteEntity n : notes) {
            timeline.add(new HistoryEntry(HistoryEntryType.NOTE, n.getCreatedAt(),
                    n.getId(), idOf(n.getEpisodeEntity()), "Notatka (" + n.getType() + "): " + fullName(n.getPhysiotherapistEntity()), n.getContent()));
        }

        timeline.sort(Comparator.comparing(HistoryEntry::at).reversed());

        List<EpisodeResponse> episodeResponses = episodes.stream().map(episodeMapper::toResponse).toList();

        return new PatientHistoryResponse(patient.getId(), patient.getFirstName() + " " + patient.getLastName(),
                episodeResponses, timeline);
    }

    private OffsetDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
    }

    private String fullName(PhysiotherapistEntity p) {
        return p.getFirstName() + " " + p.getLastName();
    }

    private Long idOf(TreatmentEpisodeEntity episode) {
        return episode != null ? episode.getId() : null;
    }
}
