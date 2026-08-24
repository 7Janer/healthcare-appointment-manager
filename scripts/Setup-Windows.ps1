[CmdletBinding()]
param(
    [string]$GeminiApiKey = "",
    [string]$AdminEmail = "admin@clinic.local",
    [string]$AdminPassword = "ChangeMe123!",
    [string]$SmtpHost = "",
    [int]$SmtpPort = 587,
    [string]$SmtpUsername = "",
    [string]$SmtpPassword = "",
    [string]$SmtpFrom = "no-reply@clinic.local",
    [string]$GoogleClientId = "",
    [string]$GoogleClientSecret = ""
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

function New-RandomSecret([int]$Bytes = 32) {
    $buffer = New-Object byte[] $Bytes
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($buffer) } finally { $rng.Dispose() }
    return [Convert]::ToBase64String($buffer)
}

function Require-Command([string]$Name, [string]$InstallMessage) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name was not found. $InstallMessage"
    }
}

Require-Command "docker" "Install and start Docker Desktop, then reopen PowerShell."

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw "Docker Desktop is installed but its engine is not running. Start Docker Desktop and wait until it says Engine running."
}

docker compose version *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker Compose v2 is unavailable. Update Docker Desktop." }

$EnvPath = Join-Path $ProjectRoot ".env"
$ExistingEnv = @{}
if (Test-Path $EnvPath) {
    foreach ($line in Get-Content $EnvPath) {
        if ($line -match '^([^#=]+)=(.*)$') { $ExistingEnv[$matches[1].Trim()] = $matches[2] }
    }
    if (-not $PSBoundParameters.ContainsKey("GeminiApiKey") -and $ExistingEnv.ContainsKey("GEMINI_API_KEY")) { $GeminiApiKey = $ExistingEnv["GEMINI_API_KEY"] }
    if (-not $PSBoundParameters.ContainsKey("AdminEmail") -and $ExistingEnv.ContainsKey("ADMIN_EMAIL")) { $AdminEmail = $ExistingEnv["ADMIN_EMAIL"] }
    if (-not $PSBoundParameters.ContainsKey("AdminPassword") -and $ExistingEnv.ContainsKey("ADMIN_PASSWORD")) { $AdminPassword = $ExistingEnv["ADMIN_PASSWORD"] }
    if (-not $PSBoundParameters.ContainsKey("SmtpHost") -and $ExistingEnv.ContainsKey("SMTP_HOST")) { $SmtpHost = $ExistingEnv["SMTP_HOST"] }
    if (-not $PSBoundParameters.ContainsKey("SmtpPort") -and $ExistingEnv.ContainsKey("SMTP_PORT")) { $SmtpPort = [int]$ExistingEnv["SMTP_PORT"] }
    if (-not $PSBoundParameters.ContainsKey("SmtpUsername") -and $ExistingEnv.ContainsKey("SMTP_USERNAME")) { $SmtpUsername = $ExistingEnv["SMTP_USERNAME"] }
    if (-not $PSBoundParameters.ContainsKey("SmtpPassword") -and $ExistingEnv.ContainsKey("SMTP_PASSWORD")) { $SmtpPassword = $ExistingEnv["SMTP_PASSWORD"] }
    if (-not $PSBoundParameters.ContainsKey("SmtpFrom") -and $ExistingEnv.ContainsKey("SMTP_FROM")) { $SmtpFrom = $ExistingEnv["SMTP_FROM"] }
    if (-not $PSBoundParameters.ContainsKey("GoogleClientId") -and $ExistingEnv.ContainsKey("GOOGLE_CLIENT_ID")) { $GoogleClientId = $ExistingEnv["GOOGLE_CLIENT_ID"] }
    if (-not $PSBoundParameters.ContainsKey("GoogleClientSecret") -and $ExistingEnv.ContainsKey("GOOGLE_CLIENT_SECRET")) { $GoogleClientSecret = $ExistingEnv["GOOGLE_CLIENT_SECRET"] }
}

