package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.auth.*;
import com.virusxar.physiobook.domain.entities.PatientEntity;
import com.virusxar.physiobook.domain.entities.PhysiotherapistEntity;
import com.virusxar.physiobook.domain.entities.UserEntity;
import com.virusxar.physiobook.domain.enums.Role;
import com.virusxar.physiobook.exceptions.ConflictException;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.exceptions.UnauthorizedException;
import com.virusxar.physiobook.repositories.PatientRepository;
import com.virusxar.physiobook.repositories.PhysiotherapistRepository;
import com.virusxar.physiobook.repositories.UserRepository;
import com.virusxar.physiobook.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse registerPatient(RegisterPatientRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Konto z tym adresem e-mail już istnieje");
        }

        UserEntity user = userRepository.save(UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.PATIENT)
                .build());

        PatientEntity patient = patientRepository.save(PatientEntity.builder()
                .userEntity(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .phone(request.phone())
                .email(request.email())
                .build());

        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), patient.getId());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Niepoprawny e-mail lub hasło"));

        return jwtService.createToken(user, findProfileId(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika o id " + userId));
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), findProfileId(user));
    }

    @Transactional
    public UserResponse createPhysiotherapistAccount(Long physiotherapistId, CreateAccountRequest request) {
        PhysiotherapistEntity physiotherapist = physiotherapistRepository.findById(physiotherapistId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono fizjoterapeuty o id " + physiotherapistId));
        if (physiotherapist.getUserEntity() != null) {
            throw new ConflictException("Ten fizjoterapeuta ma już konto");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Konto z tym adresem e-mail już istnieje");
        }

        UserEntity user = userRepository.save(UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.PHYSIOTHERAPIST)
                .build());
        physiotherapist.setUserEntity(user);

        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), physiotherapist.getId());
    }

    private Long findProfileId(UserEntity user) {
        return switch (user.getRole()) {
            case PATIENT -> patientRepository.findByUserEntityId(user.getId()).map(PatientEntity::getId).orElse(null);
            case PHYSIOTHERAPIST -> physiotherapistRepository.findByUserEntityId(user.getId()).map(PhysiotherapistEntity::getId).orElse(null);
            case ADMIN -> null;
        };
    }
}
