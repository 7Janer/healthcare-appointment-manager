# API Documentation

Base URL: `http://localhost:8080`. JSON is used throughout. Except for registration, login, health, and the OAuth callback, send `Authorization: Bearer <token>`.

## Authentication

### Register patient

`POST /api/auth/register`

```json
{"fullName":"Asha Singh","email":"asha@example.com","password":"StrongPass123!"}
```

Returns `201` with `token`, `userId`, `fullName`, `email`, and `role`.

### Login

`POST /api/auth/login`

```json
{"email":"asha@example.com","password":"StrongPass123!"}
```

### Create staff login (admin)

`POST /api/auth/admin/users`

```json
{"fullName":"Dr Neha Rao","email":"neha@clinic.com","password":"TempPass123!","role":"DOCTOR"}
```

## Doctors

- `GET /api/doctors?specialisation=cardio` - active doctor search.
- `GET /api/doctors/{id}` - doctor profile.
- `GET /api/doctors/me` - current doctor's profile.
- `GET /api/doctors/{id}/availability?startAt=2026-09-10T10:00:00+05:30` - internal schedule rule check.
- `GET /api/doctors/{id}/leaves` - upcoming leave dates.
- `POST /api/admin/doctors` - create profile after staff login creation.

```json
{
  "userId":"e9a30d2b-817c-4a7e-bd84-cf2b63d54f60",
  "fullName":"Dr Neha Rao",
  "email":"neha@clinic.com",
  "specialisation":"Cardiology",
  "qualifications":"MBBS, MD",
  "workingStart":"09:00",
  "workingEnd":"17:00",
  "slotDurationMinutes":30
}
```

- `PUT /api/admin/doctors/{id}` - update all profile fields plus `active`.
- `POST /api/admin/doctors/{id}/leaves` - body: `{"date":"2026-09-12","reason":"Conference"}`.
- `DELETE /api/admin/doctors/{id}/leaves/{leaveId}` - remove leave.

## Appointments

### List displayable slots

`GET /api/appointments/available-slots?doctorId=<uuid>&date=2026-09-10`

### Hold one slot

`POST /api/appointments/holds`

```json
{"doctorId":"<uuid>","startAt":"2026-09-10T10:00:00+05:30"}
```

Returns a single-use `holdToken` and five-minute `expiresAt`. A collision returns `409 SLOT_CONFLICT`.

### Confirm booking

`POST /api/appointments`

```json
{
  "holdToken":"<uuid>",
  "patientName":"Asha Singh",
  "symptoms":"Intermittent chest discomfort for two days; worse when climbing stairs."
}
```

### Role-scoped reads

- `GET /api/appointments` - patient sees own, doctor sees assigned, admin sees all.
- `GET /api/appointments/{id}` - authorized participants/admin only.

### Reschedule

First create a new hold for the same doctor, then call `PUT /api/appointments/{id}/reschedule`:

```json
{"holdToken":"<new-hold-uuid>"}
```

### Cancel

`DELETE /api/appointments/{id}?reason=Travel`

### Complete visit (assigned doctor)

`POST /api/appointments/{id}/post-visit`

```json
{
  "clinicalNotes":"Blood pressure stable. Gastritis suspected. Review in one week.",
  "prescriptions":[
    {"medication":"Pantoprazole 40 mg","instructions":"Take one tablet before breakfast","intervalHours":24,"durationDays":7}
  ]
}
```

## Calendar

- `GET /api/calendar/oauth/url` - returns `{ "authorizationUrl": "..." }`.
- `GET /api/calendar/oauth/callback?state=...&code=...` - public Google redirect target.
- `GET /api/calendar/status` - returns `{ "connected": true|false }`.

## Error format

```json
{"timestamp":"2026-08-24T12:00:00Z","code":"SLOT_CONFLICT","message":"Slot is already booked or temporarily held"}
```

Typical statuses: `400` validation, `401` missing/invalid JWT, `403` ownership/role, `404` missing record, `409` slot race, and `503` safe downstream failure.
