package com.virusxar.physiobook.demo;

import com.virusxar.physiobook.domain.entities.AppointmentEntity;
import com.virusxar.physiobook.domain.entities.AvailabilityEntity;
import com.virusxar.physiobook.domain.entities.ClinicalNoteEntity;
import com.virusxar.physiobook.domain.entities.PatientEntity;
import com.virusxar.physiobook.domain.entities.PhysiotherapistEntity;
import com.virusxar.physiobook.domain.entities.TreatmentEpisodeEntity;
import com.virusxar.physiobook.domain.entities.UserEntity;
import com.virusxar.physiobook.domain.enums.AppointmentStatus;
import com.virusxar.physiobook.domain.enums.NoteType;
import com.virusxar.physiobook.domain.enums.Role;
import com.virusxar.physiobook.repositories.AppointmentRepository;
import com.virusxar.physiobook.repositories.AvailabilityRepository;
import com.virusxar.physiobook.repositories.ClinicalNoteRepository;
import com.virusxar.physiobook.repositories.PatientRepository;
import com.virusxar.physiobook.repositories.PhysiotherapistRepository;
import com.virusxar.physiobook.repositories.TreatmentEpisodeRepository;
import com.virusxar.physiobook.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    public static final String DEMO_PASSWORD = "Demo1234";

    private static final String MARKER_EMAIL = "katarzyna.mazur@fizjobook.demo";
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Europe/Warsaw");
    private static final List<DayOfWeek> WORKDAYS = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private final UserRepository userRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final PatientRepository patientRepository;
    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final TreatmentEpisodeRepository episodeRepository;
    private final ClinicalNoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase(MARKER_EMAIL)) {
            return;
        }

        // Physiotherapists with their weekly working hours
        PhysiotherapistEntity mazur = physiotherapist("Katarzyna", "Mazur", MARKER_EMAIL, "Ortopedia", "PWZF-DEMO-1");
        workingHours(mazur, WORKDAYS, "08:00", "16:00", 45);

        PhysiotherapistEntity kaczmarek = physiotherapist("Michał", "Kaczmarek", "michal.kaczmarek@fizjobook.demo",
                "Neurologia", "PWZF-DEMO-2");
        workingHours(kaczmarek, List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), "10:00", "18:00", 60);

        PhysiotherapistEntity zajac = physiotherapist("Agnieszka", "Zając", "agnieszka.zajac@fizjobook.demo",
                "Terapia manualna", "PWZF-DEMO-3");
        // Two ranges a day: before and after the lunch break
        workingHours(zajac, List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), "08:00", "12:00", 30);
        workingHours(zajac, List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), "13:00", "17:00", 30);

        // Patients
        PatientEntity lewandowski = patient("Piotr", "Lewandowski", "piotr.lewandowski@fizjobook.demo", "1985-03-14");
        PatientEntity wojcik = patient("Magdalena", "Wójcik", "magdalena.wojcik@fizjobook.demo", "1992-07-02");
        PatientEntity kaminski = patient("Krzysztof", "Kamiński", "krzysztof.kaminski@fizjobook.demo", "1978-11-23");
        patient("Joanna", "Dąbrowska", "joanna.dabrowska@fizjobook.demo", "2001-01-30");

        // Magdalena's treatment history: an episode, a completed visit and two notes
        LocalDate today = LocalDate.now(CLINIC_ZONE);
        TreatmentEpisodeEntity episode = episodeRepository.save(TreatmentEpisodeEntity.builder()
                .patientEntity(wojcik)
                .physiotherapistEntity(mazur)
                .title("Rehabilitacja po rekonstrukcji ACL")
                .diagnosis("Stan po rekonstrukcji więzadła krzyżowego przedniego, kolano prawe")
                .goal("Pełny zakres ruchu i powrót do biegania")
                .startDate(today.minusWeeks(3))
                .build());

        AppointmentEntity firstVisit = appointment(wojcik, mazur, episode, today.minusWeeks(1), "09:00", 45,
                "Pierwsza wizyta po operacji", AppointmentStatus.COMPLETED);

        note(wojcik, mazur, episode, firstVisit, NoteType.EXAMINATION,
                "Zakres zgięcia 95°, niewielki obrzęk, blizna bez cech zapalenia.", false);
        note(wojcik, mazur, episode, firstVisit, NoteType.RECOMMENDATION,
                "Ćwiczenia izometryczne mięśnia czworogłowego 3x dziennie, lód po ćwiczeniach.", true);

        // Upcoming appointments on the next working day
        LocalDate nextWorkday = nextWorkday(today);
        appointment(lewandowski, mazur, null, nextWorkday, "08:00", 45,
                "Ból odcinka lędźwiowego", AppointmentStatus.CONFIRMED);
        appointment(wojcik, mazur, episode, nextWorkday, "09:30", 45,
                "Kontrola po rekonstrukcji ACL", AppointmentStatus.BOOKED);
        appointment(kaminski, mazur, null, nextWorkday, "11:00", 45,
                "Ból barku", AppointmentStatus.BOOKED);

        log.info("Added demo data: 3 physiotherapists, 4 patients (password for all: {})", DEMO_PASSWORD);
    }

    private PhysiotherapistEntity physiotherapist(String firstName, String lastName, String email,
                                                  String specialization, String licenseNumber) {
        return physiotherapistRepository.save(PhysiotherapistEntity.builder()
                .userEntity(user(email, Role.PHYSIOTHERAPIST))
                .firstName(firstName)
                .lastName(lastName)
                .specialization(specialization)
                .licenseNumber(licenseNumber)
                .build());
    }

    private PatientEntity patient(String firstName, String lastName, String email, String birthDate) {
        return patientRepository.save(PatientEntity.builder()
                .userEntity(user(email, Role.PATIENT))
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .birthDate(LocalDate.parse(birthDate))
                .build());
    }

    private UserEntity user(String email, Role role) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .role(role)
                .build());
    }

    private void workingHours(PhysiotherapistEntity physiotherapist, List<DayOfWeek> days,
                              String from, String to, int slotMinutes) {
        for (DayOfWeek day : days) {
            availabilityRepository.save(AvailabilityEntity.builder()
                    .physiotherapistEntity(physiotherapist)
                    .dayOfWeek((short) day.getValue())
                    .startTime(LocalTime.parse(from))
                    .endTime(LocalTime.parse(to))
                    .slotMinutes(slotMinutes)
                    .build());
        }
    }

    private AppointmentEntity appointment(PatientEntity patient, PhysiotherapistEntity physiotherapist,
                                          TreatmentEpisodeEntity episode, LocalDate date, String time,
                                          int minutes, String reason, AppointmentStatus status) {
        OffsetDateTime start = date.atTime(LocalTime.parse(time)).atZone(CLINIC_ZONE).toOffsetDateTime();
        return appointmentRepository.save(AppointmentEntity.builder()
                .patientEntity(patient)
                .physiotherapistEntity(physiotherapist)
                .episodeEntity(episode)
                .startTime(start)
                .endTime(start.plusMinutes(minutes))
                .reason(reason)
                .status(status)
                .build());
    }

    private void note(PatientEntity patient, PhysiotherapistEntity author, TreatmentEpisodeEntity episode,
                      AppointmentEntity appointment, NoteType type, String content, boolean visibleToPatient) {
        noteRepository.save(ClinicalNoteEntity.builder()
                .patientEntity(patient)
                .physiotherapistEntity(author)
                .episodeEntity(episode)
                .appointmentEntity(appointment)
                .type(type)
                .content(content)
                .visibleToPatient(visibleToPatient)
                .build());
    }

    private static LocalDate nextWorkday(LocalDate from) {
        LocalDate day = from.plusDays(1);
        while (!WORKDAYS.contains(day.getDayOfWeek())) {
            day = day.plusDays(1);
        }
        return day;
    }
}