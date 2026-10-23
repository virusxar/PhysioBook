package com.virusxar.physiobook.controllers;

import com.virusxar.physiobook.domain.dto.availability.AvailabilityResponse;
import com.virusxar.physiobook.domain.dto.availability.SlotResponse;
import com.virusxar.physiobook.domain.dto.availability.UpdateAvailabilityRequest;
import com.virusxar.physiobook.services.AvailabilityService;
import com.virusxar.physiobook.services.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/physiotherapists/{physiotherapistId}")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final SlotService slotService;

    @GetMapping("/availability")
    public List<AvailabilityResponse> get(@PathVariable Long physiotherapistId) {
        return availabilityService.get(physiotherapistId);
    }

    @PutMapping("/availability")
    public List<AvailabilityResponse> replace(@PathVariable Long physiotherapistId,
                                              @Valid @RequestBody UpdateAvailabilityRequest request) {
        return availabilityService.replace(physiotherapistId, request);
    }

    @GetMapping("/slots")
    public List<SlotResponse> getFreeSlots(@PathVariable Long physiotherapistId,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return slotService.getFreeSlots(physiotherapistId, date);
    }
}