$JwtSecret = if ($ExistingEnv.ContainsKey("JWT_SECRET")) { $ExistingEnv["JWT_SECRET"] } else { New-RandomSecret 48 }
$InternalApiKey = if ($ExistingEnv.ContainsKey("INTERNAL_API_KEY")) { $ExistingEnv["INTERNAL_API_KEY"] } else { New-RandomSecret 48 }
$PostgresPassword = if ($ExistingEnv.ContainsKey("POSTGRES_PASSWORD")) { $ExistingEnv["POSTGRES_PASSWORD"] } else { New-RandomSecret 24 }
$SmtpEnabled = -not [string]::IsNullOrWhiteSpace($SmtpHost)
$DryRun = -not $SmtpEnabled

$EnvText = @"
POSTGRES_USER=clinic
POSTGRES_PASSWORD=$PostgresPassword
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
JWT_SECRET=$JwtSecret
INTERNAL_API_KEY=$InternalApiKey
ADMIN_EMAIL=$AdminEmail
ADMIN_PASSWORD=$AdminPassword
GEMINI_API_KEY=$GeminiApiKey
GEMINI_PRIMARY_MODEL=gemini-2.5-flash
GEMINI_FALLBACK_MODEL=gemini-2.0-flash-lite
SMTP_HOST=$SmtpHost
SMTP_PORT=$SmtpPort
SMTP_USERNAME=$SmtpUsername
SMTP_PASSWORD=$($SmtpPassword.Replace(' ', ''))
SMTP_FROM=$SmtpFrom
SMTP_ENABLED=$($SmtpEnabled.ToString().ToLower())
NOTIFICATION_DRY_RUN=$($DryRun.ToString().ToLower())
GOOGLE_CLIENT_ID=$GoogleClientId
GOOGLE_CLIENT_SECRET=$GoogleClientSecret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/calendar/oauth/callback
GOOGLE_FRONTEND_SUCCESS_URI=http://localhost:5173/calendar-connected
FRONTEND_URL=http://localhost:5173
VITE_API_URL=http://localhost:8080
"@

if (Test-Path $EnvPath) {
    Copy-Item $EnvPath (Join-Path $ProjectRoot ".env.backup") -Force
    Write-Host "Existing .env backed up as .env.backup" -ForegroundColor Yellow
}
$Utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($EnvPath, $EnvText, $Utf8NoBom)

Write-Host "Validating Docker Compose configuration..." -ForegroundColor Cyan
docker compose config --quiet
if ($LASTEXITCODE -ne 0) { throw "Docker Compose configuration validation failed." }

Write-Host "Building and starting CareFlow. The first build may take several minutes..." -ForegroundColor Cyan
docker compose up --build -d
if ($LASTEXITCODE -ne 0) { throw "Docker Compose startup failed. Run: docker compose logs --tail 200" }

Write-Host "Waiting for services..." -ForegroundColor Cyan
for ($attempt = 1; $attempt -le 36; $attempt++) {
    Start-Sleep -Seconds 5
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3
        if ($health.status -eq "UP") { break }
    } catch { }
    if ($attempt -eq 36) {
        docker compose ps
        throw "The gateway did not become healthy within three minutes. Run: docker compose logs --tail 200"
    }
}

docker compose ps
Write-Host ""
Write-Host "CareFlow is running." -ForegroundColor Green
Write-Host "Frontend: http://localhost:5173"
Write-Host "Gateway:  http://localhost:8080"
Write-Host "Admin:    $AdminEmail"
Write-Host "Password: $AdminPassword"
if ([string]::IsNullOrWhiteSpace($GeminiApiKey)) {
    Write-Host "Gemini is not configured; the built-in AI fallback chain will be used." -ForegroundColor Yellow
}
if ($DryRun) {
    Write-Host "Email is in dry-run mode; notifications are logged but not sent." -ForegroundColor Yellow
}
if ([string]::IsNullOrWhiteSpace($GoogleClientId)) {
    Write-Host "Google Calendar is not configured; booking still works normally." -ForegroundColor Yellow
}
