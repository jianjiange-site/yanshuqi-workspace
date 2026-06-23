# mobile-gateway GW-6 final verification (Windows PowerShell)
# Run from repo root: .\scripts\verify-gateway-final.ps1
# No Postgres/Redis/Nacos required; no curl.
$ErrorActionPreference = "Continue"
$Root = Split-Path -Parent $PSScriptRoot
$GwDir = Join-Path $Root "mobile-gateway"
$DocsDir = Join-Path $Root "docs\mobile-gateway"
$Base = Join-Path $GwDir "src\main\java\com\dating\gateway"
$PassCount = 0
$FailCount = 0

function Record-Pass([string]$msg) { Write-Host "[PASS] $msg"; $script:PassCount++ }
function Record-Fail([string]$msg) { Write-Host "[FAIL] $msg"; $script:FailCount++ }

Write-Host "=== mobile-gateway Final Verification (GW-6) ==="
Write-Host "Root: $Root"

Write-Host "--- 1. mobile-gateway directory ---"
if ((Test-Path $GwDir) -and (Test-Path (Join-Path $GwDir "pom.xml"))) {
    Record-Pass "mobile-gateway dir and pom.xml"
} else {
    Record-Fail "mobile-gateway dir or pom.xml missing"
}

Write-Host "--- 2. key Controllers ---"
$Controllers = @(
    "AuthController.java", "ProfileController.java", "UploadController.java",
    "HomeController.java", "MatchController.java", "PostController.java",
    "PaymentController.java", "ImTokenController.java", "OpenImCallbackController.java"
)
$CtrlDir = Join-Path $Base "controller"
$missingCtrl = $Controllers | Where-Object { -not (Test-Path (Join-Path $CtrlDir $_)) }
if ($null -eq $missingCtrl -or $missingCtrl.Count -eq 0) { Record-Pass "9 key Controllers" }
else { $missingCtrl | ForEach-Object { Write-Host "  missing: $_" }; Record-Fail "Controllers incomplete" }

Write-Host "--- 3. key security classes ---"
$SecFiles = @(
    (Join-Path $Base "security\JwtAuthFilter.java"),
    (Join-Path $Base "security\JwtIssuer.java"),
    (Join-Path $Base "security\JwtVerifier.java"),
    (Join-Path $Base "resolver\JwtCallerUserResolver.java")
)
$missingSec = $SecFiles | Where-Object { -not (Test-Path $_) }
if ($null -eq $missingSec -or $missingSec.Count -eq 0) { Record-Pass "security/resolver classes" }
else { $missingSec | ForEach-Object { Write-Host "  missing: $_" }; Record-Fail "security classes missing" }

Write-Host "--- 4. GatewayGrpcMetadataSupport ---"
if (Test-Path (Join-Path $Base "support\GatewayGrpcMetadataSupport.java")) { Record-Pass "GatewayGrpcMetadataSupport" }
else { Record-Fail "GatewayGrpcMetadataSupport missing" }

Write-Host "--- 5. Flyway auth migration ---"
$Flyway = Join-Path $GwDir "src\main\resources\db\migration\V20260623_001__create_gateway_auth_tables.sql"
if ((Test-Path $Flyway) -and ((Get-Content $Flyway -Raw) -match "auth_device") -and ((Get-Content $Flyway -Raw) -match "auth_refresh_token")) {
    Record-Pass "Flyway auth tables script"
} else {
    Record-Fail "Flyway auth script missing or incomplete"
}

Write-Host "--- 6. docs/mobile-gateway (13 markdown files) ---"
$docFiles = @()
if (Test-Path $DocsDir) {
    $docFiles = Get-ChildItem -Path $DocsDir -Filter "*.md" -File
}
$hasMap = Test-Path (Join-Path $DocsDir "00-*.md")
$hasInterview = Test-Path (Join-Path $DocsDir "12-*.md")
if ($docFiles.Count -ge 13 -and $hasMap -and $hasInterview) {
    Record-Pass "docs/mobile-gateway complete ($($docFiles.Count) md files)"
} else {
    Record-Fail "docs/mobile-gateway incomplete (count=$($docFiles.Count), map=$hasMap, interview=$hasInterview)"
}

