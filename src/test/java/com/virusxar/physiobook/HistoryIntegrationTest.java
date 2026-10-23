package com.virusxar.physiobook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HistoryIntegrationTest extends IntegrationTestBase {

    @Test
    void patientSeesOnlyNotesVisibleToPatient() throws Exception {
        Account physio = createPhysiotherapist();
        Account patient = registerPatient();
        String notesUrl = "/api/patients/" + patient.profileId() + "/notes";

        send(post(notesUrl), physio.token(), """
                { "type": "EXAMINATION", "content": "Notatka wewnętrzna" }
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorId").value(physio.profileId()));
        send(post(notesUrl), physio.token(), """
                { "type": "RECOMMENDATION", "content": "Ćwiczenia 2x dziennie", "visibleToPatient": true }
                """)
                .andExpect(status().isCreated());

        String historyUrl = "/api/patients/" + patient.profileId() + "/history";
        String notesInTimeline = "$.timeline[?(@.type == 'NOTE')]";

        send(get(historyUrl), physio.token(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath(notesInTimeline, hasSize(2)));
        send(get(historyUrl), patient.token(), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath(notesInTimeline + ".content", contains("Ćwiczenia 2x dziennie")));
    }

    @Test
    void patientCannotReadAnotherPatientsHistory() throws Exception {
        Account patient = registerPatient();
        Account otherPatient = registerPatient();

        send(get("/api/patients/" + otherPatient.profileId() + "/history"), patient.token(), null)
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotAddNotes() throws Exception {
        Account patient = registerPatient();

        send(post("/api/patients/" + patient.profileId() + "/notes"), patient.token(), """
                { "type": "OTHER", "content": "Próba" }
                """)
                .andExpect(status().isForbidden());
    }
}