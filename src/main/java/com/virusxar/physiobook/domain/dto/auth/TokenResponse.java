package com.virusxar.physiobook.domain.dto.auth;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {}
