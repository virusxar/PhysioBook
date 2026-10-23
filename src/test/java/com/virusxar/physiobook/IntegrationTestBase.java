package com.virusxar.physiobook;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
abstract class IntegrationTestBase {

    protected static final ZoneId CLINIC_ZONE = ZoneId.of("Europe/Warsaw");
    protected static final String PASSWORD = "Haslo1234";

    @Autowired
    protected MockMvc mockMvc;

    protected record Account(Long profileId, String token) {}

    protected static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@test.pl";
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected ResultActions send(MockHttpServletRequestBuilder request, String token, String json) throws Exception {
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(request);
    }

    protected static Long readId(ResultActions result, String path) throws Exception {
        Number id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
        return id.longValue();
    }

    protected String login(String email, String password) throws Exception {
        String body = send(post("/api/auth/login"), null, """
                { "email": "%s", "password": "%s" }
                """.formatted(email, password))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    protected String adminToken() throws Exception {
        return login("admin@fizjobook.local", "admin123!");
    }

    protected Account registerPatient() throws Exception {
        String email = uniqueEmail("patient");
        ResultActions result = send(post("/api/auth/register"), null, """
                { "email": "%s", "password": "%s", "firstName": "Anna", "lastName": "Testowa" }
                """.formatted(email, PASSWORD))
                .andExpect(status().isCreated());
        return new Account(readId(result, "$.profileId"), login(email, PASSWORD));
    }

    protected Account createPhysiotherapist() throws Exception {
        String email = uniqueEmail("physio");
        String licenseNumber = "PWZF-" + UUID.randomUUID().toString().substring(0, 8);
        ResultActions result = send(post("/api/physiotherapists"), adminToken(), """
                { "email": "%s", "password": "%s", "firstName": "Tomasz", "lastName": "Testowy",
                  "specialization": "Ortopedia", "licenseNumber": "%s" }
                """.formatted(email, PASSWORD, licenseNumber))
                .andExpect(status().isCreated());
        return new Account(readId(result, "$.id"), login(email, PASSWORD));
    }

    protected void setWorkingHours(Account physio, DayOfWeek day, String from, String to, int slotMinutes) throws Exception {
        send(put("/api/physiotherapists/" + physio.profileId() + "/availability"), physio.token(), """
                { "entries": [ { "dayOfWeek": "%s", "startTime": "%s", "endTime": "%s", "slotMinutes": %d } ] }
                """.formatted(day, from, to, slotMinutes))
                .andExpect(status().isOk());
    }

    protected static LocalDate nextMonday() {
        return LocalDate.now(CLINIC_ZONE).plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    protected static OffsetDateTime at(LocalDate date, String time) {
        return date.atTime(LocalTime.parse(time)).atZone(CLINIC_ZONE).toOffsetDateTime();
    }

    protected ResultActions book(Account as, Long patientId, Long physioId, OffsetDateTime start, int minutes) throws Exception {
        return send(post("/api/appointments"), as.token(), """
                { "patientId": %d, "physiotherapistId": %d, "startTime": "%s", "durationMinutes": %d }
                """.formatted(patientId, physioId, start, minutes));
    }
}