package com.virusxar.physiobook.controllers;

import com.virusxar.physiobook.domain.dto.history.PatientHistoryResponse;
import com.virusxar.physiobook.services.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/api/patients/{patientId}/history")
    public PatientHistoryResponse getHistory(@PathVariable Long patientId) {
        return historyService.getHistory(patientId);
    }
}
