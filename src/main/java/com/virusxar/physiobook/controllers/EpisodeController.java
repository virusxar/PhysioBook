package com.virusxar.physiobook.controllers;

import com.virusxar.physiobook.domain.dto.episode.CloseEpisodeRequest;
import com.virusxar.physiobook.domain.dto.episode.CreateEpisodeRequest;
import com.virusxar.physiobook.domain.dto.episode.EpisodeResponse;
import com.virusxar.physiobook.domain.dto.episode.UpdateEpisodeRequest;
import com.virusxar.physiobook.services.EpisodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;

    @PostMapping("/patients/{patientId}/episodes")
    public ResponseEntity<EpisodeResponse> create(@PathVariable Long patientId, @Valid @RequestBody CreateEpisodeRequest request) {
        EpisodeResponse created = episodeService.create(patientId, request);
        return ResponseEntity.created(URI.create("/api/episodes/" + created.id())).body(created);
    }

    @GetMapping("/patients/{patientId}/episodes")
    public List<EpisodeResponse> getForPatient(@PathVariable Long patientId) {
        return episodeService.getForPatient(patientId);
    }

    @GetMapping("/episodes/{id}")
    public EpisodeResponse getById(@PathVariable Long id) {
        return episodeService.getById(id);
    }

    @PutMapping("/episodes/{id}")
    public EpisodeResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEpisodeRequest request) {
        return episodeService.update(id, request);
    }

    @PatchMapping("/episodes/{id}/close")
    public EpisodeResponse close(@PathVariable Long id, @Valid @RequestBody CloseEpisodeRequest request) {
        return episodeService.close(id, request);
    }
}
