package com.virusxar.physiobook.controllers;

import com.virusxar.physiobook.domain.dto.auth.*;
import com.virusxar.physiobook.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterPatientRequest request) {
        return authService.registerPatient(request);
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/auth/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.me(Long.valueOf(jwt.getSubject()));
    }

    @PostMapping("/physiotherapists/{physiotherapistId}/account")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createPhysiotherapistAccount(@PathVariable Long physiotherapistId,
                                                     @Valid @RequestBody CreateAccountRequest request) {
        return authService.createPhysiotherapistAccount(physiotherapistId, request);
    }
}