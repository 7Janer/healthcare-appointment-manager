CREATE TABLE appointments(
 id UUID PRIMARY KEY,patient_id UUID NOT NULL,patient_name VARCHAR(100) NOT NULL,patient_email VARCHAR(255) NOT NULL,
 doctor_id UUID NOT NULL,doctor_user_id UUID NOT NULL,doctor_name VARCHAR(100) NOT NULL,doctor_email VARCHAR(255) NOT NULL,
 start_at TIMESTAMPTZ NOT NULL,duration_minutes INTEGER NOT NULL,status VARCHAR(20) NOT NULL,
 symptoms TEXT NOT NULL,pre_visit_summary TEXT,urgency_level VARCHAR(20),clinical_notes TEXT,prescription_text TEXT,post_visit_summary TEXT,
 reminder_queued BOOLEAN NOT NULL DEFAULT FALSE,cancellation_reason VARCHAR(255),created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_appointments_patient ON appointments(patient_id,start_at DESC);
CREATE INDEX idx_appointments_doctor_user ON appointments(doctor_user_id,start_at DESC);
CREATE INDEX idx_appointments_doctor_time ON appointments(doctor_id,start_at);

CREATE TABLE slot_reservations(
 id UUID PRIMARY KEY,doctor_id UUID NOT NULL,start_at TIMESTAMPTZ NOT NULL,patient_id UUID NOT NULL,hold_token UUID NOT NULL UNIQUE,
 expires_at TIMESTAMPTZ NOT NULL,confirmed BOOLEAN NOT NULL DEFAULT FALSE,appointment_id UUID REFERENCES appointments(id) ON DELETE CASCADE,version BIGINT NOT NULL DEFAULT 0,
 UNIQUE(doctor_id,start_at)
);
CREATE INDEX idx_slot_expiry ON slot_reservations(confirmed,expires_at);

CREATE TABLE prescription_items(
 id UUID PRIMARY KEY,appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,medication VARCHAR(255) NOT NULL,
 instructions VARCHAR(1000) NOT NULL,interval_hours INTEGER NOT NULL,duration_days INTEGER NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE outbox_events(
 id UUID PRIMARY KEY,event_key VARCHAR(255) NOT NULL UNIQUE,target VARCHAR(30) NOT NULL,payload TEXT NOT NULL,status VARCHAR(20) NOT NULL,
 attempts INTEGER NOT NULL DEFAULT 0,next_attempt_at TIMESTAMPTZ NOT NULL,last_error VARCHAR(1000),created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_outbox_due ON outbox_events(status,next_attempt_at);
