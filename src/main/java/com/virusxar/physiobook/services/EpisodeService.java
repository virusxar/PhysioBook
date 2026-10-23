package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.episode.CloseEpisodeRequest;
import com.virusxar.physiobook.domain.dto.episode.CreateEpisodeRequest;
import com.virusxar.physiobook.domain.dto.episode.EpisodeResponse;
import com.virusxar.physiobook.domain.dto.episode.UpdateEpisodeRequest;
import com.virusxar.physiobook.domain.entities.PatientEntity;
import com.virusxar.physiobook.domain.entities.PhysiotherapistEntity;
import com.virusxar.physiobook.domain.entities.TreatmentEpisodeEntity;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.mappers.EpisodeMapper;
import com.virusxar.physiobook.repositories.PatientRepository;
import com.virusxar.physiobook.repositories.PhysiotherapistRepository;
import com.virusxar.physiobook.repositories.TreatmentEpisodeRepository;
import com.virusxar.physiobook.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EpisodeService {
    private final TreatmentEpisodeRepository episodeRepository;
    private final PatientRepository patientRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final EpisodeMapper episodeMapper;
    private final AccessGuard accessGuard;

    @Transactional
    public EpisodeResponse create(Long patientId, CreateEpisodeRequest request) {
        Long physiotherapistId = accessGuard.currentProfileId();
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono pacjenta o id " + patientId));
        PhysiotherapistEntity physiotherapist = physiotherapistRepository.findById(physiotherapistId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono fizjoterapeuty o id " + physiotherapistId));

        TreatmentEpisodeEntity episode = episodeMapper.toEntity(request);
        episode.setPatientEntity(patient);
        episode.setPhysiotherapistEntity(physiotherapist);

        return episodeMapper.toResponse(episodeRepository.save(episode));
    }

    @Transactional(readOnly = true)
    public List<EpisodeResponse> getForPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new NotFoundException("Nie znaleziono pacjenta o id " + patientId);
        }
        return episodeRepository.findByPatientEntityIdOrderByStartDateDesc(patientId).stream()
                .map(episodeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EpisodeResponse getById(Long id) {
        return episodeMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public EpisodeResponse update(Long id, UpdateEpisodeRequest request) {
        TreatmentEpisodeEntity episode = findOrThrow(id);
        episodeMapper.update(episode, request);
        return episodeMapper.toResponse(episode);
    }

    @Transactional
    public EpisodeResponse close(Long id, CloseEpisodeRequest request) {
        TreatmentEpisodeEntity episode = findOrThrow(id);
        episode.close(request.status(), request.endDate());
        return episodeMapper.toResponse(episode);
    }

    private TreatmentEpisodeEntity findOrThrow(Long id) {
        return episodeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono epizodu o id " + id));
    }
}
