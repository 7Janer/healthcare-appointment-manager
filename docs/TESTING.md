# Testing Guide

## Automated checks

```bash
mvn test
cd frontend
npm install
npm run build
```

## Concurrent booking test

1. Create one doctor and select one future aligned slot.
2. Authenticate two patients.
3. Send both `POST /api/appointments/holds` requests concurrently for the same doctor/time.
4. Assert exactly one `201 Created` and one `409 SLOT_CONFLICT`.
5. Confirm the winning hold twice; assert the first succeeds and the replay returns `409`.
6. Cancel the appointment, acquire the slot again, and assert it becomes available.

The guarantee comes from PostgreSQL's unique `(doctor_id,start_at)` index and `INSERT ... ON CONFLICT DO NOTHING`, not an in-memory lock.

## Integration scenarios

- Gemini key missing: booking succeeds and `generatedBy` becomes `RULES` or `APPOINTMENT_LOCAL_RULES`.
- SMTP unavailable: notification job moves through retry states without rolling back booking.
- Doctor leave with bookings: appointments become `CANCELLED`, reservations are released, and outbox rows exist.
- Calendar disconnected: email still sends; Calendar job retries and records failure independently.
- Reschedule: old slot becomes free, new hold is consumed, and Calendar action is `UPDATE`.
- Post-visit: exact prescription persists, summary persists, and one medication plan is created per item.
