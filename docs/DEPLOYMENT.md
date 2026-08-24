# Deployment Guide

## Suggested free-tier layout

- Vercel: `frontend/`
- Railway or Render: API gateway and five Spring Boot services using their Dockerfiles
- Managed PostgreSQL: one server containing the five databases created by `infra/postgres/init.sql`

## Backend

Create services from this repository. For each service select its matching Dockerfile and keep internal services private. Expose only the API gateway. Set every variable from `.env.example`; replace `*_SERVICE_URL` with private service URLs. Use the same `JWT_SECRET` in auth and gateway, and the same `INTERNAL_API_KEY` in internal services.

Run the SQL in `infra/postgres/init.sql` using a privileged database connection, then give each service its matching JDBC URL. Flyway creates the tables automatically on first startup.

## Frontend

Import the repository into Vercel, set root directory to `frontend`, build command to `npm run build`, output directory to `dist`, and `VITE_API_URL` to the public HTTPS gateway URL. Configure the gateway `FRONTEND_URL` to the Vercel origin.

## Integrations

Set SMTP and Gemini secrets only in the host's secret manager. Register the final HTTPS Google callback and update both Google and `GOOGLE_REDIRECT_URI`. Set `NOTIFICATION_DRY_RUN=false` and `SMTP_ENABLED=true` only after sending a test email.

## Release verification

Check service health, create a patient and doctor, book one slot, confirm only one parallel hold succeeds, verify stored summaries, submit a prescription, inspect notification jobs, connect Calendar for both accounts, and verify create/update/delete behavior. Configure backups, logs, alerts, encryption, privacy controls, and a real secret manager before using real health information.

No live URL is embedded because hosting requires access to the owner's cloud account and secrets.
