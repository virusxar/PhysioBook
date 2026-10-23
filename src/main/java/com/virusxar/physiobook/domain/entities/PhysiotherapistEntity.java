package com.virusxar.physiobook.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "physiotherapist")
public class PhysiotherapistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    private String firstName;

    private String lastName;

    private String phone;

    private String licenseNumber;

    private String specialization;

    @Column(columnDefinition = "text")
    private String bio;
}
