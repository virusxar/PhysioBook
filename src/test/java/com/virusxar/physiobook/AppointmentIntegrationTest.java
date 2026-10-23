package com.virusxar.physiobook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AppointmentIntegrationTest extends IntegrationTestBase {

    private Account physio;
    private Account patient;
    private LocalDate monday;

    // Fresh physiotherapist (Mon 08:00-12:00, 60 min slots = 4 slots) and patient for every test
    @BeforeEach
    void setUp() throws Exception {
        physio = createPhysiotherapist();
        patient = registerPatient();
        monday = nextMonday();
        setWorkingHours(physio, DayOfWeek.MONDAY, "08:00", "12:00", 60);
    }

    @Test
    void patientBooksOwnAppointmentWithinWorkingHours() throws Exception {
        book(patient, patient.profileId(), physio.profileId(), at(monday, "10:00"), 60)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andExpect(jsonPath("$.patientId").value(patient.profileId()));
    }

    @Test
    void bookedSlotDisappearsFromFreeSlots() throws Exception {
        String slotsUrl = "/api/physiotherapists/" + physio.profileId() + "/slots?date=" + monday;

        send(get(slotsUrl), null, null).andExpect(jsonPath("$", hasSize(4)));
        book(patient, patient.profileId(), physio.profileId(), at(monday, "10:00"), 60)
                .andExpect(status().isCreated());
        send(get(slotsUrl), null, null).andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void overlappingAppointmentReturns409() throws Exception {
        book(patient, patient.profileId(), physio.profileId(), at(monday, "10:00"), 60)
                .andExpect(status().isCreated());

        Account otherPatient = registerPatient();
        book(otherPatient, otherPatient.profileId(), physio.profileId(), at(monday, "10:30"), 60)
                .andExpect(status().isConflict());
    }

    @Test
    void appointmentOutsideWorkingHoursReturns400() throws Exception {
        book(patient, patient.profileId(), physio.profileId(), at(monday, "13:00"), 60)
                .andExpect(status().isBadRequest());
    }

    @Test
    void patientCannotBookForAnotherPatient() throws Exception {
        Account otherPatient = registerPatient();

        book(patient, otherPatient.profileId(), physio.profileId(), at(monday, "10:00"), 60)
                .andExpect(status().isForbidden());
    }

    @Test
    void physiotherapistConfirmsButPatientCannot() throws Exception {
        Long appointmentId = readId(
                book(patient, patient.profileId(), physio.profileId(), at(monday, "10:00"), 60)
                        .andExpect(status().isCreated()),
                "$.id");

        send(patch("/api/appointments/" + appointmentId + "/confirm"), patient.token(), null)
                .andExpect(status().isForbidden());
        send(patch("/api/appointments/" + appointmentId + "/confirm"), physio.token(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelledAppointmentFreesTheSlot() throws Exception {
        Long appointmentId = readId(
                book(patient, patient.profileId(), physio.profileId(), at(monday, "10:00"), 60)
                        .andExpect(status().isCreated()),
                "$.id");

        send(patch("/api/appointments/" + appointmentId + "/cancel"), patient.token(), """
                { "reason": "Choroba" }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Account otherPatient = registerPatient();
        book(otherPatient, otherPatient.profileId(), physio.profileId(), at(monday, "10:00"), 60)
                .andExpect(status().isCreated());
    }
}