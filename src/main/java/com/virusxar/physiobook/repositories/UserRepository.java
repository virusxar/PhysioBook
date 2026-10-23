package com.virusxar.physiobook.repositories;

import com.virusxar.physiobook.domain.entities.UserEntity;
import com.virusxar.physiobook.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(Role role);
}