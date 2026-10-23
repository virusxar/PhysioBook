CREATE TABLE physiotherapist (
                                 id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 user_id         BIGINT       NOT NULL UNIQUE REFERENCES app_user (id),
                                 first_name      VARCHAR(100) NOT NULL,
                                 last_name       VARCHAR(100) NOT NULL,
                                 phone           VARCHAR(30),
                                 license_number  VARCHAR(30)  UNIQUE,   -- numer prawa wykonywania zawodu
                                 specialization  VARCHAR(150),
                                 bio             TEXT
);

CREATE TABLE availability (
                              id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              physiotherapist_id BIGINT   NOT NULL REFERENCES physiotherapist (id) ON DELETE CASCADE,
                              day_of_week        SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
                              start_time         TIME     NOT NULL,
                              end_time           TIME     NOT NULL,
                              slot_minutes       INT      NOT NULL DEFAULT 60 CHECK (slot_minutes BETWEEN 15 AND 240),
                              CHECK (end_time > start_time)
);

CREATE INDEX ix_availability_physio ON availability (physiotherapist_id, day_of_week);

CREATE TABLE patient (
                         id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         user_id     BIGINT       UNIQUE REFERENCES app_user (id), -- NULL = pacjent bez konta
                         first_name  VARCHAR(100) NOT NULL,
                         last_name   VARCHAR(100) NOT NULL,
                         birth_date  DATE,
                         phone       VARCHAR(30),
                         email       VARCHAR(255),
                         created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_patient_last_name ON patient (lower(last_name));