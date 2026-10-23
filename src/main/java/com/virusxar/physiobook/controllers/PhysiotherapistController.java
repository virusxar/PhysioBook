package com.virusxar.physiobook.controllers;

import com.virusxar.physiobook.domain.dto.physiotherapist.CreatePhysiotherapistRequest;
import com.virusxar.physiobook.domain.dto.physiotherapist.PhysiotherapistResponse;
import com.virusxar.physiobook.domain.dto.physiotherapist.UpdatePhysiotherapistRequest;
import com.virusxar.physiobook.services.PhysiotherapistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/physiotherapists")
@RequiredArgsConstructor
public class PhysiotherapistController {

    private final PhysiotherapistService physiotherapistService;

    @PostMapping
    public ResponseEntity<PhysiotherapistResponse> create(@Valid @RequestBody CreatePhysiotherapistRequest request){
        PhysiotherapistResponse created = physiotherapistService.create(request);
        return ResponseEntity.created(URI.create("/api/physiotherapist/" + created.id())).body(created);
    }

    @GetMapping
    public List<PhysiotherapistResponse> search(@RequestParam(required = false) String lastName){
        return physiotherapistService.search(lastName);
    }

    @GetMapping("/{id}")
    public PhysiotherapistResponse getById(@PathVariable Long id) {
        return physiotherapistService.getById(id);
    }

    @PutMapping("/{id}")
    public PhysiotherapistResponse update(@PathVariable Long id, @Valid @RequestBody UpdatePhysiotherapistRequest request) {
        return physiotherapistService.update(id, request);
    }

    @GetMapping("/license/{licenseNumber}")
    public PhysiotherapistResponse getByLicenseNumber(@PathVariable String licenseNumber) {
        return physiotherapistService.getByLicenseNumber(licenseNumber);
    }
}
