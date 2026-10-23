package com.virusxar.physiobook.domain.entities;

import com.virusxar.physiobook.domain.enums.AppointmentStatus;
import com.virusxar.physiobook.domain.enums.EpisodeStatus;
import com.virusxar.physiobook.exceptions.BadRequestException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "treatment_episode")
public class TreatmentEpisodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id")
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physiotherapist_id")
    private PhysiotherapistEntity physiotherapistEntity;

    private String title;

    @Column(columnDefinition = "text")
    private String diagnosis;

    @Column(columnDefinition = "text")
    private String goal;

    @Builder.Default
    private LocalDate startDate = LocalDate.now();

    private LocalDate endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EpisodeStatus episodeStatus = EpisodeStatus.ACTIVE;

    public boolean isActive() {
        return episodeStatus == EpisodeStatus.ACTIVE;
    }

    public void close(EpisodeStatus newStatus, LocalDate endDate) {
        if (!isActive()) {
            throw new IllegalStateException("Epizod jest już zamknięty (status " + episodeStatus + ")");
        }
        if (newStatus == EpisodeStatus.ACTIVE) {
            throw new BadRequestException("Epizod można zamknąć tylko jako COMPLETED albo ABANDONED");
        }
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        if (end.isBefore(startDate)) {
            throw new BadRequestException("Data zakończenia nie może być wcześniejsza niż data rozpoczęcia");
        }
        this.episodeStatus = newStatus;
        this.endDate = end;
    }
}