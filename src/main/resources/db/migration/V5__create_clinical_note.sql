CREATE TABLE clinical_note (
                               id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               patient_id         BIGINT      NOT NULL REFERENCES patient (id),
                               author_id          BIGINT      NOT NULL REFERENCES physiotherapist (id),
                               appointment_id     BIGINT      REFERENCES appointment (id),
                               episode_id         BIGINT      REFERENCES treatment_episode (id),
                               type               VARCHAR(20) NOT NULL DEFAULT 'OTHER'
                                   CHECK (type IN ('INTERVIEW', 'EXAMINATION', 'TREATMENT', 'RECOMMENDATION', 'OTHER')),
                               content            TEXT        NOT NULL CHECK (length(content) BETWEEN 1 AND 10000),
                               visible_to_patient BOOLEAN     NOT NULL DEFAULT FALSE,
                               created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                               updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_note_patient     ON clinical_note (patient_id, created_at DESC);
CREATE INDEX ix_note_appointment ON clinical_note (appointment_id);