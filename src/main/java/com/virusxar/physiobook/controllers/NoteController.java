package com.virusxar.physiobook.controllers;

import com.virusxar.physiobook.domain.dto.note.CreateNoteRequest;
import com.virusxar.physiobook.domain.dto.note.NoteResponse;
import com.virusxar.physiobook.domain.dto.note.UpdateNoteRequest;
import com.virusxar.physiobook.services.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/patients/{patientId}/notes")
    public ResponseEntity<NoteResponse> create(@PathVariable Long patientId,
                                               @Valid @RequestBody CreateNoteRequest request) {
        NoteResponse created = noteService.create(patientId, request);
        return ResponseEntity.created(URI.create("/api/notes/" + created.id())).body(created);
    }

    @GetMapping("/patients/{patientId}/notes")
    public List<NoteResponse> getForPatient(@PathVariable Long patientId,
                                            @RequestParam(required = false) Long episodeId) {
        return noteService.getForPatient(patientId, episodeId);
    }

    @PutMapping("/notes/{id}")
    public NoteResponse update(@PathVariable Long id, @Valid @RequestBody UpdateNoteRequest request) {
        return noteService.update(id, request);
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
