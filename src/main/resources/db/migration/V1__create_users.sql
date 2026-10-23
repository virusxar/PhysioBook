CREATE TABLE app_user (
                          id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          email         VARCHAR(255) NOT NULL,
                          password_hash VARCHAR(100) NOT NULL,
                          role          VARCHAR(20)  NOT NULL CHECK (role IN ('PATIENT', 'PHYSIOTHERAPIST', 'ADMIN')),
                          enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
                          created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- e-mail unikalny bez względu na wielkość liter
CREATE UNIQUE INDEX ux_app_user_email ON app_user (lower(email));