[CmdletBinding()]
param(
    [switch]$RebuildFrontend,
    [switch]$RunMavenTests
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot
$Failures = New-Object System.Collections.Generic.List[string]

function Check([bool]$Condition, [string]$Pass, [string]$Fail) {
    if ($Condition) { Write-Host "[PASS] $Pass" -ForegroundColor Green }
    else { Write-Host "[FAIL] $Fail" -ForegroundColor Red; $script:Failures.Add($Fail) }
}

$Required = @(
    "pom.xml", "docker-compose.yml", ".env.example", "frontend\package.json",
    "auth-service\pom.xml", "doctor-service\pom.xml", "appointment-service\pom.xml",
    "ai-service\pom.xml", "notification-service\pom.xml", "api-gateway\pom.xml"
)
foreach ($file in $Required) { Check (Test-Path (Join-Path $ProjectRoot $file)) "$file exists" "$file is missing" }

Check ([bool](Get-Command docker -ErrorAction SilentlyContinue)) "Docker command found" "Docker Desktop is not installed or not on PATH"
if (Get-Command docker -ErrorAction SilentlyContinue) {
    try { docker info *> $null; Check $true "Docker engine is running" "" }
    catch { Check $false "" "Docker Desktop engine is not running" }
    docker compose config --quiet
    Check ($LASTEXITCODE -eq 0) "Docker Compose configuration is valid" "Docker Compose configuration is invalid"
    if ($LASTEXITCODE -eq 0) { docker compose ps }
}

if ($RebuildFrontend) {
    Check ([bool](Get-Command npm -ErrorAction SilentlyContinue)) "npm command found" "Node.js/npm is missing"
    if (Get-Command npm -ErrorAction SilentlyContinue) {
        Push-Location (Join-Path $ProjectRoot "frontend")
        try {
            npm ci
            if ($LASTEXITCODE -eq 0) { npm run build }
            Check ($LASTEXITCODE -eq 0) "React production build passed" "React production build failed"
        } finally { Pop-Location }
    }
}

if ($RunMavenTests) {
    Check ([bool](Get-Command mvn -ErrorAction SilentlyContinue)) "Maven command found" "Maven 3.9+ is missing"
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        mvn test
        Check ($LASTEXITCODE -eq 0) "Backend Maven tests passed" "Backend Maven tests failed"
    }
}

foreach ($port in 5173,8080) {
    $open = Test-NetConnection -ComputerName localhost -Port $port -InformationLevel Quiet -WarningAction SilentlyContinue
    Check $open "localhost:$port is reachable" "localhost:$port is not reachable"
}

try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Check ($health.status -eq "UP") "API gateway health is UP" "API gateway health is not UP"
} catch { Check $false "" "API gateway health endpoint is unavailable" }

if ($Failures.Count -gt 0) {
    Write-Host ""
    Write-Host "$($Failures.Count) verification check(s) failed." -ForegroundColor Red
    exit 1
}
Write-Host ""
Write-Host "All selected verification checks passed." -ForegroundColor Green
