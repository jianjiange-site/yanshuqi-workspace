# Stage 00-A verification script (PowerShell)
$ErrorActionPreference = "Stop"
$workspace = Resolve-Path (Join-Path $PSScriptRoot "..")

Write-Host "=== Stage 00-A Verification ==="

$requiredDirs = @(
    "$workspace\ai-chat",
    "$workspace\proto",
    "$workspace\deploy",
    "$workspace\scripts",
    "$workspace\docs",
    "$workspace\mobile-gateway",
    "$workspace\user-service",
    "$workspace\match-service",
    "$workspace\im-service",
    "$workspace\post-service",
    "$workspace\payment-service",
    "$workspace\example-service"
)

foreach ($dir in $requiredDirs) {
    if (-not (Test-Path $dir)) { throw "Missing directory: $dir" }
    Write-Host "[OK] $dir"
}

if (Test-Path "$workspace\dating-server") {
    $legacyPoms = Get-ChildItem "$workspace\dating-server" -Recurse -Filter "pom.xml" -ErrorAction SilentlyContinue
    if ($legacyPoms) {
        throw "Legacy directory dating-server/ still contains Maven modules"
    }
    Write-Host "[WARN] Empty legacy dating-server/ directory remains (likely file lock). Delete manually after closing IDE/terminals."
} else {
    Write-Host "[OK] dating-server/ not present"
}

$forbiddenPackages = @("com.chatvibe", "com.dating.yanshuqi")
$javaRoots = @("mobile-gateway","user-service","match-service","im-service","post-service","payment-service","example-service")
foreach ($svc in $javaRoots) {
    Get-ChildItem -Path "$workspace\$svc" -Recurse -Filter "*.java" | ForEach-Object {
        $content = Get-Content $_.FullName -Raw
        foreach ($pkg in $forbiddenPackages) {
            if ($content -match [regex]::Escape($pkg)) {
                throw "Forbidden package reference in $($_.FullName): $pkg"
            }
        }
    }
}
Write-Host "[OK] Java package naming check passed"

$services = @(
    @{ name = "mobile-gateway"; port = 8080 },
    @{ name = "user-service"; port = 8081 },
    @{ name = "match-service"; port = 8082 },
    @{ name = "im-service"; port = 8083 },
    @{ name = "post-service"; port = 8084 },
    @{ name = "payment-service"; port = 8085 },
    @{ name = "example-service"; port = 8086 }
)

foreach ($svc in $services) {
    $healthRoot = Join-Path $workspace "$($svc.name)\src\main\java\com\dating"
    $controller = Get-ChildItem -Path $healthRoot -Recurse -Filter "HealthController.java" -ErrorAction SilentlyContinue
    if (-not $controller) { throw "Missing HealthController for $($svc.name)" }
    Write-Host "[OK] $($svc.name) HealthController exists (port $($svc.port))"
}

if (-not (Test-Path "$workspace\ai-chat\app\main.py")) { throw "Missing ai-chat main.py" }
Write-Host "[OK] ai-chat skeleton exists"

if (-not (Test-Path "$workspace\proto\README.md")) { throw "Missing proto README" }
Write-Host "[OK] proto README exists"

Write-Host "=== Stage 00-A structure verification passed ==="
