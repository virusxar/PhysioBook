package com.virusxar.physiobook.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "availability")
public class AvailabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physiotherapist_id")
    private PhysiotherapistEntity physiotherapistEntity;

    private short dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    @Builder.Default
    private int slotMinutes = 60;
}
