# Stage 00-B verification script (PowerShell)
$ErrorActionPreference = "Stop"
$workspace = Resolve-Path (Join-Path $PSScriptRoot "..")

Write-Host "=== Stage 00-B Verification ==="

# Reuse Stage 00-A structure checks
& (Join-Path $PSScriptRoot "verify-stage-00a.ps1")

$requiredFiles = @(
    "$workspace\deploy\postgres\00_create_database.sql",
    "$workspace\deploy\postgres\01_create_schemas.sql",
    "$workspace\deploy\postgres\02_check_connection.sql",
    "$workspace\deploy\nacos\user-service-dev.yaml",
    "$workspace\deploy\minio\create_buckets.md",
    "$workspace\deploy\redis\REDIS_KEYS.md",
    "$workspace\scripts\build-all.ps1"
)

foreach ($file in $requiredFiles) {
    if (-not (Test-Path $file)) { throw "Missing required file: $file" }
    Write-Host "[OK] $file"
}

$javaServices = @("mobile-gateway","user-service","match-service","im-service","post-service","payment-service","example-service")
foreach ($svc in $javaServices) {
    $infraController = Get-ChildItem "$workspace\$svc" -Recurse -Filter "InfraHealthController.java" -ErrorAction SilentlyContinue
    if (-not $infraController) { throw "Missing InfraHealthController in $svc" }
    $appDev = Join-Path $workspace "$svc\src\main\resources\application-dev.yml"
    $bootstrap = Join-Path $workspace "$svc\src\main\resources\bootstrap.yml"
    $content = (Get-Content $appDev -Raw) + (Get-Content $bootstrap -Raw)
    if ($content -notmatch 'yanshuqi-dev') { throw "$svc missing Nacos namespace config" }
    if ($content -notmatch 'dating-yanshuqi') { throw "$svc missing MinIO bucket config" }
    if ($content -notmatch 'yanshuqi_dev') { throw "$svc missing RocketMQ prefix config" }
    if ($content -notmatch 'infra:ping') { throw "$svc missing Redis infra ping key" }
    Write-Host "[OK] $svc infra config template"
}

$imDev = Get-Content "$workspace\im-service\src\main\resources\application-dev.yml" -Raw
if ($imDev -notmatch 'yanshuqi_') { throw "im-service missing OpenIM user id prefix" }
Write-Host "[OK] im-service OpenIM prefix configured"

$otherServices = @("mobile-gateway","user-service","match-service","post-service","payment-service")
foreach ($svc in $otherServices) {
    $content = Get-Content "$workspace\$svc\src\main\resources\application-dev.yml" -Raw
    if ($content -match 'OPENIM_ADMIN_SECRET|openim:') { throw "$svc must not contain OpenIM admin config" }
}
Write-Host "[OK] OpenIM secret only reserved in im-service"

if (-not (Test-Path "$workspace\ai-chat\app\infra_health.py")) { throw "Missing ai-chat infra_health.py" }
Write-Host "[OK] ai-chat infra health module exists"

Write-Host "=== Stage 00-B structure verification passed ==="
Write-Host "Run services with deploy/.env (local, gitignored) and check GET /health/infra for live connectivity."
