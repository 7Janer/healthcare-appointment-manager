# Database Schema

The platform uses database-per-service ownership. Cross-service identifiers are UUID references by contract rather than physical foreign keys across databases.

## `auth_db`

- `user_accounts`: UUID, name, unique email, BCrypt hash, role, active flag, creation time.

## `doctor_db`

- `doctor_profiles`: auth `user_id`, identity, specialisation, qualifications, working interval, slot duration, active flag.
- `doctor_leaves`: doctor/date unique pair and reason.

## `appointment_db`

- `appointments`: patient and doctor snapshots, time/duration/status, symptoms, stored pre/post summaries, clinician notes, prescription text, reminder flag, optimistic version.
- `slot_reservations`: unique `(doctor_id, start_at)`, patient, unique hold token, expiry, confirmation, appointment reference. This table is the booking mutex.
- `prescription_items`: exact medicine, instructions, interval hours, and duration days entered by the doctor.
- `outbox_events`: idempotent event key, target, JSON payload, retry state, next attempt, and last error.

## `ai_db`

- `ai_summary_cache`: unique prompt type/input SHA-256 pair, structured JSON payload, source, timestamp. Raw API keys are never stored.

## `notification_db`

- `notification_jobs`: idempotent channel jobs with retry state.
- `oauth_credentials`: per-user Google access/refresh token and expiry. A production system should additionally use application-level envelope encryption/KMS for token columns.
- `oauth_states`: ten-minute OAuth anti-CSRF state.
- `calendar_links`: Google event ID per user/appointment.
- `medication_plans`: interval, next due time, end time, and active state.

All schema creation is versioned by Flyway; JPA uses `ddl-auto: validate` so code cannot silently mutate production schema.
