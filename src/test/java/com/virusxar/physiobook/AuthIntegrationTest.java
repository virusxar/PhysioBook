package com.virusxar.physiobook;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void registeredPatientCanLogInAndReadOwnAccount() throws Exception {
        Account patient = registerPatient();

        send(get("/api/auth/me"), patient.token(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.profileId").value(patient.profileId()));
    }

    @Test
    void registerWithSameEmailInDifferentCaseReturns409() throws Exception {
        String email = uniqueEmail("dup");
        String json = """
                { "email": "%s", "password": "%s", "firstName": "Anna", "lastName": "Testowa" }
                """;

        send(post("/api/auth/register"), null, json.formatted(email, PASSWORD))
                .andExpect(status().isCreated());
        send(post("/api/auth/register"), null, json.formatted(email.toUpperCase(), PASSWORD))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        send(post("/api/auth/login"), null, """
                { "email": "admin@fizjobook.local", "password": "zlehaslo123" }
                """)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        send(get("/api/auth/me"), null, null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithInvalidTokenReturns401() throws Exception {
        send(get("/api/auth/me"), "not-a-real-token", null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCannotListAllPatients() throws Exception {
        Account patient = registerPatient();

        send(get("/api/patients"), patient.token(), null)
                .andExpect(status().isForbidden());
    }

    @Test
    void createdPhysiotherapistCanLogIn() throws Exception {
        Account physio = createPhysiotherapist();

        send(get("/api/auth/me"), physio.token(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PHYSIOTHERAPIST"))
                .andExpect(jsonPath("$.profileId").value(physio.profileId()));
    }
}