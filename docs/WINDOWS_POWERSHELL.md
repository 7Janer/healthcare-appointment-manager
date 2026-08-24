# Windows PowerShell Run Guide

## Fastest path: Docker Desktop

Open PowerShell in the folder containing the downloaded ZIP and run:

```powershell
$Zip = ".\Healthcare_Appointment_Manager_Complete.zip"
$Destination = ".\Healthcare_Appointment_Manager"
if (Test-Path $Destination) { throw "$Destination already exists. Rename or remove it first." }
Expand-Archive -Path $Zip -DestinationPath $Destination
Set-Location "$Destination\healthcare-appointment-manager"
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\scripts\Setup-Windows.ps1
```

The no-key mode is intentional: booking, AI fallback summaries, dry-run notification jobs, and all portals remain usable. Open `http://localhost:5173` and sign in with the admin credentials printed by the script.

## Run with Gemini

Create a Gemini API key in Google AI Studio, then rerun cleanly:

```powershell
docker compose down
.\scripts\Setup-Windows.ps1 -GeminiApiKey "PASTE_YOUR_GEMINI_API_KEY"
```

Never paste the real key into source code or commit `.env`.

## Run with Gmail SMTP

Enable two-step verification on the Gmail account and create a Google App Password. Then run:

```powershell
docker compose down
.\scripts\Setup-Windows.ps1 `
  -GeminiApiKey "PASTE_YOUR_GEMINI_API_KEY" `
  -SmtpHost "smtp.gmail.com" `
  -SmtpPort 587 `
  -SmtpUsername "YOUR_EMAIL@gmail.com" `
  -SmtpPassword "YOUR_16_CHARACTER_APP_PASSWORD" `
  -SmtpFrom "YOUR_EMAIL@gmail.com"
```

Use an App Password, not the normal Gmail password.

## Run with Google Calendar

In Google Cloud Console, enable Google Calendar API, configure the OAuth consent screen, create a Web application OAuth client, and add this exact authorized redirect URI:

```text
http://localhost:8080/api/calendar/oauth/callback
```

Then run:

```powershell
docker compose down
.\scripts\Setup-Windows.ps1 `
  -GeminiApiKey "PASTE_YOUR_GEMINI_API_KEY" `
  -GoogleClientId "PASTE_CLIENT_ID" `
  -GoogleClientSecret "PASTE_CLIENT_SECRET"
```

After login, choose **Connect calendar** separately for the patient and doctor accounts.

## Verification and logs

```powershell
.\scripts\Verify-Windows.ps1
docker compose ps
docker compose logs --tail 200
```

Optional local source builds:

```powershell
.\scripts\Verify-Windows.ps1 -RebuildFrontend
.\scripts\Verify-Windows.ps1 -RunMavenTests
```

The project root is the extracted `healthcare-appointment-manager` folder containing the top-level `pom.xml`. Do not run Maven from the ZIP's parent folder.

## Stop and restart

```powershell
docker compose stop
docker compose start
```

To rebuild after source changes:

```powershell
docker compose up --build -d
```

To stop containers while keeping the database:

```powershell
docker compose down
```

Only when you intentionally want to delete all local CareFlow database data:

```powershell
docker compose down -v
```

## Common fixes

```powershell
# Confirm you are in the correct folder
Get-Location
Test-Path .\pom.xml
Test-Path .\docker-compose.yml

# Check Docker Desktop
docker version
docker compose version
docker info

# Inspect only failing services
docker compose ps
docker compose logs api-gateway --tail 200
docker compose logs appointment-service --tail 200
docker compose logs postgres --tail 200

# Check ports
Get-NetTCPConnection -LocalPort 5173,8080 -ErrorAction SilentlyContinue
```

If port 5173 or 8080 is occupied, stop the conflicting process or change the left side of the relevant `ports` entry in `docker-compose.yml`.
