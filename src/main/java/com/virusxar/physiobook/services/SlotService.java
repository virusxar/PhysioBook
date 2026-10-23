package com.virusxar.physiobook.services;

import com.virusxar.physiobook.domain.dto.availability.SlotResponse;
import com.virusxar.physiobook.domain.entities.AppointmentEntity;
import com.virusxar.physiobook.domain.entities.AvailabilityEntity;
import com.virusxar.physiobook.domain.enums.AppointmentStatus;
import com.virusxar.physiobook.exceptions.NotFoundException;
import com.virusxar.physiobook.repositories.AppointmentRepository;
import com.virusxar.physiobook.repositories.AvailabilityRepository;
import com.virusxar.physiobook.repositories.PhysiotherapistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SlotService {

    private static final ZoneId CLINIC_ZONE = ZoneId.of("Europe/Warsaw");
    private static final Set<AppointmentStatus> ACTIVE_STATUSES =
            Set.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED);

    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final PhysiotherapistRepository physiotherapistRepository;

    @Transactional(readOnly = true)
    public List<SlotResponse> getFreeSlots(Long physiotherapistId, LocalDate date) {
        if (!physiotherapistRepository.existsById(physiotherapistId)) {
            throw new NotFoundException("Nie znaleziono fizjoterapeuty o id " + physiotherapistId);
        }

        short dayOfWeek = (short) date.getDayOfWeek().getValue();
        List<AvailabilityEntity> workingHours =
                availabilityRepository.findByPhysiotherapistEntityIdAndDayOfWeek(physiotherapistId, dayOfWeek);

        OffsetDateTime dayStart = date.atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(CLINIC_ZONE).toOffsetDateTime();
        List<AppointmentEntity> booked = appointmentRepository
                .findByPhysiotherapistEntityIdAndStartTimeBetweenOrderByStartTime(physiotherapistId, dayStart, dayEnd)
                .stream()
                .filter(a -> ACTIVE_STATUSES.contains(a.getStatus()))
                .toList();

        OffsetDateTime now = OffsetDateTime.now();
        List<SlotResponse> slots = new ArrayList<>();

        for (AvailabilityEntity hours : workingHours) {
            LocalDateTime slotStart = date.atTime(hours.getStartTime());
            LocalDateTime workEnd = date.atTime(hours.getEndTime());

            while (!slotStart.plusMinutes(hours.getSlotMinutes()).isAfter(workEnd)) {
                LocalDateTime slotEnd = slotStart.plusMinutes(hours.getSlotMinutes());
                OffsetDateTime start = toClinicTime(slotStart);
                OffsetDateTime end = toClinicTime(slotEnd);

                if (start.isAfter(now) && end.isAfter(start) && !overlapsAny(start, end, booked)) {
                    slots.add(new SlotResponse(start, end));
                }
                slotStart = slotEnd;
            }
        }

        slots.sort(Comparator.comparing(SlotResponse::start));
        return slots;
    }

    private OffsetDateTime toClinicTime(LocalDateTime localDateTime) {
        return localDateTime.atZone(CLINIC_ZONE).toOffsetDateTime();
    }

    private boolean overlapsAny(OffsetDateTime start, OffsetDateTime end, List<AppointmentEntity> appointments) {
        for (AppointmentEntity a : appointments) {
            if (start.isBefore(a.getEndTime()) && end.isAfter(a.getStartTime())) {
                return true;
            }
        }
        return false;
    }
}
