package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.physiotherapist.CreatePhysiotherapistRequest;
import com.virusxar.physiobook.domain.dto.physiotherapist.PhysiotherapistResponse;
import com.virusxar.physiobook.domain.dto.physiotherapist.UpdatePhysiotherapistRequest;
import com.virusxar.physiobook.domain.entities.PhysiotherapistEntity;
import com.virusxar.physiobook.domain.entities.UserEntity;
import com.virusxar.physiobook.domain.enums.Role;
import com.virusxar.physiobook.exceptions.ConflictException;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.mappers.PhysiotherapistMapper;
import com.virusxar.physiobook.repositories.PhysiotherapistRepository;
import com.virusxar.physiobook.repositories.UserRepository;
import com.virusxar.physiobook.security.AccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhysiotherapistService {
    private final PhysiotherapistRepository physiotherapistRepository;
    private final PhysiotherapistMapper physiotherapistMapper;
    private final AccessGuard accessGuard;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PhysiotherapistResponse create(CreatePhysiotherapistRequest request) {
        if (physiotherapistRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new ConflictException("Numer licencji " + request.licenseNumber() + " jest już zajęty");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Konto z tym adresem e-mail już istnieje");
        }

        UserEntity user = userRepository.save(UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.PHYSIOTHERAPIST)
                .build());

        PhysiotherapistEntity physiotherapist = physiotherapistMapper.toEntity(request);
        physiotherapist.setUserEntity(user);

        PhysiotherapistEntity saved = physiotherapistRepository.save(physiotherapist);
        return physiotherapistMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PhysiotherapistResponse getById(Long id){
        return physiotherapistMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<PhysiotherapistResponse> search(String lastName) {
        List<PhysiotherapistEntity> physiotherapists = (lastName == null || lastName.isBlank())
                ? physiotherapistRepository.findAll()
                : physiotherapistRepository.findByLastNameContainingIgnoreCase(lastName);
        return physiotherapists.stream().map(physiotherapistMapper::toResponse).toList();
    }

    @Transactional
    public PhysiotherapistResponse update(Long id, UpdatePhysiotherapistRequest request){
        accessGuard.checkPhysiotherapistAccess(id);
        PhysiotherapistEntity physiotherapist = findOrThrow(id);
        if (physiotherapistRepository.
                existsByLicenseNumberAndIdNot(request.licenseNumber(), id)) {
            throw new ConflictException("Numer licencji " + request.licenseNumber() + " jest już zajęty");
        }
        physiotherapistMapper.update(physiotherapist, request);
        return physiotherapistMapper.toResponse(physiotherapist);
    }

    @Transactional(readOnly = true)
    public PhysiotherapistResponse getByLicenseNumber(String licenseNumber) {
        return physiotherapistRepository.findByLicenseNumber(licenseNumber)
                .map(physiotherapistMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono fizjoterapeuty o numerze licencji " + licenseNumber));
    }

    private PhysiotherapistEntity findOrThrow(Long id) {
        return physiotherapistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono fizjoterapeuty o id " + id));
    }
}