Write-Host "--- 7. root README MobileGateway section ---"
$RootReadme = Join-Path $Root "README.md"
if ((Test-Path $RootReadme) -and (Select-String -Path $RootReadme -Pattern "MobileGateway" -Quiet)) {
    Record-Pass "root README MobileGateway index"
} else {
    Record-Fail "root README missing MobileGateway section"
}

Write-Host "--- 8. mobile-gateway/README not skeleton-only ---"
$GwReadme = Join-Path $GwDir "README.md"
$gwContent = if (Test-Path $GwReadme) { Get-Content $GwReadme -Raw } else { "" }
if ($gwContent -match "BFF" -and $gwContent -notmatch "Stage 00-A skeleton service") {
    Record-Pass "mobile-gateway/README updated"
} else {
    Record-Fail "mobile-gateway/README still skeleton or missing BFF description"
}

Write-Host "--- 9. Payment/IM not ready documented ---"
$ifaceDoc = Get-ChildItem -Path $DocsDir -Filter "04-*.md" -File -ErrorAction SilentlyContinue | Select-Object -First 1
if ($ifaceDoc -and (Select-String -Path $ifaceDoc.FullName -Pattern "10701" -Quiet) -and (Select-String -Path $ifaceDoc.FullName -Pattern "mock" -Quiet)) {
    Record-Pass "Payment/IM not ready documented"
} else {
    Record-Fail "Payment/IM boundary not in docs"
}

Write-Host "--- 10. proto user/match/post exist ---"
$protoUser = Join-Path $Root "proto\user\user_auth_service.proto"
$protoMatch = Join-Path $Root "proto\match\match_service.proto"
$protoPost = Join-Path $Root "proto\post\post_service.proto"
if ((Test-Path $protoUser) -and (Test-Path $protoMatch) -and (Test-Path $protoPost)) {
    Record-Pass "user/match/post proto exist"
} else {
    Record-Fail "user/match/post proto missing"
}

Write-Host "--- 11. no obvious hardcoded secrets ---"
$srcDir = Join-Path $GwDir "src"
$SecretPatterns = @("BEGIN PRIVATE KEY", "AKIA")
$hits = @()
if (Test-Path $srcDir) {
    Get-ChildItem -Path $srcDir -Recurse -File | ForEach-Object {
        $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
        foreach ($pat in $SecretPatterns) {
            if ($content -and $content.Contains($pat)) {
                $hits += "$($_.FullName): matched $pat"
            }
        }
    }
}
if ($hits.Count -eq 0) { Record-Pass "no obvious secrets in src" }
else { $hits | ForEach-Object { Write-Host $_ }; Record-Fail "possible hardcoded secrets" }

Write-Host "--- 12. GatewayErrorCode payment/im codes ---"
$ErrFile = Join-Path $Base "exception\GatewayErrorCode.java"
if ((Test-Path $ErrFile) -and (Select-String -Path $ErrFile -Pattern "PAYMENT_SERVICE_NOT_READY" -Quiet) -and (Select-String -Path $ErrFile -Pattern "IM_SERVICE_NOT_READY" -Quiet)) {
    Record-Pass "GatewayErrorCode 107xx/108xx"
} else {
    Record-Fail "GatewayErrorCode missing payment/im codes"
}

Write-Host "--- 13. mobile-gateway mvn test ---"
try {
    Push-Location $GwDir
    & mvn -B -ntp -q test
    if ($LASTEXITCODE -eq 0) { Record-Pass "mobile-gateway mvn test" }
    else { Record-Fail "mobile-gateway mvn test (exit $LASTEXITCODE)" }
} catch {
    Record-Fail "mobile-gateway mvn test: $_"
} finally {
    Pop-Location
}

Write-Host "--- 14. verify bash script exists ---"
if (Test-Path (Join-Path $Root "scripts\verify-gateway-final.sh")) { Record-Pass "verify-gateway-final.sh exists" }
else { Record-Fail "verify-gateway-final.sh missing" }

Write-Host ""
Write-Host ("=== Summary: PASS={0} FAIL={1} ===" -f $PassCount, $FailCount)
if ($FailCount -gt 0) { exit 1 }
exit 0
