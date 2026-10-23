package com.virusxar.physiobook.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AccessGuard {

    public boolean isPatient() {
        return hasRole("PATIENT");
    }

    public boolean isPhysiotherapist() {
        return hasRole("PHYSIOTHERAPIST");
    }

    public Long currentProfileId() {
        Object value = currentJwt().getClaim("profileId");
        return value == null ? null : ((Number) value).longValue();
    }

    public void checkPatientAccess(Long patientId) {
        if (isPatient() && !patientId.equals(currentProfileId())) {
            throw new AccessDeniedException("Brak dostępu do danych innego pacjenta");
        }
    }

    public void checkPhysiotherapistAccess(Long physiotherapistId) {
        if (isPhysiotherapist() && !physiotherapistId.equals(currentProfileId())) {
            throw new AccessDeniedException("Możesz zmieniać tylko swoje dane");
        }
    }

    private boolean hasRole(String role) {
        return currentAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private Jwt currentJwt() {
        return (Jwt) currentAuthentication().getPrincipal();
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
