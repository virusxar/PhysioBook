CREATE TABLE treatment_episode (
                                   id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   patient_id         BIGINT       NOT NULL REFERENCES patient (id),
                                   physiotherapist_id BIGINT       NOT NULL REFERENCES physiotherapist (id),
                                   title              VARCHAR(200) NOT NULL,
                                   diagnosis          TEXT,
                                   goal               TEXT,
                                   start_date         DATE         NOT NULL DEFAULT current_date,
                                   end_date           DATE,
                                   status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                                       CHECK (status IN ('ACTIVE', 'COMPLETED', 'ABANDONED')),
                                   CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX ix_episode_patient ON treatment_episode (patient_id);

CREATE TABLE appointment (
                             id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             patient_id          BIGINT       NOT NULL REFERENCES patient (id),
                             physiotherapist_id  BIGINT       NOT NULL REFERENCES physiotherapist (id),
                             episode_id          BIGINT       REFERENCES treatment_episode (id),
                             start_time          TIMESTAMPTZ  NOT NULL,
                             end_time            TIMESTAMPTZ  NOT NULL,
                             status              VARCHAR(20)  NOT NULL DEFAULT 'BOOKED'
                                 CHECK (status IN ('BOOKED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
                             reason              VARCHAR(500),
                             cancellation_reason VARCHAR(500),
                             version             BIGINT       NOT NULL DEFAULT 0,
                             created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
                             updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
                             CHECK (end_time > start_time)
);

CREATE INDEX ix_appointment_patient ON appointment (patient_id, start_time);
CREATE INDEX ix_appointment_physio  ON appointment (physiotherapist_id, start_time);