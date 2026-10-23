package com.virusxar.physiobook.domain.dto.auth;

import com.virusxar.physiobook.domain.enums.Role;

public record UserResponse(
        Long id,
        String email,
        Role role,
        Long profileId
) {}
