# MatchService 阶段 9 最终验收脚本
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$MatchDir = Join-Path $Root "match-service"
$GatewayDir = Join-Path $Root "mobile-gateway"
$DocsDir = Join-Path $Root "docs\match-service"
$Pass = 0
$Fail = 0

function Pass([string]$msg) { Write-Host "[PASS] $msg"; $script:Pass++ }
function Fail([string]$msg) { Write-Host "[FAIL] $msg"; $script:Fail++ }
function Warn([string]$msg) { Write-Host "[WARN] $msg" }

Write-Host "=== MatchService Final Verification (Stage 9) ==="

Write-Host "--- 1. match-service mvn clean test ---"
try {
    Push-Location $MatchDir
    & mvn -q clean test
    Pass "match-service tests"
} catch { Fail "match-service tests" } finally { Pop-Location }

Write-Host "--- 2. match-service mvn clean compile ---"
try {
    Push-Location $MatchDir
    & mvn -q clean compile
    Pass "match-service compile"
} catch { Fail "match-service compile" } finally { Pop-Location }

Write-Host "--- 3. mobile-gateway mvn clean test ---"
try {
    Push-Location $GatewayDir
    & mvn -q clean test
    Pass "mobile-gateway tests"
} catch { Fail "mobile-gateway tests" } finally { Pop-Location }

Write-Host "--- 4. mobile-gateway mvn clean compile ---"
try {
    Push-Location $GatewayDir
    & mvn -q clean compile
    Pass "mobile-gateway compile"
} catch { Fail "mobile-gateway compile" } finally { Pop-Location }

Write-Host "--- 5. proto/match/match_service.proto exists ---"
$Proto = Join-Path $Root "proto\match\match_service.proto"
if (Test-Path $Proto) { Pass "match proto exists" } else { Fail "match proto missing" }

Write-Host "--- 6. no forbidden cross-service imports ---"
$bad = @()
Get-ChildItem -Path (Join-Path $MatchDir "src\main\java") -Recurse -Filter "*.java" | ForEach-Object {
    Select-String -Path $_.FullName -Pattern "import com\.dating\.(user|payment|im)\." | ForEach-Object {
        if ($_.Line -notmatch "grpc\.proto") { $bad += "$($_.Path):$($_.LineNumber):$($_.Line.Trim())" }
    }
}
if ($bad.Count -eq 0) { Pass "no forbidden cross-service imports" } else { $bad | ForEach-Object { Write-Host $_ }; Fail "forbidden imports" }

Write-Host "--- 7. gateway has no Match business logic keywords ---"
$gwSrc = Join-Path $GatewayDir "src\main\java"
$gwHits = @()
if (Test-Path $gwSrc) {
    $gwHits = Get-ChildItem -Path $gwSrc -Recurse -Filter "*.java" |
        Select-String -Pattern "QuotaService|SwipeHistoryManager|MatchCreationService|D1Generator" -ErrorAction SilentlyContinue
}
if ($gwHits) { Fail "gateway contains Match business logic keywords" } else { Pass "gateway clean" }

Write-Host "--- 8. Redis key prefix yanshuqi ---"
$RedisConst = Join-Path $MatchDir "src\main\java\com\dating\match\constant\RedisKeyConstants.java"
$c = Get-Content $RedisConst -Raw
if ($c -match "yanshuqi:match:" -and $c -match "yanshuqi:lock:match:") { Pass "Redis prefix ok" } else { Fail "Redis prefix" }

Write-Host "--- 9. docs/match-service/ files complete ---"
$RequiredDocs = @(
    "00_MATCH_SERVICE_PROJECT_MAP.md", "01_BUSINESS_FLOWS.md", "02_TECH_ARCHITECTURE.md",
    "03_DATA_MODEL.md", "04_API_MAP.md", "05_CALL_CHAIN.md", "06_TECH_DECISIONS.md",
    "07_PROBLEMS_AND_SOLUTIONS.md", "08_ACCEPTANCE_CHECKLIST.md", "09_STAGE_REVIEW.md",
    "10_INTERVIEW_SUMMARY.md"
)
$missing = $RequiredDocs | Where-Object { -not (Test-Path (Join-Path $DocsDir $_)) }
if ($missing.Count -eq 0) { Pass "docs complete ($($RequiredDocs.Count) files)" } else { $missing | ForEach-Object { Write-Host "  missing: $_" }; Fail "docs incomplete" }

Write-Host "--- 10. scan suspected secrets ---"
$secretPattern = "(?i)(password=|token=|secret=|\bak=|\bsk=)"
$secretHits = @()
foreach ($dir in @((Join-Path $MatchDir "src"), (Join-Path $GatewayDir "src"))) {
    if (-not (Test-Path $dir)) { continue }
    Get-ChildItem -Path $dir -Recurse -Include "*.java","*.properties","*.xml" -ErrorAction SilentlyContinue | ForEach-Object {
        Select-String -Path $_.FullName -Pattern $secretPattern -ErrorAction SilentlyContinue | ForEach-Object {
            if ($_.Path -notmatch "application.*\.yml") { $secretHits += "$($_.Path):$($_.LineNumber)" }
        }
    }
}
if ($secretHits.Count -eq 0) { Pass "no suspected hardcoded secrets" } else { $secretHits | ForEach-Object { Write-Host $_ }; Fail "suspected secrets" }

Warn "PostgreSQL Testcontainers integration tests require Docker (MatchManagerIntegrationTest)"

Write-Host "=== Summary: PASS=$Pass FAIL=$Fail ==="
if ($Fail -gt 0) { exit 1 }
