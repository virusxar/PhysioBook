package com.virusxar.physiobook.domain.entities;

import com.virusxar.physiobook.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "app_user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private Boolean enabled = true;
}
