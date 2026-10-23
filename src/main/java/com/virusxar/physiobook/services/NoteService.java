package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.note.CreateNoteRequest;
import com.virusxar.physiobook.domain.dto.note.NoteResponse;
import com.virusxar.physiobook.domain.dto.note.UpdateNoteRequest;
import com.virusxar.physiobook.domain.entities.*;
import com.virusxar.physiobook.exceptions.BadRequestException;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.mappers.NoteMapper;
import com.virusxar.physiobook.repositories.*;
import com.virusxar.physiobook.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final ClinicalNoteRepository noteRepository;
    private final PatientRepository patientRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final AppointmentRepository appointmentRepository;
    private final TreatmentEpisodeRepository episodeRepository;
    private final NoteMapper noteMapper;
    private final AccessGuard accessGuard;

    @Transactional
    public NoteResponse create(Long patientId, CreateNoteRequest request) {
        Long authorId = accessGuard.currentProfileId();
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono pacjenta o id " + patientId));
        PhysiotherapistEntity author = physiotherapistRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono fizjoterapeuty o id " + authorId));

        AppointmentEntity appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> new NotFoundException("Nie znaleziono wizyty o id " + request.appointmentId()));
            if (!appointment.getPatientEntity().getId().equals(patientId)) {
                throw new BadRequestException("Wizyta " + request.appointmentId() + " należy do innego pacjenta");
            }
        }

        TreatmentEpisodeEntity episode = null;
        if (request.episodeId() != null) {
            episode = episodeRepository.findById(request.episodeId())
                    .orElseThrow(() -> new NotFoundException("Nie znaleziono epizodu o id " + request.episodeId()));
            if (!episode.getPatientEntity().getId().equals(patientId)) {
                throw new BadRequestException("Epizod " + request.episodeId() + " należy do innego pacjenta");
            }
        }

        ClinicalNoteEntity note = ClinicalNoteEntity.builder()
                .patientEntity(patient)
                .physiotherapistEntity(author)
                .appointmentEntity(appointment)
                .episodeEntity(episode)
                .type(request.type())
                .content(request.content())
                .visibleToPatient(Boolean.TRUE.equals(request.visibleToPatient()))
                .build();

        return noteMapper.toResponse(noteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getForPatient(Long patientId, Long episodeId) {
        if (!patientRepository.existsById(patientId)) {
            throw new NotFoundException("Nie znaleziono pacjenta o id " + patientId);
        }
        List<ClinicalNoteEntity> notes = (episodeId == null)
                ? noteRepository.findByPatientEntityIdOrderByCreatedAtDesc(patientId)
                : noteRepository.findByPatientEntityIdAndEpisodeEntityIdOrderByCreatedAtDesc(patientId, episodeId);
        return notes.stream().map(noteMapper::toResponse).toList();
    }

    @Transactional
    public NoteResponse update(Long id, UpdateNoteRequest request) {
        ClinicalNoteEntity note = findOrThrow(id);
        note.setType(request.type());
        note.setContent(request.content());
        note.setVisibleToPatient(request.visibleToPatient());
        return noteMapper.toResponse(note);
    }

    @Transactional
    public void delete(Long id) {
        noteRepository.delete(findOrThrow(id));
    }

    private ClinicalNoteEntity findOrThrow(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono notatki o id " + id));
    }
}
