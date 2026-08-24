# Google Calendar OAuth 2.0 Setup

1. Open Google Cloud Console and create/select a project.
2. Enable **Google Calendar API**.
3. Configure the OAuth consent screen. Add test users while the app remains in testing mode.
4. Create an **OAuth client ID** of type **Web application**.
5. Add the local authorized redirect URI exactly: `http://localhost:8080/api/calendar/oauth/callback`.
6. Add the deployed HTTPS callback when hosting, for example `https://api.example.com/api/calendar/oauth/callback`.
7. Set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI`, and `GOOGLE_FRONTEND_SUCCESS_URI` in `.env`.
8. Restart the notification service and gateway. Sign in and choose **Connect calendar**.

The app requests only `calendar.events`, offline access, and explicit consent. A separate event is created in each connected participant's primary calendar. Booking creates events, rescheduling updates them, and cancellation or doctor leave deletes them. Jobs use idempotency keys and retry temporary failures.

Never commit the client secret. Use HTTPS in production. Encrypt stored refresh tokens with a managed key service before handling real patient data, and configure token revocation/account deletion procedures.
