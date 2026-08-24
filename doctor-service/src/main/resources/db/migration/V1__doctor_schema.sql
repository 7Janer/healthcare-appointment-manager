CREATE TABLE doctor_profiles(
 id UUID PRIMARY KEY,user_id UUID NOT NULL UNIQUE,full_name VARCHAR(100) NOT NULL,email VARCHAR(255) NOT NULL,
 specialisation VARCHAR(100) NOT NULL,qualifications VARCHAR(255),working_start TIME NOT NULL,working_end TIME NOT NULL,
 slot_duration_minutes INTEGER NOT NULL CHECK(slot_duration_minutes BETWEEN 10 AND 180),active BOOLEAN NOT NULL DEFAULT TRUE,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_doctor_specialisation ON doctor_profiles(LOWER(specialisation));
CREATE TABLE doctor_leaves(
 id UUID PRIMARY KEY,doctor_id UUID NOT NULL REFERENCES doctor_profiles(id) ON DELETE CASCADE,leave_date DATE NOT NULL,reason VARCHAR(255),created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),UNIQUE(doctor_id,leave_date)
);
