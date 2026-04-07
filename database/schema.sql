-- Health Wallet — Database Schema
-- DRAW DB LINK: https://www.drawdb.app/editor/diagrams/4eb18d5e-5a55-4726-9ecc-c343f297331d

CREATE TABLE users (
    id                UUID          PRIMARY KEY,
    name              VARCHAR(100)  NOT NULL,
    email             VARCHAR(150)  NOT NULL UNIQUE,
    cpf               VARCHAR(11)   NOT NULL UNIQUE,
    password          VARCHAR(255)  NOT NULL,
    role              VARCHAR(10)   NOT NULL CHECK (role IN ('PATIENT', 'DOCTOR', 'ADMIN')),
    terms_accepted    BOOLEAN       NOT NULL DEFAULT false,
    email_confirmed   BOOLEAN       NOT NULL DEFAULT false,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMP
);

CREATE TABLE anamnesis (
    id                UUID       PRIMARY KEY,
    patient_id        UUID       NOT NULL,
    allergies         TEXT,
    chronic_diseases  TEXT,
    medications       TEXT,
    blood_type        VARCHAR(5),
    family_history    TEXT,
    observations      TEXT,
    created_at        TIMESTAMP  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP  NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMP,
    CONSTRAINT fk_anamnesis_patient FOREIGN KEY (patient_id) REFERENCES users(id)
);

CREATE TABLE anamnesis_history (
    id                UUID       PRIMARY KEY,
    anamnesis_id      UUID       NOT NULL,
    changed_by_id     UUID       NOT NULL,
    allergies         TEXT,
    chronic_diseases  TEXT,
    medications       TEXT,
    blood_type        VARCHAR(5),
    family_history    TEXT,
    observations      TEXT,
    changed_at        TIMESTAMP  NOT NULL DEFAULT NOW(),
    created_at        TIMESTAMP  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP  NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMP,
    CONSTRAINT fk_anamnesis_history_anamnesis  FOREIGN KEY (anamnesis_id)  REFERENCES anamnesis(id),
    CONSTRAINT fk_anamnesis_history_changed_by FOREIGN KEY (changed_by_id) REFERENCES users(id)
);

CREATE TABLE anamnesis_attachments (
    id            UUID          PRIMARY KEY,
    anamnesis_id  UUID          NOT NULL,
    file_name     VARCHAR(255)  NOT NULL,
    path          VARCHAR(500)  NOT NULL,
    type          VARCHAR(5)    NOT NULL CHECK (type IN ('PDF', 'JPG', 'PNG')),
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMP,
    CONSTRAINT fk_anamnesis_attachments_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamnesis(id)
);

CREATE TABLE vaccines (
    id                UUID          PRIMARY KEY,
    patient_id        UUID          NOT NULL,
    name              VARCHAR(100)  NOT NULL,
    manufacturer      VARCHAR(100),
    lot               VARCHAR(50),
    application_date  DATE          NOT NULL,
    dose              VARCHAR(10)   NOT NULL CHECK (dose IN ('FIRST', 'SECOND', 'BOOSTER')),
    proof             VARCHAR(500),
    observations      TEXT,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMP,
    CONSTRAINT fk_vaccines_patient FOREIGN KEY (patient_id) REFERENCES users(id)
);

CREATE TABLE appointments (
    id                   UUID          PRIMARY KEY,
    patient_id           UUID          NOT NULL,
    date                 TIMESTAMP     NOT NULL,
    specialty            VARCHAR(100)  NOT NULL,
    professional         VARCHAR(100)  NOT NULL,
    clinic               VARCHAR(100),
    summary              TEXT,
    prescription         TEXT,
    medical_observation  TEXT,
    created_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at           TIMESTAMP,
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES users(id)
);

CREATE TABLE shared_reports (
    id               UUID          PRIMARY KEY,
    patient_id       UUID          NOT NULL,
    doctor_id        UUID,
    token            UUID          NOT NULL UNIQUE,
    expires_at       TIMESTAMP,
    revoked          BOOLEAN       NOT NULL DEFAULT false,
    data_types       TEXT,
    recipient_email  VARCHAR(150),
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMP,
    CONSTRAINT fk_shared_reports_patient FOREIGN KEY (patient_id) REFERENCES users(id),
    CONSTRAINT fk_shared_reports_doctor  FOREIGN KEY (doctor_id)  REFERENCES users(id)
);

CREATE TABLE shared_access_logs (
    id                UUID         PRIMARY KEY,
    shared_report_id  UUID         NOT NULL,
    ip_address        VARCHAR(45),
    accessed_data     TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMP,
    CONSTRAINT fk_shared_access_logs_report FOREIGN KEY (shared_report_id) REFERENCES shared_reports(id)
);

CREATE TABLE reports (
    id             UUID          PRIMARY KEY,
    patient_id     UUID          NOT NULL,
    generated_at   TIMESTAMP,
    type           VARCHAR(30)   NOT NULL CHECK (type IN ('FULL', 'ANAMNESIS', 'VACCINES', 'APPOINTMENTS')),
    content_url    VARCHAR(500),
    status         VARCHAR(20)   NOT NULL DEFAULT 'PROCESSING' CHECK (status IN ('PROCESSING', 'COMPLETED', 'ERROR')),
    filter_params  TEXT,
    observations   TEXT,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMP,
    CONSTRAINT fk_reports_patient FOREIGN KEY (patient_id) REFERENCES users(id)
);
