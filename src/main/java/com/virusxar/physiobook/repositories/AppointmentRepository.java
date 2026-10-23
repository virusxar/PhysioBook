package com.virusxar.physiobook.repositories;

import com.virusxar.physiobook.domain.entities.AppointmentEntity;
import com.virusxar.physiobook.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    @Query("""
            select count(a) > 0 from AppointmentEntity a
            where a.physiotherapistEntity.id = :physiotherapistId
              and a.status in :activeStatuses
              and a.startTime < :end
              and a.endTime > :start
            """)
    boolean existsOverlapping(@Param("physiotherapistId") Long physiotherapistId,
                              @Param("start") OffsetDateTime start,
                              @Param("end") OffsetDateTime end,
                              @Param("activeStatuses") Collection<AppointmentStatus> activeStatuses);

    List<AppointmentEntity> findByPatientEntityIdOrderByStartTimeDesc(Long patientId);

    List<AppointmentEntity> findByPhysiotherapistEntityIdAndStartTimeBetweenOrderByStartTime(Long physiotherapistId, OffsetDateTime from, OffsetDateTime to);

    List<AppointmentEntity> findByPatientEntityIdAndStatus(Long patientId, AppointmentStatus status);
}
