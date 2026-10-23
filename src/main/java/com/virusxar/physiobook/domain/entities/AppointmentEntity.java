package com.virusxar.physiobook.domain.entities;

import com.virusxar.physiobook.domain.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "appointment")

public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id")
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physiotherapist_id")
    private PhysiotherapistEntity physiotherapistEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private TreatmentEpisodeEntity episodeEntity;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    private String reason;
    private String cancellationReason;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public void confirm() {
        requireStatus(AppointmentStatus.BOOKED);
        this.status = AppointmentStatus.CONFIRMED;
    }

    public void complete() {
        requireStatus(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED);
        this.status = AppointmentStatus.COMPLETED;
    }

    public void markNoShow() {
        requireStatus(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED);
        this.status = AppointmentStatus.NO_SHOW;
    }

    public void cancel(String reason) {
        requireStatus(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED);
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    private void requireStatus(AppointmentStatus... allowed) {
        for (AppointmentStatus s : allowed) {
            if (status == s) {
                return;
            }
        }
        throw new IllegalStateException("Operacja niedozwolona dla wizyty w statusie " + status);
    }

}
