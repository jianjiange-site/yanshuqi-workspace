$ErrorActionPreference = "Stop"
$workspaceRoot = Split-Path -Parent $PSScriptRoot

$services = @(
    "mobile-gateway",
    "user-service",
    "match-service",
    "im-service",
    "post-service",
    "payment-service",
    "example-service"
)

Write-Host "=== Build all Java services (independent Maven projects) ==="

foreach ($service in $services) {
    $serviceDir = Join-Path $workspaceRoot $service
    if (-not (Test-Path $serviceDir)) {
        throw "Service directory not found: $serviceDir"
    }

    Write-Host ""
    Write-Host ">>> Building $service ..."
    Push-Location $serviceDir
    try {
        mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build failed for $service (exit code $LASTEXITCODE)"
        }
    }
    finally {
        Pop-Location
    }
    Write-Host ">>> $service build succeeded"
}

Write-Host ""
Write-Host "=== All services built successfully ==="
